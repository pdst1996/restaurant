package com.ps.restaurant.config;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/** Signs and verifies JWTs whose subject contains the user's email address. */
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

    /** Creates a signed token with the email as its subject and a configured expiration time. */
    public String generateToken(String email) {
        Date now = new Date();
        return Jwts.builder()
            .subject(email)
            .issuedAt(now)
            .expiration(new Date(now.getTime() + expirationMs))
            .signWith(key)
            .compact();
    }

    /** Verifies the token and returns its subject; invalid or expired tokens raise an exception. */
    public String extractEmail(String token) {
        return parse(token).getSubject();
    }

    /** Returns whether signature verification and claims parsing succeed without a JWT error. */
    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false; // Reject invalid signatures, expired tokens, and malformed input.
        }
    }

    /** Verifies the signature and expiration before returning the signed claims. */
    private Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build()
            .parseSignedClaims(token).getPayload();
    }
}
