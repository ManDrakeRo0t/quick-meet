package ru.bogatov.quickmeet.service.auth;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import ru.bogatov.quickmeet.error.ErrorUtils;
import ru.bogatov.quickmeet.model.enums.ApplicationError;
import ru.bogatov.quickmeet.model.request.VerificationBody;

import static ru.bogatov.quickmeet.config.rabbitmq.RabbitMqConfig.NOTIFICATION_EXCHANGE;

@Service
public class NotificationEventSenderService {

    private final RabbitTemplate rabbitTemplate;

    public NotificationEventSenderService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendVerificationNotification(VerificationBody body) {
        if (body.getCode() == null || body.getCode().isEmpty()) {
            throw ErrorUtils.buildException(ApplicationError.COMMON_ERROR, "Error during code generation");
        }
        rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, "notification.verification", body);
    }


}
