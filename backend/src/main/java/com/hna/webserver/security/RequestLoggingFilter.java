package com.hna.webserver.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Log all requests to auth endpoints
        if (request.getRequestURI().startsWith("/api/auth")) {
            log.info("=== Incoming Request ===");
            log.info("Method: {} URI: {}", request.getMethod(), request.getRequestURI());
            log.info("Origin: {}", request.getHeader("Origin"));
            log.info("Referer: {}", request.getHeader("Referer"));
            log.info("Content-Type: {}", request.getHeader("Content-Type"));
            log.info("User-Agent: {}", request.getHeader("User-Agent"));
            log.info("========================");
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Log response status for auth endpoints
            if (request.getRequestURI().startsWith("/api/auth")) {
                log.info("=== Response ===");
                log.info("Status: {}", response.getStatus());
                log.info("================");
            }
        }
    }
}
