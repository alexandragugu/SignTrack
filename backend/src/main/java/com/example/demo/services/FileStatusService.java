package com.example.demo.services;

import com.example.demo.entities.FileStatus;
import com.example.demo.models.FileActionStatus;
import com.example.demo.models.FileStatusEnum;
import com.example.demo.models.UserAction;
import com.example.demo.repositories.FileStatusRepository;
import com.example.demo.utils.UserDetails;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileStatusService {

    private final FileStatusRepository fileStatusRepository;

    @Autowired
    FileActionService fileActionService;

    @Autowired
    public FileStatusService(FileStatusRepository fileStatusRepository) {
        this.fileStatusRepository = fileStatusRepository;
    }

    @Transactional
    public FileStatus updateFileStatus(UUID fileId, FileStatusEnum status) {
        Optional<FileStatus> existingStatus = fileStatusRepository.findByFileId(fileId);

        FileStatus fileStatus = existingStatus.orElseGet(() -> new FileStatus(fileId, status));
        fileStatus.setStatus(status);

        return fileStatusRepository.save(fileStatus);
    }

    @Transactional
    public FileStatus createFileStatus(UUID fileId, FileStatusEnum status) {
        Optional<FileStatus> existing = fileStatusRepository.findByFileId(fileId);

        if (existing.isPresent()) {
            throw new IllegalStateException("A status already exists for this file.");
        }

        FileStatus fileStatus = new FileStatus(fileId, status);
        return fileStatusRepository.save(fileStatus);
    }


    public boolean checkForFinished(UUID fileId) {
        List<FileActionStatus> fileStatusList = fileActionService.getFileActions(fileId);
        for (FileActionStatus status : fileStatusList) {
            if (status.getCurrentStatus().contains("TO_")) {
                return false;
            }
        }
        return true;
    }

    public boolean checkForPersonalFinished(UUID fileId) {
        List<FileActionStatus> statuses = fileActionService.getPersonalFileActions(fileId);
        for (FileActionStatus status : statuses) {
            if (status.getCurrentStatus().contains("TO_")) {
                return false;
            }
        }
        return true;
    }
}