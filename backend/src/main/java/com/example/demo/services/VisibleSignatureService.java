package com.example.demo.services;

import com.example.demo.entities.Signatures;
import com.example.demo.entities.VisibleSignature;
import com.example.demo.models.SignaturePageEnum;
import com.example.demo.models.SignaturePositionEnum;
import com.example.demo.repositories.SignaturesRepository;
import com.example.demo.repositories.VisibleSignatureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class VisibleSignatureService {

    private final VisibleSignatureRepository visibleSignatureRepository;
    private final SignaturesRepository signatureRepository;

    @Autowired
    public VisibleSignatureService(VisibleSignatureRepository visibleSignatureRepository,
                                   SignaturesRepository signatureRepository) {
        this.visibleSignatureRepository = visibleSignatureRepository;
        this.signatureRepository = signatureRepository;
    }

    public VisibleSignature createVisibleSignature(UUID signatureId, SignaturePageEnum page, SignaturePositionEnum position) {
        Signatures signature = signatureRepository.findById(signatureId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid signature ID"));

        VisibleSignature vs = new VisibleSignature();
        vs.setSignature(signature);
        vs.setPage(page);
        vs.setPosition(position);

        return visibleSignatureRepository.save(vs);
    }
}