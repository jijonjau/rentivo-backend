package com.rentivo.rental_saas.service;

import com.rentivo.rental_saas.auth.dto.AuthResponse;
import com.rentivo.rental_saas.auth.dto.LoginRequest;
import com.rentivo.rental_saas.auth.entity.Users;
import com.rentivo.rental_saas.auth.repository.UserRepo;
import com.rentivo.rental_saas.auth.security.JwtService;
import com.rentivo.rental_saas.auth.service.AuthService;
import com.rentivo.rental_saas.common.exception.AccountDisabledException;
import com.rentivo.rental_saas.common.exception.InvalidCredentialsException;
import com.rentivo.rental_saas.common.messaging.event.OtpRequestedEvent;
import com.rentivo.rental_saas.common.messaging.publisher.RabbitMQPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepo userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RabbitMQPublisher rabbitMQPublisher;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_shouldReturnOtpToken_whenCredentialsAreValid() {

        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("tenant@example.com");
        request.setPassword("Password123!");

        Users user = Users.builder()
                .id(1L)
                .email("tenant@example.com")
                .passwordHash("encodedPassword")
                .enabled(true)
                .verified(true)
                .build();

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                request.getPassword(), user.getPasswordHash()))
                .thenReturn(true);

        when(jwtService.generateOtpToken(1L, "LOGIN_OTP"))
                .thenReturn("test-otp-token");

        when(jwtService.getOtpTokenExpirationTime())
                .thenReturn(300000L);

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals("test-otp-token", response.getOtpToken());
        assertEquals(300000L, response.getExpiresIn());

        verify(rabbitMQPublisher).publish(
                eq("login.otp"),
                any(OtpRequestedEvent.class)
        );
    }


    @Test
    void login_shouldThrowException_whenEmailDoesNotExist() {

        LoginRequest request = new LoginRequest();
        request.setEmail("unknown@example.com");
        request.setPassword("Password123!");

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );

        verifyNoInteractions(passwordEncoder, rabbitMQPublisher, jwtService);
    }

    @Test
    void login_shouldThrowException_whenPasswordIsIncorrect() {

        LoginRequest request = new LoginRequest();
        request.setEmail("tenant@example.com");
        request.setPassword("WrongPassword!");

        Users user = Users.builder()
                .id(1L)
                .email("tenant@example.com")
                .passwordHash("encodedPassword")
                .build();

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                request.getPassword(), user.getPasswordHash()))
                .thenReturn(false);

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );

        verifyNoInteractions(rabbitMQPublisher, jwtService);
    }

    @Test
    void login_shouldThrowException_whenAccountIsDisabledOrUnverified() {

        LoginRequest request = new LoginRequest();
        request.setEmail("tenant@example.com");
        request.setPassword("Password123!");

        Users user = Users.builder()
                .id(1L)
                .email("tenant@example.com")
                .passwordHash("encodedPassword")
                .enabled(false)
                .verified(true)
                .build();

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                request.getPassword(), user.getPasswordHash()))
                .thenReturn(true);

        assertThrows(
                AccountDisabledException.class,
                () -> authService.login(request)
        );

        verifyNoInteractions(rabbitMQPublisher, jwtService);
    }


    @Test
    void login_shouldThrowException_whenAccountIsUnverified() {

        LoginRequest request = new LoginRequest();
        request.setEmail("tenant@example.com");
        request.setPassword("Password123!");

        Users user = Users.builder()
                .id(1L)
                .email("tenant@example.com")
                .passwordHash("encodedPassword")
                .enabled(true)
                .verified(false)
                .build();

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                request.getPassword(), user.getPasswordHash()))
                .thenReturn(true);

        assertThrows(
                AccountDisabledException.class,
                () -> authService.login(request)
        );

        verifyNoInteractions(rabbitMQPublisher, jwtService);
    }

}
