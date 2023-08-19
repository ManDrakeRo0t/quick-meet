package ru.bogatov.notificationservice.service;

import org.springframework.stereotype.Service;
import ru.bogatov.notificationservice.model.request.VerificationEventBody;

@Service
public class SmsMessageContentProducer {

    public String getRegistrationMessage(VerificationEventBody payload) {
        return String.format("[Quick meet] Добро пожаловать! Ваш код : %s", payload.getCode());
    }

    public String getVerificationMessage(VerificationEventBody payload) {
        return String.format("[Quick meet] Ваш проверочный код: %s", payload.getCode());
    }

}
