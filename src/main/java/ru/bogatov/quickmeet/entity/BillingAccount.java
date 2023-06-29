package ru.bogatov.quickmeet.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Data
@Table(name = "billing_account")
public class BillingAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "card_nubmer", length = 20)
    private String cardNumber;

    @Column(name = "next_bill_date")
    private Date nextBillDate;

    @OneToOne(fetch = FetchType.LAZY)
    private User userData;
}
