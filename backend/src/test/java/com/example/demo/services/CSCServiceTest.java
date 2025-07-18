package com.example.demo.services;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class CSCServiceTest {

    private CSCService cscService;
    private RestTemplate restTemplate;

    @BeforeEach
    public void setUp() {
        cscService = new CSCService();

        ReflectionTestUtils.setField(cscService, "clientId", "test-client-id");
        ReflectionTestUtils.setField(cscService, "clientSecret", "test-client-secret");
        ReflectionTestUtils.setField(cscService, "redirectUri", "http://localhost/callback");
        ReflectionTestUtils.setField(cscService, "grantType", "authorization_code");
        ReflectionTestUtils.setField(cscService, "cscTokenUrl", "https://mock-csc.com/token");
        restTemplate = mock(RestTemplate.class);
        CSCService spyService = Mockito.spy(cscService);
        doReturn(restTemplate).when(spyService).createRestTemplate();

        this.cscService = spyService;
    }

    private RestTemplate createRestTemplate() {
        return new RestTemplate();
    }

    @Test
    public void getToken_shouldReturnResponseEntity() {
        String code = "auth-code";
        String mockResponseBody = "{\"access_token\":\"abc123\"}";
        ResponseEntity<String> mockResponse = new ResponseEntity<>(mockResponseBody, HttpStatus.OK);

        when(restTemplate.postForEntity(
                anyString(),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(mockResponse);

        ResponseEntity<String> result = cscService.getToken(code);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(mockResponseBody, result.getBody());
    }
}