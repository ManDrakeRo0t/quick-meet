package ru.bogatov.quickmeetmessenger.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.bogatov.quickmeetmessenger.model.ChatEvent;
import ru.bogatov.quickmeetmessenger.model.request.ChatEventCoreRequest;
import ru.bogatov.quickmeetmessenger.model.ChatUpdateEvent;

@Component
@Slf4j
public class ChatEventHandler {

    ObjectMapper mapper = new ObjectMapper();
    private final ChatService chatService;

    public ChatEventHandler(ChatService chatService) {
        this.chatService = chatService;
    }

    public void handleCoreChatEvent(String message) {
        try {
            ChatEventCoreRequest payload = mapper.readValue(message, ChatEventCoreRequest.class);
            ChatEvent event = ChatEvent.builder()
                    .message(null)
                    .event(
                            ChatUpdateEvent.builder()
                                    .field(payload.getField())
                                    .isSystemUpdate(payload.isSystemUpdate())
                                    .newValue(payload.getNewValue())
                                    .userId(payload.getUserId())
                                    .build()
                    )
                    .type(payload.getType())
                    .build();
            chatService.sendChatCoreEvent(payload.getMeetId(), event);
        } catch (JsonProcessingException ex) {
            log.error("Error during mapping event : {}", message);
        }

    }

}
