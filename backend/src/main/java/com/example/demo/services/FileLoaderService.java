package com.example.demo.services;

import com.example.demo.entities.FileAction;
import com.example.demo.entities.FileStatus;
import com.example.demo.entities.Files;
import com.example.demo.models.FileActionStatus;
import com.example.demo.models.FileDetailsModel;
import com.example.demo.models.UserAction;
import jakarta.validation.constraints.Min;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Service
public class FileLoaderService {

    @Autowired
    private UserBucketService userBucketService;

    @Autowired
    private MinIOService minIOService;

    @Autowired
    private FileService fileService;

    @Autowired
    private FileActionService fileActionService;

    @Autowired
    private FileStatusService fileStatusService;

    private static final int MAX_FILES_PER_THREAD = 30;
    private static final int MAX_THREAD_COUNT = 10;

    public List<FileDetailsModel> loadUserFiles(String userId, String username){
        String bucketUUID = userBucketService.getBucketUuid(UUID.fromString(userId));
        List<Files> files = fileService.getFilesByBucket(UUID.fromString(bucketUUID));

        if (files == null || files.isEmpty()) {
            return Collections.emptyList();
        }

        int maxFilesPerThread = 30;
        int threadCount = Math.min((int) Math.ceil((double) files.size() / maxFilesPerThread), 10);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        List<CompletableFuture<FileDetailsModel>> futures = files.stream()
                .map(file -> CompletableFuture.supplyAsync(() -> {
                    FileDetailsModel model = new FileDetailsModel();
                    model.setFilename(file.getObjectName());

                    try {
                        model.setFileUrl(minIOService.getPresignedUrl(
                                file.getId().toString(), bucketUUID, file.getObjectName()));
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to generate URL", e);
                    }

                    model.setFileId(file.getId().toString());
                    model.setDate(file.getCreatedAt().toString());
                    model.setFileStatus("Finished");

                    List<FileActionStatus> statuses = fileActionService.getPersonalFileActions(file.getId());
                    List<UserAction> actions = new ArrayList<>();

                    for (FileActionStatus status : statuses) {
                        UserAction userAction = new UserAction();
                        userAction.setUserId(status.getReceiverId().toString());
                        userAction.setUsername(status.getUsername());
                        userAction.setAction(status.getCurrentStatus());
                        userAction.setCurrentDate(status.getCurrentStatusDate());

                        if (userId.equals(status.getReceiverId().toString())) {
                            String role = userAction.mapOwerRole(status.getCurrentStatus());
                            model.setOwnerRole(role);
                            model.setActionRequired(status.getCurrentStatus());
                        }

                        if ("Finished".equals(model.getFileStatus()) && status.getCurrentStatus().contains("TO_")) {
                            model.setFileStatus("Pending");
                        }

                        actions.add(userAction);
                    }

                    model.setReceiverActions(actions);
                    return model;
                }, executor))
                .toList();

        List<FileDetailsModel> result = futures.stream().map(CompletableFuture::join).toList();
        executor.shutdown();
        return result;
    }


    public List<FileDetailsModel> loadAssignedFiles(String userId, String username) {
        List<FileAction> filesForUser = fileActionService.findLatestFileActionsByReceiverId(UUID.fromString(userId));

        if (filesForUser == null || filesForUser.isEmpty()) {
            return Collections.emptyList();
        }

        int threadCount = Math.min(
                Math.max(1, (int) Math.ceil((double) filesForUser.size() / MAX_FILES_PER_THREAD)),
                MAX_THREAD_COUNT
        );

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<CompletableFuture<FileDetailsModel>> futures = filesForUser.stream()
                .map(fileAction -> CompletableFuture.supplyAsync(() -> {
                    FileDetailsModel model = new FileDetailsModel();

                    try {
                        UUID fileId = fileAction.getFile().getId();
                        String objectName = fileAction.getFile().getObjectName();
                        UUID bucketUuid = fileAction.getSender().getBucketUuid();

                        model.setFilename(objectName);
                        model.setFileId(fileId.toString());
                        model.setFileUrl(minIOService.getPresignedUrl(
                                fileId.toString(),
                                bucketUuid.toString(),
                                objectName
                        ));


                        model.setOwnerUsername(userBucketService.getUserBucket(UUID.fromString(fileAction.getSender().getUserId().toString())).getUsername());

                        boolean isFinished = fileStatusService.checkForFinished(fileId);
                        model.setFileStatus(isFinished ? "Finished" : "Pending");

                        List<FileActionStatus> fileStatusList = fileActionService.getPersonalFileActions(fileId);
                        List<UserAction> resultList = new ArrayList<>();

                        for (FileActionStatus status : fileStatusList) {
                            UserAction userAction = new UserAction();
                            String receiverUsername=userBucketService.getUserBucket(UUID.fromString(status.getReceiverId().toString())).getUsername();

                            userAction.setUserId(status.getReceiverId().toString());
                            userAction.setUsername(receiverUsername);
                            userAction.setAction(status.getCurrentStatus());

                            if (userId.equals(status.getReceiverId().toString())) {
                                String lastAction = status.getCurrentStatus();
                                String mappedRole;

                                if ("DECLINED".equals(lastAction)) {
                                    String requestedAction = fileActionService.findLastRequestedAction(
                                            status.getFileId(),
                                            UUID.fromString(status.getReceiverId().toString())
                                    );
                                    mappedRole = userAction.mapOwerRole(requestedAction);
                                    userAction.setAction(lastAction + "_" + requestedAction);
                                } else {
                                    mappedRole = userAction.mapOwerRole(lastAction);
                                    model.setActionRequired(lastAction);
                                }

                                model.setOwnerRole(mappedRole);
                                model.setActionRequired(lastAction);
                                model.setDate(status.getCurrentStatusDate().toString());
                            }

                            resultList.add(userAction);
                        }

                        model.setReceiverActions(resultList);

                    } catch (Exception e) {
                    }

                    return model;
                }, executor))
                .toList();

        List<FileDetailsModel> result = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        executor.shutdown();
        return result;
    }

}
