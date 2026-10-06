package com.rentivo.rental_saas.auth.dto;

import com.rentivo.rental_saas.auth.common.UserProfile;
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
    @NotNull
    private UserProfile profile;

    private String phoneNumber;
    private Long agencyId;
    private String agencyRole;
}
