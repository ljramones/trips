package com.teamgannon.trips.particlefields;

import javafx.scene.paint.Color;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Factory for creating ring field configurations and generators.
 * Provides preset configurations for common ring types and manages generator creation.
 */
public class RingFieldFactory {

    private static final Map<RingType, RingElementGenerator> generators = new EnumMap<>(RingType.class);

    static {
        generators.put(RingType.PLANETARY_RING, new PlanetaryRingGenerator());
        generators.put(RingType.ASTEROID_BELT, new AsteroidBeltGenerator());
        generators.put(RingType.DEBRIS_DISK, new DebrisDiskGenerator());
        generators.put(RingType.DUST_CLOUD, new DustCloudGenerator());
        generators.put(RingType.ACCRETION_DISK, new AccretionDiskGenerator());
    }

    /**
     * Gets the appropriate generator for the given ring type.
     */
    public static RingElementGenerator getGenerator(RingType type) {
        RingElementGenerator generator = generators.get(type);
        if (generator == null) {
            throw new IllegalArgumentException("No generator registered for type: " + type);
        }
        return generator;
    }

    /**
     * Generates ring elements using the appropriate generator for the configuration's type.
     */
    public static List<RingElement> generateElements(RingConfiguration config, Random random) {
        return getGenerator(config.type()).generate(config, random);
    }

    /**
     * Generates ring elements with a default random seed.
     */
    public static List<RingElement> generateElements(RingConfiguration config) {
        return generateElements(config, new Random(42));
    }

    // ========== PRESET CONFIGURATIONS ==========

    /**
     * Saturn-like planetary ring: thin, dense, icy particles.
     */
    public static RingConfiguration saturnRing() {
        return RingConfiguration.builder()
                .type(RingType.PLANETARY_RING)
                .innerRadius(15)
                .outerRadius(45)
                .numElements(10000)
                .minSize(0.2)
                .maxSize(0.8)
                .thickness(0.1)
                .maxInclinationDeg(0.5)
                .maxEccentricity(0.01)
                .baseAngularSpeed(0.004)
                .centralBodyRadius(10)
                .primaryColor(Color.rgb(230, 220, 200))  // Icy white-tan
                .secondaryColor(Color.rgb(180, 170, 160)) // Dusty gray
                .name("Saturn-like Ring")
                .build();
    }

    /**
     * Uranus-like planetary ring: thin, dark, narrow bands.
     */
    public static RingConfiguration uranusRing() {
        return RingConfiguration.builder()
                .type(RingType.PLANETARY_RING)
                .innerRadius(20)
                .outerRadius(35)
                .numElements(6000)
                .minSize(0.15)
                .maxSize(0.5)
                .thickness(0.05)
                .maxInclinationDeg(0.3)
                .maxEccentricity(0.008)
                .baseAngularSpeed(0.003)
                .centralBodyRadius(8)
                .primaryColor(Color.rgb(80, 80, 90))     // Dark gray
                .secondaryColor(Color.rgb(50, 50, 60))   // Darker
                .name("Uranus-like Ring")
                .build();
    }

    /**
     * Main asteroid belt: thick, sparse, rocky bodies.
     */
    public static RingConfiguration mainAsteroidBelt() {
        return RingConfiguration.builder()
                .type(RingType.ASTEROID_BELT)
                .innerRadius(95)
                .outerRadius(105)
                .numElements(5000)
                .minSize(0.9)
                .maxSize(2.6)
                .thickness(8.0)
                .maxInclinationDeg(12.0)
                .maxEccentricity(0.06)
                .baseAngularSpeed(0.002)
                .centralBodyRadius(8)
                .primaryColor(Color.rgb(140, 130, 120))  // Rocky gray
                .secondaryColor(Color.rgb(100, 90, 80))  // Brown-gray
                .name("Asteroid Belt")
                .build();
    }

    /**
     * Kuiper belt: very wide, sparse, icy bodies.
     */
    public static RingConfiguration kuiperBelt() {
        return RingConfiguration.builder()
                .type(RingType.ASTEROID_BELT)
                .innerRadius(150)
                .outerRadius(250)
                .numElements(3000)
                .minSize(1.0)
                .maxSize(3.5)
                .thickness(15.0)
                .maxInclinationDeg(20.0)
                .maxEccentricity(0.1)
                .baseAngularSpeed(0.0008)
                .centralBodyRadius(5)
                .primaryColor(Color.rgb(180, 190, 200))  // Icy blue-gray
                .secondaryColor(Color.rgb(140, 140, 150))
                .name("Kuiper Belt")
                .build();
    }

