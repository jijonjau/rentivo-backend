package com.rentivo.rental_saas.auth.config;

import com.rentivo.rental_saas.auth.entity.Permissions;
import com.rentivo.rental_saas.auth.repository.PermissionsRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class PermissionSeederConfig {
    @Bean
    CommandLineRunner seedPermissions(PermissionsRepo repo) {
        return args -> {
            List<String> permissions = List.of(
                    "USER_READ",
                    "USER_CREATE",
                    "USER_UPDATE",
                    "PROPERTY_READ",
                    "PROPERTY_CREATE",
                    "PAYMENT_READ",
                    "ISSUE_MANAGE"
            );

            for (String name : permissions) {
                if (!repo.existsByPermissionName(name)) {
                    Permissions permission = new Permissions();
                    permission.setPermissionName(name);
                    repo.save(permission);
                }
            }
        };
    }
}
