package ru.bogatov.quickmeetmessenger.entity;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class ChatViewedHistory {

    private static final long serialVersionUID = 1227534547L;

    LocalDateTime lastMessageSendTime;

    Set<ChatViewedRecord> records;

}
