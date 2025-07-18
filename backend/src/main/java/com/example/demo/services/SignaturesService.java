package com.example.demo.services;

import com.example.demo.DTO.MostActiveSignerDTO;
import com.example.demo.DTO.SignatureActivityDTO;
import com.example.demo.entities.Signatures;
import com.example.demo.models.SignatureOptionEnum;
import com.example.demo.models.SignatureTypeEnum;
import com.example.demo.repositories.FileActionRepository;
import com.example.demo.repositories.SignaturesRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SignaturesService {

    private final SignaturesRepository signatureRepository;
    private final FileActionRepository fileActionRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public SignaturesService(SignaturesRepository signatureRepository, FileActionRepository fileActionRepository) {
        this.signatureRepository = signatureRepository;
        this.fileActionRepository = fileActionRepository;
    }

    public Signatures createSignature(UUID fileActionId, SignatureOptionEnum option, SignatureTypeEnum type, boolean visibility) {
        var fileAction = fileActionRepository.findById(fileActionId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid file action ID"));

        Signatures signature = new Signatures();
        signature.setFileAction(fileAction);
        signature.setSignatureOption(option);
        signature.setSignatureType(type);
        signature.setVisibility(visibility);

        return signatureRepository.save(signature);
    }

    public int getTotalSignatures(){
        return signatureRepository.findAll().size();
    }

    public Optional<MostActiveSignerDTO> findMostActiveSigner() {
        try {
            Object result = signatureRepository.findMostActiveSignerNative();

            if (result != null) {
                Object[] row = (Object[]) result;

                MostActiveSignerDTO dto = new MostActiveSignerDTO();
                dto.setUsername((String) row[0]);
                dto.setSignedCount(((Number) row[1]).intValue());

                return Optional.of(dto);
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    public int getThisMonthSignatures() {
        return signatureRepository.countSignaturesThisMonth();
    }

    public List<SignatureActivityDTO> getSignatureActivity() {
        List<Object[]> results = signatureRepository.findSignatureActivity();
        List<SignatureActivityDTO> activityList = new ArrayList<>();

        for (Object[] row : results) {
            SignatureActivityDTO dto = new SignatureActivityDTO(
                    (String) row[0],
                    (row[1] != null) ? ((Timestamp) row[1]).toLocalDateTime() : null,
                    (String) row[2],
                    (Boolean) row[3],
                    (String) row[4],
                    (String) row[5]
            );
            activityList.add(dto);
        }

        return activityList;
    }

}