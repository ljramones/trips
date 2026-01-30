package com.teamgannon.trips.solarsysmodelling.accrete;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.Math.*;

/**
 * Generates post-accretion structures: planetary rings, asteroid belts, and Kuiper belts.
 *
 * These structures don't form through the standard ACCRETE dust accretion process but
 * are instead consequences of:
 * - Gravitational perturbation preventing accretion (asteroid belts)
 * - Primordial material beyond planetary formation zone (Kuiper belt)
 * - Tidal disruption and captured material (planetary rings)
 *
 * This generator should be called after setEnvironments() when all planet properties are finalized.
 */
@Slf4j
@Data
public class PostAccretionGenerator {

    // Ring probability by planet type
    private static final double GAS_GIANT_RING_PROBABILITY = 0.85;      // Jupiter, Saturn-like
    private static final double ICE_GIANT_RING_PROBABILITY = 0.60;      // Uranus, Neptune-like
    private static final double TERRESTRIAL_RING_PROBABILITY = 0.02;    // Very rare

    // Ring type probabilities for gas giants (must sum to 1.0)
    private static final double SATURN_LIKE_RING_PROBABILITY = 0.25;    // Bright, icy, prominent
    private static final double URANUS_LIKE_RING_PROBABILITY = 0.35;    // Dark, narrow
    private static final double NEPTUNE_LIKE_RING_PROBABILITY = 0.25;   // Faint, dusty
    private static final double JUPITER_LIKE_RING_PROBABILITY = 0.15;   // Very faint

    // Mass thresholds (in Earth masses)
    private static final double GAS_GIANT_MASS_THRESHOLD = 50.0;        // Saturn is ~95 Earth masses
    private static final double ICE_GIANT_MASS_THRESHOLD = 10.0;        // Neptune is ~17 Earth masses

    // Belt generation parameters
    private static final double ASTEROID_BELT_GAP_RATIO_MIN = 0.35;     // Belt inner edge as fraction of giant SMA
    private static final double ASTEROID_BELT_GAP_RATIO_MAX = 0.65;     // Belt outer edge as fraction of giant SMA
    private static final double KUIPER_BELT_INNER_RATIO = 1.0;          // Inner edge relative to outer giant
    private static final double KUIPER_BELT_OUTER_RATIO = 1.7;          // Outer edge relative to outer giant
    private static final double MIN_GIANT_SMA_FOR_KUIPER = 5.0;         // AU - minimum outer giant distance

    private final Random random;
    private final SimStar centralBody;

    // Generated structures
    private final List<RingData> planetaryRings = new ArrayList<>();
    private AsteroidBeltData asteroidBelt = null;
    private KuiperBeltData kuiperBelt = null;

    public PostAccretionGenerator(SimStar centralBody, long seed) {
        this.centralBody = centralBody;
        this.random = new Random(seed);
    }

    public PostAccretionGenerator(SimStar centralBody, Random random) {
        this.centralBody = centralBody;
        this.random = random;
    }

    /**
     * Generate all post-accretion structures based on the planet configuration.
     *
     * @param planets the list of planets (after setEnvironments)
     */
    public void generate(List<Planet> planets) {
        if (planets == null || planets.isEmpty()) {
            log.debug("No planets to analyze for post-accretion structures");
            return;
        }

        // Sort planets by semi-major axis for analysis
        List<Planet> sortedPlanets = new ArrayList<>(planets);
        sortedPlanets.sort((a, b) -> Double.compare(a.getSma(), b.getSma()));

        // Find giant planets
        Planet innermostGiant = null;
        Planet outermostGiant = null;

        for (Planet p : sortedPlanets) {
            if (isGiantPlanet(p)) {
                if (innermostGiant == null) {
                    innermostGiant = p;
                }
                outermostGiant = p;
            }
        }

        // Generate asteroid belt if conditions are met
        if (innermostGiant != null) {
            generateAsteroidBelt(sortedPlanets, innermostGiant);
        }

        // Generate Kuiper belt if conditions are met
        if (outermostGiant != null) {
            generateKuiperBelt(sortedPlanets, outermostGiant);
        }

        // Generate rings for each giant planet
        for (Planet p : sortedPlanets) {
            if (isGiantPlanet(p)) {
                generatePlanetaryRing(p);
            }
        }

        logGeneratedStructures();
    }

    /**
     * Determine if a planet qualifies as a giant planet.
     */
    private boolean isGiantPlanet(Planet p) {
        return p.isGasGiant() || p.massInEarthMasses() >= ICE_GIANT_MASS_THRESHOLD;
    }

