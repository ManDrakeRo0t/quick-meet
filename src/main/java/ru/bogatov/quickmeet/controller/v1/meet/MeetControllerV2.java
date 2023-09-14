package ru.bogatov.quickmeet.controller.v1.meet;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.bogatov.quickmeet.constant.RouteConstants;
import ru.bogatov.quickmeet.model.request.SearchMeetBodyV2;
import ru.bogatov.quickmeet.model.response.MeetSearchResponse;
import ru.bogatov.quickmeet.service.meet.MeetService;

@RestController
@RequestMapping(RouteConstants.API_V2 + RouteConstants.MEET_MANAGEMENT + RouteConstants.MEET)
@AllArgsConstructor
public class MeetControllerV2 {

    MeetService meetService;

    @PostMapping("/search")
    public ResponseEntity<MeetSearchResponse> searchMeet(@RequestBody SearchMeetBodyV2 body) {
        return ResponseEntity.ok(meetService.searchWithCoords(body));
    }

}
