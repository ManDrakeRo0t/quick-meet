package ru.bogatov.quickmeet.model.validation;

import lombok.Data;
import ru.bogatov.quickmeet.config.application.MeetValidationRuleProperties;
import ru.bogatov.quickmeet.model.enums.AccountClass;
import ru.bogatov.quickmeet.model.enums.IconUpdateType;

@Data
public class AccountClassProperties {

    public int limit;
    public int maxCapacity;
    public int updateLimit;
    public boolean highlightMeet;
    public IconUpdateType iconType;

    public static AccountClassProperties getForClass(AccountClass accountClass, MeetValidationRuleProperties properties) {
        AccountClassProperties accountClassProperties = new AccountClassProperties();
        switch (accountClass) {
            case BASE:
                accountClassProperties.setLimit(properties.baseLimit);
                accountClassProperties.setMaxCapacity(properties.baseMaxCapacity);
                accountClassProperties.setHighlightMeet(properties.baseHighlightMeet);
                accountClassProperties.setUpdateLimit(properties.baseUpdateLimit);
                accountClassProperties.setIconType(IconUpdateType.valueOf(properties.baseIconType));
                break;
            case PREMIUM:
                accountClassProperties.setLimit(properties.premiumLimit);
                accountClassProperties.setMaxCapacity(properties.premiumMaxCapacity);
                accountClassProperties.setHighlightMeet(properties.premiumHighlightMeet);
                accountClassProperties.setUpdateLimit(properties.premiumUpdateLimit);
                accountClassProperties.setIconType(IconUpdateType.valueOf(properties.premiumIconType));
                break;
            case VIP:
                accountClassProperties.setLimit(properties.vipLimit);
                accountClassProperties.setMaxCapacity(properties.vipMaxCapacity);
                accountClassProperties.setHighlightMeet(properties.vipHighlightMeet);
                accountClassProperties.setUpdateLimit(properties.vipUpdateLimit);
                accountClassProperties.setIconType(IconUpdateType.valueOf(properties.vipIconType));
                break;
            case BUSINESS:
                accountClassProperties.setLimit(properties.businessLimit);
                accountClassProperties.setMaxCapacity(properties.businessMaxCapacity);
                accountClassProperties.setHighlightMeet(properties.businessHighlightMeet);
                accountClassProperties.setUpdateLimit(properties.businessUpdateLimit);
                accountClassProperties.setIconType(IconUpdateType.valueOf(properties.businessIconType));
        }
        return accountClassProperties;
    }

}
