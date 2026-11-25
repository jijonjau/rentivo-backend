package com.rentivo.rental_saas.auth.service;

import com.rentivo.rental_saas.auth.common.AccountType;
import com.rentivo.rental_saas.auth.common.OnboardingStatus;
import com.rentivo.rental_saas.auth.common.OnboardingStatusResponse;
import com.rentivo.rental_saas.auth.common.OnboardingStep;
import com.rentivo.rental_saas.auth.dto.FetchUserResponse;
import com.rentivo.rental_saas.auth.entity.Agencies;
import com.rentivo.rental_saas.auth.entity.Permissions;
import com.rentivo.rental_saas.auth.entity.Users;
import com.rentivo.rental_saas.auth.repository.AgencyRepo;
import com.rentivo.rental_saas.auth.repository.PropertyRepo;
import com.rentivo.rental_saas.auth.repository.UserRepo;
import com.rentivo.rental_saas.common.exception.ResourceNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserManagementService {

    private final UserRepo userRepo;
    private final AgencyRepo agencyRepo;
    private final PropertyRepo propertyRepo;

    @Transactional(readOnly = true)
    public FetchUserResponse fetchCurrentUser(Long userId) {

        Users user = userRepo.findByIdWithRoleAndPermissions(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found")
                );
        OnboardingStatusResponse onboarding = getOnboardingStatus(user);
        return new FetchUserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getAccountType(),
                user.getRole().getRoleName(),
                onboarding,
                user.isVerified(),
                user.getStatus().name(),
                user.getRole().getPermissions()
                        .stream()
                        .map(Permissions::getPermissionName)
                        .collect(Collectors.toSet())
        );
    }

    private OnboardingStatusResponse getOnboardingStatus(Users user) {

        OnboardingStatus status = user.getOnboardingStatus();

        if (status == OnboardingStatus.PENDING_VERIFICATION
                || status == OnboardingStatus.PENDING_APPROVAL) {
            return new OnboardingStatusResponse(
                    status, false, false, OnboardingStep.COMPLETED
            );
        }

        if (user.getAccountType() == AccountType.TENANT) {
            return new OnboardingStatusResponse(
                    OnboardingStatus.COMPLETED, false, false,
                    OnboardingStep.COMPLETED
            );
        }

        boolean agencyCreated = false;
        boolean propertyAdded = false;

        if (user.getAccountType() == AccountType.AGENCY) {

            Optional<Agencies> agency =
                    agencyRepo.findByCreatedBy_Id(user.getId());

            agencyCreated = agency.isPresent();

            if (agencyCreated) {
                propertyAdded = propertyRepo.existsByAgency_Id(
                        agency.get().getId()
                );
            }

        } else if (user.getAccountType() == AccountType.LANDLORD) {
            propertyAdded = propertyRepo.existsByLandlordId(user.getId());
        }

        OnboardingStep nextStep = switch (user.getAccountType()) {
            case AGENCY -> !agencyCreated
                    ? OnboardingStep.CREATE_AGENCY
                    : !propertyAdded
                    ? OnboardingStep.ADD_PROPERTY
                    : OnboardingStep.COMPLETED;

            case LANDLORD -> propertyAdded
                    ? OnboardingStep.COMPLETED
                    : OnboardingStep.ADD_PROPERTY;

            default -> OnboardingStep.COMPLETED;
        };

        return new OnboardingStatusResponse(
                nextStep == OnboardingStep.COMPLETED
                        ? OnboardingStatus.COMPLETED
                        : OnboardingStatus.IN_PROGRESS,
                agencyCreated,
                propertyAdded,
                nextStep
        );
    }
}