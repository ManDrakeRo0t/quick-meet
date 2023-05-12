package ru.bogatov.quickmeet.controllers.v1.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.bogatov.quickmeet.constants.RouteConstants;
import ru.bogatov.quickmeet.entities.UserData;

import java.util.UUID;

@RestController
@RequestMapping(RouteConstants.API_V1 + RouteConstants.USER_MANAGEMENT + RouteConstants.USER_DATA)
public class UserDataController {

    @GetMapping("/{id}")
    public ResponseEntity<UserData> getUserDataById(@PathVariable UUID id) {
        return ResponseEntity.ok(new UserData());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserData> updateUserDataById(@PathVariable UUID id, @RequestBody UserData user) {
        return ResponseEntity.ok(new UserData());
    }

    @PostMapping("")
    public ResponseEntity<UserData> createUserData(@PathVariable UUID id, @RequestBody UserData user) {
        return ResponseEntity.ok(new UserData());
    }

}
