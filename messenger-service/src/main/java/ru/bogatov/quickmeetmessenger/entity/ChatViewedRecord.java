package ru.bogatov.quickmeetmessenger.entity;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;
@Data
@Builder
public class ChatViewedRecord {

    private static final long serialVersionUID = 122754457547L;

    UUID userId;
    LocalDateTime readTime;
}
