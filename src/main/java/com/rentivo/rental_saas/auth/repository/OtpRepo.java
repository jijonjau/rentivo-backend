package com.rentivo.rental_saas.auth.repository;

import com.rentivo.rental_saas.auth.entity.Otps;
import com.rentivo.rental_saas.common.messaging.event.OtpEventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpRepo extends JpaRepository<Otps, Long> {
    Optional<Otps> findTopByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Otps> findTopByUserIdAndEventTypeOrderByCreatedAtDesc(
            Long userId,
            OtpEventType eventType
    );

    void deleteByUserId(Long userId);
}
