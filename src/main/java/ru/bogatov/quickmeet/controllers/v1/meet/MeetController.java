package ru.bogatov.quickmeet.controllers.v1.meet;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.bogatov.quickmeet.constants.RouteConstants;
import ru.bogatov.quickmeet.entities.Meet;
import ru.bogatov.quickmeet.request.SearchMeetBody;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(RouteConstants.API_V1 + RouteConstants.MEET_MANAGEMENT + RouteConstants.MEET)
public class MeetController {

    @GetMapping("/{id}")
    public ResponseEntity<Meet> getMeetById(@PathVariable UUID id) {
        return ResponseEntity.ok(new Meet());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Meet> updateMeetById(@PathVariable UUID id, @RequestBody Meet user) {
        return ResponseEntity.ok(new Meet());
    }

    @PostMapping("")
    public ResponseEntity<Meet> createMeet(@PathVariable UUID id, @RequestBody Meet user) {
        return ResponseEntity.ok(new Meet());
    }

    @PostMapping("/search")
    public ResponseEntity<List<Meet>> searchMeet(@RequestBody SearchMeetBody body) {
        return ResponseEntity.ok(new ArrayList<>());
    }

    @PostMapping("/{id}/join/{userId}")
    public ResponseEntity<Meet> joinMeet(@PathVariable UUID id, @PathVariable UUID userId) {
        return ResponseEntity.ok(new Meet());
    }

    @PostMapping("/{id}/leave/{userId}")
    public ResponseEntity<Meet> leaveMeet(@PathVariable UUID id, @PathVariable UUID userId) {
        return ResponseEntity.ok(new Meet());
    }


}
