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
    @NotNull
    String address;
    @NotNull
    //@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy'T'HH:mm:ss.SSSZ")
    LocalDateTime time;
    int userAmount;
    @NotNull
    UUID ownerId;
    @NotNull
    double latitude;
    @NotNull
    double longevity;
    @NotNull
    private Integer expectedDuration;
    private boolean useRules;
    private double requiredRank;
    private boolean isForAdults;
}
