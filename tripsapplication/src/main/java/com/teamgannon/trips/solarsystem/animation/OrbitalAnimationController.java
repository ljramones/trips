package com.teamgannon.trips.solarsystem.animation;

import com.teamgannon.trips.planetarymodelling.PlanetDescription;
import com.teamgannon.trips.solarsystem.orbits.KeplerOrbitSamplingProvider;
import com.teamgannon.trips.solarsystem.orbits.OrbitSamplingProvider;
import com.teamgannon.trips.solarsystem.rendering.SolarSystemRenderer;
import javafx.animation.AnimationTimer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controls orbital animation for the solar system view.
 * Manages the animation loop, time model, and coordinates position updates.
 */
@Slf4j
public class OrbitalAnimationController {

    /**
     * Amplification factor for moon orbits to make them visible.
     * Must match the value in SolarSystemRenderer.
     */
    private static final double MOON_ORBIT_AMPLIFICATION = 15.0;

    private final AnimationTimer timer;
    private final AnimationTimeModel timeModel;
    private final SolarSystemRenderer renderer;
    private final OrbitSamplingProvider orbitSampler;
    private final Runnable onUpdate;

    @Getter
    private boolean running;

    private List<PlanetDescription> planets;

    /**
     * Create a new animation controller.
     *
     * @param renderer the solar system renderer
     * @param onUpdate callback to run after each position update (e.g., updateLabels)
     */
    public OrbitalAnimationController(SolarSystemRenderer renderer, Runnable onUpdate) {
        this.renderer = renderer;
        this.onUpdate = onUpdate;
        this.timeModel = new AnimationTimeModel();
        this.orbitSampler = new KeplerOrbitSamplingProvider();
        this.running = false;

        this.timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (running) {
                    updateFrame(now);
                }
            }
        };
    }

    /**
     * Set the planets to animate.
     *
     * @param planets list of planet descriptions with orbital elements
     */
    public void setPlanets(List<PlanetDescription> planets) {
        this.planets = planets;
        log.info("Animation controller configured with {} planets", planets != null ? planets.size() : 0);
    }

    /**
     * Update one frame of animation.
     *
     * @param nowNanos current time in nanoseconds
     */
    private void updateFrame(long nowNanos) {
        if (planets == null || planets.isEmpty()) {
            log.warn("No planets to animate");
            return;
        }

        timeModel.update(nowNanos);
        double elapsedDays = timeModel.getElapsedDays();

        Map<String, double[]> newPositions = new HashMap<>();

        Map<String, PlanetDescription> planetsById = new HashMap<>();
        for (PlanetDescription planet : planets) {
            planetsById.put(planet.getId(), planet);
        }

        for (PlanetDescription planet : planets) {
            if (planet.isMoon()) {
                continue;
            }
            double trueAnomaly = calculateTrueAnomaly(planet, elapsedDays);

            double[] posAu = orbitSampler.calculatePositionAu(
                    planet.getSemiMajorAxis(),
                    planet.getEccentricity(),
                    planet.getInclination(),
                    planet.getLongitudeOfAscendingNode(),
                    planet.getArgumentOfPeriapsis(),
                    trueAnomaly
            );

            newPositions.put(planet.getName(), posAu);

            // Debug logging (every ~60 frames = 1 second)
            if (frameCount % 60 == 0 && planet == planets.get(0)) {
                log.debug("Animation frame: elapsedDays={}, planet={}, trueAnomaly={}, pos=[{},{},{}]",
                        "%.2f".formatted(elapsedDays),
                        planet.getName(),
                        "%.2f".formatted(trueAnomaly),
                        "%.4f".formatted(posAu[0]),
                        "%.4f".formatted(posAu[1]),
                        "%.4f".formatted(posAu[2]));
            }
        }

        for (PlanetDescription moon : planets) {
            if (!moon.isMoon()) {
                continue;
            }
            PlanetDescription parent = planetsById.get(moon.getParentPlanetId());
            if (parent == null) {
                continue;
            }
            double[] parentPos = newPositions.get(parent.getName());
            if (parentPos == null) {
                continue;
            }

            double trueAnomaly = calculateTrueAnomaly(moon, elapsedDays);
            // Use amplified SMA for moons so they appear on the visible orbit
            double amplifiedSma = moon.getSemiMajorAxis() * MOON_ORBIT_AMPLIFICATION;
            double[] offset = orbitSampler.calculatePositionAu(
                    amplifiedSma,
                    moon.getEccentricity(),
                    moon.getInclination(),
                    moon.getLongitudeOfAscendingNode(),
                    moon.getArgumentOfPeriapsis(),
                    trueAnomaly
            );

            newPositions.put(moon.getName(), new double[] {
                    parentPos[0] + offset[0],
                    parentPos[1] + offset[1],
                    parentPos[2] + offset[2]
            });
        }
        frameCount++;

        renderer.updatePlanetPositions(newPositions);

        if (onUpdate != null) {
            onUpdate.run();
        }
    }

    private long frameCount = 0;

    /**
     * Calculate the true anomaly for a planet at a given elapsed time.
     * Uses Kepler's equation to convert mean anomaly to true anomaly.
     *
     * @param planet      the planet description
     * @param elapsedDays elapsed simulation days since epoch
     * @return true anomaly in degrees
     */
    private double calculateTrueAnomaly(PlanetDescription planet, double elapsedDays) {
        double period = planet.getOrbitalPeriod(); // in days
        if (period <= 0) {
            // Fallback: estimate period from semi-major axis using Kepler's 3rd law
            // P^2 = a^3 (for solar mass star, P in years, a in AU)
            double a = planet.getSemiMajorAxis();
            period = Math.pow(a, 1.5) * 365.25; // days
        }

        // Mean motion (degrees per day)
        double meanMotion = 360.0 / period;

        // Mean anomaly at current time (in degrees)
        double meanAnomaly = (meanMotion * elapsedDays) % 360.0;
        if (meanAnomaly < 0) meanAnomaly += 360.0;

        // Convert to true anomaly via eccentric anomaly
        double eccentricity = Math.max(0, Math.min(0.99, planet.getEccentricity()));
        double trueAnomaly = meanToTrueAnomaly(Math.toRadians(meanAnomaly), eccentricity);

        return Math.toDegrees(trueAnomaly);
    }

    /**
     * Convert mean anomaly to true anomaly using Kepler's equation.
     * Uses Newton-Raphson iteration to solve for eccentric anomaly.
     *
     * @param meanAnomalyRad mean anomaly in radians
     * @param eccentricity   orbital eccentricity
     * @return true anomaly in radians
     */
    private double meanToTrueAnomaly(double meanAnomalyRad, double eccentricity) {
        // Solve Kepler's equation: M = E - e*sin(E)
        // Using Newton-Raphson iteration
        double E = meanAnomalyRad; // Initial guess
        for (int i = 0; i < 10; i++) {
            double deltaE = (E - eccentricity * Math.sin(E) - meanAnomalyRad)
                    / (1 - eccentricity * Math.cos(E));
            E -= deltaE;
            if (Math.abs(deltaE) < 1e-10) break;
        }

        // Convert eccentric anomaly to true anomaly
        // tan(v/2) = sqrt((1+e)/(1-e)) * tan(E/2)
        double sinE = Math.sin(E);
        double cosE = Math.cos(E);
        double trueAnomaly = Math.atan2(
                Math.sqrt(1 - eccentricity * eccentricity) * sinE,
                cosE - eccentricity
        );

        return trueAnomaly;
    }

    /**
     * Start or resume animation.
     */
    public void play() {
        running = true;
        timer.start();
        log.info("Animation started at speed {}x with {} planets",
                timeModel.getSpeedMultiplier(),
                planets != null ? planets.size() : 0);
        if (planets != null && !planets.isEmpty()) {
            PlanetDescription first = planets.get(0);
            log.info("First planet: {} sma={} AU, period={} days, ecc={}",
                    first.getName(),
                    first.getSemiMajorAxis(),
                    first.getOrbitalPeriod(),
                    first.getEccentricity());
        }
    }

    /**
     * Pause animation without resetting time.
     */
    public void pause() {
        running = false;
        log.info("Animation paused at simulation time: {}", timeModel.getFormattedDate());
    }

    /**
     * Toggle between play and pause.
     */
    public void togglePlayPause() {
        if (running) {
            pause();
        } else {
            play();
        }
    }

    /**
     * Stop animation and reset to epoch.
     */
    public void stop() {
        running = false;
        timer.stop();
        timeModel.reset();
        log.info("Animation stopped and reset to epoch");

        // Update positions to initial state
        if (planets != null && !planets.isEmpty()) {
            updateFrame(System.nanoTime());
        }
    }

    /**
     * Set animation speed.
     *
     * @param speedMultiplier speed multiplier (1 = real-time, 86400 = 1 day/sec)
     */
    public void setSpeed(double speedMultiplier) {
        timeModel.setSpeedMultiplier(speedMultiplier);
    }

    /**
     * Get the current animation speed.
     *
     * @return speed multiplier
     */
    public double getSpeed() {
        return timeModel.getSpeedMultiplier();
    }

    /**
     * Get the time model for external access (e.g., displaying current date).
     *
     * @return the animation time model
     */
    public AnimationTimeModel getTimeModel() {
        return timeModel;
    }

    /**
     * Clean up resources when done.
     */
    public void dispose() {
        stop();
        timer.stop();
    }
}
