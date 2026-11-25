package com.rentivo.rental_saas.auth.dto;

import com.rentivo.rental_saas.auth.common.AccountType;
import com.rentivo.rental_saas.auth.common.OnboardingStatusResponse;

import java.util.Set;

public record FetchUserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        AccountType accountType,
        String role,
        OnboardingStatusResponse onboardingStatus,
        boolean verified,
        String status,
        Set<String> permissions
) {}