package com.rentivo.rental_saas.auth.service;

import com.rentivo.rental_saas.auth.common.AccountType;
import com.rentivo.rental_saas.auth.common.OnboardingStatus;
import com.rentivo.rental_saas.auth.dto.AuthResponse;
import com.rentivo.rental_saas.auth.entity.Otps;
import com.rentivo.rental_saas.auth.entity.Users;
import com.rentivo.rental_saas.auth.repository.OtpRepo;
import com.rentivo.rental_saas.auth.repository.UserRepo;
import com.rentivo.rental_saas.auth.security.JwtService;
import com.rentivo.rental_saas.common.exception.AccountDisabledException;
import com.rentivo.rental_saas.common.exception.InvalidOtpException;
import com.rentivo.rental_saas.common.exception.ResourceNotFoundException;
import com.rentivo.rental_saas.common.messaging.event.OtpEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class OtpService {
    private final OtpRepo otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepo userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
//    private final NotificationService notificationService;

    @Value("${rentivo.otp.expiry}")
    private long otpExpiry;

    @Value("${rentivo.otp.max-attempts}")
    private int maxAttempts;

    public void generateOtp(Long userId, OtpEventType eventType) {
        String otpValue = String.valueOf(
                ThreadLocalRandom.current().nextInt(100000, 1000000)
        );

        Otps otp = Otps.builder()
                .userId(userId)
                .eventType(eventType)
                .otpHash(passwordEncoder.encode(otpValue))
                .expiresAt(LocalDateTime.now().plusSeconds(otpExpiry / 1000))
                .attempts(0)
                .verified(false)
                .build();

        otpRepository.save(otp);

        System.out.println("OTP: " + otpValue);

        //Publish a queue
        //        Users user = userRepository.findById(userId)
//                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

//        notificationService.sendOtpEmail(user.getEmail(), otpValue);
    }

    @Transactional(noRollbackFor = InvalidOtpException.class)
    public AuthResponse verifyOtp(
            Long userId,
            String otp,
            OtpEventType expectedEventType
    ) {
        Otps otpSent = otpRepository
                .findTopByUserIdAndEventTypeOrderByCreatedAtDesc(userId, expectedEventType)
                .orElseThrow(() -> new InvalidOtpException("Invalid OTP"));

        if (otpSent.isVerified()) {
            throw new InvalidOtpException("OTP already verified");
        }

        if (LocalDateTime.now().isAfter(otpSent.getExpiresAt())) {
            throw new InvalidOtpException("OTP has expired");
        }

        if (otpSent.getAttempts() >= maxAttempts) {
            throw new InvalidOtpException("Too many attempts");
        }

        otpSent.setAttempts(otpSent.getAttempts() + 1);

        if (!passwordEncoder.matches(otp, otpSent.getOtpHash())) {
            otpRepository.saveAndFlush(otpSent);
            throw new InvalidOtpException("Invalid OTP");
        }

        Users user = userRepository.findByIdWithRole(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        AuthResponse response = new AuthResponse();

        switch (expectedEventType) {

            case REGISTER -> {
                user.setVerified(true);
                user.setEnabled(true);
                user.setOnboardingStatus(
                        user.getAccountType() == AccountType.TENANT
                                ? OnboardingStatus.COMPLETED
                                : OnboardingStatus.IN_PROGRESS
                );

                String accessToken = jwtService.generateAccessToken(
                        user.getEmail(), user.getRole().getRoleName()
                );

                String refreshToken = refreshTokenService.issueRefreshToken(user);
//                        jwtService.generateRefreshToken(user.getEmail());

                response.setAccessToken(accessToken);
                response.setRefreshToken(refreshToken);
                response.setExpiresIn(jwtService.getAccessTokenExpirationTime());
            }

            case LOGIN -> {
                if (!user.isEnabled() || !user.isVerified()) {
                    throw new AccountDisabledException("Account is disabled or unverified");
                }

                String accessToken = jwtService.generateAccessToken(
                        user.getEmail(), user.getRole().getRoleName()
                );

                String refreshToken = refreshTokenService.issueRefreshToken(user);
//                        jwtService.generateRefreshToken(user.getEmail());

                response.setAccessToken(accessToken);
                response.setRefreshToken(refreshToken);
                response.setExpiresIn(jwtService.getAccessTokenExpirationTime());
            }

            case PASSWORD_RESET -> {
                String resetToken = jwtService.generatePasswordResetToken(userId);

                response.setPasswordResetToken(resetToken);
                response.setExpiresIn(jwtService.getPasswordResetTokenExpirationTime());
            }
        }

        otpSent.setVerified(true);
        otpRepository.save(otpSent);
        userRepository.save(user);

        return response;
    }
}
