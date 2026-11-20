package com.rentivo.rental_saas.auth.service;

import com.rentivo.rental_saas.auth.common.*;
import com.rentivo.rental_saas.auth.dto.CreateAgencyRequest;
import com.rentivo.rental_saas.auth.dto.FetchAgencyResponse;
import com.rentivo.rental_saas.auth.dto.ReviewApprovalRequest;
import com.rentivo.rental_saas.auth.entity.Agencies;
import com.rentivo.rental_saas.auth.entity.Users;
import com.rentivo.rental_saas.auth.repository.AgencyRepo;
import com.rentivo.rental_saas.auth.repository.UserRepo;
import com.rentivo.rental_saas.common.exception.ResourceAlreadyExistsException;
import com.rentivo.rental_saas.common.exception.ResourceNotFoundException;
import com.rentivo.rental_saas.common.response.PageResponseDto;
import com.rentivo.rental_saas.common.response.PageResponseMapper;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@AllArgsConstructor
public class AgencyService {

    private final AgencyRepo agencyRepo;
    private final UserRepo userRepo;
    private final FileStorageService fileStorageService;

    // Helper methods
    private static @NonNull Agencies getAgencies(CreateAgencyRequest request) {
        Agencies agency = new Agencies();

        agency.setAgencyName(request.getAgencyName());
        agency.setRegistrationNumber(request.getRegistrationNumber());
        agency.setEmail(request.getEmail());
        agency.setPhoneNumber(request.getPhoneNumber());
        agency.setPhysicalAddress(request.getPhysicalAddress());
        agency.setVerificationStatus(AgencyVerificationStatus.PENDING);
        agency.setStatus(EntityStatus.DISABLED);
        return agency;
    }

    @Transactional
    public void createAgency(
            CreateAgencyRequest request,
            MultipartFile registrationDocument,
            Users user
    ) {

        if (user.getAccountType() != AccountType.AGENCY ||
                user.getOnboardingStatus() != OnboardingStatus.IN_PROGRESS) {
            throw new AccessDeniedException("You cannot create an agency");
        }

        if (user.getAgencyId() != null) {
            throw new ResourceAlreadyExistsException(
                    "User is already associated with an agency"
            );
        }

        if (agencyRepo.existsByRegistrationNumber(
                request.getRegistrationNumber())) {
            throw new ResourceAlreadyExistsException(
                    "Agency registration number already exists"
            );
        }

        if (agencyRepo.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException(
                    "Agency email already exists"
            );
        }

        String documentPath = fileStorageService.storeAgencyDocument(
                registrationDocument
        );

        try {
            Agencies agency = getAgencies(request);

            agency.setRegistrationDocumentUrl(documentPath);

            agencyRepo.saveAndFlush(agency);

            user.setAgencyId(agency.getId());
            userRepo.save(user);

        } catch (RuntimeException exception) {
            fileStorageService.deleteFile(documentPath);
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public FetchAgencyResponse fetchMyAgency(Users user) {

        if (user.getAccountType() != AccountType.AGENCY) {
            throw new AccessDeniedException("Only agency accounts can access this resource");
        }

        if (user.getAgencyId() == null) {
            throw new ResourceNotFoundException("No agency profile found");
        }

        Agencies agency = agencyRepo.findById(user.getAgencyId())
                .orElseThrow(() -> new ResourceNotFoundException("Agency not found"));

        return new FetchAgencyResponse(
                agency.getId(),
                agency.getAgencyName(),
                agency.getRegistrationNumber(),
                agency.getEmail(),
                agency.getPhoneNumber(),
                agency.getPhysicalAddress(),
                agency.getVerificationStatus(),
                agency.getStatus().name(),
                agency.getRegistrationDocumentUrl() != null,
                agency.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public Resource fetchRegistrationDocument(Users user) {

        if (user.getAccountType() != AccountType.AGENCY ||
                user.getAgencyId() == null) {
            throw new AccessDeniedException("You cannot access this document");
        }

        Agencies agency = agencyRepo.findById(user.getAgencyId())
                .orElseThrow(() -> new ResourceNotFoundException("Agency not found"));

        String documentPath = agency.getRegistrationDocumentUrl();

        if (documentPath == null || documentPath.isBlank()) {
            throw new ResourceNotFoundException("Registration document not found");
        }

        try {
            Path path = Paths.get(documentPath).toAbsolutePath().normalize();

            if (!Files.exists(path) || !Files.isRegularFile(path)) {
                throw new ResourceNotFoundException("Registration document not found");
            }

            return new UrlResource(path.toUri());

        } catch (MalformedURLException exception) {
            throw new RuntimeException("Could not retrieve registration document", exception);
        }
    }

    @Transactional(readOnly = true)
    public PageResponseDto<FetchAgencyResponse> fetchAllAgencies(
            AgencyVerificationStatus verificationStatus,
            EntityStatus status,
            String search,
            Pageable pageable,
            Users user) {

        if (!"SUPER_ADMIN".equals(user.getRole().getRoleName())) {
            throw new AccessDeniedException("Only super admins can fetch agencies");
        }

        Page<Agencies> agencies = agencyRepo.findAllWithFilters(
                verificationStatus, status, search, pageable);

        Page<FetchAgencyResponse> mapped = agencies.map(this::mapToFetchAgencyResponse);

        return PageResponseMapper.toPageResponse(mapped);
    }

    @Transactional
    public void reviewAgency(
            Long agencyId,
            ReviewApprovalRequest request,
            Users user) {

        if (!"SUPER_ADMIN".equals(user.getRole().getRoleName())) {
            throw new AccessDeniedException("Only super admins can review agencies");
        }

        Agencies agency = agencyRepo.findById(agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Agency not found"));

        if (agency.getVerificationStatus() != AgencyVerificationStatus.PENDING) {
            throw new IllegalStateException("Agency is not awaiting verification");
        }

        ReviewStatus reviewStatus = ReviewStatus.fromCode(request.getStatus());

        agency.setReviewDescription(request.getDescription());

        if (reviewStatus == ReviewStatus.APPROVED) {
            agency.setVerificationStatus(AgencyVerificationStatus.VERIFIED);
            agency.setStatus(EntityStatus.ACTIVE);
        } else {
            if (request.getDescription() == null || request.getDescription().isBlank()) {
                throw new IllegalArgumentException("A rejection reason is required");
            }

            agency.setVerificationStatus(AgencyVerificationStatus.REJECTED);
            agency.setStatus(EntityStatus.DISABLED);
        }

        agencyRepo.save(agency);
    }

    private FetchAgencyResponse mapToFetchAgencyResponse(Agencies agency) {
        return new FetchAgencyResponse(
                agency.getId(),
                agency.getAgencyName(),
                agency.getRegistrationNumber(),
                agency.getEmail(),
                agency.getPhoneNumber(),
                agency.getPhysicalAddress(),
                agency.getVerificationStatus(),
                agency.getStatus().name(),
                agency.getRegistrationDocumentUrl() != null
                        && !agency.getRegistrationDocumentUrl().isBlank(),
                agency.getCreatedAt()
        );
    }
}