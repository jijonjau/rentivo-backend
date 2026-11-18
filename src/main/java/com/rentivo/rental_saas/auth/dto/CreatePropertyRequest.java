package com.rentivo.rental_saas.auth.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePropertyRequest {

    @NotBlank
    private String propertyName;

    @NotNull
    private Integer propertyType;

    @NotBlank
    private String physicalAddress;

    private String city;

    private String county;

    private String description;

    @NotNull
    @Min(1)
    private Integer totalUnits;
}