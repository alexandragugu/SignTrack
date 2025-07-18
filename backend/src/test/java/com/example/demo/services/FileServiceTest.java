package com.example.demo.services;

import com.example.demo.DTO.FilesInfoDTO;
import com.example.demo.entities.Files;
import com.example.demo.entities.UserBucket;
import com.example.demo.repositories.FileActionRepository;
import com.example.demo.repositories.FilesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private FilesRepository filesRepository;

    @InjectMocks
    private FileService fileService;

    private UUID fileId;
    private UUID userId;
    private UUID bucketId;
    private Files testFile;
    private UserBucket testBucket;

    @BeforeEach
    void setUp() {
        fileId = UUID.randomUUID();
        userId = UUID.randomUUID();
        bucketId = UUID.randomUUID();

        testBucket = new UserBucket();
        testBucket.setBucketUuid(bucketId);
        testBucket.setUserId(userId);

        testFile = new Files();
        testFile.setId(fileId);
        testFile.setObjectName("test.pdf");
        testFile.setUserBucket(testBucket);
        testFile.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void getFilesByBucket_success() {
        when(filesRepository.findByUserBucket_BucketUuid(bucketId)).thenReturn(List.of(testFile));

        List<Files> result = fileService.getFilesByBucket(bucketId);
        assertEquals(1, result.size());
        assertEquals(testFile, result.get(0));
    }

    @Test
    void getFilesByBucket_paginated_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Files> page = new PageImpl<>(List.of(testFile));
        when(filesRepository.findByUserBucket_BucketUuid(bucketId, pageable)).thenReturn(page);

        Page<Files> result = fileService.getFilesByBucket(bucketId, pageable);
        assertEquals(1, result.getTotalElements());
    }


    @Test
    void getFileByFileId_success() {
        when(filesRepository.findById(fileId)).thenReturn(Optional.of(testFile));
        Files result = fileService.getFileByFileId(fileId);
        assertEquals(testFile, result);
    }

    @Test
    void getFileByIdAndReceiverId_success() {
        when(filesRepository.findFileByIdAndReceiverId(fileId, userId)).thenReturn(Optional.of(testFile));
        Files result = fileService.getFileByIdAndReceiverId(fileId, userId);
        assertEquals(testFile, result);
    }

    @Test
    void getFileCountByBucketId_success() {
        when(filesRepository.countFilesByBucketId(bucketId)).thenReturn(5L);
        Long result = fileService.getFileCountByBucketId(bucketId);
        assertEquals(5L, result);
    }


    @Test
    void deleteFile_success() {
        fileService.deleteFile(fileId);
        verify(filesRepository).deleteFileById(fileId);
    }

    @Test
    void deleteFilesByBucketId_success() {
        fileService.deleteFilesByBucketId(bucketId);
        verify(filesRepository).deleteFileByBucketId(bucketId);
    }
}