    /**
     * Generate an asteroid belt in the gap before the innermost giant planet,
     * if no planet exists in that region.
     */
    private void generateAsteroidBelt(List<Planet> planets, Planet innermostGiant) {
        double giantSma = innermostGiant.getSma();

        // Scale belt location by stellar luminosity (more luminous = wider spacing)
        double luminosityFactor = sqrt(centralBody.luminosity);

        // Calculate belt region (roughly 3:1 resonance zone with giant)
        double beltInner = giantSma * ASTEROID_BELT_GAP_RATIO_MIN;
        double beltOuter = giantSma * ASTEROID_BELT_GAP_RATIO_MAX;

        // Check if any planet exists in this region
        boolean regionClear = true;
        for (Planet p : planets) {
            if (p == innermostGiant) continue;

            // Check if planet's orbit overlaps with belt region
            double periapsis = p.getSma() * (1 - p.getEccentricity());
            double apoapsis = p.getSma() * (1 + p.getEccentricity());

            if (periapsis < beltOuter && apoapsis > beltInner) {
                regionClear = false;
                log.debug("Planet at {} AU blocks asteroid belt region {}-{} AU",
                        String.format("%.2f", p.getSma()),
                        String.format("%.2f", beltInner),
                        String.format("%.2f", beltOuter));
                break;
            }
        }

        if (regionClear && beltInner > 0.3 * luminosityFactor) {
            asteroidBelt = new AsteroidBeltData(
                    "Main Asteroid Belt",
                    beltInner,
                    beltOuter,
                    estimateAsteroidBeltMass(beltInner, beltOuter),
                    calculateBeltInclination(),
                    calculateBeltEccentricity()
            );
            log.info("Generated asteroid belt: {:.2f} - {:.2f} AU (giant at {:.2f} AU)",
                    beltInner, beltOuter, giantSma);
        }
    }

    /**
     * Generate a Kuiper belt beyond the outermost giant planet.
     */
    private void generateKuiperBelt(List<Planet> planets, Planet outermostGiant) {
        double giantSma = outermostGiant.getSma();

        // Scale by luminosity
        double luminosityFactor = sqrt(centralBody.luminosity);
        double minSma = MIN_GIANT_SMA_FOR_KUIPER * luminosityFactor;

        if (giantSma < minSma) {
            log.debug("Outermost giant at {} AU is too close for Kuiper belt (min: {} AU)",
                    String.format("%.2f", giantSma),
                    String.format("%.2f", minSma));
            return;
        }

        double kuiperInner = giantSma * KUIPER_BELT_INNER_RATIO;
        double kuiperOuter = giantSma * KUIPER_BELT_OUTER_RATIO;

        // Ensure no planet exists in this region
        boolean regionClear = true;
        for (Planet p : planets) {
            if (p.getSma() > kuiperInner && p.getSma() < kuiperOuter) {
                // Allow small bodies (potential dwarf planets like Pluto)
                if (p.massInEarthMasses() > 0.01) {  // > 0.01 Earth masses
                    regionClear = false;
                    break;
                }
            }
        }

        if (regionClear) {
            kuiperBelt = new KuiperBeltData(
                    "Kuiper Belt",
                    kuiperInner,
                    kuiperOuter,
                    estimateKuiperBeltMass(kuiperInner, kuiperOuter),
                    calculateKuiperInclination(),
                    calculateKuiperEccentricity()
            );
            log.info("Generated Kuiper belt: {:.2f} - {:.2f} AU (outer giant at {:.2f} AU)",
                    kuiperInner, kuiperOuter, giantSma);
        }
    }

    /**
     * Generate a ring system for a giant planet based on its properties.
     */
    private void generatePlanetaryRing(Planet planet) {
        double mass = planet.massInEarthMasses();
        double ringProbability;

        if (mass >= GAS_GIANT_MASS_THRESHOLD) {
            ringProbability = GAS_GIANT_RING_PROBABILITY;
        } else if (mass >= ICE_GIANT_MASS_THRESHOLD) {
            ringProbability = ICE_GIANT_RING_PROBABILITY;
        } else {
            ringProbability = TERRESTRIAL_RING_PROBABILITY;
        }

        if (random.nextDouble() >= ringProbability) {
            log.debug("Planet at {} AU ({}M⊕) did not develop rings (probability: {})",
                    String.format("%.2f", planet.getSma()),
                    String.format("%.1f", mass),
                    String.format("%.0f%%", ringProbability * 100));
            return;
        }

        // Determine ring type
        RingType ringType = determineRingType(planet);

        // Calculate ring dimensions based on planet radius
        double planetRadiusAU = planet.getRadius() / SystemObject.KM_PER_AU;
        double rocheLimit = calculateRocheLimit(planet, planetRadiusAU);

        // Ring extends from just above atmosphere to Roche limit
        double innerRadius = planetRadiusAU * ringType.innerRadiusFactor;
        double outerRadius = min(rocheLimit, planetRadiusAU * ringType.outerRadiusFactor);

        if (outerRadius <= innerRadius) {
            log.debug("Invalid ring dimensions for planet at {} AU, skipping",
                    String.format("%.2f", planet.getSma()));
            return;
        }

        RingData ring = new RingData(
                planet,
                ringType,
                innerRadius,
                outerRadius
        );

        planetaryRings.add(ring);

        // Store ring data on the planet for later conversion
        planet.setRingType(ringType.name());
        planet.setRingInnerRadiusAU(innerRadius);
        planet.setRingOuterRadiusAU(outerRadius);

        log.info("Generated {} ring for planet at {} AU: {:.6f} - {:.6f} AU (planet radius: {:.6f} AU)",
                ringType,
                String.format("%.2f", planet.getSma()),
                innerRadius, outerRadius, planetRadiusAU);
    }

