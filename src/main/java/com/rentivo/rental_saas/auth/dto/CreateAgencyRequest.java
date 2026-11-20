package com.rentivo.rental_saas.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAgencyRequest {

    @NotBlank
    private String agencyName;

    @NotBlank
    private String registrationNumber;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String phoneNumber;

    private String physicalAddress;
}