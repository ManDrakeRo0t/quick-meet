package ru.bogatov.quickmeet.model.request;

import com.sun.istack.NotNull;
import lombok.Data;
import ru.bogatov.quickmeet.model.enums.IsAdultFilter;
import ru.bogatov.quickmeet.model.enums.MeetStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
public class SearchMeetBody {
    @NotNull
    private List<MeetStatus> statuses;
    @NotNull
    private Set<UUID> categories;
    @NotNull
    private double latitude;
    @NotNull
    private double longevity;
    @NotNull
    private double radius;
    @NotNull
    private LocalDateTime dateFrom;
    @NotNull
    private LocalDateTime dateTo;
    @NotNull
    private boolean notFull;
    @NotNull
    private IsAdultFilter adultFilter;
}
