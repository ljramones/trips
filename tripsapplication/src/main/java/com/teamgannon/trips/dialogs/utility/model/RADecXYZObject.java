package com.teamgannon.trips.dialogs.utility.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RADecXYZObject {

    private boolean calculated;

    private double rightAscension;

    private double distance;

    private double declination;

    private double[] coordinates;

}
