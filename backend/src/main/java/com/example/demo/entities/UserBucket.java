package com.example.demo.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_bucket")
public class UserBucket {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "bucket_uuid", nullable = false, unique = true)
    private UUID bucketUuid;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "username", nullable = false, unique = false)
    private String username;

    public UserBucket() {
    }

    public UserBucket(UUID userId, UUID bucketUuid, String username) {
        this.userId = userId;
        this.bucketUuid = bucketUuid;
        this.username = username;
    }

    public UserBucket(UUID id, UUID userId, UUID bucketUuid, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.bucketUuid = bucketUuid;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getBucketUuid() {
        return bucketUuid;
    }

    public void setBucketUuid(UUID bucketUuid) {
        this.bucketUuid = bucketUuid;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
