package ru.bogatov.notificationservice.controller.v1;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ru.bogatov.notificationservice.service.NotificationEventService;

import static ru.bogatov.notificationservice.config.RabbitMqConfig.NOTIFICATION_EVENT_QUEUE;

@Component
@Slf4j
public class NotificationEventListener {

    private final NotificationEventService notificationEventService;

    public NotificationEventListener(NotificationEventService notificationEventService) {
        this.notificationEventService = notificationEventService;
    }

    @RabbitListener(queues = NOTIFICATION_EVENT_QUEUE)
    public void receiveMessage(String message) {
        log.debug("received : {}" , message);
        notificationEventService.handleEvent(message);
    }
}
