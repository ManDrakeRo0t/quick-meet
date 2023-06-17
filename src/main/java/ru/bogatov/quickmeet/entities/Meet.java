package ru.bogatov.quickmeet.entities;

import lombok.Data;
import org.hibernate.annotations.Type;
import ru.bogatov.quickmeet.model.enums.MeetStatus;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Data
public class Meet {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String name;

    private String description;

    private LocalDateTime dateTime;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User owner;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "city_id")
    private City city;

    private int maxPeople;

    private int currentPeople;

    private double latitude;

    private double longevity;

    private float rank;


    @ManyToMany(fetch = FetchType.LAZY)
    private Set<User> guests;

    @ElementCollection(targetClass = UUID.class)
    private List<UUID> userBlackList;

    @Enumerated(EnumType.STRING)
    private MeetStatus meetStatus;

    private String address;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private MeetCategory category;
}
