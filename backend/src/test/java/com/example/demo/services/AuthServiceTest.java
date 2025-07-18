package com.example.demo.services;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AuthServiceTest {
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService();
        Jwt jwt = createMockJwt();
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(jwt, null);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Jwt createMockJwt() {
        return new Jwt(
                "test_token_value",
                null,
                null,
                Map.of("alg", "none"),
                Map.of(
                        "sub", "randomUUID",
                        "preferred_username", "test_username"
                )
        );
    }

    @Test
    void getUserIdFromJwt() {
        String userId = authService.getUserIdFromJwt();
        assertEquals("randomUUID", userId);
    }

    @Test
    void getUsernameFromJwt() {
        String username = authService.getUsernameFromJwt();
        assertEquals("test_username", username);
    }

    @Test
    void getToken() {
        String token = authService.getToken();
        assertEquals("test_token_value", token);
    }

}
