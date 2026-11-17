package com.rentivo.rental_saas.auth.common;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum AccountType {
    TENANT(1),
    LANDLORD(2),
    AGENCY(3),
    PLATFORM(4);

    private final int code;

    AccountType(int code) {
        this.code = code;
    }

    public static AccountType fromCode(int code) {
        return Arrays.stream(values())
                .filter(type -> type.code == code)
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid account type")
                );
    }
}