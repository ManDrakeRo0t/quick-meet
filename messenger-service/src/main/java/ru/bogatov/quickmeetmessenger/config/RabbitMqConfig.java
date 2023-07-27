package ru.bogatov.quickmeetmessenger.config;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.Configuration;

@EnableRabbit
@Configuration
public class RabbitMqConfig {
    public static final String MEET_UPDATE_EVENT_QUEUE = "meet-update-event-queue";
    public static final String MEET_EXCHANGE = "meet-exchange";
    public static final String MEET_ROUTING_KEY = "meet.#";
}
