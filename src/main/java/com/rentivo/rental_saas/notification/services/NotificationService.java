package com.rentivo.rental_saas.notification.services;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;

    public void sendOtpEmail(String email, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Rentivo - OTP Verification");
        message.setText(
                "Your Rentivo verification code is: " + otp + "\n\nThis code expires in 5 minutes");
        mailSender.send(message);
    }
}
