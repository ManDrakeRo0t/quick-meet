package ru.bogatov.notificationservice.controller.v1;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.bogatov.notificationservice.model.enums.VerificationSourceType;
import ru.bogatov.notificationservice.model.enums.VerificationStep;
import ru.bogatov.notificationservice.model.request.VerificationEventBody;
import ru.bogatov.notificationservice.service.MailNotificationSender;
import ru.bogatov.notificationservice.service.SmsNotificationSender;

@RestController
@Slf4j
public class NotificationController {

    private final MailNotificationSender mailSender;

    private final SmsNotificationSender smsNotificationSender;


    public NotificationController(MailNotificationSender mailSender, SmsNotificationSender smsNotificationSender) {
        this.mailSender = mailSender;
        this.smsNotificationSender = smsNotificationSender;
    }

    @GetMapping("api/v1/test/sms")
    public ResponseEntity testSmsNotification() {
        VerificationEventBody body = new VerificationEventBody();
        body.setCode("0000");
        body.setVerificationStep(VerificationStep.REGISTRATION);
        body.setVerificationType(VerificationSourceType.PHONE);
        body.setSource("79020439309");
        smsNotificationSender.sendSmsNotification(body);
        log.info("send test sms");
        return ResponseEntity.ok(null);
    }

    @GetMapping("api/v1/test/mail")
    public ResponseEntity testMailNotification() {
        VerificationEventBody body = new VerificationEventBody();
        body.setCode("0000");
        body.setVerificationStep(VerificationStep.VERIFICATION);
        body.setVerificationType(VerificationSourceType.MAIL);
        body.setSource("bogatov-danila@mail.ru");
        mailSender.sendMailNotification(body);
        log.info("send test mail");
        return ResponseEntity.ok(null);
    }
}
