package com.rentivo.rental_saas.auth.jobs;

import com.rentivo.rental_saas.auth.config.FileStorageProperties;
import com.rentivo.rental_saas.auth.repository.AgencyRepo;
import com.rentivo.rental_saas.auth.repository.PropertyImagesRepo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@AllArgsConstructor
@Slf4j
public class FileUploadCleanup {

    private final FileStorageProperties properties;
    private final AgencyRepo agencyRepo;
    private final PropertyImagesRepo propertyImagesRepo;

    @Scheduled(fixedDelayString =
            "${app.file.upload.cleanup-interval-ms}")
    public void cleanupOrphanedFiles() {

        Set<Path> referencedAgencyFiles = agencyRepo
                .findAllRegistrationDocumentUrls()
                .stream()
                .map(path -> Paths.get(path).toAbsolutePath().normalize())
                .collect(Collectors.toSet());

        Set<Path> referencedPropertyImages = propertyImagesRepo
                .findAllImageUrls()
                .stream()
                .map(path -> Paths.get(path).toAbsolutePath().normalize())
                .collect(Collectors.toSet());

        Instant cutoff = Instant.now().minus(
                properties.getCleanupAfterHours(), ChronoUnit.HOURS
        );

        cleanupDirectory(
                properties.getAgencyDocumentsPath(),
                referencedAgencyFiles,
                cutoff,
                "agency document"
        );

        cleanupDirectory(
                properties.getPropertyImagesPath(),
                referencedPropertyImages,
                cutoff,
                "property image"
        );
    }

    private void cleanupDirectory(
            String directory,
            Set<Path> referencedFiles,
            Instant cutoff,
            String fileType) {

        Path uploadDirectory = Paths.get(directory)
                .toAbsolutePath().normalize();

        if (!Files.exists(uploadDirectory)) {
            return;
        }

        try (Stream<Path> files = Files.list(uploadDirectory)) {

            files.filter(Files::isRegularFile)
                    .filter(file -> !referencedFiles.contains(
                            file.toAbsolutePath().normalize()
                    ))
                    .filter(file -> isOlderThan(file, cutoff))
                    .forEach(file -> deleteFile(file, fileType));

        } catch (IOException exception) {
            log.error("Failed to scan {} directory: {}",
                    fileType, uploadDirectory, exception);
        }
    }

    private boolean isOlderThan(Path file, Instant cutoff) {
        try {
            return Files.getLastModifiedTime(file)
                    .toInstant()
                    .isBefore(cutoff);
        } catch (IOException exception) {
            log.warn("Could not check file age: {}", file, exception);
            return false;
        }
    }

    private void deleteFile(Path file, String fileType) {
        try {
            Files.deleteIfExists(file);
            log.info("Deleted orphaned {}: {}", fileType, file);
        } catch (IOException exception) {
            log.error("Failed to delete orphaned file: {}",
                    file, exception);
        }
    }
}