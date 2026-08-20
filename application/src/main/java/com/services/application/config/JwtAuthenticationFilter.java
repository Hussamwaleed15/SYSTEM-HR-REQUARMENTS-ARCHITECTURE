package com.services.application.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Stateless JWT filter for Application Service.
 * Extracts Bearer token from Authorization header,
 * validates it using JwtUtil, and sets SecurityContext with roles from claims.
 * No DB call is made - purely stateless validation.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = authHeader.substring(7);
            UsernamePasswordAuthenticationToken authentication = jwtUtil.getAuthentication(jwt);

            if (authentication != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("JWT authenticated user: {} with roles: {}",
                        authentication.getPrincipal(), authentication.getAuthorities());
            }
        } catch (Exception e) {
            log.warn("JWT processing failed for request {}: {}", request.getServletPath(), e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
