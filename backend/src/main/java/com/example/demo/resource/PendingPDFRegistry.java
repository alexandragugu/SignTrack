package com.example.demo.resource;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PendingPDFRegistry {
    private final ConcurrentHashMap<String, PendingPDFs> registry = new ConcurrentHashMap<>();

    public void put(String fileId, PendingPDFs pending) {
        registry.put(fileId, pending);
    }

    public PendingPDFs get(String fileId) {
        return registry.get(fileId);
    }

    public Collection<String> getAllFileIds() {
        return registry.keySet();
    }

    public void remove(String fileId) {
        registry.remove(fileId);
    }

    public boolean contains(String fileId) {
        return registry.containsKey(fileId);
    }
}
