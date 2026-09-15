package com.cogito.minijira.controller;

import com.cogito.minijira.domain.Project;
import com.cogito.minijira.common.dto.ProjectRequest;
import com.cogito.minijira.service.ProjectService;
import com.cogito.minijira.security.UserPrincipal;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);
    private final ProjectService projectService;
    private final RestTemplate restTemplate = new RestTemplate();

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    private void validateUserExists(Long userId) {
        try {
            Boolean exists = restTemplate.getForObject("http://localhost:8081/auth/exists/" + userId, Boolean.class);
            if (exists == null || !exists) {
                throw new RuntimeException("User does not exist");
            }
        } catch (Exception e) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid user session");
        }
    }

    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getProject(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Project> updateProject(@PathVariable Long id, @Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.ok(projectService.updateProject(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<Project> createProject(@Valid @RequestBody ProjectRequest request) {
        logger.info("Received request to create project: {}", request.getName());

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isInternal = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_INTERNAL"));

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        Long userId = principal.getUserId();

        // Bypass user-existence check only for internal requests
        if (!isInternal) {
            validateUserExists(userId);
        }
        
        return ResponseEntity.ok(projectService.createProject(request, userId));
    }
}
