package ru.bogatov.quickmeetmessenger.entity;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
@Data
@Builder
public class ChatViewedRecord implements Serializable {

    private static final long serialVersionUID = 122754457547L;

    UUID userId;
    LocalDateTime readTime;
}
