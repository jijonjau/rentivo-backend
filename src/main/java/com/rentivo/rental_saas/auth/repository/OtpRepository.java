package com.rentivo.rental_saas.auth.repository;

import com.rentivo.rental_saas.auth.entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {
    Optional<Otp> findTopByUserIdOrderByCreatedAtDesc(Long userId);

    void deleteByUserId(Long userId);
}
