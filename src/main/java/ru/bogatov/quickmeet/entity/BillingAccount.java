package ru.bogatov.quickmeet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import ru.bogatov.quickmeet.model.enums.AccountClass;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
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

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "locations_amount")
    private int locationsAmount;

    @Column(name = "max_amount")
    private int maxAmount;

    @Transient
    private AccountClass actualClass;

    @Column(name = "premium_start")
    private LocalDateTime premiumEndTime;

    @Column(name = "vip_start")
    private LocalDateTime vipEndTime;

    @Column(name = "business_start")
    private LocalDateTime businessEndTime;
}
