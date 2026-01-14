package com.teamgannon.trips.nightsky.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A star point ready for rendering on the sky dome.
 * All calculations complete - just needs to be drawn.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkyStarPoint {
    /** Azimuth in radians (0 = North, π/2 = East) */
    private double azRad;

    /** Altitude in radians (0 = horizon, π/2 = zenith) */
    private double altRad;

    /** Apparent magnitude as seen from observer */
    private float apparentMag;

    /** ARGB color value for rendering */
    private int color;

    /** Original star ID for click-through */
    private long starId;

    /** Star name for tooltip */
    private String starName;

    /** Distance from observer in light years */
    private double distanceLy;
}
