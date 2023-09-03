package ru.bogatov.quickmeet.config.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.bogatov.quickmeet.entity.Location;
import ru.bogatov.quickmeet.entity.Meet;
import ru.bogatov.quickmeet.model.auth.CustomUserDetails;
import ru.bogatov.quickmeet.service.meet.LocationService;
import ru.bogatov.quickmeet.service.meet.MeetService;

import java.util.UUID;

@Component
public class CustomSecurityRules {

    private final MeetService meetService;

    private final LocationService locationService;

    public CustomSecurityRules(MeetService meetService, LocationService locationService) {
        this.meetService = meetService;
        this.locationService = locationService;
    }

    public boolean isUserRequest(UUID userId) {
        UUID idFromToken = ((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUserId();
        return idFromToken.equals(userId);
    }

    public boolean isMeetOwnerRequest(UUID meetId) {
        UUID idFromToken = ((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUserId();
        Meet meet = meetService.findById(meetId);
        return idFromToken.equals(meet.getOwner().getId());
    }

    public boolean isLocationOwnerRequest(UUID locationId) {
        UUID idFromToken = ((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUserId();
        Location location = locationService.getLocationById(locationId);
        return idFromToken.equals(location.getOwner().getId());
    }

}
