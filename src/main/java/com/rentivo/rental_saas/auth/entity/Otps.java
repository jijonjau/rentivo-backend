package com.rentivo.rental_saas.auth.entity;

import com.rentivo.rental_saas.common.messaging.event.OtpEventType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Otps {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;
    @Column(nullable = false)
    private String otpHash;
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OtpEventType eventType;
    @Column(nullable = false)
    private int attempts;
    @Column(nullable = false)
    private boolean verified;
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}