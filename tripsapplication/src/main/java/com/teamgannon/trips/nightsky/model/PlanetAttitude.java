package com.teamgannon.trips.nightsky.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hipparchus.geometry.euclidean.threed.Vector3D;

/**
 * Planetary rotation state at a specific instant.
 * Defines the planet's orientation in space.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanetAttitude {
    /** Planet's spin axis direction in world coordinates (unit vector) */
    private Vector3D spinAxisWorldUnit;

    /** Direction of prime meridian at time t in world coordinates (unit vector) */
    private Vector3D primeMeridianWorldUnitAtT;

    /** Planet's position in light years from Sol */
    private double[] positionLy;

    /** Rotation rate in radians per second */
    private double rotationRateRadPerSec;
}
