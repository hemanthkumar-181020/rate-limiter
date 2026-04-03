package com.ratelimiter.rate_limiter.config;



import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration        // This class provides Spring beans
@EnableWebSecurity    // Activates Spring Security
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http)
            throws Exception {
        http
                // 1. Disable CSRF
                // CSRF protects browser-based form submissions
                // REST APIs use tokens (JWT) — CSRF not needed
                .csrf(csrf -> csrf.disable())

                // 2. Stateless sessions
                // No HttpSession created or used
                // Every request must carry its own JWT token
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. URL-based access rules
                .authorizeHttpRequests(auth -> auth

                        // Public — no token needed
                        .requestMatchers("/api/public/**").permitAll()

                        // Monitoring endpoints — open for now
                        .requestMatchers("/actuator/**").permitAll()

                        // Login endpoint — must be public
                        .requestMatchers("/api/auth/**").permitAll()

                        // Everything else — must have valid JWT
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}