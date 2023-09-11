package ru.bogatov.quickmeet.controller.v1.user;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.bogatov.quickmeet.constant.RouteConstants;
import ru.bogatov.quickmeet.entity.Location;
import ru.bogatov.quickmeet.entity.Meet;
import ru.bogatov.quickmeet.entity.User;
import ru.bogatov.quickmeet.model.request.UserUpdateBody;
import ru.bogatov.quickmeet.service.meet.LocationCacheService;
import ru.bogatov.quickmeet.service.meet.LocationService;
import ru.bogatov.quickmeet.service.meet.MeetService;
import ru.bogatov.quickmeet.service.user.UserService;

import java.util.*;

@RestController
@RequestMapping(RouteConstants.API_V1 + RouteConstants.USER_MANAGEMENT + RouteConstants.USER)
public class UserController {

    private final UserService userService;
    private final MeetService meetService;
    private final LocationService locationService;
    private final LocationCacheService locationCacheService;

    public UserController(UserService userService, MeetService meetService, LocationService locationService, LocationCacheService locationCacheService) {
        this.userService = userService;
        this.meetService = meetService;
        this.locationService = locationService;
        this.locationCacheService = locationCacheService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.findUserByID(id));
    }

    @PreAuthorize("@customSecurityRules.isUserRequest(#id) || hasAnyAuthority('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable UUID id, @RequestBody UserUpdateBody body) {
        return ResponseEntity.ok(userService.updateUser(id, body));
    }

    @PreAuthorize("@customSecurityRules.isUserRequest(#id) || hasAnyAuthority('ADMIN')")
    @PostMapping(path = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<User> updateUserAvatar(@PathVariable UUID id, @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(userService.updateUserAvatar(id, file));
    }

    @PreAuthorize("@customSecurityRules.isUserRequest(#id) || hasAnyAuthority('ADMIN')")
    @DeleteMapping(path = "/{id}/avatar")
    public ResponseEntity<User> deleteUserAvatar(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.deleteUserAvatar(id));
    }

    @PostMapping("/list")
    public ResponseEntity<List<User>> getUsersByIdsList(@RequestBody Map<String, Set<UUID>> body) {
        return ResponseEntity.ok(userService.findUsersByIdsList(body));
    }

    @PreAuthorize("@customSecurityRules.isUserRequest(#id) || hasAnyAuthority('ADMIN')")
    @GetMapping("/{id}/meet-list/guest")
    public ResponseEntity<Set<Meet>> getMeetListWhereUserGuest(@PathVariable UUID id) {
        return ResponseEntity.ok(meetService.findMeetListWhereUserGuest(id));
    }

    @PreAuthorize("@customSecurityRules.isUserRequest(#id) || hasAnyAuthority('ADMIN')")
    @GetMapping("/{id}/meet-list/owner")
    public ResponseEntity<Set<Meet>> getMeetListWhereUserOwner(@PathVariable UUID id) {
        return ResponseEntity.ok(meetService.findMeetListWhereUserOwner(id));
    }

    @PreAuthorize("@customSecurityRules.isUserRequest(#id) || hasAnyAuthority('ADMIN')")
    @GetMapping("/{id}/locations")
    public ResponseEntity<Set<Location>> getUserLocations(@PathVariable UUID id) {
        return ResponseEntity.ok(locationCacheService.findUserLocations(id));
    }

}
