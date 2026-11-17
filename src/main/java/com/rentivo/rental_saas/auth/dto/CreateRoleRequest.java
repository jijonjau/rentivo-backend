package com.rentivo.rental_saas.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Data
public class CreateRoleRequest {
    @NotBlank
    private String roleName;
    @NotNull
    private Integer accountType;
    @NotEmpty
    private Set<Long> permissionIds;
}
