package com.rentivo.rental_saas.auth.config;

import com.rentivo.rental_saas.auth.common.AccountType;
import com.rentivo.rental_saas.auth.common.OnboardingStatus;
import com.rentivo.rental_saas.auth.entity.Roles;
import com.rentivo.rental_saas.auth.entity.Users;
import com.rentivo.rental_saas.auth.repository.RolesRepo;
import com.rentivo.rental_saas.auth.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class AdminSeederConfig {

    private final UserRepo userRepository;
    private final RolesRepo rolesRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${rentivo.admin.first-name:}")
    private String firstName;

    @Value("${rentivo.admin.last-name:}")
    private String lastName;

    @Value("${rentivo.admin.email:}")
    private String email;

    @Value("${rentivo.admin.password:}")
    private String password;

    @Bean
    CommandLineRunner seedSuperAdmin() {
        return args -> {

            // Skip seeding if no bootstrap credentials are configured.
            if (firstName.isBlank() && lastName.isBlank()
                    && email.isBlank() && password.isBlank()) {
                return;
            }

            // Prevent partially configured admin credentials.
            if (firstName.isBlank() || lastName.isBlank()
                    || email.isBlank() || password.isBlank()) {
                throw new IllegalStateException(
                        "Incomplete super admin configuration"
                );
            }

            Optional<Users> existingUser = userRepository.findByEmail(email);

            if (existingUser.isPresent()) {
                Users user = existingUser.get();

                if (user.getRole() == null
                        || !"SUPER_ADMIN".equals(user.getRole().getRoleName())) {
                    throw new IllegalStateException(
                            "Admin email already belongs to another account"
                    );
                }

                return;
            }

            Roles superAdminRole = rolesRepository.findByRoleName("SUPER_ADMIN")
                    .orElseThrow(() -> new IllegalStateException(
                            "SUPER_ADMIN role not found. Run RoleSeederConfig first."
                    ));

            Users admin = new Users();
            admin.setFirstName(firstName);
            admin.setLastName(lastName);
            admin.setEmail(email);
            admin.setPasswordHash(passwordEncoder.encode(password));
            admin.setAccountType(AccountType.PLATFORM);
            admin.setRole(superAdminRole);
            admin.setEnabled(true);
            admin.setVerified(true);
            admin.setOnboardingStatus(OnboardingStatus.COMPLETED);

            userRepository.save(admin);
        };
    }
}