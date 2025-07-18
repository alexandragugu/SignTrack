package com.example.demo.services;

import com.example.demo.DTO.RegisterRequest;
import com.example.demo.DTO.SessionDetailsDTO;
import com.example.demo.DTO.UserDetailsDTO;
import com.example.demo.DTO.UserResponseDTO;
import com.example.demo.utils.UserDetails;
import com.example.demo.utils.UserRoles;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import jakarta.annotation.PostConstruct;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class KeycloakService {

    @Autowired
    private final RedisTemplate redisTemplate;

    @Value("${keycloak.auth-server-url}")
    String keycloakUrl;

    @Value("${keycloak.realm}")
    String realm;

    @Value("${keycloak.resource}")
    String clientId;

    @Value("${keycloak.credentials.secret}")
    String clientSecret;

    @Value("${keycloak.admin.usernme}")
    private String adminUsername;

    @Value("${keycloak.admin.password}")
    private String adminPassword;

    @Value("${keycloak.admin-cli.secret}")
    String adminCliSecret;

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String publicKeyUrl;

    private String CLIENT_ID = "admin-cli";

    private String adminToken;

    private final RestTemplate restTemplate;

    public KeycloakService(RestTemplate restTemplate, @Qualifier("redisTemplate") RedisTemplate redisTemplate) {
        this.restTemplate = restTemplate;
        this.redisTemplate = redisTemplate;
    }


    public Map<String, String> getAccessToken(String code, String redirectUri) throws JsonProcessingException {
        String tokenUrl = UriComponentsBuilder
                .fromHttpUrl(keycloakUrl)
                .pathSegment("realms", realm, "protocol", "openid-connect", "token")
                .toUriString();

        String params = "code=" + code +
                "&client_id=" + clientId +
                "&client_secret=" + clientSecret +
                "&redirect_uri=" + redirectUri +
                "&grant_type=authorization_code";

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded");

        HttpEntity<String> entity = new HttpEntity<>(params, headers);

        ResponseEntity<String> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, entity, String.class);

        System.out.println("STATUS: " + response.getStatusCode());
        System.out.println("BODY: " + response.getBody());

        Map<String, String> data = new HashMap<>();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.getBody());
        String accessToken = root.path("access_token").asText();

        data.put("access_token", accessToken);

        String refreshToken = root.path("refresh_token").asText();
        String idToken = root.path("id_token").asText();

        data.put("refresh_token", refreshToken);
        data.put("id_token", idToken);

        String jwksUrl = publicKeyUrl;
        JwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwksUrl).build();
        Jwt jwt = decoder.decode(accessToken);

        String sessionId = jwt.getClaimAsString("sid");
        String sub = jwt.getClaimAsString("sub");

        data.put("sub", sub);
        return data;
    }


    public void assignRole(String userUUID, String roleName) {
        String adminToken = getAdminToken();

        String roleUrl = keycloakUrl + "/admin/realms/" + realm + "/roles/" + roleName;
        HttpHeaders roleHeaders = new HttpHeaders();
        roleHeaders.setBearerAuth(adminToken);
        HttpEntity<Void> roleRequest = new HttpEntity<>(roleHeaders);
        ResponseEntity<JsonNode> roleResponse = restTemplate.exchange(roleUrl, HttpMethod.GET, roleRequest, JsonNode.class);
        JsonNode role = roleResponse.getBody();

        String assignRoleUrl = keycloakUrl + "/admin/realms/" + realm + "/users/" + userUUID + "/role-mappings/realm";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ArrayNode rolesArray = new ObjectMapper().createArrayNode();
        rolesArray.add(role);

        HttpEntity<String> assignEntity = new HttpEntity<>(rolesArray.toString(), headers);
        restTemplate.postForEntity(assignRoleUrl, assignEntity, String.class);
    }

    public List<String> getRoles(String userId) {
        String adminToken = getAdminToken(); //

        String url = keycloakUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                JsonNode.class
        );

        List<String> roles = new ArrayList<>();

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            for (JsonNode role : response.getBody()) {
                roles.add(role.path("name").asText());
            }
        }

        return roles;
    }

    public Map<String, String> refreshAccessToken(String refreshToken) {
        String tokenUrl = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "refresh_token");
        params.add("refresh_token", refreshToken);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, String> tokens = new HashMap<>();
                tokens.put("access_token", response.getBody().get("access_token").toString());
                tokens.put("refresh_token", response.getBody().get("refresh_token").toString());

                return tokens;
            } else {
                throw new RuntimeException("Failed to refresh access token: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error refreshing access token: " + e.getMessage(), e);
        }
    }

    private String getRoleIdByName(String roleName) {
        String adminToken = getAdminToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<RoleRepresentation[]> response = restTemplate.exchange(
                keycloakUrl + "/admin/realms/" + realm + "/roles",
                HttpMethod.GET,
                entity,
                RoleRepresentation[].class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            for (RoleRepresentation role : response.getBody()) {
                if (role.getName().equals(roleName)) {
                    return role.getId();
                }
            }
        }

        throw new RuntimeException("Role not found: " + roleName);
    }


    public String createUser(Map<String, Object> userDetails) {
        String endpoint = keycloakUrl + "/admin/realms/" + realm + "/users";

        adminToken = getAdminToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(userDetails, headers);
        ResponseEntity<Void> response = restTemplate.postForEntity(endpoint, request, Void.class);

        if (response.getStatusCode() == HttpStatus.CREATED) {

            URI location = response.getHeaders().getLocation();
            if (location != null) {
                String[] segments = location.getPath().split("/");
                return segments[segments.length - 1];
            }
        }

        throw new RuntimeException("Failed to create user in Keycloak");
    }

    public void setPasswordForUser(String userId, String password) {
        String endpoint = keycloakUrl + "/admin/realms/" + realm + "/users/" + userId + "/reset-password";

        adminToken = getAdminToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> passwordPayload = new HashMap<>();
        passwordPayload.put("type", "password");
        passwordPayload.put("value", password);
        passwordPayload.put("temporary", false);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(passwordPayload, headers);
        restTemplate.put(endpoint, request);
    }

    public void assignRealmRoleToUser(String userId, String roleName) {
        String rolesEndpoint = keycloakUrl + "/admin/realms/" + realm + "/roles/" + roleName;
        String assignEndpoint = keycloakUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm";

        adminToken = getAdminToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map> roleResponse = restTemplate.exchange(
                rolesEndpoint, HttpMethod.GET, new HttpEntity<>(headers), Map.class);

        if (!roleResponse.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Could not fetch role details from Keycloak");
        }

        List<Map<String, Object>> roleList = new ArrayList<>();
        Map<String, Object> role = roleResponse.getBody();
        roleList.add(Map.of(
                "id", role.get("id"),
                "name", role.get("name")
        ));

        HttpEntity<List<Map<String, Object>>> assignRequest = new HttpEntity<>(roleList, headers);
        restTemplate.postForEntity(assignEndpoint, assignRequest, Void.class);
    }

    public String getUserById(String userId, UserDetails userDetails) {
        String endpoint = keycloakUrl + "/admin/realms/" + realm + "/users/" + userId;

        adminToken = getAdminToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    endpoint, HttpMethod.GET, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                switch (userDetails) {
                    case USERNAME:
                        return response.getBody().get("username").toString();
                    case EMAIL:
                        return response.getBody().get("email").toString();
                    case FULLNAME:
                        return response.getBody().get("firstName").toString() + " "
                                + response.getBody().get("lastName").toString();
                    default:
                        throw new RuntimeException("Invalid user details");
                }

            } else {
                throw new RuntimeException("Utilizatorul nu a fost gasit");
            }

        } catch (Exception e) {
            throw new RuntimeException("Eroare la obtinerea numelui utilizatorului: " + e.getMessage());
        }
    }

    private List<String> getUserRoles(String userId, HttpEntity<Void> request) {
        String rolesEndpoint = keycloakUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm";
        ResponseEntity<List> rolesResponse = restTemplate.exchange(
                rolesEndpoint, HttpMethod.GET, request, List.class);

        if (rolesResponse.getStatusCode() == HttpStatus.OK && rolesResponse.getBody() != null) {
            List<String> roles = new ArrayList<>();
            for (Object roleObj : rolesResponse.getBody()) {
                Map<String, Object> roleMap = (Map<String, Object>) roleObj;
                roles.add(roleMap.get("name").toString());
            }
            return roles;
        } else {
            throw new RuntimeException("Rolurile utilizatorului nu au fost gasite");
        }
    }

    private Map<String, Object> getUserSessionStatus(String userId, HttpEntity<Void> request) {

        Map<String, Object> details = new HashMap<>();
        String sessionsEndpoint = keycloakUrl + "/admin/realms/" + realm + "/users/" + userId + "/sessions";
        ResponseEntity<List> sessionResponse = restTemplate.exchange(sessionsEndpoint, HttpMethod.GET, request,
                List.class);
        if (sessionResponse.getStatusCode() == HttpStatus.OK && sessionResponse.getBody() != null) {
            details.put("sessionStatus", !sessionResponse.getBody().isEmpty());

            if (!sessionResponse.getBody().isEmpty()) {
                Map<String, Object> lastSession = (Map<String, Object>) sessionResponse.getBody().get(0);
                if (lastSession.containsKey("lastAccess")) {
                    long lastAccess = (long) lastSession.get("lastAccess");
                    details.put("lastAccess",
                            Instant.ofEpochMilli(lastAccess).atZone(ZoneId.systemDefault()).toLocalDateTime());

                }
            }
        } else {
            throw new RuntimeException("Sesiunea utilizatorului nu a fost gasita");
        }
        return details;
    }

    public UserDetailsDTO getUserDetails(String userId) {
        String endpoint = keycloakUrl + "/admin/realms/" + realm + "/users/" + userId;

        adminToken = getAdminToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        UserDetailsDTO userDetails = new UserDetailsDTO();

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    endpoint, HttpMethod.GET, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {

                userDetails.setUsername(response.getBody().get("username").toString());
                userDetails.setEmail(response.getBody().get("email").toString());
                userDetails.setFirstName(response.getBody().get("firstName").toString());
                userDetails.setLastName(response.getBody().get("lastName").toString());
                long createdTimestamp = (long) response.getBody().get("createdTimestamp");
                userDetails.setRegistrationDate(
                        Instant.ofEpochMilli(createdTimestamp).atZone(ZoneId.systemDefault()).toLocalDateTime());

            } else {
                throw new RuntimeException("Utilizatorul nu a fost gasit");
            }

            userDetails.setRole(getUserRoles(userId, request));
            Map<String, Object> sessionStatus = getUserSessionStatus(userId, request);
            userDetails.setStatus((Boolean) sessionStatus.get("sessionStatus"));
            userDetails.setLastLogin(
                    (sessionStatus.get("lastAccess") != null) ? (LocalDateTime) sessionStatus.get("lastAccess") : null);

            return userDetails;
        } catch (Exception e) {
            throw new RuntimeException("Eroare la obținerea numelui utilizatorului: " + e.getMessage());
        }
    }

    public String getUserIdByUsername(String username) {
        String endpoint = keycloakUrl + "/admin/realms/" + realm + "/users?username=" + username + "&exact=true";
        adminToken = getAdminToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<Map[]> response = restTemplate.exchange(endpoint, HttpMethod.GET, request, Map[].class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null && response.getBody().length > 0) {
            return response.getBody()[0].get("id").toString();
        }
        throw new RuntimeException("Nu a  gasit user");
    }

    public String getUserRoleById(String userId) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        String rolesEndpoint = keycloakUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm";
        HttpEntity<Void> roleRequest = new HttpEntity<>(headers);
        ResponseEntity<List> rolesResponse = restTemplate.exchange(rolesEndpoint, HttpMethod.GET, roleRequest, List.class);


        if (rolesResponse.getStatusCode() == HttpStatus.OK && rolesResponse.getBody() != null) {
            for (Object roleObj : rolesResponse.getBody()) {
                Map<String, Object> roleMap = (Map<String, Object>) roleObj;
                String roleName = (String) roleMap.get("name");

                if ("Admin".equalsIgnoreCase(roleName) || "User".equalsIgnoreCase(roleName)) {
                    return roleName;

                }
            }
        }

        return null;
    }

    public List<UserResponseDTO> getAllUsers() {
        String endpoint = keycloakUrl + "/admin/realms/" + realm + "/users";

        adminToken = getAdminToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map[]> response = restTemplate.exchange(endpoint, HttpMethod.GET, request, Map[].class);


        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<UserResponseDTO> users = new ArrayList<>();
            for (Map user : response.getBody()) {
                String id = (user.get("id") != null) ? user.get("id").toString() : null;
                String username = (String) user.get("username");
                String email = (String) user.get("email");
                String firstName = (String) user.get("firstName");
                String lastName = (String) user.get("lastName");

                String userRole = getUserRoleById(id);

                if (userRole.equals("User")) {
                    users.add(new UserResponseDTO(id, username, email, userRole, firstName, lastName));
                }
            }
            return users;
        }
        throw new RuntimeException("No users found in Keycloak");
    }

    public List<UserResponseDTO> getAllAccounts() {
        String endpoint = keycloakUrl + "/admin/realms/" + realm + "/users";

        adminToken = getAdminToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map[]> response = restTemplate.exchange(endpoint, HttpMethod.GET, request, Map[].class);


        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<UserResponseDTO> users = new ArrayList<>();
            for (Map user : response.getBody()) {
                String id = (user.get("id") != null) ? user.get("id").toString() : null;
                String username = (String) user.get("username");
                String email = (String) user.get("email");
                String firstName = (String) user.get("firstName");
                String lastName = (String) user.get("lastName");

                String userRole = getUserRoleById(id);

                users.add(new UserResponseDTO(id, username, email, userRole, firstName, lastName));
            }
            return users;
        }
        throw new RuntimeException("No users found in Keycloak");
    }

    public String getAdminToken() {
        String tokenUrl = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "client_credentials");
        params.add("client_secret", adminCliSecret);
        params.add("client_id", CLIENT_ID);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().get("access_token").toString();
            } else {
                throw new RuntimeException("Failed to obtain token: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error fetching admin token: " + e.getMessage(), e);
        }
    }

    public void updateUserDetails(String userId, Map<String, Object> params) {
        String endpoint = keycloakUrl + "/admin/realms/" + realm + "/users/" + userId;

        adminToken = getAdminToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(params, headers);

        ResponseEntity<Void> response = restTemplate.exchange(endpoint, HttpMethod.PUT, request, Void.class);
        if (response.getStatusCode() != HttpStatus.NO_CONTENT) {
            throw new RuntimeException("Eroare la actualizarea detaliilor utilizatorului");
        } else {
            System.out.println("Detaliile utilizatorului au fost actualizate");
        }

    }


    public void deleteUser(String userId) {
        String url = keycloakUrl + "/admin/realms/" + realm + "/users/" + userId;

        String adminToken = getAdminToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to delete user");
        }
    }

    public SessionDetailsDTO getUserSessions(String id, String userToken) {
    }
}
