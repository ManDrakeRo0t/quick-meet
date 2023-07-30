package ru.bogatov.quickmeetmessenger.model;

import lombok.Builder;
import lombok.Data;
import ru.bogatov.quickmeetmessenger.entity.Message;

@Data
@Builder
public class ChatEvent {

    Message message;
    EventType type;
    ChatUpdateEvent event;
}
