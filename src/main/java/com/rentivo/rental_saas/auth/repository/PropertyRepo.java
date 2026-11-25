package com.rentivo.rental_saas.auth.repository;

import com.rentivo.rental_saas.auth.common.ListingStatus;
import com.rentivo.rental_saas.auth.common.PropertyType;
import com.rentivo.rental_saas.auth.entity.Properties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PropertyRepo extends JpaRepository<Properties, Long> {

    @Query("""
                SELECT p FROM Properties p
                WHERE p.listingStatus = :listingStatus
                AND p.enabled = true
                AND (:propertyType IS NULL OR p.propertyType = :propertyType)
                AND (
                    CAST(:county AS string) IS NULL
                    OR LOWER(p.county) = LOWER(CAST(:county AS string))
                )
            """)
    Page<Properties> findPublicProperties(
            ListingStatus listingStatus,
            PropertyType propertyType,
            String county,
            Pageable pageable
    );

    Optional<Properties> findByIdAndListingStatusAndEnabledTrue(
            Long id,
            ListingStatus listingStatus
    );

    @Query("""
                SELECT p FROM Properties p
                WHERE (:listingStatus IS NULL OR p.listingStatus = :listingStatus)
                AND (:propertyType IS NULL OR p.propertyType = :propertyType)
                AND (
                    CAST(:county AS string) IS NULL
                    OR LOWER(p.county) = LOWER(CAST(:county AS string))
                )
            """)
    Page<Properties> findAllWithFilters(
            @Param("listingStatus") ListingStatus listingStatus,
            @Param("propertyType") PropertyType propertyType,
            @Param("county") String county, // to add filter by owner
            Pageable pageable
    );

    Page<Properties> findByAgencyId(Long agencyId, Pageable pageable);

    Page<Properties> findByLandlordId(Long landlordId, Pageable pageable);

    boolean existsByLandlordId(Long landlordId);

    boolean existsByAgency_Id(Long agencyId);
}