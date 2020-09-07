package com.teamgannon.trips.dialogs.search.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DistanceRoutes {

    private boolean selected;

    private double distance;
}
