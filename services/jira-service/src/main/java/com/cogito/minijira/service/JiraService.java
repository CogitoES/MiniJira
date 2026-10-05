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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JiraService {

    private static final Logger logger = LoggerFactory.getLogger(JiraService.class);

    // API Constants
    private static final String JIRA_API_BASE = "/rest/api/3";
    private static final String JIRA_PROJECT_ENDPOINT = JIRA_API_BASE + "/project/";
    private static final String JIRA_ISSUE_ENDPOINT = JIRA_API_BASE + "/issue/";
    private static final String JIRA_COMMENT_ENDPOINT = "/comment/";

    // ADF Constants
    private static final String ADF_TYPE_DOC = "doc";
    private static final String ADF_TYPE_PARAGRAPH = "paragraph";
    private static final String ADF_TYPE_TEXT = "text";
    private static final int ADF_VERSION = 1;

    // Header Constants
    private static final String HEADER_INTERNAL_SECRET = "X-Internal-Service-Secret";
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String AUTH_BASIC_PREFIX = "Basic ";

    @Value("${jira.url}")
    private String jiraUrl;

    @Value("${jira.email}")
    private String jiraEmail;

    @Value("${jira.api.token}")
    private String jiraApiToken;

    @Value("${project.service.url}")
    private String projectServiceUrl;

    @Value("${task.service.url}")
    private String taskServiceUrl;

    @Value("${comment.service.url}")
    private String commentServiceUrl;

    @Value("${app.internal.secret}")
    private String internalSecret;

    private final RestTemplate restTemplate;
    private HttpHeaders cachedJiraHeaders;

    @Autowired
    public JiraService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

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

        for (TaskDto task : tasks) {
            logger.debug("Exporting task: {} (JiraKey: {})", task.getTitle(), task.getJiraKey());

            // Update Task (Issue) in JIRA if jiraKey is present
            if (isValidJiraKey(task.getJiraKey())) {
                try {
                    updateJiraIssue(task);
                    logger.debug("Successfully updated task in JIRA: {}", task.getJiraKey());
                } catch (Exception e) {
                    logger.error("Failed to update task in JIRA: {}", task.getJiraKey(), e);
                    throw e;
                }
            }

            List<CommentDto> comments = fetchComments(task.getId());
            if (comments == null) {
                comments = Collections.emptyList();
            }

            for (CommentDto comment : comments) {
                logger.debug("Exporting comment for task: {}", task.getJiraKey());

                // Update Comment in JIRA if both jiraKeys are present
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

    private boolean isValidJiraKey(String jiraKey) {
        return jiraKey != null && !jiraKey.isEmpty();
    }

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

    private HttpHeaders getInternalServiceHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HEADER_INTERNAL_SECRET, internalSecret);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

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
