package com.teamgannon.trips.solarsystem.rendering;

import com.teamgannon.trips.particlefields.RingConfiguration;
import com.teamgannon.trips.particlefields.RingFieldRenderer;
import com.teamgannon.trips.particlefields.RingType;
import com.teamgannon.trips.particlefields.SolarSystemRingAdapter;
import com.teamgannon.trips.model.PlanetDescription;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Sphere;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Owns the planetary-ring + asteroid/Kuiper belt sub-system of the solar
 * system renderer: Groups, per-planet ring renderers, visibility toggles,
 * and the public add / remove / animate API.
 * <p>
 * Extracted from {@code SolarSystemRenderer} in Phase 4.1.5 of the
 * codebase-review remediation. Holds references to the renderer's
 * {@code planetNodes} and {@code planetDescriptions} maps so it can look up
 * planet positions and metadata when adding rings by name; those maps remain
 * owned by the renderer.
 */
@Slf4j
public final class PlanetaryRingManager {

    private final ScaleManager scaleManager;
    private final Map<String, Sphere> planetNodes;
    private final Map<String, PlanetDescription> planetDescriptions;

    private final Group ringsGroup = new Group();
    private final Group asteroidBeltGroup = new Group();
    private final Group kuiperBeltGroup = new Group();

    private final Map<String, RingFieldRenderer> planetRings = new HashMap<>();
    private final SolarSystemRingAdapter ringAdapter;

    /** Seeded for reproducible particle placement; reseeded per planet/belt by name hash. */
    private final Random ringRandom = new Random(42);

    private boolean showRings = true;
    private boolean showAsteroidBelt = true;
    private boolean showKuiperBelt = true;

    public PlanetaryRingManager(ScaleManager scaleManager,
                                Map<String, Sphere> planetNodes,
                                Map<String, PlanetDescription> planetDescriptions) {
        this.scaleManager = scaleManager;
        this.planetNodes = planetNodes;
        this.planetDescriptions = planetDescriptions;
        this.ringAdapter = new SolarSystemRingAdapter(scaleManager);
    }

    // ----- group accessors (for the renderer to attach to its systemGroup) -----

    public Group getRingsGroup() { return ringsGroup; }
    public Group getAsteroidBeltGroup() { return asteroidBeltGroup; }
    public Group getKuiperBeltGroup() { return kuiperBeltGroup; }

    /**
     * Exposed so the renderer's feature pass (asteroid-belt + Kuiper-belt
     * features driven by FeatureDescription rows) can build belt configurations
     * with the same adapter used by the ring API. The adapter is stateless
     * w.r.t. the renderer; no concurrency concerns.
     */
    public SolarSystemRingAdapter getRingAdapter() { return ringAdapter; }

    /**
     * Exposed so the renderer's feature pass can reseed the shared {@link Random}
     * by feature id for reproducible particle placement. Callers must
     * {@code setSeed(...)} before {@link RingFieldRenderer#initialize}.
     */
    public Random getRingRandom() { return ringRandom; }

    // ----- visibility toggles -----

    public void setShowRings(boolean show) {
        this.showRings = show;
        ringsGroup.setVisible(show);
    }

    public boolean isShowRings() { return showRings; }

    public void setShowAsteroidBelt(boolean show) {
        this.showAsteroidBelt = show;
        asteroidBeltGroup.setVisible(show);
    }

    public boolean isShowAsteroidBelt() { return showAsteroidBelt; }

    public void setShowKuiperBelt(boolean show) {
        this.showKuiperBelt = show;
        kuiperBeltGroup.setVisible(show);
    }

    public boolean isShowKuiperBelt() { return showKuiperBelt; }

    // ----- inline ring render (called during renderPlanet) -----

