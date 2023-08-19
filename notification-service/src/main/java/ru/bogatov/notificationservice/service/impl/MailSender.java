package ru.bogatov.notificationservice.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import ru.bogatov.notificationservice.model.enums.VerificationSourceType;
import ru.bogatov.notificationservice.model.enums.VerificationStep;
import ru.bogatov.notificationservice.model.request.VerificationEventBody;
import ru.bogatov.notificationservice.service.MailMessageContentProducer;
import ru.bogatov.notificationservice.service.MailNotificationSender;

@Component
@Slf4j
public class MailSender implements MailNotificationSender {
    private final JavaMailSender mailSender;
    private final MailMessageContentProducer contentProducer;

    public MailSender(JavaMailSender mailSender, MailMessageContentProducer contentProducer) {
        this.mailSender = mailSender;
        this.contentProducer = contentProducer;
    }

    @Override
    public void sendMailNotification(VerificationEventBody payload) {
        if (payload.getVerificationType() == VerificationSourceType.MAIL && payload.getVerificationStep() == VerificationStep.VERIFICATION) {
            this.send(contentProducer.getSimpleMailVerificationMessage(payload));
        }
    }


    public void send(SimpleMailMessage message) {
        log.info("Send message {}", message);
        try {
            mailSender.send(message);
        } catch (Exception ex) {
            log.error("Mail message failed with error : {}", ex.getMessage());
        }
    }
}
