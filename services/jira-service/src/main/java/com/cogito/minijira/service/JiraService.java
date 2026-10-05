package com.cogito.minijira.service;

import com.cogito.minijira.common.dto.CommentDto;
import com.cogito.minijira.common.dto.ProjectDto;
import com.cogito.minijira.common.dto.TaskDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JiraService - Integration with Atlassian Jira API
 * 
 * Provides functionality to export projects, tasks, and comments to Jira,
 * synchronizing data between MiniJira and Jira instances. Handles Jira API
 * communication with proper authentication and Atlassian Document Format (ADF).
 */
@Service
public class JiraService {

    // ========== Logging ==========

    private static final Logger logger = LoggerFactory.getLogger(JiraService.class);

    // ========== API Constants ==========

    /** Base path for Jira REST API v3 */
    private static final String JIRA_API_BASE = "/rest/api/3";
    
    /** Jira project endpoint path */
    private static final String JIRA_PROJECT_ENDPOINT = JIRA_API_BASE + "/project/";
    
    /** Jira issue endpoint path */
    private static final String JIRA_ISSUE_ENDPOINT = JIRA_API_BASE + "/issue/";
    
    /** Jira comment endpoint path */
    private static final String JIRA_COMMENT_ENDPOINT = "/comment/";

    // ========== ADF Constants ==========

    /** Atlassian Document Format (ADF) type for document root */
    private static final String ADF_TYPE_DOC = "doc";
    
    /** Atlassian Document Format (ADF) type for paragraph */
    private static final String ADF_TYPE_PARAGRAPH = "paragraph";
    
    /** Atlassian Document Format (ADF) type for text */
    private static final String ADF_TYPE_TEXT = "text";
    
    /** Atlassian Document Format (ADF) version */
    private static final int ADF_VERSION = 1;

    // ========== HTTP Header Constants ==========

    /** Header name for internal service authentication secret */
    private static final String HEADER_INTERNAL_SECRET = "X-Internal-Service-Secret";
    
    /** Standard HTTP Authorization header name */
    private static final String HEADER_AUTHORIZATION = "Authorization";
    
    /** Authorization scheme prefix for Basic authentication */
    private static final String AUTH_BASIC_PREFIX = "Basic ";

    // ========== Configuration Properties ==========

    /** Jira instance URL from configuration */
    @Value("${jira.url}")
    private String jiraUrl;

    /** Jira account email for API authentication */
    @Value("${jira.email}")
    private String jiraEmail;

    /** Jira API token for authentication */
    @Value("${jira.api.token}")
    private String jiraApiToken;

    /** Project Service URL for inter-service communication */
    @Value("${project.service.url}")
    private String projectServiceUrl;

    /** Task Service URL for inter-service communication */
    @Value("${task.service.url}")
    private String taskServiceUrl;

    /** Comment Service URL for inter-service communication */
    @Value("${comment.service.url}")
    private String commentServiceUrl;

    /** Internal service secret for service-to-service authentication */
    @Value("${app.internal.secret}")
    private String internalSecret;

    // ========== Dependencies ==========

    /** REST template for HTTP communications */
    private final RestTemplate restTemplate;
    
    /** Cached Jira HTTP headers to avoid repeated encoding */
    private HttpHeaders cachedJiraHeaders;

    /**
     * Constructs JiraService with required dependencies
     * 
     * @param restTemplate the REST template for HTTP requests
     */
    @Autowired
    public JiraService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // ========== Export Operations ==========

