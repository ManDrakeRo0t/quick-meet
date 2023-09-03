package ru.bogatov.quickmeet.service.billing;

import lombok.AllArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.bogatov.quickmeet.entity.BillingAccount;
import ru.bogatov.quickmeet.error.ErrorUtils;
import ru.bogatov.quickmeet.model.enums.AccountClass;
import ru.bogatov.quickmeet.model.enums.ApplicationError;
import ru.bogatov.quickmeet.model.request.PaymentCreationBody;
import ru.bogatov.quickmeet.repository.userdata.BillingAccountRepository;

import java.time.LocalDateTime;
import java.util.UUID;

import static ru.bogatov.quickmeet.constant.CacheConstants.BILLING_ACCOUNT_CACHE;

@Service
@AllArgsConstructor
public class BillingAccountService {

    private final BillingAccountRepository billingAccountRepository;

    private final CacheManager cacheManager;

    public BillingAccount createPayment(PaymentCreationBody body) {

        if (body.getType() == AccountClass.BASE) {
            throw ErrorUtils.buildException(ApplicationError.REQUEST_PARAMETERS_ERROR, "Base type not applicable for payment");
        }

        BillingAccount billingAccount = getCustomerBillingAccount(body.getUserId());
        if (billingAccount == null) {
            billingAccount = createBillingAccount(body.getUserId());
        }
        if (body.getPeriod() != null && body.getType() != null) {
            switch (body.getType()) {
                case PREMIUM:
                    updatePremiumPeriod(billingAccount, body.getPeriod());
                    break;
                case VIP:
                    updateVipPeriod(billingAccount, body.getPeriod());
                    break;
                case BUSINESS:
                    updateBusinessPeriod(billingAccount, body.getPeriod());
                    if (billingAccount.getMaxAmount() == 0) {
                        billingAccount.setMaxAmount(1);
                    }
                    break;
            }
        }
        if (body.getLocations() != null) {
            billingAccount.setLocationsAmount(billingAccount.getLocationsAmount() + body.getLocations());
        }
        return  saveAndUpdateInCache(billingAccount);
    }
    @Cacheable(value = BILLING_ACCOUNT_CACHE, key = "#customerId")
    public BillingAccount getCustomerBillingAccount(UUID customerId) {
        return billingAccountRepository.findBillingAccountByUserId(customerId).orElse(null);
    }

    public BillingAccount setBillingAccountClass(BillingAccount billingAccount) {
        if (billingAccount == null) {
            return null;
        }
        billingAccount.setActualClass(AccountClass.BASE);
        LocalDateTime now = LocalDateTime.now();
        if (billingAccount.getPremiumEndTime().isAfter(now)) {
            billingAccount.setActualClass(AccountClass.PREMIUM);
        }
        if (billingAccount.getVipEndTime().isAfter(now)) {
            billingAccount.setActualClass(AccountClass.VIP);
        }
        if (billingAccount.getBusinessEndTime().isAfter(now)) {
            billingAccount.setActualClass(AccountClass.BUSINESS);
        }
        return billingAccount;
    }

    public BillingAccount createBillingAccount(UUID userId) {
        BillingAccount account = new BillingAccount();
        account.setId(userId);
        account.setLocationsAmount(0);
        account.setMaxAmount(0);
        return account;
    }

    public BillingAccount saveAndUpdateInCache(BillingAccount billingAccount) {
        BillingAccount updatedAccount = billingAccountRepository.save(billingAccount);
        cacheManager.getCache(BILLING_ACCOUNT_CACHE).put(updatedAccount.getUserId(), updatedAccount);
        return updatedAccount;
    }

    public void updatePremiumPeriod(BillingAccount account, Integer period) {
        LocalDateTime endTime = account.getPremiumEndTime();
        if (endTime != null && LocalDateTime.now().isBefore(endTime)) {
            account.setPremiumEndTime(endTime.plusMonths(period));
        } else {
            account.setPremiumEndTime(LocalDateTime.now().plusMonths(period));
        }
    }

    public void updateVipPeriod(BillingAccount account, Integer period) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = account.getVipEndTime();
        if (endTime != null && now.isBefore(endTime)) {
            account.setVipEndTime(endTime.plusMonths(period));
        } else {
            account.setVipEndTime(now.plusMonths(period));
        }
        if (account.getPremiumEndTime().isAfter(now)) {
            updatePremiumPeriod(account, period);
        }
    }

    public void updateBusinessPeriod(BillingAccount account, Integer period) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = account.getBusinessEndTime();
        if (endTime != null && now.isBefore(endTime)) {
            account.setBusinessEndTime(endTime.plusMonths(period));
        } else {
            account.setBusinessEndTime(now.plusMonths(period));
        }
        if (account.getVipEndTime().isAfter(now)) {
            updateVipPeriod(account, period);
        }
    }
}
