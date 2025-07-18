package com.example.demo.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityLogger {
    private static final Logger log = LoggerFactory.getLogger("SECURITY");

    public static void log(String action, String username, String userId, String message) {
        log.info("[SECURITY][{}] User: '{}' (ID: {}) - {}", action.toUpperCase(), username, userId, message);
    }

    public static void log(String action, String userId, String message) {
        log.info("[SECURITY][{}] UserID: {} - {}", action.toUpperCase(), userId, message);
    }

    public static void log(String message) {
        log.info("[SECURITY] {}", message);
    }

    public static void log(String action, String message) {
        log.info("[SECURITY] {}", message);
    }
}
