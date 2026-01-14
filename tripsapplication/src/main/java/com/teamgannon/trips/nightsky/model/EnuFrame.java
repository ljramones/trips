package com.teamgannon.trips.nightsky.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hipparchus.geometry.euclidean.threed.Vector3D;

/**
 * East-North-Up local horizon reference frame for an observer.
 * Used to transform world coordinates to local sky coordinates.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnuFrame {
    /** East unit vector in world coordinates */
    private Vector3D east;

    /** North unit vector in world coordinates */
    private Vector3D north;

    /** Up (zenith) unit vector in world coordinates */
    private Vector3D up;

    /** Observer's latitude in radians */
    private double latRad;

    /** Observer's longitude in radians */
    private double lonRad;
}
