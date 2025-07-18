package com.example.demo.repositories;

import com.example.demo.entities.UserBucket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserBucketRepository extends JpaRepository<UserBucket, UUID> {
    Optional<UserBucket> findByUserId(UUID userId);

    Optional<UserBucket> findByBucketUuid(UUID bucketUuid);

    void deleteByUserId(UUID userId);
}