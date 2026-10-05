package com.cogito.minijira.service;

import com.cogito.minijira.domain.Project;
import com.cogito.minijira.common.dto.ProjectRequest;
import com.cogito.minijira.repository.ProjectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Project Service - Business logic for project management
 * 
 * Handles project creation, retrieval, update, and deletion operations,
 * including synchronization with Jira projects.
 */
@Service
@Transactional
public class ProjectService {

    // ========== Dependencies ==========

    private final ProjectRepository projectRepository;

    /**
     * Constructs a ProjectService with required dependencies
     * 
     * @param projectRepository the project repository for data access
     */
    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    // ========== Project Operations ==========

    /**
     * Creates a new project or updates an existing one
     * 
     * If a project with the same Jira key or name exists, it will be updated.
     * For new projects, the owner ID is set from the provided parameter.
     * 
     * @param request the project creation/update request
     * @param ownerId the owner ID for new projects
     * @return the created or updated project
     */
    public Project createProject(ProjectRequest request, Long ownerId) {
        Project project;
        
        // Find existing project by jiraKey if provided
        if (request.getJiraKey() != null) {
            project = projectRepository.findByJiraKey(request.getJiraKey())
                    .orElse(projectRepository.findByName(request.getName()).orElse(new Project()));
        } else {
            project = projectRepository.findByName(request.getName()).orElse(new Project());
        }

        // If it's a new project, set ownerId
        if (project.getId() == null) {
            project.setOwnerId(ownerId);
        }

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStatus(request.getStatus());
        project.setJiraKey(request.getJiraKey());
        
        return projectRepository.save(project);
    }

    /**
     * Retrieves all projects
     * 
     * @return a list of all projects
     */
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    /**
     * Retrieves a specific project by ID
     * 
     * @param id the project ID
     * @return the project details
     * @throws ResponseStatusException if project is not found
     */
    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
    }

    /**
     * Updates an existing project
     * 
     * @param id the project ID to update
     * @param request the updated project data
     * @return the updated project
     * @throws ResponseStatusException if project is not found or name conflicts exist
     */
    public Project updateProject(Long id, ProjectRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        
        if (!project.getName().equals(request.getName()) && projectRepository.existsByName(request.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Project with this name already exists");
        }
        
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStatus(request.getStatus());
        return projectRepository.save(project);
    }

    /**
     * Deletes a project by ID
     * 
     * @param id the project ID to delete
     */
    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }
}
