package com.cogito.minijira.common.dto;

/**
 * Authentication Response DTO - Represents JWT tokens returned after successful authentication
 * 
 * This DTO contains the access and refresh tokens issued to a user after successful
 * login, enabling authenticated requests to protected endpoints.
 */
public class AuthResponse {
    
    private String accessToken;
    private String refreshToken;

    public AuthResponse() {
    }

    public AuthResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    // ========== Getters and Setters ==========

    public String getAccessToken() {
        return accessToken;
    }
    
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
