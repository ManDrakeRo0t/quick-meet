package ru.bogatov.quickmeet.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Data
@Table(name = "meet_category")
public class MeetCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "name", length = 20)
    private String name;

    @Column(name = "is_hidden")
    private boolean isHidden;


}
