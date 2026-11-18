package com.rentivo.rental_saas.auth.dto;

import com.rentivo.rental_saas.auth.common.AccountType;

import java.time.LocalDateTime;
import java.util.Set;

public record FetchRoleResponse(
        Long id,
        String roleName,
        AccountType accountType,
        Set<Long> permissionIds,
        boolean enabled,
        boolean systemRole,
        Long ownerId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
