package ru.bogatov.quickmeet.controller.v1.meet;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.bogatov.quickmeet.constant.RouteConstants;
import ru.bogatov.quickmeet.entity.Meet;
import ru.bogatov.quickmeet.model.request.MeetCreationBody;
import ru.bogatov.quickmeet.model.request.MeetUpdateBody;
import ru.bogatov.quickmeet.model.request.SearchMeetBody;
import ru.bogatov.quickmeet.model.response.MeetModificationResponse;
import ru.bogatov.quickmeet.service.meet.MeetService;

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
    public ResponseEntity<MeetModificationResponse> createMeet(@RequestBody MeetCreationBody body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(meetService.createNewMeet(body));
    }

    @PostMapping("/search")
    public ResponseEntity<List<Meet>> searchMeet(@RequestBody SearchMeetBody body) {
        return ResponseEntity.ok(meetService.search(body));
    }

    @PostMapping("/{id}/join/{userId}")
    public ResponseEntity<MeetModificationResponse> joinMeet(@PathVariable UUID id, @PathVariable UUID userId) {
        return ResponseEntity.ok(meetService.joinToMeet(id, userId));
    }

    @PostMapping("/{id}/leave/{userId}")
    public ResponseEntity<MeetModificationResponse> leaveMeet(@PathVariable UUID id, @PathVariable UUID userId) {
        return ResponseEntity.ok(meetService.leaveFromMeet(id, userId));
    }

    @PatchMapping("/{id}/guest/{guestId}")
    public ResponseEntity<Void> updateGuestStatus(@PathVariable UUID id, @PathVariable UUID guestId, @RequestParam("attend") boolean isAttend) {
        return ResponseEntity.ok(meetService.updateGuest(id, guestId, isAttend));
    }

    @PostMapping("/{id}/remove/{userId}")
    public ResponseEntity<MeetModificationResponse> removeUserFromMeet(@PathVariable UUID id, @PathVariable UUID userId) {
        return ResponseEntity.ok(meetService.removeUserFromMeet(id, userId));
    }

}
