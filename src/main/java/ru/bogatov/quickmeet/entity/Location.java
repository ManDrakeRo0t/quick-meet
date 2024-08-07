package ru.bogatov.quickmeet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "location")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Location implements Serializable {

    private static final long serialVersionUID = 67275917588L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @OneToOne(fetch = FetchType.EAGER)
    private File avatar;

    @OneToMany(mappedBy = "location", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Banner> banners;

    @JsonIgnore
    @OneToMany(mappedBy = "location", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    private Set<Meet> meets;

    @Column(name = "name", length = 60)
    private String name;

    @Column(name = "address")
    private String address;

    @Column(name = "description")
    private String description;

    @JsonIgnore
    @Column(name = "is_hidden")
    private boolean isHidden;

    @Column(name = "longevity")
    private double longevity;

    @Column(name = "available_till")
    private LocalDateTime availableTill;

    @Column(name = "latitude")
    private double latitude;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User owner;
}
