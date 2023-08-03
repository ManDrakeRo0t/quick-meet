package ru.bogatov.quickmeetmessenger.controller.v1;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.bogatov.quickmeetmessenger.model.request.GetChatContentBody;
import ru.bogatov.quickmeetmessenger.model.response.ChatMessagesResponse;
import ru.bogatov.quickmeetmessenger.service.ChatService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static ru.bogatov.quickmeetmessenger.constant.RouteConstant.*;

@RestController
@RequestMapping(API_V1 + CHAT_MANAGEMENT + CHAT)
public class ChatRestController {

    private final ChatService chatService;

    public ChatRestController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PreAuthorize("@customSecurityRules.isUserRequest(#body.getSenderId())")
    @PostMapping("/notifications")
    public ResponseEntity<Map<String, Boolean>> getChatsNotifications(@RequestBody GetChatContentBody body) {
        return ResponseEntity.ok(chatService.getChatsNotifications(body));
    }
    @PreAuthorize("@customSecurityRules.isUserRequestAndMeetApplicable(#request, #id, #senderId)")
    @GetMapping("{id}/messages/{senderId}")
    public ResponseEntity<ChatMessagesResponse> getChatMessages(HttpServletRequest request,
                                                                @PathVariable UUID id,
                                                                @PathVariable UUID senderId,
                                                                @RequestParam(required = false, defaultValue = "20") int limit,
                                                                @RequestParam(required = false, defaultValue = "0") int offset) {
        return ResponseEntity.ok().body(chatService.getChatsMessages(id, senderId, limit, offset));
    }

}
