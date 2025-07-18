package com.example.demo.services;

import com.example.demo.DTO.UploadedFileDTO;
import com.example.demo.entities.FileAction;
import com.example.demo.entities.FileStatus;
import com.example.demo.entities.Files;
import com.example.demo.models.FileDetailsModel;
import com.example.demo.models.FileStatusEnum;
import com.example.demo.models.UserAction;
import com.example.demo.utils.FileActionType;
import com.example.demo.utils.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class BulkFileService {

    @Autowired
    private FileService fileService;

    @Autowired
    private MinIOService minIOService;

    @Autowired
    private FileActionService fileActionService;

    @Autowired
    private FileStatusService fileStatusService;

    @Autowired
    private KeycloakService keycloakService;

    @Autowired
    private EmailService emailService;


    @Autowired
    private UserBucketService userBucketService;

    private final String toSignValeu="to-sign";
    private final String toApproveValue="to-approve";
    private final String toViewValue="to-view";


    public List<FileDetailsModel> processFilesInList(List<MultipartFile> files, List<UserAction> receivers, String userId, String ownerUsername) throws IOException {
        String bucketId=userBucketService.getBucketUuid(UUID.fromString(userId));
        ExecutorService executor= Executors.newFixedThreadPool(5);
        List<CompletableFuture<Optional<FileDetailsModel>>> futures = new ArrayList<>();
        Semaphore semaphore = new Semaphore(5);
        AtomicInteger fileCounter = new AtomicInteger(0);
        for (MultipartFile fileData : files) {
            byte[] fileBytes = fileData.getInputStream().readAllBytes();
            int index = fileCounter.incrementAndGet();
            if (index >= 35) {
                System.out.println("Fisier:" + index);
            }
            CompletableFuture<Optional<FileDetailsModel>> future = CompletableFuture
                    .supplyAsync(() -> {
                        try {
                            semaphore.acquire();
                            String filename=fileData.getOriginalFilename();
                            Files fileObject = fileService.uploadFile(UUID.fromString(userId), filename);
                            String filenameMinio = minIOService.uploadMultipartFileThreadSafe(fileBytes, fileData.getOriginalFilename(), bucketId, fileObject.getId().toString());

                            System.out.println("Inainte de generarea URL");
                            String fileUrl = minIOService.getPresignedUrl(fileObject.getId().toString(), bucketId, filename);
                            System.out.println("A generat url pentru: " + filename);
                            UploadedFileDTO newFileData = new UploadedFileDTO(filename, fileUrl);
                            newFileData.setFileId(fileObject.getId().toString());

                            Files myFile = fileService.getFileByFileId(fileObject.getId());


                            for (UserAction user : receivers) {
                                String userEmail = keycloakService.getUserById(user.getUserId(), UserDetails.EMAIL);
                                UUID receiverId = UUID.fromString(user.getUserId());

                                if (user.getAction().equals(toSignValeu)) {
                                    System.out.println("A daugat actiunea");
                                    emailService.sendEmailNotification(userEmail, ownerUsername, myFile.getObjectName(), FileActionType.TO_SIGN, myFile.getId(), receiverId);
                                    fileActionService.createFileAction(myFile.getId(), UUID.fromString(userId), receiverId, FileActionType.TO_SIGN);
                                } else if (user.getAction().equals(toApproveValue)) {
                                    emailService.sendEmailNotification(userEmail, ownerUsername, myFile.getObjectName(), FileActionType.TO_APPROVE, myFile.getId(), receiverId);
                                    fileActionService.createFileAction(myFile.getId(), UUID.fromString(userId), receiverId, FileActionType.TO_APPROVE);
                                } else if (user.getAction().equals(toViewValue)) {
                                    emailService.sendEmailNotification(userEmail, ownerUsername, myFile.getObjectName(), FileActionType.TO_VIEW, myFile.getId(), receiverId);
                                    fileActionService.createFileAction(myFile.getId(), UUID.fromString(userId), receiverId, FileActionType.TO_VIEW);
                                }
                            }


                            fileStatusService.createFileStatus(myFile.getId(), FileStatusEnum.PENDING);

                            FileDetailsModel model = new FileDetailsModel();
                            model.setFileId(fileObject.getId().toString());
                            model.setFilename(filename);
                            model.setFileUrl(fileUrl);
                            return Optional.of(model);
                        } catch (Exception e) {
                            return Optional.empty();
                        }finally {
                            semaphore.release();
                        }
                    }, executor)
                    .orTimeout(3, TimeUnit.MINUTES)
                    .exceptionally(ex -> {
                        System.err.println("CompletableFuture error: " + ex.getClass().getName() + " - " + ex.getMessage());
                        ex.printStackTrace();
                        return Optional.empty();
                    })
                    .thenApply(result -> (Optional<FileDetailsModel>) result);

            futures.add(future);
        }

        List<FileDetailsModel> results = futures.stream()
                .map(CompletableFuture::join)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        executor.shutdown();
        return results;

    }




}
