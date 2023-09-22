package ru.bogatov.quickmeetmessenger.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@Table(name = "guest_record", indexes = @Index(name = "meet_index", columnList = "meet_id"))
public class MeetGuestRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "meet_id")
    private UUID meetId;

    @Column(name = "user_id")
    private UUID userId;
}
