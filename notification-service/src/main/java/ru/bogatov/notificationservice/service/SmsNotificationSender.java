package ru.bogatov.notificationservice.service;

import ru.bogatov.notificationservice.model.request.VerificationEventBody;

public interface SmsNotificationSender {
     void sendSmsNotification(VerificationEventBody payload);

}
