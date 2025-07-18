package com.example.demo.controller;

import com.example.demo.entities.Files;
import com.example.demo.models.FileActionStatus;
import com.example.demo.services.AuthService;
import com.example.demo.services.FileActionService;
import com.example.demo.services.KeycloakService;
import com.example.demo.services.TokenService;
import com.example.demo.utils.SecurityLogger;
import com.example.demo.utils.UserDetails;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Controller
public class TokenController {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private AuthService authService;

    @Autowired
    private FileActionService fileActionService;

    @Autowired
    private KeycloakService keycloakService;

    @GetMapping("/validateToken")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestParam String token) {
        String id = authService.getUserIdFromJwt();
        String username = authService.getUsernameFromJwt();
        try {

            Map<String, Object> claims = tokenService.validateToken(token);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("filename", claims.get("filename"));
            response.put("senderUsername", claims.get("senderUsername"));
            response.put("fileUrl", claims.get("fileUrl"));
            response.put("actionType", claims.get("actionType"));
            response.put("fileId", claims.get("fileId"));

            List<FileActionStatus> fileStatusList = new ArrayList<>();

            if (claims.get("senderId").equals(id)) {
                fileStatusList = fileActionService.getPersonalFileActions(UUID.fromString(claims.get("fileId").toString()));
            } else {
                fileStatusList = fileActionService.getFileActions(UUID.fromString(claims.get("fileId").toString()));
            }

            System.out.println("File status list: " + fileStatusList);

            List<Map<String, String>> resultList = new ArrayList<>();

            for (FileActionStatus status : fileStatusList) {
                Map<String, String> statusMap = new HashMap<>();
                String receiverUsername = keycloakService.getUserById(status.getReceiverId().toString(), UserDetails.USERNAME);
                statusMap.put("receiverUsername", receiverUsername);
                statusMap.put("action", status.getCurrentStatus());
                statusMap.put("currentStatus", status.getCurrentStatus());
                statusMap.put("currentDate", status.getCurrentStatusDate().toString());
                resultList.add(statusMap);
            }

            response.put("receiverActions", resultList);

            SecurityLogger.log("VALIDATE_TOKEN", username, id, "Validated token for file ID: " + claims.get("fileId"));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            SecurityLogger.log("VALIDATE_TOKEN_FAIL", username, id, "Runtime error during token validation: " + e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            SecurityLogger.log("VALIDATE_TOKEN_FAIL", username, id, "Token invalid sau expirat.");
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Token invalid sau expirat.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }


}
