package com.rentivo.rental_saas.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyOtpRequest {
    @NotBlank
    private String otp;
}
