package ru.bogatov.notificationservice.service;

import ru.bogatov.notificationservice.model.request.VerificationEventBody;

public interface MailNotificationSender {
    void sendMailNotification(VerificationEventBody payload);

}
