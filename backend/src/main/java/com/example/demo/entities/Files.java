package com.example.demo.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "files")
public class Files {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "bucket_uuid", referencedColumnName = "bucket_uuid", nullable = false)
    private UserBucket userBucket;

    @Column(name = "object_name", nullable = false)
    private String objectName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Files() {}

    public Files(UserBucket userBucket, String objectName) {
        this.userBucket = userBucket;
        this.objectName = objectName;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UserBucket getUserBucket() { return userBucket; }
    public void setUserBucket(UserBucket userBucket) { this.userBucket = userBucket; }

    public String getObjectName() { return objectName; }
    public void setObjectName(String objectName) { this.objectName = objectName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
