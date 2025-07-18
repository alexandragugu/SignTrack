package com.example.demo.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.MockedStatic;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KeycloakServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private KeycloakService keycloakService;

    @BeforeEach
    void setUp() {
        keycloakService.keycloakUrl = "http://localhost:8080";
        keycloakService.realm = "my-realm";
        keycloakService.clientId = "test-client";
        keycloakService.clientSecret = "test-secret";
        keycloakService.adminCliSecret = "admin-secret";
    }

    @Test
    void testAssignRole_success() {
        String userUUID = UUID.randomUUID().toString();
        String roleName = "User";
        String token = "admin-access-token";

        String expectedRoleUrl = keycloakService.keycloakUrl + "/admin/realms/" + keycloakService.realm + "/roles/" + roleName;
        String expectedAssignUrl = keycloakService.keycloakUrl + "/admin/realms/" + keycloakService.realm +
                "/users/" + userUUID + "/role-mappings/realm";

        ObjectNode roleJson = new ObjectMapper().createObjectNode();
        roleJson.put("id", "role-id");
        roleJson.put("name", roleName);

        ResponseEntity<JsonNode> mockGetRoleResponse = new ResponseEntity<>(roleJson, HttpStatus.OK);

        KeycloakService spyService = Mockito.spy(keycloakService);
        doReturn(token).when(spyService).getAdminToken();

        when(restTemplate.exchange(
                eq(expectedRoleUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(JsonNode.class))
        ).thenReturn(mockGetRoleResponse);

        when(restTemplate.postForEntity(
                eq(expectedAssignUrl),
                any(HttpEntity.class),
                eq(String.class))
        ).thenReturn(ResponseEntity.ok().build());

        spyService.assignRole(userUUID, roleName);

        verify(restTemplate).postForEntity(
                eq(expectedAssignUrl),
                argThat((HttpEntity<String> entity) -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        ArrayNode arrayNode = (ArrayNode) mapper.readTree(entity.getBody());
                        JsonNode roleNode = arrayNode.get(0);
                        return roleNode.get("id").asText().equals("role-id") &&
                                roleNode.get("name").asText().equals(roleName);
                    } catch (Exception e) {
                        return false;
                    }
                }),
                eq(String.class)
        );
    }

    @Test
    void testSetPasswordForUser_success() {
        String userId = "test-user-id";
        String password = "securePassword123";
        String expectedToken = "admin-token";

        KeycloakService spyService = Mockito.spy(keycloakService);
        doReturn(expectedToken).when(spyService).getAdminToken();

        String expectedUrl = keycloakService.keycloakUrl + "/admin/realms/" + keycloakService.realm +
                "/users/" + userId + "/reset-password";

        spyService.setPasswordForUser(userId, password);

        ArgumentCaptor<HttpEntity<Map<String, Object>>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).put(eq(expectedUrl), captor.capture());

        HttpEntity<Map<String, Object>> capturedEntity = captor.getValue();
        Map<String, Object> body = capturedEntity.getBody();
        HttpHeaders headers = capturedEntity.getHeaders();

        assertEquals("password", body.get("type"));
        assertEquals(password, body.get("value"));
        assertEquals(false, body.get("temporary"));

        assertEquals(MediaType.APPLICATION_JSON, headers.getContentType());
        assertEquals("Bearer " + expectedToken, headers.getFirst(HttpHeaders.AUTHORIZATION));
    }

}