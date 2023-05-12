package ru.bogatov.quickmeet.request;

import lombok.Data;

import java.util.Set;

@Data
public class SearchMeetBody {

    private Set<String> categories;
}
