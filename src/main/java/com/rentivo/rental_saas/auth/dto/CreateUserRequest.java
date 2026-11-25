package com.rentivo.rental_saas.auth.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateUserRequest {
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String password;
    private String phoneNumber;
    @NotNull
    private Integer accountType;
    @AssertTrue(message = "You must accept the terms and conditions.")
    private boolean acceptTerms;
}
