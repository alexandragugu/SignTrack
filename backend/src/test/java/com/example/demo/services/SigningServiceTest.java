package com.example.demo.services;

import com.example.demo.DTO.HashPayload;
import com.example.demo.resource.PendingPDFs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class SigningServiceTest {
    private SigningService signingService;

    @BeforeEach
    void setUp() {
        signingService = new SigningService();
    }


    @Test
    void testRegisterEmitter() {
        String requestId = "randomNumberGenerated";
        SseEmitter emitter = signingService.registerEmitter(requestId);
        assertNotNull(emitter);
    }

    @Test
    void testSendHashToClient() {
        String requestId = "randomNumberGenerated";
        signingService.registerEmitter(requestId);
        HashPayload payload = new HashPayload(UUID.randomUUID(), "base64HashValue");
        assertDoesNotThrow(() -> signingService.sendHashToClient(requestId, payload));
    }

    @Test
    void testCompleteEmitter() {
        String requestId = "randomNumberGenerated";
        signingService.registerEmitter(requestId);
        signingService.completeEmitter(requestId);
    }

    @Test
    void testPendingFilesOperations() {
        UUID fileId = UUID.randomUUID();
        PendingPDFs pending = new PendingPDFs(null, null);
        signingService.savePending(fileId, pending);
        assertEquals(pending, signingService.getPending(fileId));
        signingService.removePending(fileId);
        assertNull(signingService.getPending(fileId));
    }
}
