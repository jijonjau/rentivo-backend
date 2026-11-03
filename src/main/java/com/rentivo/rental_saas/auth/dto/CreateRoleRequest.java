package com.rentivo.rental_saas.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Set;

@Data
public class CreateRoleRequest {
    @NotBlank
    private String roleName;
    @NotBlank
    private Set<Long> permissionIds;
}
