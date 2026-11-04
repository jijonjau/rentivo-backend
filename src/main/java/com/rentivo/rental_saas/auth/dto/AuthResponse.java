package com.rentivo.rental_saas.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class AuthResponse {

    @JsonIgnore
    private String accessToken;

    @JsonIgnore
    private String refreshToken;

    private String otpToken;
    private String passwordResetToken;
    private long expiresIn;
    private String tokenType = "Bearer";
}
