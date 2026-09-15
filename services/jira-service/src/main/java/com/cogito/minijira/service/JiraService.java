package com.cogito.minijira.service;

import com.cogito.minijira.common.dto.CommentDto;
import com.cogito.minijira.common.dto.ProjectDto;
import com.cogito.minijira.common.dto.TaskDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.List;

@Service
public class JiraService {

    private static final Logger logger = LoggerFactory.getLogger(JiraService.class);

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

    private final RestTemplate restTemplate = new RestTemplate();

    public void exportProject(Long projectId) {
        logger.info("Starting export for project ID: {}", projectId);

        logger.info("Fetching project...");
        ProjectDto project = fetchProject(projectId);
        logger.info("Project fetched: {}", project);

        if (project == null) {
            logger.error("Project not found: {}", projectId);
            throw new IllegalArgumentException("Project not found: " + projectId);
        }

        logger.info("Fetching tasks...");
        List<TaskDto> tasks = fetchTasks(projectId);
        logger.info("Tasks fetched: {}", tasks);

        if (tasks == null) {
            logger.warn("No tasks found for project: {}", projectId);
            tasks = java.util.Collections.emptyList();
        }

        logger.info("Exporting project: {} (JiraKey: {})", project.getName(), project.getJiraKey());

        // 1. Update Project in JIRA if jiraKey is present
        if (project.getJiraKey() != null && !project.getJiraKey().isEmpty()) {
            try {
                logger.info("Updating JIRA project: {}", project.getJiraKey());
                updateJiraProject(project);
                logger.info("Successfully updated project in JIRA: {}", project.getJiraKey());
            } catch (Exception e) {
                logger.error("Failed to update project in JIRA: {}", project.getJiraKey(), e);
                throw e; // Re-throw to see where it fails in the controller
            }
        } else {
            logger.info("No JiraKey found, skipping JIRA project update");
        }

        for (TaskDto task : tasks) {
            logger.info("Exporting task: {} (JiraKey: {})", task.getTitle(), task.getJiraKey());
            
            // 2. Update Task (Issue) in JIRA if jiraKey is present
            if (task.getJiraKey() != null && !task.getJiraKey().isEmpty()) {
                try {
                    updateJiraIssue(task);
                    logger.info("Successfully updated task in JIRA: {}", task.getJiraKey());
                } catch (Exception e) {
                    logger.error("Failed to update task in JIRA: {}", task.getJiraKey(), e);
                }
            }

            List<CommentDto> comments = fetchComments(task.getId());
            if (comments == null) {
                logger.warn("No comments found for task: {}", task.getId());
                comments = java.util.Collections.emptyList();
            }
            
            for (CommentDto comment : comments) {
                logger.info("Exporting comment: {} (JiraKey: {})", comment.getText(), comment.getJiraKey());
                
                // 3. Update Comment in JIRA if jiraKey and task's jiraKey are present
                if (comment.getJiraKey() != null && !comment.getJiraKey().isEmpty() && task.getJiraKey() != null) {
                    try {
                        updateJiraComment(task.getJiraKey(), comment);
                        logger.info("Successfully updated comment in JIRA: {}", comment.getJiraKey());
                    } catch (Exception e) {
                        logger.error("Failed to update comment in JIRA: {}", comment.getJiraKey(), e);
                    }
                }
            }
        }
    }

    private void updateJiraProject(ProjectDto project) {
        String url = jiraUrl + "/rest/api/3/project/" + project.getJiraKey();
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("name", project.getName());
        body.put("description", project.getDescription());

        HttpEntity<java.util.Map<String, Object>> entity = new HttpEntity<>(body, getJiraHeaders());
        restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
    }

    private void updateJiraIssue(TaskDto task) {
        String url = jiraUrl + "/rest/api/3/issue/" + task.getJiraKey();
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        java.util.Map<String, Object> fields = new java.util.HashMap<>();
        fields.put("summary", task.getTitle());
        fields.put("description", convertToAdf(task.getDescription()));
        body.put("fields", fields);

        HttpEntity<java.util.Map<String, Object>> entity = new HttpEntity<>(body, getJiraHeaders());
        restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
    }

    private void updateJiraComment(String taskJiraKey, CommentDto comment) {
        String url = jiraUrl + "/rest/api/3/issue/" + taskJiraKey + "/comment/" + comment.getJiraKey();
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("body", convertToAdf(comment.getText()));

        HttpEntity<java.util.Map<String, Object>> entity = new HttpEntity<>(body, getJiraHeaders());
        restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
    }

    private java.util.Map<String, Object> convertToAdf(String text) {
        java.util.Map<String, Object> adf = new java.util.HashMap<>();
        adf.put("type", "doc");
        adf.put("version", 1);
        
        java.util.Map<String, Object> paragraph = new java.util.HashMap<>();
        paragraph.put("type", "paragraph");
        
        java.util.Map<String, Object> textNode = new java.util.HashMap<>();
        textNode.put("type", "text");
        textNode.put("text", text != null ? text : "");
        
        paragraph.put("content", java.util.Collections.singletonList(textNode));
        adf.put("content", java.util.Collections.singletonList(paragraph));
        return adf;
    }

    private ProjectDto fetchProject(Long projectId) {
        String url = projectServiceUrl + "/projects/" + projectId;
        logger.info("Fetching project from: {}", url);
        try {
            return restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(getInternalServiceHeaders()),
                    ProjectDto.class
            ).getBody();
        } catch (Exception e) {
            logger.error("Error fetching project from {}: {}", url, e.getMessage());
            throw e;
        }
    }

    private List<TaskDto> fetchTasks(Long projectId) {
        String url = taskServiceUrl + "/projects/" + projectId + "/tasks";
        logger.info("Fetching tasks from: {}", url);
        try {
            return restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(getInternalServiceHeaders()),
                    new ParameterizedTypeReference<List<TaskDto>>() {}
            ).getBody();
        } catch (Exception e) {
            logger.error("Error fetching tasks from {}: {}", url, e.getMessage());
            throw e;
        }
    }

    private List<CommentDto> fetchComments(Long taskId) {
        String url = commentServiceUrl + "/tasks/" + taskId + "/comments";
        logger.info("Fetching comments from: {}", url);
        try {
            return restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(getInternalServiceHeaders()),
                    new ParameterizedTypeReference<List<CommentDto>>() {}
            ).getBody();
        } catch (Exception e) {
            logger.error("Error fetching comments from {}: {}", url, e.getMessage());
            throw e;
        }
    }

    private HttpHeaders getInternalServiceHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Internal-Service-Secret", internalSecret);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private HttpHeaders getJiraHeaders() {
        HttpHeaders headers = new HttpHeaders();
        String auth = jiraEmail + ":" + jiraApiToken;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        headers.set("Authorization", "Basic " + encodedAuth);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
