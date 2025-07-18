package com.example.demo.configuration;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;

public class ConditionalBearerTokenResolver implements BearerTokenResolver {

    private final CookieBearerTokenResolver cookieResolver = new CookieBearerTokenResolver();

    @Override
    public String resolve(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path != null && path.startsWith("/auth/")) {
            return null;
        }

        return cookieResolver.resolve(request);
    }
}
