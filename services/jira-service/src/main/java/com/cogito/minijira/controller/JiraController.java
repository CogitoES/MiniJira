package com.cogito.minijira.controller;

import com.cogito.minijira.service.JiraService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/jira")
public class JiraController {

    static final Logger logger = LoggerFactory.getLogger(JiraController.class);
    private final JiraService jiraService;

    public JiraController(JiraService jiraService) {
        this.jiraService = jiraService;
    }

    @PostMapping("/export/project/{projectId}")
    public ResponseEntity<Void> exportProject(@PathVariable Long projectId) {
        try {
            jiraService.exportProject(projectId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            JiraController.logger.error("Failed to export project: {}", projectId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
