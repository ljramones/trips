package com.teamgannon.trips.solarsystem.nightsky;

import com.teamgannon.trips.jpa.model.StarObject;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Frame bridge for night-sky queries.
 *
 * <p>StarObject coordinates (x,y,z) are Sol-centric, heliocentric inertial coordinates (J2000-era orientation)
 * expressed in light-years. To compute the sky from another star system, we translate the observer position
 * into the same inertial frame:
 *
 * <pre>
 * observerPositionLy = systemStarPositionLy + observerRelativeToStarLy
 * </pre>
 *
 * <p>This class performs <b>translation only</b>; inertial axes are unchanged (no rotation applied).
 *
 * <p>Null policy:
 * <ul>
 *   <li>If {@code systemStar} is null, we treat it as Sol at the origin (0,0,0).</li>
 *   <li>If the relative offset is null, the observer is assumed to be located at the system star.</li>
 * </ul>
 */
public final class NightSkyFrameBridge {

    /**
     * Astronomical Unit to light-year conversion factor.
     * 1 ly ≈ 63241.077 AU.
     */
    public static final double AU_TO_LY = 1.0 / 63241.077;

    private NightSkyFrameBridge() {
        // utility class
    }

    public static Vector3D starPositionLy(StarObject systemStar) {
        if (systemStar == null) {
            return Vector3D.ZERO;
        }
        return new Vector3D(systemStar.getX(), systemStar.getY(), systemStar.getZ());
    }

    public static Vector3D observerPositionLy(StarObject systemStar, Vector3D observerRelativeToStarLy) {
        Vector3D starPosition = starPositionLy(systemStar);
        if (observerRelativeToStarLy == null) {
            return starPosition;
        }
        return starPosition.add(observerRelativeToStarLy);
    }

    public static Vector3D observerPositionLyFromAu(StarObject systemStar, Vector3D observerRelativeToStarAu) {
        Vector3D starPosition = starPositionLy(systemStar);
        if (observerRelativeToStarAu == null) {
            return starPosition;
        }
        Vector3D offsetLy = observerRelativeToStarAu.scalarMultiply(AU_TO_LY);
        return starPosition.add(offsetLy);
    }

    public static Vector3D observerPositionLyFromAu(StarObject systemStar, double xAu, double yAu, double zAu) {
        return observerPositionLyFromAu(systemStar, new Vector3D(xAu, yAu, zAu));
    }
}