    /**
     * Exports a project and all its tasks and comments to Jira
     * 
     * Fetches project data from local services and synchronizes with Jira,
     * updating project information, all tasks/issues, and their comments.
     * 
     * @param projectId the ID of the project to export
     * @throws IllegalArgumentException if project not found
     * @throws RuntimeException if Jira API calls fail
     */
    public void exportProject(Long projectId) {
        logger.info("Starting export for project ID: {}", projectId);

        ProjectDto project = fetchProject(projectId);
        if (project == null) {
            logger.error("Project not found: {}", projectId);
            throw new IllegalArgumentException("Project not found: " + projectId);
        }

        List<TaskDto> tasks = fetchTasks(projectId);
        if (tasks == null) {
            tasks = Collections.emptyList();
        }

        logger.info("Exporting project: {} (JiraKey: {})", project.getName(), project.getJiraKey());

        // Update Project in JIRA if jiraKey is present
        if (isValidJiraKey(project.getJiraKey())) {
            try {
                updateJiraProject(project);
                logger.info("Successfully updated project in JIRA: {}", project.getJiraKey());
            } catch (Exception e) {
                logger.error("Failed to update project in JIRA: {}", project.getJiraKey(), e);
                throw e;
            }
        }

        // Export all tasks associated with the project
        for (TaskDto task : tasks) {
            logger.debug("Exporting task: {} (JiraKey: {})", task.getTitle(), task.getJiraKey());

            if (isValidJiraKey(task.getJiraKey())) {
                try {
                    updateJiraIssue(task);
                    logger.debug("Successfully updated task in JIRA: {}", task.getJiraKey());
                } catch (Exception e) {
                    logger.error("Failed to update task in JIRA: {}", task.getJiraKey(), e);
                    throw e;
                }
            }

            // Export all comments for the task
            List<CommentDto> comments = fetchComments(task.getId());
            if (comments == null) {
                comments = Collections.emptyList();
            }

            for (CommentDto comment : comments) {
                logger.debug("Exporting comment for task: {}", task.getJiraKey());

                if (isValidJiraKey(comment.getJiraKey()) && isValidJiraKey(task.getJiraKey())) {
                    try {
                        updateJiraComment(task.getJiraKey(), comment);
                        logger.debug("Successfully updated comment in JIRA: {}", comment.getJiraKey());
                    } catch (Exception e) {
                        logger.error("Failed to update comment in JIRA: {}", comment.getJiraKey(), e);
                        throw e;
                    }
                }
            }
        }

        logger.info("Export completed for project ID: {}", projectId);
    }

    // ========== Jira Update Operations ==========