    /**
     * Render a per-planet ring system (Saturn/Uranus/Neptune/Custom presets)
     * at the planet's screen position. Used internally by the renderer's
     * planet pass.
     */
    public void renderPlanetRing(PlanetDescription planet, double[] position, double displayRadius) {
        String ringType = planet.getRingType();
        double innerAU = planet.getRingInnerRadiusAU();
        double outerAU = planet.getRingOuterRadiusAU();

        // Ring radii are stored in AU relative to the planet's centre.
        // Convert via Jupiter-radius proxy: 1 Jupiter radius ≈ 4.778e-4 AU.
        double planetRadiusAU = planet.getRadius() * 4.778e-4;

        double innerRatio = 1.5;
        double outerRatio = 2.5;
        if (innerAU > 0 && outerAU > 0 && outerAU > innerAU && planetRadiusAU > 0) {
            innerRatio = innerAU / planetRadiusAU;
            outerRatio = outerAU / planetRadiusAU;
        }

        // Scale rings to the DISPLAY radius (not physical) so they stay visible.
        double innerScreen = displayRadius * innerRatio;
        double outerScreen = displayRadius * outerRatio;

        Color primaryColor = Color.rgb(230, 220, 200);
        Color secondaryColor = Color.rgb(180, 170, 160);
        int numElements = 8000;

        switch (ringType != null ? ringType.toUpperCase() : "SATURN") {
            case "URANUS" -> {
                primaryColor = Color.rgb(80, 80, 90);
                secondaryColor = Color.rgb(50, 50, 60);
                numElements = 5000;
            }
            case "NEPTUNE" -> {
                primaryColor = Color.rgb(60, 60, 75);
                secondaryColor = Color.rgb(40, 40, 60);
                numElements = 4000;
            }
            case "CUSTOM" -> {
                primaryColor = Color.rgb(74, 74, 74);
                secondaryColor = Color.rgb(58, 58, 58);
                numElements = 3000;
            }
            default -> {
                // Default is Saturn-style: keep the initial colours above.
            }
        }

        double ringWidth = outerScreen - innerScreen;
        double minSize = Math.max(0.3, ringWidth * 0.01);
        double maxSize = Math.max(0.8, ringWidth * 0.03);

        RingConfiguration config = RingConfiguration.builder()
                .type(RingType.PLANETARY_RING)
                .innerRadius(innerScreen)
                .outerRadius(outerScreen)
                .numElements(numElements)
                .minSize(minSize)
                .maxSize(maxSize)
                .thickness(ringWidth * 0.02)
                .maxInclinationDeg(0.5)
                .maxEccentricity(0.01)
                .baseAngularSpeed(0.004)
                .centralBodyRadius(displayRadius)
                .primaryColor(primaryColor)
                .secondaryColor(secondaryColor)
                .name(planet.getName() + " Ring")
                .build();

        RingFieldRenderer renderer = new RingFieldRenderer();
        ringRandom.setSeed(planet.getName().hashCode());
        renderer.initialize(config, ringRandom);
        renderer.setPosition(position[0], position[1], position[2]);

        if (planet.getRingInclination() != 0) {
            log.debug("Ring inclination {} for {} (rotation not yet implemented)",
                    planet.getRingInclination(), planet.getName());
        }

        ringsGroup.getChildren().add(renderer.getGroup());
        planetRings.put(planet.getName(), renderer);

        log.info("Rendered ring for planet '{}': {} - {} screen units, ratio {}x-{}x (type: {})",
                planet.getName(), String.format("%.1f", innerScreen),
                String.format("%.1f", outerScreen),
                String.format("%.2f", innerRatio), String.format("%.2f", outerRatio), ringType);
    }

    // ----- public add / remove / has API -----

    public boolean addPlanetaryRing(String planetName, double innerRadiusAU, double outerRadiusAU, String ringName) {
        Sphere planetSphere = planetNodes.get(planetName);
        if (planetSphere == null) {
            log.warn("Cannot add ring to planet '{}': planet not found", planetName);
            return false;
        }
        RingConfiguration config = ringAdapter.createPlanetaryRing(innerRadiusAU, outerRadiusAU, ringName);
        RingFieldRenderer renderer = new RingFieldRenderer();
        ringRandom.setSeed(planetName.hashCode());
        renderer.initialize(config, ringRandom);
        renderer.setPosition(planetSphere.getTranslateX(), planetSphere.getTranslateY(), planetSphere.getTranslateZ());
        ringsGroup.getChildren().add(renderer.getGroup());
        planetRings.put(planetName, renderer);
        log.info("Added planetary ring '{}' to planet '{}': {} - {} AU",
                ringName, planetName, innerRadiusAU, outerRadiusAU);
        return true;
    }

    public boolean addRingFromPreset(String planetName, String presetName, double innerRadiusAU, double outerRadiusAU) {
        Sphere planetSphere = planetNodes.get(planetName);
        if (planetSphere == null) {
            log.warn("Cannot add ring to planet '{}': planet not found", planetName);
            return false;
        }
        RingConfiguration config = ringAdapter.createAdaptedConfiguration(presetName, innerRadiusAU, outerRadiusAU);
        RingFieldRenderer renderer = new RingFieldRenderer();
        ringRandom.setSeed(planetName.hashCode());
        renderer.initialize(config, ringRandom);
        renderer.setPosition(planetSphere.getTranslateX(), planetSphere.getTranslateY(), planetSphere.getTranslateZ());
        ringsGroup.getChildren().add(renderer.getGroup());
        planetRings.put(planetName, renderer);
        log.info("Added '{}' ring preset to planet '{}'", presetName, planetName);
        return true;
    }

