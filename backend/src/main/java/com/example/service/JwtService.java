package com.example.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import java.time.Duration;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey key;
    private final Duration ttl = Duration.ofHours(12);

    public JwtService(@Value("${app.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // Generate token
    public String generateToken(String username) {
        Date now = new Date();
        return Jwts.builder().subject(username).issuedAt(now).expiration(new Date(now.getTime() + ttl.toMillis())).signWith(key).compact();
    }

    // Verify and return token 
    public String verifyAndGetUsername(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
    }
}
