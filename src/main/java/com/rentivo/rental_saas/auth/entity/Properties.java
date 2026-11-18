package com.rentivo.rental_saas.auth.entity;

import com.rentivo.rental_saas.auth.common.ListingStatus;
import com.rentivo.rental_saas.auth.common.PropertyType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "properties")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Properties {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String propertyName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PropertyType propertyType;

    @Column(nullable = false)
    private String physicalAddress;

    private String city;

    private String county;

    private String description;

    @Column(nullable = false)
    private Integer totalUnits;

    @Column(nullable = false)
    private Integer occupiedUnits;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agency_id")
    private Agencies agency;

    @Column(name = "landlord_id")
    private Long landlordId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ListingStatus listingStatus;

    @Column(nullable = false)
    private boolean enabled;

    @Column(columnDefinition = "TEXT")
    private String reviewDescription;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}