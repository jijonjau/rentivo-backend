package com.rentivo.rental_saas.auth.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FetchAgencyResponse {

    private Long id;
    private String agencyName;
    private String registrationNumber;
    private String email;
    private String phoneNumber;
    private String physicalAddress;
    private String status;
    private boolean hasRegistrationDocument;
    private LocalDateTime createdAt;
    private Long createdBy;
    private LocalDateTime updatedAt;
}