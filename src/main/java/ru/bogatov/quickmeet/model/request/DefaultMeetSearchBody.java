package ru.bogatov.quickmeet.model.request;

import ru.bogatov.quickmeet.model.enums.IsAdultFilter;
import ru.bogatov.quickmeet.model.enums.MeetStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public interface DefaultMeetSearchBody {
    List<MeetStatus> getStatuses();
    Set<UUID> getCategories();
    LocalDateTime getDateFrom();
    LocalDateTime getDateTo();
    boolean isNotFull();
    IsAdultFilter getAdultFilter();
}
