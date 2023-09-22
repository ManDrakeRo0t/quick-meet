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
    public final MeetGuestRecordService meetGuestRecordService;

    @Value("${spring.cache.cache-names}")
    String CHAT_VIEWED_CACHE;
    public ChatService(MessageService messageService, SimpMessagingTemplate messagingTemplate, CacheManager cacheManager, MeetGuestRecordService meetGuestRecordService) {
        this.messageService = messageService;
        this.messagingTemplate = messagingTemplate;
        this.cacheManager = cacheManager;
        this.meetGuestRecordService = meetGuestRecordService;
    }

    public void sendMessage(UUID destinationId, UUID senderId, String content) {
        messagingTemplate.convertAndSend(getFetchMessagesDestination(destinationId),
                ChatEventUtils.newMessageEvent(
                        saveMessage(destinationId, senderId, content)
                        )
        );
    }

    public void sendMessageToUserTopics(UUID destinationId, UUID senderId, String content) {
        Set<UUID> userIds = meetGuestRecordService.getMeetUsers(destinationId);
        Message message = saveMessage(destinationId, senderId, content);
        userIds.forEach(userId -> messagingTemplate.convertAndSend(getFetchMessagesDestinationV2(userId),
                ChatEventUtils.newMessageEvent(message)
        ));
    }

    public Message saveMessage(UUID destinationId, UUID senderId, String content) {
        return messageService.saveMessage(destinationId, senderId, content);
    }

    public void sendChatReadEvent(UUID destinationId, UUID senderId) {
        messagingTemplate.convertAndSend(getFetchMessagesDestination(destinationId),
                ChatEventUtils.newReadChatEvent(senderId)
        );
    }

    public void sendChatReadEventToUserTopics(UUID destinationId, UUID senderId) {
        Set<UUID> userIds = meetGuestRecordService.getMeetUsers(destinationId);
        userIds.forEach(userId -> messagingTemplate.convertAndSend(getFetchMessagesDestinationV2(userId),
                ChatEventUtils.newReadChatEvent(senderId, destinationId)
        ));
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
        Set<UUID> userIds = meetGuestRecordService.getMeetUsers(destinationId);
        userIds.forEach(userId -> messagingTemplate.convertAndSend(getFetchMessagesDestinationV2(userId),
                event
        ));
//        messagingTemplate.convertAndSend(getFetchMessagesDestination(destinationId),
//                event);
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
        LocalDateTime lastReadTime = null;
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
                lastReadTime = record.getReadTime();
                messages = messageService.searchWithHistory(chatId, limit, record.getReadTime());
            }
        }
        return ChatMessagesResponse.builder()
                .messages(messages)
                .messageCount(messages == null ? 0 : messages.size())
                .lastReadTime(lastReadTime)
                .build();
    }

    public void handleMessageEventFromMeetTopic(UUID chatId, MessageEvent event) {
        ChatViewedHistory chatHistory = getRecentlyViewedFromCache(chatId);
        LocalDateTime lastSendTime = null;
        LocalDateTime lastReadTime = null;
        Set<ChatViewedRecord> recentlyViewed = new HashSet<>();
        if (chatHistory != null) {
            lastSendTime = chatHistory.getLastMessageSendTime() == null ? null : chatHistory.getLastMessageSendTime();
            recentlyViewed = chatHistory.getRecords() == null ? new HashSet<>() : chatHistory.getRecords();
        }
        if (event.getContent() != null && !event.getContent().isEmpty()) {
            lastSendTime = lastReadTime = LocalDateTime.now();
            sendMessage(chatId, event.getSenderId(), event.getContent());
        } else if (event.isChatViewed()) {
            lastReadTime = LocalDateTime.now();
            sendChatReadEvent(chatId, event.getSenderId());
        }
        replaceOrAdd(recentlyViewed, ChatViewedRecord.builder()
                .userId(event.getSenderId())
                .readTime(lastReadTime)
                .build());
        saveRecentlyViewedInCache(chatId, recentlyViewed, lastSendTime);
    }

    public void handleMessageEventFromUserTopic(UUID userId, MessageEvent event) {
        ChatViewedHistory chatHistory = getRecentlyViewedFromCache(event.getDestinationId());
        LocalDateTime lastSendTime = null;
        LocalDateTime lastReadTime = null;
        Set<ChatViewedRecord> recentlyViewed = new HashSet<>();
        if (chatHistory != null) {
            lastSendTime = chatHistory.getLastMessageSendTime() == null ? null : chatHistory.getLastMessageSendTime();
            recentlyViewed = chatHistory.getRecords() == null ? new HashSet<>() : chatHistory.getRecords();
        }
        if (event.getContent() != null && !event.getContent().isEmpty()) {
            lastSendTime = lastReadTime = LocalDateTime.now();
            sendMessageToUserTopics(event.getDestinationId(), event.getSenderId(), event.getContent());
        } else if (event.isChatViewed()) {
            lastReadTime = LocalDateTime.now();
            sendChatReadEventToUserTopics(event.getDestinationId(), event.getSenderId());
        }
        replaceOrAdd(recentlyViewed, ChatViewedRecord.builder()
                .userId(event.getSenderId())
                .readTime(lastReadTime)
                .build());
        saveRecentlyViewedInCache(event.getDestinationId(), recentlyViewed, lastSendTime);
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

    public static String getFetchMessagesDestinationV2(UUID userId) {
        return TOPIC_DESTINATION_PREFIX + FETCH_MESSAGES_V2.replace(USER_ID, userId.toString());
    }

    public static String createCoreEventMessage(ChatEvent event) {
        return String.format("%s|%s|%s|%s",
                event.getType(),
                event.getEvent().getField(),
                event.getEvent().getNewValue(),
                event.getEvent().isSystemUpdate() //todo save meet id
        );
    }

    public static void replaceOrAdd(Set<ChatViewedRecord> recentlyViewed, ChatViewedRecord record) {
        recentlyViewed.removeIf(existing -> existing.getUserId() != null && existing.getUserId().equals(record.getUserId()));
        recentlyViewed.add(record);
    }
}
