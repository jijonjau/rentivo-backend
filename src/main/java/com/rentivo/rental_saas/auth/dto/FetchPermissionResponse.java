package com.rentivo.rental_saas.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FetchPermissionResponse {
    private Long id;
    private String permissionName;
}