package com.teamgannon.trips.nightsky.service;

import com.teamgannon.trips.nightsky.model.AtmosphereModel;
import com.teamgannon.trips.nightsky.model.StarRenderRow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Converts intrinsic brightness to apparent magnitude.
 * Handles distance modulus and atmospheric extinction.
 */
@Slf4j
@Service
public class PhotometryService {

    /**
     * Calculate apparent magnitude from absolute magnitude and distance.
     *
     * m = M + 5 * log10(d) - 5
     *
     * where:
     * m = apparent magnitude
     * M = absolute magnitude
     * d = distance in parsecs (1 parsec = 3.26156 light years)
     */
    public float calculateApparentMagnitude(float absMag, double distanceLy) {
        if (distanceLy <= 0) return absMag;

        double distancePc = distanceLy / 3.26156;
        double apparentMag = absMag + 5 * Math.log10(distancePc) - 5;

        return (float) apparentMag;
    }

    /**
     * Adjust apparent magnitude for atmospheric extinction.
     * Uses Kasten & Young (1989) formula for airmass.
     *
     * @param apparentMag Base apparent magnitude
     * @param altitudeRad Altitude above horizon in radians
     * @param atmosphere  Atmospheric model
     * @return Extinction-adjusted magnitude (higher = dimmer)
     */
    public float applyAtmosphericExtinction(float apparentMag, double altitudeRad,
                                             AtmosphereModel atmosphere) {
        if (!atmosphere.isEnabled() || altitudeRad <= 0) {
            return apparentMag;
        }

        // Calculate airmass using Kasten & Young formula
        double altDeg = Math.toDegrees(altitudeRad);
        double zenithAngle = 90 - altDeg;
        double zenithRad = Math.toRadians(zenithAngle);

        double airmass;
        if (altDeg > 0) {
            // Kasten & Young (1989)
            airmass = 1.0 / (Math.cos(zenithRad) +
                    0.50572 * Math.pow(96.07995 - zenithAngle, -1.6364));
        } else {
            airmass = 40.0;  // Maximum airmass at horizon
        }

        // Apply extinction
        double extinction = atmosphere.getExtinctionCoefficient() * airmass;

        return (float) (apparentMag + extinction);
    }

    /**
     * Convert spectral class or temperature to RGB color.
     */
    public int starToColor(StarRenderRow star) {
        String spectralClass = star.getSpectralClass();
        float teff = star.getBpRpOrTeff();

        // Use spectral class if available
        if (spectralClass != null && !spectralClass.isEmpty()) {
            return spectralClassToColor(spectralClass.charAt(0));
        }

        // Otherwise use effective temperature
        return temperatureToColor(teff);
    }

    /**
     * Map spectral class to RGB color.
     */
    private int spectralClassToColor(char type) {
        return switch (type) {
            case 'O' -> packRGB(155, 176, 255);  // Blue
            case 'B' -> packRGB(170, 191, 255);  // Blue-white
            case 'A' -> packRGB(202, 215, 255);  // White
            case 'F' -> packRGB(248, 247, 255);  // Yellow-white
            case 'G' -> packRGB(255, 244, 234);  // Yellow (Sun-like)
            case 'K' -> packRGB(255, 210, 161);  // Orange
            case 'M' -> packRGB(255, 204, 111);  // Red-orange
            case 'L' -> packRGB(255, 150, 100);  // Brown dwarf
            case 'T' -> packRGB(200, 100, 150);  // Methane dwarf
            default -> packRGB(255, 255, 255);   // White default
        };
    }

    /**
     * Map effective temperature (Kelvin) to RGB color.
     * Uses blackbody approximation.
     */
    private int temperatureToColor(float teff) {
        if (teff <= 0) return packRGB(255, 255, 255);

        // Blackbody color approximation
        double t = teff / 100.0;

        int r, g, b;

        // Red
        if (t <= 66) {
            r = 255;
        } else {
            r = (int) (329.698727446 * Math.pow(t - 60, -0.1332047592));
        }

        // Green
        if (t <= 66) {
            g = (int) (99.4708025861 * Math.log(t) - 161.1195681661);
        } else {
            g = (int) (288.1221695283 * Math.pow(t - 60, -0.0755148492));
        }

        // Blue
        if (t >= 66) {
            b = 255;
        } else if (t <= 19) {
            b = 0;
        } else {
            b = (int) (138.5177312231 * Math.log(t - 10) - 305.0447927307);
        }

        return packRGB(clamp(r), clamp(g), clamp(b));
    }

    /**
     * Calculate display size from magnitude.
     * Brighter (lower magnitude) = larger.
     */
    public float magnitudeToSize(float apparentMag) {
        // Magnitude -1 -> size 5, magnitude 6 -> size 0.5
        float size = 5.0f - (apparentMag + 1) * 0.6f;
        return Math.max(0.5f, Math.min(5.0f, size));
    }

    private int packRGB(int r, int g, int b) {
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
