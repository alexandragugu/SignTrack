package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

@Component
public class JwtDecoderFactory {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String JWK_SET_URI;

    public JwtDecoder create() {
        return NimbusJwtDecoder.withJwkSetUri(JWK_SET_URI).build();
    }
}
