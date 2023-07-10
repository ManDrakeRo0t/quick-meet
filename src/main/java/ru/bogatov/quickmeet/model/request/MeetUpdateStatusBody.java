package ru.bogatov.quickmeet.model.request;

import lombok.Data;
import ru.bogatov.quickmeet.model.enums.MeetStatus;

import javax.validation.constraints.NotNull;

@Data
public class MeetUpdateStatusBody {
    @NotNull
    private MeetStatus targetState;
}
