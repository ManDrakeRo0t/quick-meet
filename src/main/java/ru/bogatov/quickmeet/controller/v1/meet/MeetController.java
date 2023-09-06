package ru.bogatov.quickmeet.controller.v1.meet;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.bogatov.quickmeet.constant.RouteConstants;
import ru.bogatov.quickmeet.entity.Meet;
import ru.bogatov.quickmeet.model.request.MeetCreationBody;
import ru.bogatov.quickmeet.model.request.MeetUpdateBody;
import ru.bogatov.quickmeet.model.request.MeetUpdateStatusBody;
import ru.bogatov.quickmeet.model.request.SearchMeetBody;
import ru.bogatov.quickmeet.model.response.MeetModificationResponse;
import ru.bogatov.quickmeet.model.response.MeetSearchResponse;
import ru.bogatov.quickmeet.service.meet.MeetService;

import java.util.Set;
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
    @PreAuthorize("@customSecurityRules.isMeetOwnerRequest(#id) || hasAnyAuthority('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<Meet> updateMeetById(@PathVariable UUID id, @RequestBody MeetUpdateBody body) {
        return ResponseEntity.ok(meetService.updateMeet(id, body));
    }

    @PreAuthorize("@customSecurityRules.isMeetOwnerRequest(#id) || hasAnyAuthority('ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<Meet> updateMeetStatusById(@PathVariable UUID id, @RequestBody MeetUpdateStatusBody body) {
        return ResponseEntity.ok(meetService.updateMeetStatus(id, body, false));
    }

    @PostMapping("")
    @PreAuthorize("@customSecurityRules.isUserRequest(#body.ownerId) || hasAnyAuthority('ADMIN')")
    public ResponseEntity<MeetModificationResponse> createMeet(@RequestBody MeetCreationBody body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(meetService.createNewMeet(body));
    }

    @PreAuthorize("@customSecurityRules.isMeetOwnerRequest(#id) || hasAnyAuthority('ADMIN')")
    @PostMapping(path = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Meet> updateMeetAvatar(@PathVariable UUID id, @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(meetService.updateMeetAvatar(id, file));
    }

    @PreAuthorize("@customSecurityRules.isMeetOwnerRequest(#id) || hasAnyAuthority('ADMIN')")
    @DeleteMapping(path = "/{id}/avatar")
    public ResponseEntity<Meet> deleteMeetAvatar(@PathVariable UUID id) {
        return ResponseEntity.ok(meetService.deleteMeetAvatar(id));
    }

    @PostMapping("/search")
    public ResponseEntity<MeetSearchResponse> searchMeet(@RequestBody SearchMeetBody body) {
        return ResponseEntity.ok(meetService.search(body));
    }
    @PreAuthorize("@customSecurityRules.isUserRequest(#userId) || hasAnyAuthority('ADMIN')")
    @PostMapping("/{id}/join/{userId}")
    public ResponseEntity<MeetModificationResponse> joinMeet(@PathVariable UUID id, @PathVariable UUID userId) {
        return ResponseEntity.ok(meetService.joinToMeet(id, userId));
    }
    @PreAuthorize("@customSecurityRules.isUserRequest(#userId) || hasAnyAuthority('ADMIN')")
    @PostMapping("/{id}/leave/{userId}")
    public ResponseEntity<MeetModificationResponse> leaveMeet(@PathVariable UUID id, @PathVariable UUID userId) {
        return ResponseEntity.ok(meetService.leaveFromMeet(id, userId));
    }
    @PreAuthorize("@customSecurityRules.isMeetOwnerRequest(#id) || hasAnyAuthority('ADMIN')")
    @PatchMapping("/{id}/guest/{guestId}")
    public ResponseEntity<Void> updateGuestStatus(@PathVariable UUID id, @PathVariable UUID guestId, @RequestParam("attend") boolean isAttend) {
        return ResponseEntity.ok(meetService.updateGuest(id, guestId, isAttend));
    }
    @PreAuthorize("@customSecurityRules.isMeetOwnerRequest(#id) || hasAnyAuthority('ADMIN')")
    @PatchMapping("/{id}/owner/status")
    public ResponseEntity<Void> updateOwnerStatus(@PathVariable UUID id, @RequestParam("attend") boolean isAttend) {
        return ResponseEntity.ok(meetService.updateOwner(id, isAttend));
    }
    @PreAuthorize("@customSecurityRules.isMeetOwnerRequest(#id) || hasAnyAuthority('ADMIN')")
    @PostMapping("/{id}/remove/{userId}")
    public ResponseEntity<MeetModificationResponse> removeUserFromMeet(@PathVariable UUID id, @PathVariable UUID userId) {
        return ResponseEntity.ok(meetService.removeUserFromMeet(id, userId));
    }

}
