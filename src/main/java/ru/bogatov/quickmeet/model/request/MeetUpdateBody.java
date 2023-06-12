package ru.bogatov.quickmeet.model.request;

import com.sun.istack.NotNull;
import lombok.Data;
import ru.bogatov.quickmeet.model.enums.MeetStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class MeetUpdateBody implements MeetCommonData {
    private String name;
    private String description;
    private LocalDateTime time;
    private UUID categoryId;
    private MeetStatus status;
}
