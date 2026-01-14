package com.teamgannon.trips.nightsky.service;

import com.teamgannon.trips.nightsky.model.EnuFrame;
import com.teamgannon.trips.nightsky.model.StarRenderRow;
import lombok.extern.slf4j.Slf4j;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.springframework.stereotype.Service;

/**
 * Pure math: transforms world vectors to local horizon coordinates.
 * Stateless service for coordinate transformations.
 */
@Slf4j
@Service
public class SkyTransformService {

    /**
     * Transform a star's world position to azimuth/altitude as seen from observer.
     *
     * @param star       Star position in world coordinates (light years)
     * @param observerLy Observer position in world coordinates (light years)
     * @param enu        Observer's local ENU frame
     * @return double[3]: {azimuth (rad), altitude (rad), distance (ly)}
     */
    public double[] worldToHorizontal(StarRenderRow star, double[] observerLy, EnuFrame enu) {
        // Vector from observer to star in world coordinates
        double dx = star.getXLy() - observerLy[0];
        double dy = star.getYLy() - observerLy[1];
        double dz = star.getZLy() - observerLy[2];

        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (distance < 1e-10) {
            // Star is at observer position
            return new double[]{0, Math.PI / 2, 0};
        }

        // Direction unit vector
        Vector3D direction = new Vector3D(dx / distance, dy / distance, dz / distance);

        // Project onto ENU frame
        double eastComponent = direction.dotProduct(enu.getEast());
        double northComponent = direction.dotProduct(enu.getNorth());
        double upComponent = direction.dotProduct(enu.getUp());

        // Azimuth: angle from North, clockwise (0=N, π/2=E)
        double azimuth = Math.atan2(eastComponent, northComponent);
        if (azimuth < 0) azimuth += 2 * Math.PI;

        // Altitude: angle above horizon
        double horizontalDist = Math.sqrt(eastComponent * eastComponent + northComponent * northComponent);
        double altitude = Math.atan2(upComponent, horizontalDist);

        return new double[]{azimuth, altitude, distance};
    }

    /**
     * Check if a star is above the horizon.
     */
    public boolean isAboveHorizon(double altitude) {
        return altitude > 0;
    }

    /**
     * Transform horizontal coordinates back to screen coordinates for rendering.
     * Maps azimuth/altitude to X/Y on a sky dome projection.
     *
     * @param azRad      Azimuth in radians
     * @param altRad     Altitude in radians
     * @param domeRadius Radius of sky dome in screen units
     * @return double[3]: {x, y, z} screen coordinates on dome surface
     */
    public double[] horizontalToScreen(double azRad, double altRad, double domeRadius) {
        // Spherical to cartesian on dome
        double r = domeRadius * Math.cos(altRad);
        double x = r * Math.sin(azRad);
        double z = r * Math.cos(azRad);
        double y = domeRadius * Math.sin(altRad);

        return new double[]{x, y, z};
    }

    /**
     * Calculate angular separation between two sky positions.
     */
    public double angularSeparation(double az1, double alt1, double az2, double alt2) {
        // Haversine formula for angular distance
        double dAz = az2 - az1;
        double dAlt = alt2 - alt1;

        double a = Math.sin(dAlt / 2) * Math.sin(dAlt / 2)
                + Math.cos(alt1) * Math.cos(alt2) * Math.sin(dAz / 2) * Math.sin(dAz / 2);

        return 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
