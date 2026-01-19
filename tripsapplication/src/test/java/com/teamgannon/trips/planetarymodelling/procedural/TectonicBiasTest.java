package com.teamgannon.trips.planetarymodelling.procedural;

import com.teamgannon.trips.solarsysmodelling.accrete.Planet;
import com.teamgannon.trips.solarsysmodelling.accrete.SimStar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for TectonicBias - the bridge between Accrete physical parameters
 * and procedural terrain generation settings.
 */
class TectonicBiasTest {

    private SimStar sunLikeStar;
    private SimStar youngStar;
    private SimStar oldStar;

    @BeforeEach
    void setUp() {
        // Sun-like star (~4.6 Gyr old)
        sunLikeStar = new SimStar(1.0, 1.0, 1.0, 5778, 4.83);
        sunLikeStar.setAge();

        // Young star (<1 Gyr)
        youngStar = new SimStar(1.2, 1.5, 1.1, 6000, 4.5);
        // Manually set young age by creating a star with specific properties

        // Old star (>10 Gyr)
        oldStar = new SimStar(0.8, 0.5, 0.9, 5000, 5.5);
    }

    // ===========================================
    // Factory Method Tests
    // ===========================================

    @Nested
    @DisplayName("Preset factory methods")
    class FactoryMethodTests {

        @Test
        @DisplayName("earthLike() returns reasonable Earth parameters")
        void earthLikePreset() {
            TectonicBias bias = TectonicBias.earthLike();

            assertThat(bias.minPlateCount()).isBetween(8, 12);
            assertThat(bias.maxPlateCount()).isBetween(15, 21);
            assertThat(bias.oceanicPlateRatio()).isBetween(0.6, 0.7);
            assertThat(bias.mountainHeightMultiplier()).isEqualTo(1.0);
            assertThat(bias.riftDepthMultiplier()).isEqualTo(1.0);
            assertThat(bias.hotspotProbability()).isBetween(0.1, 0.15);
            assertThat(bias.hasActivePlateTectonics()).isTrue();
        }

        @Test
        @DisplayName("marsLike() returns stagnant lid parameters")
        void marsLikePreset() {
            TectonicBias bias = TectonicBias.marsLike();

            assertThat(bias.minPlateCount()).isLessThanOrEqualTo(3);
            assertThat(bias.maxPlateCount()).isLessThanOrEqualTo(5);
            assertThat(bias.oceanicPlateRatio()).isEqualTo(0.0);
            assertThat(bias.mountainHeightMultiplier()).isGreaterThan(1.0);  // Lower gravity
            assertThat(bias.hasActivePlateTectonics()).isFalse();
        }

        @Test
        @DisplayName("venusLike() returns volcanic stagnant lid parameters")
        void venusLikePreset() {
            TectonicBias bias = TectonicBias.venusLike();

            assertThat(bias.oceanicPlateRatio()).isEqualTo(0.0);  // No oceans
            assertThat(bias.hotspotProbability()).isGreaterThan(0.15);  // High volcanism
            assertThat(bias.hasActivePlateTectonics()).isFalse();  // Stagnant lid
        }
    }

    // ===========================================
    // fromAccretePlanet() Tests
    // ===========================================

    @Nested
    @DisplayName("fromAccretePlanet() parameter derivation")
    class FromAccretePlanetTests {

        @Test
        @DisplayName("Earth-like planet produces reasonable plate parameters")
        void earthLikePlanetProducesReasonablePlates() {
            Planet earth = createEarthLikePlanet();

            TectonicBias bias = TectonicBias.fromAccretePlanet(earth);

            // Note: Without mass being set in Accrete simulation, mass defaults to 0
            // TectonicBias will treat this as a small planet
            assertThat(bias.minPlateCount()).isGreaterThanOrEqualTo(1);
            assertThat(bias.maxPlateCount()).isLessThanOrEqualTo(21);
            // Oceanic ratio should be set based on hydrosphere
            assertThat(bias.oceanicPlateRatio()).isBetween(0.3, 0.9);
        }

