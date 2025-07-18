package com.example.demo.repositories;

import com.example.demo.entities.Files;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FilesRepository extends JpaRepository<Files, UUID> {
        List<Files> findByUserBucket_BucketUuid(UUID bucket_uuid);

        Page<Files> findByUserBucket_BucketUuid(UUID bucket_uuid, Pageable pageable);

        Optional<Files> findByObjectNameAndUserBucket_BucketUuid(String object_name, UUID bucket_uuid);

        Optional<Files> findById(UUID id);

        @Query("SELECT f FROM Files f JOIN FileAction fa ON f.id = fa.file.id WHERE f.id = :fileId AND fa.receiverId = :receiverId")
        Optional<Files> findFileByIdAndReceiverId(@Param("fileId") UUID fileId, @Param("receiverId") UUID receiverId);

        @Query("SELECT COUNT(f) FROM Files f WHERE f.userBucket.bucketUuid = :bucketId")
        Long countFilesByBucketId(@Param("bucketId") UUID bucketId);

        List<Files> findTop5ByUserBucket_BucketUuidOrderByCreatedAtDesc(UUID userId);

        @Modifying
        @Transactional
        @Query("DELETE FROM Files f WHERE f.id = :fileId")
        void deleteFileById(@Param("fileId") UUID fileId);

        @Modifying
        @Transactional
        @Query("DELETE FROM Files f WHERE f.userBucket.bucketUuid = :bucketId")
        void deleteFileByBucketId(@Param("bucketId") UUID bucketId);
}

