package com.rentivo.rental_saas.auth.controller;

import com.rentivo.rental_saas.auth.common.ListingStatus;
import com.rentivo.rental_saas.auth.common.PropertyType;
import com.rentivo.rental_saas.auth.dto.CreatePropertyRequest;
import com.rentivo.rental_saas.auth.dto.FetchPropertyResponse;
import com.rentivo.rental_saas.auth.dto.ReviewApprovalRequest;
import com.rentivo.rental_saas.auth.entity.Users;
import com.rentivo.rental_saas.auth.service.FileStorageService;
import com.rentivo.rental_saas.auth.service.PropertyService;
import com.rentivo.rental_saas.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/properties")
@AllArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;
    private final FileStorageService fileStorageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProperty(
            @Valid @RequestPart("property") CreatePropertyRequest request,
            @RequestPart("images") List<MultipartFile> images,
            Authentication authentication) {

        Users user = (Users) authentication.getPrincipal();

        propertyService.createProperty(request, images, user);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(201, "Property created successfully", null)
        );
    }

    @GetMapping("/{propertyId}")
    public ResponseEntity<?> fetchProperty(
            @PathVariable Long propertyId,
            Authentication authentication) {

        Users user = (Users) authentication.getPrincipal();

        FetchPropertyResponse response =
                propertyService.fetchProperty(propertyId, user);

        return ResponseEntity.ok(
                ApiResponse.success(200, "Property fetched successfully", response)
        );
    }

    @GetMapping("/public")
    public ResponseEntity<?> browseProperties(
            @RequestParam(required = false) Integer propertyType,
            @RequestParam(required = false) String county,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int size
    ) {

        PropertyType type = propertyType != null
                ? PropertyType.fromCode(propertyType)
                : null;

        PageRequest pageRequest = PageRequest.of(pageNumber, size);

        return ResponseEntity.ok(ApiResponse.success(
                200,
                "Properties fetched successfully",
                propertyService.browseProperties(type, county, pageRequest)
        ));
    }

    @GetMapping("/public/{propertyId}")
    public ResponseEntity<?> fetchPublicProperty(
            @PathVariable Long propertyId) {

        FetchPropertyResponse response =
                propertyService.fetchPublicProperty(propertyId);

        return ResponseEntity.ok(ApiResponse.success(
                200,
                "Property details fetched successfully",
                response
        ));
    }

    @PostMapping("/{propertyId}/submit")
    public ResponseEntity<?> submitPropertyForApproval(
            @PathVariable Long propertyId,
            Authentication authentication) {

        Users user = (Users) authentication.getPrincipal();

        propertyService.submitPropertyForApproval(propertyId, user);

        return ResponseEntity.ok(ApiResponse.success(
                200,
                "Property submitted for approval successfully",
                null
        ));
    }

    @GetMapping("/platform")
    public ResponseEntity<?> fetchAllProperties(
            @RequestParam(required = false) Integer listingStatus,
            @RequestParam(required = false) Integer propertyType,
            @RequestParam(required = false) String county,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {

        Users user = (Users) authentication.getPrincipal();

        ListingStatus status = listingStatus != null
                ? ListingStatus.fromCode(listingStatus)
                : null;

        PropertyType type = propertyType != null
                ? PropertyType.fromCode(propertyType)
                : null;

        PageRequest pageRequest = PageRequest.of(pageNumber, size);

        return ResponseEntity.ok(ApiResponse.success(
                200,
                "Properties fetched successfully",
                propertyService.fetchAllProperties(
                        status, type, county, pageRequest, user
                )
        ));
    }

    @PatchMapping("/{propertyId}/review")
    public ResponseEntity<?> reviewProperty(
            @PathVariable Long propertyId,
            @Valid @RequestBody ReviewApprovalRequest request,
            Authentication authentication) {

        Users user = (Users) authentication.getPrincipal();

        propertyService.reviewProperty(propertyId, request, user);

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        "Property reviewed successfully",
                        null
                )
        );
    }

    @GetMapping("/my-properties")
    public ResponseEntity<?> fetchMyProperties(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        Users user = (Users) authentication.getPrincipal();

        PageRequest pageRequest = PageRequest.of(pageNumber, size);

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        "Properties fetched successfully",
                        propertyService.fetchMyProperties(pageRequest, user)
                )
        );
    }

    @GetMapping("/public/images/{imageId}")
    public ResponseEntity<Resource> fetchPropertyImage(
            @PathVariable Long imageId) {

        Resource image = propertyService.fetchPropertyImage(imageId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        fileStorageService.detectContentType(image)))
                .body(image);
    }
}