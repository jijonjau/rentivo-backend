package com.rentivo.rental_saas.auth.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum EntityStatus {
    ACTIVE(200),
    SUSPENDED(301),
    DISABLED(300);

    private final long code;

    public static EntityStatus fromCode(long code) {
        return Arrays.stream(values())
                .filter(entityStatus -> entityStatus.getCode() == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid status"));
    }
}
