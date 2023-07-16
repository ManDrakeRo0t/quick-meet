package ru.bogatov.quickmeet.entity;

import lombok.Data;
import ru.bogatov.quickmeet.model.enums.MeetStatus;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Data
@Table(name = "meet")
public class Meet implements Serializable {

    private static final long serialVersionUID = 672754457547L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "name", length = 30, nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "date_time")
    private LocalDateTime dateTime;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User owner;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "city_id")
    private City city;

    @Column(name = "max_people")
    private int maxPeople;

    @Column(name = "current_people")
    private int currentPeople;

    @Column(name = "latitude")
    private double latitude;

    @Column(name = "is_rating_processed")
    private boolean isRatingProcessed;

    @Column(name = "longevity")
    private double longevity;

    @Column(name = "expected_duration")
    private int expectedDuration;

    @Column(name = "attend_required")
    private boolean attendRequired;

    @Column(name = "rank")
    private float rank;

    @OneToMany(mappedBy = "meet", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Guest> guests;

    @ElementCollection(targetClass = UUID.class, fetch = FetchType.EAGER)
    private Set<UUID> userBlackList;

    @Enumerated(EnumType.STRING)
    @Column(name = "meet_status")
    private MeetStatus meetStatus;

    @Column(name = "address", length = 128)
    private String address;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private MeetCategory category;

    @OneToOne(fetch = FetchType.EAGER)
    private File avatar;
}
