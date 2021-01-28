package com.teamgannon.trips.solarsysmodelling.habitable;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HabitableZone {

    private double innerRadius;

    private double outerRadius;

}
