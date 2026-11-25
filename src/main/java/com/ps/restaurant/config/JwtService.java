package com.ps.restaurant.config;

import java.util.Date;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/** Signs and verifies JWTs whose subject contains the immutable account identifier. */
@Service 
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    /** Decodes the configured Base64 HMAC secret and stores the token lifetime in milliseconds. */
    public JwtService(@Value ("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expirationMs = expirationMs;
    }

    public String generateToken(Long userId) {
        Date now = new Date();
        return Jwts.builder()
            .subject("user:" + userId)
            .issuedAt(now)
            .expiration(new Date(now.getTime() + expirationMs))
            .signWith(key)
            .compact();
    }

    /** Old email-based tokens no longer authenticate after the upgrade. */
    public Optional<Long> userId(String token) {
        try {
            String subject = parse(token).getSubject();
            if (subject == null || !subject.startsWith("user:")) return Optional.empty();
            long id = Long.parseLong(subject.substring(5));
            return id > 0 ? Optional.of(id) : Optional.empty();
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /** Verifies the signature and expiration before returning the signed claims. */
    private Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build()
            .parseSignedClaims(token).getPayload();
    }
}
