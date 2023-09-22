package ru.bogatov.quickmeetmessenger.controller.v2;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import ru.bogatov.quickmeetmessenger.model.ChatEvent;
import ru.bogatov.quickmeetmessenger.model.MessageEvent;
import ru.bogatov.quickmeetmessenger.service.ChatService;

import java.util.UUID;

import static ru.bogatov.quickmeetmessenger.constant.RouteConstant.*;

@Controller
@Slf4j
@AllArgsConstructor
public class ChatWsControllerV2 {

    private final ChatService chatService;

    @MessageMapping(SEND_MESSAGE_V2)
    public void sendMessage(@DestinationVariable("user_id") String userId, @Payload MessageEvent messageEvent) {
        chatService.handleMessageEventFromUserTopic(UUID.fromString(userId), messageEvent);
    }

    @SubscribeMapping(FETCH_MESSAGES_V2)
    public ChatEvent fetchMessages(@DestinationVariable("user_id") String userId) {
        return null;
    }
}
