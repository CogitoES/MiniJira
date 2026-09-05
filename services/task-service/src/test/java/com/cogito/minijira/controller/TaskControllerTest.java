package com.cogito.minijira.controller;

import com.cogito.minijira.domain.Task;
import com.cogito.minijira.repository.TaskRepository;
import com.cogito.minijira.service.UserService;
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

class TaskControllerTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserService userService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TaskController taskController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getTasksByProjectId_ShouldReturnTasks() {
        Long projectId = 1L;
        Task task = new Task();
        when(taskRepository.findByProjectId(projectId)).thenReturn(Collections.singletonList(task));

        ResponseEntity<List<Task>> response = taskController.getTasksByProjectId(projectId);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        verify(taskRepository, times(1)).findByProjectId(projectId);
    }

    @Test
    void createTask_ShouldReturnCreatedTask() {
        Long projectId = 1L;
        Task task = new Task();
        String username = "testUser";
        Long userId = 100L;

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(username);
        when(userService.getUserIdByUsername(username)).thenReturn(userId);
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        ResponseEntity<Task> response = taskController.createTask(projectId, task);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(userId, task.getReporterId());
        verify(taskRepository, times(1)).save(task);
    }

    @Test
    void deleteTask_WhenTaskExists_ShouldReturnNoContent() {
        Long taskId = 1L;
        when(taskRepository.existsById(taskId)).thenReturn(true);

        ResponseEntity<Void> response = taskController.deleteTask(taskId);

        assertEquals(204, response.getStatusCode().value());
        verify(taskRepository, times(1)).deleteById(taskId);
    }

    @Test
    void deleteTask_WhenTaskDoesNotExist_ShouldReturnNotFound() {
        Long taskId = 1L;
        when(taskRepository.existsById(taskId)).thenReturn(false);

        ResponseEntity<Void> response = taskController.deleteTask(taskId);

        assertEquals(404, response.getStatusCode().value());
        verify(taskRepository, never()).deleteById(taskId);
    }
}
