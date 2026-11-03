package com.rentivo.rental_saas.auth.controller;

import com.rentivo.rental_saas.auth.dto.*;
import com.rentivo.rental_saas.auth.entity.Users;
import com.rentivo.rental_saas.auth.service.AuthService;
import com.rentivo.rental_saas.auth.service.OtpService;
import com.rentivo.rental_saas.auth.service.RoleService;
import com.rentivo.rental_saas.common.messaging.event.OtpEventType;
import com.rentivo.rental_saas.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final OtpService otpService;
    private final RoleService roleService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(ApiResponse.success(
                200, "Login successful. OTP sent for verification.", authService.login(loginRequest)
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody CreateUserRequest createUserRequest) {
        return ResponseEntity.ok(ApiResponse.success(201, "Registration successful. OTP sent for verification.", authService.registerUser(createUserRequest)));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest verifyOtpRequest,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();

        OtpEventType eventType = OtpEventType.valueOf(
                authentication.getAuthorities()
                        .iterator()
                        .next()
                        .getAuthority()
                        .replace("_OTP", "")
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        "OTP verified successfully",
                        otpService.verifyOtp(
                                userId,
                                verifyOtpRequest.getOtp(),
                                eventType
                        )
                )
        );
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        String authority = authentication.getAuthorities()
                .iterator()
                .next()
                .getAuthority();

        OtpEventType eventType = OtpEventType.valueOf(
                authority.replace("_OTP", "")
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        "OTP resent successfully",
                        authService.resendOtp(userId, eventType)
                )
        );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        AuthResponse response = authService.forgotPassword(request.getEmail());

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        "If an account exists with that email, a password reset OTP will be sent.",
                        response
                )
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();

        authService.resetPassword(userId, request.getNewPassword());

        return ResponseEntity.ok(
                ApiResponse.success(200, "Password reset successfully", null)
        );
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication
    ) {
        Users user = (Users) authentication.getPrincipal();

        authService.changePassword(
                user.getId(),
                request.getCurrentPassword(),
                request.getNewPassword()
        );

        return ResponseEntity.ok(
                ApiResponse.success(200, "Password changed successfully", null)
        );
    }

    @PostMapping("/role")
    public ResponseEntity<?> createRole(@Valid @RequestBody CreateRoleRequest createRoleRequest) {
        roleService.createRole(createRoleRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(201, "Role created successfully", null)
        );
    }

    @GetMapping("/roles")
    public ResponseEntity<?> getRoles(
            @RequestParam(defaultValue = "false") boolean enabled,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageRequest pageRequest = PageRequest.of(pageNumber, size);
        return ResponseEntity.ok(ApiResponse.success(
                200, "Roles fetched successfully", roleService.fetchRoles(enabled, pageRequest)
        ));
    }

    @GetMapping("/roles/{id}")
    public ResponseEntity<?> getRoleById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                200, "Role fetched successfully", roleService.fetchRoleById(id)
        ));
    }

    @PutMapping("/roles/{id}")
    public ResponseEntity<?> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody CreateRoleRequest createRoleRequest
    ) {
        roleService.updateRole(id, createRoleRequest);
        return ResponseEntity.ok(ApiResponse.success(
                200, "Role updated successfully", null)
        );
    }

    @PatchMapping("/roles/{id}/status")
    public ResponseEntity<?> disableRole(
            @PathVariable Long id,
            @RequestParam boolean enabled
    ) {
        roleService.updateRoleStatus(id, enabled);
        return ResponseEntity.ok(ApiResponse.success(
                200, "Role disabled successfully", null
        ));
    }
}
