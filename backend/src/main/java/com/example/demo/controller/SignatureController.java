package com.example.demo.controller;


import com.example.demo.DTO.SignRequestDTO;
import com.example.demo.DTO.UploadedFileDTO;
import com.example.demo.services.PadESigningService;
import com.example.demo.entities.*;
import com.example.demo.models.FileStatusEnum;
import com.example.demo.models.SignatureTypeEnum;
import com.example.demo.resource.CertificateData;
import com.example.demo.resource.PendingPDFRegistry;
import com.example.demo.resource.PendingPDFs;
import com.example.demo.resource.SignatureProperties;
import com.example.demo.services.*;
import com.example.demo.utils.CertificateUtils;
import com.example.demo.utils.FileActionType;
import com.example.demo.utils.UserDetails;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.esig.dss.model.DSSDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


import static java.nio.file.Paths.get;


@RestController
@RequestMapping("/file")
public class SignatureController {


    @Autowired
    private final PadESigningService padESigningService;

    private final List<PendingPDFs> pendingPDFsList = new ArrayList<>();

    @Autowired
    private MinIOService minIOService;

    @Autowired
    private KeycloakService keycloakService;

    @Autowired
    private AuthService authService;

    @Autowired
    private FileService fileService;

    @Autowired
    private UserBucketService userBucketService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private FileActionService fileActionService;

    @Autowired
    private FileStatusService fileStatusService;

    @Autowired
    private SignaturesService signaturesService;

    @Autowired
    private VisibleSignatureService visibleSignatureService;

    @Autowired
    private HashGenerationService hashGenerationService;

    @Autowired
    private PendingPDFRegistry pendingPDFRegistry;

    @Autowired
    private SigningService signingService;

