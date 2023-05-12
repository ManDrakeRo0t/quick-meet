package ru.bogatov.quickmeet.entities;

import lombok.Data;
import ru.bogatov.quickmeet.enums.MeetStatus;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

//@Entity
@Data
public class Meet {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String name;

    private String description;

    private Date date;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User owner;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "city_id")
    private City city;

    private int maxPeople;

    private int currentPeople;

    private float latitude;

    private float longevity;

    private float rank;

    @ManyToMany
    private Set<User> guests;

    @Enumerated(EnumType.STRING)
    private MeetStatus meetStatus;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private MeetCategory category;
}
