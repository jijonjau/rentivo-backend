package com.rentivo.rental_saas.common.messaging.consumer;

import com.rentivo.rental_saas.auth.service.OtpService;
import com.rentivo.rental_saas.common.messaging.event.OtpEventType;
import com.rentivo.rental_saas.common.messaging.event.OtpRequestedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OtpEventConsumer {
    private final OtpService otpService;

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory, JacksonJsonMessageConverter messageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        return factory;
    }

    @RabbitListener(queues = "${rentivo.rabbitmq.queues.register-otp}")
    public void handleRegisterOtpRequest(OtpRequestedEvent event) {
        if (event.getOtpEventType() == OtpEventType.REGISTER) {
            otpService.generateOtp(event.getUserId());
        }
    }

    @RabbitListener(queues = "${rentivo.rabbitmq.queues.login-otp}")
    public void handleLoginOtpRequest(OtpRequestedEvent event) {
        if (event.getOtpEventType() == OtpEventType.LOGIN) {
            otpService.generateOtp(event.getUserId());
        }
    }
}
