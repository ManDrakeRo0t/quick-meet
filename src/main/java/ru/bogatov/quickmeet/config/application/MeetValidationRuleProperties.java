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
    @Value(value = "${application.meet-validation.gold-account.meet-limit-per-day}")
    public int goldLimit;
    @Value(value = "${application.meet-validation.gold-account.meet-max-capacity}")
    public int goldMaxCapacity;
    @Value(value = "${application.meet-validation.base-account.meet-limit-per-day}")
    public int baseLimit;
    @Value(value = "${application.meet-validation.base-account.meet-max-capacity}")
    public int baseMaxCapacity;

}
