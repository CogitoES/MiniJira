package com.cogito.minijira.controller;

import com.cogito.minijira.client.AuthClient;
import com.cogito.minijira.domain.Task;
import com.cogito.minijira.repository.TaskRepository;
import com.cogito.minijira.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * TaskController - REST API for Task Management
 * 
 * Provides REST endpoints for CRUD operations on tasks.
 * Handles task creation, retrieval, updating, and deletion.
 * Integrates with authentication to validate user permissions.
 */
@RestController
public class TaskController {

    private final TaskRepository taskRepository;
    private final AuthClient authClient;

    /**
     * Constructor with dependency injection
     * @param taskRepository Repository for task data access
     * @param authClient Client for authentication service communication
     */
    public TaskController(TaskRepository taskRepository, AuthClient authClient) {
        this.taskRepository = taskRepository;
        this.authClient = authClient;
    }

    /**
     * Validates that a user exists in the authentication service
     * Throws ResponseStatusException if user validation fails
     * @param userId The ID of the user to validate
     * @throws ResponseStatusException with UNAUTHORIZED status if validation fails
     */
    private void validateUserExists(Long userId) {
        try {
            Boolean exists = authClient.exists(userId);
            if (exists == null || !exists) {
                throw new RuntimeException("User does not exist");
            }
        } catch (Exception e) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid user session");
        }
    }

    /**
     * Retrieves all tasks for a specific project
     * @param projectId The ID of the project
     * @return ResponseEntity containing list of tasks
     */
    @GetMapping("/projects/{projectId}/tasks")
    public ResponseEntity<List<Task>> getTasksByProjectId(@PathVariable Long projectId) {
        return ResponseEntity.ok(taskRepository.findByProjectId(projectId));
    }

    /**
     * Creates a new task or updates existing task if already present
     * 
     * Handles duplicate detection using jiraKey or title.
     * Sets the reporter ID and project ID from security context and path variable.
     * @param projectId The ID of the project this task belongs to
     * @param task The task details to create
     * @return ResponseEntity containing the created or updated task
     */
    @PostMapping("/projects/{projectId}/tasks")
    public ResponseEntity<Task> createTask(@PathVariable Long projectId, @RequestBody Task task) {
        // Check for existing task by jiraKey or title
        Task existingTask = null;
        if (task.getJiraKey() != null) {
            existingTask = taskRepository.findByJiraKey(task.getJiraKey()).orElse(null);
        }
        if (existingTask == null) {
            existingTask = taskRepository.findByTitle(task.getTitle()).orElse(null);
        }

        if (existingTask != null) {
            // Update existing task with new details
            existingTask.setJiraKey(task.getJiraKey());
            existingTask.setTitle(task.getTitle());
            existingTask.setDescription(task.getDescription());
            existingTask.setStatus(task.getStatus());
            existingTask.setPriority(task.getPriority());
            task = existingTask;
        } else {
            // Create new task with project ID and reporter
            task.setProjectId(projectId);
            
            // Extract user from security context
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean isInternal = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_INTERNAL"));

            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            Long userId = principal.getUserId();

            // Validate user exists (skip for internal service calls)
            if (!isInternal) {
                validateUserExists(userId);
            }
            task.setReporterId(userId);
        }

        return ResponseEntity.ok(taskRepository.save(task));
    }

    /**
     * Updates an existing task with new details
     * @param taskId The ID of the task to update
     * @param taskDetails The updated task details
     * @return ResponseEntity containing the updated task, or 404 if task not found
     */
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

    /**
     * Deletes a task by its ID
     * @param taskId The ID of the task to delete
     * @return ResponseEntity with no content on success, or 404 if task not found
     */
    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            return ResponseEntity.notFound().build();
        }
        taskRepository.deleteById(taskId);
        return ResponseEntity.noContent().build();
    }
}
