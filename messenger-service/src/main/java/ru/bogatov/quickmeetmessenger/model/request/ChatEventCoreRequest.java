package ru.bogatov.quickmeetmessenger.model.request;

import lombok.Data;
import ru.bogatov.quickmeetmessenger.model.EventType;

import java.util.UUID;

@Data
public class ChatEventCoreRequest {
    EventType type;
    UUID meetId;
    UUID userId;
    String field;
    String newValue;
    boolean isSystemUpdate;
}
