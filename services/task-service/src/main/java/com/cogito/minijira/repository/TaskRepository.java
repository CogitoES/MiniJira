package com.cogito.minijira.repository;

import com.cogito.minijira.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Task Repository - Data access layer for Task entities
 * 
 * This repository provides CRUD operations and custom query methods
 * for Task entities, facilitating database interactions and task retrieval.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    /**
     * Finds all tasks belonging to a specific project
     * 
     * @param projectId the project ID to filter by
     * @return a list of tasks in the specified project
     */
    List<Task> findByProjectId(Long projectId);
    
    /**
     * Finds a task by its Jira key
     * 
     * @param jiraKey the Jira key to search for
     * @return an Optional containing the task if found, empty otherwise
     */
    Optional<Task> findByJiraKey(String jiraKey);
    
    /**
     * Finds a task by its title
     * 
     * @param title the task title to search for
     * @return an Optional containing the task if found, empty otherwise
     */
    Optional<Task> findByTitle(String title);
}
