package com.rentivo.rental_saas.auth.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyImageResponse {

    private Long id;
    private String imageUrl;
    private boolean primaryImage;
}