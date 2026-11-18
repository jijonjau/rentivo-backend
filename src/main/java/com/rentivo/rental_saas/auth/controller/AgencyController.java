package com.rentivo.rental_saas.auth.controller;

import com.rentivo.rental_saas.auth.common.AgencyVerificationStatus;
import com.rentivo.rental_saas.auth.dto.CreateAgencyRequest;
import com.rentivo.rental_saas.auth.dto.FetchAgencyResponse;
import com.rentivo.rental_saas.auth.dto.ReviewApprovalRequest;
import com.rentivo.rental_saas.auth.entity.Users;
import com.rentivo.rental_saas.auth.service.AgencyService;
import com.rentivo.rental_saas.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/agencies")
@AllArgsConstructor
public class AgencyController {

    private final AgencyService agencyService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createAgency(
            @Valid @RequestPart("agency") CreateAgencyRequest request,
            @RequestPart("registrationDocument") MultipartFile registrationDocument,
            Authentication authentication
    ) {
        Users user = (Users) authentication.getPrincipal();

        agencyService.createAgency(request, registrationDocument, user);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(201, "Agency created successfully", null)
        );
    }

    @GetMapping("/me")
    public ResponseEntity<?> fetchMyAgency(Authentication authentication) {

        Users user = (Users) authentication.getPrincipal();

        FetchAgencyResponse response = agencyService.fetchMyAgency(user);

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        "Agency profile fetched successfully",
                        response
                )
        );
    }

    @GetMapping("/me/documents/registration")
    public ResponseEntity<Resource> fetchRegistrationDocument(
            Authentication authentication
    ) {
        Users user = (Users) authentication.getPrincipal();

        Resource document = agencyService.fetchRegistrationDocument(user);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + document.getFilename() + "\""  // attachment – prompts the user to download the document VS inline – opens the PDF/image directly in the browser for preview.
                )
                .body(document);
    }

    @GetMapping("/platform")
    public ResponseEntity<?> fetchAllAgencies(
            @RequestParam(required = false) AgencyVerificationStatus verificationStatus,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        Users user = (Users) authentication.getPrincipal();

        PageRequest pageRequest = PageRequest.of(pageNumber, size);

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        "Agencies fetched successfully",
                        agencyService.fetchAllAgencies(
                                verificationStatus, enabled, search, pageRequest, user)
                )
        );
    }

    @PatchMapping("/{agencyId}/review")
    public ResponseEntity<?> reviewAgency(
            @PathVariable Long agencyId,
            @Valid @RequestBody ReviewApprovalRequest request,
            Authentication authentication) {

        Users user = (Users) authentication.getPrincipal();

        agencyService.reviewAgency(agencyId, request, user);

        return ResponseEntity.ok(
                ApiResponse.success(200, "Agency reviewed successfully", null)
        );
    }
}