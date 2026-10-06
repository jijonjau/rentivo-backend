package com.rentivo.rental_saas.common.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Value("${rentivo.rabbitmq.exchange}")
    private String exchange;
    @Value("${rentivo.rabbitmq.queues.register-otp}")
    private String registerOtpQueue;
    @Value("${rentivo.rabbitmq.queues.login-otp}")
    private String loginOtpQueue;
    @Value("${rentivo.rabbitmq.queues.reset-password-otp}")
    private String resetPasswordOtpQueue;

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchange);
    }

    @Bean
    public JacksonJsonMessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, JacksonJsonMessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    @Bean
    public Queue registerOtpQueue() {
        return new Queue(registerOtpQueue);
    }

    @Bean
    public Queue loginOtpQueue() {
        return new Queue(loginOtpQueue);
    }

    @Bean
    public Queue resetPasswordOtpQueue() {
        return new Queue(resetPasswordOtpQueue);
    }

    @Bean
    public Binding registerOtpBinding(Queue registerOtpQueue, TopicExchange exchange) {
        return BindingBuilder.bind(registerOtpQueue).to(exchange).with("register.otp");
    }

    @Bean
    public Binding loginOtpBinding(Queue loginOtpQueue, TopicExchange exchange) {
        return BindingBuilder.bind(loginOtpQueue).to(exchange).with("login.otp");
    }

    @Bean
    public Binding resetPasswordOtpBinding(Queue resetPasswordOtpQueue, TopicExchange exchange) {
        return BindingBuilder.bind(resetPasswordOtpQueue).to(exchange).with("reset.password.otp");
    }
}
