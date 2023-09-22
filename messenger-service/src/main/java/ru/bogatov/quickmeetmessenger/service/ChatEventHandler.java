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
    private final MeetGuestRecordService meetGuestRecordService;

    public ChatEventHandler(ChatService chatService, MeetGuestRecordService meetGuestRecordService) {
        this.chatService = chatService;
        this.meetGuestRecordService = meetGuestRecordService;
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
                                    .meetId(payload.getMeetId())
                                    .build()
                    )
                    .type(payload.getType())
                    .build();

            switch (event.getType()) {
                case USER_LEFT, USER_DELETED ->
                        meetGuestRecordService.handleUserLeftOrRemovedEvent(payload.getMeetId(), payload.getUserId());
                case USER_JOINED, MEET_CREATED ->
                        meetGuestRecordService.handleUserJoinEvent(payload.getMeetId(), payload.getUserId());
            }

            chatService.sendChatCoreEvent(payload.getMeetId(), event);

        } catch (JsonProcessingException ex) {
            log.error("Error during mapping event : {}", message);
        }

    }

}
