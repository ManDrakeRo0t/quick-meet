package ru.bogatov.quickmeetmessenger.service;

import org.springframework.stereotype.Service;
import ru.bogatov.quickmeetmessenger.entity.Message;
import ru.bogatov.quickmeetmessenger.repository.MessageRepository;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
public class MessageService {

    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public Message saveMessage(UUID destinationId, UUID senderId, String content) {
        Message message = new Message();
        message.setContent(content);
        message.setSenderId(senderId);
        message.setDestinationId(destinationId);
        message.setSendTime(LocalDateTime.now());
        return messageRepository.save(message);
    }

    public Set<Message> defaultSearch(UUID chatId, int limit, int offset) {
        return messageRepository.defaultSearch(chatId, limit, offset);
    }

    public Set<Message> searchWithHistory(UUID chatId, int limit, LocalDateTime readTime) {
        return messageRepository.historySearch(chatId, limit, readTime);
    }
}
