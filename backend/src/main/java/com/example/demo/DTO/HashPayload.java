package com.example.demo.DTO;

import java.util.UUID;

public record HashPayload(UUID fileId, String hashBase64) {}
