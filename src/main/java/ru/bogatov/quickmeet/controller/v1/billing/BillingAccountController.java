package ru.bogatov.quickmeet.controller.v1.billing;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.bogatov.quickmeet.constant.RouteConstants;
import ru.bogatov.quickmeet.entity.BillingAccount;

import java.util.UUID;

@RestController
@RequestMapping(RouteConstants.API_V1 + RouteConstants.BILLING_MANAGEMENT + RouteConstants.BILLING_ACCOUNT)
public class BillingAccountController {
    @PreAuthorize("hasAnyAuthority('USER')")
    @GetMapping("/{id}")
    public ResponseEntity<BillingAccount> getBillingAccountById(@PathVariable UUID id) {
        return ResponseEntity.ok(new BillingAccount());
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<BillingAccount> deleteBillingAccountById(@PathVariable UUID id) {
        return ResponseEntity.ok(new BillingAccount());
    }

}
