package com.teamgannon.trips.solarsystem.modelling.habitable;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HabitableZone {

    private double innerRadius;

    private double outerRadius;

}
