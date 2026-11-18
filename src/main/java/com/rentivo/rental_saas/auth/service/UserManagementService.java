package com.rentivo.rental_saas.auth.service;

import com.rentivo.rental_saas.auth.dto.FetchUserResponse;
import com.rentivo.rental_saas.auth.entity.Permissions;
import com.rentivo.rental_saas.auth.entity.Users;
import com.rentivo.rental_saas.auth.repository.UserRepo;
import com.rentivo.rental_saas.common.exception.ResourceNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserManagementService {

    private final UserRepo userRepo;

    @Transactional(readOnly = true)
    public FetchUserResponse fetchCurrentUser(Long userId) {

        Users user = userRepo.findByIdWithRoleAndPermissions(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found")
                );

        return new FetchUserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getAccountType(),
                user.getRole().getRoleName(),
                user.getOnboardingStatus(),
                user.isVerified(),
                user.isEnabled(),
                user.getRole().getPermissions()
                        .stream()
                        .map(Permissions::getPermissionName)
                        .collect(Collectors.toSet())
        );
    }
}