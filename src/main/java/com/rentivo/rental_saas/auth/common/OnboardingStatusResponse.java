package com.rentivo.rental_saas.auth.common;

public record OnboardingStatusResponse(
        OnboardingStatus status,
        boolean agencyCreated,
        boolean propertyAdded,
        OnboardingStep nextStep
) {
}