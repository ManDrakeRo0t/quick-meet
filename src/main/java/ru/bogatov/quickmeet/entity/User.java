package ru.bogatov.quickmeet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import ru.bogatov.quickmeet.model.enums.AccountClass;
import ru.bogatov.quickmeet.model.enums.Role;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name ="usr")
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

    private int missSeries;

    private int attendSeries;

    @ElementCollection(targetClass = Role.class,fetch = FetchType.EAGER)
    @CollectionTable(name = "user_role" , joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    private Set<Role> roleSet;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "city_id")
    private City city;

    private String phoneNumber;

    private String description;

    @JsonIgnore
    private String password;

    @JsonIgnore
    private String refresh;

    private boolean isActive;

    private boolean isRemoved;

    private boolean isBlocked;

    @JsonIgnore
    private String activationCode;

    private String email;

    @OneToOne(fetch = FetchType.LAZY)
    private BillingAccount billingAccount;

    private Date registrationDate;

    private Date birthDate;

}
