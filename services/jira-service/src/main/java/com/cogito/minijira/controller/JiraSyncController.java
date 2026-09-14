package com.cogito.minijira.controller;

import com.cogito.minijira.service.JiraSyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/jira")
public class JiraSyncController {

    private final JiraSyncService jiraSyncService;

    public JiraSyncController(JiraSyncService jiraSyncService) {
        this.jiraSyncService = jiraSyncService;
    }

    @PostMapping("/sync")
    public ResponseEntity<String> sync(@RequestHeader("Authorization") String authorizationHeader) {
        // Extract JWT from "Bearer <token>"
        String jwt = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;
        
        jiraSyncService.syncAll(jwt);
        return ResponseEntity.ok("JIRA synchronization started successfully");
    }
}