    public boolean addAsteroidBelt(double innerRadiusAU, double outerRadiusAU, String name) {
        RingConfiguration config = ringAdapter.createAsteroidBelt(innerRadiusAU, outerRadiusAU, name);
        RingFieldRenderer renderer = new RingFieldRenderer();
        ringRandom.setSeed(name.hashCode());
        renderer.initialize(config, ringRandom);
        renderer.setPosition(0, 0, 0);
        ringsGroup.getChildren().add(renderer.getGroup());
        planetRings.put("__belt__" + name, renderer);
        log.info("Added asteroid belt '{}': {} - {} AU", name, innerRadiusAU, outerRadiusAU);
        return true;
    }

    public boolean addDebrisDisk(double innerRadiusAU, double outerRadiusAU, String name) {
        RingConfiguration config = ringAdapter.createDebrisDisk(innerRadiusAU, outerRadiusAU, name);
        RingFieldRenderer renderer = new RingFieldRenderer();
        ringRandom.setSeed(name.hashCode());
        renderer.initialize(config, ringRandom);
        renderer.setPosition(0, 0, 0);
        ringsGroup.getChildren().add(renderer.getGroup());
        planetRings.put("__disk__" + name, renderer);
        log.info("Added debris disk '{}': {} - {} AU", name, innerRadiusAU, outerRadiusAU);
        return true;
    }

    public boolean removeRing(String planetName) {
        RingFieldRenderer renderer = planetRings.remove(planetName);
        if (renderer != null) {
            ringsGroup.getChildren().remove(renderer.getGroup());
            renderer.dispose();
            log.info("Removed ring from planet '{}'", planetName);
            return true;
        }
        return false;
    }

    public boolean hasRing(String planetName) {
        return planetRings.containsKey(planetName);
    }

    public RingFieldRenderer getRingRenderer(String planetName) {
        return planetRings.get(planetName);
    }

    // ----- animation -----

    public void updateRings(double timeScale) {
        if (!showRings) {
            return;
        }
        for (RingFieldRenderer renderer : planetRings.values()) {
            renderer.update(timeScale);
        }
    }

    public void refreshRingMeshes() {
        if (!showRings) {
            return;
        }
        for (RingFieldRenderer renderer : planetRings.values()) {
            renderer.refreshMeshes();
        }
    }

    public void updateRingPositions() {
        for (Map.Entry<String, RingFieldRenderer> entry : planetRings.entrySet()) {
            String planetName = entry.getKey();
            // Belts/disks orbit the star at origin — skip them.
            if (planetName.startsWith("__")) {
                continue;
            }
            Sphere planetSphere = planetNodes.get(planetName);
            if (planetSphere != null) {
                RingFieldRenderer renderer = entry.getValue();
                renderer.setPosition(
                        planetSphere.getTranslateX(),
                        planetSphere.getTranslateY(),
                        planetSphere.getTranslateZ());
            }
        }
    }

    /**
     * Auto-add Saturn-style rings to every non-moon planet with mass at or above
     * the threshold (Earth masses). Sized by planet radius, lightly weighted by
     * mass so Jupiter-class planets get larger rings.
     */
    public void addRingsToGasGiants(double massThreshold) {
        for (Map.Entry<String, PlanetDescription> entry : planetDescriptions.entrySet()) {
            String planetName = entry.getKey();
            PlanetDescription planet = entry.getValue();
            if (planet.isMoon()) {
                continue;
            }
            if (planet.getMass() < massThreshold) {
                continue;
            }
            if (hasRing(planetName)) {
                continue;
            }
            // 1.5 - 2.5 planet-radii is the canonical Saturn ring band.
            double planetRadiusAU = planet.getRadius() * 4.2635e-5; // Earth radii → AU
            double innerRadiusAU = planetRadiusAU * 1.5;
            double outerRadiusAU = planetRadiusAU * 2.5;
            // Mass-scaled: Jupiter (~318 Earth masses) anchors the curve.
            double massScale = Math.sqrt(planet.getMass() / 300.0);
            innerRadiusAU *= Math.max(0.5, massScale);
            outerRadiusAU *= Math.max(0.5, massScale);
            addRingFromPreset(planetName, "Saturn Ring", innerRadiusAU, outerRadiusAU);
            log.info("Auto-added ring to gas giant '{}' (mass={} Earth masses)",
                    planetName, planet.getMass());
        }
    }

    // ----- cleanup -----

    /** Dispose all per-planet ring renderers and clear the rings group. */
    public void clearRings() {
        for (RingFieldRenderer renderer : planetRings.values()) {
            renderer.dispose();
        }
        planetRings.clear();
        ringsGroup.getChildren().clear();
    }

    /** Clear belt group contents (asteroid + Kuiper). */
    public void clearBelts() {
        asteroidBeltGroup.getChildren().clear();
        kuiperBeltGroup.getChildren().clear();
    }
}
