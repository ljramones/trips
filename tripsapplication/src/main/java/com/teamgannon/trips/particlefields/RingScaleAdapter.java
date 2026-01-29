package com.teamgannon.trips.particlefields;

/**
 * Interface for adapting ring configurations between different coordinate systems.
 *
 * <p>The ring system internally uses "visual units" (typically ~100 for outer radius).
 * Different views use different real-world units:
 * <ul>
 *   <li>Solar system view: Astronomical Units (AU)</li>
 *   <li>Interstellar view: Light-years (ly)</li>
 * </ul>
 *
 * <p>Adapters convert between real-world units and the visual units expected
 * by {@link RingFieldRenderer}, allowing the same ring system to be used
 * in different contexts with appropriate scaling.
 */
public interface RingScaleAdapter {

    /**
     * Converts a distance from real-world units to visual/screen units.
     *
     * @param realWorldValue distance in real-world units (AU, ly, etc.)
     * @return distance in visual/screen units
     */
    double toVisualUnits(double realWorldValue);

    /**
     * Converts a distance from visual/screen units to real-world units.
     *
     * @param visualValue distance in visual/screen units
     * @return distance in real-world units (AU, ly, etc.)
     */
    double fromVisualUnits(double visualValue);

    /**
     * Adapts a ring configuration from real-world units to visual units.
     * Creates a new configuration with scaled radii, thickness, and sizes.
     *
     * @param config configuration with values in real-world units
     * @return new configuration with values in visual/screen units
     */
    RingConfiguration adaptConfiguration(RingConfiguration config);

    /**
     * Creates a ring configuration in real-world units from a preset,
     * then adapts it to visual units.
     *
     * @param presetName name of the preset
     * @param innerRadius inner radius in real-world units
     * @param outerRadius outer radius in real-world units
     * @return adapted configuration in visual units
     */
    RingConfiguration createAdaptedConfiguration(
            String presetName,
            double innerRadius,
            double outerRadius
    );

    /**
     * Returns the name of the unit system this adapter handles.
     *
     * @return unit system name (e.g., "AU", "Light-years")
     */
    String getUnitName();

    /**
     * Returns a typical scale factor for reference.
     * This is the approximate visual units per real-world unit.
     *
     * @return scale factor
     */
    double getScaleFactor();
}
