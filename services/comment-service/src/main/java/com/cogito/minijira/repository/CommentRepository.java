package com.cogito.minijira.repository;

import com.cogito.minijira.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Comment Repository - Data access layer for Comment entities
 * 
 * This repository provides CRUD operations and custom query methods
 * for Comment entities, facilitating database interactions and comment retrieval.
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    /**
     * Finds all comments for a specific task, ordered by creation time ascending
     * 
     * @param taskId the task ID to filter by
     * @return a list of comments in the specified task, ordered by creation time
     */
    List<Comment> findByTaskIdOrderByCreatedAtAsc(Long taskId);
    
    /**
     * Finds a comment by its Jira key
     * 
     * @param jiraKey the Jira key to search for
     * @return an Optional containing the comment if found, empty otherwise
     */
    Optional<Comment> findByJiraKey(String jiraKey);
}
