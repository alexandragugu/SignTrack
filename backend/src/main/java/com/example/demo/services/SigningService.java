package com.example.demo.services;

import com.example.demo.DTO.HashPayload;
import com.example.demo.resource.PendingPDFs;
import io.jsonwebtoken.io.IOException;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class SigningService {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<UUID, PendingPDFs> pendingFiles = new ConcurrentHashMap<>();

    public SseEmitter registerEmitter(String requestId) {
        SseEmitter emitter = new SseEmitter(30000L);

        emitter.onCompletion(() -> emitters.remove(requestId));
        emitter.onTimeout(() -> emitters.remove(requestId));
        emitter.onError((e) -> emitters.remove(requestId));

        emitters.put(requestId, emitter);

        try {
            emitter.send(SseEmitter.event()
                    .name("ping")
                    .data("connected"));
        } catch (IOException e) {
            emitter.completeWithError(e);
            emitters.remove(requestId);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }


        return emitter;
    }

    public void sendHashToClient(String requestId, HashPayload payload) {
        SseEmitter emitter = emitters.get(requestId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("hash-ready")
                        .data(payload));
            } catch (IOException e) {
                emitter.completeWithError(e);
                emitters.remove(requestId);
            } catch (java.io.IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void completeEmitter(String requestId) {
        SseEmitter emitter = emitters.remove(requestId);
        if (emitter != null) {
            emitter.complete();
        }
    }

    public void savePending(UUID fileId, PendingPDFs pending) {
        pendingFiles.put(fileId, pending);
    }

    public PendingPDFs getPending(UUID fileId) {
        return pendingFiles.get(fileId);
    }

    public void removePending(UUID fileId) {
        pendingFiles.remove(fileId);
    }
}
