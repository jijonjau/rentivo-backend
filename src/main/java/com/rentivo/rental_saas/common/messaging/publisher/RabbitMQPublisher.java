package com.rentivo.rental_saas.common.messaging.publisher;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RabbitMQPublisher {
    private final RabbitTemplate rabbitTemplate;

    @Value("${rentivo.rabbitmq.exchange}")
    private String exchange;

    public void publish(String routingKey, Object message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }
}
