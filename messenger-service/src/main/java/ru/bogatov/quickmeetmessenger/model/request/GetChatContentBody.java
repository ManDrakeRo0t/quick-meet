package ru.bogatov.quickmeetmessenger.model.request;

import lombok.Data;

import java.util.Set;
import java.util.UUID;
@Data
public class GetChatContentBody {
    Set<UUID> chatIds;
    UUID senderId;
}
