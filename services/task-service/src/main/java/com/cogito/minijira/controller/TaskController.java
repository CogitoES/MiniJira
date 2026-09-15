package com.cogito.minijira.controller;

import com.cogito.minijira.domain.Task;
import com.cogito.minijira.repository.TaskRepository;
import com.cogito.minijira.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
public class TaskController {

    private final TaskRepository taskRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public TaskController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
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

    @GetMapping("/projects/{projectId}/tasks")
    public ResponseEntity<List<Task>> getTasksByProjectId(@PathVariable Long projectId) {
        return ResponseEntity.ok(taskRepository.findByProjectId(projectId));
    }

    @PostMapping("/projects/{projectId}/tasks")
    public ResponseEntity<Task> createTask(@PathVariable Long projectId, @RequestBody Task task) {
        Task existingTask = null;
        if (task.getJiraKey() != null) {
            existingTask = taskRepository.findByJiraKey(task.getJiraKey()).orElse(null);
        }
        if (existingTask == null) {
            existingTask = taskRepository.findByTitle(task.getTitle()).orElse(null);
        }

        if (existingTask != null) {
            existingTask.setJiraKey(task.getJiraKey());
            existingTask.setTitle(task.getTitle());
            existingTask.setDescription(task.getDescription());
            existingTask.setStatus(task.getStatus());
            existingTask.setPriority(task.getPriority());
            // Preserve reporter and project id
            task = existingTask;
        } else {
            task.setProjectId(projectId);
            
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean isInternal = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_INTERNAL"));

            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            Long userId = principal.getUserId();

            if (!isInternal) {
                validateUserExists(userId);
            }
            task.setReporterId(userId);
        }

        return ResponseEntity.ok(taskRepository.save(task));
    }

    @PutMapping("/tasks/{taskId}")
    public ResponseEntity<Task> updateTask(@PathVariable Long taskId, @RequestBody Task taskDetails) {
        return taskRepository.findById(taskId)
                .map(task -> {
                    task.setTitle(taskDetails.getTitle());
                    task.setDescription(taskDetails.getDescription());
                    task.setStatus(taskDetails.getStatus());
                    task.setPriority(taskDetails.getPriority());
                    return ResponseEntity.ok(taskRepository.save(task));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            return ResponseEntity.notFound().build();
        }
        taskRepository.deleteById(taskId);
        return ResponseEntity.noContent().build();
    }
}
