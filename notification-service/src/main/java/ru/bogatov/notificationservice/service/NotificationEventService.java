package ru.bogatov.notificationservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.bogatov.notificationservice.model.request.VerificationEventBody;

@Service
@Slf4j
public class NotificationEventService {

    private final MailNotificationSender mailSender;

    private final SmsNotificationSender smsNotificationSender;

    private final
    ObjectMapper mapper = new ObjectMapper();

    public NotificationEventService(MailNotificationSender mailSender, SmsNotificationSender smsNotificationSender) {
        this.mailSender = mailSender;
        this.smsNotificationSender = smsNotificationSender;
    }

    public void handleEvent(String message) {
        try {
            VerificationEventBody payload = mapper.readValue(message, VerificationEventBody.class);
            switch (payload.getVerificationType()) {
                case MAIL:
                    mailSender.sendMailNotification(payload);
                    break;
                case PHONE:
                    log.info("PHONE send {}", payload);
                    smsNotificationSender.sendSmsNotification(payload);
                    break;
                default:
            }
        } catch (JsonProcessingException ex) {
            log.error("Error during mapping event : {}", message);
        }
    }
}
