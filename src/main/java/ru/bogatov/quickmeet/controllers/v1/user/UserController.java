package ru.bogatov.quickmeet.controllers.v1.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.bogatov.quickmeet.constants.RouteConstants;
import ru.bogatov.quickmeet.entities.MeetCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(RouteConstants.API_V1 + RouteConstants.USER_MANAGEMENT + RouteConstants.USER)
public class UserController {

    @GetMapping("/{id}")
    public ResponseEntity<MeetCategory> getCategoryById(@PathVariable UUID id) {
        return ResponseEntity.ok(new MeetCategory());
    }

    @GetMapping()
    public ResponseEntity<List<MeetCategory>> getAllCategories() {
        return ResponseEntity.ok(new ArrayList<>());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<MeetCategory> updateCategoryById(@PathVariable UUID id, @RequestBody MeetCategory category) {
        return ResponseEntity.ok(new MeetCategory());
    }

    @PostMapping("")
    public ResponseEntity<MeetCategory> createCategory(@PathVariable UUID id, @RequestBody MeetCategory category) {
        return ResponseEntity.ok(new MeetCategory());
    }


}
