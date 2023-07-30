package ru.bogatov.quickmeet.event.rabbitmq;

import lombok.Builder;
import lombok.Data;
import ru.bogatov.quickmeet.model.enums.MeetUpdateEventType;

import java.util.UUID;

@Data
@Builder
public class MeetUpdateEvent {

    MeetUpdateEventType type;
    UUID meetId;
    UUID userId;
    String field;
    String newValue;
    boolean isSystemUpdate;

}
