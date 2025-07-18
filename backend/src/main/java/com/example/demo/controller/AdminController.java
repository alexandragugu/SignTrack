package com.example.demo.controller;

import com.example.demo.DTO.*;
import com.example.demo.entities.FileAction;
import com.example.demo.entities.Files;
import com.example.demo.models.FileActionStatus;
import com.example.demo.models.FileDetailsModel;
import com.example.demo.models.UserAction;
import com.example.demo.services.*;
import com.example.demo.utils.FileActionType;
import com.example.demo.utils.SecurityLogger;
import com.example.demo.utils.UserDetails;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    KeycloakService keycloakService;

    @Autowired
    AdminService adminService;

    @Autowired
    AuthService authService;

    @Autowired
    FileActionService fileActionService;

    @Autowired
    MinIOService minIOService;

    @Autowired
    UserBucketService userBucketService;

    @Autowired
    FileService fileService;

    @Autowired
    EmailService emailService;

    @Autowired
    SignaturesService signaturesService;

    @Autowired
    FileStatusService fileStatusService;

    @Autowired
    AdminsService adminsService;

    @Autowired
    private CookieService cookieService;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private String extractErrorMessage(String responseBody) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(responseBody);
            return node.has("errorMessage") ? node.get("errorMessage").asText() : "Conflict occurred";
        } catch (Exception ex) {
            return "Conflict occurred";
        }
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        String requesterId = authService.getUserIdFromJwt();
        String requesterUsername = authService.getUsernameFromJwt();

        try {
            SecurityLogger.log("ADMIN_VIEW_USERS", requesterUsername, requesterId, "Requested full user list");

            List<UserResponseDTO> users = keycloakService.getAllAccounts();

            users.removeIf(user -> requesterId.equals(user.getId()));

            List<UUID> createdAdminIds = adminsService.findCreatedAdminIdsBy(UUID.fromString(requesterId));

            for (UserResponseDTO user : users) {
                if (user.getId() != null && createdAdminIds.contains(UUID.fromString(user.getId()))) {
                    user.setIsCreator(true);
                } else {
                    user.setIsCreator(false);
                }
            }

            return ResponseEntity.ok(users);
        } catch (Exception e) {
            SecurityLogger.log("ADMIN_VIEW_USERS_FAIL", requesterUsername, requesterId, "Failed to fetch users: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/users/created-admins")
    public ResponseEntity<List<UserResponseDTO>> getAllCreatedAdmins(@RequestParam("userId") UUID adminId) {
        String requesterUsername = authService.getUsernameFromJwt();

        try {
            SecurityLogger.log("ADMIN_VIEW_CREATED_USERS", requesterUsername, adminId.toString(), "Requested created user list");

            List<UserResponseDTO> users = keycloakService.getAllUsers();

            users.removeIf(user -> adminId.toString().equals(user.getId()));

            List<UUID> createdAdminIds = adminsService.findCreatedAdminIdsBy(adminId);

            users.removeIf(user -> user.getId() == null || !createdAdminIds.contains(UUID.fromString(user.getId())));

            for (UserResponseDTO user : users) {
                user.setIsCreator(true);
            }

            return ResponseEntity.ok(users);
        } catch (Exception e) {
            SecurityLogger.log("ADMIN_VIEW_CREATED_USERS_FAIL", requesterUsername, adminId.toString(), "Failed to fetch created users: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDetailsDTO> getUserById(@PathVariable String id, Authentication authentication) {

        String requesterId = authService.getUserIdFromJwt();
        String requesterUsername = authService.getUsernameFromJwt();

        if (!hasAdminRole(authentication)) {
            SecurityLogger.log("ACCESS_DENIED", requesterUsername, requesterId, "Tried to access user details for ID: " + id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        try {
            SecurityLogger.log("ADMIN_VIEW_USER", requesterUsername, requesterId, "Accessed details for user ID: " + id);


            UserDetailsDTO userDetailsDTO= keycloakService.getUserDetails(id);
        userDetailsDTO.setDocumentsUploaded(adminService.getNumberOfUploadedFiles(UUID.fromString(id)));
        userDetailsDTO.setDocumentsSigned(adminService.getNumberOfSignedFiles(UUID.fromString(id)));
        return ResponseEntity.ok(userDetailsDTO);
        } catch (Exception e) {
            SecurityLogger.log("ADMIN_VIEW_USER_FAIL", requesterUsername, requesterId, "Failed to retrieve user ID " + id + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/users/admin/{id}")
    public ResponseEntity<UserDetailsDTO> getAdminById(@PathVariable String id, Authentication authentication) {

        String requesterId = authService.getUserIdFromJwt();
        String requesterUsername = authService.getUsernameFromJwt();

        if (!hasAdminRole(authentication)) {
            SecurityLogger.log("ACCESS_DENIED", requesterUsername, requesterId, "Tried to access user details for ID: " + id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        try {
            SecurityLogger.log("ADMIN_VIEW_USER", requesterUsername, requesterId, "Accessed details for user ID: " + id);


            UserDetailsDTO userDetailsDTO= keycloakService.getUserDetails(id);
            if(adminsService.isCreatedBy(UUID.fromString(requesterId), UUID.fromString(id))){
                userDetailsDTO.setCreator(true);
            }else{
                userDetailsDTO.setCreator(false);
            }
            return ResponseEntity.ok(userDetailsDTO);
        } catch (Exception e) {
            SecurityLogger.log("ADMIN_VIEW_USER_FAIL", requesterUsername, requesterId, "Failed to retrieve user ID " + id + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/account")
    public ResponseEntity<UserDetailsDTO> getPersonalData(Authentication authentication) {

        String requesterId = authService.getUserIdFromJwt();
        String requesterUsername = authService.getUsernameFromJwt();

        if (!hasAdminRole(authentication)) {
            SecurityLogger.log("ACCESS_DENIED", requesterUsername, requesterId, "Tried to access user details for ID: " + requesterId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        try {
            SecurityLogger.log("ADMIN_VIEW_USER", requesterUsername, requesterId, "Accessed details for user ID: " + requesterId);


            UserDetailsDTO userDetailsDTO= keycloakService.getUserDetails(requesterId);
            userDetailsDTO.setId(requesterId);
            userDetailsDTO.setCreator(adminsService.isCreatedByAnotherAdmin(UUID.fromString(requesterId)));
            return ResponseEntity.ok(userDetailsDTO);
        } catch (Exception e) {
            SecurityLogger.log("ADMIN_VIEW_USER_FAIL", requesterUsername, requesterId, "Failed to retrieve user ID " + requesterId + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    private boolean hasAdminRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_Admin"));
    }


    @PutMapping("/users/update")
    public ResponseEntity<?> updateUser(@RequestBody UserProfileDTO userProfileDTO){

        log.info("Received PUT request on /update endpoint");

        try {

            String id = userProfileDTO.getUserId();

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

    @DeleteMapping("/users/delete/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String id){

        String userId = authService.getUserIdFromJwt();
        String username = authService.getUsernameFromJwt();
        log.warn("[SECURITY] User '{}' (ID: {}) requested account deletion", username, userId);
        try {

            fileActionService.deleteFileActionbySenderId(UUID.fromString(id));
            log.debug("Deleted file actions for user {}", id);

            String bucketId=userBucketService.getBucketUuid(UUID.fromString(id));
            fileService.deleteFilesByBucketId(UUID.fromString(bucketId));
            log.debug("Deleted files from bucket {}", bucketId);

            minIOService.deleteBucket(bucketId);
            userBucketService.deleteUserBucket(UUID.fromString(id));
            log.debug("Deleted bucket and user-bucket mapping for {}", id);

            keycloakService.deleteUser(id);
            log.info("[ACCOUNT] Account deleted successfully for user '{}'", username);

            return ResponseEntity.ok().body("Account deleted successfully");
        } catch (Exception e) {
            log.error("Failed to delete user account: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete account");
        }
    }


    @DeleteMapping("/users/delete-admin/{id}")
    public ResponseEntity<?> deleteAdmin(@PathVariable String id) {
        String userId = authService.getUserIdFromJwt();
        String username = authService.getUsernameFromJwt();
        log.warn("[SECURITY] User '{}' (ID: {}) requested account deletion", username, userId);

        try {
            adminsService.updateCreatorForAdmin(UUID.fromString(id), UUID.fromString(userId));
            adminsService.deleteByAdminId(UUID.fromString(id));
            keycloakService.deleteUser(id);
            log.info("[ACCOUNT] Account deleted successfully for user '{}'", username);

            return ResponseEntity.ok().body("Account deleted successfully");
        } catch (Exception e) {
            log.error("Failed to delete user account: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete account");
        }
    }


    @DeleteMapping("/account")
    public ResponseEntity<?> deletePersonalAccount(HttpServletRequest request) {
        String userId = authService.getUserIdFromJwt();
        String username = authService.getUsernameFromJwt();
        log.warn("[SECURITY] User '{}' (ID: {}) requested account deletion", username, userId);

        try {
            adminsService.updateCreatorForAdmin(UUID.fromString(userId), UUID.fromString(userId));
            adminsService.deleteByAdminId(UUID.fromString(userId));
            keycloakService.deleteUser(userId);
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

    @PostMapping("/users/delete-multiple")
    public ResponseEntity<?> deleteMultipleUsers(@RequestBody List<String> userIds) {
        String requestorId = authService.getUserIdFromJwt();
        String requestorUsername = authService.getUsernameFromJwt();
        log.warn("[SECURITY] User '{}' (ID: {}) requested deletion of multiple accounts", requestorUsername, requestorId);

        List<String> failedDeletions = new ArrayList<>();

        for (String id : userIds) {
            try {
                fileActionService.deleteFileActionbySenderId(UUID.fromString(id));
                log.debug("Deleted file actions for user {}", id);

                String bucketId=userBucketService.getBucketUuid(UUID.fromString(id));
                fileService.deleteFilesByBucketId(UUID.fromString(bucketId));
                log.debug("Deleted files from bucket {}", bucketId);

                minIOService.deleteBucket(bucketId);
                userBucketService.deleteUserBucket(UUID.fromString(id));
                log.debug("Deleted bucket and user-bucket mapping for {}", id);

                keycloakService.deleteUser(id);
                log.info("[ACCOUNT] Account deleted successfully for user '{}'", id);

            } catch (Exception e) {
                log.error("Failed to delete user {}: {}", id, e.getMessage());
                failedDeletions.add(id);
            }
        }

        if (failedDeletions.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "All users deleted successfully."));
        } else {
            String friendlyMessage = String.format(
                    "Some users could not be deleted. (%d out of %d)",
                    failedDeletions.size(),
                    userIds.size()
            );

            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .body(Map.of(
                            "errorMessage", friendlyMessage,
                            "failedUserIds", failedDeletions
                    ));
        }
    }


    @PostMapping("/users/delete-multiple/admins")
    public ResponseEntity<?> deleteMultipleAdmins(@RequestBody List<String> userIds) {
        String requestorId = authService.getUserIdFromJwt();
        String requestorUsername = authService.getUsernameFromJwt();
        log.warn("[SECURITY] User '{}' (ID: {}) requested deletion of multiple accounts", requestorUsername, requestorId);

        List<String> failedDeletions = new ArrayList<>();

        for (String id : userIds) {
            try {
                adminsService.updateCreatorForAdmin(UUID.fromString(id), UUID.fromString(requestorId));
                adminsService.deleteByAdminId(UUID.fromString(id));
                keycloakService.deleteUser(id);
                log.info("[ACCOUNT] Account deleted successfully for user ID '{}'", id);
            } catch (Exception e) {
                log.error("Failed to delete user {}: {}", id, e.getMessage());
                failedDeletions.add(id);
            }
        }

        if (failedDeletions.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "All users deleted successfully."));
        } else {
            String friendlyMessage = String.format(
                    "Some users could not be deleted. (%d out of %d)",
                    failedDeletions.size(),
                    userIds.size()
            );

            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .body(Map.of(
                            "errorMessage", friendlyMessage,
                            "failedUserIds", failedDeletions
                    ));
        }
    }

    @PostMapping("/users/create")
    public ResponseEntity<?> createUser(@RequestBody UserProfileDTO userProfileDTO) {
        log.info("Received POST request on /users/create endpoint");

        try {
            Map<String, Object> userPayload = new HashMap<>();
            userPayload.put("username", userProfileDTO.getUsername());
            userPayload.put("enabled", true);
            userPayload.put("email", userProfileDTO.getEmail());
            userPayload.put("firstName", userProfileDTO.getFirstName());
            userPayload.put("lastName", userProfileDTO.getLastName());

            String userId = keycloakService.createUser(userPayload);
            log.info("Created user with ID: {}", userId);

            if (userProfileDTO.getPassword() != null) {
                keycloakService.setPasswordForUser(userId, userProfileDTO.getPassword());
                log.info("Password set for user: {}", userId);
            }

            keycloakService.assignRealmRoleToUser(userId, "Admin");
            log.info("Assigned Admin role to user: {}", userId);

            UUID createdById = UUID.fromString(authService.getUserIdFromJwt());
            UUID newAdminId = UUID.fromString(userId);
            adminsService.addAdmin(createdById, newAdminId);
            return ResponseEntity.ok("User created successfully with Admin role");
        } catch (HttpClientErrorException.Conflict e) {
            log.warn("User creation failed with conflict: {}", e.getResponseBodyAsString());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("errorMessage", extractErrorMessage(e.getResponseBodyAsString())));
        } catch (RestClientException e) {
            log.error("Client error during user creation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("errorMessage", "External service error: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to create user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("errorMessage", "Something went wrong while creating user"));
        }
    }



    @GetMapping("/systemFiles")
    public ResponseEntity<List<FileDetailsModel>> getAssignedFlows() {
        String username = authService.getUsernameFromJwt();

        try {
            SecurityLogger.log("ADMIN", username, "Fetching system flows");

            List<FileAction> filesForUser = fileActionService.getAllFileActions();

            if (filesForUser == null || filesForUser.isEmpty()) {
                return ResponseEntity.badRequest().body(null);
            }

            List<FileDetailsModel> filesDetails = filesForUser.parallelStream().map(row -> {
                FileDetailsModel fileDetailsModel = new FileDetailsModel();

                try {
                    UUID fileId = row.getFile().getId();
                    String objectName = row.getFile().getObjectName();
                    UUID bucketUuid = row.getSender().getBucketUuid();

                    fileDetailsModel.setFilename(objectName);
                    fileDetailsModel.setFileId(fileId.toString());
                    fileDetailsModel.setFileUrl(minIOService.getPresignedUrl(
                            fileId.toString(), bucketUuid.toString(), objectName));

                    fileDetailsModel.setOwnerUsername(
                            keycloakService.getUserById(row.getSender().getUserId().toString(), UserDetails.USERNAME));

                    fileDetailsModel.setFileStatus("Finished");

                    List<FileActionStatus> fileStatusList = fileActionService.getFileActions(fileId);
                    List<UserAction> resultList = new ArrayList<>();

                    for (FileActionStatus status : fileStatusList) {
                        UserAction userAction = new UserAction();
                        userAction.setUserId(status.getReceiverId().toString());
                        userAction.setUsername(status.getUsername());
                        userAction.setAction(status.getCurrentStatus());
                        userAction.setCurrentDate(status.getCurrentStatusDate());

                        String role = userAction.mapOwerRole(status.getCurrentStatus());
                        fileDetailsModel.setOwnerRole(role);
                        fileDetailsModel.setActionRequired(status.getCurrentStatus());
                        fileDetailsModel.setDate(status.getCurrentStatusDate().toString());

                        if (fileDetailsModel.getFileStatus().equals("Finished")
                                && status.getCurrentStatus().contains("TO_")) {
                            fileDetailsModel.setFileStatus("Pending");
                        }

                        resultList.add(userAction);
                    }

                    fileDetailsModel.setReceiverActions(resultList);

                } catch (Exception e) {
                    // log individual errors but continue processing other files
                    SecurityLogger.log("ADMIN", username, "Error processing file ID: " +
                            row.getFile().getId() + " - " + e.getMessage());
                }

                return fileDetailsModel;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(filesDetails);

        } catch (Exception e) {
            SecurityLogger.log("ADMIN", username, "Error fetching assigned flows: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @DeleteMapping("/delete-file")
    public ResponseEntity<String> deleteFile(@RequestBody Map<String, String> requestData) {
        String fileId = requestData.get("fileId");
        String filename = requestData.get("filename");

        String adminUsername= authService.getUsernameFromJwt();

        if (fileId == null || filename == null) {
            return ResponseEntity.badRequest().body("Missing required parameters");
        }

        try {
            Files file= fileService.getFileByFileId(UUID.fromString(fileId));

            String bucketUUID = file.getUserBucket().getBucketUuid().toString();
            String ownerEmail= keycloakService.getUserById(file.getUserBucket().getUserId().toString(), UserDetails.EMAIL);
            String ownerId= file.getUserBucket().getUserId().toString();
            minIOService.deleteFileFromMinIO(filename, bucketUUID);
            fileActionService.deleteAllFileActionsByFile(UUID.fromString(fileId));
            fileService.deleteFile(UUID.fromString(fileId));
            emailService.sendAdminNotification(ownerEmail,adminUsername,filename, FileActionType.ADMIN_DELETE, UUID.fromString(fileId), UUID.fromString(ownerId));
            SecurityLogger.log("ADMIN", adminUsername, "Administrator deleted file " + filename);
            return ResponseEntity.ok("File deleted");
        } catch (Exception e) {
            SecurityLogger.log("DELETE_FILE_FAIL", adminUsername, "Failed to delete file " + filename + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting file");
        }
    }

    @PostMapping("/files/delete-multiple-files")
    public ResponseEntity<?> deleteMultipleFiles(@RequestBody List<String> fileIds) {
        String adminUsername = authService.getUsernameFromJwt();
        List<String> failedFiles = new ArrayList<>();

        for (String fileId : fileIds) {
            try {
                Files file = fileService.getFileByFileId(UUID.fromString(fileId));

                String bucketUUID = file.getUserBucket().getBucketUuid().toString();
                String filename = file.getObjectName();
                String ownerEmail = keycloakService.getUserById(
                        file.getUserBucket().getUserId().toString(),
                        UserDetails.EMAIL
                );
                String ownerId = file.getUserBucket().getUserId().toString();

                minIOService.deleteFileFromMinIO(filename, bucketUUID);

                fileActionService.deleteAllFileActionsByFile(UUID.fromString(fileId));
                fileService.deleteFile(UUID.fromString(fileId));

                emailService.sendEmailNotification(
                        ownerEmail,
                        adminUsername,
                        filename,
                        FileActionType.ADMIN_DELETE,
                        UUID.fromString(fileId),
                        UUID.fromString(ownerId)
                );

                SecurityLogger.log("ADMIN", adminUsername, "Administrator deleted file " + filename);

            } catch (Exception e) {
                failedFiles.add(fileId);
                SecurityLogger.log("DELETE_FILE_FAIL", adminUsername, "Failed to delete file " + fileId + ": " + e.getMessage());
            }
        }

        if (failedFiles.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "All files deleted successfully."));
        } else {
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .body(Map.of(
                            "errorMessage", "Some files could not be deleted. (" + failedFiles.size() + " of " + fileIds.size() + ")",
                            "failedFileIds", failedFiles
                    ));
        }
    }


    @GetMapping("/signature-preview")
    public ResponseEntity<?> getSignaturePreview() {
        String adminUsername = authService.getUsernameFromJwt();

        try {
            int totalSignatures = signaturesService.getTotalSignatures();
            MostActiveSignerDTO mostActiveSigner = signaturesService.findMostActiveSigner()
                    .orElse(new MostActiveSignerDTO());
            int thisMonthSignatures = signaturesService.getThisMonthSignatures();

            Map<String, Object> response = new HashMap<>();
            response.put("totalSignedFiles", totalSignatures);
            response.put("mostActiveSigner", mostActiveSigner);
            response.put("thisMonthSigned", thisMonthSignatures);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            SecurityLogger.log(
                    "SIGNATURE_PREVIEW_FAIL",
                    adminUsername,
                    "Failed to fetch signature preview: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching signature preview");
        }
    }

    @GetMapping("/signature-activity")
    public ResponseEntity<?> getSignatureActivity() {
        String adminUsername = authService.getUsernameFromJwt();

        try {
            List<SignatureActivityDTO> activityList = signaturesService.getSignatureActivity();
            return ResponseEntity.ok(activityList);
        } catch (Exception e) {
            SecurityLogger.log("SIGNATURE_ACTIVITY_FAIL", adminUsername, "Failed to fetch signature activity: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Error fetching signature activity");
        }
    }

    @GetMapping ("/users/activity/{userId}")
    public ResponseEntity<?> getUserActivity(@PathVariable UUID userId) {
        String adminUsername = authService.getUsernameFromJwt();

        try {
            Map<String, Object> response = new HashMap<>();
            response.put("signedDocs", fileActionService.getSignedDocuments(userId));
            response.put("declinedDocs", fileActionService.getDeclinedDocuments(userId));
            response.put("approvedDocs", fileActionService.getApprovedDocuments(userId));
            response.put("viewedDocs", fileActionService.getViewedDocuments(userId));
            response.put("lastActivity", fileActionService.getLastAction(userId));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            SecurityLogger.log("USER_ACTIVITY_FAIL", adminUsername, "Failed to fetch user activity: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Error fetching user activity");
        }
    }

    @GetMapping("/users/personal-files/{userId}")
    public ResponseEntity<List<FileDetailsModel>> getUserFiles(@PathVariable String userId) {
        String adminId = authService.getUserIdFromJwt();
        String adminUsername = authService.getUsernameFromJwt();

        try {
            SecurityLogger.log("GET_USER_FILES", adminUsername, adminId, "Requested list of uploaded files for user " + userId);

            String bucketUUID = userBucketService.getBucketUuid(UUID.fromString(userId));
            List<Files> files = fileService.getFilesByBucket(UUID.fromString(bucketUUID));

            List<FileDetailsModel> fileDetails = files.parallelStream().map(myFile -> {
                FileDetailsModel fileDetailsModel = new FileDetailsModel();

                try {
                    fileDetailsModel.setFilename(myFile.getObjectName());
                    fileDetailsModel.setFileUrl(minIOService.getPresignedUrl(
                            myFile.getId().toString(), bucketUUID, myFile.getObjectName()));
                    fileDetailsModel.setFileId(myFile.getId().toString());
                    fileDetailsModel.setDate(myFile.getCreatedAt().toString());

                    fileDetailsModel.setFileStatus("Finished");

                    List<FileActionStatus> fileStatusList = fileActionService.getPersonalFileActions(myFile.getId());
                    List<UserAction> resultList = new ArrayList<>();

                    for (FileActionStatus status : fileStatusList) {
                        UserAction userAction = new UserAction();
                        String receiverUsername = status.getUsername();
                        userAction.setUserId(status.getReceiverId().toString());
                        userAction.setUsername(receiverUsername);
                        userAction.setAction(status.getCurrentStatus());
                        userAction.setCurrentDate(status.getCurrentStatusDate());

                        if (userId.equals(status.getReceiverId().toString())) {
                            String role = userAction.mapOwerRole(status.getCurrentStatus());
                            fileDetailsModel.setOwnerRole(role);
                            fileDetailsModel.setActionRequired(status.getCurrentStatus());
                        }

                        if (fileDetailsModel.getFileStatus().equals("Finished")
                                && status.getCurrentStatus().contains("TO_")) {
                            fileDetailsModel.setFileStatus("Pending");
                        }

                        resultList.add(userAction);
                    }

                    fileDetailsModel.setReceiverActions(resultList);

                } catch (Exception e) {
                    SecurityLogger.log("USER_FILES_THREAD_FAIL", adminUsername, adminId,
                            "Error processing file " + myFile.getId() + ": " + e.getMessage());
                }

                return fileDetailsModel;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(fileDetails);

        } catch (Exception e) {
            SecurityLogger.log("GET_USER_FILES_FAIL", adminUsername, adminId,
                    "Failed to retrieve file list: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }



    @GetMapping("/users/assigned/{userId}")
    public ResponseEntity<List<FileDetailsModel>> getAssignedFlows(@PathVariable String userId) {
        String adminId = authService.getUserIdFromJwt();
        String adminUsername = authService.getUsernameFromJwt();

        try {
            SecurityLogger.log("ASSIGNED_FLOWS", adminUsername, adminId, "Fetching assigned flows for user " + userId);

            List<FileAction> filesForUser = fileActionService.findLatestFileActionsByReceiverId(UUID.fromString(userId));

            if (filesForUser == null || filesForUser.isEmpty()) {
                return ResponseEntity.badRequest().body(null);
            }

            List<FileDetailsModel> filesDetails = filesForUser.parallelStream().map(row -> {
                FileDetailsModel fileDetailsModel = new FileDetailsModel();

                try {
                    UUID fileId = row.getFile().getId();
                    String objectName = row.getFile().getObjectName();
                    UUID bucketUuid = row.getSender().getBucketUuid();

                    fileDetailsModel.setFilename(objectName);
                    fileDetailsModel.setFileId(fileId.toString());

                    fileDetailsModel.setFileUrl(minIOService.getPresignedUrl(
                            fileId.toString(), bucketUuid.toString(), objectName));

                    fileDetailsModel.setOwnerUsername(
                            keycloakService.getUserById(row.getSender().getUserId().toString(), UserDetails.USERNAME));

                    boolean isFinished = fileStatusService.checkForFinished(fileId);
                    fileDetailsModel.setFileStatus(isFinished ? "Finished" : "Pending");

                    List<FileActionStatus> fileStatusList = fileActionService.getFileActions(fileId);
                    List<UserAction> resultList = new ArrayList<>();

                    for (FileActionStatus status : fileStatusList) {
                        UserAction userAction = new UserAction();
                        String receiverUsername = status.getUsername(); // sau din Keycloak, dacă preferi
                        userAction.setUserId(status.getReceiverId().toString());
                        userAction.setUsername(receiverUsername);
                        userAction.setAction(status.getCurrentStatus());

                        if (userId.equals(status.getReceiverId().toString())) {
                            String lastAction = status.getCurrentStatus();
                            String mappedRole;

                            if ("DECLINED".equals(lastAction)) {
                                String requestedAction = fileActionService.findLastRequestedAction(
                                        status.getFileId(), UUID.fromString(status.getReceiverId().toString()));
                                mappedRole = userAction.mapOwerRole(requestedAction);
                                userAction.setAction(lastAction + "_" + requestedAction);
                            } else {
                                mappedRole = userAction.mapOwerRole(lastAction);
                                fileDetailsModel.setActionRequired(lastAction);
                            }

                            fileDetailsModel.setOwnerRole(mappedRole);
                            fileDetailsModel.setActionRequired(lastAction);
                            fileDetailsModel.setDate(status.getCurrentStatusDate().toString());
                        }

                        if ("Finished".equals(fileDetailsModel.getFileStatus())
                                && status.getCurrentStatus().contains("TO_")) {
                            fileDetailsModel.setFileStatus("Pending");
                        }

                        resultList.add(userAction);
                    }

                    fileDetailsModel.setReceiverActions(resultList);

                } catch (Exception e) {
                    SecurityLogger.log("ASSIGNED_FLOW_PROCESS_FAIL", adminUsername, adminId,
                            "Error processing file " + row.getFile().getId() + ": " + e.getMessage());
                }

                return fileDetailsModel;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(filesDetails);

        } catch (Exception e) {
            SecurityLogger.log("ASSIGNED_FLOWS_FAIL", adminUsername, adminId,
                    "Error fetching assigned flows: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}
