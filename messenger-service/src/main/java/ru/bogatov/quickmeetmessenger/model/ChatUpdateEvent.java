package ru.bogatov.quickmeetmessenger.model;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ChatUpdateEvent {
    UUID userId;
    UUID meetId;
    String field;
    String newValue;
    boolean isSystemUpdate;
}
