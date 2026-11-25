package com.rentivo.rental_saas.auth.dto;

import com.rentivo.rental_saas.auth.common.PropertyType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FetchPropertyResponse {

    private Long id;
    private String propertyName;
    private PropertyType propertyType;
    private String physicalAddress;
    private String city;
    private String county;
    private String description;
    private Integer totalUnits;
    private Integer occupiedUnits;
    private boolean enabled;
    private List<PropertyImageResponse> images;
    private LocalDateTime createdAt;
}