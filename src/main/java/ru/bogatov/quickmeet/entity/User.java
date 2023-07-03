package ru.bogatov.quickmeet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import ru.bogatov.quickmeet.entity.auth.UserForAuth;
import ru.bogatov.quickmeet.model.enums.AccountClass;
import ru.bogatov.quickmeet.model.enums.Role;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name ="usr", indexes = @Index(name = "phone_number_index", columnList = "phone_number", unique = true))
public class User implements UserForAuth {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "first_name", length = 20, nullable = false)
    private String firstName;

    @Column(name = "second_name", length = 20, nullable = false)
    private String secondName;

    @Column(name = "last_name", length = 20)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_class")
    private AccountClass accountClass;

    @Column(name = "account_rank")
    private float accountRank;

    @Column(name = "miss_series")
    private int missSeries;

    @Column(name = "attend_series")
    private int attendSeries;

    @JsonIgnore
    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;

    @Column(name = "phone_number", length = 16)
    private String phoneNumber;

    @Column(name = "description")
    private String description;

    @JsonIgnore
    @Column(name = "password", length = 100)
    private String password;

    @JsonIgnore
    @Column(name = "refresh")
    private String refresh;

    @Column(name = "is_active")
    private boolean isActive;

    @Column(name = "is_removed")
    private boolean isRemoved;

    @Column(name = "is_blocked")
    private boolean isBlocked;

    @JsonIgnore
    @Column(name = "activation_code")
    private String activationCode;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "is_email_confirmed")
    private boolean isEmailConfirmed;

    @OneToOne(fetch = FetchType.LAZY)
    private BillingAccount billingAccount;

    @Column(name = "registration_date")
    private Date registrationDate;

    @Column(name = "birth_date")
    private Date birthDate;

}
