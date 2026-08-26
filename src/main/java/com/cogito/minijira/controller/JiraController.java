package com.cogito.minijira.controller;

import com.cogito.minijira.domain.Project;
import com.cogito.minijira.repository.ProjectRepository;
import com.cogito.minijira.service.JiraService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/jira")
public class JiraController {

    private final JiraService jiraService;
    private final ProjectRepository projectRepository;

    public JiraController(JiraService jiraService, ProjectRepository projectRepository) {
        this.jiraService = jiraService;
        this.projectRepository = projectRepository;
    }

    @PostMapping("/export/project/{projectId}")
    public ResponseEntity<Void> exportProject(@PathVariable Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        try {
            jiraService.exportProject(project);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
