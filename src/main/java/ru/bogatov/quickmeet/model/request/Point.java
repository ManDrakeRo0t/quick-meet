package ru.bogatov.quickmeet.model.request;

import com.sun.istack.NotNull;
import lombok.Data;

@Data
public class Point {
    @NotNull
    private double latitude;
    @NotNull
    private double longevity;
}
