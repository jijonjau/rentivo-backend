package com.rentivo.rental_saas.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    private int status;
    private String statusDesc;
    private LocalDateTime responseDateTime;
    private T payload;

    public static <T> ApiResponse<T> success(int status, String statusDesc, T payload) {
        return new ApiResponse<>(
                status,
                statusDesc,
                LocalDateTime.now(),
                payload
        );
    }

    public static ApiResponse<Void> error(int status, String statusDesc) {
        return new ApiResponse<>(
                status,
                statusDesc,
                LocalDateTime.now(),
                null
        );
    }
}
