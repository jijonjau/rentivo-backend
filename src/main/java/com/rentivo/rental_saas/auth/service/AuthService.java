package com.rentivo.rental_saas.auth.service;

import com.rentivo.rental_saas.auth.dto.CreateUserRequest;
import com.rentivo.rental_saas.auth.dto.LoginRequest;
import com.rentivo.rental_saas.auth.dto.AuthResponse;
import com.rentivo.rental_saas.auth.entity.User;
import com.rentivo.rental_saas.auth.mappers.UserMapper;
import com.rentivo.rental_saas.auth.repository.UserRepository;
import com.rentivo.rental_saas.common.exception.AccountDisabledException;
import com.rentivo.rental_saas.common.exception.EmailAlreadyExistsException;
import com.rentivo.rental_saas.common.exception.InvalidCredentialsException;
import com.rentivo.rental_saas.common.messaging.event.OtpEventType;
import com.rentivo.rental_saas.common.messaging.event.OtpRequestedEvent;
import com.rentivo.rental_saas.common.messaging.publisher.RabbitMQPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final RabbitMQPublisher rabbitMQPublisher;

    public AuthResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
        if (!user.isEnabled()) {
            throw new AccountDisabledException("Account is disabled");
        }

        String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getProfile().name());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        // publish request otp event
        rabbitMQPublisher.publish(
                "login.otp",
                new OtpRequestedEvent(
                        user.getId(),
                        user.getEmail(),
                        OtpEventType.LOGIN
                )
        );

        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(jwtService.getAccessTokenExpirationTime());

        return response;
    }

    public AuthResponse registerUser(CreateUserRequest createUserRequest) {
        if (userRepository.existsByEmail(createUserRequest.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        User user = userMapper.toEntity(createUserRequest);
        user.setPasswordHash(passwordEncoder.encode(createUserRequest.getPassword()));
        User savedUser = userRepository.save(user);

        // publish request otp event
        rabbitMQPublisher.publish(
                "register.otp",
                new OtpRequestedEvent(
                        savedUser.getId(),
                        savedUser.getEmail(),
                        OtpEventType.REGISTER
                )
        );

        String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getProfile().name());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());
        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(jwtService.getAccessTokenExpirationTime());

        return response;
    }
}
