package com.example.demo.repositories;

import com.example.demo.DTO.MostActiveSignerDTO;
import com.example.demo.entities.Signatures;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SignaturesRepository extends JpaRepository<Signatures, UUID> {

    @Query(value = """
        SELECT ub.username, COUNT(s.id) AS signed_count
        FROM signatures s
        JOIN file_action fa ON s.file_action = fa.id
        JOIN user_bucket ub ON fa.sender_id = ub.user_id
        GROUP BY ub.username
        ORDER BY signed_count DESC
        LIMIT 1
        """, nativeQuery = true)
    Object findMostActiveSignerNative();

    @Query(value = """
        SELECT COUNT(s.id)
        FROM signatures s
        JOIN file_action fa ON s.file_action = fa.id
        WHERE 
          EXTRACT(MONTH FROM fa.created_at) = EXTRACT(MONTH FROM CURRENT_DATE)
          AND EXTRACT(YEAR FROM fa.created_at) = EXTRACT(YEAR FROM CURRENT_DATE)
        """, nativeQuery = true)
    int countSignaturesThisMonth();


    @Query(value = """
        SELECT 
            ub.username AS signer,
            s.created_at AS signatureDate,
            s.signature_type AS signatureType,
            s.visibility AS visibility,
            vs.position AS position,
            s.signature_option AS profile,
            f.object_name AS fileName,
            f.id AS fileId
        FROM signatures s
        JOIN file_action fa ON s.file_action = fa.id
        JOIN files f ON fa.file_id = f.id
        JOIN user_bucket ub ON fa.sender_id = ub.user_id
        LEFT JOIN visible_signature vs ON vs.signature = s.id
        ORDER BY s.created_at DESC
        """, nativeQuery = true)
    List<Object[]> findSignatureActivity();
}
