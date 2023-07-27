package ru.bogatov.quickmeetmessenger.controller.v1;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.bogatov.quickmeetmessenger.model.request.GetChatContentBody;
import ru.bogatov.quickmeetmessenger.model.response.ChatMessagesResponse;
import ru.bogatov.quickmeetmessenger.service.ChatService;

import java.util.List;
import java.util.Map;

import static ru.bogatov.quickmeetmessenger.constant.RouteConstant.*;

@RestController
@RequestMapping(API_V1 + CHAT_MANAGEMENT + CHAT)
public class ChatRestController {

    private final ChatService chatService;

    public ChatRestController(ChatService chatService) {
        this.chatService = chatService;
    }
    @PostMapping("/notifications")
    public ResponseEntity<Map<String, Boolean>> getChatsNotifications(@RequestBody GetChatContentBody body) {
        return ResponseEntity.ok(chatService.getChatsNotifications(body));
    }

    @GetMapping("{id}/messages/{senderId}")
    public ResponseEntity<ChatMessagesResponse> getChatMessages(@PathVariable String id,
                                                                @PathVariable String senderId,
                                                                @RequestParam(required = false, defaultValue = "20") int limit,
                                                                @RequestParam(required = false, defaultValue = "0") int offset) {
        return ResponseEntity.ok().body(null);
    }

}
