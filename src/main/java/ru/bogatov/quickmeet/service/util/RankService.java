package ru.bogatov.quickmeet.service.util;

import org.springframework.stereotype.Service;
import ru.bogatov.quickmeet.config.application.MeetValidationRuleProperties;
import ru.bogatov.quickmeet.entity.BillingAccount;
import ru.bogatov.quickmeet.entity.Guest;
import ru.bogatov.quickmeet.entity.Meet;
import ru.bogatov.quickmeet.entity.User;
import ru.bogatov.quickmeet.model.validation.AccountClassProperties;
import ru.bogatov.quickmeet.service.billing.BillingAccountService;
import ru.bogatov.quickmeet.service.user.UserService;

import java.time.LocalDateTime;

import static ru.bogatov.quickmeet.constant.UserConstants.*;
import static ru.bogatov.quickmeet.constant.UserConstants.MIN_RANK;

@Service
public class RankService {

    private final UserService userService;
    private final BillingAccountService billingAccountService;

    public RankService(UserService userService, BillingAccountService billingAccountService) {
        this.userService = userService;
        this.billingAccountService = billingAccountService;
    }

    public void updateOwnerRank(Meet meet) {
        User owner = meet.getOwner();
        float currentRank = owner.getAccountRank();
        float updatedRank = currentRank;
        if (meet.getGuests() != null && !meet.getGuests().isEmpty()) {
            long attendedGuests = meet.getGuests().stream().filter(Guest::isAttend).count();
            updatedRank += (float) 1 / meet.getMaxPeople() * attendedGuests;
        }
        updatedRank = Math.min(updatedRank, MAX_RANK);
        if (!meet.isOwnerAttend()) {
            updatedRank -= RANK_UPDATE_DELTA * 10;
        }
        updatedRank = Math.max(updatedRank, MIN_RANK);
        if (updatedRank != currentRank) {
            owner.setAccountRank(updatedRank);
            userService.updateUser(owner);
        }
    }

    public void updateOwnerRankForUpdate(Meet meet, int updates, MeetValidationRuleProperties properties) {
        User owner = meet.getOwner();
        BillingAccount account = billingAccountService.getCustomerBillingAccount(owner.getId());
        float currentRank = owner.getAccountRank();
        int updateLimit = AccountClassProperties.getForClass(
                billingAccountService.setBillingAccountClass(account, LocalDateTime.now()).getActualClass(),
                properties).updateLimit;
        if (meet.getUpdateCount() < updateLimit && updates > updateLimit) {
            currentRank -= (RANK_UPDATE_DELTA * (updates - updateLimit));
        } else  {
            currentRank -= (RANK_UPDATE_DELTA * updates);
        }
        meet.setUpdateCount(meet.getUpdateCount() + updates);
        currentRank = Math.max(currentRank, MIN_RANK);
        owner.setAccountRank(currentRank);
        userService.updateUser(owner);
    }

    public void updateGuestRank(Meet meet, Guest guest) {
        User user = userService.findUserByID(guest.getUserId());
        int attendSeries = user.getAttendSeries();
        int missSeries = user.getMissSeries();
        float updatedRank = user.getAccountRank();
        if (guest.isAttend()) {
            attendSeries += 1;
            missSeries = 0;
        } else {
            missSeries += 1;
            attendSeries = 0;
        }
        if (meet.isGuestRatingProcessRequired()) {
            if (!guest.isAttend()) {
                updatedRank -= missSeries * RANK_UPDATE_DELTA;
                if (updatedRank < MIN_RANK) {
                    updatedRank = MIN_RANK;
                }
            } else {
                updatedRank += attendSeries * RANK_UPDATE_DELTA;
                if (updatedRank > MAX_RANK) {
                    updatedRank = MAX_RANK;
                }
            }
        }
        user.setAccountRank(updatedRank);
        user.setAttendSeries(attendSeries);
        user.setMissSeries(missSeries);
        userService.updateUser(user);
    }

}
