package com.example.demo.services;

import com.example.demo.entities.FileAction;
import com.example.demo.entities.Files;
import com.example.demo.entities.UserBucket;
import com.example.demo.models.FileActionStatus;
import com.example.demo.models.FileDetailsModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileLoaderServiceTest {

    @InjectMocks
    private FileLoaderService fileLoaderService;

    @Mock
    private UserBucketService userBucketService;

    @Mock
    private MinIOService minIOService;

    @Mock
    private FileService fileService;

    @Mock
    private FileActionService fileActionService;

    @Mock
    private FileStatusService fileStatusService;

    private UUID fileId;
    private String userId;
    private String bucketUuid;
    private Files file;

    @BeforeEach
    void setup() {
        fileId = UUID.randomUUID();
        userId = UUID.randomUUID().toString();
        bucketUuid = UUID.randomUUID().toString();

        file = new Files();
        file.setId(fileId);
        file.setObjectName("file.pdf");
        file.setCreatedAt(new Timestamp(System.currentTimeMillis()).toLocalDateTime());
    }

    @Test
    void testLoadUserFiles_returnsModelsCorrectly() throws Exception {
        when(userBucketService.getBucketUuid(UUID.fromString(userId))).thenReturn(bucketUuid);
        when(fileService.getFilesByBucket(UUID.fromString(bucketUuid))).thenReturn(List.of(file));
        when(minIOService.getPresignedUrl(fileId.toString(), bucketUuid, "file.pdf"))
                .thenReturn("http://localhost/file.pdf");

        FileActionStatus status = mock(FileActionStatus.class);
        when(status.getReceiverId()).thenReturn(UUID.fromString(userId));
        when(status.getUsername()).thenReturn("john.doe");
        when(status.getCurrentStatus()).thenReturn("TO_SIGN");
        when(status.getCurrentStatusDate()).thenReturn(new Date());

        when(fileActionService.getPersonalFileActions(fileId)).thenReturn(List.of(status));

        List<FileDetailsModel> result = fileLoaderService.loadUserFiles(userId, "john.doe");

        assertEquals(1, result.size());
        FileDetailsModel model = result.get(0);
        assertEquals("file.pdf", model.getFilename());
        assertEquals("Pending", model.getFileStatus()); 
        assertEquals("Signer", model.getOwnerRole());
    }

    @Test
    void testLoadAssignedFiles_returnsModelsCorrectly() throws Exception {
        UUID receiverId = UUID.fromString(userId);
        UUID bucketId = UUID.randomUUID();

        Files file = new Files();
        file.setId(fileId);
        file.setObjectName("file.pdf");

        UserBucket senderBucket = new UserBucket();
        senderBucket.setBucketUuid(bucketId);
        senderBucket.setUserId(receiverId);
        senderBucket.setUsername("john.doe");

        FileAction fileAction = new FileAction();
        fileAction.setReceiverId(receiverId);
        fileAction.setSender(senderBucket);
        fileAction.setFile(file);

        when(fileActionService.findLatestFileActionsByReceiverId(receiverId))
                .thenReturn(List.of(fileAction));

        when(minIOService.getPresignedUrl(fileId.toString(), bucketId.toString(), "file.pdf"))
                .thenReturn("http://localhost/file.pdf");

        when(userBucketService.getUserBucket(receiverId)).thenReturn(senderBucket);
        when(fileStatusService.checkForFinished(fileId)).thenReturn(true);

        FileActionStatus status = mock(FileActionStatus.class);
        when(status.getReceiverId()).thenReturn(UUID.fromString(userId));
        when(status.getCurrentStatus()).thenReturn("TO_SIGN");
        when(status.getCurrentStatusDate()).thenReturn(new Date());

        when(fileActionService.getPersonalFileActions(fileId)).thenReturn(List.of(status));

        List<FileDetailsModel> result = fileLoaderService.loadAssignedFiles(userId, "john.doe");

        assertEquals(1, result.size());
        assertEquals("file.pdf", result.get(0).getFilename());
        assertEquals("Finished", result.get(0).getFileStatus());
    }

}