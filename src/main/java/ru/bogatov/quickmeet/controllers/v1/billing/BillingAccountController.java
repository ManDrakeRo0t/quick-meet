package ru.bogatov.quickmeet.controllers.v1.billing;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.bogatov.quickmeet.constants.RouteConstants;
import ru.bogatov.quickmeet.entities.BillingAccount;

import java.util.UUID;

@RestController
@RequestMapping(RouteConstants.API_V1 + RouteConstants.BILLING_MANAGEMENT + RouteConstants.BILLING_ACCOUNT)
public class BillingAccountController {

    @GetMapping("/{id}")
    public ResponseEntity<BillingAccount> getBillingAccountById(@PathVariable UUID id) {
        return ResponseEntity.ok(new BillingAccount());
    }

}
