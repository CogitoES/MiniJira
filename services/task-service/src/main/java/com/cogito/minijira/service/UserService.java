package com.cogito.minijira.service;

import org.springframework.stereotype.Service;

@Service
public class UserService {

    // TODO: Implement actual user lookup (e.g., via FeignClient to auth-service
    // or by extracting userId from JWT claims)
    public Long getUserIdByUsername(String username) {
        // Temporary placeholder mapping
        return 1L;
    }
}
