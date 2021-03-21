package com.teamgannon.trips.dynamics;

import lombok.Builder;
import lombok.Data;
import org.orekit.utils.TimeStampedPVCoordinates;

import java.util.UUID;

@Data
@Builder
public class OrbitalPosition {

    /**
     * the object we are tracking
     */
    private UUID object;

    /**
     * the time we were at the last position marked here
     */
    private double positionTime;

    /**
     *
     */
    private TimeStampedPVCoordinates orbitCoordinates;

}
