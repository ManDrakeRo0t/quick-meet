package ru.bogatov.quickmeet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "guest")
public class Guest implements Serializable {

    private static final long serialVersionUID = 672754457588L;

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
