package ru.bogatov.quickmeet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "banner")
public class Banner {

    private static final long serialVersionUID = 62075917588L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @OneToOne(fetch = FetchType.EAGER)
    private File avatar;

    @Column(name = "name")
    private String name;

    @Column(name = "description", length = 400)
    private String description;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    private Location location;
}
