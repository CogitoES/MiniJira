package com.cogito.minijira.controller;

import com.cogito.minijira.client.AuthClient;
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

import java.util.List;

/**
 * Project Controller - REST endpoints for project management
 * 
 * Provides endpoints for creating, retrieving, updating, and deleting projects.
 * All endpoints require authentication except for retrieval operations.
 */
@RestController
@RequestMapping("/projects")
public class ProjectController {

    // ========== Logger ==========

    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);

    // ========== Dependencies ==========

    private final ProjectService projectService;
    private final AuthClient authClient;

    /**
     * Constructs a ProjectController with required dependencies
     * 
     * @param projectService the project business logic service
     * @param authClient the Feign client for auth service communication
     */
    public ProjectController(ProjectService projectService, AuthClient authClient) {
        this.projectService = projectService;
        this.authClient = authClient;
    }

    // ========== Private Methods ==========

    /**
     * Validates that a user exists in the auth service
     * 
     * @param userId the user ID to validate
     * @throws org.springframework.web.server.ResponseStatusException if user does not exist
     */
    private void validateUserExists(Long userId) {
        try {
            Boolean exists = authClient.exists(userId);
            if (exists == null || !exists) {
                throw new RuntimeException("User does not exist");
            }
        } catch (Exception e) {
            logger.error("Failed to validate user existence for ID: {}", userId, e);
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid user session");
        }
    }

    // ========== REST Endpoints ==========

    /**
     * Retrieves all projects
     * 
     * @return a list of all projects
     */
    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    /**
     * Retrieves a specific project by ID
     * 
     * @param id the project ID
     * @return the project details
     */
    @GetMapping("/{id}")
    public ResponseEntity<Project> getProject(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    /**
     * Updates an existing project
     * 
     * @param id the project ID to update
     * @param request the updated project data
     * @return the updated project
     */
    @PutMapping("/{id}")
    public ResponseEntity<Project> updateProject(@PathVariable Long id, @Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.ok(projectService.updateProject(id, request));
    }

    /**
     * Deletes a project
     * 
     * @param id the project ID to delete
     * @return no content response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Creates a new project
     * 
     * @param request the project creation data
     * @return the created project
     */
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
