package ru.bogatov.quickmeet.controllers.v1.meet;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.bogatov.quickmeet.constants.RouteConstants;
import ru.bogatov.quickmeet.entities.Meet;
import ru.bogatov.quickmeet.model.request.MeetCreationBody;
import ru.bogatov.quickmeet.model.request.MeetUpdateBody;
import ru.bogatov.quickmeet.model.request.SearchMeetBody;
import ru.bogatov.quickmeet.services.meet.MeetService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(RouteConstants.API_V1 + RouteConstants.MEET_MANAGEMENT + RouteConstants.MEET)
public class MeetController {

    MeetService meetService;

    public MeetController(MeetService meetService) {
        this.meetService = meetService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Meet> getMeetById(@PathVariable UUID id) {
        return ResponseEntity.ok(meetService.findById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Meet> updateMeetById(@PathVariable UUID id, @RequestBody MeetUpdateBody body) {
        return ResponseEntity.ok(meetService.updateMeet(id, body));
    }

    @PostMapping("")
    public ResponseEntity<Meet> createMeet(@RequestBody MeetCreationBody body) {
        return ResponseEntity.ok(meetService.createNewMeet(body));
    }

    @PostMapping("/search")
    public ResponseEntity<List<Meet>> searchMeet(@RequestBody SearchMeetBody body) {
        return ResponseEntity.ok(meetService.search(body));
    }

    @PostMapping("/{id}/join/{userId}")
    public ResponseEntity<Meet> joinMeet(@PathVariable UUID id, @PathVariable UUID userId) {
        return ResponseEntity.ok(meetService.joinToMeet(id, userId));
    }

    @PostMapping("/{id}/leave/{userId}")
    public ResponseEntity<Meet> leaveMeet(@PathVariable UUID id, @PathVariable UUID userId) {
        return ResponseEntity.ok(meetService.leaveFromMeet(id, userId));
    }

    @PostMapping("/{id}/remove/{userId}")
    public ResponseEntity<Meet> removeUserFromMeet(@PathVariable UUID id, @PathVariable UUID userId) {
        return ResponseEntity.ok(meetService.removeUserFromMeet(id, userId));
    }


}
