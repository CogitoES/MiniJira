package com.cogito.minijira.service;

import com.cogito.minijira.domain.Project;
import com.cogito.minijira.domain.Task;
import com.cogito.minijira.domain.User;
import com.cogito.minijira.repository.ProjectRepository;
import com.cogito.minijira.repository.TaskRepository;
import com.cogito.minijira.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
    }

    public List<Task> getByProjectId(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        return project.getTasks();
    }

    public Task create(Long projectId, Task task, Long reporterId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        
        task.setProject(project);
        task.setReporterId(reporterId);
        return taskRepository.save(task);
    }

    public Task update(Long taskId, Task taskDetails) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        task.setTitle(taskDetails.getTitle());
        task.setDescription(taskDetails.getDescription());
        task.setStatus(taskDetails.getStatus());
        task.setPriority(taskDetails.getPriority());
        return taskRepository.save(task);
    }

    public void delete(Long taskId) {
        taskRepository.deleteById(taskId);
    }
}
