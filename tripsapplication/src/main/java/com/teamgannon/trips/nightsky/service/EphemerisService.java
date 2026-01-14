package com.teamgannon.trips.nightsky.service;

import com.teamgannon.trips.jpa.model.ExoPlanet;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.jpa.repository.ExoPlanetRepository;
import com.teamgannon.trips.jpa.repository.StarObjectRepository;
import com.teamgannon.trips.nightsky.model.EnuFrame;
import com.teamgannon.trips.nightsky.model.PlanetAttitude;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

/**
 * Computes planetary positions and orientations at any given time.
 * Handles Keplerian orbit propagation for exoplanets.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EphemerisService {

    private static final double AU_TO_LY = 1.0 / 63241.077;  // 1 AU in light years

    private final StarObjectRepository starObjectRepository;
    private final ExoPlanetRepository exoPlanetRepository;
    private final TimeService timeService;

    /**
     * Get planet attitude (position and orientation) at a given time.
     */
    public PlanetAttitude getPlanetAttitude(String planetId, String hostStarId, Instant instant) {
        // Get host star position
        Optional<StarObject> starOpt = starObjectRepository.findById(hostStarId);
        if (starOpt.isEmpty()) {
            log.warn("Host star not found: {}", hostStarId);
            return getDefaultAttitude();
        }
        StarObject hostStar = starOpt.get();

        // Get planet orbital elements
        Optional<ExoPlanet> planetOpt = exoPlanetRepository.findById(planetId);
        if (planetOpt.isEmpty()) {
            log.warn("Planet not found: {}", planetId);
            return getDefaultAttitude();
        }
        ExoPlanet planet = planetOpt.get();

        // Calculate planet position in orbit at time t
        double[] orbitalPos = calculateOrbitalPosition(planet, instant);

        // Star position in light years
        double starX = hostStar.getX();
        double starY = hostStar.getY();
        double starZ = hostStar.getZ();

        // Planet position = star position + orbital offset (converted to ly)
        double[] planetPos = new double[]{
                starX + orbitalPos[0] * AU_TO_LY,
                starY + orbitalPos[1] * AU_TO_LY,
                starZ + orbitalPos[2] * AU_TO_LY
        };

        // Calculate spin axis (assume aligned with orbit normal for now)
        double inclination = planet.getInclination() != null ?
                Math.toRadians(planet.getInclination()) : 0.0;
        double ascendingNode = planet.getLongitudeOfAscendingNode() != null ?
                Math.toRadians(planet.getLongitudeOfAscendingNode()) : 0.0;

        Vector3D spinAxis = calculateSpinAxis(inclination, ascendingNode);
        Vector3D primeMeridian = calculatePrimeMeridian(planet, instant, spinAxis);

        return PlanetAttitude.builder()
                .positionLy(planetPos)
                .spinAxisWorldUnit(spinAxis)
                .primeMeridianWorldUnitAtT(primeMeridian)
                .rotationRateRadPerSec(calculateRotationRate(planet))
                .build();
    }

    /**
     * Calculate orbital position at time t using Keplerian elements.
     */
    private double[] calculateOrbitalPosition(ExoPlanet planet, Instant instant) {
        double a = planet.getSemiMajorAxis() != null ? planet.getSemiMajorAxis() : 1.0;
        double e = planet.getEccentricity() != null ? planet.getEccentricity() : 0.0;
        double i = planet.getInclination() != null ? Math.toRadians(planet.getInclination()) : 0.0;
        double omega = planet.getOmega() != null ?
                Math.toRadians(planet.getOmega()) : 0.0;
        double Omega = planet.getLongitudeOfAscendingNode() != null ?
                Math.toRadians(planet.getLongitudeOfAscendingNode()) : 0.0;

        // Calculate mean anomaly at time t
        double period = planet.getOrbitalPeriod() != null ? planet.getOrbitalPeriod() : 365.25;
        double n = 2.0 * Math.PI / period;  // Mean motion (rad/day)

        // Time since epoch (assume J2000 for now)
        double daysSinceEpoch = instant.toEpochMilli() / 86400000.0 - 10957.5; // Days since J2000
        double M = (n * daysSinceEpoch) % (2.0 * Math.PI);

        // Solve Kepler's equation for eccentric anomaly
        double E = solveKeplerEquation(M, e);

        // True anomaly
        double nu = 2.0 * Math.atan2(
                Math.sqrt(1 + e) * Math.sin(E / 2),
                Math.sqrt(1 - e) * Math.cos(E / 2)
        );

        // Distance from star
        double r = a * (1 - e * Math.cos(E));

        // Position in orbital plane
        double xOrbit = r * Math.cos(nu);
        double yOrbit = r * Math.sin(nu);

        // Rotate to world coordinates
        double cosOmega = Math.cos(Omega);
        double sinOmega = Math.sin(Omega);
        double cosI = Math.cos(i);
        double sinI = Math.sin(i);
        double cosOm = Math.cos(omega);
        double sinOm = Math.sin(omega);

        double x = (cosOmega * cosOm - sinOmega * sinOm * cosI) * xOrbit
                + (-cosOmega * sinOm - sinOmega * cosOm * cosI) * yOrbit;
        double y = (sinOmega * cosOm + cosOmega * sinOm * cosI) * xOrbit
                + (-sinOmega * sinOm + cosOmega * cosOm * cosI) * yOrbit;
        double z = (sinOm * sinI) * xOrbit + (cosOm * sinI) * yOrbit;

        return new double[]{x, y, z};
    }

    /**
     * Solve Kepler's equation M = E - e*sin(E) using Newton-Raphson.
     */
    private double solveKeplerEquation(double M, double e) {
        double E = M;  // Initial guess
        for (int iter = 0; iter < 10; iter++) {
            double dE = (M - E + e * Math.sin(E)) / (1 - e * Math.cos(E));
            E += dE;
            if (Math.abs(dE) < 1e-10) break;
        }
        return E;
    }

    /**
     * Calculate spin axis from orbital inclination.
     */
    private Vector3D calculateSpinAxis(double inclination, double ascendingNode) {
        // Spin axis perpendicular to orbital plane
        double nx = Math.sin(inclination) * Math.sin(ascendingNode);
        double ny = Math.cos(inclination);
        double nz = -Math.sin(inclination) * Math.cos(ascendingNode);
        return new Vector3D(nx, ny, nz).normalize();
    }

    /**
     * Calculate prime meridian direction at time t.
     */
    private Vector3D calculatePrimeMeridian(ExoPlanet planet, Instant instant, Vector3D spinAxis) {
        // For now, use a simple rotation based on time
        double rotationPeriod = 24.0;  // Assume 24 hour day if not specified
        double daysSinceEpoch = instant.toEpochMilli() / 86400000.0;
        double rotationAngle = (daysSinceEpoch * 24.0 / rotationPeriod) * 2.0 * Math.PI;

        // Start with an arbitrary prime meridian perpendicular to spin axis
        Vector3D arbitrary = Math.abs(spinAxis.getY()) < 0.9 ? Vector3D.PLUS_J : Vector3D.PLUS_K;
        Vector3D primeMeridian = spinAxis.crossProduct(arbitrary).normalize();

        // Rotate by rotation angle
        return rotateAroundAxis(primeMeridian, spinAxis, rotationAngle);
    }

    private Vector3D rotateAroundAxis(Vector3D v, Vector3D axis, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double dot = v.dotProduct(axis);
        Vector3D cross = axis.crossProduct(v);

        return v.scalarMultiply(cos)
                .add(cross.scalarMultiply(sin))
                .add(axis.scalarMultiply(dot * (1 - cos)));
    }

    private double calculateRotationRate(ExoPlanet planet) {
        // Default to 24-hour rotation period
        return 2.0 * Math.PI / 86400.0;
    }

    /**
     * Build ENU frame for an observer on the planet surface.
     */
    public EnuFrame getEnuFrame(PlanetAttitude attitude, double latRad, double lonRad) {
        Vector3D spinAxis = attitude.getSpinAxisWorldUnit();
        Vector3D primeMeridian = attitude.getPrimeMeridianWorldUnitAtT();

        // Calculate observer's position on planet surface
        double cosLat = Math.cos(latRad);
        double sinLat = Math.sin(latRad);
        double cosLon = Math.cos(lonRad);
        double sinLon = Math.sin(lonRad);

        // Up vector (zenith) - points radially outward from planet center
        Vector3D east90 = spinAxis.crossProduct(primeMeridian).normalize();

        Vector3D up = primeMeridian.scalarMultiply(cosLat * cosLon)
                .add(east90.scalarMultiply(cosLat * sinLon))
                .add(spinAxis.scalarMultiply(sinLat))
                .normalize();

        // East vector - perpendicular to up and spin axis
        Vector3D east = spinAxis.crossProduct(up).normalize();

        // North vector - perpendicular to up and east
        Vector3D north = up.crossProduct(east).normalize();

        return EnuFrame.builder()
                .east(east)
                .north(north)
                .up(up)
                .latRad(latRad)
                .lonRad(lonRad)
                .build();
    }

    private PlanetAttitude getDefaultAttitude() {
        return PlanetAttitude.builder()
                .positionLy(new double[]{0, 0, 0})
                .spinAxisWorldUnit(Vector3D.PLUS_J)
                .primeMeridianWorldUnitAtT(Vector3D.PLUS_I)
                .rotationRateRadPerSec(2.0 * Math.PI / 86400.0)
                .build();
    }
}
