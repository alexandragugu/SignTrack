package com.example.demo.controller;

import com.example.demo.DTO.SessionDetailsDTO;
import com.example.demo.DTO.UserDetailsDTO;
import com.example.demo.DTO.UserProfileDTO;
import com.example.demo.DTO.UserResponseDTO;
import com.example.demo.services.*;
import com.example.demo.utils.SecurityLogger;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private KeycloakService keycloakService;

    @Autowired
    private final AuthService authService;


    @Autowired
    private FileService fileService;

    @Autowired
    private FileActionService fileActionService;

    @Autowired
    private UserBucketService userBucketService;

    @Autowired
    private MinIOService minIOService;

    @Autowired
    private CookieService cookieService;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    public UserController(AuthService authService) {

        this.authService = authService;
    }


    @GetMapping("/allUsers")
    public ResponseEntity<?> getUsers() {

        log.info("Received GET request on /allUsers endpoint");
        try {
            log.debug("Calling keycloakService.getAllUsers()");
            List<UserResponseDTO> usersDTO = keycloakService.getAllUsers();
            log.debug("Received {} users from Keycloak", usersDTO.size());
            return ResponseEntity.ok().body(usersDTO);
        } catch (Exception e) {
            log.error("Failed to fetch users: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong while fetching users.");
        }

    }


    @GetMapping("/details")
    public ResponseEntity<UserDetailsDTO> getUserById() {
        log.info("Received GET request on /details endpoint");

        try {
            String id = authService.getUserIdFromJwt();
            UserDetailsDTO userDetails = keycloakService.getUserDetails(id);
            userDetails.setId(id);
            log.info("Successfully fetched user details for ID: {}", id);
            return ResponseEntity.ok(userDetails);
        } catch (Exception e) {
            log.error("Failed to fetch user details: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }


    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@Valid @RequestBody UserProfileDTO userProfileDTO) {

        log.info("Received PUT request on /update endpoint");

        try {

            String id = authService.getUserIdFromJwt();
            String oldUsername = authService.getUsernameFromJwt();
            log.debug("Extracted user ID: {}, username: {}", id, oldUsername);

            if (userProfileDTO.getPassword() != null) {
                log.debug("User requested password update");
                keycloakService.setPasswordForUser(id, userProfileDTO.getPassword());
            }

            Map<String, Object> attributes = new HashMap<>();
            attributes.put("firstName", userProfileDTO.getFirstName());
            attributes.put("lastName", userProfileDTO.getLastName());
            attributes.put("email", userProfileDTO.getEmail());
            attributes.put("username", userProfileDTO.getUsername());

            log.debug("Updating user {} with attributes: {}", id, attributes);
            keycloakService.updateUserDetails(id, attributes);

            log.info("Successfully updated user {}", id);
            return ResponseEntity.ok().body("User updated successfully");
        } catch (Exception e) {
            log.error("Failed to update user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong while updating user");
        }

    }

    @DeleteMapping("/account")
    public ResponseEntity<?> deleteAccount(HttpServletRequest request) {

        String userId = authService.getUserIdFromJwt();
        String username = authService.getUsernameFromJwt();
        log.warn("[SECURITY] User '{}' (ID: {}) requested account deletion", username, userId);
        try {
            String id = authService.getUserIdFromJwt();
            fileActionService.deleteFileActionbySenderId(UUID.fromString(id));
            log.debug("Deleted file actions for user {}", id);

            String bucketId = userBucketService.getBucketUuid(UUID.fromString(id));
            fileService.deleteFilesByBucketId(UUID.fromString(bucketId));
            log.debug("Deleted files from bucket {}", bucketId);

            minIOService.deleteBucket(bucketId);
            userBucketService.deleteUserBucket(UUID.fromString(id));
            log.debug("Deleted bucket and user-bucket mapping for {}", id);

            keycloakService.deleteUser(id);
            log.info("[ACCOUNT] Account deleted successfully for user '{}'", username);
            String idToken = cookieService.extractCookie(request, "id_token");

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("message", "Account deleted successfully");
            responseBody.put("id_token", idToken);

            return ResponseEntity.ok(responseBody);

        } catch (Exception e) {
            log.error("Failed to delete user account: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete account");
        }
    }

    @GetMapping("/activity/{userId}")
    public ResponseEntity<?> getUserActivity(@PathVariable UUID userId) {
        String username = authService.getUsernameFromJwt();

        try {
            Map<String, Object> response = new HashMap<>();
            response.put("signedDocs", fileActionService.getSignedDocuments(userId));
            response.put("declinedDocs", fileActionService.getDeclinedDocuments(userId));
            response.put("approvedDocs", fileActionService.getApprovedDocuments(userId));
            response.put("viewedDocs", fileActionService.getViewedDocuments(userId));
            response.put("lastActivity", fileActionService.getLastAction(userId));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            SecurityLogger.log("USER_ACTIVITY_FAIL", username, "Failed to fetch user activity: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Error fetching user activity");
        }
    }
}
