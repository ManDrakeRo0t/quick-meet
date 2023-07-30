package ru.bogatov.quickmeetmessenger.service.util;

import ru.bogatov.quickmeetmessenger.entity.Message;
import ru.bogatov.quickmeetmessenger.model.ChatEvent;
import ru.bogatov.quickmeetmessenger.model.EventType;

import java.util.UUID;

public class ChatEventUtils {
    public static ChatEvent newMessageEvent(Message message) {
        return ChatEvent.builder()
                .message(message)
                .type(EventType.NEW_MESSAGE)
                .build();
    }

    public static ChatEvent newReadChatEvent(UUID senderId) {
        Message message = new Message();
        message.setSenderId(senderId);
        return ChatEvent.builder()
                .message(message)
                .type(EventType.READ_CHAT)
                .build();
    }

}
