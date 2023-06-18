package ru.bogatov.quickmeet.services.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bogatov.quickmeet.configs.security.JwtProvider;
import ru.bogatov.quickmeet.constants.AuthConstants;
import ru.bogatov.quickmeet.entities.User;
import ru.bogatov.quickmeet.error.ErrorUtils;
import ru.bogatov.quickmeet.model.enums.ApplicationError;
import ru.bogatov.quickmeet.model.request.LoginForm;
import ru.bogatov.quickmeet.model.request.RegistrationBody;
import ru.bogatov.quickmeet.model.response.AuthenticationResponse;
import ru.bogatov.quickmeet.services.user.UserService;

import java.util.HashMap;
import java.util.Map;


@Service
public class AuthenticationService {

    UserService userService;
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
            throw ErrorUtils.buildException(ApplicationError.AUTHENTICATION_ERROR, ex ,"Error during token generation");
        }
    }

    public Map<String,String> refreshTokens(String token) {

        if (!jwtProvider.validateToken(token)) {
            logger.warn("not able to validate refresh token : "  + token);
            throw ErrorUtils.buildException(ApplicationError.AUTHENTICATION_ERROR, "Not able to validate refresh token");
        }
        String phoneNumber = jwtProvider.getLoginFromToken(token);
        User user = userService.findUserByPhoneNumber(phoneNumber);
        if (user.getRefresh() != null && token.equals(user.getRefresh())) {
            Map<String,String> response = new HashMap<>();
            response.put(AuthConstants.TOKEN,jwtProvider.generateTokenForUser(user));
            response.put(AuthConstants.REFRESH,jwtProvider.generateRefreshForUser(user));
            logger.info("new token for number + " + phoneNumber + " is def [ " + response.get(AuthConstants.TOKEN) + " ]" +
                    "ref : [ " +  response.get(AuthConstants.REFRESH) + " ] ");
            return response;
        }
        logger.warn("refresh tokens not same for user : "
                + phoneNumber + "[ from db = " + user.getRefresh() + " | from req + " + token + " ]");
        throw ErrorUtils.buildException(ApplicationError.AUTHENTICATION_ERROR, "Refresh tokens not same");
    }





}
