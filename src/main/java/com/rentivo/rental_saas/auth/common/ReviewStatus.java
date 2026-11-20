package com.rentivo.rental_saas.auth.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum ReviewStatus {
    APPROVED(200),
    REJECTED(300);

    private final long code;

    public static ReviewStatus fromCode(long code) {
        return Arrays.stream(values())
                .filter(status -> status.getCode() == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid status"));
    }
}