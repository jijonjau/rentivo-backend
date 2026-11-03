package com.rentivo.rental_saas.auth.repository;

import com.rentivo.rental_saas.auth.entity.PasswordHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PasswordHistoryRepo extends JpaRepository<PasswordHistory, Long> {

    List<PasswordHistory> findTop4ByUserIdOrderByCreatedAtDesc(Long userId);
}