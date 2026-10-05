package com.cogito.minijira.controller;

import com.cogito.minijira.service.JiraSyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Jira Sync Controller - REST endpoints for Jira synchronization
 * 
 * Provides endpoints for synchronizing data from Jira back to the internal system.
 */
@RestController
@RequestMapping("/jira")
public class JiraSyncController {

    // ========== Dependencies ==========

    private final JiraSyncService jiraSyncService;

    /**
     * Constructs a JiraSyncController with required dependencies
     * 
     * @param jiraSyncService the Jira synchronization service
     */
    public JiraSyncController(JiraSyncService jiraSyncService) {
        this.jiraSyncService = jiraSyncService;
    }

    // ========== REST Endpoints ==========

    /**
     * Synchronizes data from Jira to the internal system
     * 
     * Extracts the JWT token from the Authorization header and uses it to
     * perform the synchronization with authenticated user context.
     * 
     * @param authorizationHeader the Authorization header containing the JWT token
     * @return success message if synchronization starts successfully
     */
    @PostMapping("/sync")
    public ResponseEntity<String> sync(@RequestHeader("Authorization") String authorizationHeader) {
        // Extract JWT from "Bearer <token>"
        String jwt = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;
        
        jiraSyncService.syncAll(jwt);
        return ResponseEntity.ok("JIRA synchronization started successfully");
    }
}
