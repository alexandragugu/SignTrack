package com.example.demo.services;

import com.example.demo.DTO.FilesInfoDTO;
import com.example.demo.entities.FileAction;
import com.example.demo.entities.Files;
import com.example.demo.entities.UserBucket;
import com.example.demo.repositories.FileActionRepository;
import com.example.demo.repositories.FilesRepository;
import io.minio.messages.Bucket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileService {

    private final FilesRepository fileRepository;

    @Autowired
    private final FileActionRepository fileActionRepository;

    @Autowired
    private UserBucketService userBucketService;
    @Autowired
    private MinIOService minIOService;

    public FileService(FilesRepository fileRepository, FileActionRepository fileActionRepository) {
        this.fileRepository = fileRepository;
        this.fileActionRepository = fileActionRepository;
    }

    public Files uploadFile(UUID userUuid, String objectName) {
        UserBucket userBucket = userBucketService.getUserBucket(userUuid);
        Files file = new Files(userBucket, objectName);
        return fileRepository.save(file);
    }

    public List<Files> getFilesByBucket(UUID bucketUuid) {
        return fileRepository.findByUserBucket_BucketUuid(bucketUuid);
    }

    public Page<Files> getFilesByBucket(UUID bucketUuid, Pageable pageable) {
        return fileRepository.findByUserBucket_BucketUuid(bucketUuid, pageable);
    }

    public  Files getFileByUserUUIDAndObjectName(UUID userUuid, String object_name){
        UserBucket userBucket=userBucketService.getUserBucket(userUuid);
        return fileRepository.findByObjectNameAndUserBucket_BucketUuid(object_name,userBucket.getBucketUuid()).orElseThrow(()-> new RuntimeException("File not found"));
    }

    public Files getFileByFileId(UUID fileId){
        return fileRepository.findById(fileId).orElseThrow(()-> new RuntimeException("File not found"));
    }

    public Files getFileByIdAndReceiverId(UUID fileId, UUID receiverId){
        return fileRepository.findFileByIdAndReceiverId(fileId, receiverId)
                .orElseThrow(() -> new RuntimeException("File not found for fileId: " + fileId + " and receiverId: " + receiverId));
    }

    public Long getFileCountByBucketId(UUID bucketId) {
        return fileRepository.countFilesByBucketId(bucketId);
    }

    public List<FilesInfoDTO> getLastFiles(UUID userId) throws Exception {
        List<FilesInfoDTO> fileInfo=new ArrayList<>();
        String bucketId=userBucketService.getBucketUuid(userId);
        List<Files> files=fileRepository.findTop5ByUserBucket_BucketUuidOrderByCreatedAtDesc(UUID.fromString(bucketId));
        for (Files file:files){
            FilesInfoDTO newFile=new FilesInfoDTO();
            newFile.setFileId(file.getId().toString());
            newFile.setFilename(file.getObjectName());
            newFile.setCreatedDate(file.getCreatedAt());
            newFile.setFileUrl(minIOService.getPresignedUrl(file.getId().toString(),bucketId,file.getObjectName()));
            Optional<FileAction> latestAction = fileActionRepository.findTopByFile_IdOrderByCreatedAtDesc(file.getId());

            LocalDateTime lastModifiedDate;
            if (latestAction.isPresent()) {
                lastModifiedDate = latestAction.get().getCreatedAt();
            } else {
                lastModifiedDate = null;
            }

            if(lastModifiedDate==null){
                newFile.setLastModifiedDate(file.getCreatedAt());
            }else {
                newFile.setLastModifiedDate(lastModifiedDate);
            }
            fileInfo.add(newFile);
        }

        return fileInfo;
    }

    public void deleteFile(UUID fileId){
        fileRepository.deleteFileById(fileId);
    }

    public void deleteFilesByBucketId(UUID bucketId){
        fileRepository.deleteFileByBucketId(bucketId);
    }


}