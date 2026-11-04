package com.rentivo.rental_saas.auth.service;

import com.rentivo.rental_saas.auth.dto.CreateRoleRequest;
import com.rentivo.rental_saas.auth.dto.FetchRoleResponse;
import com.rentivo.rental_saas.auth.entity.Permissions;
import com.rentivo.rental_saas.auth.entity.Roles;
import com.rentivo.rental_saas.auth.repository.PermissionsRepo;
import com.rentivo.rental_saas.auth.repository.RolesRepo;
import com.rentivo.rental_saas.common.exception.ResourceAlreadyExistsException;
import com.rentivo.rental_saas.common.exception.ResourceNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class RoleService {

    private final RolesRepo roleRepo;
    private final PermissionsRepo permissionsRepo;

    public void createRole(CreateRoleRequest request) {
        if (roleRepo.existsByRoleName(request.getRoleName())) {
            throw new ResourceAlreadyExistsException("Role already exists");
        }
        Roles role = new Roles();
        role.setRoleName(request.getRoleName());
        Set<Permissions> permissions = new HashSet<>(
                permissionsRepo.findAllById(request.getPermissionIds())
        );
        if (permissions.size() != request.getPermissionIds().size()) {
            throw new ResourceNotFoundException("One or more permissions do not exist");
        }
        role.setPermissions(permissions);
        role.setEnabled(true);
        roleRepo.save(role);
    }

    @Transactional(readOnly = true)
    public Page<FetchRoleResponse> fetchRoles(boolean enabled, Pageable pageable) {
        return roleRepo.findByEnabled(enabled, pageable)
                .map(role -> new FetchRoleResponse(
                        role.getId(),
                        role.getRoleName(),
                        role.getPermissions().stream()
                                .map(Permissions::getId)
                                .collect(Collectors.toSet()),
                        role.getCreatedAt()
                ));
    }

    @Transactional(readOnly = true)
    public FetchRoleResponse fetchRoleById(Long id) {
        Roles role = roleRepo.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Role not found")
        );

        return new FetchRoleResponse(
                role.getId(),
                role.getRoleName(),
                role.getPermissions().stream()
                        .map(Permissions::getId)
                        .collect(Collectors.toSet()),
                role.getCreatedAt()
        );
    }

    public void updateRole(Long id, CreateRoleRequest request) {
        Roles role = roleRepo.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Role not found")
        );
        if (!role.getRoleName().equals(request.getRoleName()) && roleRepo.existsByRoleName(request.getRoleName())) {
            throw new ResourceAlreadyExistsException("Role already exists");
        }
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

    public void updateRoleStatus(Long id, boolean enabled) {
        Roles role = roleRepo.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Role not found")
        );
        if ("SUPER_ADMIN".equals(role.getRoleName()) && !enabled) {
            throw new IllegalArgumentException("Cannot disable role super_admin");
        }
        role.setEnabled(enabled);
        roleRepo.save(role);
    }
}
