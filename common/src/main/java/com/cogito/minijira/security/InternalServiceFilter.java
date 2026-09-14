package com.cogito.minijira.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

public class InternalServiceFilter extends OncePerRequestFilter {

    private final String internalSecret;
    private static final String HEADER_NAME = "X-Internal-Service-Secret";

    public InternalServiceFilter(String internalSecret) {
        this.internalSecret = internalSecret;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Skip secret check for public/user-facing sync endpoint
        if (request.getRequestURI().equals("/jira/sync")) {
            filterChain.doFilter(request, response);
            return;
        }

        String secret = request.getHeader(HEADER_NAME);
        
        System.out.println("DEBUG: Incoming request " + request.getRequestURI() + " with secret: " + secret + ", expected: " + internalSecret);

        if (internalSecret.equals(secret)) {
            System.out.println("DEBUG: Secret matched, setting authentication");
            // Use a dummy UserPrincipal for internal service requests
            UserPrincipal principal = new UserPrincipal("internal-service", -1L);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    principal, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_INTERNAL")));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            System.out.println("DEBUG: Authentication set: " + SecurityContextHolder.getContext().getAuthentication());
        } else {
            System.out.println("DEBUG: Secret did not match");
        }
        
        filterChain.doFilter(request, response);
    }
}
