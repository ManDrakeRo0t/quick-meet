package ru.bogatov.quickmeetmessenger.service.auth;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import ru.bogatov.quickmeetmessenger.model.auth.CustomUserDetails;

import java.util.UUID;


@Component
public class CustomUserDetailsService implements UserDetailsService {

    @Override
    public CustomUserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        try {
            return CustomUserDetails.fromUserToUserDetails(UUID.fromString(userId));
        }catch (Exception e) {
            return  null;
        }

    }
}
