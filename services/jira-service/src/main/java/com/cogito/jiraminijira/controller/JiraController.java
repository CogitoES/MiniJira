package com.cogito.jiraminijira.controller;

import com.cogito.jiraminijira.service.JiraService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/jira")
public class JiraController {

    private final JiraService jiraService;

    public JiraController(JiraService jiraService) {
        this.jiraService = jiraService;
    }

    @PostMapping("/export/project/{projectId}")
    public ResponseEntity<Void> exportProject(@PathVariable Long projectId) {
        // TODO: Fetch project details from Project Service via REST
        try {
            // jiraService.exportProject(project);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