    /**
     * Protoplanetary debris disk: forming planetary system.
     */
    public static RingConfiguration protoplanetaryDisk() {
        return RingConfiguration.builder()
                .type(RingType.DEBRIS_DISK)
                .innerRadius(10)
                .outerRadius(80)
                .numElements(8000)
                .minSize(0.3)
                .maxSize(1.5)
                .thickness(3.0)
                .maxInclinationDeg(5.0)
                .maxEccentricity(0.04)
                .baseAngularSpeed(0.003)
                .centralBodyRadius(6)
                .primaryColor(Color.rgb(200, 180, 150))  // Dusty tan
                .secondaryColor(Color.rgb(180, 140, 100)) // Brown
                .name("Protoplanetary Disk")
                .build();
    }

    /**
     * Collision debris disk: aftermath of planetary collision.
     */
    public static RingConfiguration collisionDebris() {
        return RingConfiguration.builder()
                .type(RingType.DEBRIS_DISK)
                .innerRadius(20)
                .outerRadius(50)
                .numElements(6000)
                .minSize(0.2)
                .maxSize(2.0)
                .thickness(5.0)
                .maxInclinationDeg(8.0)
                .maxEccentricity(0.08)
                .baseAngularSpeed(0.0025)
                .centralBodyRadius(7)
                .primaryColor(Color.rgb(160, 150, 140))  // Rocky gray
                .secondaryColor(Color.rgb(120, 100, 90)) // Darker debris
                .name("Collision Debris")
                .build();
    }

    /**
     * Emission nebula: colorful, glowing gas cloud.
     * Features dense core with gradual falloff and moderate filamentary structure.
     */
    public static RingConfiguration emissionNebula() {
        return RingConfiguration.builder()
                .type(RingType.DUST_CLOUD)
                .innerRadius(5)
                .outerRadius(100)
                .numElements(8000)
                .minSize(0.5)
                .maxSize(2.0)
                .thickness(60.0)
                .maxInclinationDeg(90.0)  // Full 3D distribution
                .maxEccentricity(0.02)
                .baseAngularSpeed(0.0003)
                .centralBodyRadius(4)
                .primaryColor(Color.rgb(255, 100, 150))  // Pink-red (hydrogen alpha)
                .secondaryColor(Color.rgb(100, 200, 255)) // Blue-cyan (oxygen)
                .name("Emission Nebula")
                // Nebula-specific parameters
                .radialPower(0.4)         // Dense core concentration
                .noiseStrength(0.4)       // Moderate filaments
                .noiseOctaves(3)          // Good detail level
                .seed(42L)
                .build();
    }

    /**
     * Dark nebula: obscuring dust cloud.
     * Features fairly uniform distribution with some turbulent structure.
     */
    public static RingConfiguration darkNebula() {
        return RingConfiguration.builder()
                .type(RingType.DUST_CLOUD)
                .innerRadius(10)
                .outerRadius(80)
                .numElements(5000)
                .minSize(0.8)
                .maxSize(2.5)
                .thickness(50.0)
                .maxInclinationDeg(90.0)
                .maxEccentricity(0.02)
                .baseAngularSpeed(0.0002)
                .centralBodyRadius(3)
                .primaryColor(Color.rgb(40, 35, 30))     // Very dark brown
                .secondaryColor(Color.rgb(20, 18, 15))   // Nearly black
                .name("Dark Nebula")
                // Nebula-specific parameters
                .radialPower(0.5)         // Fairly uniform
                .noiseStrength(0.3)       // Some structure
                .noiseOctaves(3)
                .seed(43L)
                .build();
    }

    /**
     * Planetary nebula: expanding shell from dying star.
     * Features shell-like distribution with hollow center.
     */
    public static RingConfiguration planetaryNebula() {
        return RingConfiguration.builder()
                .type(RingType.DUST_CLOUD)
                .innerRadius(15)          // Hollow center (white dwarf remnant)
                .outerRadius(50)
                .numElements(8000)
                .minSize(0.3)
                .maxSize(1.2)
                .thickness(40.0)
                .maxInclinationDeg(90.0)
                .maxEccentricity(0.01)
                .baseAngularSpeed(0.0003)  // Slow expansion
                .centralBodyRadius(2)
                .primaryColor(Color.rgb(100, 255, 200))  // Ionized oxygen green
                .secondaryColor(Color.rgb(200, 100, 255)) // Ionized nitrogen purple
                .name("Planetary Nebula")
                // Nebula-specific parameters
                .radialPower(0.7)         // Shell-like (hollow center)
                .noiseStrength(0.25)      // Some asymmetry
                .noiseOctaves(3)
                .seed(44L)
                .build();
    }

