package com.example.demo.controller;

import com.example.demo.DTO.UploadedFileDTO;
import com.example.demo.entities.Files;
import com.example.demo.models.FileDetailsModel;
import com.example.demo.models.FileStatusEnum;
import com.example.demo.models.UserAction;
import com.example.demo.services.*;
import com.example.demo.utils.FileActionType;
import com.example.demo.utils.UserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/email")
public class EmailController {

    private final String toSignValeu="to-sign";
    private final String toApproveValue="to-approve";
    private final String toViewValue="to-view";

    @Autowired
    private EmailService emailService;


    @Autowired
    private AuthService authService;

    @Autowired
    private FileActionService fileActionService;

    @Autowired
    private FileService fileService;
    @Autowired
    private KeycloakService keycloakService;
    @Autowired
    private MinIOService minIOService;

    @Autowired
    private UserBucketService userBucketService;

    @Autowired
    private FileStatusService fileStatusService;

    @Autowired
    private BulkFileService bulkFileService;

    @PostMapping("/sendMail")
    public ResponseEntity<FileDetailsModel> sendEmail(@RequestParam("file") MultipartFile file,@RequestParam("receivers") String receiversJson) throws Exception {
        String id=authService.getUserIdFromJwt();
        String ownerUsername=authService.getUsernameFromJwt();
        String bucketId=userBucketService.getBucketUuid(UUID.fromString(id));

        ObjectMapper mapper = new ObjectMapper();
        List<UserAction> receivers = mapper.readValue(
                receiversJson,
                new TypeReference<List<UserAction>>() {}
        );

        MultipartFile fileData= file;
        String filename=fileData.getOriginalFilename();

        Files fileObject=fileService.uploadFile(UUID.fromString(id),filename);
        String filenameMinio=minIOService.uploadMultipartFile(fileData,bucketId,fileObject.getId().toString());

        String fileUrl=minIOService.getPresignedUrl(filenameMinio,bucketId,filename);
        UploadedFileDTO newFileData=new UploadedFileDTO(filename,fileUrl);
        newFileData.setFileId(fileObject.getId().toString());
        Files myFile=fileService.getFileByFileId(fileObject.getId());



        if(receivers==null || receivers.isEmpty()){
            return ResponseEntity.badRequest().body(null);
        }

        receivers.forEach(user -> System.out.println("Received:" + user));

        FileDetailsModel fileDetails=new FileDetailsModel();

        for (UserAction user : receivers) {
            System.out.println("Processing user:" + user.getUserId() + " action:" + user.getAction());

            String userEmail=keycloakService.getUserById(user.getUserId(), UserDetails.EMAIL);
            if (user.getAction().equals(toSignValeu)) {

                emailService.sendEmailNotification(userEmail,ownerUsername, myFile.getObjectName(), FileActionType.TO_SIGN, myFile.getId(), UUID.fromString(user.getUserId()));
                fileActionService.createFileAction(myFile.getId(), UUID.fromString(id), UUID.fromString(user.getUserId()),FileActionType.TO_SIGN);

            }
            if (user.getAction().equals(toApproveValue)) {
                    emailService.sendEmailNotification(userEmail,ownerUsername, myFile.getObjectName(), FileActionType.TO_APPROVE, myFile.getId(), UUID.fromString(user.getUserId()));
                    fileActionService.createFileAction(myFile.getId(), UUID.fromString(id), UUID.fromString(user.getUserId()),FileActionType.TO_APPROVE);


            }

            if (user.getAction().equals(toViewValue)) {
                emailService.sendEmailNotification(userEmail,ownerUsername, myFile.getObjectName(), FileActionType.TO_VIEW, myFile.getId(), UUID.fromString(user.getUserId()));
                fileActionService.createFileAction(myFile.getId(), UUID.fromString(id), UUID.fromString(user.getUserId()),FileActionType.TO_VIEW);


            }



        }
        fileStatusService.createFileStatus(myFile.getId(), FileStatusEnum.PENDING);
        fileDetails.setFileId(fileObject.getId().toString());
        fileDetails.setFileUrl(fileUrl);
        fileDetails.setFilename(filename);
        return ResponseEntity.ok(fileDetails);
    }


    @PostMapping("/sendMail-bulk")
    public ResponseEntity<List<FileDetailsModel>> sendEmails(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("receivers") String receiversJson
    ) throws Exception {
        long startTime = System.currentTimeMillis();
        String userId = authService.getUserIdFromJwt();
        String ownerUsername = authService.getUsernameFromJwt();

        ObjectMapper mapper = new ObjectMapper();
        List<UserAction> receivers = mapper.readValue(receiversJson, new TypeReference<List<UserAction>>() {});

        if (receivers == null || receivers.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        List<FileDetailsModel> resultList = bulkFileService.processFilesInList(files, receivers, userId, ownerUsername);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("Durata sendEmails: " + duration + " ms");

        return ResponseEntity.ok(resultList);
    }

}
