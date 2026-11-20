package com.rentivo.rental_saas.auth.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewApprovalRequest {

    @NotNull
    private Integer status;

    private String description;
}