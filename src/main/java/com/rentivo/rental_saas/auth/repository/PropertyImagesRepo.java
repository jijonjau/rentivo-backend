package com.rentivo.rental_saas.auth.repository;

import com.rentivo.rental_saas.auth.entity.PropertyImages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyImagesRepo extends JpaRepository<PropertyImages, Long> {

    List<PropertyImages> findByPropertyIdOrderByPrimaryImageDesc(Long propertyId);

    @Query("""
    SELECT p.imageUrl
    FROM PropertyImages p
    WHERE p.imageUrl IS NOT NULL
""")
    List<String> findAllImageUrls();

    @Query("""
    SELECT pi FROM PropertyImages pi
    WHERE pi.property.id IN :propertyIds
    ORDER BY pi.primaryImage DESC
""")
    List<PropertyImages> findByPropertyIds(
            @Param("propertyIds") List<Long> propertyIds
    );

    long countByPropertyId(Long propertyId);

    void deleteByPropertyId(Long propertyId);
}