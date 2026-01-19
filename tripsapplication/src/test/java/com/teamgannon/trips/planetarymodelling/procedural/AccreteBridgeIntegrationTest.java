package com.teamgannon.trips.planetarymodelling.procedural;

import com.teamgannon.trips.planetarymodelling.procedural.PlanetGenerator.GeneratedPlanet;
import com.teamgannon.trips.solarsysmodelling.accrete.Planet;
import com.teamgannon.trips.solarsysmodelling.accrete.SimStar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for the Accrete → Procedural Planet bridge.
 * Tests that planets generated from Accrete physical parameters
 * produce valid procedural terrain with appropriate characteristics.
 */
class AccreteBridgeIntegrationTest {

    private SimStar sunLikeStar;

    @BeforeEach
    void setUp() {
        // Create a Sun-like star for test planets
        sunLikeStar = new SimStar(
            1.0,    // mass (solar masses)
            1.0,    // luminosity (solar luminosities)
            1.0,    // radius (solar radii)
            5778,   // temperature (K)
            4.83    // absolute magnitude
        );
        sunLikeStar.setAge();  // Generate random age based on stellar lifetime
    }

    // ===========================================
    // generateFromAccrete() Tests
    // ===========================================

    @Nested
    @DisplayName("generateFromAccrete() with various planet types")
    class GenerateFromAccreteTests {

        @Test
        @DisplayName("Earth-like planet generates valid terrain")
        void earthLikePlanet() {
            Planet earth = createEarthLikePlanet();

            GeneratedPlanet generated = PlanetGenerator.generateFromAccrete(earth, 12345L);

            assertThat(generated).isNotNull();
            assertThat(generated.polygons()).isNotEmpty();
            assertThat(generated.heights()).hasSize(generated.polygons().size());
            assertThat(generated.climates()).hasSize(generated.polygons().size());
            assertThat(generated.plateAssignment()).isNotNull();
            assertThat(generated.boundaryAnalysis()).isNotNull();

            // Earth-like should have reasonable water and land distribution
            int landCount = 0, oceanCount = 0;
            for (int h : generated.heights()) {
                if (h >= 0) landCount++;
                else oceanCount++;
            }
            double landRatio = (double) landCount / generated.heights().length;
            // Earth is ~29% land, allow some variance
            assertThat(landRatio).isBetween(0.1, 0.6);
        }

        @Test
        @DisplayName("Mars-like planet (small, dry) generates appropriate terrain")
        void marsLikePlanet() {
            Planet mars = createMarsLikePlanet();

            GeneratedPlanet generated = PlanetGenerator.generateFromAccrete(mars, 54321L);

            assertThat(generated).isNotNull();
            assertThat(generated.polygons()).isNotEmpty();
            assertThat(generated.heights()).hasSize(generated.polygons().size());
            assertThat(generated.climates()).hasSize(generated.polygons().size());

            // Mars-like with 0% hydrosphere should have low water fraction config
            PlanetConfig config = PlanetGenerator.createBiasedConfig(mars, 54321L);
            assertThat(config.waterFraction()).isLessThan(0.1);
        }

        @Test
        @DisplayName("Ocean world generates mostly water")
        void oceanWorld() {
            Planet waterWorld = createOceanWorld();

            GeneratedPlanet generated = PlanetGenerator.generateFromAccrete(waterWorld, 99999L);

            assertThat(generated).isNotNull();

            // Ocean world should be >80% water
            int oceanCount = 0;
            for (int h : generated.heights()) {
                if (h < 0) oceanCount++;
            }
            double oceanRatio = (double) oceanCount / generated.heights().length;
            assertThat(oceanRatio).isGreaterThan(0.7);
        }

