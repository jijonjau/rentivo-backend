package com.rentivo.rental_saas.auth.dto;

import lombok.Data;

@Data
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String otpToken;
    private String passwordResetToken;
    private long expiresIn;
    private String tokenType = "Bearer";
}
