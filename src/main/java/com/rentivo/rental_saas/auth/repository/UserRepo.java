package com.rentivo.rental_saas.auth.repository;

import com.rentivo.rental_saas.auth.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepo extends JpaRepository<Users, Long> {
    Optional<Users> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    @Query("SELECT u FROM Users u JOIN FETCH u.role WHERE u.id = :id")
    Optional<Users> findByIdWithRole(@Param("id") Long id);

    @Query("SELECT u FROM Users u JOIN FETCH u.role WHERE u.email = :email")
    Optional<Users> findByEmailWithRole(@Param("email") String email);
}
