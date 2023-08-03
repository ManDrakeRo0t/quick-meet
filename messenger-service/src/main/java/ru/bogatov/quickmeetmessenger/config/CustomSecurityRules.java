package ru.bogatov.quickmeetmessenger.config;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.bogatov.quickmeetmessenger.model.auth.CustomUserDetails;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
public class CustomSecurityRules {

    private final JwtProvider jwtProvider;

    public CustomSecurityRules(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    public boolean isUserRequestAndMeetApplicable(HttpServletRequest request, UUID chatId, UUID userId) {
        return isUserRequest(userId) && isAllowedByChatId(request.getHeader(AUTHORIZATION).substring(7), chatId.toString());
    }

    public boolean isUserRequest(UUID userId) {
        UUID idFromToken = ((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUserId();
        return idFromToken.equals(userId);
    }

    public boolean isAllowedByChatId(String token, String chatId) {
        return jwtProvider.getAllowedChatsIds(token).contains(chatId);
    }
}
