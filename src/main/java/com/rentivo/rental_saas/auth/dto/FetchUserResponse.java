package com.rentivo.rental_saas.auth.dto;

import com.rentivo.rental_saas.auth.common.AccountType;
import com.rentivo.rental_saas.auth.common.OnboardingStatus;

import java.util.Set;

public record FetchUserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        AccountType accountType,
        String role,
        OnboardingStatus onboardingStatus,
        boolean verified,
        boolean enabled,
        Set<String> permissions
) {}