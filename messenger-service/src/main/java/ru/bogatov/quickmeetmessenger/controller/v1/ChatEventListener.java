package ru.bogatov.quickmeetmessenger.controller.v1;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ru.bogatov.quickmeetmessenger.service.ChatEventHandler;

import static ru.bogatov.quickmeetmessenger.config.RabbitMqConfig.MEET_UPDATE_EVENT_QUEUE;

@Component
@Slf4j
public class ChatEventListener {

    private final ChatEventHandler chatEventHandler;

    public ChatEventListener(ChatEventHandler chatEventHandler) {
        this.chatEventHandler = chatEventHandler;
    }

    @RabbitListener(queues = MEET_UPDATE_EVENT_QUEUE)
    public void receiveMessage(String message) {
        log.debug("received : {}" , message);
        chatEventHandler.handleCoreChatEvent(message);
    }

}
