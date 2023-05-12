package ru.bogatov.quickmeet.controllers.v1.messanger;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.bogatov.quickmeet.constants.RouteConstants;
import ru.bogatov.quickmeet.response.Chat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(RouteConstants.API_V1 + RouteConstants.MESSENGER + RouteConstants.CHAT)
public class ChatController {

    @GetMapping("/user/{id}")
    public ResponseEntity<List<Chat>> getUserChats(@PathVariable UUID id) {
        return ResponseEntity.ok(new ArrayList<>());
    }


}
