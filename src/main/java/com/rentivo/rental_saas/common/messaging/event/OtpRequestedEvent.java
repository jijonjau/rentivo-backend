package com.rentivo.rental_saas.common.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpRequestedEvent {
    private Long userId;
    private String email;
    private OtpEventType otpEventType;
}
