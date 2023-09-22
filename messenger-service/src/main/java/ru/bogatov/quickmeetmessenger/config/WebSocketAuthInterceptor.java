package ru.bogatov.quickmeetmessenger.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.messaging.simp.stomp.StompHeaders.DESTINATION;
import static ru.bogatov.quickmeetmessenger.constant.AuthConstant.BEARER;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtProvider jwtProvider;
    public WebSocketAuthInterceptor(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        StompCommand command = accessor.getCommand();
        String token = null;
        if (StompCommand.CONNECT == command) {
            final var requestTokenHeader = accessor.getFirstNativeHeader(AUTHORIZATION);
            if (requestTokenHeader != null && requestTokenHeader.startsWith(BEARER)) {
                token = requestTokenHeader.substring(7);
            }
            if (token == null || !jwtProvider.validateToken(token)){
                return null;
            }
        }
        if (StompCommand.SUBSCRIBE == command || StompCommand.SEND == command) {
            final String requestTokenHeader = accessor.getFirstNativeHeader(AUTHORIZATION);
            final String destination = accessor.getFirstNativeHeader(DESTINATION);
            if (requestTokenHeader != null && requestTokenHeader.startsWith(BEARER)) {
                token = requestTokenHeader.substring(7);
            }
            if (token == null || !jwtProvider.validateToken(token)) {
                //|| !isAllowedByTopicId(token, destination)
                return null;
            }
        }
        return message;
    }

    public boolean isAllowedByTopicId(String token, String destinationTopic) {
        String chatId = destinationTopic.substring(13, 49);
        return jwtProvider.getAllowedChatsIds(token).contains(chatId);
    }

}
