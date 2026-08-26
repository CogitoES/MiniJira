package com.cogito.minijira.service;

import com.cogito.minijira.domain.Project;
import com.cogito.minijira.domain.User;
import com.cogito.minijira.dto.ProjectRequest;
import com.cogito.minijira.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectService projectService;

    private ProjectRequest projectRequest;
    private User owner;

    @BeforeEach
    void setUp() {
        projectRequest = new ProjectRequest();
        projectRequest.setName("New Project");
        projectRequest.setDescription("Description");
        projectRequest.setStatus("OPEN");
        owner = new User();
    }

    @Test
    void createProject_ShouldSaveProject() {
        when(projectRepository.existsByName(anyString())).thenReturn(false);
        when(projectRepository.save(any(Project.class))).thenAnswer(i -> i.getArguments()[0]);

        Project created = projectService.createProject(projectRequest, owner);

        assertNotNull(created);
        assertEquals("New Project", created.getName());
        verify(projectRepository, times(1)).save(any(Project.class));
    }

    @Test
    void createProject_Exists_ShouldThrowException() {
        when(projectRepository.existsByName("New Project")).thenReturn(true);
        assertThrows(ResponseStatusException.class, () -> projectService.createProject(projectRequest, owner));
    }

    @Test
    void updateProject_ShouldUpdateProject() {
        Project existing = new Project();
        existing.setName("Old Name");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(projectRepository.save(any(Project.class))).thenAnswer(i -> i.getArguments()[0]);

        Project updated = projectService.updateProject(1L, projectRequest);

        assertEquals("New Project", updated.getName());
        verify(projectRepository, times(1)).save(existing);
    }
}
