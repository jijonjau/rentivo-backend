package com.rentivo.rental_saas.auth.service;

import com.rentivo.rental_saas.auth.config.FileStorageProperties;
import org.apache.tika.Tika;
import org.springframework.core.io.Resource;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Service;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.UUID;

@Service
@AllArgsConstructor
public class FileStorageService {

    private final FileStorageProperties properties;

    private final Tika tika = new Tika();

    public String storeAgencyDocument(MultipartFile file) {

        String detectedType = validateFile(file);

        String extension = switch (detectedType) {
            case "application/pdf" -> ".pdf";
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            default -> throw new IllegalArgumentException("Unsupported file type");
        };

        try {
            Path uploadPath = Paths.get(properties.getAgencyDocumentsPath())
                    .toAbsolutePath()
                    .normalize();

            Files.createDirectories(uploadPath);

            String filename = UUID.randomUUID() + extension;

            Path targetLocation = uploadPath.resolve(filename).normalize();

            if (!targetLocation.startsWith(uploadPath)) {
                throw new IllegalArgumentException("Invalid file path");
            }

            Files.copy(file.getInputStream(), targetLocation);

            return targetLocation.toString();

        } catch (IOException exception) {
            throw new RuntimeException("Failed to store agency document", exception);
        }
    }

    public String storePropertyImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Property image cannot be empty");
        }

        try {
            // Validate file size
            long maxSize = DataSize.parse(properties.getMaxImageSize()).toBytes();

            if (file.getSize() > maxSize) {
                throw new IllegalArgumentException("Property image exceeds maximum allowed size");
            }

            // Detect actual file type
            String detectedType;

            try (InputStream inputStream = file.getInputStream()) {
                detectedType = tika.detect(inputStream);
            }

            if (!properties.getAllowedImageTypes().contains(detectedType)) {
                throw new IllegalArgumentException("Unsupported property image format");
            }

            // Determine extension
            String extension = switch (detectedType) {
                case "image/jpeg" -> ".jpg";
                case "image/png" -> ".png";
                case "image/webp" -> ".webp";
                default -> throw new IllegalArgumentException("Unsupported image format");
            };

            // Create storage directory
            Path uploadDir = Paths.get(properties.getPropertyImagesPath())
                    .toAbsolutePath()
                    .normalize();

            Files.createDirectories(uploadDir);

            // Generate unique filename
            String filename = UUID.randomUUID() + extension;
            Path targetPath = uploadDir.resolve(filename).normalize();

            if (!targetPath.startsWith(uploadDir)) {
                throw new IllegalArgumentException("Invalid file path");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath);
            }

            return targetPath.toString();

        } catch (IOException e) {
            throw new RuntimeException("Failed to store property image", e);
        }
    }

    private String validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please upload a document");
        }

        long maxSize = DataSize.parse(properties.getMaxSize()).toBytes();

        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size exceeds the allowed limit");
        }

        try (InputStream inputStream = file.getInputStream()) {

            String detectedType = tika.detect(inputStream);

            if (!properties.getAllowedTypes().contains(detectedType)) {
                throw new IllegalArgumentException("Unsupported or invalid file content");
            }

            return detectedType;

        } catch (IOException exception) {
            throw new RuntimeException("Could not validate uploaded file", exception);
        }
    }

    public void deleteFile(String filePath) {
        try {
            Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException exception) {
            throw new RuntimeException("Failed to delete uploaded document", exception);
        }
    }

    public String detectContentType(Resource resource) {
        try (InputStream input = resource.getInputStream()) {
            return tika.detect(input);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to detect image type", exception);
        }
    }
}