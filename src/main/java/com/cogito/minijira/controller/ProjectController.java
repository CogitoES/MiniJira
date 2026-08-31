package com.cogito.minijira.controller;

import com.cogito.minijira.domain.Project;
import com.cogito.minijira.domain.User;
import com.cogito.minijira.common.dto.ProjectRequest;
import com.cogito.minijira.repository.UserRepository;
import com.cogito.minijira.service.ProjectService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import java.util.List;

// ... (previous imports)

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
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
public ResponseEntity<Project> createProject(@Valid @RequestBody ProjectRequest request, 
                                            @AuthenticationPrincipal UserDetails userDetails) {

        logger.info("Received request to create project: {} for user: {}", request.getName(), userDetails.getUsername());
        
        // TODO: Resolve userId from AuthService via API call using userDetails.getUsername()
        Long userId = 1L; // Placeholder for now
        return ResponseEntity.ok(projectService.createProject(request, userId));
    }
}
