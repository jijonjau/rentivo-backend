package com.rentivo.rental_saas.auth.repository;

import com.rentivo.rental_saas.auth.entity.Permissions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermissionsRepo extends JpaRepository<Permissions, Long> {
    Optional<Permissions> findByPermissionName(String name);
    boolean existsByPermissionName(String name);
}
