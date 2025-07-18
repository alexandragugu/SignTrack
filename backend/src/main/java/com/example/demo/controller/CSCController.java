package com.example.demo.controller;

import com.example.demo.services.*;
import com.example.demo.utils.SecurityLogger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Controller
@RequestMapping("/csc")
public class CSCController {

    @Autowired
    CSCService cscService;

    @Autowired
    AuthService authService;

    @Value("${spring.csc.client_id}")
    private String clientId;

    @Value("${spring.csc.client_secret}")
    private String clientSecret;

    @Value("${spring.csc.redirect_uri}")
    private String redirectUri;

    @Value("${spring.csc.grant_type}")
    private String grantType;

    @Value("${sprin.csc.sad.url}")
    private String cscSADUrl;


    @GetMapping("/token")
    public ResponseEntity<Map<String,Object>> getToken(@RequestParam("code") String code, HttpServletResponse response) {
        String requesterId = authService.getUserIdFromJwt();
        String requesterUsername = authService.getUsernameFromJwt();
        try {
            SecurityLogger.log("CSC_TOKEN", requesterUsername, requesterId, "Requested CSC access token with code: " + code);

            ResponseEntity<String> token_response= cscService.getToken(code);
            ObjectMapper mapper=new ObjectMapper();
            JsonNode root=mapper.readTree(token_response.getBody());;

            String accessToken=root.path("access_token").asText();
            String expiresIn=root.path("expires_in").asText();

            Cookie cookie=new Cookie("csc_access_token", accessToken);
            cookie.setHttpOnly(true);
            cookie.setSecure(false);
            cookie.setMaxAge(3600);


            Cookie cookie_expires=new Cookie("csc_expires_in", expiresIn);
            cookie.setHttpOnly(true);
            cookie.setSecure(false);
            cookie.setMaxAge(3600);

            response.addCookie(cookie);
            response.addCookie(cookie_expires);

            Map<String,Object> responseBody=new HashMap<>();
            responseBody.put("access_token", accessToken);
            responseBody.put("expires_in", expiresIn);
            return  ResponseEntity.ok(responseBody);


        } catch (Exception e) {
            SecurityLogger.log("CSC_TOKEN_FAIL", requesterUsername, requesterId, "Failed to get CSC access token: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error","Error retrieving CSC token"));
        }
    }

    @PostMapping("/sadToken")
    public ResponseEntity<String> getSADToken(@RequestParam("code") String code) {
        String requesterId = authService.getUserIdFromJwt();
        String requesterUsername = authService.getUsernameFromJwt();

        try {
            SecurityLogger.log("CSC_SAD_TOKEN", requesterUsername, requesterId, "Requested CSC SAD token with code: " + code);
            return cscService.getSADToken(code);
        } catch (Exception e) {
            SecurityLogger.log("CSC_SAD_TOKEN_FAIL", requesterUsername, requesterId, "Failed to get CSC SAD token: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving CSC SAD token");
        }
    }

    @GetMapping("/extractToken")
    public ResponseEntity<?> extractTokenFromCookie(@CookieValue(value="csc_access_token", required = false) String accessToken, @CookieValue(value="csc_expires_in", required = false) String expiresInRaw){
        if(accessToken==null || expiresInRaw==null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error","No access token found in cookies"));
        }

        int expiresIn;
        try{
            expiresIn=Integer.parseInt(expiresInRaw);
        }catch (NumberFormatException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error","Invalid expires_in format cookie"));
            
        }
        return ResponseEntity.ok(Map.of("access_token",accessToken, "expires_in",expiresIn));


    }


}