    public SignatureController(PadESigningService padESigningService) {
        this.padESigningService = padESigningService;
    }


    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam String requestId) {
        return signingService.registerEmitter(requestId);
    }

    @PostMapping("/upload/csc/inMemBulk/stream")
    public ResponseEntity<Void> uploadFilesCSCBulkStream(@RequestBody SignRequestDTO signRequestDTO) throws Exception {
        String decodedCertificatesData = URLDecoder.decode(signRequestDTO.getCertificatesData(), StandardCharsets.UTF_8);
        String requestId = signRequestDTO.getRequestId();
        String signerId = authService.getUserIdFromJwt();

        SignatureProperties signatureProperties = new SignatureProperties(
                signRequestDTO.getProfile(),
                signRequestDTO.isVisibleSignature(),
                signRequestDTO.getPage(),
                signRequestDTO.getPosition(),
                signRequestDTO.getType()
        );

        SignatureTypeEnum signatureType = SignatureTypeEnum.valueOf(signRequestDTO.getType().toUpperCase());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(decodedCertificatesData);
        String certificate = rootNode.get("certificate").asText().replaceAll("\r\n", "").trim();

        List<String> certificateList = new ArrayList<>();
        for (JsonNode node : rootNode.get("certificateChain")) {
            certificateList.add(node.asText().replaceAll("\r\n", "").trim());
        }

        List<X509Certificate> certificateChain = CertificateUtils.getCertificatChain(certificateList);
        CertificateData certificateData = new CertificateData(certificate, certificateList, "1.2.840.113549.1.1.11");

        hashGenerationService.generateHashesAndStream(
                signRequestDTO.getFiles(), certificateData, certificateChain, signatureProperties, requestId, signatureType, signerId
        );

        return ResponseEntity.accepted().build();
    }

    @PostMapping("/receiveSignature")
    public ResponseEntity<UploadedFileDTO> getSignedHash(@RequestBody SignatureResponse signatureResponse) throws Exception {

        String id = authService.getUserIdFromJwt();

        byte[] decodedHash = Base64.getDecoder().decode(signatureResponse.getHashValue());
        byte[] decodedSignature = Base64.getDecoder().decode(signatureResponse.getSignatureValue().replaceAll("\\r\\n", ""));
        signatureResponse.setHashValue(decodedHash.toString());

        Optional<PendingPDFs> match = pendingPDFsList.stream().filter(p -> Arrays.equals(p.getHashValue(), decodedHash)).findFirst();

        if (match.isPresent()) {
            PendingPDFs pdf = match.get();
            System.out.println("Am gasit match: " + Arrays.toString(pdf.getHashValue()));
            pdf.setSignedHash(decodedSignature);

            DSSDocument signedDocument = padESigningService.insertSignature(pdf);

            String bucketId = userBucketService.getBucketUuid(UUID.fromString(id));
            String documentName = signedDocument.getName();
            Files fileObject = fileService.uploadFile(UUID.fromString(id), documentName);

            String filenameMinio = minIOService.uploadFile(signedDocument, bucketId, fileObject.getId().toString());
            String fileUrl = minIOService.getPresignedUrl(filenameMinio, bucketId, documentName);
            UploadedFileDTO newFileData = new UploadedFileDTO(documentName, fileUrl);
            newFileData.setFileId(fileObject.getId().toString());

            return ResponseEntity.ok(newFileData);
        } else {
            System.out.println("Nu am gasit match");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }


    @PostMapping("/receiveSignature/inMemFile")
    public ResponseEntity<UploadedFileDTO> receiveSingleSignedHash(@RequestBody SignatureResponse signatureResponse) throws Exception {
        long start = System.currentTimeMillis();
        String userId = authService.getUserIdFromJwt();
        String username = authService.getUsernameFromJwt();

        UUID fileId = UUID.fromString(signatureResponse.getFileId());
        PendingPDFs pending = signingService.getPending(fileId);

        if (pending == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        byte[] decodedSignature = Base64.getDecoder().decode(signatureResponse.getSignatureValue().replaceAll("\\r\\n", ""));
        pending.setSignedHash(decodedSignature);

        DSSDocument signedDocument = padESigningService.insertSignatureInMemFile(pending);

        Files fileObject = fileService.getFileByIdAndReceiverId(fileId, UUID.fromString(userId));
        String bucketId = userBucketService.getBucketUuid(fileObject.getUserBucket().getUserId());
        String filenameMinio = minIOService.uploadFile(signedDocument, bucketId, fileObject.getId().toString());

        String fileUrl = minIOService.getPresignedUrl(pending.getFileId(), bucketId, signedDocument.getName());

        UploadedFileDTO uploadedDTO = new UploadedFileDTO(signedDocument.getName(), fileUrl);
        uploadedDTO.setFileId(fileObject.getId().toString());

        String ownerEmail = keycloakService.getUserById(fileObject.getUserBucket().getUserId().toString(), UserDetails.EMAIL);

        FileAction fileAction = fileActionService.createFileAction(
                fileObject.getId(),
                fileObject.getUserBucket().getUserId(),
                UUID.fromString(userId),
                FileActionType.SIGNED
        );

        Signatures newSignature = signaturesService.createSignature(
                fileAction.getId(),
                pending.getSignatureOptionEnum(),
                pending.getSignatureTypeEnum(),
                pending.getVisibility()
        );

        if (pending.getVisibility()) {
            visibleSignatureService.createVisibleSignature(
                    newSignature.getId(),
                    pending.getSignaturePageEnum(),
                    pending.getSignaturePositionEnum()
            );
        }

        emailService.sendEmailNotification(
                ownerEmail,
                username,
                fileObject.getObjectName(),
                FileActionType.SIGNED,
                fileObject.getId(),
                fileObject.getUserBucket().getUserId()
        );

        if (fileStatusService.checkForFinished(fileObject.getId())) {
            fileStatusService.updateFileStatus(fileObject.getId(), FileStatusEnum.FINISHED);
        }
        signingService.removePending(fileId);

        long end = System.currentTimeMillis();
        long duration = end - start;
        System.out.println("Timp de execuție /receiveSignature/inMemFile: " + duration + " ms");


        return ResponseEntity.ok(uploadedDTO);
    }


}
