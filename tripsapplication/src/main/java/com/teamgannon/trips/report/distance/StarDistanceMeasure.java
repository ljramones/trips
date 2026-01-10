package com.teamgannon.trips.report.distance;


import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class StarDistanceMeasure {

    private String starId;

    private String starName;

    private double distance;

}