    /**
     * Supernova remnant: explosive debris with complex filaments.
     * Features expanding shell with highly turbulent structure.
     */
    public static RingConfiguration supernovaRemnant() {
        return RingConfiguration.builder()
                .type(RingType.DUST_CLOUD)
                .innerRadius(20)
                .outerRadius(80)
                .numElements(10000)
                .minSize(0.4)
                .maxSize(1.8)
                .thickness(60.0)
                .maxInclinationDeg(90.0)
                .maxEccentricity(0.03)
                .baseAngularSpeed(0.0004)  // Rapid expansion
                .centralBodyRadius(1)      // Neutron star/pulsar
                .primaryColor(Color.rgb(255, 150, 100))  // Orange-red
                .secondaryColor(Color.rgb(255, 220, 100)) // Yellow
                .name("Supernova Remnant")
                // Nebula-specific parameters
                .radialPower(0.65)        // Expanding shell
                .noiseStrength(0.6)       // Complex filaments (shock waves)
                .noiseOctaves(4)          // High detail
                .seed(45L)
                .build();
    }

    /**
     * Reflection nebula: dust reflecting starlight (blue-shifted).
     */
    public static RingConfiguration reflectionNebula() {
        return RingConfiguration.builder()
                .type(RingType.DUST_CLOUD)
                .innerRadius(5)
                .outerRadius(60)
                .numElements(6000)
                .minSize(0.3)
                .maxSize(1.5)
                .thickness(40.0)
                .maxInclinationDeg(90.0)
                .maxEccentricity(0.02)
                .baseAngularSpeed(0.00015)
                .centralBodyRadius(3)
                .primaryColor(Color.rgb(100, 150, 255))   // Blue (scattered light)
                .secondaryColor(Color.rgb(150, 180, 255)) // Lighter blue
                .name("Reflection Nebula")
                // Nebula-specific parameters
                .radialPower(0.35)        // Concentrated near illuminating star
                .noiseStrength(0.2)       // Smoother appearance
                .noiseOctaves(2)
                .seed(46L)
                .build();
    }

    /**
     * Black hole accretion disk: thin, hot, fast-rotating.
     */
    public static RingConfiguration blackHoleAccretion() {
        return RingConfiguration.builder()
                .type(RingType.ACCRETION_DISK)
                .innerRadius(8)
                .outerRadius(50)
                .numElements(10000)
                .minSize(0.2)
                .maxSize(0.6)
                .thickness(0.5)
                .maxInclinationDeg(1.0)
                .maxEccentricity(0.01)
                .baseAngularSpeed(0.008)
                .centralBodyRadius(5)
                .primaryColor(Color.rgb(200, 220, 255))  // Hot blue-white (inner)
                .secondaryColor(Color.rgb(255, 150, 50)) // Cooler orange (outer)
                .name("Black Hole Accretion Disk")
                .build();
    }

    /**
     * Neutron star accretion disk: very compact, extremely hot.
     */
    public static RingConfiguration neutronStarAccretion() {
        return RingConfiguration.builder()
                .type(RingType.ACCRETION_DISK)
                .innerRadius(3)
                .outerRadius(25)
                .numElements(8000)
                .minSize(0.15)
                .maxSize(0.4)
                .thickness(0.3)
                .maxInclinationDeg(0.5)
                .maxEccentricity(0.005)
                .baseAngularSpeed(0.012)
                .centralBodyRadius(2)
                .primaryColor(Color.rgb(220, 240, 255))  // Very hot white-blue
                .secondaryColor(Color.rgb(255, 200, 100)) // Yellow-orange
                .name("Neutron Star Accretion")
                .build();
    }

    /**
     * Returns all available preset names.
     */
    public static String[] getPresetNames() {
        return new String[]{
                "Saturn Ring",
                "Uranus Ring",
                "Main Asteroid Belt",
                "Kuiper Belt",
                "Protoplanetary Disk",
                "Collision Debris",
                "Emission Nebula",
                "Dark Nebula",
                "Planetary Nebula",
                "Supernova Remnant",
                "Reflection Nebula",
                "Black Hole Accretion",
                "Neutron Star Accretion"
        };
    }

    /**
     * Returns preset names for nebulae only.
     */
    public static String[] getNebulaPresetNames() {
        return new String[]{
                "Emission Nebula",
                "Dark Nebula",
                "Planetary Nebula",
                "Supernova Remnant",
                "Reflection Nebula"
        };
    }

    /**
     * Gets a preset configuration by name.
     */
    public static RingConfiguration getPreset(String name) {
        return switch (name) {
            case "Saturn Ring" -> saturnRing();
            case "Uranus Ring" -> uranusRing();
            case "Main Asteroid Belt" -> mainAsteroidBelt();
            case "Kuiper Belt" -> kuiperBelt();
            case "Protoplanetary Disk" -> protoplanetaryDisk();
            case "Collision Debris" -> collisionDebris();
            case "Emission Nebula" -> emissionNebula();
            case "Dark Nebula" -> darkNebula();
            case "Planetary Nebula" -> planetaryNebula();
            case "Supernova Remnant" -> supernovaRemnant();
            case "Reflection Nebula" -> reflectionNebula();
            case "Black Hole Accretion" -> blackHoleAccretion();
            case "Neutron Star Accretion" -> neutronStarAccretion();
            default -> throw new IllegalArgumentException("Unknown preset: " + name);
        };
    }
}
