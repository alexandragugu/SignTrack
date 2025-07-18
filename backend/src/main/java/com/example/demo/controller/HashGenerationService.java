package com.example.demo.controller;

import com.example.demo.DTO.FilesInfoDTO;
import com.example.demo.DTO.HashPayload;
import com.example.demo.services.PadESigningService;
import com.example.demo.entities.Files;
import com.example.demo.entities.UserBucket;
import com.example.demo.models.SignatureTypeEnum;
import com.example.demo.resource.CertificateData;
import com.example.demo.resource.PendingPDFs;
import com.example.demo.resource.SignatureProperties;
import com.example.demo.services.FileService;
import com.example.demo.services.KeycloakService;
import com.example.demo.services.MinIOService;
import com.example.demo.services.SigningService;
import com.example.demo.utils.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.example.demo.utils.SecurityLogger.log;

@Service
public class HashGenerationService {

    @Autowired
    private FileService fileService;
    @Autowired
    private MinIOService minIOService;
    @Autowired
    private KeycloakService keycloakService;
    @Autowired
    private PadESigningService padESigningService;

    @Autowired
    private SigningService signingService;

    int MAX_THREADS = 10;
    @Autowired
    @Qualifier("hashExecutor")
    private ExecutorService executorService;


    public void generateHashesAndStream(
            List<FilesInfoDTO> files,
            CertificateData certificateData,
            List<X509Certificate> certificateChain,
            SignatureProperties signatureProperties,
            String requestId,
            SignatureTypeEnum signatureTypeEnum,
            String signerId
    ) {
        CountDownLatch latch = new CountDownLatch(files.size());

        for (FilesInfoDTO fileInfo : files) {
            executorService.submit(() -> {
                try {

                    String fileId = fileInfo.getFileId();
                    String filename = fileInfo.getFilename();
                    Files fileObject = fileService.getFileByFileId(UUID.fromString(fileId));
                    UserBucket senderBucket = fileObject.getUserBucket();
                    String fileExtension = filename.substring(filename.lastIndexOf("."));
                    String fileIdentifier = fileId + fileExtension;
                    byte[] fileBytes = minIOService.getFile(fileIdentifier, senderBucket.getBucketUuid().toString());
                    String userFullName = keycloakService.getUserById(signerId, UserDetails.FULLNAME);

                    PendingPDFs pending = null;
                    if (signatureTypeEnum.equals(SignatureTypeEnum.CLOUD)) {

                        pending = padESigningService.signDocumentCSCInMem(
                                fileBytes, certificateData, certificateChain, filename, signatureProperties, userFullName
                        );
                    } else {
                        pending = padESigningService.signDocumentInMem(fileBytes, certificateData, certificateChain, filename, signatureProperties, userFullName);
                    }

                    byte[] hash = pending.getHashValue();
                    pending.setFileId(fileId);
                    pending = padESigningService.setOptionsForPending(pending, signatureProperties);
                    pending.setSignatureTypeEnum(signatureTypeEnum);

                    signingService.savePending(UUID.fromString(fileId), pending);

                    String base64Hash = Base64.getEncoder().encodeToString(hash);
                    signingService.sendHashToClient(requestId, new HashPayload(UUID.fromString(fileId), base64Hash));
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        executorService.submit(() -> {
            try {
                latch.await();
                signingService.completeEmitter(requestId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }
}




