package ru.bogatov.quickmeet.repository.userdata;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bogatov.quickmeet.entity.BillingAccount;

import java.util.Optional;
import java.util.UUID;

public interface BillingAccountRepository extends JpaRepository<BillingAccount, UUID> {

    Optional<BillingAccount> findBillingAccountByUserId(UUID userId);

}
