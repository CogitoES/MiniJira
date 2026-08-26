package com.cogito.minijira.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        
        // Add the custom header for expired tokens if needed, 
        // though identifying it here might be tricky without re-parsing the token.
        // For simplicity, we add it generally if the error message indicates expiration.
        if (authException.getMessage() != null && authException.getMessage().contains("Token expired")) {
            response.setHeader("X-Token-Expired", "true");
        }
        
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
    }
}
