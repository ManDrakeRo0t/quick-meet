package ru.bogatov.quickmeet.model.request;

import lombok.Data;
import ru.bogatov.quickmeet.model.enums.MeetStatus;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
public class SearchMeetBody {

    private List<MeetStatus> statuses;
    private Set<UUID> categories;
    private double latitude;
    private double longevity;
    private double radius;

}
