package ru.bogatov.quickmeet.entities;

import lombok.Data;
import ru.bogatov.quickmeet.enums.MessageStatus;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

//@Entity
@Data
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private MessageStatus status;

    private String content;

    private Date sentDate;

    private String sender;

    private String chatId;

}
