package com.example.demo.entities;

import com.example.demo.utils.FileActionType;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "file_action")
public class FileAction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "file_id", nullable = false)
    private Files file;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false, referencedColumnName = "user_id")
    private UserBucket sender;

    @Column(name = "receiver_id", nullable = false)
    private UUID receiverId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private FileActionType action;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "fileAction", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Signatures> signatures = new ArrayList<>();


    public FileAction() {
    }

    public FileAction(Files file, UserBucket sender, UUID receiverId, FileActionType action) {
        this.file = file;
        this.sender = sender;
        this.receiverId = receiverId;
        this.action = action;
    }

    public FileAction(UUID id, Files file, UserBucket sender, UUID receiverId, FileActionType action, LocalDateTime createdAt) {
        this.id = id;
        this.file = file;
        this.sender = sender;
        this.receiverId = receiverId;
        this.action = action;
        this.createdAt = createdAt;
    }




    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Files getFile() {
        return file;
    }

    public void setFile(Files file) {
        this.file = file;
    }

    public UserBucket getSender() {
        return sender;
    }

    public void setSender(UserBucket sender) {
        this.sender = sender;
    }

    public UUID getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(UUID receiverId) {
        this.receiverId = receiverId;
    }

    public FileActionType getAction() {
        return action;
    }

    public void setAction(FileActionType action) {
        this.action = action;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Signatures> getSignatures() {
        return signatures;
    }

    public void setSignatures(List<Signatures> signatures) {
        this.signatures = signatures;
    }
}
