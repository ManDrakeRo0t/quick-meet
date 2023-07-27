package ru.bogatov.quickmeetmessenger.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import ru.bogatov.quickmeetmessenger.entity.ChatViewedHistory;
import ru.bogatov.quickmeetmessenger.entity.ChatViewedRecord;
import ru.bogatov.quickmeetmessenger.entity.Message;
import ru.bogatov.quickmeetmessenger.model.ChatEvent;
import ru.bogatov.quickmeetmessenger.model.request.GetChatContentBody;
import ru.bogatov.quickmeetmessenger.model.MessageEvent;
import ru.bogatov.quickmeetmessenger.model.response.ChatMessagesResponse;
import ru.bogatov.quickmeetmessenger.service.util.ChatEventUtils;

import java.time.LocalDateTime;
import java.util.*;

import static ru.bogatov.quickmeetmessenger.constant.RouteConstant.*;

@Service
@Slf4j
public class ChatService {
    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final CacheManager cacheManager;

    @Value("${spring.cache.cache-names}")
    String CHAT_VIEWED_CACHE;
    public ChatService(MessageService messageService, SimpMessagingTemplate messagingTemplate, CacheManager cacheManager) {
        this.messageService = messageService;
        this.messagingTemplate = messagingTemplate;
        this.cacheManager = cacheManager;
    }

    public void sendMessage(UUID destinationId, UUID senderId, String content) {
        messagingTemplate.convertAndSend(getFetchMessagesDestination(destinationId),
                ChatEventUtils.newMessageEvent(
                        saveMessage(destinationId, senderId, content)
                        )
        );
    }

    public Message saveMessage(UUID destinationId, UUID senderId, String content) {
        return messageService.saveMessage(destinationId, senderId, content);
    }

    public void sendChatReadEvent(UUID destinationId, UUID senderId) {
        messagingTemplate.convertAndSend(getFetchMessagesDestination(destinationId),
                ChatEventUtils.newReadChatEvent(senderId)
        );
    }

    public void sendChatCoreEvent(UUID destinationId, ChatEvent event) {
        ChatViewedHistory history = getRecentlyViewedFromCache(destinationId);
        LocalDateTime now = LocalDateTime.now();
        if (history == null) {
            saveRecentlyViewedInCache(destinationId, new HashSet<>(), now);
        } else {
            saveRecentlyViewedInCache(destinationId, history.getRecords(), now);
        }
        saveMessage(destinationId, null, createCoreEventMessage(event));
        messagingTemplate.convertAndSend(getFetchMessagesDestination(destinationId),
                event);
    }

    public Map<String, Boolean> getChatsNotifications(GetChatContentBody body) {
        Map<String, Boolean> response = new HashMap<>();
        Cache cache = cacheManager.getCache(CHAT_VIEWED_CACHE);
        for (UUID chatId : body.getChatIds()) {
            ChatViewedHistory history = ChatViewedHistory.builder().build();
            if (cache != null) {
                ChatViewedHistory fromCache = cache.get(chatId, ChatViewedHistory.class);
                if (fromCache != null) {
                    history = fromCache;
                }
            }
            if (history.getLastMessageSendTime() != null && history.getRecords() != null && !history.getRecords().isEmpty()) {
                boolean notificationExists = true;
                ChatViewedRecord record = history.getRecords()
                        .stream()
                        .filter(rec -> rec.getUserId().equals(body.getSenderId()))
                        .findFirst().orElse(null);
                if (record != null) {
                    notificationExists = record.getReadTime().isBefore(history.getLastMessageSendTime());
                }
                response.put(chatId.toString(), notificationExists);
            } else {
                response.put(chatId.toString(), true);
            }
        }
        return response;
    }

    public ChatMessagesResponse getChatsMessages(UUID chatId, UUID senderId, int limit, int offset) {
        Set<Message> messages = null;
        if (offset != 0) {
            //default search
            messages = messageService.defaultSearch(chatId, limit, offset);
        } else {
            //search with history
            ChatViewedHistory chatHistory = getRecentlyViewedFromCache(chatId);
            ChatViewedRecord record = chatHistory.getRecords().stream()
                    .filter(rec -> rec.getUserId().equals(senderId)).findFirst().orElse(null);
            if (record == null) {
                messages = messageService.defaultSearch(chatId, limit, offset);
            } else {
                messages = messageService.searchWithHistory(chatId, limit, record.getReadTime());
            }
        }
        return ChatMessagesResponse.builder()
                .messages(messages)
                .messageCount(messages == null ? 0 : messages.size())
                .build();
    }

    public void handleMessageEvent(UUID chatId, MessageEvent event) {
        ChatViewedHistory chatHistory = getRecentlyViewedFromCache(chatId);
        LocalDateTime lastSendTime = null;
        Set<ChatViewedRecord> recentlyViewed = new HashSet<>();
        if (chatHistory != null) {
            lastSendTime = chatHistory.getLastMessageSendTime() == null ? null : chatHistory.getLastMessageSendTime();
            recentlyViewed = chatHistory.getRecords() == null ? new HashSet<>() : chatHistory.getRecords();
        }
        if (event.getContent() != null && !event.getContent().isEmpty()) {
            lastSendTime = LocalDateTime.now();
            sendMessage(chatId, event.getSenderId(), event.getContent());
        } else if (event.isChatViewed()) {
            sendChatReadEvent(chatId, event.getSenderId());
        }
        replaceOrAdd(recentlyViewed, ChatViewedRecord.builder()
                .userId(event.getSenderId())
                .readTime(lastSendTime)
                .build());
        saveRecentlyViewedInCache(chatId, recentlyViewed, lastSendTime);
    }

    public void saveRecentlyViewedInCache(UUID chatId, Set<ChatViewedRecord> recentlyViewed, LocalDateTime time) {
        Cache cache = cacheManager.getCache(CHAT_VIEWED_CACHE);
        if (cache == null) {
            log.error("cache {} is null", CHAT_VIEWED_CACHE);
            return;
        }
        cache.put(chatId, ChatViewedHistory.builder()
                .lastMessageSendTime(time)
                .records(recentlyViewed)
                .build()
        );
    }

    public ChatViewedHistory getRecentlyViewedFromCache(UUID chatId) {
        Cache cache = cacheManager.getCache(CHAT_VIEWED_CACHE);
        if (cache == null) {
            log.error("cache {} is null", CHAT_VIEWED_CACHE);
            return null;
        }
        return cache.get(chatId, ChatViewedHistory.class);
    }

    public static String getFetchMessagesDestination(UUID chatId) {
        return TOPIC_DESTINATION_PREFIX + FETCH_MESSAGES.replace(CHAT_ID, chatId.toString());
    }

    public static String createCoreEventMessage(ChatEvent event) {
        return String.format("%s|%s|%s|%s",
                event.getType(),
                event.getEvent().getField(),
                event.getEvent().getNewValue(),
                event.getEvent().isSystemUpdate()
        );
    }

    public static void replaceOrAdd(Set<ChatViewedRecord> recentlyViewed, ChatViewedRecord record) {
        recentlyViewed.removeIf(existing -> existing.getUserId().equals(record.getUserId()));
        recentlyViewed.add(record);
    }
}
