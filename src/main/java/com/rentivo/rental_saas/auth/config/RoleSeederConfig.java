package com.rentivo.rental_saas.auth.config;

import com.rentivo.rental_saas.auth.common.AccountType;
import com.rentivo.rental_saas.auth.common.EntityStatus;
import com.rentivo.rental_saas.auth.entity.Permissions;
import com.rentivo.rental_saas.auth.entity.Roles;
import com.rentivo.rental_saas.auth.repository.PermissionsRepo;
import com.rentivo.rental_saas.auth.repository.RolesRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class RoleSeederConfig {

    @Value("${rentivo.seeder.enabled}")
    private boolean seedEnabled;

    @Bean
    @Order(1)
    CommandLineRunner seedSystemRoles(
            RolesRepo roleRepo,
            PermissionsRepo permissionsRepo
    ) {
        return args -> {
            if (!seedEnabled) return;

            List<Permissions> permissions = permissionsRepo.findAll();

            Map<String, List<String>> rolePermissions = Map.of(
                    "SUPER_ADMIN", permissions.stream()
                            .map(Permissions::getPermissionName)
                            .toList(),

                    "TENANT", List.of(
                            "PAYMENT_READ"
                    ),

                    "LANDLORD", List.of(
                            "PROPERTY_READ",
                            "PROPERTY_CREATE",
                            "PROPERTY_UPDATE",
                            "PAYMENT_READ",
                            "ISSUE_MANAGE"
                    ),

                    "AGENCY_ADMIN", List.of(
                            "USER_READ",
                            "USER_CREATE",
                            "USER_UPDATE",
                            "PROPERTY_READ",
                            "PROPERTY_CREATE",
                            "PROPERTY_UPDATE",
                            "PROPERTY_ENABLE_DISABLE",
                            "PAYMENT_READ",
                            "ISSUE_MANAGE",
                            "ROLE_READ"
                    ),

                    "AGENCY_USER", List.of(
                            "PROPERTY_READ"
                    )
            );

            Map<String, AccountType> accountTypes = Map.of(
                    "SUPER_ADMIN", AccountType.PLATFORM,
                    "TENANT", AccountType.TENANT,
                    "LANDLORD", AccountType.LANDLORD,
                    "AGENCY_ADMIN", AccountType.AGENCY,
                    "AGENCY_USER", AccountType.AGENCY
            );

            rolePermissions.forEach((roleName, permissionNames) -> {

                Roles role = roleRepo
                        .findByRoleNameAndAccountTypeAndSystemRole(
                                roleName,
                                accountTypes.get(roleName),
                                true
                        )
                        .orElseGet(() -> {
                            Roles newRole = new Roles();
                            newRole.setRoleName(roleName);
                            newRole.setAccountType(accountTypes.get(roleName));
                            newRole.setSystemRole(true);
                            newRole.setOwner(null);
                            newRole.setStatus(EntityStatus.ACTIVE);
                            return newRole;
                        });

                Set<Permissions> assignedPermissions = permissions.stream()
                        .filter(permission ->
                                permissionNames.contains(
                                        permission.getPermissionName()
                                )
                        )
                        .collect(Collectors.toSet());

                role.setPermissions(assignedPermissions);

                roleRepo.save(role);
            });
        };
    }
}
