package ru.bogatov.quickmeet.model.request;

import lombok.Data;
import ru.bogatov.quickmeet.model.enums.AccountClass;

import java.util.UUID;

@Data
public class PaymentCreationBody {
    AccountClass type;
    Integer period;
    Integer locations;
    UUID userId;
}
