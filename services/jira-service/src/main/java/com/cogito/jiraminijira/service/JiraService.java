package com.cogito.jiraminijira.service;

import com.cogito.minijira.domain.Project;
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

    public void exportProject(Project project) {
        HttpHeaders headers = getHeaders();
        
        // Note: Task/Comment fetching needs to be updated to call other services via REST
        // This is now decoupled from local Repositories.
        
        // ... (Keep the logic for interacting with Jira API)
        // ... (Update Task/Comment references to call REST endpoints of Task/Comment Services)
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
