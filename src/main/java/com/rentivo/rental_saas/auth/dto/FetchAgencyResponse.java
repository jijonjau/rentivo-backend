package com.rentivo.rental_saas.auth.dto;

import com.rentivo.rental_saas.auth.common.AgencyVerificationStatus;

import java.time.LocalDateTime;

public record FetchAgencyResponse(
        Long id,
        String agencyName,
        String registrationNumber,
        String email,
        String phoneNumber,
        String physicalAddress,
        AgencyVerificationStatus verificationStatus,
        String status,
        boolean hasRegistrationDocument,
        LocalDateTime createdAt
) {}