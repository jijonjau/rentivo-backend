package com.rentivo.rental_saas.auth.service;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey secretKey;
    @Getter
    private final long accessTokenExpirationTime;
    private final long refreshTokenExpirationTime;

    public JwtService(
            @Value("${security.jwt.secret-key}") String secret,
            @Value("${security.jwt.access-token-expiration}") long accessTokenExpirationTime,
            @Value("${security.jwt.refresh-token-expiration}") long refreshTokenExpirationTime
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpirationTime = accessTokenExpirationTime;
        this.refreshTokenExpirationTime = refreshTokenExpirationTime;
    }

    public String generateAccessToken(String email, String role) {
        return generateToken(email, role, accessTokenExpirationTime);
    }

    public String generateRefreshToken(String email) {
        return generateToken(email, null, refreshTokenExpirationTime);
    }

    public String generateToken(String email, String role, long expirationTime) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expirationTime);

        JwtBuilder builder = Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(secretKey);

        if (role != null) {
            builder.claim("role", role);
        }
        return builder.compact();
    }

    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token).getPayload().getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
