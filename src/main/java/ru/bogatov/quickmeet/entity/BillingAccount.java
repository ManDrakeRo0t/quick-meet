package ru.bogatov.quickmeet.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Data
public class BillingAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String cardNumber;

    private Date nextBillDate;

    @OneToOne(fetch = FetchType.LAZY)
    private User userData;
}
