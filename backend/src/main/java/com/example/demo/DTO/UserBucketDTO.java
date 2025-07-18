package com.example.demo.DTO;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserBucketDTO {

    private UUID id;
    private UUID userId;
    private UUID bucketUuid;
    private LocalDateTime createdAt;

    public UserBucketDTO() {
    }

    public UserBucketDTO(UUID id, UUID userId, UUID bucketUuid, LocalDateTime createdAt) {
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
}
