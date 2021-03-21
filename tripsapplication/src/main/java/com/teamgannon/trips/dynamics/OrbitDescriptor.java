package com.teamgannon.trips.dynamics;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrbitDescriptor {
    double semiMajorAxis;
    double eccentricity;
    double inclination;
    double perigreeArgument;
    double rightAscensionOfAscendingNode;
    double meanAnomaly;
}
