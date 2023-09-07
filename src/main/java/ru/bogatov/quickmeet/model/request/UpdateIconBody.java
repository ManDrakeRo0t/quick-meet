package ru.bogatov.quickmeet.model.request;

import lombok.Data;
import ru.bogatov.quickmeet.model.enums.Icon;

@Data
public class UpdateIconBody {
    Icon icon;
}
