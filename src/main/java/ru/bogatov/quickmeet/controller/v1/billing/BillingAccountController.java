package ru.bogatov.quickmeet.controller.v1.billing;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.bogatov.quickmeet.constant.RouteConstants;
import ru.bogatov.quickmeet.entity.BillingAccount;
import ru.bogatov.quickmeet.model.request.PaymentCreationBody;
import ru.bogatov.quickmeet.service.billing.BillingAccountService;

import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping(RouteConstants.API_V1 + RouteConstants.BILLING_MANAGEMENT + RouteConstants.BILLING_ACCOUNT)
public class BillingAccountController {

    private final BillingAccountService billingAccountService;
    @PreAuthorize("@customSecurityRules.isUserRequest(#body.userId) || hasAnyAuthority('ADMIN')")
    @PostMapping()
    public ResponseEntity<BillingAccount> createPayment(@RequestBody PaymentCreationBody body) {
        return ResponseEntity.ok(billingAccountService.createPayment(body));
    }
    @PreAuthorize("@customSecurityRules.isUserRequest(#id) || hasAnyAuthority('ADMIN')")
    @GetMapping("/user/{id}")
    public ResponseEntity<BillingAccount> getBillingAccount(@PathVariable UUID id) {
        return ResponseEntity.ok(
                billingAccountService.setBillingAccountClass(
                        billingAccountService.getCustomerBillingAccount(id))
        );
    }


}
