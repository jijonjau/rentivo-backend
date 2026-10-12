package com.rentivo.rental_saas.auth.service;

import com.rentivo.rental_saas.auth.dto.AuthResponse;
import com.rentivo.rental_saas.auth.entity.Otp;
import com.rentivo.rental_saas.auth.entity.User;
import com.rentivo.rental_saas.auth.repository.OtpRepository;
import com.rentivo.rental_saas.auth.repository.UserRepository;
import com.rentivo.rental_saas.common.exception.InvalidOtpException;
import com.rentivo.rental_saas.common.exception.ResourceNotFoundException;
import com.rentivo.rental_saas.notification.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class OtpService {
    private final OtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final NotificationService notificationService;

    public void generateOtp(Long userId) {
        String otpValue = String.valueOf(
                ThreadLocalRandom.current().nextInt(10000, 1000000)
        );

        Otp otp = Otp.builder()
                .userId(userId)
                .otpHash(passwordEncoder.encode(otpValue))
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .attempts(0)
                .verified(false)
                .build();
        otpRepository.save(otp);

        // publish a queue
        System.out.println("OTP: " + otpValue);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        notificationService.sendOtpEmail(user.getEmail(), otpValue);
    }

    public AuthResponse verifyOtp(Long userId, String otp) {
        Otp otpSent = otpRepository
                .findTopByUserIdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new InvalidOtpException("Invalid OTP"));

        if (otpSent.isVerified()) {
            throw new InvalidOtpException("OTP already verified");
        }

        if (LocalDateTime.now().isAfter(otpSent.getExpiresAt())) {
            throw new InvalidOtpException("OTP has expired");
        }

        if (otpSent.getAttempts() >= 5) {
            throw new InvalidOtpException("Too many attempts");
        }

        otpSent.setAttempts(otpSent.getAttempts() + 1);

        if (!passwordEncoder.matches(otp, otpSent.getOtpHash())) {
            otpRepository.save(otpSent);
            throw new InvalidOtpException("Invalid OTP");
        }

        otpSent.setVerified(true);
        otpRepository.save(otpSent);

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setEnabled(true);
        userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getProfile().name());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(jwtService.getAccessTokenExpirationTime());

        return response;
    }
}
