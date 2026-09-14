package com.cogito.minijira.service;

import com.cogito.minijira.common.dto.CommentDto;
import com.cogito.minijira.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class JiraSyncService {

    private static final Logger logger = LoggerFactory.getLogger(JiraSyncService.class);

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

    public void syncAll(String userJwt) {
        logger.info("Starting JIRA synchronization");

        HttpHeaders internalHeaders = getInternalServiceHeaders();
        internalHeaders.setContentType(MediaType.APPLICATION_JSON);

        List<JiraProjectDto> projects = fetchProjectsFromJira();
        for (JiraProjectDto projectDto : projects) {
            // 1. Create project in project-service
            logger.info("Syncing project: {}", projectDto.getName());
            
            Long projectId = null;
            try {
                com.cogito.minijira.common.dto.ProjectRequest projectRequest = new com.cogito.minijira.common.dto.ProjectRequest();
                projectRequest.setName(projectDto.getName());
                projectRequest.setDescription(projectDto.getDescription());
                projectRequest.setStatus("ACTIVE");

                HttpEntity<com.cogito.minijira.common.dto.ProjectRequest> entity = new HttpEntity<>(projectRequest, internalHeaders);
                logger.info("Sending request to {} with headers: {}", projectServiceUrl + "/projects", entity.getHeaders());
                
                ResponseEntity<com.cogito.minijira.common.dto.ProjectDto> projectResponse = restTemplate.postForEntity(
                        projectServiceUrl + "/projects",
                        entity,
                        com.cogito.minijira.common.dto.ProjectDto.class
                );
                projectId = projectResponse.getBody().getId();
                logger.info("Project created/synchronized: {} (ID: {})", projectDto.getName(), projectId);
            } catch (Exception e) {
                logger.error("Failed to sync project: {}", projectDto.getName(), e);
            }

            if (projectId != null) {
                List<JiraIssueDto> issues = fetchIssuesFromJira(projectDto.getKey());
                for (JiraIssueDto issueDto : issues) {
                    // 2. Create task in task-service
                    try {
                        com.cogito.minijira.common.dto.TaskDto task = new com.cogito.minijira.common.dto.TaskDto();
                        task.setTitle(issueDto.getFields().getSummary());
                        task.setStatus(issueDto.getFields().getStatus() != null ? issueDto.getFields().getStatus().getName() : "OPEN");
                        task.setPriority(issueDto.getFields().getPriority() != null ? issueDto.getFields().getPriority().getName() : "MEDIUM");
                        task.setJiraKey(issueDto.getKey());

                        // POST the task
                        ResponseEntity<com.cogito.minijira.common.dto.TaskDto> taskResponse = restTemplate.postForEntity(
                                taskServiceUrl + "/projects/" + projectId + "/tasks",
                                new HttpEntity<>(task, internalHeaders),
                                com.cogito.minijira.common.dto.TaskDto.class
                        );
                        Long taskId = taskResponse.getBody().getId(); // Assuming TaskDto has an ID
                        logger.info("Task synchronized: {} (ID: {})", issueDto.getKey(), taskId);

                        // 3. Sync comments for the task
                        List<JiraCommentDto> comments = fetchCommentsFromJira(issueDto.getKey());
                        for (JiraCommentDto commentDto : comments) {
                            try {
                                CommentDto comment = new CommentDto();
                                comment.setText(extractTextFromAdf(commentDto.getBody()));
                                comment.setJiraKey(commentDto.getId());
                                
                                restTemplate.postForEntity(
                                        commentServiceUrl + "/tasks/" + taskId + "/comments",
                                        new HttpEntity<>(comment, internalHeaders),
                                        CommentDto.class
                                );
                                logger.info("Comment synchronized: {}", commentDto.getId());
                            } catch (Exception e) {
                                logger.error("Failed to sync comment: {}", commentDto.getId(), e);
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Failed to sync task: {}", issueDto.getKey(), e);
                    }
                }
            }
        }
        logger.info("JIRA synchronization completed");
    }

    @SuppressWarnings("unchecked")
    private String extractTextFromAdf(Object adf) {
        if (!(adf instanceof java.util.Map)) {
            return adf != null ? adf.toString() : "";
        }
        java.util.Map<String, Object> map = (java.util.Map<String, Object>) adf;
        
        StringBuilder sb = new StringBuilder();
        if (map.containsKey("text")) {
            sb.append(map.get("text"));
        }
        if (map.containsKey("content")) {
            Object content = map.get("content");
            if (content instanceof java.util.List) {
                for (Object item : (java.util.List<Object>) content) {
                    sb.append(extractTextFromAdf(item));
                }
            }
        }
        return sb.toString();
    }

    private HttpHeaders getInternalServiceHeaders() {
        HttpHeaders headers = new HttpHeaders();
        logger.info("Setting internal secret header: {}", internalSecret);
        headers.set("X-Internal-Service-Secret", internalSecret);
        return headers;
    }

    public List<JiraProjectDto> fetchProjectsFromJira() {
        ResponseEntity<JiraProjectDto[]> response = restTemplate.exchange(
                jiraUrl + "/rest/api/3/project",
                HttpMethod.GET,
                new HttpEntity<>(getJiraHeaders()),
                JiraProjectDto[].class
        );
        return List.of(response.getBody());
    }

    public List<JiraIssueDto> fetchIssuesFromJira(String projectKey) {
        List<JiraIssueDto> allIssues = new ArrayList<>();

        int startAt = 0;
        int maxResults = 100;

        while (true) {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromUriString(jiraUrl)
                    .path("/rest/api/3/search/jql")
                    .queryParam("jql", "project = " + projectKey)
                    .queryParam("maxResults", maxResults)
                    .queryParam("startAt", startAt)
                    .queryParam("fields", "summary,comment");;

            URI uri = builder
                    .build()
                    .encode()
                    .toUri();

            logger.info("Jira uri: {}", uri);
            // Capture raw response to debug deserialization
            ResponseEntity<String> rawResponse = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    new HttpEntity<>(getJiraHeaders()),
                    String.class
            );

            logger.info("Jira API response status: {}", rawResponse.getStatusCode());
            logger.info("Jira API raw response body: {}", rawResponse.getBody());

            ResponseEntity<JiraSearchResponse> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    new HttpEntity<>(getJiraHeaders()),
                    JiraSearchResponse.class
            );

            logger.info("Jira API response status: {}", response.getStatusCode());
            JiraSearchResponse body = response.getBody();
            logger.info("Jira API response body: {}", body);

            if (body == null || body.getIssues() == null || body.getIssues().isEmpty()) {
                logger.warn("Jira API response body is null, or issues list is empty/null. Body: {}", body);
                break;
            }

            allIssues.addAll(body.getIssues());

            logger.info(
                    "Fetched {} issues from Jira, total: {}",
                    body.getIssues().size(),
                    allIssues.size()
            );

            if (startAt + body.getIssues().size() >= body.getTotal()) {
                break;
            }

            startAt += body.getIssues().size();
        }
        logger.info(
                "Fetched total {} issues from Jira",
                allIssues.size()
        );
        return allIssues;
    }

    public List<JiraCommentDto> fetchCommentsFromJira(String issueKey) {
        ResponseEntity<JiraCommentResponse> response = restTemplate.exchange(
                jiraUrl + "/rest/api/3/issue/" + issueKey + "/comment",
                HttpMethod.GET,
                new HttpEntity<>(getJiraHeaders()),
                JiraCommentResponse.class
        );
        return response.getBody().getComments();
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
