package ru.bogatov.quickmeetmessenger.model;

import lombok.Data;

import java.util.UUID;

@Data
public class MessageEvent {

    String content;
    UUID senderId;
    UUID destinationId;
    boolean chatViewed;

}
