package com.rentivo.rental_saas.auth.repository;

import com.rentivo.rental_saas.auth.entity.Users;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepo extends JpaRepository<Users, Long> {
    @EntityGraph(attributePaths = "role")
    Optional<Users> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM Users u JOIN FETCH u.role WHERE u.email = :email")
    Optional<Users> findByEmailWithRole(@Param("email") String email);

    @Query("""
            SELECT DISTINCT u
            FROM Users u
            LEFT JOIN FETCH u.role r
            LEFT JOIN FETCH r.permissions
            WHERE u.id = :userId
            """)
    Optional<Users> findByIdWithRoleAndPermissions(
            @Param("userId") Long userId
    );

    @Query("""
            SELECT DISTINCT u
            FROM Users u
            JOIN FETCH u.role r
            LEFT JOIN FETCH r.permissions
            WHERE u.email = :email
            """)
    Optional<Users> findByEmailWithRoleAndPermissions(
            @Param("email") String email
    );
}
