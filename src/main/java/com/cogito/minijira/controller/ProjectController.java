package com.cogito.minijira.controller;

import com.cogito.minijira.domain.Project;
import com.cogito.minijira.domain.User;
import com.cogito.minijira.dto.ProjectRequest;
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
    private final UserRepository userRepository;

    public ProjectController(ProjectService projectService, UserRepository userRepository) {
        this.projectService = projectService;
        this.userRepository = userRepository;
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
// ...

        logger.info("Received request to create project: {} for user: {}", request.getName(), userDetails.getUsername());
        
        return userRepository.findByEmail(userDetails.getUsername())
                .map(owner -> ResponseEntity.ok(projectService.createProject(request, owner)))
                .orElseGet(() -> {
                    logger.warn("User not found with email: {}", userDetails.getUsername());
                    return ResponseEntity.status(401).build();
                });
    }
}
