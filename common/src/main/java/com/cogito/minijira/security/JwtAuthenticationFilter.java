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

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final String jwtSecret;

    public JwtAuthenticationFilter(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

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
            // If the header is missing, we shouldn't necessarily fail here because 
            // other filters (like InternalServiceFilter) might handle authentication.
            // However, if we reach this point and no auth is set, we must allow 
            // the filter chain to continue so the AuthenticationEntryPoint can 
            // trigger the 401.
        }
        filterChain.doFilter(request, response);
    }
}
