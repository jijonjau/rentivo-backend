package com.rentivo.rental_saas.auth.common;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum ListingStatus {
    PUBLISHED(1),
    UNPUBLISHED(2),
    RENTED(3),
    SOLD(4);

    private final long code;

    ListingStatus(long code) {
        this.code = code;
    }

    public static ListingStatus fromCode(long code) {
        return Arrays.stream(values())
                .filter(status -> status.code == code)
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid listing status"));
    }
}