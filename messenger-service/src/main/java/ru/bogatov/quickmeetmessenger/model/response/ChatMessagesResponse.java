package ru.bogatov.quickmeetmessenger.model.response;

import lombok.Builder;
import lombok.Data;
import ru.bogatov.quickmeetmessenger.entity.Message;

import java.time.LocalDateTime;
import java.util.Set;


@Data
@Builder
public class ChatMessagesResponse {

    Set<Message> messages;
    int messageCount;
    LocalDateTime lastReadTime;

}
