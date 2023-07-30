package ru.bogatov.quickmeet.model.request;


import lombok.Data;


import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class MeetUpdateBody implements MeetCommonData {
    private String name;
    private String description;
    private LocalDateTime time;
    private UUID categoryId;
    private Integer expectedDuration;
}
