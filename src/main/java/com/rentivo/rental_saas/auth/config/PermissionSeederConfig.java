package com.rentivo.rental_saas.auth.config;

import com.rentivo.rental_saas.auth.entity.Permissions;
import com.rentivo.rental_saas.auth.repository.PermissionsRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.List;

@Configuration
public class PermissionSeederConfig {

    @Bean
    @Order(0)
    CommandLineRunner seedPermissions(PermissionsRepo repo) {
        return args -> {
            List<String> permissions = List.of(
                    "USER_READ",
                    "USER_CREATE",
                    "USER_UPDATE",
                    "USER_ENABLE_DISABLE",
                    "USER_ARCHIVE",
                    "PROPERTY_READ",
                    "PROPERTY_CREATE",
                    "PROPERTY_UPDATE",
                    "PROPERTY_ENABLE_DISABLE",
                    "ROLE_READ",
                    "ROLE_CREATE",
                    "ROLE_UPDATE",
                    "ROLE_ENABLE_DISABLE",
                    "AGENCY_READ",
                    "AGENCY_CREATE",
                    "AGENCY_ENABLE_DISABLE",
                    "PAYMENT_READ",
                    "ISSUE_MANAGE"
            );

            List<Permissions> newPermissions = permissions.stream()
                    .filter(name -> !repo.existsByPermissionName(name))
                    .map(name -> {
                        Permissions permission = new Permissions();
                        permission.setPermissionName(name);
                        return permission;
                    })
                    .toList();

            repo.saveAll(newPermissions);
        };
    }
}
