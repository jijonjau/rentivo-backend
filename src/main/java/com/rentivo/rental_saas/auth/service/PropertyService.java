
package com.rentivo.rental_saas.auth.service;

import com.rentivo.rental_saas.auth.common.AccountType;
import com.rentivo.rental_saas.auth.common.AgencyVerificationStatus;
import com.rentivo.rental_saas.auth.common.ListingStatus;
import com.rentivo.rental_saas.auth.common.PropertyType;
import com.rentivo.rental_saas.auth.config.FileStorageProperties;
import com.rentivo.rental_saas.auth.dto.CreatePropertyRequest;
import com.rentivo.rental_saas.auth.dto.FetchPropertyResponse;
import com.rentivo.rental_saas.auth.dto.PropertyImageResponse;
import com.rentivo.rental_saas.auth.dto.ReviewApprovalRequest;
import com.rentivo.rental_saas.auth.entity.Agencies;
import com.rentivo.rental_saas.auth.entity.Properties;
import com.rentivo.rental_saas.auth.entity.PropertyImages;
import com.rentivo.rental_saas.auth.entity.Users;
import com.rentivo.rental_saas.auth.repository.AgencyRepo;
import com.rentivo.rental_saas.auth.repository.PropertyImagesRepo;
import com.rentivo.rental_saas.auth.repository.PropertyRepo;
import com.rentivo.rental_saas.auth.repository.UserRepo;
import com.rentivo.rental_saas.common.exception.ResourceNotFoundException;
import com.rentivo.rental_saas.common.response.PageResponseDto;
import com.rentivo.rental_saas.common.response.PageResponseMapper;
import lombok.AllArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PropertyService {

    private static final String PROPERTY_IMAGE_URL =
            "/api/v1/properties/public/images/";

    private final PropertyRepo propertyRepo;
    private final AgencyRepo agencyRepo;
    private final PropertyImagesRepo propertyImagesRepo;
    private final FileStorageService fileStorageService;
    private final FileStorageProperties properties;
    private final UserRepo userRepo;

    // CREATE PROPERTY

    @Transactional
    public void createProperty(
            CreatePropertyRequest request,
            List<MultipartFile> images,
            Users user) {

        validatePropertyImages(images);

        Properties property = new Properties();

        property.setPropertyName(request.getPropertyName());
        property.setPropertyType(PropertyType.fromCode(request.getPropertyType()));
        property.setPhysicalAddress(request.getPhysicalAddress());
        property.setCity(request.getCity());
        property.setCounty(request.getCounty());
        property.setDescription(request.getDescription());
        property.setTotalUnits(request.getTotalUnits());
        property.setOccupiedUnits(0);
        property.setListingStatus(ListingStatus.DRAFT);

        assignPropertyOwnership(property, user);

        List<String> uploadedFiles = new ArrayList<>();

        try {
            Properties savedProperty = propertyRepo.saveAndFlush(property);

            List<PropertyImages> propertyImages = new ArrayList<>();

            for (int i = 0; i < images.size(); i++) {

                String imagePath =
                        fileStorageService.storePropertyImage(images.get(i));

                uploadedFiles.add(imagePath);

                PropertyImages propertyImage = new PropertyImages();
                propertyImage.setProperty(savedProperty);
                propertyImage.setImageUrl(imagePath);
                propertyImage.setPrimaryImage(i == 0);

                propertyImages.add(propertyImage);
            }

            propertyImagesRepo.saveAll(propertyImages);

        } catch (RuntimeException exception) {

            uploadedFiles.forEach(fileStorageService::deleteFile);

            throw exception;
        }
    }

    // FETCH SINGLE PROPERTY - OWNER

    @Transactional(readOnly = true)
    public FetchPropertyResponse fetchProperty(Long propertyId, Users user) {

        Properties property = getProperty(propertyId);

        validatePropertyOwnership(property, user);

        List<PropertyImageResponse> images =
                propertyImagesRepo.findByPropertyIdOrderByPrimaryImageDesc(propertyId)
                        .stream()
                        .map(this::mapToPropertyImageResponse)
                        .toList();

        return mapToFetchPropertyResponse(property, images);
    }

    // BROWSE PUBLIC PROPERTIES

    @Transactional(readOnly = true)
    public PageResponseDto<FetchPropertyResponse> browseProperties(
            PropertyType propertyType,
            String county,
            Pageable pageable) {

        Page<Properties> propertyPage = propertyRepo.findPublicProperties(
                ListingStatus.PUBLISHED,
                propertyType,
                county,
                pageable
        );

        Map<Long, List<PropertyImageResponse>> imagesMap =
                fetchPropertyImagesMap(propertyPage);

        Page<FetchPropertyResponse> mappedProperties =
                propertyPage.map(property ->
                        mapToFetchPropertyResponse(
                                property,
                                imagesMap.getOrDefault(property.getId(), List.of())
                        )
                );

        return PageResponseMapper.toPageResponse(mappedProperties);
    }

    // FETCH SINGLE PUBLIC PROPERTY

    @Transactional(readOnly = true)
    public FetchPropertyResponse fetchPublicProperty(Long propertyId) {

        Properties property = propertyRepo
                .findByIdAndListingStatusAndEnabledTrue(
                        propertyId,
                        ListingStatus.PUBLISHED
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException("Property not found"));

        List<PropertyImageResponse> images =
                propertyImagesRepo.findByPropertyIdOrderByPrimaryImageDesc(propertyId)
                        .stream()
                        .map(this::mapToPropertyImageResponse)
                        .toList();

        return mapToFetchPropertyResponse(property, images);
    }

    // SUBMIT PROPERTY FOR APPROVAL

    @Transactional
    public void submitPropertyForApproval(Long propertyId, Users user) {

        Properties property = getProperty(propertyId);

        validatePropertyOwnership(property, user);

        if (property.getListingStatus() != ListingStatus.DRAFT
                && property.getListingStatus() != ListingStatus.REJECTED) {

            throw new IllegalStateException(
                    "Only draft or rejected properties can be submitted");
        }

        long imageCount = propertyImagesRepo.countByPropertyId(propertyId);

        if (imageCount < 2) {
            throw new IllegalArgumentException(
                    "At least 2 property images are required");
        }

        property.setListingStatus(ListingStatus.PENDING_APPROVAL);

        propertyRepo.save(property);
    }

    // ADMIN - FETCH ALL PROPERTIES

    @Transactional(readOnly = true)
    public PageResponseDto<FetchPropertyResponse> fetchAllProperties(
            ListingStatus listingStatus,
            PropertyType propertyType,
            String county,
            Pageable pageable,
            Users user) {

        validateSuperAdmin(user);

        Page<Properties> propertyPage = propertyRepo.findAllWithFilters(
                listingStatus,
                propertyType,
                county,
                pageable
        );

        Map<Long, List<PropertyImageResponse>> imagesMap =
                fetchPropertyImagesMap(propertyPage);

        Page<FetchPropertyResponse> mappedProperties =
                propertyPage.map(property ->
                        mapToFetchPropertyResponse(
                                property,
                                imagesMap.getOrDefault(property.getId(), List.of())
                        )
                );

        return PageResponseMapper.toPageResponse(mappedProperties);
    }

    // ADMIN - APPROVE OR REJECT PROPERTY

    @Transactional
    public void reviewProperty(
            Long propertyId,
            ReviewApprovalRequest request,
            Users user) {

        validateSuperAdmin(user);

        Properties property = getProperty(propertyId);

        if (property.getListingStatus() != ListingStatus.PENDING_APPROVAL) {
            throw new IllegalStateException(
                    "Property is not awaiting approval");
        }

        if (Boolean.TRUE.equals(request.getApproved())) {

            validatePropertyOwnerAccount(property);

            property.setListingStatus(ListingStatus.PUBLISHED);
            property.setEnabled(true);
            property.setReviewDescription(request.getDescription());

        } else {

            if (request.getDescription() == null
                    || request.getDescription().isBlank()) {

                throw new IllegalArgumentException(
                        "A rejection reason is required");
            }

            property.setListingStatus(ListingStatus.REJECTED);
            property.setReviewDescription(request.getDescription());
            property.setEnabled(false);
        }

        propertyRepo.save(property);
    }

    // OWNER - FETCH MY PROPERTIES

    @Transactional(readOnly = true)
    public PageResponseDto<FetchPropertyResponse> fetchMyProperties(
            Pageable pageable,
            Users user) {

        Page<Properties> propertyPage = switch (user.getAccountType()) {
            case AGENCY -> {
                if (user.getAgencyId() == null) {
                    throw new ResourceNotFoundException("Agency profile not found");
                }
                yield propertyRepo.findByAgencyId(user.getAgencyId(), pageable);
            }

            case LANDLORD ->
                    propertyRepo.findByLandlordId(user.getId(), pageable);

            default -> throw new AccessDeniedException(
                    "You are not authorized to access these properties");
        };

        Map<Long, List<PropertyImageResponse>> imagesMap =
                fetchPropertyImagesMap(propertyPage);

        Page<FetchPropertyResponse> mapped =
                propertyPage.map(property ->
                        mapToFetchPropertyResponse(
                                property,
                                imagesMap.getOrDefault(property.getId(), List.of())
                        )
                );

        return PageResponseMapper.toPageResponse(mapped);
    }

    // SERVE PROPERTY IMAGE

    @Transactional(readOnly = true)
    public Resource fetchPropertyImage(Long imageId) {

        PropertyImages propertyImage = propertyImagesRepo.findById(imageId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Image not found"));

        Path uploadDirectory = Paths.get(
                properties.getPropertyImagesPath()
        ).toAbsolutePath().normalize();

        Path imagePath = Paths.get(propertyImage.getImageUrl())
                .toAbsolutePath()
                .normalize();

        // Prevent path traversal / serving files outside upload directory
        if (!imagePath.startsWith(uploadDirectory)) {
            throw new AccessDeniedException("Invalid image path");
        }

        Resource resource = new FileSystemResource(imagePath);

        if (!resource.exists() || !resource.isReadable()
                || !Files.isRegularFile(imagePath)) {

            throw new ResourceNotFoundException("Image file not found");
        }

        return resource;
    }

    // PRIVATE HELPER METHODS

    private void validatePropertyImages(List<MultipartFile> images) {

        if (images == null || images.size() < 2) {
            throw new IllegalArgumentException(
                    "At least 2 property images are required");
        }

        if (images.size() > properties.getMaxImages()) {
            throw new IllegalArgumentException(
                    "Maximum allowed images: " + properties.getMaxImages());
        }
    }

    private void assignPropertyOwnership(Properties property, Users user) {

        if (user.getAccountType() == AccountType.AGENCY) {

            if (user.getAgencyId() == null) {
                throw new ResourceNotFoundException(
                        "Create your agency profile first");
            }

            Agencies agency = agencyRepo.findById(user.getAgencyId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Agency not found"));

            property.setAgency(agency);

        } else if (user.getAccountType() == AccountType.LANDLORD) {

            property.setLandlordId(user.getId());

        } else {

            throw new AccessDeniedException("You cannot create properties");
        }
    }

    private Properties getProperty(Long propertyId) {

        return propertyRepo.findById(propertyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Property not found"));
    }

    private void validatePropertyOwnership(Properties property, Users user) {

        if (!isPropertyOwner(property, user)) {
            throw new AccessDeniedException(
                    "You cannot access this property");
        }
    }

    private boolean isPropertyOwner(Properties property, Users user) {

        return switch (user.getAccountType()) {

            case AGENCY ->
                    property.getAgency() != null
                            && user.getAgencyId() != null
                            && property.getAgency().getId()
                            .equals(user.getAgencyId());

            case LANDLORD ->
                    property.getLandlordId() != null
                            && property.getLandlordId().equals(user.getId());

            default -> false;
        };
    }

    private void validateSuperAdmin(Users user) {

        if (!"SUPER_ADMIN".equals(user.getRole().getRoleName())) {
            throw new AccessDeniedException(
                    "Only super admins can perform this action");
        }
    }

    private void validatePropertyOwnerAccount(Properties property) {

        if (property.getAgency() != null) {

            Agencies agency = property.getAgency();

            if (!agency.isEnabled()
                    || agency.getVerificationStatus()
                    != AgencyVerificationStatus.VERIFIED) {

                throw new IllegalStateException(
                        "The agency must be enabled and verified before this property can be approved");
            }

        } else {

            if (property.getLandlordId() == null) {
                throw new IllegalStateException(
                        "Property does not have a valid owner");
            }

            Users landlord = userRepo.findById(property.getLandlordId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Landlord not found"));

            if (!landlord.isEnabled()) {
                throw new IllegalStateException(
                        "The landlord account must be enabled before this property can be approved");
            }
        }
    }

    private Map<Long, List<PropertyImageResponse>> fetchPropertyImagesMap(
            Page<Properties> propertyPage) {

        List<Long> propertyIds = propertyPage.getContent()
                .stream()
                .map(Properties::getId)
                .toList();

        if (propertyIds.isEmpty()) {
            return Map.of();
        }

        return propertyImagesRepo.findByPropertyIds(propertyIds)
                .stream()
                .collect(Collectors.groupingBy(
                        image -> image.getProperty().getId(),
                        Collectors.mapping(
                                this::mapToPropertyImageResponse,
                                Collectors.toList()
                        )
                ));
    }

    private PropertyImageResponse mapToPropertyImageResponse(
            PropertyImages image) {

        return PropertyImageResponse.builder()
                .id(image.getId())
                .imageUrl(PROPERTY_IMAGE_URL + image.getId())
                .primaryImage(image.isPrimaryImage())
                .build();
    }

    private FetchPropertyResponse mapToFetchPropertyResponse(
            Properties property,
            List<PropertyImageResponse> images) {

        return FetchPropertyResponse.builder()
                .id(property.getId())
                .propertyName(property.getPropertyName())
                .propertyType(property.getPropertyType())
                .physicalAddress(property.getPhysicalAddress())
                .city(property.getCity())
                .county(property.getCounty())
                .description(property.getDescription())
                .totalUnits(property.getTotalUnits())
                .occupiedUnits(property.getOccupiedUnits())
                .enabled(property.isEnabled())
                .images(images)
                .createdAt(property.getCreatedAt())
                .build();
    }
}
