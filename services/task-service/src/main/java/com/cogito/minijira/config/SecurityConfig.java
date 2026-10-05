package com.cogito.minijira.config;

import com.cogito.minijira.security.InternalServiceFilter;
import com.cogito.minijira.security.JwtAuthenticationEntryPoint;
import com.cogito.minijira.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security Configuration - Configures Spring Security for the Task Service
 * 
 * Configures JWT-based authentication, internal service-to-service authentication,
 * stateless session management, and HTTP security policies.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // ========== Configuration Properties ==========

    /** JWT secret key for token validation */
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    /** Internal service secret key for service-to-service authentication */
    @Value("${app.internal.secret}")
    private String internalSecret;

    /** Authentication entry point handler for unauthorized requests */
    @Autowired
    private JwtAuthenticationEntryPoint unauthorizedHandler;

    // ========== Bean Definitions ==========

    /**
     * Configures the security filter chain
     * 
     * Sets up:
     * - CSRF protection disabled (stateless API)
     * - Form login disabled (JWT-based authentication)
     * - HTTP Basic disabled (JWT-based authentication)
     * - JWT authentication filter
     * - Internal service authentication filter
     * - Stateless session management
     * 
     * @param http the HttpSecurity to configure
     * @return configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .anonymous(AbstractHttpConfigurer::disable)
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(new JwtAuthenticationFilter(jwtSecret), UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(new InternalServiceFilter(internalSecret), JwtAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .anyRequest().authenticated()
            );
        return http.build();
    }
}
