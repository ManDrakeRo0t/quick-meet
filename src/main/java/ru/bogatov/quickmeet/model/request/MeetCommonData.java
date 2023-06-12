package ru.bogatov.quickmeet.model.request;

import java.time.LocalDateTime;

public interface MeetCommonData {
    String getName();
    String getDescription();
    LocalDateTime getTime();
}
