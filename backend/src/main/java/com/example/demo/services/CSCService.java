package com.example.demo.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class CSCService {

    @Value("${spring.csc.client_id}")
    private String clientId;

    @Value("${spring.csc.client_secret}")
    private String clientSecret;

    @Value("${spring.csc.redirect_uri}")
    private String redirectUri;

    @Value("${spring.csc.grant_type}")
    private String grantType;

    @Value("${sprin.csc.token.url}")
    private String cscTokenUrl;


    public ResponseEntity<String > getToken(String code) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("grant_type", grantType);
        params.add("code", code);
        params.add("redirect_uri", redirectUri);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        RestTemplate restTemplate = createRestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(cscTokenUrl, request, String.class);

        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    public  ResponseEntity<String> getSADToken(String code){

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("grant_type", grantType);
        body.add("code", code);
        body.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            RestTemplate restTemplate= createRestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(cscTokenUrl, HttpMethod.POST, request, String.class);
            return ResponseEntity.ok(response.getBody());
        } catch (RestClientException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching SAD token: " + e.getMessage());
        }
    }

    public RestTemplate createRestTemplate() {
        return new RestTemplate();
    }
}


