package com.example.demo.repositories;

import com.example.demo.entities.FileAction;
import com.example.demo.models.FileActionStatus;
import com.example.demo.utils.FileActionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.io.File;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FileActionRepository extends JpaRepository<FileAction, UUID> {

    List<FileAction> findByFile_Id(UUID fileId);

    List<FileAction> findByReceiverId(UUID receiverId);

    List<FileAction> findByActionAndReceiverId(FileActionType action, UUID receiverId);

    Long countByActionAndReceiverId(FileActionType action, UUID receiverId);

    FileAction findByFile_IdAndReceiverId(UUID fileId, UUID receiverId);

    Optional<FileAction> findTopByFile_IdOrderByCreatedAtDesc(UUID fileId);

    @Query(value = """
WITH action_data AS (
    SELECT 
        fa.file_id,
        fa.receiver_id,
        fa.sender_id,
        fa.action,
        fa.created_at
    FROM file_action fa
    WHERE fa.file_id = :fileId
),
filtered_data AS (
    SELECT * FROM action_data
    WHERE sender_id <> receiver_id
),
prioritized_status AS (
    SELECT 
        file_id,
        receiver_id,
        action,
        created_at,
        CASE 
            WHEN action = 'SIGNED' THEN 1
            WHEN action = 'APPROVED' THEN 2
            WHEN action = 'VIEWED' THEN 3
            WHEN action = 'DECLINED' THEN 4
            WHEN action = 'TO_APPROVE' THEN 5
            WHEN action = 'TO_SIGN' THEN 6
            WHEN action = 'TO_VIEW' THEN 7
            ELSE 999
        END AS priority
    FROM filtered_data
),
ranked_status AS (
    SELECT DISTINCT ON (file_id, receiver_id)
        file_id,
        receiver_id,
        action AS currentStatus,
        created_at AS currentStatusDate
    FROM prioritized_status
    ORDER BY file_id, receiver_id, priority
)
SELECT 
    r.file_id AS fileId,
    r.receiver_id AS receiverId,
    ub.username AS username,
    r.currentStatus,
    r.currentStatusDate
FROM ranked_status r
LEFT JOIN user_bucket ub ON r.receiver_id = ub.user_id
""", nativeQuery = true)
    List<FileActionStatus> findActionsByFileId(UUID fileId);


    @Query(value = """
WITH action_status AS (
    SELECT 
        fa.file_id,
        fa.receiver_id,
        ARRAY_AGG(fa.action ORDER BY fa.created_at) AS actions,
        
        MIN(fa.created_at) FILTER (WHERE fa.action = 'SIGNED') AS signed_date,
        MIN(fa.created_at) FILTER (WHERE fa.action = 'APPROVED') AS approved_date,
        MIN(fa.created_at) FILTER (WHERE fa.action = 'VIEWED') AS viewed_date,
        MIN(fa.created_at) FILTER (WHERE fa.action = 'DECLINED') AS declined_date,
        MIN(fa.created_at) FILTER (WHERE fa.action = 'TO_APPROVE') AS to_approve_date,
        MIN(fa.created_at) FILTER (WHERE fa.action = 'TO_SIGN') AS to_sign_date,
        MIN(fa.created_at) FILTER (WHERE fa.action = 'TO_VIEW') AS to_view_date

    FROM file_action fa
    WHERE fa.file_id = :fileId
    GROUP BY fa.file_id, fa.receiver_id
)
SELECT 
    a.file_id AS fileId,
    a.receiver_id AS receiverId,
    ub.username AS username,
    a.actions AS actions,
    
    CASE 
        WHEN 'SIGNED' = ANY(a.actions) THEN 'SIGNED'
        WHEN 'APPROVED' = ANY(a.actions) THEN 'APPROVED'
        WHEN 'VIEWED' = ANY(a.actions) THEN 'VIEWED'
        WHEN 'DECLINED' = ANY(a.actions) THEN 'APPROVED'
        WHEN 'TO_APPROVE' = ANY(a.actions) THEN 'TO_APPROVE'
        WHEN 'TO_SIGN' = ANY(a.actions) THEN 'TO_SIGN'
        WHEN 'TO_VIEW' = ANY(a.actions) THEN 'TO_VIEW'
        ELSE 'UNKNOWN'
    END AS currentStatus,

    CASE 
        WHEN 'SIGNED' = ANY(a.actions) THEN a.signed_date
        WHEN 'APPROVED' = ANY(a.actions) THEN a.approved_date
        WHEN 'VIEWED' = ANY(a.actions) THEN a.viewed_date
        WHEN 'DECLINED' = ANY(a.actions) THEN a.declined_date
        WHEN 'TO_APPROVE' = ANY(a.actions) THEN a.to_approve_date
        WHEN 'TO_SIGN' = ANY(a.actions) THEN a.to_sign_date
        WHEN 'TO_VIEW' = ANY(a.actions) THEN a.to_view_date
        ELSE NULL
    END AS currentStatusDate

FROM action_status a
LEFT JOIN user_bucket ub ON a.receiver_id = ub.user_id
""", nativeQuery = true)
    List<FileActionStatus> findActionsByFileIdPersonal(UUID fileId);



    @Query("SELECT fa FROM FileAction fa " +
            "WHERE fa.receiverId = :receiverId " +
            "AND fa.file.id = :fileId " +
            "AND fa.action = :action")
    Optional<FileAction> findByReceiverIdAndFileIdAndAction(
            @Param("receiverId") UUID receiverId,
            @Param("fileId") UUID fileId,
            @Param("action") FileActionType action
    );


    @Query("SELECT fa FROM FileAction fa " +
            "WHERE fa.sender.userId = :ownerId " +
            "AND fa.file.id = :fileId " +
            "AND fa.action = :action")
    Optional<FileAction> findByOwnerIdAndFileIdAndAction(
            @Param("ownerId") UUID ownerId,
            @Param("fileId") UUID fileId,
            @Param("action") FileActionType action
    );


    @Query("SELECT COUNT(fa) > 0 FROM FileAction fa " +
            "WHERE fa.receiverId = :receiverId " +
            "AND fa.file.id = :fileId " +
            "AND fa.action = :action")
    boolean existsByReceiverIdAndFileIdAndAction(
            @Param("receiverId") UUID receiverId,
            @Param("fileId") UUID fileId,
            @Param("action") FileActionType action
    );


    @Query("SELECT fa FROM FileAction fa " +
            "WHERE fa.receiverId = :receiverId " +
            "AND fa.file.id = :fileId " +
            "AND fa.action = :action")
    List<FileAction> findByReceiverIdAndFileIdAndAction2(
            @Param("receiverId") UUID receiverId,
            @Param("fileId") UUID fileId,
            @Param("action") FileActionType action
    );


    @Modifying
    @Query("DELETE FROM FileAction fa WHERE fa.sender.userId = :senderId")
    void deleteBySenderId(@Param("senderId") UUID senderId);


    @Query(value = """
           WITH action_status AS (
               SELECT 
                   fa.file_id,
                   fa.receiver_id,
                   ARRAY_AGG(fa.action::text ORDER BY fa.created_at) AS actions
               FROM file_action fa
               WHERE fa.receiver_id = :receiverId
               GROUP BY fa.file_id, fa.receiver_id
           ),
           files_with_status AS (
               SELECT 
                   file_id,
                   receiver_id,
                   actions,
                   CASE 
                       WHEN 'SIGNED' = ANY(actions) THEN 'SIGNED'
                       WHEN 'APPROVED' = ANY(actions) THEN 'APPROVED'
                       WHEN 'VIEWED' = ANY(actions) THEN 'VIEWED'
                       WHEN 'DECLINED' = ANY(actions) THEN 'DELCIEND'
                       WHEN 'TO_APPROVE' = ANY(actions) THEN 'TO_APPROVE'
                       WHEN 'TO_SIGN' = ANY(actions) THEN 'TO_SIGN'
                       WHEN 'TO_VIEW' = ANY(actions) THEN 'TO_VIEW'
                       ELSE 'UNKNOWN'
                   END AS currentStatus
               FROM action_status
           )
           SELECT fa.*
           FROM file_action fa
           JOIN files_with_status fws ON fa.file_id = fws.file_id 
              AND fa.receiver_id = fws.receiver_id
           WHERE :specificAction = ANY(fws.actions)
             AND fws.currentStatus = :specificAction
           """, nativeQuery = true)
    List<FileAction> findPendingActionsByReceiverIdAndAction(
            @Param("receiverId") UUID receiverId,
            @Param("specificAction") String specificAction);


    @Query(value = "SELECT COUNT(*) FROM file_action WHERE action = 'VIEWED' AND sender_id = :userId", nativeQuery = true)
    int countViewedDocuments(@Param("userId") UUID userId);

    @Query(value = "SELECT COUNT(*) FROM file_action WHERE action = 'SIGNED' AND sender_id = :userId", nativeQuery = true)
    int countSignedDocuments(@Param("userId") UUID userId);

    @Query(value = "SELECT COUNT(*) FROM file_action WHERE action = 'APPROVED' AND sender_id = :userId", nativeQuery = true)
    int countApprovedDocuments(@Param("userId") UUID userId);

    @Query(value = "SELECT COUNT(*) FROM file_action WHERE action = 'DECLINED' AND sender_id = :userId", nativeQuery = true)
    int countDeclinedDocuments(@Param("userId") UUID userId);

    @Query(value = "SELECT MAX(created_at) FROM file_action WHERE sender_id = :userId", nativeQuery = true)
    Timestamp findLastActivity(@Param("userId") UUID userId);


    @Query("""
    SELECT fa
    FROM FileAction fa
    WHERE fa.receiverId = :receiverId
    AND fa.sender.userId <> :receiverId
    AND fa.createdAt = (
        SELECT MAX(sub.createdAt)
        FROM FileAction sub
        WHERE sub.file.id = fa.file.id
        AND sub.receiverId = fa.receiverId
    )
""")
    List<FileAction> findLatestFileActionsByReceiverId(@Param("receiverId") UUID receiverId);

    @Query("""
    SELECT f.action
    FROM FileAction f
    WHERE f.file.id = :fileId
      AND f.receiverId = :receiverId
      AND f.action IN ('TO_SIGN', 'TO_VIEW', 'TO_APPROVE')
    ORDER BY f.createdAt DESC
""")
    List<String> findLastRequestedAction(@Param("fileId") UUID fileId, @Param("receiverId") UUID receiverId);
}