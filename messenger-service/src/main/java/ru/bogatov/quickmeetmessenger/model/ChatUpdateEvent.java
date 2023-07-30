package ru.bogatov.quickmeetmessenger.model;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ChatUpdateEvent {
    UUID userId;
    String field;
    String newValue;
    boolean isSystemUpdate;
}
