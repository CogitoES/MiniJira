package com.cogito.minijira.common.dto;

/**
 * Register Request DTO - Represents user registration data
 * 
 * This DTO contains the required fields for user registration including
 * email, username, and password credentials.
 */
public class RegisterRequest {
    
    private String email;
    private String username;
    private String password;

    public RegisterRequest() {
    }

    // ========== Getters and Setters ==========

    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}
