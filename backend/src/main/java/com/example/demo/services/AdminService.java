package com.example.demo.services;

import com.example.demo.entities.UserBucket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AdminService {

    @Autowired
    UserBucketService userBucketService;

    @Autowired
    FileService fileService;

    @Autowired
    FileActionService fileActionService;

    public Long getNumberOfUploadedFiles(UUID userId){
        UserBucket userBucket = userBucketService.getUserBucket(userId);
        Long filesCount = fileService.getFileCountByBucketId(userBucket.getBucketUuid());
        return filesCount;
    }

    public Long getNumberOfSignedFiles(UUID userId){
        Long filesCount = fileActionService.getFilesSignedByReceiverId(userId);
        return filesCount;
    }



}
