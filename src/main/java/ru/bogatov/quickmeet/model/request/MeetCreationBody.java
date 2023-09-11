package ru.bogatov.quickmeet.model.request;

import com.sun.istack.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class MeetCreationBody implements MeetCommonData {
    @NotNull
    String name;
    @NotNull
    String description;
    @NotNull
    UUID categoryId;

    String address;
    @NotNull
    //@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy'T'HH:mm:ss.SSSZ")
    LocalDateTime time;
    int userAmount;
    @NotNull
    UUID ownerId;
    double latitude;
    double longevity;
    @NotNull
    private Integer expectedDuration;
    private boolean skipRules;
    private double requiredRank;
    private boolean forAdults;
    private UUID locationId;
}
