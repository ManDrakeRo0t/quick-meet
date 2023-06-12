package ru.bogatov.quickmeet.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.util.Set;
import java.util.UUID;

@Entity
@Data
public class MeetCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String name;

    private boolean isHidden;

    @OneToMany
    @JsonIgnore
    Set<Meet> meetSet;

}
