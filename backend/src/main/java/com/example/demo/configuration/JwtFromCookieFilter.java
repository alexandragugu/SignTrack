package com.example.demo.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Instant;
import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtFromCookieFilter extends OncePerRequestFilter {

    private final JwtDecoder jwtDecoder;

    public JwtFromCookieFilter(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        if (path.startsWith("/auth/") || path.startsWith("/api/sign/stream")) {
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("token".equals(cookie.getName())) {
                    String token = cookie.getValue();

                    logger.debug("Token gasit: " + token);

                    try {
                        Jwt jwt = jwtDecoder.decode(token);

                        Instant expirationTime = jwt.getExpiresAt();
                        Instant now = Instant.now();

                        if (expirationTime != null && expirationTime.isBefore(now)) {
                            throw new InvalidBearerTokenException("Token expired");
                        }

                        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
                        JwtAuthenticationToken authentication = new JwtAuthenticationToken(
                                jwt,
                                authorities);

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        logger.debug("Autentificare reușita cu token din cookie.");
                    } catch (Exception e) {
                        logger.warn("Autentificare esuata cu tokenul din cookie: " + e.getMessage());
                        SecurityContextHolder.clearContext();

                        logger.warn("Autentificarea tokenului din cookie a esuat: " + e.getMessage());


                    }
                    break;
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {

        Collection<String> roles = jwt.getClaimAsStringList("roles");
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }

}
