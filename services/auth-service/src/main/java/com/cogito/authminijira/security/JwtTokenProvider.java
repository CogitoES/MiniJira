package com.cogito.authminijira.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT Token Provider - Generates and validates JWT tokens
 * 
 * This component manages the creation and validation of JWT tokens for user
 * authentication. It generates both access tokens (short-lived) and refresh
 * tokens (long-lived) using configurable expiration times.
 */
@Component
public class JwtTokenProvider {
    
    // ========== Logger ==========

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    // ========== Configuration ==========

    /** JWT secret key from application configuration */
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    /** Access token expiration time in milliseconds */
    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    /** Refresh token expiration time in milliseconds (default 7 days) */
    @Value("${app.jwt.refresh-expiration-ms:604800000}")
    private long jwtRefreshExpirationMs;

    // ========== Private Methods ==========

    /**
     * Generates the HMAC-SHA signing key from the JWT secret
     * 
     * @return the SecretKey for JWT signing and validation
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // ========== Public Methods ==========

    /**
     * Generates an access token for a user
     * 
     * @param username the username to encode in the token
     * @param userId the user ID to encode in the token
     * @return the generated JWT access token
     */
    public String generateToken(String username, Long userId) {
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Generates a refresh token for a user
     * 
     * @param username the username to encode in the token
     * @param userId the user ID to encode in the token
     * @return the generated JWT refresh token
     */
    public String generateRefreshToken(String username, Long userId) {
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime() + jwtRefreshExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extracts the username from a JWT token
     * 
     * @param token the JWT token
     * @return the username encoded in the token
     */
    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Validates a JWT token
     * 
     * @param token the JWT token to validate
     * @return true if the token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            logger.error("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Checks if a JWT token has expired
     * 
     * @param token the JWT token to check
     * @return true if the token is expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return false;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
