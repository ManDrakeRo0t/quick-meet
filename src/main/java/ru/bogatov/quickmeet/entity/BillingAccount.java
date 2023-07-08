package ru.bogatov.quickmeet.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Entity
@Data
@Table(name = "billing_account")
public class BillingAccount implements Serializable {

    private static final long serialVersionUID = 771754347547L;

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