    /**
     * Updates a project in Jira with current data
     * 
     * @param project the project data to update
     * @throws RuntimeException if the Jira API request fails
     */
    private void updateJiraProject(ProjectDto project) {
        String url = jiraUrl + JIRA_PROJECT_ENDPOINT + project.getJiraKey();
        Map<String, Object> body = new HashMap<>();
        body.put("name", project.getName());
        body.put("description", project.getDescription());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, getJiraHeaders());
        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to update JIRA project. Status: " + response.getStatusCode());
        }
    }

    /**
     * Updates a task/issue in Jira with current data
     * 
     * @param task the task data to update
     * @throws RuntimeException if the Jira API request fails
     */
    private void updateJiraIssue(TaskDto task) {
        String url = jiraUrl + JIRA_ISSUE_ENDPOINT + task.getJiraKey();
        Map<String, Object> body = new HashMap<>();
        Map<String, Object> fields = new HashMap<>();
        fields.put("summary", task.getTitle());
        fields.put("description", convertToAdf(task.getDescription()));
        body.put("fields", fields);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, getJiraHeaders());
        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to update JIRA issue. Status: " + response.getStatusCode());
        }
    }

    /**
     * Updates a comment in Jira with current data
     * 
     * @param taskJiraKey the Jira key of the task/issue
     * @param comment the comment data to update
     * @throws RuntimeException if the Jira API request fails
     */
    private void updateJiraComment(String taskJiraKey, CommentDto comment) {
        String url = jiraUrl + JIRA_ISSUE_ENDPOINT + taskJiraKey + JIRA_COMMENT_ENDPOINT + comment.getJiraKey();
        Map<String, Object> body = new HashMap<>();
        body.put("body", convertToAdf(comment.getText()));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, getJiraHeaders());
        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to update JIRA comment. Status: " + response.getStatusCode());
        }
    }

    // ========== Data Fetching Operations ==========

    /**
     * Fetches a project from the Project Service
     * 
     * @param projectId the project ID to fetch
     * @return the project DTO, or null if not found
     */
    private ProjectDto fetchProject(Long projectId) {
        String url = projectServiceUrl + "/projects/" + projectId;
        logger.debug("Fetching project from: {}", url);
        try {
            return restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(getInternalServiceHeaders()),
                    ProjectDto.class
            ).getBody();
        } catch (Exception e) {
            logger.error("Error fetching project from {}: {}", url, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Fetches all tasks for a project from the Task Service
     * 
     * @param projectId the project ID
     * @return list of task DTOs
     */
    private List<TaskDto> fetchTasks(Long projectId) {
        String url = taskServiceUrl + "/projects/" + projectId + "/tasks";
        logger.debug("Fetching tasks from: {}", url);
        try {
            return restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(getInternalServiceHeaders()),
                    new ParameterizedTypeReference<List<TaskDto>>() {}
            ).getBody();
        } catch (Exception e) {
            logger.error("Error fetching tasks from {}: {}", url, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Fetches all comments for a task from the Comment Service
     * 
     * @param taskId the task ID
     * @return list of comment DTOs
     */
    private List<CommentDto> fetchComments(Long taskId) {
        String url = commentServiceUrl + "/tasks/" + taskId + "/comments";
        logger.debug("Fetching comments from: {}", url);
        try {
            return restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(getInternalServiceHeaders()),
                    new ParameterizedTypeReference<List<CommentDto>>() {}
            ).getBody();
        } catch (Exception e) {
            logger.error("Error fetching comments from {}: {}", url, e.getMessage(), e);
            throw e;
        }
    }

    // ========== Utility Methods ==========

    /**
     * Validates if a Jira key string is non-null and non-empty
     * 
     * @param jiraKey the Jira key to validate
     * @return true if the key is valid, false otherwise
     */
    private boolean isValidJiraKey(String jiraKey) {
        return jiraKey != null && !jiraKey.isEmpty();
    }

    /**
     * Converts plain text to Atlassian Document Format (ADF)
     * 
     * Creates an ADF document with a single paragraph containing the provided text.
     * 
     * @param text the plain text to convert
     * @return a Map representing the ADF structure
     */
    private Map<String, Object> convertToAdf(String text) {
        String safeText = text != null ? text : "";
        
        Map<String, Object> adf = new HashMap<>();
        adf.put("type", ADF_TYPE_DOC);
        adf.put("version", ADF_VERSION);

        Map<String, Object> paragraph = new HashMap<>();
        paragraph.put("type", ADF_TYPE_PARAGRAPH);

        Map<String, Object> textNode = new HashMap<>();
        textNode.put("type", ADF_TYPE_TEXT);
        textNode.put("text", safeText);

        paragraph.put("content", Collections.singletonList(textNode));
        adf.put("content", Collections.singletonList(paragraph));
        return adf;
    }

    // ========== HTTP Header Builders ==========

    /**
     * Builds HTTP headers for internal service-to-service communication
     * 
     * @return HttpHeaders configured with internal service secret
     */
    private HttpHeaders getInternalServiceHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HEADER_INTERNAL_SECRET, internalSecret);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    /**
     * Builds HTTP headers for Jira API authentication
     * 
     * Uses cached headers if available to avoid repeated Base64 encoding.
     * Includes Basic Authentication with Jira email and API token.
     * 
     * @return HttpHeaders configured with Jira authentication
     */
    private HttpHeaders getJiraHeaders() {
        if (cachedJiraHeaders != null) {
            return cachedJiraHeaders;
        }

        HttpHeaders headers = new HttpHeaders();
        String auth = jiraEmail + ":" + jiraApiToken;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        headers.set(HEADER_AUTHORIZATION, AUTH_BASIC_PREFIX + encodedAuth);
        headers.setContentType(MediaType.APPLICATION_JSON);

        cachedJiraHeaders = headers;
        return cachedJiraHeaders;
    }
}
