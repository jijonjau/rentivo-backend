package com.rentivo.rental_saas.auth.dto;

import java.time.LocalDateTime;
import java.util.Set;

public record FetchRoleResponse(
        Long id,
        String roleName,
        Set<Long>  permissionIds,
        LocalDateTime createdAt
) {}
