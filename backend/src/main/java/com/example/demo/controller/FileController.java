package com.example.demo.controller;

import com.example.demo.DTO.DeclineRequestDTO;
import com.example.demo.DTO.FilesInfoDTO;
import com.example.demo.entities.*;
import com.example.demo.models.*;
import com.example.demo.services.*;
import com.example.demo.utils.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/files")
public class FileController {

    @Autowired
    private MinIOService minIOService;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserBucketService userBucketService;

    @Autowired
    private FileService fileService;

    @Autowired
    private FileActionService fileActionService;

    @Autowired
    private KeycloakService keycloakService;
    @Autowired
    private EmailService emailService;

    @Autowired
    private FileStatusService fileStatusService;

    @Autowired
    private FileLoaderService fileLoaderService;



    @GetMapping("/allFiles")
    public ResponseEntity<List<FileDetailsModel>> getUserFiles(){
        String userId=authService.getUserIdFromJwt();
        String username=authService.getUsernameFromJwt();

        try {
            SecurityLogger.log("GET_USER_FILES", username, userId, "Requested list of uploaded files");
            List<FileDetailsModel> result = fileLoaderService.loadUserFiles(userId, username);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            SecurityLogger.log("GET_USER_FILES_FAIL", username, userId, "Failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardData(){
        String id= authService.getUserIdFromJwt();

        UserBucket userBucket= userBucketService.getUserBucket(UUID.fromString(id));
        List<Files> files= fileService.getFilesByBucket(userBucket.getBucketUuid());
        int personal_flows=files.size();
        List<FileAction>filesForUser=fileActionService.findLatestFileActionsByReceiverId(UUID.fromString(id));
        int assigned_flows=filesForUser.size();

        int pending=0;

        for (Files file : files) {
            if(!fileStatusService.checkForPersonalFinished(file.getId()))
                pending++;
        }

        for (FileAction action : filesForUser) {
            if(fileStatusService.checkForFinished(action.getFile().getId()))
                pending++;
        }

        Map<String, Integer> response = new HashMap<>();
        response.put("personal", personal_flows);
        response.put("assigned", assigned_flows);
        response.put("pending", pending);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/fileStatus")
    public ResponseEntity<List<FileDetailsModel>> getFileStatus( @RequestParam("filename") String filename) {
        String ownerId=authService.getUserIdFromJwt();

        String username = authService.getUsernameFromJwt();

        try {
            SecurityLogger.log("FILE_STATUS", username, ownerId, "Requested file status for filename: " + filename);
        List<FileDetailsModel> fileDetails = new ArrayList<>();
        UUID file_id=fileService.getFileByUserUUIDAndObjectName(UUID.fromString(ownerId),filename).getId();
        List<FileAction> fileStatus = fileActionService.getFileActionsByFileId(file_id);
        if (fileStatus == null || fileStatus.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        for (FileAction row : fileStatus) {
            String receiverUsername = (String) keycloakService.getUserById(row.getReceiverId().toString(), UserDetails.USERNAME);
            DocumentStatus status = DocumentStatus.valueOf(row.getAction().toString());

            FileDetailsModel aux = new FileDetailsModel();
            aux.setReceiverUsername(receiverUsername);
            aux.setReceiverStatus(status);

            fileDetails.add(aux);

            System.out.println("Receiver Name: " + receiverUsername);
            System.out.println("Status: " + status);
        }
        return ResponseEntity.ok(fileDetails);
        } catch (Exception e) {
            SecurityLogger.log("FILE_STATUS_FAIL", username, ownerId, "Failed to get file status for '" + filename + "': " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/lastFiles")
    public ResponseEntity<List<FilesInfoDTO>> getLastFiles() throws Exception {
        String userId=authService.getUserIdFromJwt();
        String username = authService.getUsernameFromJwt();

        try {
            SecurityLogger.log("LAST_FILES", username, userId, "Fetching last uploaded files");

        List<FilesInfoDTO> lastFiles=fileService.getLastFiles(UUID.fromString(userId));
        return ResponseEntity.ok(lastFiles);

        } catch (Exception e) {
            SecurityLogger.log("LAST_FILES_FAIL", username, userId, "Failed to fetch last files: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }



    @GetMapping("/assigned")
    public ResponseEntity<List<FileDetailsModel>> getAssignedFlows() {
        String userId = authService.getUserIdFromJwt();
        String username = authService.getUsernameFromJwt();

        try {
            SecurityLogger.log("ASSIGNED_FLOWS", username, userId, "Fetching assigned flows");

            List<FileDetailsModel> result = fileLoaderService.loadAssignedFiles(userId, username);

            if (result.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            SecurityLogger.log("ASSIGNED_FLOWS_FAIL", username, userId,
                    "Error fetching assigned flows: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/filesAction")
    public ResponseEntity<List<FileDetailsModel>> getFilesToSign(@RequestParam ("action") int action) throws Exception {
        String userId=authService.getUserIdFromJwt();
        String username = authService.getUsernameFromJwt();

        try {
            SecurityLogger.log("FILES_ACTION", username, userId, "Fetching files with action: " + action);

        List<FileAction> filesForUser = new ArrayList<>();


        if(action==DocumentStatus.TO_SIGN.getCode()) {
            filesForUser=fileActionService.geFileActionsByReceiverIdAndFileIdAndAction(UUID.fromString(userId),FileActionType.TO_SIGN);
        }

        if(action==DocumentStatus.TO_APPROVE.getCode()) {
            filesForUser = fileActionService.geFileActionsByReceiverIdAndFileIdAndAction(UUID.fromString(userId),FileActionType.TO_APPROVE);
        }

        if(action==DocumentStatus.TO_VIEW.getCode()) {
            filesForUser = fileActionService.geFileActionsByReceiverIdAndFileIdAndAction(UUID.fromString(userId),FileActionType.TO_VIEW);
        }

        if(action==DocumentStatus.APPROVED.getCode()) {
            filesForUser = fileActionService.geFileActionsByReceiverIdAndFileIdAndAction(UUID.fromString(userId),FileActionType.APPROVED);
        }

        if(action==DocumentStatus.SIGNED.getCode()){
            filesForUser = fileActionService.geFileActionsByReceiverIdAndFileIdAndAction(UUID.fromString(userId),FileActionType.SIGNED);
        }


        if ( filesForUser== null || filesForUser.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        List<FileDetailsModel> filesDetails = new ArrayList<>();

        for (FileAction row : filesForUser) {
            String senderUsername = keycloakService.getUserById(row.getSender().getUserId().toString(), UserDetails.USERNAME);
           String filename =fileService.getFileByFileId(row.getFile().getId()).getObjectName();
            String fileId=row.getFile().getId().toString();
            String fileUrl=minIOService.getPresignedUrl(fileId,row.getSender().getBucketUuid().toString(),filename);
            FileDetailsModel aux = new FileDetailsModel();
            aux.setOwnerUsername(senderUsername);
            aux.setFilename(filename);
            aux.setFileId(fileId);
            aux.setFileUrl(fileUrl);


            filesDetails.add(aux);

        }
        return ResponseEntity.ok(filesDetails);
        } catch (Exception e) {
            SecurityLogger.log("FILES_ACTION_FAIL", username, userId, "Failed to fetch files for action: " + action + " - " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/fileData")
    public ResponseEntity<byte[]> getFile(@RequestBody Map<String, String> requestBody) {

        String fileId=requestBody.get("fileId");
        String userId = authService.getUserIdFromJwt();
        String username = authService.getUsernameFromJwt();

        try {
        Files file=fileService.getFileByFileId(UUID.fromString(fileId));
        String fileExtention=file.getObjectName().substring(file.getObjectName().lastIndexOf(".")+1);
        String filename=file.getId().toString()+"."+fileExtention;

        byte[] file_stream=minIOService.getFile(filename,file.getUserBucket().getBucketUuid().toString());
            SecurityLogger.log("DOWNLOAD_FILE", username, userId, "Downloaded file " + filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                .body(file_stream);
        } catch (Exception e) {
            SecurityLogger.log("DOWNLOAD_FILE_FAIL", username, userId, "Error downloading file " + fileId + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @PostMapping("declineRequest")
    public ResponseEntity<String> denyRequest(@RequestBody DeclineRequestDTO declineRequest){
        String receiverId=authService.getUserIdFromJwt();
        String receiverUsername=authService.getUsernameFromJwt();
        try {
        String senderId=keycloakService.getUserIdByUsername(declineRequest.getSenderUsername());
        String senderEmail=keycloakService.getUserById(senderId, UserDetails.EMAIL);
        Files file=fileService.getFileByFileId(UUID.fromString(declineRequest.getFileId()));
        fileActionService.createFileAction(file.getId(),UUID.fromString(senderId),UUID.fromString(receiverId), FileActionType.DECLINED);
        emailService.sendEmailNotification(senderEmail,receiverUsername,file.getObjectName(), FileActionType.DECLINED,file.getId(),UUID.fromString(receiverId));
        if(fileStatusService.checkForFinished(file.getId())){
            fileStatusService.updateFileStatus(file.getId(), FileStatusEnum.FINISHED);
        }
            SecurityLogger.log("DECLINE_REQUEST", receiverUsername, receiverId, "Declined file " + file.getObjectName());
        return ResponseEntity.ok("Request denied");

        } catch (Exception e) {
            SecurityLogger.log("DECLINE_REQUEST_FAIL", receiverUsername, receiverId, "Error declining request: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error declining request");
        }
    }

    @PostMapping("/declineRequest/bulk")
    public ResponseEntity<String> denyRequestsBulk(@RequestBody List<DeclineRequestDTO> declineRequests) {
        String receiverId = authService.getUserIdFromJwt();
        String receiverUsername = authService.getUsernameFromJwt();

        if (declineRequests == null || declineRequests.isEmpty()) {
            return ResponseEntity.badRequest().body("No requests provided");
        }

        for (DeclineRequestDTO declineRequest : declineRequests) {
            try {
                String senderId = keycloakService.getUserIdByUsername(declineRequest.getSenderUsername());
                String senderEmail = keycloakService.getUserById(senderId, UserDetails.EMAIL);
                Files file = fileService.getFileByFileId(UUID.fromString(declineRequest.getFileId()));

                fileActionService.createFileAction(
                        file.getId(),
                        UUID.fromString(senderId),
                        UUID.fromString(receiverId),
                        FileActionType.DECLINED
                );

                emailService.sendEmailNotification(
                        senderEmail,
                        receiverUsername,
                        file.getObjectName(),
                        FileActionType.DECLINED,
                        file.getId(),
                        UUID.fromString(senderId)
                );

                if (fileStatusService.checkForFinished(file.getId())) {
                    fileStatusService.updateFileStatus(file.getId(), FileStatusEnum.FINISHED);
                }

                SecurityLogger.log("DECLINE_REQUEST", receiverUsername, receiverId, "Declined file " + file.getObjectName());
            } catch (Exception e) {
                SecurityLogger.log("DECLINE_REQUEST_FAIL", receiverUsername, receiverId, "Error declining request for file: " + e.getMessage());

            }
        }

        return ResponseEntity.ok("All decline requests processed");
    }

    @PostMapping("/answerRequest")
    public ResponseEntity<?> answerRequest(@RequestBody Map<String, String> body) {
        String fileId = body.get("fileId");
        String receiverId=authService.getUserIdFromJwt();
        String receiverUsername=authService.getUsernameFromJwt();

        try {
        FileAction fileAction=fileActionService.getFileActionByFileIdAndReceiverId(UUID.fromString(fileId),UUID.fromString(receiverId));
        String senderEmail=keycloakService.getUserById(fileAction.getSender().getUserId().toString(), UserDetails.EMAIL);

        String senderId=fileAction.getSender().getUserId().toString();

        Files file=fileAction.getFile();

        FileActionType actionType = FileActionType.valueOf(fileAction.getAction().toString());

        switch (actionType) {
            case TO_APPROVE -> {
                fileActionService.createFileAction(file.getId(),UUID.fromString(senderId),UUID.fromString(receiverId), FileActionType.APPROVED);
                emailService.sendEmailNotification(senderEmail,receiverUsername,file.getObjectName(), FileActionType.APPROVED,file.getId(),UUID.fromString(senderId));
                SecurityLogger.log("APPROVED_FILE", receiverUsername, receiverId, "Approved file " + file.getObjectName());
            }
            case TO_VIEW -> {
                fileActionService.createFileAction(file.getId(),UUID.fromString(senderId),UUID.fromString(receiverId), FileActionType.VIEWED);
                emailService.sendEmailNotification(senderEmail,receiverUsername,file.getObjectName(), FileActionType.VIEWED,file.getId(),UUID.fromString(senderId));
                SecurityLogger.log("VIEWED_FILE", receiverUsername, receiverId, "Viewed file " + file.getObjectName());
            }

        }

            if(fileStatusService.checkForFinished(file.getId())){
                fileStatusService.updateFileStatus(file.getId(), FileStatusEnum.FINISHED);
            }
        return ResponseEntity.ok("Answer sent");
        } catch (Exception e) {
            SecurityLogger.log("ANSWER_REQUEST_FAIL", receiverUsername, receiverId, "Error answering request: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error answering request");
        }
    }

    @PostMapping("/answerRequest/bulk")
    public ResponseEntity<?> answerMultipleRequests(@RequestBody List<Map<String, String>> requests) {
        String receiverId = authService.getUserIdFromJwt();
        String receiverUsername = authService.getUsernameFromJwt();

        try {
            for (Map<String, String> body : requests) {
                String fileId = body.get("fileId");

                FileAction fileAction = fileActionService.getFileActionByFileIdAndReceiverId(
                        UUID.fromString(fileId),
                        UUID.fromString(receiverId)
                );

                String senderEmail = keycloakService.getUserById(
                        fileAction.getSender().getUserId().toString(),
                        UserDetails.EMAIL
                );

                String senderId = fileAction.getSender().getUserId().toString();
                Files file = fileAction.getFile();
                FileActionType actionType = FileActionType.valueOf(fileAction.getAction().toString());

                switch (actionType) {
                    case TO_APPROVE -> {
                        fileActionService.createFileAction(
                                file.getId(),
                                UUID.fromString(senderId),
                                UUID.fromString(receiverId),
                                FileActionType.APPROVED
                        );
                        emailService.sendEmailNotification(
                                senderEmail, receiverUsername, file.getObjectName(),
                                FileActionType.APPROVED, file.getId(), UUID.fromString(senderId)
                        );
                        SecurityLogger.log("APPROVED_FILE", receiverUsername, receiverId, "Approved file " + file.getObjectName());
                    }
                    case TO_VIEW -> {
                        fileActionService.createFileAction(
                                file.getId(),
                                UUID.fromString(senderId),
                                UUID.fromString(receiverId),
                                FileActionType.VIEWED
                        );
                        emailService.sendEmailNotification(
                                senderEmail, receiverUsername, file.getObjectName(),
                                FileActionType.VIEWED, file.getId(), UUID.fromString(senderId)
                        );
                        SecurityLogger.log("VIEWED_FILE", receiverUsername, receiverId, "Viewed file " + file.getObjectName());
                    }
                }

                if (fileStatusService.checkForFinished(file.getId())) {
                    fileStatusService.updateFileStatus(file.getId(), FileStatusEnum.FINISHED);
                }
            }

            return ResponseEntity.ok("All answers processed successfully.");
        } catch (Exception e) {
            SecurityLogger.log("ANSWER_REQUEST_BULK_FAIL", receiverUsername, receiverId, "Error answering bulk request: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error answering one or more requests.");
        }
    }



    @GetMapping("/fileStatusActions")
    public ResponseEntity<List<Map<String, String>>> getFileStatusActions(@RequestParam("fileId") String fileId) {
        try {
        Files file = fileService.getFileByFileId(UUID.fromString(fileId));

        if (file == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonList(Map.of("error", "File not found")));
        }

        List<FileActionStatus> fileStatusList = fileActionService.getFileActions(file.getId());

        List<Map<String, String>> resultList = new ArrayList<>();

        for (FileActionStatus status : fileStatusList) {
            Map<String, String> statusMap = new HashMap<>();
            String receiverUsername=keycloakService.getUserById(status.getReceiverId().toString(), UserDetails.USERNAME);
            statusMap.put("receiverUsername", receiverUsername);
            statusMap.put("actions", status.getActions().get(0).toString());
            statusMap.put("currentStatus", status.getCurrentStatus());

            resultList.add(statusMap);
        }

        return ResponseEntity.ok(resultList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/delete")
    public ResponseEntity<String> deleteFile(@RequestBody Map<String, String> requestData) {
        String fileId = requestData.get("fileId");
        String filename = requestData.get("filename");

        if (fileId == null || filename == null) {
            return ResponseEntity.badRequest().body("Missing required parameters");
        }

        String userId = authService.getUserIdFromJwt();
        String username = authService.getUsernameFromJwt();

        try {
        String bucketUUID = userBucketService.getBucketUuid(UUID.fromString(userId));
        minIOService.deleteFileFromMinIO(filename, bucketUUID);
        fileActionService.deleteAllFileActionsByFile(UUID.fromString(fileId));
        fileService.deleteFile(UUID.fromString(fileId));

        return ResponseEntity.ok("File deleted");
        } catch (Exception e) {
            SecurityLogger.log("DELETE_FILE_FAIL", username, userId, "Failed to delete file " + filename + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting file");
        }
    }


    @PostMapping("/delete-multiple-files")
    public ResponseEntity<?> deleteMultipleFiles(@RequestBody List<String> fileIds) {
        String username = authService.getUsernameFromJwt();
        List<String> failedFiles = new ArrayList<>();

        for (String fileId : fileIds) {
            try {
                Files file = fileService.getFileByFileId(UUID.fromString(fileId));

                String bucketUUID = file.getUserBucket().getBucketUuid().toString();
                String filename = file.getObjectName();

                minIOService.deleteFileFromMinIO(filename, bucketUUID);

                fileActionService.deleteAllFileActionsByFile(UUID.fromString(fileId));
                fileService.deleteFile(UUID.fromString(fileId));

                SecurityLogger.log("USER",username , "Administrator deleted file " + filename);

            } catch (Exception e) {
                failedFiles.add(fileId);
                SecurityLogger.log("DELETE_FILE_FAIL", username, "Failed to delete file " + fileId + ": " + e.getMessage());
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


}
