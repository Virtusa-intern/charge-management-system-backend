package com.bank.charge_management_system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Temporary security configuration for initial development
     * This allows all requests to pass through without authentication
     * We'll implement proper security later in the project
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(authz -> authz
                // Allow health and welcome endpoints for testing
                .requestMatchers("/api/health", "/api/welcome").permitAll()
                // Allow database test endpoints
                .requestMatchers("/api/database/**").permitAll()
                // Allow H2 console for development
                .requestMatchers("/h2-console/**").permitAll()
                
                .requestMatchers("/api/rules/**").permitAll()
                // Allow Swagger UI for API documentation
                .requestMatchers("/swagger-ui/**", "/api-docs/**").permitAll()
                // For now, permit all other requests (we'll secure them later)
                .anyRequest().permitAll()
            )
            // Disable CSRF for API endpoints (will configure properly later)
            .csrf(csrf -> csrf.disable())
            // Allow frames for H2 console
            .headers(headers -> headers.frameOptions().disable());
            
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}