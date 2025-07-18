package com.example.demo.services;

import com.example.demo.entities.Files;
import com.example.demo.models.FileDetailsModel;
import com.example.demo.models.FileStatusEnum;
import com.example.demo.models.UserAction;
import com.example.demo.utils.FileActionType;
import com.example.demo.utils.UserDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BulkFileServiceTest {

    @InjectMocks
    private BulkFileService bulkFileService;

    @Mock
    private FileService fileService;

    @Mock
    private MinIOService minIOService;

    @Mock
    private FileActionService fileActionService;

    @Mock
    private FileStatusService fileStatusService;

    @Mock
    private KeycloakService keycloakService;

    @Mock
    private EmailService emailService;

    @Mock
    private UserBucketService userBucketService;

    private final UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final String bucketId = "bucket-123";

    private MultipartFile mockMultipartFile(String filename) {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn(filename);
        return file;
    }

    private Files createFile(UUID id, String name) {
        Files file = new Files();
        file.setId(id);
        file.setObjectName(name);
        return file;
    }

    @Test
    void processFilesInList() throws Exception {
        MultipartFile file = mockMultipartFile("document.pdf");
        List<MultipartFile> files = List.of(file);

        UserAction action = new UserAction();
        action.setUserId("00000000-0000-0000-0000-000000000002");
        action.setAction("to-sign");
        List<UserAction> receivers = List.of(action);

        Files uploadedFile = createFile(UUID.randomUUID(), "document.pdf");

        when(userBucketService.getBucketUuid(userId)).thenReturn(bucketId);
        when(fileService.uploadFile(eq(userId), anyString())).thenReturn(uploadedFile);
        when(minIOService.uploadMultipartFile(any(), eq(bucketId), anyString())).thenReturn("minio-file-name");
        when(minIOService.getPresignedUrl(anyString(), eq(bucketId), anyString())).thenReturn("http://presigned-url.com");
        when(fileService.getFileByFileId(uploadedFile.getId())).thenReturn(uploadedFile);
        when(keycloakService.getUserById(anyString(), eq(UserDetails.EMAIL))).thenReturn("test@example.com");

        List<FileDetailsModel> results = bulkFileService.processFilesInList(files, receivers, userId.toString(), "testuser");

        assertEquals(1, results.size());
        FileDetailsModel model = results.get(0);
        assertEquals(uploadedFile.getId().toString(), model.getFileId());
        assertEquals("document.pdf", model.getFilename());
        assertEquals("http://presigned-url.com", model.getFileUrl());

        verify(emailService).sendEmailNotification(any(), any(), any(), eq(FileActionType.TO_SIGN), eq(uploadedFile.getId()), any());
        verify(fileActionService).createFileAction(eq(uploadedFile.getId()), eq(userId), any(), eq(FileActionType.TO_SIGN));
        verify(fileStatusService).createFileStatus(eq(uploadedFile.getId()), eq(FileStatusEnum.PENDING));
    }
}