    /**
     * Determine the type of ring system based on planet properties.
     */
    private RingType determineRingType(Planet planet) {
        double roll = random.nextDouble();
        double mass = planet.massInEarthMasses();

        // Ice giants tend toward Uranus/Neptune-like rings
        if (mass < GAS_GIANT_MASS_THRESHOLD) {
            if (roll < 0.5) return RingType.URANUS;
            if (roll < 0.85) return RingType.NEPTUNE;
            return RingType.SATURN;  // Rare bright rings for ice giants
        }

        // Gas giants can have any type
        if (roll < SATURN_LIKE_RING_PROBABILITY) {
            return RingType.SATURN;
        } else if (roll < SATURN_LIKE_RING_PROBABILITY + URANUS_LIKE_RING_PROBABILITY) {
            return RingType.URANUS;
        } else if (roll < SATURN_LIKE_RING_PROBABILITY + URANUS_LIKE_RING_PROBABILITY + NEPTUNE_LIKE_RING_PROBABILITY) {
            return RingType.NEPTUNE;
        } else {
            return RingType.JUPITER;
        }
    }

    /**
     * Calculate the Roche limit for a planet.
     * Roche limit = 2.44 × R_planet × (ρ_planet / ρ_ring_material)^(1/3)
     */
    private double calculateRocheLimit(Planet planet, double planetRadiusAU) {
        // Assume icy ring material density ~0.9 g/cm³
        double ringDensity = 0.9;
        double planetDensity = planet.getDensity();

        if (planetDensity <= 0) planetDensity = 1.3;  // Default gas giant density

        return 2.44 * planetRadiusAU * pow(planetDensity / ringDensity, 1.0 / 3.0);
    }

    /**
     * Estimate asteroid belt mass based on region size.
     * Real main belt is ~3×10^21 kg ≈ 0.0005 Earth masses
     */
    private double estimateAsteroidBeltMass(double innerAU, double outerAU) {
        double width = outerAU - innerAU;
        double baseMass = 0.0005;  // Earth masses
        return baseMass * (width / 1.2) * (0.5 + random.nextDouble());  // Scale by width, add randomness
    }

    /**
     * Estimate Kuiper belt mass.
     * Real Kuiper belt is estimated at ~0.1 Earth masses
     */
    private double estimateKuiperBeltMass(double innerAU, double outerAU) {
        double width = outerAU - innerAU;
        double baseMass = 0.1;  // Earth masses
        return baseMass * (width / 20.0) * (0.5 + random.nextDouble());
    }

    private double calculateBeltInclination() {
        // Main belt has average inclination ~10°
        return 8.0 + random.nextDouble() * 4.0;  // 8-12 degrees
    }

    private double calculateBeltEccentricity() {
        // Main belt has average eccentricity ~0.08
        return 0.05 + random.nextDouble() * 0.06;  // 0.05-0.11
    }

    private double calculateKuiperInclination() {
        // Kuiper belt has higher inclinations
        return 10.0 + random.nextDouble() * 10.0;  // 10-20 degrees
    }

    private double calculateKuiperEccentricity() {
        // Kuiper belt objects have higher eccentricities
        return 0.05 + random.nextDouble() * 0.1;  // 0.05-0.15
    }

    private void logGeneratedStructures() {
        int ringCount = planetaryRings.size();
        log.info("Post-accretion generation complete: {} planetary ring(s), asteroid belt: {}, Kuiper belt: {}",
                ringCount,
                asteroidBelt != null ? "yes" : "no",
                kuiperBelt != null ? "yes" : "no");
    }

    // ============== Inner classes for structure data ==============

    /**
     * Types of planetary ring systems.
     */
    public enum RingType {
        SATURN(1.2, 2.5),    // Bright, icy, prominent (like Saturn)
        URANUS(1.5, 2.0),    // Dark, narrow bands (like Uranus)
        NEPTUNE(1.3, 2.3),   // Faint, dusty (like Neptune)
        JUPITER(1.3, 1.8);   // Very faint, dust rings (like Jupiter)

        public final double innerRadiusFactor;  // Multiple of planet radius
        public final double outerRadiusFactor;

        RingType(double inner, double outer) {
            this.innerRadiusFactor = inner;
            this.outerRadiusFactor = outer;
        }
    }

    /**
     * Data for a planetary ring system.
     */
    @Data
    public static class RingData {
        private final Planet planet;
        private final RingType type;
        private final double innerRadiusAU;
        private final double outerRadiusAU;
    }

    /**
     * Data for an asteroid belt.
     */
    @Data
    public static class AsteroidBeltData {
        private final String name;
        private final double innerRadiusAU;
        private final double outerRadiusAU;
        private final double massEarthMasses;
        private final double inclinationDeg;
        private final double eccentricity;
    }

    /**
     * Data for a Kuiper belt.
     */
    @Data
    public static class KuiperBeltData {
        private final String name;
        private final double innerRadiusAU;
        private final double outerRadiusAU;
        private final double massEarthMasses;
        private final double inclinationDeg;
        private final double eccentricity;
    }
}
