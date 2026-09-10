package com.cogito.minijira.controller;

import com.cogito.minijira.common.dto.ProjectRequest;
import com.cogito.minijira.domain.Project;
import com.cogito.minijira.service.ProjectService;
import com.cogito.minijira.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ProjectControllerTest {

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private ProjectController projectController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllProjects_ShouldReturnList() {
        when(projectService.getAllProjects()).thenReturn(Collections.singletonList(new Project()));

        ResponseEntity<List<Project>> response = projectController.getAllProjects();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        verify(projectService, times(1)).getAllProjects();
    }

    @Test
    void updateProject_ShouldReturnUpdatedProject() {
        Long id = 1L;
        ProjectRequest request = new ProjectRequest();
        Project updatedProject = new Project();
        when(projectService.updateProject(id, request)).thenReturn(updatedProject);

        ResponseEntity<Project> response = projectController.updateProject(id, request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(updatedProject, response.getBody());
        verify(projectService, times(1)).updateProject(id, request);
    }

    @Test
    void deleteProject_ShouldReturnNoContent() {
        Long id = 1L;

        ResponseEntity<Void> response = projectController.deleteProject(id);

        assertEquals(204, response.getStatusCode().value());
        verify(projectService, times(1)).deleteProject(id);
    }

    @Test
    void createProject_ShouldReturnCreatedProject() {
        // Mock Auth
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        UserPrincipal principal = new UserPrincipal("testuser", 123L);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        ProjectRequest request = new ProjectRequest();
        request.setName("Test Project");
        Project createdProject = new Project();
        createdProject.setName("Test Project");
        
        when(projectService.createProject(request, 123L)).thenReturn(createdProject);

        ResponseEntity<Project> response = projectController.createProject(request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(createdProject, response.getBody());
        verify(projectService, times(1)).createProject(request, 123L);
        
        // Cleanup
        SecurityContextHolder.clearContext();
    }
}
