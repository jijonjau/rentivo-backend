package com.rentivo.rental_saas.auth.controller;

import com.rentivo.rental_saas.auth.dto.CreateUserRequest;
import com.rentivo.rental_saas.auth.dto.LoginRequest;
import com.rentivo.rental_saas.auth.dto.VerifyOtpRequest;
import com.rentivo.rental_saas.auth.entity.User;
import com.rentivo.rental_saas.auth.service.AuthService;
import com.rentivo.rental_saas.auth.service.OtpService;
import com.rentivo.rental_saas.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final OtpService otpService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(ApiResponse.success(
                200, "Login successful. OTP sent for verification.", authService.login(loginRequest)
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody CreateUserRequest createUserRequest) {
        System.out.println("REGISTER ENDPOINT HIT");
        return ResponseEntity.ok(ApiResponse.success(201, "Registration successful. OTP sent for verification.", authService.registerUser(createUserRequest)));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpRequest verifyOtpRequest, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        assert user != null;
        return ResponseEntity.ok(
                ApiResponse.success(200, "OTP verified successfully", otpService.verifyOtp(user.getId(), verifyOtpRequest.getOtp()))
        );
    }
}
