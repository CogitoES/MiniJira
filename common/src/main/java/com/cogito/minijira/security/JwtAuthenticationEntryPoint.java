package com.cogito.minijira.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JWT Authentication Entry Point - Handles authentication failures
 * 
 * This component is triggered when an unauthenticated user attempts to access
 * a protected resource, sending a 401 Unauthorized response.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    
    /**
     * Responds to authentication failures by sending a 401 Unauthorized response
     * 
     * @param request the HTTP request
     * @param response the HTTP response
     * @param authException the authentication exception that occurred
     * @throws IOException if an IO error occurs
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }
}
