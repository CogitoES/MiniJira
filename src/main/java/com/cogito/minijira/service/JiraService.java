package com.cogito.minijira.service;

import com.cogito.minijira.domain.Project;
import com.cogito.minijira.repository.ProjectRepository;
import com.cogito.minijira.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JiraService {

    private static final Logger logger = LoggerFactory.getLogger(JiraService.class);

    @Value("${jira.url}")
    private String jiraUrl;

    @Value("${jira.email}")
    private String jiraEmail;

    @Value("${jira.api-token}")
    private String jiraApiToken;

    @Value("${jira.lead-account-id}")
    private String jiraLeadAccountId;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    public JiraService(ProjectRepository projectRepository, TaskRepository taskRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
    }

    public void exportProject(Project project) {
        HttpHeaders headers = getHeaders();
        
        // Try to find if project already exists in JIRA
        String existingKey = findExistingProjectKey(project.getName(), headers);
        if (existingKey != null) {
            project.setJiraKey(existingKey);
            projectRepository.save(project);
        }

        if (project.getJiraKey() != null && !project.getJiraKey().isEmpty()) {
            updateProject(project, headers);
        } else {
            createProject(project, headers);
        }
    }

    private String findExistingProjectKey(String projectName, HttpHeaders headers) {
        String url = jiraUrl + "/rest/api/3/project";
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        
        try {
            List<Map<String, Object>> projects = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity, List.class).getBody();
            if (projects != null) {
                for (Map<String, Object> p : projects) {
                    if (projectName.equals(p.get("name"))) {
                        return (String) p.get("key");
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error checking existing projects in Jira", e);
        }
        return null;
    }

    private void createProject(Project project, HttpHeaders headers) {
        String url = jiraUrl + "/rest/api/3/project";
        
        Map<String, Object> jiraProject = new HashMap<>();
        String projectKey = project.getName().substring(0, Math.min(project.getName().length(), 10)).toUpperCase().replaceAll("\\s", "");
        jiraProject.put("key", projectKey);
        jiraProject.put("name", project.getName());
        jiraProject.put("projectTypeKey", "software");
        jiraProject.put("leadAccountId", jiraLeadAccountId); 

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(jiraProject, headers);
        
        logger.info("Sending project creation request to Jira for project: {} (Key: {})", project.getName(), projectKey);
        
        try {
            restTemplate.postForObject(url, entity, String.class);
            
            project.setJiraKey(projectKey);
            projectRepository.save(project);
            
            logger.info("Project created successfully. Exporting {} tasks...", project.getTasks().size());
            
            for (com.cogito.minijira.domain.Task task : project.getTasks()) {
                exportTask(task, projectKey, headers);
            }
        } catch (Exception e) {
            logger.error("Error creating project in Jira", e);
            throw e;
        }
    }

    private void updateProject(Project project, HttpHeaders headers) {
        String url = jiraUrl + "/rest/api/3/project/" + project.getJiraKey();
        
        Map<String, Object> jiraProject = new HashMap<>();
        jiraProject.put("name", project.getName());
        jiraProject.put("description", project.getDescription());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(jiraProject, headers);
        
        logger.info("Sending project update request to Jira for project: {} (Key: {})", project.getName(), project.getJiraKey());
        
        try {
            restTemplate.put(url, entity);
            
            logger.info("Project updated successfully. Updating {} tasks...", project.getTasks().size());
            
            // Fetch existing issues for this project to map summaries to keys
            Map<String, String> existingTasks = fetchExistingTasks(project.getJiraKey(), headers);
            logger.info("Existing tasks fetched from JIRA: {}", existingTasks);
            
            for (com.cogito.minijira.domain.Task task : project.getTasks()) {
                String existingKey = existingTasks.get(task.getTitle());
                if (existingKey != null) {
                    task.setJiraKey(existingKey);
                    taskRepository.save(task);
                }
                exportTask(task, project.getJiraKey(), headers);
            }
        } catch (Exception e) {
            logger.error("Error updating project in Jira", e);
            throw e;
        }
    }

    private Map<String, String> fetchExistingTasks(String projectKey, HttpHeaders headers) {
        String url = jiraUrl + "/rest/api/3/search/jql?jql=project=\"" + projectKey + "\"&fields=summary";
        logger.info("Fetching existing tasks from Jira URL: {}", url);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        Map<String, String> taskMap = new HashMap<>();

        try {
            Map<String, Object> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity, Map.class).getBody();
            if (response != null && response.containsKey("issues")) {
                List<Map<String, Object>> issues = (List<Map<String, Object>>) response.get("issues");
                for (Map<String, Object> issue : issues) {
                    Map<String, Object> fields = (Map<String, Object>) issue.get("fields");
                    if (fields != null) {
                        String summary = (String) fields.get("summary");
                        String key = (String) issue.get("key");
                        if (summary != null && key != null) {
                            taskMap.put(summary, key);
                            logger.info("Mapped JIRA task: {} -> {}", summary, key);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching existing tasks from Jira", e);
        }
        return taskMap;
    }


    private void exportTask(com.cogito.minijira.domain.Task task, String projectKey, HttpHeaders headers) {
        if (task.getJiraKey() != null) {
            updateTask(task, headers);
        } else {
            createTask(task, projectKey, headers);
        }
        exportComments(task, headers);
    }

    private void exportComments(com.cogito.minijira.domain.Task task, HttpHeaders headers) {
        String url = jiraUrl + "/rest/api/3/issue/" + task.getJiraKey() + "/comment";
        for (com.cogito.minijira.domain.Comment comment : task.getComments()) {
            Map<String, Object> body = new HashMap<>();
            body.put("body", Map.of(
                    "type", "doc",
                    "version", 1,
                    "content", new Object[]{
                            Map.of("type", "paragraph", "content", new Object[]{
                                    Map.of("type", "text", "text", comment.getText())
                            })
                    }
            ));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            try {
                restTemplate.postForObject(url, entity, Map.class);
                logger.info("Exported comment for task: {}", task.getJiraKey());
            } catch (Exception e) {
                logger.error("Error exporting comment for task: {}", task.getJiraKey(), e);
            }
        }
    }

    private void createTask(com.cogito.minijira.domain.Task task, String projectKey, HttpHeaders headers) {
        String url = jiraUrl + "/rest/api/3/issue";
        
        Map<String, Object> issue = new HashMap<>();
        Map<String, Object> fields = new HashMap<>();
        fields.put("project", Map.of("key", projectKey));
        fields.put("summary", task.getTitle());
        fields.put("description", Map.of(
                "type", "doc",
                "version", 1,
                "content", new Object[]{
                        Map.of("type", "paragraph", "content", new Object[]{
                                Map.of("type", "text", "text", task.getDescription() != null ? task.getDescription() : "")
                        })
                }
        ));
        fields.put("issuetype", Map.of("name", "Task"));
        issue.put("fields", fields);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(issue, headers);
        
        logger.info("Exporting task: {}", task.getTitle());
        Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
        if (response != null && response.containsKey("key")) {
            task.setJiraKey((String) response.get("key"));
            taskRepository.save(task);
        }
    }

    private void updateTask(com.cogito.minijira.domain.Task task, HttpHeaders headers) {
        String url = jiraUrl + "/rest/api/3/issue/" + task.getJiraKey();
        
        Map<String, Object> issue = new HashMap<>();
        Map<String, Object> fields = new HashMap<>();
        fields.put("summary", task.getTitle());
        fields.put("description", Map.of(
                "type", "doc",
                "version", 1,
                "content", new Object[]{
                        Map.of("type", "paragraph", "content", new Object[]{
                                Map.of("type", "text", "text", task.getDescription() != null ? task.getDescription() : "")
                        })
                }
        ));
        // Priority requires the name field in Jira
        fields.put("priority", task.getPriority());
        
        issue.put("fields", fields);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(issue, headers);
        
        logger.info("Updating task: {} (Jira Key: {})", task.getTitle(), task.getJiraKey());
        restTemplate.put(url, entity);
        
        // Status updates often require transition IDs, so we'll skip direct mapping for now 
        // to avoid API errors if transitions are not configured.
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        String auth = jiraEmail + ":" + jiraApiToken;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        headers.set("Authorization", "Basic " + encodedAuth);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
