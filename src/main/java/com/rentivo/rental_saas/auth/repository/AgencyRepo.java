package com.rentivo.rental_saas.auth.repository;

import com.rentivo.rental_saas.auth.common.EntityStatus;
import com.rentivo.rental_saas.auth.entity.Agencies;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgencyRepo extends JpaRepository<Agencies, Long> {

    boolean existsByRegistrationNumber(String registrationNumber);

    boolean existsByEmail(String email);

    @Query("""
                SELECT a.registrationDocumentUrl
                FROM Agencies a
                WHERE a.registrationDocumentUrl IS NOT NULL
            """)
    List<String> findAllRegistrationDocumentUrls();

    @Query("""
                SELECT a FROM Agencies a
                WHERE (:status IS NULL OR a.status = :status)
                AND (
                    CAST(:search AS string) IS NULL OR
                    LOWER(a.agencyName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR
                    LOWER(a.email) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR
                    LOWER(a.registrationNumber) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                )
            """)
    Page<Agencies> findAllWithFilters(
            @Param("status") EntityStatus status,
            @Param("search") String search,
            Pageable pageable);

    boolean existsByCreatedBy_Id(Long userId);

    Optional<Agencies> findByCreatedBy_Id(Long userId);
}