package com.example.demo.repositories;

import com.example.demo.entities.FileStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileStatusRepository extends JpaRepository<FileStatus, UUID> {
    Optional<FileStatus> findByFileId(UUID fileId);
}