package com.teamgannon.trips.dialogs.utility.model;

import com.teamgannon.trips.jpa.model.StarObject;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DistanceCalculationObject {

    private boolean calculated;

    private StarObject fromStar;

    private StarObject toStar;

    private double distance;
}
