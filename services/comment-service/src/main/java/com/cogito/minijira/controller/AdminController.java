package com.cogito.minijira.controller;

import com.cogito.minijira.repository.CommentRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

/**
 * Admin Controller - Administrative endpoints for comment management
 * 
 * Provides administrative operations such as database cleanup.
 * Only available in development profile.
 */
@RestController
@RequestMapping("/comments/admin")
@Profile("dev")
public class AdminController {

    // ========== Dependencies ==========

    private final CommentRepository repository;

    /**
     * Constructs an AdminController with required dependencies
     * 
     * @param repository the comment repository
     */
    public AdminController(CommentRepository repository) {
        this.repository = repository;
    }

    // ========== Admin Endpoints ==========

    /**
     * Clears all comments from the database
     * 
     * This endpoint is only available in development mode and should be used
     * with caution as it permanently deletes all comment data.
     * 
     * @return no content response
     */
    @PostMapping("/clear-db")
    @Transactional
    public ResponseEntity<Void> clearDb() {
        repository.deleteAll();
        return ResponseEntity.noContent().build();
    }
}
