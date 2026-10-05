package com.cogito.minijira.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.Collections;

/**
 * JWT Authentication Filter - Validates JWT tokens in incoming HTTP requests
 * 
 * This filter extracts and validates JWT tokens from the Authorization header
 * of incoming requests, populating the Spring Security context with the authenticated
 * user's information if the token is valid.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // ========== Logger ==========

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    // ========== Fields ==========

    /** JWT secret key used for token validation */
    private final String jwtSecret;

    /**
     * Constructs a JwtAuthenticationFilter with the specified JWT secret
     * 
     * @param jwtSecret the secret key for validating JWT tokens
     */
    public JwtAuthenticationFilter(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    // ========== Private Methods ==========

    /**
     * Generates the HMAC-SHA signing key from the JWT secret
     * 
     * @return the SecretKey for JWT validation
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // ========== Filter Implementation ==========

    /**
     * Performs JWT validation for each HTTP request
     * 
     * Extracts the JWT token from the Authorization header, validates it,
     * and sets the authenticated user in the Spring Security context.
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
        String authHeader = request.getHeader("Authorization");
        logger.info(">>> Processing request: {} {}. Auth Header: {}", request.getMethod(), request.getRequestURI(), authHeader != null ? "Present" : "Missing");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                var claims = Jwts.parser()
                        .verifyWith(getSigningKey())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
                
                String username = claims.getSubject();
                Long userId = claims.get("userId", Long.class);

                if (username != null && userId != null) {
                    logger.info("Successfully authenticated user: {} (ID: {})", username, userId);
                    UserPrincipal principal = new UserPrincipal(username, userId);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            principal, null, Collections.emptyList());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    logger.warn("JWT validation successful but claims missing: username={}, userId={}", username, userId);
                }
            } catch (Exception e) {
                logger.error("JWT validation failed for token: {}. Error: {}", token, e.getMessage());
            }
        } else {
            logger.warn("Request missing or invalid Authorization header: {} {}", request.getMethod(), request.getRequestURI());
        }
        filterChain.doFilter(request, response);
    }
}
