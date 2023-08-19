package ru.bogatov.notificationservice.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import ru.bogatov.notificationservice.model.enums.VerificationSourceType;
import ru.bogatov.notificationservice.model.request.VerificationEventBody;
import ru.bogatov.notificationservice.service.SmsMessageContentProducer;
import ru.bogatov.notificationservice.service.SmsNotificationSender;

@Component
@Slf4j
public class SmsAeroSender implements SmsNotificationSender {

    private final WebClient webClient;

    private final SmsMessageContentProducer contentProducer;

    public SmsAeroSender(@Qualifier("aeroWebClient") WebClient webClient, SmsMessageContentProducer contentProducer) {
        this.webClient = webClient;
        this.contentProducer = contentProducer;
    }

    @Override
    public void sendSmsNotification(VerificationEventBody payload) {
        String message = "";
        if (payload.getVerificationType() == VerificationSourceType.PHONE) {
            switch (payload.getVerificationStep()) {
                case VERIFICATION:
                    message = contentProducer.getVerificationMessage(payload);
                    break;
                case REGISTRATION:
                    message = contentProducer.getRegistrationMessage(payload);
                    break;
            }
            if (!message.isEmpty()) {
                String finalMessage = message;
                String resp = webClient.get()
                        .uri(uriBuilder ->
                                uriBuilder.queryParam("text", finalMessage)
                                        .queryParam("sign", "SMS Aero")
                                        .queryParam("number", payload.getSource())
                                        .build()
                        ).retrieve()
                        .bodyToMono(String.class)
                        .block();
                log.info(resp);
            }
        }
    }
}
