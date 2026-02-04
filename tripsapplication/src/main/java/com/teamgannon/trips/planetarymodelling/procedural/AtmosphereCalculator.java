package com.teamgannon.trips.planetarymodelling.procedural;

import com.cognitivedynamics.noisegen.FastNoiseLite;
import com.cognitivedynamics.noisegen.spatial.TurbulenceNoise;
import org.hipparchus.geometry.euclidean.threed.Vector3D;

import java.util.List;

/**
 * Calculates atmospheric flow fields for procedural planets using curl noise.
 *
 * <p>Provides divergence-free (incompressible) flow fields suitable for:
 * <ul>
 *   <li>Cloud motion and advection</li>
 *   <li>Atmospheric circulation patterns</li>
 *   <li>Storm system visualization</li>
 *   <li>Wind field generation</li>
 * </ul>
 *
 * <p>Uses curl noise to generate physically plausible flow that:
 * <ul>
 *   <li>Has no divergence (mass-conserving)</li>
 *   <li>Creates swirling, turbulent patterns</li>
 *   <li>Can be animated over time for dynamic clouds</li>
 * </ul>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * AtmosphereCalculator atmo = new AtmosphereCalculator(config);
 *
 * // Get wind velocity at a point on the planet surface
 * float[] wind = atmo.getWindVelocity(polygon.center(), time);
 *
 * // Get cloud density for rendering
 * float density = atmo.getCloudDensity(polygon.center(), time);
 *
 * // Advect a point along the flow field
 * Vector3D newPos = atmo.advect(position, deltaTime);
 * }</pre>
 *
 * <p><b>Future Integration:</b> This class is prepared for integration with
 * atmospheric rendering systems. When atmospheric visualization is implemented,
 * use {@link #calculateAtmosphereForPolygons} to generate per-polygon atmosphere data.
 */
public class AtmosphereCalculator {

    // Atmospheric flow field generator using curl noise
    private final TurbulenceNoise windNoise;
    private final TurbulenceNoise cloudNoise;

    // Configuration
    private final PlanetConfig config;
    private final float baseWindSpeed;
    private final float cloudFrequency;

    // Physical parameters
    private static final float CORIOLIS_STRENGTH = 0.5f;  // Coriolis effect strength

    /**
     * Result of atmosphere calculations for a polygon.
     *
     * @param windVelocity    3D wind velocity vector (tangent to surface)
     * @param cloudDensity    Cloud density [0, 1]
     * @param stormIntensity  Storm intensity [0, 1] (for severe weather visualization)
     */
    public record AtmosphereData(
        float[] windVelocity,
        float cloudDensity,
        float stormIntensity
    ) {}

    /**
     * Creates atmosphere calculator with default settings.
     *
     * @param config Planet configuration (provides seed)
     */
    public AtmosphereCalculator(PlanetConfig config) {
        this(config, 1.0f, 2.0f);
    }

    /**
     * Creates atmosphere calculator with custom parameters.
     *
     * @param config         Planet configuration
     * @param baseWindSpeed  Base wind speed multiplier
     * @param cloudFrequency Cloud pattern frequency
     */
    public AtmosphereCalculator(PlanetConfig config, float baseWindSpeed, float cloudFrequency) {
        this.config = config;
        this.baseWindSpeed = baseWindSpeed;
        this.cloudFrequency = cloudFrequency;

        // Initialize wind field noise (curl noise for divergence-free flow)
        FastNoiseLite windBase = new FastNoiseLite((int) config.subSeed(10));
        windBase.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        this.windNoise = new TurbulenceNoise(windBase, 0.5f);
        this.windNoise.setLacunarity(2.0f);
        this.windNoise.setPersistence(0.5f);

        // Initialize cloud density noise (turbulence for billowy clouds)
        FastNoiseLite cloudBase = new FastNoiseLite((int) config.subSeed(11));
        cloudBase.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        this.cloudNoise = new TurbulenceNoise(cloudBase, cloudFrequency);
        this.cloudNoise.setLacunarity(2.2f);
        this.cloudNoise.setPersistence(0.45f);
    }

    /**
     * Get wind velocity at a point on the planet surface.
     *
     * <p>Returns a 3D velocity vector tangent to the surface, suitable for
     * advecting particles or displacing clouds.
     *
     * @param position 3D position on the unit sphere
     * @param time     Animation time (for dynamic flow)
     * @return float[3] containing (vx, vy, vz) wind velocity
     */
    public float[] getWindVelocity(Vector3D position, float time) {
        Vector3D normalized = position.normalize();
        float x = (float) normalized.getX();
        float y = (float) normalized.getY();
        float z = (float) normalized.getZ();

        // Sample curl noise for base wind (3 octaves for detail)
        float[] curl = windNoise.curlFBm3D(x + time * 0.01f, y, z, 3);

        // Apply Coriolis effect based on latitude
        // Higher latitudes have stronger deflection
        double latitude = Math.asin(y);  // [-π/2, π/2]
        float coriolisFactor = (float) Math.sin(latitude) * CORIOLIS_STRENGTH;

        // Adjust wind components for Coriolis (deflects wind perpendicular to motion)
        float vx = curl[0] + coriolisFactor * curl[2];
        float vy = curl[1];  // Vertical component less affected
        float vz = curl[2] - coriolisFactor * curl[0];

        // Scale by base wind speed
        vx *= baseWindSpeed;
        vy *= baseWindSpeed * 0.5f;  // Reduce vertical
        vz *= baseWindSpeed;

        // Project onto tangent plane (remove radial component)
        float dot = vx * x + vy * y + vz * z;
        vx -= dot * x;
        vy -= dot * y;
        vz -= dot * z;

        return new float[] { vx, vy, vz };
    }

