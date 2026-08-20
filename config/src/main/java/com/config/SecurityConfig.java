package com.config;

import com.services.auth.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/jobs/open").permitAll()
                        .requestMatchers("/api/applications/public/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()

                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        .requestMatchers("/api/jobs/**").hasAnyRole("ADMIN", "HR")
                        .requestMatchers("/api/applications/assign-interviewer/**").hasAnyRole("ADMIN", "HR")
                        .requestMatchers("/api/applications/hire/**").hasAnyRole("ADMIN", "HR")
                        .requestMatchers("/api/applications/{id}/hire").hasAnyRole("ADMIN", "HR")

                        .requestMatchers("/api/applications/evaluation/**").hasAnyRole("ADMIN", "HR", "INTERVIEWER")
                        .requestMatchers("/api/applications/status/**").hasAnyRole("ADMIN", "HR", "INTERVIEWER")

                        .requestMatchers("/api/applications/**").hasAnyRole("ADMIN", "HR", "INTERVIEWER")
                        .requestMatchers("/api/candidates/**").hasAnyRole("ADMIN", "HR", "INTERVIEWER")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}