package ru.bogatov.quickmeet.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import ru.bogatov.quickmeet.enums.AccountClass;
import ru.bogatov.quickmeet.enums.Role;

import javax.persistence.*;
import java.util.Set;
import java.util.UUID;

//@Entity
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String firstName;

    private String secondName;

    private String lastName;

    @Enumerated(EnumType.STRING)
    private AccountClass accountClass;

    private float accountRank;

    @ElementCollection(targetClass = Role.class,fetch = FetchType.EAGER)
    @CollectionTable(name = "user_role" , joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    private Set<Role> roleSet;

    @OneToOne
    @JsonIgnore
    private UserData information;

    @OneToMany(fetch = FetchType.LAZY)
    private Set<Meet> meetSet;


}
