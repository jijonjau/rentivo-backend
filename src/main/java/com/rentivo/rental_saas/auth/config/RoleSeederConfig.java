package com.rentivo.rental_saas.auth.config;

import com.rentivo.rental_saas.auth.entity.Roles;
import com.rentivo.rental_saas.auth.repository.RolesRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class RoleSeederConfig {

    @Bean
    CommandLineRunner seedRoles(RolesRepo repo) {
        return args -> {
            List<String> roles = List.of(
                    "SUPER_ADMIN",
                    "TENANT",
                    "LANDLORD",
                    "AGENCY_APPLICANT",
                    "AGENCY_ADMIN",
                    "AGENCY_AGENT",
                    "AGENCY_ACCOUNTANT"
            );

            for (String name : roles) {
                if (!repo.existsByRoleName(name)) {
                    Roles role = new Roles();
                    role.setRoleName(name);
                    role.setEnabled(true);

                    repo.save(role);
                }
            }
        };
    }
}