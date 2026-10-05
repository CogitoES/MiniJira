package com.cogito.minijira.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Auth Client - Feign client for communicating with the Auth Service
 * 
 * This client provides methods for querying the Auth Service, such as
 * verifying user existence. It uses Spring Cloud Feign for declarative
 * HTTP client implementation.
 */
@FeignClient(name = "auth-service")
public interface AuthClient {

    /**
     * Checks if a user exists by ID
     * 
     * @param id the user ID to check
     * @return true if the user exists, false otherwise
     */
    @GetMapping("/auth/exists/{id}")
    Boolean exists(@PathVariable("id") Long id);
}
