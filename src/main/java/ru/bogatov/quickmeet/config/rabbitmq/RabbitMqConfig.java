package ru.bogatov.quickmeet.config.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableRabbit
@Configuration
public class RabbitMqConfig {

    public static final String MEET_UPDATE_EVENT_QUEUE = "meet-update-event-queue";
    public static final String MEET_EXCHANGE = "meet-exchange";
    public static final String MEET_ROUTING_KEY = "meet.#";
    public static final String NOTIFICATION_EVENT_QUEUE = "notification-event-queue";
    public static final String NOTIFICATION_EVENT_DLQ = "notification-event-queue-dlq";
    public static final String NOTIFICATION_EXCHANGE = "notification-exchange";
    public static final String NOTIFICATION_ROUTING_KEY = "notification.#";

    @Bean
    public Queue meetUpdateEventQueue() {
        return new Queue(MEET_UPDATE_EVENT_QUEUE);
    }
    @Bean
    public Queue notificationEventQueue() {
        return QueueBuilder.durable(NOTIFICATION_EVENT_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", NOTIFICATION_EVENT_DLQ)
                .build();
//        return new Queue(NOTIFICATION_EVENT_QUEUE);
    }

    @Bean
    Queue notificationsDeadLetterQueue() {
        return QueueBuilder.durable(NOTIFICATION_EVENT_DLQ).build();
    }

    @Bean
    public TopicExchange meetExchange() {
        return new TopicExchange(MEET_EXCHANGE);
    }

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE);
    }

    @Bean
    Binding meetBinding(@Qualifier("meetUpdateEventQueue") Queue queue, @Qualifier("meetExchange") TopicExchange topicExchange) {
        return BindingBuilder.bind(queue).to(topicExchange).with(MEET_ROUTING_KEY);
    }

    @Bean
    Binding notificartionBinding(@Qualifier("notificationEventQueue") Queue queue, @Qualifier("notificationExchange") TopicExchange topicExchange) {
        return BindingBuilder.bind(queue).to(topicExchange).with(NOTIFICATION_ROUTING_KEY);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    @Bean
    public MessageConverter messageConverter(ObjectMapper jsonMapper) {
        return new Jackson2JsonMessageConverter(jsonMapper);
    }

    @Bean
    ApplicationRunner runner(ConnectionFactory cf) {
        return args -> cf.createConnection().close();
    }

}
