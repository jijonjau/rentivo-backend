package com.rentivo.rental_saas.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {
    private final SecretKey secretKey;
    @Getter
    private final long accessTokenExpirationTime;
    private final long refreshTokenExpirationTime;
    @Getter
    private final long otpTokenExpirationTime;
    @Getter
    private final long passwordResetTokenExpirationTime;

    public JwtService(
            @Value("${security.jwt.secret-key}") String secret,
            @Value("${security.jwt.access-token-expiration}") long accessTokenExpirationTime,
            @Value("${security.jwt.refresh-token-expiration}") long refreshTokenExpirationTime,
            @Value("${security.jwt.otp-token-expiration}") long otpTokenExpirationTime,
            @Value("${security.jwt.password-reset-token-expiration}")
            long passwordResetTokenExpirationTime
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpirationTime = accessTokenExpirationTime;
        this.refreshTokenExpirationTime = refreshTokenExpirationTime;
        this.otpTokenExpirationTime = otpTokenExpirationTime;
        this.passwordResetTokenExpirationTime = passwordResetTokenExpirationTime;
    }

    public String generateAccessToken(String email, String role) {
        return generateToken(email, role, accessTokenExpirationTime, "ACCESS");
    }

    public String generateRefreshToken(String email) {
        return generateToken(email, null, refreshTokenExpirationTime, "REFRESH");
    }

    public String generateToken(
            String email,
            String role,
            long expirationTime,
            String type
    ) {
        Date now = new Date();

        JwtBuilder builder = Jwts.builder()
                .subject(email)
                .claim("type", type)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationTime))
                .signWith(secretKey);

        if (role != null) {
            builder.claim("role", role);
        }

        return builder.compact();
    }

    public String generateOtpToken(Long userId, String tokenType) {
        Date now = new Date();

        return Jwts.builder()
                .subject(userId.toString())
                .claim("type", tokenType)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + otpTokenExpirationTime))
                .signWith(secretKey)
                .compact();
    }

    public String generatePasswordResetToken(Long userId) {
        Date now = new Date();

        return Jwts.builder()
                .subject(userId.toString())
                .claim("type", "PASSWORD_RESET")
                .issuedAt(now)
                .expiration(new Date(now.getTime() + passwordResetTokenExpirationTime))
                .signWith(secretKey)
                .compact();
    }

    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token).getPayload().getSubject();
    }

    public String extractTokenType(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("type", String.class);
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

    public Long extractOtpUserId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String tokenType = claims.get("type", String.class);

        if (!List.of(
                "REGISTER_OTP",
                "LOGIN_OTP",
                "PASSWORD_RESET_OTP"
        ).contains(tokenType)) {
            throw new IllegalArgumentException("Invalid OTP token");
        }

        return Long.valueOf(claims.getSubject());
    }

    public Long extractPasswordResetUserId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        if (!"PASSWORD_RESET".equals(claims.get("type", String.class))) {
            throw new IllegalArgumentException("Invalid password reset token");
        }

        return Long.valueOf(claims.getSubject());
    }
}
