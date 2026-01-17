package com.teamgannon.trips.planetarymodelling.procedural;

import com.teamgannon.trips.solarsysmodelling.accrete.Planet;

import java.util.Random;

/**
 * Computes tectonic bias parameters from Accrete planet properties.
 * These parameters influence plate tectonics simulation to produce
 * geologically plausible results based on planetary physics.
 */
public record TectonicBias(
    int minPlateCount,
    int maxPlateCount,
    double oceanicPlateRatio,
    double mountainHeightMultiplier,
    double riftDepthMultiplier,
    double hotspotProbability,
    boolean hasActivePlateTectonics
) {

    /**
     * Creates tectonic bias parameters from an Accrete-generated planet.
     * Uses mass, radius, gravity, hydrosphere, temperature, and stellar age
     * to derive geologically reasonable tectonic behavior.
     *
     * @param p The Accrete planet
     * @return TectonicBias with computed parameters
     */
    public static TectonicBias fromAccretePlanet(Planet p) {
        // Gas giants have no solid surface tectonics
        if (p.isGasGiant()) {
            return new TectonicBias(0, 0, 0.0, 0.0, 0.0, 0.0, false);
        }

        double mass = p.massInEarthMasses();
        double radiusRatio = p.ratioRadiusToEarth();
        double gravity = p.getSurfaceGravity();
        double hydrosphere = p.getHydrosphere() / 100.0;  // Convert from percentage
        double surfaceTemp = p.getSurfaceTemperature();
        double stellarAge = p.getPrimary().getAge();  // In years

        // --- Plate Count ---
        // More massive planets have more internal heat → more plates
        // Earth (~1.0 mass) has ~15 major plates
        int minPlates, maxPlates;
        if (mass < 0.3) {
            // Small planets: limited internal heat, fewer plates
            minPlates = 3;
            maxPlates = 7;
        } else if (mass < 0.7) {
            minPlates = 5;
            maxPlates = 10;
        } else if (mass < 1.5) {
            // Earth-like range
            minPlates = 8;
            maxPlates = 16;
        } else if (mass < 3.0) {
            // Super-Earths: vigorous convection
            minPlates = 12;
            maxPlates = 21;
        } else {
            // Massive super-Earths: may transition to stagnant lid
            minPlates = 7;
            maxPlates = 14;
        }

        // --- Oceanic Plate Ratio ---
        // More water → more oceanic crust
        // Also factor in surface area (larger planets can have more ocean basins)
        double oceanicRatio = 0.5 + (hydrosphere * 0.4);  // 0.5-0.9 range based on water
        oceanicRatio = Math.min(0.85, Math.max(0.3, oceanicRatio));

        // --- Mountain Height Multiplier ---
        // Higher gravity compresses mountains: h ∝ 1/g
        // Lower gravity allows taller mountains (Mars's Olympus Mons)
        double heightMultiplier = 1.0 / Math.sqrt(gravity);
        // Clamp to reasonable range
        heightMultiplier = Math.min(2.0, Math.max(0.5, heightMultiplier));

        // Water erosion also affects mountains
        if (hydrosphere > 0.5) {
            heightMultiplier *= 0.9;  // Erosion reduces peak heights
        }

        // --- Rift Depth Multiplier ---
        // Gravity affects rift/trench depth similarly
        // But water-rich planets have deeper ocean trenches
        double riftMultiplier = 1.0 / Math.sqrt(gravity);
        if (hydrosphere > 0.3) {
            riftMultiplier *= 1.0 + (hydrosphere * 0.3);  // Water enhances trenches
        }
        riftMultiplier = Math.min(2.0, Math.max(0.5, riftMultiplier));

        // --- Hotspot Probability ---
        // Internal heat drives hotspots; decreases with age
        // Young planets have more volcanic activity
        double ageGyr = stellarAge / 1e9;
        double hotspotProb;
        if (ageGyr < 1.0) {
            hotspotProb = 0.25;  // Young, very active
        } else if (ageGyr < 3.0) {
            hotspotProb = 0.18;
        } else if (ageGyr < 6.0) {
            hotspotProb = 0.12;  // Earth-like age
        } else {
            hotspotProb = 0.06;  // Old, cooling
        }

        // Mass affects internal heat retention
        if (mass > 1.5) {
            hotspotProb *= 1.3;  // Larger planets retain heat longer
        } else if (mass < 0.5) {
            hotspotProb *= 0.6;  // Small planets cool faster
        }
        hotspotProb = Math.min(0.4, Math.max(0.02, hotspotProb));

        // --- Active Plate Tectonics ---
        // Requires: sufficient mass (internal heat), water (lubrication),
        // not too old (mantle not frozen), reasonable temperature
        boolean activeTectonics = true;

        // Too small → stagnant lid (like Mars)
        if (mass < 0.2) {
            activeTectonics = false;
        }

        // Too little water → stagnant lid (water lubricates subduction)
        if (hydrosphere < 0.05) {
            activeTectonics = false;
        }

        // Very old planets may have cooled to stagnant lid
        if (ageGyr > 10.0 && mass < 1.0) {
            activeTectonics = false;
        }

        // Venus-like conditions: too hot, thick atmosphere prevents subduction
        if (surfaceTemp > 700 && hydrosphere < 0.01) {
            activeTectonics = false;
        }

        // Frozen worlds: too cold for mantle convection at surface
        if (surfaceTemp < 150) {
            activeTectonics = false;
        }

        return new TectonicBias(
            minPlates, maxPlates,
            oceanicRatio,
            heightMultiplier,
            riftMultiplier,
            hotspotProb,
            activeTectonics
        );
    }

    /**
     * Applies this bias to a base PlanetConfig, returning a new config
     * with tectonic parameters set according to the bias.
     *
     * @param base         The base configuration to modify
     * @param overrideSeed Seed for random plate count selection
     * @return New PlanetConfig with bias applied
     */
    public PlanetConfig applyTo(PlanetConfig base, long overrideSeed) {
        if (!hasActivePlateTectonics || maxPlateCount == 0) {
            // Stagnant lid mode: minimal "plates", reduced activity
            return base.toBuilder()
                .seed(overrideSeed)
                .plateCount(Math.max(1, minPlateCount))
                .oceanicPlateRatio(oceanicPlateRatio)
                .heightScaleMultiplier(mountainHeightMultiplier * 0.5)  // Less mountain building
                .riftDepthMultiplier(riftDepthMultiplier * 0.3)         // Minimal rifting
                .hotspotProbability(hotspotProbability * 1.5)           // More hotspot volcanism
                .enableActiveTectonics(false)
                .build();
        }

        // Active tectonics: select plate count randomly within range
        Random rng = new Random(overrideSeed);
        int plateCount = minPlateCount + rng.nextInt(Math.max(1, maxPlateCount - minPlateCount + 1));

        return base.toBuilder()
            .seed(overrideSeed)
            .plateCount(plateCount)
            .oceanicPlateRatio(oceanicPlateRatio)
            .heightScaleMultiplier(mountainHeightMultiplier)
            .riftDepthMultiplier(riftDepthMultiplier)
            .hotspotProbability(hotspotProbability)
            .enableActiveTectonics(true)
            .build();
    }

    /**
     * Creates a default Earth-like bias for testing or fallback.
     */
    public static TectonicBias earthLike() {
        return new TectonicBias(
            10, 18,     // plate count range
            0.65,       // oceanic ratio (Earth is ~60% oceanic crust)
            1.0,        // height multiplier
            1.0,        // rift multiplier
            0.12,       // hotspot probability
            true        // active tectonics
        );
    }

    /**
     * Creates a Mars-like stagnant lid bias.
     */
    public static TectonicBias marsLike() {
        return new TectonicBias(
            1, 3,       // minimal plates (stagnant lid)
            0.0,        // no ocean
            1.5,        // taller mountains (lower gravity)
            0.5,        // minimal rifting
            0.08,       // some hotspot volcanism
            false       // no active plate tectonics
        );
    }

    /**
     * Creates a Venus-like stagnant lid bias with volcanism.
     */
    public static TectonicBias venusLike() {
        return new TectonicBias(
            2, 5,       // few plates
            0.0,        // no ocean
            0.9,        // similar gravity to Earth
            0.3,        // minimal rifting
            0.20,       // high volcanic activity
            false       // stagnant lid
        );
    }

    @Override
    public String toString() {
        return String.format(
            "TectonicBias[plates=%d-%d, oceanicRatio=%.2f, heightMult=%.2f, " +
            "riftMult=%.2f, hotspotProb=%.2f, activeTectonics=%s]",
            minPlateCount, maxPlateCount, oceanicPlateRatio,
            mountainHeightMultiplier, riftDepthMultiplier,
            hotspotProbability, hasActivePlateTectonics
        );
    }
}