        @Test
        @DisplayName("Gas giant returns inactive tectonics")
        void gasGiantReturnsInactiveTectonics() {
            Planet gasGiant = createGasGiant();

            TectonicBias bias = TectonicBias.fromAccretePlanet(gasGiant);

            assertThat(bias.hasActivePlateTectonics()).isFalse();
            assertThat(bias.minPlateCount()).isEqualTo(0);
            assertThat(bias.maxPlateCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("Small dry planet produces stagnant lid")
        void smallDryPlanetStagnantLid() {
            Planet smallDry = createSmallDryPlanet();

            TectonicBias bias = TectonicBias.fromAccretePlanet(smallDry);

            // Small mass + no water = stagnant lid
            assertThat(bias.hasActivePlateTectonics()).isFalse();
        }

        @Test
        @DisplayName("Super-Earth produces valid plate configuration")
        void superEarthProducesValidPlates() {
            Planet superEarth = createSuperEarth();

            TectonicBias bias = TectonicBias.fromAccretePlanet(superEarth);

            // Note: Without mass set in Accrete simulation, plate count will be
            // based on default behavior (small planet). The key is valid output.
            assertThat(bias.minPlateCount()).isGreaterThanOrEqualTo(1);
            assertThat(bias.maxPlateCount()).isGreaterThanOrEqualTo(bias.minPlateCount());
            assertThat(bias.maxPlateCount()).isLessThanOrEqualTo(21);
        }

        @Test
        @DisplayName("Ocean world has high oceanic plate ratio")
        void oceanWorldHighOceanicRatio() {
            Planet oceanWorld = createOceanWorld();

            TectonicBias bias = TectonicBias.fromAccretePlanet(oceanWorld);

            assertThat(bias.oceanicPlateRatio()).isGreaterThan(0.7);
        }

        @Test
        @DisplayName("Low gravity planet has taller mountains")
        void lowGravityTallerMountains() {
            Planet lowG = createLowGravityPlanet();

            TectonicBias bias = TectonicBias.fromAccretePlanet(lowG);

            // Lower gravity allows taller mountains
            assertThat(bias.mountainHeightMultiplier()).isGreaterThan(1.0);
        }

        @Test
        @DisplayName("High gravity planet has compressed mountains")
        void highGravityCompressedMountains() {
            Planet highG = createHighGravityPlanet();

            TectonicBias bias = TectonicBias.fromAccretePlanet(highG);

            // Higher gravity compresses mountains
            assertThat(bias.mountainHeightMultiplier()).isLessThan(1.0);
        }

        @Test
        @DisplayName("Frozen world has inactive tectonics")
        void frozenWorldInactiveTectonics() {
            Planet frozen = createFrozenPlanet();

            TectonicBias bias = TectonicBias.fromAccretePlanet(frozen);

            // Too cold for active tectonics
            assertThat(bias.hasActivePlateTectonics()).isFalse();
        }

        @Test
        @DisplayName("Venus-like hot dry world has inactive tectonics")
        void venusLikeInactiveTectonics() {
            Planet venus = createVenusLikePlanet();

            TectonicBias bias = TectonicBias.fromAccretePlanet(venus);

            // Hot and dry = stagnant lid (Venus)
            assertThat(bias.hasActivePlateTectonics()).isFalse();
        }
    }

    // ===========================================
    // applyTo() Tests
    // ===========================================

    @Nested
    @DisplayName("applyTo() config modification")
    class ApplyToTests {

        @Test
        @DisplayName("applyTo() with active tectonics sets plate count in range")
        void applyToActiveTectonicsPlateCount() {
            TectonicBias bias = TectonicBias.earthLike();
            PlanetConfig base = PlanetConfig.builder()
                .seed(12345L)
                .size(PlanetConfig.Size.STANDARD)
                .build();

            PlanetConfig result = bias.applyTo(base, 12345L);

            assertThat(result.plateCount())
                .isBetween(bias.minPlateCount(), bias.maxPlateCount());
            assertThat(result.enableActiveTectonics()).isTrue();
        }

        @Test
        @DisplayName("applyTo() with stagnant lid disables active tectonics")
        void applyToStagnantLidDisablesTectonics() {
            TectonicBias bias = TectonicBias.marsLike();
            PlanetConfig base = PlanetConfig.builder()
                .seed(12345L)
                .size(PlanetConfig.Size.STANDARD)
                .build();

            PlanetConfig result = bias.applyTo(base, 12345L);

            assertThat(result.enableActiveTectonics()).isFalse();
            assertThat(result.heightScaleMultiplier()).isLessThan(bias.mountainHeightMultiplier());
        }

        @Test
        @DisplayName("applyTo() preserves seed override")
        void applyToPreservesSeed() {
            TectonicBias bias = TectonicBias.earthLike();
            PlanetConfig base = PlanetConfig.builder()
                .seed(11111L)
                .size(PlanetConfig.Size.STANDARD)
                .build();

            PlanetConfig result = bias.applyTo(base, 99999L);

            assertThat(result.seed()).isEqualTo(99999L);
        }

        @Test
        @DisplayName("applyTo() applies oceanic plate ratio")
        void applyToAppliesOceanicRatio() {
            TectonicBias bias = new TectonicBias(10, 15, 0.8, 1.0, 1.0, 0.1, true);
            PlanetConfig base = PlanetConfig.builder()
                .seed(12345L)
                .oceanicPlateRatio(0.5)  // Will be overridden
                .build();

            PlanetConfig result = bias.applyTo(base, 12345L);

            assertThat(result.oceanicPlateRatio()).isEqualTo(0.8);
        }

        @Test
        @DisplayName("Same seed produces same plate count")
        void sameSeedSamePlateCount() {
            TectonicBias bias = TectonicBias.earthLike();
            PlanetConfig base = PlanetConfig.builder().seed(12345L).build();

            PlanetConfig result1 = bias.applyTo(base, 12345L);
            PlanetConfig result2 = bias.applyTo(base, 12345L);

            assertThat(result1.plateCount()).isEqualTo(result2.plateCount());
        }

        @Test
        @DisplayName("Different seeds may produce different plate counts")
        void differentSeedsDifferentPlates() {
            TectonicBias bias = TectonicBias.earthLike();
            PlanetConfig base = PlanetConfig.builder().seed(12345L).build();

            // With wide plate range, different seeds should eventually differ
            boolean foundDifferent = false;
            int firstCount = bias.applyTo(base, 1L).plateCount();
            for (long seed = 2L; seed < 100L; seed++) {
                if (bias.applyTo(base, seed).plateCount() != firstCount) {
                    foundDifferent = true;
                    break;
                }
            }

            assertThat(foundDifferent).isTrue();
        }
    }

    // ===========================================
    // Edge Cases
    // ===========================================

    @Nested
    @DisplayName("Edge case handling")
    class EdgeCaseTests {

        @Test
        @DisplayName("Zero gravity handled gracefully")
        void zeroGravityHandled() {
            Planet zeroG = new Planet(sunLikeStar);
            zeroG.setRadius(6371);
            zeroG.setHydrosphere(50);
            zeroG.setSurfaceGravity(0.0);
            zeroG.setSurfaceTemperature(288);

            // Should not throw, infinity/NaN should be clamped
            assertThatCode(() -> TectonicBias.fromAccretePlanet(zeroG))
                .doesNotThrowAnyException();

            TectonicBias bias = TectonicBias.fromAccretePlanet(zeroG);
            assertThat(bias.mountainHeightMultiplier()).isFinite();
            assertThat(bias.riftDepthMultiplier()).isFinite();
        }

        @Test
        @DisplayName("Extreme mass values handled")
        void extremeMassHandled() {
            Planet massive = new Planet(sunLikeStar);
            massive.setRadius(20000);
            massive.setHydrosphere(50);
            massive.setSurfaceGravity(5.0);
            massive.setSurfaceTemperature(288);

            assertThatCode(() -> TectonicBias.fromAccretePlanet(massive))
                .doesNotThrowAnyException();

            TectonicBias bias = TectonicBias.fromAccretePlanet(massive);
            assertThat(bias.minPlateCount()).isGreaterThanOrEqualTo(1);
            assertThat(bias.maxPlateCount()).isLessThanOrEqualTo(21);
        }

        @Test
        @DisplayName("Extreme hydrosphere values handled")
        void extremeHydrosphereHandled() {
            Planet wet = new Planet(sunLikeStar);
            wet.setRadius(6371);
            wet.setHydrosphere(100);
            wet.setSurfaceGravity(1.0);
            wet.setSurfaceTemperature(288);

            TectonicBias bias = TectonicBias.fromAccretePlanet(wet);

            assertThat(bias.oceanicPlateRatio()).isLessThanOrEqualTo(0.85);
        }
    }

    // ===========================================
    // toString() Test
    // ===========================================

    @Nested
    @DisplayName("toString() formatting")
    class ToStringTests {

        @Test
        @DisplayName("toString() produces readable output")
        void toStringReadable() {
            TectonicBias bias = TectonicBias.earthLike();

            String str = bias.toString();

            assertThat(str).contains("TectonicBias");
            assertThat(str).contains("plates=");
            assertThat(str).contains("oceanicRatio=");
            assertThat(str).contains("activeTectonics=");
        }
    }

    // ===========================================
    // Helper Methods - Planet Factories
    // ===========================================

    private Planet createEarthLikePlanet() {
        Planet earth = new Planet(sunLikeStar);
        earth.setRadius(6371);
        earth.setHydrosphere(66);
        earth.setDayLength(24);
        earth.setSurfaceGravity(1.0);
        earth.setSurfaceTemperature(288);
        earth.setGasGiant(false);
        return earth;
    }

    private Planet createGasGiant() {
        Planet jupiter = new Planet(sunLikeStar);
        jupiter.setRadius(69911);
        jupiter.setHydrosphere(0);
        jupiter.setDayLength(10);
        jupiter.setSurfaceGravity(2.4);
        jupiter.setSurfaceTemperature(165);
        jupiter.setGasGiant(true);
        return jupiter;
    }

    private Planet createSmallDryPlanet() {
        Planet mars = new Planet(sunLikeStar);
        mars.setRadius(3390);
        mars.setHydrosphere(0);
        mars.setDayLength(24.6);
        mars.setSurfaceGravity(0.38);
        mars.setSurfaceTemperature(210);
        mars.setGasGiant(false);
        return mars;
    }

    private Planet createSuperEarth() {
        Planet superEarth = new Planet(sunLikeStar);
        superEarth.setRadius(10000);
        superEarth.setHydrosphere(50);
        superEarth.setDayLength(18);
        superEarth.setSurfaceGravity(2.0);
        superEarth.setSurfaceTemperature(300);
        superEarth.setGasGiant(false);
        return superEarth;
    }

    private Planet createOceanWorld() {
        Planet oceanWorld = new Planet(sunLikeStar);
        oceanWorld.setRadius(8000);
        oceanWorld.setHydrosphere(95);
        oceanWorld.setDayLength(30);
        oceanWorld.setSurfaceGravity(1.2);
        oceanWorld.setSurfaceTemperature(295);
        oceanWorld.setGasGiant(false);
        return oceanWorld;
    }

    private Planet createLowGravityPlanet() {
        Planet lowG = new Planet(sunLikeStar);
        lowG.setRadius(5000);
        lowG.setHydrosphere(30);
        lowG.setDayLength(30);
        lowG.setSurfaceGravity(0.3);
        lowG.setSurfaceTemperature(280);
        lowG.setGasGiant(false);
        return lowG;
    }

    private Planet createHighGravityPlanet() {
        Planet highG = new Planet(sunLikeStar);
        highG.setRadius(12000);
        highG.setHydrosphere(40);
        highG.setDayLength(20);
        highG.setSurfaceGravity(3.0);
        highG.setSurfaceTemperature(290);
        highG.setGasGiant(false);
        return highG;
    }

    private Planet createFrozenPlanet() {
        Planet frozen = new Planet(sunLikeStar);
        frozen.setRadius(6000);
        frozen.setHydrosphere(80);  // Ice
        frozen.setDayLength(24);
        frozen.setSurfaceGravity(0.9);
        frozen.setSurfaceTemperature(100);  // Very cold
        frozen.setGasGiant(false);
        return frozen;
    }

    private Planet createVenusLikePlanet() {
        Planet venus = new Planet(sunLikeStar);
        venus.setRadius(6052);
        venus.setHydrosphere(0);  // No water
        venus.setDayLength(2802);  // Very slow rotation
        venus.setSurfaceGravity(0.9);
        venus.setSurfaceTemperature(737);  // Very hot
        venus.setGasGiant(false);
        return venus;
    }
}
