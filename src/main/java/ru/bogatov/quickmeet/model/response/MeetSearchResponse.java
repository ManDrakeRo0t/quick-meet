package ru.bogatov.quickmeet.model.response;

import lombok.Data;
import ru.bogatov.quickmeet.entity.Location;
import ru.bogatov.quickmeet.entity.Meet;

import java.util.Set;

@Data
public class MeetSearchResponse {

    Set<Meet> meets;

    Set<Location> locations;

}