        @Test
        @DisplayName("Super-Earth generates valid terrain with plates")
        void superEarth() {
            Planet superEarth = createSuperEarth();

            GeneratedPlanet generated = PlanetGenerator.generateFromAccrete(superEarth, 11111L);

            assertThat(generated).isNotNull();
            assertThat(generated.plateAssignment()).isNotNull();

            // Verify plates are generated (count varies based on TectonicBias)
            // Note: Without mass setter, defaults to small planet behavior
            int plateCount = generated.plateAssignment().plates().size();
            assertThat(plateCount).isGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("Gas giant returns minimal terrain (no solid surface)")
        void gasGiant() {
            Planet jupiter = createGasGiant();

            GeneratedPlanet generated = PlanetGenerator.generateFromAccrete(jupiter, 77777L);

            // Gas giants still generate (for visualization) but with minimal terrain
            assertThat(generated).isNotNull();
            assertThat(generated.polygons()).isNotEmpty();
        }

        @Test
        @DisplayName("Small moon generates valid terrain")
        void smallMoon() {
            Planet moon = createSmallMoon();

            GeneratedPlanet generated = PlanetGenerator.generateFromAccrete(moon, 33333L);

            assertThat(generated).isNotNull();
            assertThat(generated.polygons()).isNotEmpty();

            // Small bodies may have stagnant lid tectonics (no active plates)
            // Just verify generation completes successfully
        }
    }

    // ===========================================
    // createBiasedConfig() Tests
    // ===========================================

    @Nested
    @DisplayName("createBiasedConfig() validation and parameter mapping")
    class CreateBiasedConfigTests {

