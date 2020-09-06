package com.teamgannon.trips.report.distance;


import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class StarDistanceMeasure {

    private UUID starId;

    private String starName;

    private double distance;

}
