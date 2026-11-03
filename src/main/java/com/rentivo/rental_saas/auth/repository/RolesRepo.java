package com.rentivo.rental_saas.auth.repository;

import com.rentivo.rental_saas.auth.entity.Roles;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolesRepo extends JpaRepository<Roles, Long> {
    Optional<Roles> findByRoleName(String name);

    boolean existsByRoleName(String name);

    Page<Roles> findByEnabled(boolean enabled, Pageable pageable);
}
