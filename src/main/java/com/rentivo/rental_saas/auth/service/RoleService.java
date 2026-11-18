package com.rentivo.rental_saas.auth.service;

import com.rentivo.rental_saas.auth.common.AccountType;
import com.rentivo.rental_saas.auth.dto.CreateRoleRequest;
import com.rentivo.rental_saas.auth.dto.FetchReferenceData;
import com.rentivo.rental_saas.auth.dto.FetchRoleResponse;
import com.rentivo.rental_saas.auth.entity.Permissions;
import com.rentivo.rental_saas.auth.entity.Roles;
import com.rentivo.rental_saas.auth.entity.Users;
import com.rentivo.rental_saas.auth.repository.PermissionsRepo;
import com.rentivo.rental_saas.auth.repository.RolesRepo;
import com.rentivo.rental_saas.common.exception.ResourceAlreadyExistsException;
import com.rentivo.rental_saas.common.exception.ResourceNotFoundException;
import com.rentivo.rental_saas.common.response.PageResponseDto;
import com.rentivo.rental_saas.common.response.PageResponseMapper;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class RoleService {

    private final RolesRepo roleRepo;
    private final PermissionsRepo permissionsRepo;

    @Transactional
    public void createRole(CreateRoleRequest request, Users user) {

        AccountType accountType = AccountType.fromCode(request.getAccountType());

        boolean isSuperAdmin = "SUPER_ADMIN".equals(
                user.getRole().getRoleName()
        );

        // Non-super-admins can only create roles for their account type
        if (!isSuperAdmin && user.getAccountType() != accountType) {
            throw new AccessDeniedException("You cannot create roles for this account type");
        }

        if (isSuperAdmin) {
            if (roleRepo.existsByRoleNameAndAccountTypeAndSystemRole(
                    request.getRoleName(), accountType, true)) {
                throw new ResourceAlreadyExistsException("Role already exists");
            }
        } else {
            if (roleRepo.existsByRoleNameAndAccountTypeAndOwner(
                    request.getRoleName(), accountType, user)) {
                throw new ResourceAlreadyExistsException("Role already exists");
            }
        }

        Set<Permissions> permissions = new HashSet<>(
                permissionsRepo.findAllById(request.getPermissionIds())
        );

        if (permissions.size() != request.getPermissionIds().size()) {
            throw new ResourceNotFoundException("One or more permissions do not exist");
        }

        Roles role = new Roles();
        role.setRoleName(request.getRoleName());
        role.setAccountType(accountType);
        role.setPermissions(permissions);
        role.setEnabled(true);
        role.setSystemRole(isSuperAdmin);

        // Only custom roles have an owner
        if (!isSuperAdmin) {
            role.setOwner(user);
        }

        roleRepo.save(role);
    }

    @Transactional(readOnly = true)
    public PageResponseDto<FetchRoleResponse> fetchRoles(
            Boolean enabled,
            AccountType accountType,
            Pageable pageable,
            Users user
    ) {
        boolean isSuperAdmin = "SUPER_ADMIN".equals(
                user.getRole().getRoleName()
        );

        Page<Roles> roles;

        if (isSuperAdmin) {
            roles = roleRepo.findAllWithFilters(enabled, accountType, pageable);
        } else {
            AccountType userAccountType = user.getAccountType();

            if (userAccountType != AccountType.AGENCY &&
                    userAccountType != AccountType.LANDLORD) {
                throw new AccessDeniedException("You cannot access roles");
            }

            if (accountType != null && accountType != userAccountType) {
                throw new AccessDeniedException("You cannot access roles for this account type");
            }

            roles = roleRepo.findVisibleRoles(
                    enabled != null ? enabled : true,
                    userAccountType,
                    user,
                    pageable
            );
        }

        Page<FetchRoleResponse> mappedRoles = roles.map(role ->
                new FetchRoleResponse(
                        role.getId(),
                        role.getRoleName(),
                        role.getAccountType(),
                        role.getPermissions().stream()
                                .map(Permissions::getId)
                                .collect(Collectors.toSet()),
                        role.isEnabled(),
                        role.isSystemRole(),
                        role.getOwner() != null ? role.getOwner().getId() : null,
                        role.getCreatedAt(),
                        role.getUpdatedAt()
                )
        );

        return PageResponseMapper.toPageResponse(mappedRoles);
    }

    @Transactional(readOnly = true)
    public FetchRoleResponse fetchRoleById(Long id, Users user) {

        Roles role = roleRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        boolean isSuperAdmin = "SUPER_ADMIN".equals(
                user.getRole().getRoleName()
        );

        if (!isSuperAdmin) {

            AccountType userAccountType = user.getAccountType();

            if (userAccountType != AccountType.AGENCY &&
                    userAccountType != AccountType.LANDLORD) {
                throw new AccessDeniedException("You cannot access roles");
            }

            boolean isVisible = role.getAccountType() == userAccountType &&
                    (role.isSystemRole() || user.equals(role.getOwner()));

            if (!isVisible) {
                throw new AccessDeniedException("You cannot access this role");
            }
        }

        return new FetchRoleResponse(
                role.getId(),
                role.getRoleName(),
                role.getAccountType(),
                role.getPermissions().stream()
                        .map(Permissions::getId)
                        .collect(Collectors.toSet()),
                role.isEnabled(),
                role.isSystemRole(),
                role.getOwner() != null ? role.getOwner().getId() : null,
                role.getCreatedAt(),
                role.getUpdatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<FetchReferenceData> fetchPermissions() {
        return permissionsRepo.findAll()
                .stream()
                .map(permission -> new FetchReferenceData(
                        permission.getId(),
                        permission.getPermissionName()
                ))
                .toList();
    }

    @Transactional
    public void updateRole(Long id, CreateRoleRequest request, Users user) {

        Roles role = roleRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        boolean isSuperAdmin = validateRoleAccess(user, role);

        // Duplicate name validation
        boolean duplicate = isSuperAdmin
                ? roleRepo.existsByRoleNameAndAccountTypeAndSystemRole(
                request.getRoleName(), role.getAccountType(), true)
                : roleRepo.existsByRoleNameAndAccountTypeAndOwner(
                request.getRoleName(), role.getAccountType(), user);

        if (!role.getRoleName().equals(request.getRoleName()) && duplicate) {
            throw new ResourceAlreadyExistsException("Role already exists");
        }

        // Validate permissions
        Set<Permissions> permissions = new HashSet<>(
                permissionsRepo.findAllById(request.getPermissionIds())
        );

        if (permissions.size() != request.getPermissionIds().size()) {
            throw new ResourceNotFoundException("One or more permissions do not exist");
        }

        role.setRoleName(request.getRoleName());
        role.setPermissions(permissions);

        roleRepo.save(role);
    }

    @Transactional
    public void updateRoleStatus(Long id, boolean enabled, Users user) {

        Roles role = roleRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        validateRoleAccess(user, role);

        role.setEnabled(enabled);
        roleRepo.save(role);
    }

    // Helper methods
    private boolean validateRoleAccess(Users user, Roles role) {

        boolean isSuperAdmin = "SUPER_ADMIN".equals(
                user.getRole().getRoleName()
        );

        // Protect Super Admin role
        if ("SUPER_ADMIN".equals(role.getRoleName())) {
            throw new AccessDeniedException("Super Admin role cannot be modified");
        }

        if (isSuperAdmin) {
            if (!role.isSystemRole()) {
                throw new AccessDeniedException("You cannot modify custom roles");
            }
        } else {
            if (role.isSystemRole()
                    || !Objects.equals(role.getOwner().getId(), user.getId())) {
                throw new AccessDeniedException("You cannot modify this role");
            }
        }

        return isSuperAdmin;
    }
}
