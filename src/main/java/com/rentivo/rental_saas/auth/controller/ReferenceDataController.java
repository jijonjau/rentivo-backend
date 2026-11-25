package com.rentivo.rental_saas.auth.controller;

import com.rentivo.rental_saas.auth.service.ReferenceDataService;
import com.rentivo.rental_saas.common.response.ApiResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reference-data")
@AllArgsConstructor
public class ReferenceDataController {

    private final ReferenceDataService referenceDataService;

    @GetMapping("/account-types")
    public ResponseEntity<?> fetchAccountTypes() {
        return ResponseEntity.ok(ApiResponse.success(
                200,
                "Account types fetched successfully",
                referenceDataService.fetchAccountTypes()
        ));
    }

    @GetMapping("/property-types")
    public ResponseEntity<?> getPropertyTypes() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        "Property types fetched successfully",
                        referenceDataService.getPropertyTypes()
                )
        );
    }

    @GetMapping("/listing-statuses")
    public ResponseEntity<?> getListingStatuses() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        "Listing statuses fetched successfully",
                        referenceDataService.getListingStatuses()
                )
        );
    }
}