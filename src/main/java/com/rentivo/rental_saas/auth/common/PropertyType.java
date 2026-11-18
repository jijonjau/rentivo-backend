package com.rentivo.rental_saas.auth.common;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum PropertyType {

    APARTMENT(1),
    HOUSE(2),
    BUNGALOW(3),
    MAISONETTE(4),
    COMMERCIAL(5),
    HOSTEL(6);

    private final long code;

    PropertyType(long code) {
        this.code = code;
    }

    public static PropertyType fromCode(long code) {
        return Arrays.stream(values())
                .filter(type -> type.code == code)
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid property type"));
    }
}