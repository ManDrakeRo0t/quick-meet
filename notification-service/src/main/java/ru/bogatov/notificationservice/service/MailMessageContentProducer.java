package ru.bogatov.notificationservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import ru.bogatov.notificationservice.model.request.VerificationEventBody;

@Service
public class MailMessageContentProducer {

    @Value("${spring.mail.username}")
    String username;

    public SimpleMailMessage getSimpleMailVerificationMessage(VerificationEventBody body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(username);
        message.setTo(body.getSource());
        message.setSubject("[Quick meet] Подтвердите почту");
        message.setText("Ваш код подтверждения : " + body.getCode());
        return message;
    }


}
