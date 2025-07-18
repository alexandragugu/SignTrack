package com.example.demo.entities;

import com.example.demo.models.FileStatusEnum;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "file_status")
public class FileStatus {

    @Id
    @Column(name = "file_id")
    private UUID fileId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FileStatusEnum status;

    public FileStatus() {
    }

    public FileStatus(UUID fileId, FileStatusEnum status) {
        this.fileId = fileId;
        this.status = status;
    }

    public UUID getFileId() {
        return fileId;
    }

    public void setFileId(UUID fileId) {
        this.fileId = fileId;
    }

    public FileStatusEnum getStatus() {
        return status;
    }

    public void setStatus(FileStatusEnum status) {
        this.status = status;
    }
}