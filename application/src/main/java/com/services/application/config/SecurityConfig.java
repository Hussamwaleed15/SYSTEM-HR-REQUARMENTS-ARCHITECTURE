package com.services.application.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public tracking - candidates can check their own status
                        .requestMatchers("/api/applications/public/**").permitAll()
                        // Submit application - authenticated employees/candidates
                        .requestMatchers(HttpMethod.POST, "/api/applications").hasAnyRole("EMPLOYEE", "HR", "ADMIN")
                        // View all applications - HR/ADMIN/INTERVIEWER
                        .requestMatchers(HttpMethod.GET, "/api/applications").hasAnyRole("HR", "ADMIN", "INTERVIEWER")
                        .requestMatchers(HttpMethod.GET, "/api/applications/**").hasAnyRole("HR", "ADMIN", "INTERVIEWER")
                        // Status update & assign interviewer - HR/ADMIN
                        .requestMatchers(HttpMethod.PUT, "/api/applications/*/status").hasAnyRole("HR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/applications/*/assign-interviewer").hasAnyRole("HR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/applications/*/hire").hasAnyRole("HR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/applications/*/reject").hasAnyRole("HR", "ADMIN")
                        // Evaluation - interviewers and HR/ADMIN
                        .requestMatchers(HttpMethod.PUT, "/api/applications/*/evaluation").hasAnyRole("INTERVIEWER", "HR", "ADMIN")
                        // Delete - ADMIN only
                        .requestMatchers(HttpMethod.DELETE, "/api/applications/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}