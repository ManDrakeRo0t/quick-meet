package ru.bogatov.quickmeet.model.response;

import lombok.Builder;
import lombok.Data;
import ru.bogatov.quickmeet.entity.Meet;

@Data
@Builder
public class MeetModificationResponse {
    Meet meet;
    String token;
}
