package ru.bogatov.quickmeet.service.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bogatov.quickmeet.config.security.JwtProvider;
import ru.bogatov.quickmeet.constant.AuthConstants;
import ru.bogatov.quickmeet.entity.User;
import ru.bogatov.quickmeet.entity.auth.UserForAuth;
import ru.bogatov.quickmeet.error.ErrorUtils;
import ru.bogatov.quickmeet.model.enums.ApplicationError;
import ru.bogatov.quickmeet.model.request.LoginForm;
import ru.bogatov.quickmeet.model.request.RegistrationBody;
import ru.bogatov.quickmeet.model.response.AuthenticationResponse;
import ru.bogatov.quickmeet.service.user.UserService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Service
public class AuthenticationService {

    private final UserService userService;
    private final JwtProvider jwtProvider;
    private final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    public AuthenticationService(UserService userService, JwtProvider jwtProvider) {
        this.userService = userService;
        this.jwtProvider = jwtProvider;
    }
    @Transactional
    public AuthenticationResponse register(RegistrationBody body) {
        User user = userService.createUser(body);
        return AuthenticationResponse.builder()
                .user(user)
                .payload(getTokens(user))
                .build();
    }

    public AuthenticationResponse refreshTokenPair(String refreshToken) {
        return AuthenticationResponse.builder()
                .payload(refreshTokens(refreshToken))
                .build();
    }

    public AuthenticationResponse login(LoginForm body) {
        User user = userService.findActiveAndAvailableUserByPhoneAndPassword(body);
        return AuthenticationResponse.builder()
                .user(user)
                .payload(getTokens(user))
                .build();
    }

    public Map<String,String> getTokens(User user) {
        try {
            String refresh = jwtProvider.generateRefreshForUser(user);
            String token = jwtProvider.generateTokenForUser(user);
            Map<String,String> response = new HashMap<>();
            response.put(AuthConstants.TOKEN,token);
            response.put(AuthConstants.REFRESH,refresh);
            return response;
        } catch (Exception ex) {
            throw ErrorUtils.buildException(ApplicationError.COMMON_ERROR, ex ,"Error during token generation");
        }
    }

    public Map<String,String> refreshTokens(String token) {

        if (!jwtProvider.validateToken(token)) {
            logger.warn("not able to validate refresh token : "  + token);
            throw ErrorUtils.buildException(ApplicationError.AUTHENTICATION_ERROR, "Not able to validate refresh token");
        }
        String userId = jwtProvider.getUserIdFromToken(token);
        UserForAuth user = userService.findUserForAuthById(UUID.fromString(userId));
        if (user.getRefresh() != null && token.equals(user.getRefresh())) {
            Map<String,String> response = new HashMap<>();
            response.put(AuthConstants.TOKEN,jwtProvider.generateTokenForUser(user));
            response.put(AuthConstants.REFRESH,jwtProvider.generateRefreshForUser(user));

            return response;
        }
        throw ErrorUtils.buildException(ApplicationError.AUTHENTICATION_ERROR, "Refresh tokens not same");
    }





}
