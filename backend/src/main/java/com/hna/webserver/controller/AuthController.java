package com.hna.webserver.controller;

import com.hna.webserver.dto.AuthResponse;
import com.hna.webserver.dto.LoginRequest;
import com.hna.webserver.model.User;
import com.hna.webserver.repository.UserRepository;
import com.hna.webserver.security.JwtTokenProvider;
import com.hna.webserver.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final UserService userService;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtTokenProvider tokenProvider,
            UserRepository userRepository,
            UserService userService) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        log.info("Login attempt for user: {} from origin: {}", loginRequest.getUsername(), request.getHeader("Origin"));
        log.debug("Request headers: User-Agent={}, Content-Type={}", 
            request.getHeader("User-Agent"), request.getHeader("Content-Type"));
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = tokenProvider.generateToken(loginRequest.getUsername());

            User user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            userService.updateLastLogin(loginRequest.getUsername());

            Set<String> roles = user.getRoles().stream()
                    .map(Enum::name)
                    .collect(Collectors.toSet());

            log.info("Login successful for user: {}", loginRequest.getUsername());
            AuthResponse response = new AuthResponse(token, user.getUsername(), user.getEmail(), roles);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            log.warn("Login failed for user: {} - Bad credentials", loginRequest.getUsername());
            throw e;
        } catch (Exception e) {
            log.error("Login failed for user: {} - {}", loginRequest.getUsername(), e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Set<String> roles = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        AuthResponse response = new AuthResponse(null, user.getUsername(), user.getEmail(), roles);
        return ResponseEntity.ok(response);
    }
}
