package ru.bogatov.quickmeet.service.auth;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import ru.bogatov.quickmeet.entity.User;
import ru.bogatov.quickmeet.model.auth.CustomUserDetails;
import ru.bogatov.quickmeet.service.user.UserService;


@Component
public class CustomUserDetailsService implements UserDetailsService {

    UserService userService;

    public CustomUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public CustomUserDetails loadUserByUsername(String phoneNumber) throws UsernameNotFoundException {
        try {
            User user = userService.findUserByPhoneNumber(phoneNumber);
            return CustomUserDetails.fromUserToUserDetails(user);
        }catch (Exception e) {
            return  null;
        }

    }
}