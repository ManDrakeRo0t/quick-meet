package ru.bogatov.quickmeet.controller.v1.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.bogatov.quickmeet.constant.RouteConstants;
import ru.bogatov.quickmeet.entity.Meet;
import ru.bogatov.quickmeet.entity.User;
import ru.bogatov.quickmeet.service.meet.MeetService;
import ru.bogatov.quickmeet.service.user.UserService;

import java.util.*;

@RestController
@RequestMapping(RouteConstants.API_V1 + RouteConstants.USER_MANAGEMENT + RouteConstants.USER)
public class UserController {

    UserService userService;

    MeetService meetService;

    public UserController(UserService userService, MeetService meetService) {
        this.userService = userService;
        this.meetService = meetService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.findUserByID(id));
    }

    @PostMapping("/list")
    public ResponseEntity<List<User>> getUsersByIdsList(@RequestBody Map<String, Set<UUID>> body) {
        return ResponseEntity.ok(userService.findUsersByIdsList(body));
    }

    @GetMapping("/{id}/meet-list/guest")
    public ResponseEntity<List<Meet>> getMeetListWhereUserGuest(@PathVariable UUID id) {
        return ResponseEntity.ok(meetService.findMeetListWhereUserGuest(id));
    }

    @GetMapping("/{id}/meet-list/owner")
    public ResponseEntity<List<Meet>> getMeetListWhereUserOwner(@PathVariable UUID id) {
        return ResponseEntity.ok(meetService.findMeetListWhereUserOwner(id));
    }

}
