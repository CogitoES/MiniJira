package com.cogito.minijira.service;

import com.cogito.minijira.domain.Project;
import com.cogito.minijira.common.dto.ProjectRequest;
import com.cogito.minijira.repository.ProjectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

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

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

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

    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }
}
