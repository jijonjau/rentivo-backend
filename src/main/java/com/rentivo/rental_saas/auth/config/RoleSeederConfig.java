package com.rentivo.rental_saas.auth.config;

import com.rentivo.rental_saas.auth.common.AccountType;
import com.rentivo.rental_saas.auth.entity.Permissions;
import com.rentivo.rental_saas.auth.entity.Roles;
import com.rentivo.rental_saas.auth.repository.PermissionsRepo;
import com.rentivo.rental_saas.auth.repository.RolesRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.HashSet;
import java.util.List;

@Configuration
public class RoleSeederConfig {

    @Bean
    @Order(1)
    CommandLineRunner seedSuperAdminRole(
            RolesRepo roleRepo,
            PermissionsRepo permissionsRepo
    ) {
        return args -> {

            List<Permissions> permissions = permissionsRepo.findAll();

            Roles role = roleRepo
                    .findByRoleNameAndAccountTypeAndSystemRole(
                            "SUPER_ADMIN",
                            AccountType.PLATFORM,
                            true
                    )
                    .orElseGet(() -> {
                        Roles newRole = new Roles();
                        newRole.setRoleName("SUPER_ADMIN");
                        newRole.setAccountType(AccountType.PLATFORM);
                        newRole.setSystemRole(true);
                        newRole.setOwner(null);
                        newRole.setEnabled(true);
                        return newRole;
                    });

            role.setPermissions(new HashSet<>(permissions));

            roleRepo.save(role);
        };
    }
}
