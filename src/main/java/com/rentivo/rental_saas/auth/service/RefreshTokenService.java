package com.rentivo.rental_saas.auth.service;

import com.rentivo.rental_saas.auth.entity.RefreshToken;
import com.rentivo.rental_saas.auth.entity.Users;
import com.rentivo.rental_saas.auth.repository.RefreshTokenRepo;
import com.rentivo.rental_saas.auth.security.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepo refreshTokenRepo;
    private final JwtService jwtService;

    @Value("${security.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    /**
     * Issues and persists a new refresh token for a user.
     */
    @Transactional
    public String issueRefreshToken(Users user) {

        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        RefreshToken token = RefreshToken.builder()
                .user(user)
                .tokenHash(hashToken(refreshToken))
                .expiresAt(LocalDateTime.now().plus(
                        Duration.ofMillis(refreshTokenExpiration)
                ))
                .build();

        refreshTokenRepo.save(token);

        return refreshToken;
    }

    /**
     * Validates the existing refresh token, revokes it,
     * and issues a replacement.
     */
    @Transactional
    public RotatedToken rotateRefreshToken(String rawToken) {

        if (!jwtService.isTokenValid(rawToken)) {
            throw new BadCredentialsException("Invalid or expired refresh token.");
        }

        if (!"REFRESH".equals(jwtService.extractTokenType(rawToken))) {
            throw new BadCredentialsException("Invalid token type.");
        }

        String tokenHash = hashToken(rawToken);

        RefreshToken existingToken = refreshTokenRepo
                .findByTokenHashForUpdate(tokenHash)
                .orElseThrow(() ->
                        new BadCredentialsException("Refresh token not found.")
                );

        if (existingToken.isRevoked() ||
                existingToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadCredentialsException("Refresh token has expired or been revoked.");
        }

        Users user = existingToken.getUser();

        // Revoke the existing token.
        existingToken.setRevoked(true);
        refreshTokenRepo.save(existingToken);

        // Issue its replacement.
        String newRefreshToken = issueRefreshToken(user);

        return new RotatedToken(user, newRefreshToken);
    }

    /**
     * Revokes all active refresh tokens belonging to a user.
     */
    @Transactional
    public void revokeAllUserTokens(Long userId) {
        refreshTokenRepo.revokeAllByUserId(userId);
    }

    /**
     * Revokes a specific refresh token.
     */
    @Transactional
    public void revokeToken(String rawToken) {

        String tokenHash = hashToken(rawToken);

        refreshTokenRepo.findByTokenHashForUpdate(tokenHash)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepo.save(token);
                });
    }

    /**
     * Generates a deterministic SHA-256 hash for database lookup.
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm unavailable.", exception);
        }
    }

    public record RotatedToken(Users user, String refreshToken) {
    }
}