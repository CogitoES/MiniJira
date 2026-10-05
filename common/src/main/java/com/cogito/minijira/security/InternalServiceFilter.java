package com.cogito.minijira.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Internal Service Filter - Authenticates requests from internal microservices
 * 
 * This filter validates the X-Internal-Service-Secret header to authenticate
 * requests from internal services, allowing inter-service communication without
 * requiring individual user JWT tokens.
 */
public class InternalServiceFilter extends OncePerRequestFilter {

    // ========== Logging ==========

    private static final Logger logger = LoggerFactory.getLogger(InternalServiceFilter.class);

    // ========== Constants ==========

    private static final String HEADER_NAME = "X-Internal-Service-Secret";
    private static final String PUBLIC_SYNC_ENDPOINT = "/jira/sync";
    private static final String INTERNAL_SERVICE_USERNAME = "internal-service";
    private static final long INTERNAL_SERVICE_ID = -1L;
    private static final String ROLE_INTERNAL = "ROLE_INTERNAL";

    // ========== Fields ==========

    private final String internalSecret;

    /**
     * Constructs an InternalServiceFilter with the specified secret
     * 
     * @param internalSecret the secret key for validating internal service requests
     */
    public InternalServiceFilter(String internalSecret) {
        this.internalSecret = internalSecret;
    }

    // ========== Filter Implementation ==========

    /**
     * Validates internal service authentication for each request
     * 
     * Checks the X-Internal-Service-Secret header and sets authentication if valid.
     * Public endpoints like /jira/sync are bypassed.
     * 
     * @param request the HTTP request
     * @param response the HTTP response
     * @param filterChain the filter chain
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an IO error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String requestUri = request.getRequestURI();
        
        // Skip secret check for public/user-facing sync endpoint
        if (PUBLIC_SYNC_ENDPOINT.equals(requestUri)) {
            filterChain.doFilter(request, response);
            return;
        }

        String secret = request.getHeader(HEADER_NAME);
        
        logger.debug("Incoming request {} with secret: {}, expected: {}", requestUri, secret, internalSecret);

        if (internalSecret != null && internalSecret.equals(secret)) {
            logger.debug("Secret matched, setting authentication");
            authenticateInternalService();
        } else {
            logger.debug("Secret did not match");
        }
        
        filterChain.doFilter(request, response);
    }

    /**
     * Authenticates the internal service by setting a dummy principal in the security context
     */
    private void authenticateInternalService() {
        UserPrincipal principal = new UserPrincipal(INTERNAL_SERVICE_USERNAME, INTERNAL_SERVICE_ID);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                Collections.singletonList(new SimpleGrantedAuthority(ROLE_INTERNAL))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        logger.debug("Authentication set for internal service");
    }
}
