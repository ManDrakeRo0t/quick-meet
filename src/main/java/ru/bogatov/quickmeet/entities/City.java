package ru.bogatov.quickmeet.entities;

import lombok.Data;

import javax.persistence.*;
import java.util.UUID;

@Data
@Entity
public class City {

    @Id
    private UUID id;

    private String name;

}
