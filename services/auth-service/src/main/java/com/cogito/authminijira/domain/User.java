package com.cogito.authminijira.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * User Entity - Represents a user account in the system
 * 
 * This JPA entity represents a user account with authentication credentials
 * and session management. Users can be assigned tasks, report issues, and
 * have their roles tracked for authorization purposes.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String encryptedPassword;

    @Column(name = "refresh_token", length = 1000)
    private String refreshToken;

    @Column(nullable = false)
    private String role;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
