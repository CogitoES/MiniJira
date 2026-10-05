package com.cogito.minijira.controller;

import com.cogito.minijira.service.JiraService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Jira Controller - REST endpoints for Jira integration
 * 
 * Provides endpoints for exporting projects to Jira.
 */
@RestController
@RequestMapping("/jira")
public class JiraController {

    // ========== Logger ==========

    static final Logger logger = LoggerFactory.getLogger(JiraController.class);

    // ========== Dependencies ==========

    private final JiraService jiraService;

    /**
     * Constructs a JiraController with required dependencies
     * 
     * @param jiraService the Jira service for integration operations
     */
    public JiraController(JiraService jiraService) {
        this.jiraService = jiraService;
    }

    // ========== REST Endpoints ==========

    /**
     * Exports a project to Jira
     * 
     * @param projectId the project ID to export
     * @return success response if export succeeds, error response otherwise
     */
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
