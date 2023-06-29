package ru.bogatov.quickmeet.model.auth;

import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.bogatov.quickmeet.entity.User;
import ru.bogatov.quickmeet.entity.auth.UserForAuth;
import ru.bogatov.quickmeet.model.enums.Role;

import java.util.Collection;
import java.util.Set;
@Data
@Builder
public class CustomUserDetails implements UserDetails {
    private String password;
    private String phoneNumber;
    private boolean isActive;
    private Set<Role> roleSet;
    private boolean isBlocked;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roleSet;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.phoneNumber;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !isBlocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.isActive;
    }

    public static CustomUserDetails fromUserToUserDetails(UserForAuth user) {
        return CustomUserDetails.builder()
                .isActive(user.isActive())
                .isBlocked(user.isBlocked())
                .password(user.getPassword())
                .phoneNumber(user.getPhoneNumber())
                .roleSet(Set.of(user.getRole()))
                .build();
    }
}
