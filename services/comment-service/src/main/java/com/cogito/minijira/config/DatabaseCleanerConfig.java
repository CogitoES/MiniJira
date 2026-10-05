package com.cogito.minijira.config;

import com.cogito.minijira.repository.CommentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Database Cleaner Configuration - Clears database on application startup
 * 
 * This component runs only in development profile and clears all comments
 * from the database during application startup for testing purposes.
 */
@Component
@Profile("dev")
public class DatabaseCleanerConfig implements CommandLineRunner {

    // ========== Dependencies ==========

    private final CommentRepository repository;

    /**
     * Constructs DatabaseCleanerConfig with required dependencies
     * 
     * @param repository the comment repository
     */
    public DatabaseCleanerConfig(CommentRepository repository) {
        this.repository = repository;
    }

    // ========== Application Lifecycle ==========

    /**
     * Executes on application startup
     * 
     * Clears all comments from the database. Only executes in development profile.
     * 
     * @param args command-line arguments (unused)
     */
    @Override
    @Transactional
    public void run(String... args) {
        repository.deleteAll();
    }
}
