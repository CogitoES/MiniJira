package com.cogito.minijira.service;

import com.cogito.minijira.domain.Project;
import com.cogito.minijira.domain.Task;
import com.cogito.minijira.domain.User;
import com.cogito.minijira.repository.ProjectRepository;
import com.cogito.minijira.repository.TaskRepository;
import com.cogito.minijira.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void createTask_ShouldSaveTask() {
        Project project = new Project();
        User reporter = new User();
        Task task = new Task();
        
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(reporter));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        Task created = taskService.create(1L, task, reporter.getId());

        assertNotNull(created);
        verify(taskRepository).save(any(Task.class));
    }
}
