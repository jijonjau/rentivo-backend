package com.rentivo.rental_saas.auth.service;

import com.rentivo.rental_saas.auth.common.OnboardingStatus;
import com.rentivo.rental_saas.auth.dto.AuthResponse;
import com.rentivo.rental_saas.auth.dto.CreateUserRequest;
import com.rentivo.rental_saas.auth.dto.LoginRequest;
import com.rentivo.rental_saas.auth.entity.PasswordHistory;
import com.rentivo.rental_saas.auth.entity.Roles;
import com.rentivo.rental_saas.auth.entity.Users;
import com.rentivo.rental_saas.auth.repository.PasswordHistoryRepo;
import com.rentivo.rental_saas.auth.repository.RolesRepo;
import com.rentivo.rental_saas.auth.repository.UserRepo;
import com.rentivo.rental_saas.auth.security.JwtService;
import com.rentivo.rental_saas.common.exception.*;
import com.rentivo.rental_saas.common.messaging.event.OtpEventType;
import com.rentivo.rental_saas.common.messaging.event.OtpRequestedEvent;
import com.rentivo.rental_saas.common.messaging.publisher.RabbitMQPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepo userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RolesRepo roleRepo;
    private final RabbitMQPublisher rabbitMQPublisher;
    private final PasswordHistoryRepo passwordHistoryRepo;
    private final RefreshTokenService refreshTokenService;

    public AuthResponse login(LoginRequest loginRequest) {

        Users user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (!user.isEnabled() || !user.isVerified()) {
            throw new AccountDisabledException("Account is unavailable");
        }

        rabbitMQPublisher.publish(
                "login.otp",
                new OtpRequestedEvent(
                        user.getId(),
                        user.getEmail(),
                        OtpEventType.LOGIN
                )
        );

        AuthResponse response = new AuthResponse();
        response.setOtpToken(
                jwtService.generateOtpToken(user.getId(), "LOGIN_OTP")
        );
        response.setExpiresIn(jwtService.getOtpTokenExpirationTime());

        return response;
    }

    public AuthResponse registerUser(CreateUserRequest createUserRequest) {
        if (userRepository.existsByEmail(createUserRequest.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already exists");
        }
        String roleName = switch (createUserRequest.getAccountType()) {
            case TENANT -> "TENANT";
            case LANDLORD -> "LANDLORD";
            case AGENCY -> "AGENCY_APPLICANT";
            case PLATFORM -> throw new IllegalArgumentException(
                    "Invalid account type"
            );
        };

        Roles role = roleRepo.findByRoleName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        Users user = new Users();
        user.setFirstName(createUserRequest.getFirstName());
        user.setLastName(createUserRequest.getLastName());
        user.setEmail(createUserRequest.getEmail());
        user.setPhoneNumber(createUserRequest.getPhoneNumber());
        user.setAccountType(createUserRequest.getAccountType());
        user.setRole(role);
        user.setPasswordHash(passwordEncoder.encode(createUserRequest.getPassword()));
        user.setVerified(false);
        user.setEnabled(false);
        user.setOnboardingStatus(OnboardingStatus.PENDING_VERIFICATION);
        Users savedUser = userRepository.save(user);

        // publish request otp event
        rabbitMQPublisher.publish(
                "register.otp",
                new OtpRequestedEvent(
                        savedUser.getId(),
                        savedUser.getEmail(),
                        OtpEventType.REGISTER
                )
        );

        String otpToken = jwtService.generateOtpToken(savedUser.getId(), "REGISTER_OTP");

        AuthResponse response = new AuthResponse();
        response.setOtpToken(otpToken);
        response.setExpiresIn(jwtService.getOtpTokenExpirationTime());

        return response;
    }

    public AuthResponse forgotPassword(String email) {

        AuthResponse response = new AuthResponse();

        userRepository.findByEmail(email).ifPresent(user -> {

            if (!user.isEnabled() || !user.isVerified()) {
                return;
            }

            rabbitMQPublisher.publish(
                    "reset.password.otp",
                    new OtpRequestedEvent(
                            user.getId(),
                            user.getEmail(),
                            OtpEventType.PASSWORD_RESET
                    )
            );

            response.setOtpToken(
                    jwtService.generateOtpToken(
                            user.getId(),
                            "PASSWORD_RESET_OTP"
                    )
            );

            response.setExpiresIn(jwtService.getOtpTokenExpirationTime());
        });

        return response;
    }

    public AuthResponse resendOtp(Long userId, OtpEventType eventType) {

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String queue;

        switch (eventType) {
            case REGISTER -> {
                if (user.isVerified()) {
                    throw new InvalidOtpException("Account is already verified");
                }
                queue = "register.otp";
            }

            case LOGIN -> {
                if (!user.isEnabled() || !user.isVerified()) {
                    throw new AccountDisabledException("Account is unavailable");
                }
                queue = "login.otp";
            }

            case PASSWORD_RESET -> {
                if (!user.isEnabled() || !user.isVerified()) {
                    throw new AccountDisabledException("Account is unavailable");
                }
                queue = "reset.password.otp";
            }

            default -> throw new IllegalArgumentException("Unsupported OTP event type");
        }

        rabbitMQPublisher.publish(
                queue,
                new OtpRequestedEvent(user.getId(), user.getEmail(), eventType)
        );

        AuthResponse response = new AuthResponse();
        response.setOtpToken(
                jwtService.generateOtpToken(userId, eventType.name() + "_OTP")
        );
        response.setExpiresIn(jwtService.getOtpTokenExpirationTime());

        return response;
    }

    @Transactional
    public void resetPassword(Long userId, String newPassword) {

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        validatePasswordHistory(userId, newPassword, user.getPasswordHash());

        savePasswordHistory(userId, user.getPasswordHash());

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        refreshTokenService.revokeAllUserTokens(userId);
    }

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        validatePasswordHistory(userId, newPassword, user.getPasswordHash());

        savePasswordHistory(userId, user.getPasswordHash());

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        refreshTokenService.revokeAllUserTokens(userId);
    }

    // Password helper methods
    private void validatePasswordHistory(Long userId, String newPassword, String currentPasswordHash) {

        if (passwordEncoder.matches(newPassword, currentPasswordHash)) {
            throw new PasswordRecentlyUsedException("You cannot reuse your recent passwords");
        }

        List<PasswordHistory> history =
                passwordHistoryRepo.findTop4ByUserIdOrderByCreatedAtDesc(userId);

        boolean previouslyUsed = history.stream()
                .anyMatch(password -> passwordEncoder.matches(
                        newPassword, password.getPasswordHash()
                ));

        if (previouslyUsed) {
            throw new PasswordRecentlyUsedException("You cannot reuse your recent passwords");
        }
    }

    private void savePasswordHistory(Long userId, String currentPasswordHash) {

        passwordHistoryRepo.save(
                PasswordHistory.builder()
                        .userId(userId)
                        .passwordHash(currentPasswordHash)
                        .build()
        );
    }
}
