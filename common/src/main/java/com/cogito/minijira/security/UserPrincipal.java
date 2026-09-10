package com.cogito.minijira.security;

public class UserPrincipal {
    private final String username;
    private final Long userId;

    public UserPrincipal(String username, Long userId) {
        this.username = username;
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public Long getUserId() {
        return userId;
    }
}
