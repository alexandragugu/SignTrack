package com.example.demo.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.demo.DTO.LoginRequest;
import com.example.demo.DTO.RegisterRequest;
import com.example.demo.entities.UserBucket;
import com.example.demo.services.AuthService;
import com.example.demo.services.KeycloakService;
import com.example.demo.services.MinIOService;
import com.example.demo.services.UserBucketService;
import com.example.demo.utils.SecurityLogger;
import com.example.demo.utils.TokenRequest;
import com.example.demo.utils.UserRoles;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.time.Duration;


@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private KeycloakService keycloakService;
    @Autowired
    private MinIOService minIOService;
    @Autowired
    private UserBucketService userBucketService;

    @Autowired
    private JwtDecoderFactory jwtDecoderFactory;

    @GetMapping("/user")
    public ResponseEntity<String> getUser() {
        try {
            String id = authService.getUserIdFromJwt();
            return ResponseEntity.ok(id);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error get user");
        }
    }

    @PostMapping("/register")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> register(@RequestBody TokenRequest tokenRequest, HttpServletResponse response) {
        try {

            Map<String,String >data=keycloakService.getAccessToken(tokenRequest.getCode(), tokenRequest.getRedirectUri());

            String responseJson=data.get("access_token");
            String sub=data.get("sub");

            keycloakService.assignRole(sub, UserRoles.USER.getValue());

            System.out.println("Response from Keycloak: " + responseJson);

            String accessToken = data.get("access_token");
            String refreshToken = data.get("refresh_token");
            String idToken = data.get("id_token");

            if (accessToken != null && !accessToken.isEmpty()) {
                DecodedJWT jwt = JWT.decode(accessToken);
                String userId = jwt.getSubject();
                String username=jwt.getClaim("preferred_username").asString();


                UserBucket saved_userBucket = userBucketService.createUserBucket(UUID.fromString(userId),username);
                minIOService.createBucketForUser(saved_userBucket.getBucketUuid().toString());
                SecurityLogger.log("REGISTER", userId, "User registered and storage bucket created");
                ResponseCookie tokenCookie = ResponseCookie.from("token", accessToken)
                        .httpOnly(true)
                        .secure(true)
                        .path("/")
                        .sameSite("None")
                        .maxAge(Duration.ofMinutes(30))
                        .build();

                ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
                        .httpOnly(true)
                        .secure(true)
                        .path("/")
                        .sameSite("None")
                        .maxAge(Duration.ofDays(7))
                        .build();

                ResponseCookie idTokenCookie = ResponseCookie.from("id_token", idToken)
                        .httpOnly(true)
                        .secure(false)
                        .path("/")
                        .sameSite("None")
                        .maxAge(Duration.ofMinutes(30))
                        .build();

                SecurityLogger.log("ACCESS_TOKEN", "Access token issued successfully for authorization code flow");

                return ResponseEntity.ok()
                        .headers(headers -> {
                            headers.add(HttpHeaders.SET_COOKIE, tokenCookie.toString());
                            headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());
                            headers.add(HttpHeaders.SET_COOKIE, idTokenCookie.toString());
                        })
                        .body("Token set in cookie successfully");

            }else{
                SecurityLogger.log("REGISTER_FAIL", "Received empty token from Keycloak – registration aborted");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error register user");
            }


        } catch (Exception e) {
            SecurityLogger.log("REGISTER_FAIL", "Exception occurred during user registration: " + e.getMessage());
            return ResponseEntity.badRequest().body("Error register user");
        }

    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@CookieValue(value = "token", required = false) String token,  @CookieValue(value = "refresh_token", required = false) String refreshToken,
                                            @CookieValue(value = "id_token", required = false) String idToken) {
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No token provided");
        }

        try {


            Jwt decoded = jwtDecoderFactory.create().decode(token);

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("username", decoded.getClaimAsString("preferred_username"));

            String userId = decoded.getClaimAsString("sub");
            userInfo.put("roles", keycloakService.getRoles(userId));
            userInfo.put("email", decoded.getClaimAsString("email"));

            userInfo.put("access_token", token);
            userInfo.put("refresh_token", refreshToken);
            userInfo.put("id_token", idToken);

            return ResponseEntity.ok(userInfo);

        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token");
        }
    }

    @PostMapping("/token")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getAccessToken(@RequestBody TokenRequest tokenRequest, HttpServletResponse response) {
        try {
            System.out.println("Received token request: " + tokenRequest);

            Map<String, String> data=keycloakService.getAccessToken(tokenRequest.getCode(), tokenRequest.getRedirectUri());

            String json = data.get("access_token");

            String accessToken = data.get("access_token");
            String refreshToken = data.get("refresh_token");
            String idToken = data.get("id_token");

            System.out.println("Access token: " + accessToken);

            ResponseCookie tokenCookie = ResponseCookie.from("token", accessToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .sameSite("None")
                    .maxAge(Duration.ofDays(7))
                    .build();

            ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .sameSite("None")
                    .maxAge(Duration.ofDays(7))
                    .build();

            ResponseCookie idTokenCookie = ResponseCookie.from("id_token", idToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .sameSite("None")
                    .maxAge(Duration.ofDays(7))
                    .build();

            SecurityLogger.log("ACCESS_TOKEN", "Access token issued successfully for authorization code flow");

            return ResponseEntity.ok()
                    .headers(headers -> {
                        headers.add(HttpHeaders.SET_COOKIE, tokenCookie.toString());
                        headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());
                        headers.add(HttpHeaders.SET_COOKIE, idTokenCookie.toString());
                    })
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("message", "Token set in cookie successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("message", "Something went wrong with your authentication. Please try again."));
        }
    }

    @GetMapping("/id_token")
    public ResponseEntity<?> getIdToken(@CookieValue(value="id_token", required = false) String idToken) {
        if (idToken == null || idToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No token provided");
        }

        try {
            return ResponseEntity.ok(idToken);

        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token");
        }
    }

    @PostMapping("/refreshToken")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> exchangeRefreshToken(@CookieValue(value = "refresh_token", required = false) String refreshToken) {

        if(refreshToken==null || refreshToken.isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Refresh token missing"));
        }

        try {

            Map<String, String> tokens = keycloakService.refreshAccessToken(refreshToken);
            String tokenResponse=tokens.get("refresh_token");



            String newAccessToken=tokens.get("access_token");
            String newRefreshToken=tokens.get("refresh_token");

            ResponseCookie tokenCookie=ResponseCookie.from("token", newAccessToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .sameSite("None")
                    .maxAge(Duration.ofMinutes(30))
                    .build();

            ResponseCookie refreshCookie=ResponseCookie.from("refresh_token", newRefreshToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .sameSite("None")
                    .maxAge(Duration.ofMinutes(30))
                    .build();
            return ResponseEntity.ok()
                    .headers(headers -> {
                        headers.add(HttpHeaders.SET_COOKIE, tokenCookie.toString());
                        headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());
                    })
                    .body("Refresh token exchanged successfully");
        } catch (Exception e) {
            SecurityLogger.log("REFRESH_TOKEN", "Refresh token exchanged successfully");
            System.err.println("Eroare la refresh token: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid refresh token"));
        }
    }


    @GetMapping("/logout")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> logout() {
        try {


            ResponseCookie tokenCookie = ResponseCookie.from("token", null)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .sameSite("None")
                    .maxAge(Duration.ofDays(7))
                    .build();

            ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", null)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .sameSite("None")
                    .maxAge(Duration.ofDays(7))
                    .build();

            ResponseCookie idTokenCookie = ResponseCookie.from("id_token", null)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .sameSite("None")
                    .maxAge(Duration.ofDays(7))
                    .build();

            SecurityLogger.log("ACCESS_TOKEN", "Access token issued successfully for authorization code flow");

            return ResponseEntity.ok()
                    .headers(headers -> {
                        headers.add(HttpHeaders.SET_COOKIE, tokenCookie.toString());
                        headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());
                        headers.add(HttpHeaders.SET_COOKIE, idTokenCookie.toString());
                    })
                    .body("Token set in cookie successfully");

        } catch (Exception e) {
            SecurityLogger.log("ACCESS_TOKEN_FAIL", "Failed to retrieve access token: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

}
