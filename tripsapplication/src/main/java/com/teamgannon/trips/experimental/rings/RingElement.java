package com.teamgannon.trips.experimental.rings;

import javafx.scene.paint.Color;

/**
 * Represents a single particle/element in a ring system.
 * Contains both orbital parameters (immutable) and current state (mutable angle).
 */
public class RingElement {

    // Immutable orbital parameters
    private final double semiMajorAxis;
    private final double eccentricity;
    private final double inclination;
    private final double argumentOfPeriapsis;
    private final double longitudeOfAscendingNode;
    private final double angularSpeed;
    private final double size;
    private final double heightOffset;
    private final Color color;

    // Mutable state
    private double currentAngle;

    // Cached position (updated each frame)
    private double x, y, z;

    public RingElement(
            double semiMajorAxis,
            double eccentricity,
            double inclination,
            double argumentOfPeriapsis,
            double longitudeOfAscendingNode,
            double initialAngle,
            double angularSpeed,
            double size,
            double heightOffset,
            Color color
    ) {
        this.semiMajorAxis = semiMajorAxis;
        this.eccentricity = eccentricity;
        this.inclination = inclination;
        this.argumentOfPeriapsis = argumentOfPeriapsis;
        this.longitudeOfAscendingNode = longitudeOfAscendingNode;
        this.currentAngle = initialAngle;
        this.angularSpeed = angularSpeed;
        this.size = size;
        this.heightOffset = heightOffset;
        this.color = color;

        // Initialize position
        updatePosition();
    }

    /**
     * Advances the element's orbital position by the given time scale.
     *
     * @param timeScale multiplier for angular movement (1.0 = normal speed)
     */
    public void advance(double timeScale) {
        currentAngle += angularSpeed * timeScale;
        updatePosition();
    }

    /**
     * Updates the cached x, y, z position based on current orbital angle.
     */
    private void updatePosition() {
        // Calculate radius at current true anomaly: r = a(1-e²)/(1+e*cos(ν))
        double r;
        if (eccentricity < 1e-10) {
            r = semiMajorAxis;
        } else {
            r = semiMajorAxis * (1 - eccentricity * eccentricity)
                    / (1 + eccentricity * Math.cos(currentAngle));
        }

        // Position in orbital plane
        double xOrbital = r * Math.cos(currentAngle);
        double yOrbital = r * Math.sin(currentAngle);

        // Apply argument of periapsis rotation (in orbital plane)
        double cosArgPeri = Math.cos(argumentOfPeriapsis);
        double sinArgPeri = Math.sin(argumentOfPeriapsis);
        double xRotated = xOrbital * cosArgPeri - yOrbital * sinArgPeri;
        double yRotated = xOrbital * sinArgPeri + yOrbital * cosArgPeri;

        // Apply inclination (tilt the orbital plane)
        double cosInc = Math.cos(inclination);
        double sinInc = Math.sin(inclination);
        double zTilted = yRotated * sinInc;
        double yTilted = yRotated * cosInc;

        // Apply longitude of ascending node (rotate around z-axis)
        double cosLAN = Math.cos(longitudeOfAscendingNode);
        double sinLAN = Math.sin(longitudeOfAscendingNode);
        this.x = xRotated * cosLAN - yTilted * sinLAN;
        this.z = xRotated * sinLAN + yTilted * cosLAN;
        this.y = zTilted + heightOffset;
    }

    // Getters for immutable properties
    public double getSemiMajorAxis() { return semiMajorAxis; }
    public double getEccentricity() { return eccentricity; }
    public double getInclination() { return inclination; }
    public double getArgumentOfPeriapsis() { return argumentOfPeriapsis; }
    public double getLongitudeOfAscendingNode() { return longitudeOfAscendingNode; }
    public double getAngularSpeed() { return angularSpeed; }
    public double getSize() { return size; }
    public double getHeightOffset() { return heightOffset; }
    public Color getColor() { return color; }

    // Getters for mutable state
    public double getCurrentAngle() { return currentAngle; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }

    /**
     * Sets the position directly (used by physics engines like ODE).
     */
    public void setPosition(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
