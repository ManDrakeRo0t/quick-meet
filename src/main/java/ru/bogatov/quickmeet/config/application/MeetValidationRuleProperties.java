package ru.bogatov.quickmeet.config.application;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class MeetValidationRuleProperties {

    @Value(value = "${application.meet-validation.use-rule}")
    public boolean useRule;
    @Value(value = "${application.meet-validation.required-rank}")
    public float requiredRankForMeetCreation;
    @Value(value = "${application.meet-validation.validate-cross-time}")
    public boolean validateCrossTime;

    @Value(value = "${application.meet-validation.premium-account.meet-limit-per-day}")
    public int premiumLimit;
    @Value(value = "${application.meet-validation.premium-account.meet-max-capacity}")
    public int premiumMaxCapacity;
    @Value(value = "${application.meet-validation.premium-account.update-limit}")
    public int premiumUpdateLimit;
    @Value(value = "${application.meet-validation.premium-account.meet-highlight}")
    public boolean premiumHighlightMeet;
    @Value(value = "${application.meet-validation.premium-account.icon-update}")
    public String premiumIconType;

    @Value(value = "${application.meet-validation.base-account.meet-limit-per-day}")
    public int baseLimit;
    @Value(value = "${application.meet-validation.base-account.meet-max-capacity}")
    public int baseMaxCapacity;
    @Value(value = "${application.meet-validation.base-account.update-limit}")
    public int baseUpdateLimit;
    @Value(value = "${application.meet-validation.base-account.meet-highlight}")
    public boolean baseHighlightMeet;
    @Value(value = "${application.meet-validation.base-account.icon-update}")
    public String baseIconType;

    @Value(value = "${application.meet-validation.vip-account.meet-limit-per-day}")
    public int vipLimit;
    @Value(value = "${application.meet-validation.vip-account.meet-max-capacity}")
    public int vipMaxCapacity;
    @Value(value = "${application.meet-validation.vip-account.update-limit}")
    public int vipUpdateLimit;
    @Value(value = "${application.meet-validation.vip-account.meet-highlight}")
    public boolean vipHighlightMeet;
    @Value(value = "${application.meet-validation.vip-account.icon-update}")
    public String vipIconType;

    @Value(value = "${application.meet-validation.business-account.meet-limit-per-day}")
    public int businessLimit;
    @Value(value = "${application.meet-validation.business-account.meet-max-capacity}")
    public int businessMaxCapacity;
    @Value(value = "${application.meet-validation.business-account.update-limit}")
    public int businessUpdateLimit;
    @Value(value = "${application.meet-validation.business-account.meet-highlight}")
    public boolean businessHighlightMeet;
    @Value(value = "${application.meet-validation.business-account.icon-update}")
    public String businessIconType;
}
