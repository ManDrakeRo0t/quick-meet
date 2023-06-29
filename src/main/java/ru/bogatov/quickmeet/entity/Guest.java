package ru.bogatov.quickmeet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "guest")
public class Guest {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    @Column(name = "user_id")
    private UUID userId;
    @Column(name = "is_attend")
    boolean isAttend;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    private Meet meet;
}
