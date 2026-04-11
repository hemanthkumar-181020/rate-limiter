package com.ratelimiter.rate_limiter.config;

import com.ratelimiter.rate_limiter.filter.RateLimitFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor   // Lombok generates constructor for final fields
public class SecurityConfig {

    private final RateLimitFilter rateLimitFilter;  // Injected automatically

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http)
            throws Exception {
        http
                // 1. Disable CSRF — not needed for REST APIs
                .csrf(csrf -> csrf.disable())

                // 2. Stateless — no sessions, we use JWT
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. URL access rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated()
                )

                // 4. Register RateLimitFilter BEFORE Spring Security runs
                // This means rate limiting happens first — blocked requests
                // never even reach the security/auth layer
                .addFilterBefore(
                        rateLimitFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}

//package com.ratelimiter.rate_limiter.config;
//
//
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.web.SecurityFilterChain;
//
//@Configuration        // This class provides Spring beans
//@EnableWebSecurity    // Activates Spring Security
//public class SecurityConfig {
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http)
//            throws Exception {
//        http
//                // 1. Disable CSRF
//                // CSRF protects browser-based form submissions
//                // REST APIs use tokens (JWT) — CSRF not needed
//                .csrf(csrf -> csrf.disable())
//
//                // 2. Stateless sessions
//                // No HttpSession created or used
//                // Every request must carry its own JWT token
//                .sessionManagement(session -> session
//                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//
//                // 3. URL-based access rules
//                .authorizeHttpRequests(auth -> auth
//
//                        // Public — no token needed
//                        .requestMatchers("/api/public/**").permitAll()
//
//                        // Monitoring endpoints — open for now
//                        .requestMatchers("/actuator/**").permitAll()
//
//                        // Login endpoint — must be public
//                        .requestMatchers("/api/auth/**").permitAll()
//
//                        // Everything else — must have valid JWT
//                        .anyRequest().authenticated()
//                );
//
//        return http.build();
//    }
//}