        @Test
        @DisplayName("Config from Earth-like planet has reasonable defaults")
        void earthLikeConfig() {
            Planet earth = createEarthLikePlanet();

            PlanetConfig config = PlanetGenerator.createBiasedConfig(earth, 12345L);

            assertThat(config.seed()).isEqualTo(12345L);
            assertThat(config.waterFraction()).isBetween(0.5, 0.8);
            // Note: Without mass setter, plate count uses "small planet" defaults (3-7)
            assertThat(config.plateCount()).isGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("Config from dry planet has low water fraction")
        void dryPlanetConfig() {
            Planet mars = createMarsLikePlanet();

            PlanetConfig config = PlanetGenerator.createBiasedConfig(mars, 54321L);

            assertThat(config.waterFraction()).isLessThan(0.2);
        }

        @Test
        @DisplayName("Config from ocean world has high water fraction")
        void oceanWorldConfig() {
            Planet waterWorld = createOceanWorld();

            PlanetConfig config = PlanetGenerator.createBiasedConfig(waterWorld, 99999L);

            assertThat(config.waterFraction()).isGreaterThan(0.8);
        }

        @Test
        @DisplayName("Null planet throws IllegalArgumentException")
        void nullPlanetThrows() {
            assertThatThrownBy(() -> PlanetGenerator.createBiasedConfig(null, 12345L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null");
        }

        @Test
        @DisplayName("Planet with zero radius throws IllegalArgumentException")
        void zeroRadiusThrows() {
            Planet invalid = new Planet(sunLikeStar);
            invalid.setRadius(0);

            assertThatThrownBy(() -> PlanetGenerator.createBiasedConfig(invalid, 12345L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("radius");
        }

        @Test
        @DisplayName("Planet with negative radius throws IllegalArgumentException")
        void negativeRadiusThrows() {
            Planet invalid = new Planet(sunLikeStar);
            invalid.setRadius(-1000);

            assertThatThrownBy(() -> PlanetGenerator.createBiasedConfig(invalid, 12345L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("radius");
        }

        @Test
        @DisplayName("Hydrosphere > 100 is clamped to valid range")
        void hydrosphereClampedHigh() {
            Planet wet = new Planet(sunLikeStar);
            wet.setRadius(6371);
            wet.setHydrosphere(150);  // Invalid: > 100%
            wet.setSurfaceGravity(1.0);

            PlanetConfig config = PlanetGenerator.createBiasedConfig(wet, 12345L);

            assertThat(config.waterFraction()).isLessThanOrEqualTo(1.0);
        }

        @Test
        @DisplayName("Hydrosphere < 0 is clamped to valid range")
        void hydrosphereClampedLow() {
            Planet dry = new Planet(sunLikeStar);
            dry.setRadius(6371);
            dry.setHydrosphere(-50);  // Invalid: < 0%
            dry.setSurfaceGravity(1.0);

            PlanetConfig config = PlanetGenerator.createBiasedConfig(dry, 12345L);

            assertThat(config.waterFraction()).isGreaterThanOrEqualTo(0.0);
        }
    }

    // ===========================================
    // Reproducibility Tests
    // ===========================================

    @Nested
    @DisplayName("Reproducibility with same seed")
    class ReproducibilityTests {

        @Test
        @DisplayName("Same planet and seed produce identical results")
        void reproducibleGeneration() {
            Planet earth = createEarthLikePlanet();

            GeneratedPlanet gen1 = PlanetGenerator.generateFromAccrete(earth, 12345L);
            GeneratedPlanet gen2 = PlanetGenerator.generateFromAccrete(earth, 12345L);

            assertThat(gen1.polygons().size()).isEqualTo(gen2.polygons().size());
            assertThat(gen1.heights()).containsExactly(gen2.heights());
            assertThat(gen1.plateAssignment().plates().size())
                .isEqualTo(gen2.plateAssignment().plates().size());
        }

        @Test
        @DisplayName("Different seeds produce different results")
        void differentSeedsDifferentResults() {
            Planet earth = createEarthLikePlanet();

            GeneratedPlanet gen1 = PlanetGenerator.generateFromAccrete(earth, 12345L);
            GeneratedPlanet gen2 = PlanetGenerator.generateFromAccrete(earth, 54321L);

            // Heights should differ
            assertThat(gen1.heights()).isNotEqualTo(gen2.heights());
        }
    }

    // ===========================================
    // Helper Methods - Planet Factories
    // ===========================================

    private Planet createEarthLikePlanet() {
        Planet earth = new Planet(sunLikeStar);
        earth.setRadius(6371);           // Earth radius in km
        earth.setHydrosphere(66);        // ~66% water coverage
        earth.setDayLength(24);          // Hours
        earth.setSurfaceGravity(1.0);    // g
        earth.setSurfaceTemperature(288); // K (~15°C)
        earth.setGasGiant(false);
        return earth;
    }

    private Planet createMarsLikePlanet() {
        Planet mars = new Planet(sunLikeStar);
        mars.setRadius(3390);            // Mars radius in km
        mars.setHydrosphere(0);          // No surface water
        mars.setDayLength(24.6);         // Hours
        mars.setSurfaceGravity(0.38);    // g
        mars.setSurfaceTemperature(210); // K (~-63°C)
        mars.setGasGiant(false);
        return mars;
    }

    private Planet createOceanWorld() {
        Planet waterWorld = new Planet(sunLikeStar);
        waterWorld.setRadius(8000);       // Larger than Earth
        waterWorld.setHydrosphere(95);    // 95% water
        waterWorld.setDayLength(30);      // Hours
        waterWorld.setSurfaceGravity(1.2);
        waterWorld.setSurfaceTemperature(295);
        waterWorld.setGasGiant(false);
        return waterWorld;
    }

    private Planet createSuperEarth() {
        // Note: Without mass setter, TectonicBias will use default mass (0)
        // which results in "small planet" behavior. Test validates generation works.
        Planet superEarth = new Planet(sunLikeStar);
        superEarth.setRadius(10000);      // ~1.5x Earth radius
        superEarth.setHydrosphere(50);    // 50% water
        superEarth.setDayLength(18);      // Hours
        superEarth.setSurfaceGravity(2.0);
        superEarth.setSurfaceTemperature(300);
        superEarth.setGasGiant(false);
        return superEarth;
    }

    private Planet createGasGiant() {
        Planet jupiter = new Planet(sunLikeStar);
        jupiter.setRadius(69911);         // Jupiter radius in km
        jupiter.setHydrosphere(0);
        jupiter.setDayLength(10);         // Hours
        jupiter.setSurfaceGravity(2.4);
        jupiter.setSurfaceTemperature(165);
        jupiter.setGasGiant(true);
        return jupiter;
    }

    private Planet createSmallMoon() {
        Planet moon = new Planet(sunLikeStar);
        moon.setRadius(1737);             // Luna radius in km
        moon.setHydrosphere(0);
        moon.setDayLength(655);           // Hours (tidally locked)
        moon.setSurfaceGravity(0.166);
        moon.setSurfaceTemperature(250);
        moon.setGasGiant(false);
        return moon;
    }
}
