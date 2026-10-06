package com.rentivo.rental_saas.auth.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponse {
    private String firstName;
    private String lastName;
    private String email;
    private boolean enabled;
    private LocalDateTime createdAt;
}
