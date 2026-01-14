package com.teamgannon.trips.planetary;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.ExoPlanet;
import com.teamgannon.trips.planetarymodelling.SolarSystemDescription;
import lombok.Builder;
import lombok.Data;

/**
 * Data container for planetary view context.
 * Contains all information needed to render the night sky from a planet's surface.
 */
@Data
@Builder
public class PlanetaryContext {

    /**
     * The planet being viewed from
     */
    private ExoPlanet planet;

    /**
     * The solar system containing the planet
     */
    private SolarSystemDescription system;

    /**
     * The host star of the planet
     */
    private StarDisplayRecord hostStar;

    /**
     * Planet's absolute position in light years from Sol.
     * Calculated as: star position + orbital offset (converted from AU to ly)
     */
    private double[] planetPositionLy;

    /**
     * Current viewing azimuth (compass direction) in degrees.
     * 0 = North, 90 = East, 180 = South, 270 = West
     */
    @Builder.Default
    private double viewingAzimuth = 0.0;

    /**
     * Current viewing altitude (up/down angle) in degrees.
     * 0 = horizon, 90 = zenith, -90 = nadir
     */
    @Builder.Default
    private double viewingAltitude = 45.0;

    /**
     * Local time of day in hours (0-24).
     * Affects host star position in sky.
     */
    @Builder.Default
    private double localTime = 12.0;

    /**
     * Magnitude limit for star visibility.
     * Stars dimmer than this limit won't be displayed.
     */
    @Builder.Default
    private double magnitudeLimit = 6.0;

    /**
     * Field of view in degrees.
     */
    @Builder.Default
    private double fieldOfView = 90.0;

    /**
     * Whether to show atmospheric effects (horizon glow, etc.)
     */
    @Builder.Default
    private boolean showAtmosphereEffects = true;

    /**
     * Get the planet name for display purposes.
     */
    public String getPlanetName() {
        return planet != null ? planet.getName() : "Unknown";
    }

    /**
     * Get the host star name for display purposes.
     */
    public String getHostStarName() {
        if (hostStar != null) {
            return hostStar.getStarName();
        }
        return planet != null ? planet.getStarName() : "Unknown";
    }
}
