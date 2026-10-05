package com.cogito.minijira.common.dto;

/**
 * Login Request DTO - Represents user login credentials
 * 
 * This DTO contains the required fields for user authentication including
 * email and password.
 */
public class LoginRequest {
    
    private String email;
    private String password;

    public LoginRequest() {
    }

    // ========== Getters and Setters ==========

    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}
