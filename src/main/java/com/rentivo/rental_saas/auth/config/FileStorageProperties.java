package com.rentivo.rental_saas.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.file.upload")
@Getter
@Setter
public class FileStorageProperties {

    private String basePath;
    private String agencyDocumentsPath;
    private String maxSize;
    private List<String> allowedTypes;
    private String propertyImagesPath;
    private int maxImages;
    private String maxImageSize;
    private List<String> allowedImageTypes;
    private long cleanupIntervalMs;
    private long cleanupAfterHours;
}