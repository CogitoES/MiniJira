package com.cogito.jiraminijira.service;

import com.cogito.jiraminijira.dto.*;
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

    private final RestTemplate restTemplate = new RestTemplate();

    public void syncAll(String userJwt) {
        logger.info("Starting JIRA synchronization");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userJwt);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

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

                ResponseEntity<com.cogito.minijira.common.dto.ProjectDto> projectResponse = restTemplate.postForEntity(
                        projectServiceUrl + "/projects",
                        new HttpEntity<>(projectRequest, headers),
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
                        // task.setDescription(issueDto.getFields().getDescription().toString());
                        task.setStatus(issueDto.getFields().getStatus() != null ? issueDto.getFields().getStatus().getName() : "OPEN");
                        task.setPriority(issueDto.getFields().getPriority() != null ? issueDto.getFields().getPriority().getName() : "MEDIUM");
                        task.setJiraKey(issueDto.getKey());

                        restTemplate.postForEntity(
                                taskServiceUrl + "/projects/" + projectId + "/tasks",
                                new HttpEntity<>(task, headers),
                                com.cogito.minijira.common.dto.TaskDto.class
                        );
                        logger.info("Task synchronized: {}", issueDto.getKey());
                    } catch (Exception e) {
                        logger.error("Failed to sync task: {}", issueDto.getKey(), e);
                    }

                    List<JiraCommentDto> comments = fetchCommentsFromJira(issueDto.getKey());
                    for (JiraCommentDto commentDto : comments) {
                        // 3. Create comment in comment-service
                        // TODO: Call comment-service POST /tasks/{taskId}/comments
                        logger.info("Syncing comment: {}", commentDto.getId());
                    }
                }
            }
        }
        logger.info("JIRA synchronization completed");
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
