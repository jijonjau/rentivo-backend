package com.rentivo.rental_saas.auth.common;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum ListingStatus {

    DRAFT(1),
    PENDING_APPROVAL(2),
    PUBLISHED(3),
    REJECTED(4),
    UNPUBLISHED(5),
    RENTED(6),
    SOLD(7);

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