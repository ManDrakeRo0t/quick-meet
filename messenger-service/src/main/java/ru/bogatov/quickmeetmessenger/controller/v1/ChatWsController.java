package ru.bogatov.quickmeetmessenger.controller.v1;

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
public class ChatWsController {

    private final ChatService chatService;

    public ChatWsController(ChatService chatService) {
        this.chatService = chatService;
    }

    @MessageMapping(SEND_MESSAGE)
    public void sendMessage(@DestinationVariable("chat_id") String chatId, @Payload MessageEvent messageEvent) {
        chatService.handleMessageEvent(UUID.fromString(chatId), messageEvent);
    }

    @SubscribeMapping(FETCH_MESSAGES)
    public ChatEvent fetchMessages(@DestinationVariable("chat_id") String chatId) {
        return null;
    }


}
