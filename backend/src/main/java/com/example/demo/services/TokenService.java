package com.example.demo.services;

import com.example.demo.entities.FileAction;
import com.example.demo.entities.Files;
import com.example.demo.entities.User;
import com.example.demo.models.FileActionStatus;
import com.example.demo.utils.FileActionType;
import com.example.demo.utils.UserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.*;

@Service
public class TokenService {

    @Autowired
    AuthService authService;

    @Autowired
    FileActionService fileActionService;

    @Autowired
    KeycloakService keycloakService;

    @Autowired
    UserBucketService userBucketService;

    @Autowired
    MinIOService minIOService;

    final String secretKey = Base64.getEncoder().encodeToString("cheie-secreta-puternica-foarte-lunga-pentru-hs256".getBytes());

    public String getSecretKey() {
        return secretKey;
    }

    public String generateFileToken(String fileId, String userId, String email, FileActionType actionType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("fileId", fileId);
        claims.put("userId", userId);
        claims.put("actionType", actionType.toString());

        long expirationTime =7* 24 * 60 * 60 * 1000;

        return Jwts.builder()
                .setSubject(email)
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey)), SignatureAlgorithm.HS256)
                .compact();
    }

    public Map<String,Object> validateToken(String token) throws Exception {
        Claims claims= Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey)))
                .build()
                .parseClaimsJws(token)
                .getBody();
        String id=authService.getUserIdFromJwt();
        if(id==null || !id.equals((String) claims.get("userId"))){
            throw new RuntimeException("This request is not for you.");
        }

        String fileActionType=(String) claims.get("actionType");
        String fileId=(String) claims.get("fileId");


        boolean valid=validateNextAction(fileId, id, fileActionType);
        if(!valid){
            throw new RuntimeException("You have already responded to this request. No further action is required.");
        }


        Files file=getFileFromAction(fileActionType,id,fileId);
        if(file==null){
            throw new RuntimeException("Token invalid sau expirat.");
        }

        UUID ownerId=userBucketService.getUserBucketByBucketUuid(file.getUserBucket().getBucketUuid()).getUserId();

        String senderUsername=keycloakService.getUserById(ownerId.toString(), UserDetails.USERNAME);

        String fileUrl=minIOService.getPresignedUrl(file.getId().toString(), file.getUserBucket().getBucketUuid().toString(), file.getObjectName());

        Map<String, Object> fileData = new HashMap<>();
        fileData.put("filename", file.getObjectName());
        fileData.put("senderUsername", senderUsername);
        fileData.put("fileUrl", fileUrl);
        fileData.put("actionType", fileActionType);
        fileData.put("fileId", fileId);
        fileData.put("senderId", ownerId.toString());

        return fileData;
    }

    private Files getFileFromAction(String fileActionType, String userId, String fileId){
        Files file=new Files();
        if(fileActionType.startsWith("TO_")){
            file=fileActionService.getFileByReceiverIdAndFileIdAndAction(UUID.fromString(userId), UUID.fromString(fileId), FileActionType.valueOf(fileActionType));
            return  file;
        }

        if(fileActionType.endsWith("ED")){
            file=fileActionService.getFileByOwnerIdAndFileIdAndAction(UUID.fromString(userId), UUID.fromString(fileId), FileActionType.valueOf(fileActionType));
            return  file;
        }

        return null;
    }

    private boolean validateNextAction(String fileId, String OwnerId, String actionType){
                switch (FileActionType.valueOf(actionType)){
                    case TO_SIGN:
                        boolean signed=fileActionService.registrationExists(UUID.fromString(OwnerId), UUID.fromString(fileId), FileActionType.SIGNED);
                        return !signed;
                    case TO_APPROVE:
                        boolean approved=fileActionService.registrationExists(UUID.fromString(OwnerId), UUID.fromString(fileId), FileActionType.APPROVED);
                        return !approved;
                    case TO_VIEW:
                        boolean viewed=fileActionService.registrationExists(UUID.fromString(OwnerId), UUID.fromString(fileId), FileActionType.VIEWED);
                        return !viewed;

                    case VIEWED:
                        return true;

                    case SIGNED:
                        return true;

                    case APPROVED:
                        return true;

                    case DECLINED:
                        return true;
                    default:
                        return false;
                }
        }
}
