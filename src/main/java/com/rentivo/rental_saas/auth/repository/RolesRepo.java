package com.rentivo.rental_saas.auth.repository;

import com.rentivo.rental_saas.auth.common.AccountType;
import com.rentivo.rental_saas.auth.entity.Roles;
import com.rentivo.rental_saas.auth.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RolesRepo extends JpaRepository<Roles, Long> {
    Optional<Roles> findByRoleName(String name);

    Optional<Roles> findByRoleNameAndAccountTypeAndSystemRole(
            String roleName,
            AccountType accountType,
            boolean systemRole
    );

    boolean existsByRoleNameAndAccountTypeAndSystemRole(
            String roleName,
            AccountType accountType,
            boolean systemRole
    );

    boolean existsByRoleNameAndAccountTypeAndOwner(
            String roleName,
            AccountType accountType,
            Users owner
    );

    @Query("""
            SELECT r FROM Roles r
            WHERE r.enabled = :enabled
            AND r.accountType = :accountType
            AND (
                r.systemRole = true
                OR r.owner = :owner
            )
            """)
    Page<Roles> findVisibleRoles(
            @Param("enabled") boolean enabled,
            @Param("accountType") AccountType accountType,
            @Param("owner") Users owner,
            Pageable pageable
    );

    @Query("""
            SELECT r FROM Roles r
            WHERE (:enabled IS NULL OR r.enabled = :enabled)
            AND (:accountType IS NULL OR r.accountType = :accountType)
            """)
    Page<Roles> findAllWithFilters(
            @Param("enabled") Boolean enabled,
            @Param("accountType") AccountType accountType,
            Pageable pageable
    );
}
