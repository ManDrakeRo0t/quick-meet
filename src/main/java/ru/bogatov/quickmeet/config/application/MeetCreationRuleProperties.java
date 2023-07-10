package ru.bogatov.quickmeet.config.application;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class MeetCreationRuleProperties {

    @Value(value = "${application.meet-creation.use-rule}")
    public boolean useRule;
    @Value(value = "${application.meet-creation.required-rank}")
    public float requiredRankForMeetCreation;
    @Value(value = "${application.meet-creation.validate-cross-time}")
    public boolean validateCrossTime;
    @Value(value = "${application.meet-creation.gold-account.meet-limit-per-day}")
    public int goldLimit;
    @Value(value = "${application.meet-creation.gold-account.meet-max-capacity}")
    public int goldMaxCapacity;
    @Value(value = "${application.meet-creation.base-account.meet-limit-per-day}")
    public int baseLimit;
    @Value(value = "${application.meet-creation.base-account.meet-max-capacity}")
    public int baseMaxCapacity;

}
