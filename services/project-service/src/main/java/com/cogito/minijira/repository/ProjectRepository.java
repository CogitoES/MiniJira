package com.cogito.minijira.repository;

import com.cogito.minijira.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Project Repository - Data access layer for Project entities
 * 
 * This repository provides CRUD operations and custom query methods
 * for Project entities, facilitating database interactions.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    
    /**
     * Finds a project by its Jira key
     * 
     * @param jiraKey the Jira key to search for
     * @return an Optional containing the project if found, empty otherwise
     */
    Optional<Project> findByJiraKey(String jiraKey);
    
    /**
     * Finds a project by its name
     * 
     * @param name the project name to search for
     * @return an Optional containing the project if found, empty otherwise
     */
    Optional<Project> findByName(String name);
    
    /**
     * Checks if a project exists by name
     * 
     * @param name the project name to check
     * @return true if a project with the given name exists, false otherwise
     */
    boolean existsByName(String name);
}
