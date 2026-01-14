package com.teamgannon.trips.nightsky.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Atmospheric parameters for extinction and scattering calculations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtmosphereModel {
    /** Whether to apply atmospheric effects */
    private boolean enabled;

    /** Extinction coefficient (magnitudes per airmass) */
    private double extinctionCoefficient;

    /** Scale height of atmosphere in km */
    private double scaleHeightKm;

    /** Observer altitude above sea level in km */
    private double observerAltitudeKm;

    /** Apply horizon reddening effect */
    private boolean horizonReddening;

    public static AtmosphereModel earthLike() {
        return AtmosphereModel.builder()
                .enabled(true)
                .extinctionCoefficient(0.2)  // Typical clear night
                .scaleHeightKm(8.5)
                .observerAltitudeKm(0.0)
                .horizonReddening(true)
                .build();
    }

    public static AtmosphereModel none() {
        return AtmosphereModel.builder()
                .enabled(false)
                .build();
    }
}
