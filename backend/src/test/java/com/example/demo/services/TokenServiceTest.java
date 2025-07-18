package com.example.demo.services;


import com.example.demo.utils.FileActionType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @InjectMocks
    private TokenService tokenService;

    @Mock
    private AuthService authService;

    @Mock
    private FileActionService fileActionService;


    private String userId = UUID.randomUUID().toString();
    private String fileId = UUID.randomUUID().toString();
    private String email = "test@example.com";
    private final String secretKey = Base64.getEncoder().encodeToString("cheie-secreta-puternica-foarte-lunga-pentru-hs256".getBytes());

    @BeforeEach
    void setUp() {
        tokenService = new TokenService();
    }

    @Test
    void testGenerateFileToken_success() {
        String token = tokenService.generateFileToken(fileId, userId, email, FileActionType.TO_SIGN);

        assertNotNull(token);

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(Base64.getDecoder().decode(tokenService.getSecretKey())))
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals(fileId, claims.get("fileId"));
        assertEquals(userId, claims.get("userId"));
        assertEquals("TO_SIGN", claims.get("actionType"));
    }



    @Test
    void testValidateToken_failsIfWrongUser() {
        String token = tokenService.generateFileToken(fileId, userId, email, FileActionType.TO_SIGN);
        when(authService.getUserIdFromJwt()).thenReturn("different-user");
        RuntimeException ex = assertThrows(RuntimeException.class, () -> tokenService.validateToken(token));
        assertTrue(ex.getMessage().contains("not for you"));
    }

    @Test
    void testValidateToken_alreadySigned() throws Exception {
        String token = tokenService.generateFileToken(fileId, userId, email, FileActionType.TO_SIGN);
        when(authService.getUserIdFromJwt()).thenReturn(userId);
        when(fileActionService.registrationExists(UUID.fromString(userId), UUID.fromString(fileId), FileActionType.SIGNED)).thenReturn(true);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> tokenService.validateToken(token));
        assertTrue(ex.getMessage().contains("already signed"));
    }

    @Test
    void testValidateToken_fileNotFound() throws Exception {
        String token = tokenService.generateFileToken(fileId, userId, email, FileActionType.TO_SIGN);
        when(authService.getUserIdFromJwt()).thenReturn(userId);
        when(fileActionService.registrationExists(UUID.fromString(userId), UUID.fromString(fileId), FileActionType.SIGNED)).thenReturn(false);
        when(fileActionService.getFileByReceiverIdAndFileIdAndAction(any(), any(), any())).thenReturn(null);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> tokenService.validateToken(token));
        assertTrue(ex.getMessage().contains("Token invalid"));
    }
}