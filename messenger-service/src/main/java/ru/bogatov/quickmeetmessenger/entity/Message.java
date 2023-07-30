package ru.bogatov.quickmeetmessenger.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@Table(name = "message", indexes = @Index(name = "destination_index", columnList = "destination_id"))
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "destination_id")
    private UUID destinationId;

    @Column(name = "sender_id")
    private UUID senderId;

    @Column(name = "content")
    private String content;

    @Column(name = "send_time")
    private LocalDateTime sendTime;

}
