package com.example.demo.repositories;

import com.example.demo.entities.Admins;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AdminsRepository extends JpaRepository<Admins, UUID> {

    @Query("SELECT a.adminId FROM Admins a WHERE a.createdByAdminId = :creatorId")
    List<UUID> findAdminIdsByCreator(@Param("creatorId") UUID creatorId);

    @Modifying
    @Query("UPDATE Admins a SET a.createdByAdminId = :newAdminId WHERE a.createdByAdminId = :oldAdminId")
    void updateAdminReferences(@Param("oldAdminId") UUID oldAdminId,
                               @Param("newAdminId") UUID newAdminId);

    @Modifying
    @Query("DELETE FROM Admins a WHERE a.adminId = :adminId")
    void deleteByAdminId(@Param("adminId") UUID adminId);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Admins a WHERE a.createdByAdminId = :creatorId AND a.adminId = :targetAdminId")
    boolean existsByCreatorIdAndAdminId(@Param("creatorId") UUID creatorId, @Param("targetAdminId") UUID targetAdminId);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Admins a WHERE a.adminId = :adminId AND a.createdByAdminId IS NOT NULL")
    boolean isCreatedByAnotherAdmin(@Param("adminId") UUID adminId);

}