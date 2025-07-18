package com.example.demo.services;

import com.example.demo.entities.FileAction;
import com.example.demo.entities.Files;
import com.example.demo.entities.UserBucket;
import com.example.demo.models.FileActionStatus;
import com.example.demo.repositories.FileActionRepository;
import com.example.demo.repositories.FilesRepository;
import com.example.demo.repositories.UserBucketRepository;
import com.example.demo.utils.FileActionType;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileActionService {

    private final FileActionRepository fileActionRepository;
    private final FilesRepository filesRepository;
    private final UserBucketRepository userBucketRepository;

    public FileActionService(FileActionRepository fileActionRepository, FilesRepository filesRepository, UserBucketRepository userBucketRepository, FileService fileService) {
        this.fileActionRepository = fileActionRepository;
        this.filesRepository = filesRepository;
        this.userBucketRepository = userBucketRepository;
    }


    public FileAction createFileAction(UUID fileId, UUID senderId, UUID receiverId, FileActionType action){
        Files file = filesRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));
        System.out.println("File gasit: " + file);

        UserBucket sender = userBucketRepository.findByUserId(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        System.out.println("Sender gasit: " + sender);

        FileAction fileAction = new FileAction(file, sender, receiverId, action);
        System.out.println("FileAction creat: " + fileAction);

        return fileActionRepository.save(fileAction);
    }

    public List<FileAction> getAllFileActions(){
        return fileActionRepository.findAll();
    }

    public List<FileAction> getFilesByReceiverId(UUID receiverId){
        return fileActionRepository.findByReceiverId(receiverId);
    }

    public List<FileAction> getFileActionsByFileId(UUID fileId){
        return fileActionRepository.findByFile_Id(fileId);
    }

    public List<FileAction> getFilesToSignByReceiverId(UUID receiverId){
        return fileActionRepository.findByActionAndReceiverId(FileActionType.TO_SIGN, receiverId);
    }

    public FileAction getFileActionByFileIdAndReceiverId(UUID fileId, UUID receiverId) {
        return fileActionRepository.findByFile_IdAndReceiverId(fileId, receiverId);
    }

    public Long getFilesSignedByReceiverId(UUID receiverId){
        return fileActionRepository.countByActionAndReceiverId(FileActionType.SIGNED, receiverId);
    }

    public List<FileActionStatus> getFileActions(UUID fileId) {
        return fileActionRepository.findActionsByFileId(fileId);
    }

    public List<FileActionStatus> getPersonalFileActions(UUID fileId) {
        return fileActionRepository.findActionsByFileIdPersonal(fileId);
    }

    public Files getFileByReceiverIdAndFileIdAndAction(UUID receiverId, UUID fileId, FileActionType action) {
        FileAction fa = fileActionRepository.findByReceiverIdAndFileIdAndAction(receiverId, fileId, action)
                .orElseThrow(() -> new RuntimeException("File action not found"));
        return fa.getFile();
    }

    public Files getFileByOwnerIdAndFileIdAndAction(UUID ownerId, UUID fileId, FileActionType action) {
        FileAction fa = fileActionRepository.findByOwnerIdAndFileIdAndAction(ownerId, fileId, action)
                .orElseThrow(() -> new RuntimeException("File action not found"));
        return fa.getFile();
    }


    public boolean registrationExists(UUID receiverId, UUID fileId, FileActionType action) {
        return fileActionRepository.existsByReceiverIdAndFileIdAndAction(receiverId, fileId, action);
    }


    @Transactional
    public void deleteAllFileActionsByFile(UUID fileId) {
        List<FileAction> actions = fileActionRepository.findByFile_Id(fileId);
        if (actions.isEmpty()) {
            throw new RuntimeException("No actions found for file ID: " + fileId);
        }

        for (FileAction action : actions) {
            fileActionRepository.delete(action);
        }
    }

    public List<FileAction> geFileActionsByReceiverIdAndFileIdAndAction(UUID receiverId ,FileActionType action) {
        return fileActionRepository.findPendingActionsByReceiverIdAndAction(receiverId, String.valueOf(action));
    }

    @Transactional
    public void deleteFileActionbySenderId(UUID senderId){
        fileActionRepository.deleteBySenderId(senderId);
    }

    public int getViewedDocuments(UUID receiverId){
        return fileActionRepository.countViewedDocuments(receiverId);
    }

    public int getSignedDocuments(UUID receiverId){
        return fileActionRepository.countSignedDocuments(receiverId);
    }

    public int getApprovedDocuments(UUID receiverId){
        return fileActionRepository.countApprovedDocuments(receiverId);
    }

    public int getDeclinedDocuments(UUID receiverId){
        return fileActionRepository.countDeclinedDocuments(receiverId);
    }
    public Timestamp getLastAction(UUID receiverId){
        return fileActionRepository.findLastActivity(receiverId);
    }

    public List<FileAction> findLatestFileActionsByReceiverId(UUID receiverId) {
        return fileActionRepository.findLatestFileActionsByReceiverId(receiverId);
    }

    public String findLastRequestedAction(UUID fileId, UUID receiverId) {
        List<String> actions = fileActionRepository.findLastRequestedAction(fileId, receiverId);
        return actions.isEmpty() ? null : actions.get(0);
    }
}
