package ru.bogatov.quickmeet.model.request;

import lombok.Data;

import java.util.UUID;

@Data
public class CreateLocationBody {
    UUID userId;
    String name;
    String address;
    String description;
    double longevity;
    double latitude;
}
