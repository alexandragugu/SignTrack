package com.example.demo.services;

import com.example.demo.entities.FileAction;
import com.example.demo.entities.Files;
import com.example.demo.entities.UserBucket;
import com.example.demo.models.FileActionStatus;
import com.example.demo.repositories.FileActionRepository;
import com.example.demo.repositories.FilesRepository;
import com.example.demo.repositories.UserBucketRepository;
import com.example.demo.utils.FileActionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileActionServiceTest {

    @Mock private FileActionRepository fileActionRepository;
    @Mock
    private FilesRepository filesRepository;
    @Mock private UserBucketRepository userBucketRepository;

    @InjectMocks
    private FileActionService fileActionService;

    private UUID fileId, senderId, receiverId;

    @BeforeEach
    void init() {
        fileId = UUID.randomUUID();
        senderId = UUID.randomUUID();
        receiverId = UUID.randomUUID();
    }

    @Test
    void testCreateFileAction() {
        Files file = new Files();
        UserBucket sender = new UserBucket();
        FileAction action = new FileAction(file, sender, receiverId, FileActionType.TO_SIGN);

        when(filesRepository.findById(fileId)).thenReturn(Optional.of(file));
        when(userBucketRepository.findByUserId(senderId)).thenReturn(Optional.of(sender));
        when(fileActionRepository.save(any())).thenReturn(action);

        FileAction result = fileActionService.createFileAction(fileId, senderId, receiverId, FileActionType.TO_SIGN);

        assertNotNull(result);
        verify(fileActionRepository).save(any());
    }

    @Test
    void testGetAllFileActions() {
        when(fileActionRepository.findAll()).thenReturn(List.of(new FileAction()));
        assertEquals(1, fileActionService.getAllFileActions().size());
    }

    @Test
    void testGetFilesByReceiverId() {
        when(fileActionRepository.findByReceiverId(receiverId)).thenReturn(List.of(new FileAction()));
        assertEquals(1, fileActionService.getFilesByReceiverId(receiverId).size());
    }

    @Test
    void testGetFileActionsByFileId() {
        when(fileActionRepository.findByFile_Id(fileId)).thenReturn(List.of(new FileAction()));
        assertEquals(1, fileActionService.getFileActionsByFileId(fileId).size());
    }

    @Test
    void testGetFilesToSignByReceiverId() {
        when(fileActionRepository.findByActionAndReceiverId(FileActionType.TO_SIGN, receiverId)).thenReturn(List.of(new FileAction()));
        assertEquals(1, fileActionService.getFilesToSignByReceiverId(receiverId).size());
    }

    @Test
    void testGetFileActionByFileIdAndReceiverId() {
        FileAction expected = new FileAction();
        when(fileActionRepository.findByFile_IdAndReceiverId(fileId, receiverId)).thenReturn(expected);
        assertEquals(expected, fileActionService.getFileActionByFileIdAndReceiverId(fileId, receiverId));
    }

    @Test
    void testGetFilesSignedByReceiverId() {
        when(fileActionRepository.countByActionAndReceiverId(FileActionType.SIGNED, receiverId)).thenReturn(2L);
        assertEquals(2, fileActionService.getFilesSignedByReceiverId(receiverId));
    }

    @Test
    void testGetFileActions() {
        when(fileActionRepository.findActionsByFileId(fileId)).thenReturn(List.of(mock(FileActionStatus.class)));
        assertEquals(1, fileActionService.getFileActions(fileId).size());
    }

    @Test
    void testRegistrationExists() {
        when(fileActionRepository.existsByReceiverIdAndFileIdAndAction(receiverId, fileId, FileActionType.TO_SIGN)).thenReturn(true);
        assertTrue(fileActionService.registrationExists(receiverId, fileId, FileActionType.TO_SIGN));
    }

    @Test
    void testDeleteAllFileActionsByFile() {
        FileAction action = mock(FileAction.class);
        when(fileActionRepository.findByFile_Id(fileId)).thenReturn(List.of(action));
        fileActionService.deleteAllFileActionsByFile(fileId);
        verify(fileActionRepository).delete(action);
    }

    @Test
    void testDeleteAllFileActionsByFile_throws() {
        when(fileActionRepository.findByFile_Id(fileId)).thenReturn(Collections.emptyList());
        assertThrows(RuntimeException.class, () -> fileActionService.deleteAllFileActionsByFile(fileId));
    }

    @Test
    void testDeleteFileActionbySenderId() {
        fileActionService.deleteFileActionbySenderId(senderId);
        verify(fileActionRepository).deleteBySenderId(senderId);
    }

    @Test
    void testGetViewedDocuments() {
        when(fileActionRepository.countViewedDocuments(receiverId)).thenReturn(5);
        assertEquals(5, fileActionService.getViewedDocuments(receiverId));
    }

    @Test
    void testFindLastRequestedAction() {
        when(fileActionRepository.findLastRequestedAction(fileId, receiverId)).thenReturn(List.of("TO_SIGN"));
        assertEquals("TO_SIGN", fileActionService.findLastRequestedAction(fileId, receiverId));
    }

    @Test
    void testFindLastRequestedAction_empty() {
        when(fileActionRepository.findLastRequestedAction(fileId, receiverId)).thenReturn(Collections.emptyList());
        assertNull(fileActionService.findLastRequestedAction(fileId, receiverId));
    }
}