    /**
     * Get cloud density at a point.
     *
     * <p>Returns a density value suitable for rendering clouds or weather patterns.
     *
     * @param position 3D position on the unit sphere
     * @param time     Animation time (for moving clouds)
     * @return Cloud density in range [0, 1]
     */
    public float getCloudDensity(Vector3D position, float time) {
        Vector3D normalized = position.normalize();
        float x = (float) normalized.getX();
        float y = (float) normalized.getY();
        float z = (float) normalized.getZ();

        // Use warped turbulence for organic cloud shapes
        float density = cloudNoise.warpedTurbulence(
            x + time * 0.02f, y, z + time * 0.015f, 4, 0.3f);

        // Apply latitude-based cloud cover (more clouds at temperate latitudes)
        double absLat = Math.abs(Math.asin(y));
        float latFactor = 1.0f - (float) Math.pow(Math.abs(absLat - 0.7), 2) * 2f;
        latFactor = Math.max(0.3f, Math.min(1.0f, latFactor));

        return Math.max(0f, Math.min(1f, density * latFactor));
    }

    /**
     * Get storm intensity at a point.
     *
     * <p>Higher values indicate severe weather (thunderstorms, hurricanes).
     * Useful for weather visualization or gameplay effects.
     *
     * @param position 3D position
     * @param time     Animation time
     * @return Storm intensity in range [0, 1]
     */
    public float getStormIntensity(Vector3D position, float time) {
        Vector3D normalized = position.normalize();
        float x = (float) normalized.getX();
        float y = (float) normalized.getY();
        float z = (float) normalized.getZ();

        // Storms form from high curl magnitude (strong rotation)
        float[] curl = windNoise.curl3D(x + time * 0.005f, y, z);
        float curlMag = (float) Math.sqrt(curl[0]*curl[0] + curl[1]*curl[1] + curl[2]*curl[2]);

        // Storms more likely at certain latitudes (hurricane belt ~10-30°)
        double absLat = Math.abs(Math.asin(y));
        float stormBelt = 0f;
        if (absLat > 0.15 && absLat < 0.55) {  // ~10° to ~30° latitude
            stormBelt = 1.0f - (float) Math.abs(absLat - 0.35) * 3f;
        }

        // Combine rotation strength with geographic likelihood
        float intensity = curlMag * 2f * (0.3f + stormBelt * 0.7f);
        return Math.max(0f, Math.min(1f, intensity));
    }

    /**
     * Advect a position along the wind field.
     *
     * <p>Useful for moving particles (cloud puffs, tracers) along the flow.
     *
     * @param position  Current position
     * @param deltaTime Time step
     * @param time      Current animation time
     * @return New position after advection
     */
    public Vector3D advect(Vector3D position, float deltaTime, float time) {
        float[] vel = getWindVelocity(position, time);
        Vector3D velocity = new Vector3D(vel[0], vel[1], vel[2]);

        // Euler integration step
        Vector3D newPos = position.add(velocity.scalarMultiply(deltaTime));

        // Re-project onto unit sphere
        return newPos.normalize();
    }

    /**
     * Calculate atmosphere data for all polygons.
     *
     * <p>Generates per-polygon atmosphere data for rendering integration.
     *
     * @param polygons Mesh polygons
     * @param time     Animation time
     * @return Array of atmosphere data per polygon
     */
    public AtmosphereData[] calculateAtmosphereForPolygons(List<Polygon> polygons, float time) {
        AtmosphereData[] data = new AtmosphereData[polygons.size()];

        for (int i = 0; i < polygons.size(); i++) {
            Vector3D center = polygons.get(i).center();
            float[] wind = getWindVelocity(center, time);
            float clouds = getCloudDensity(center, time);
            float storm = getStormIntensity(center, time);
            data[i] = new AtmosphereData(wind, clouds, storm);
        }

        return data;
    }

    /**
     * Get the wind noise generator for advanced usage.
     */
    public TurbulenceNoise getWindNoise() {
        return windNoise;
    }

    /**
     * Get the cloud noise generator for advanced usage.
     */
    public TurbulenceNoise getCloudNoise() {
        return cloudNoise;
    }
}
