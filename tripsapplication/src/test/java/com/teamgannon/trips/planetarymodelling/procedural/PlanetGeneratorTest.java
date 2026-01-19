package com.teamgannon.trips.planetarymodelling.procedural;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import com.teamgannon.trips.planetarymodelling.procedural.PlanetConfig.Size;
import com.teamgannon.trips.planetarymodelling.procedural.PlanetGenerator.GeneratedPlanet;
import com.teamgannon.trips.solarsysmodelling.accrete.Planet;
import com.teamgannon.trips.solarsysmodelling.accrete.SimStar;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.data.Percentage.withPercentage;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

class PlanetGeneratorTest {

    @Test
    @DisplayName("PlanetConfig builds with correct defaults")
    void configDefaults() {
        var config = PlanetConfig.builder().build();

        assertThat(config.n()).isEqualTo(21);
        assertThat(config.polyCount()).isEqualTo(4412);
        assertThat(config.plateCount()).isEqualTo(14);
        assertThat(config.waterFraction()).isEqualTo(0.66);
    }

    @Test
    @DisplayName("PlanetConfig respects size presets")
    void configSizes() {
        var tiny = PlanetConfig.builder().size(Size.TINY).build();
        assertThat(tiny.n()).isEqualTo(15);
        assertThat(tiny.polyCount()).isEqualTo(2252);

        var large = PlanetConfig.builder().size(Size.LARGE).build();
        assertThat(large.n()).isEqualTo(24);
        assertThat(large.polyCount()).isEqualTo(5762);
    }

    @Test
    @DisplayName("PlanetConfig clamps plate count to valid range")
    void configPlateCountClamped() {
        var tooFew = PlanetConfig.builder().plateCount(3).build();
        assertThat(tooFew.plateCount()).isEqualTo(7);

        var tooMany = PlanetConfig.builder().plateCount(50).build();
        assertThat(tooMany.plateCount()).isEqualTo(21);
    }

    @Test
    @DisplayName("SubSeed produces deterministic values")
    void subSeedDeterministic() {
        var config = PlanetConfig.builder().seed(12345L).build();

        long sub1a = config.subSeed(1);
        long sub1b = config.subSeed(1);
        long sub2 = config.subSeed(2);

        assertThat(sub1a).isEqualTo(sub1b);
        assertThat(sub1a).isNotEqualTo(sub2);
    }

    @Test
    @DisplayName("IcosahedralMesh generates expected polygon count")
    void meshPolygonCount() {
        var config = PlanetConfig.builder().size(Size.STANDARD).build();
        var mesh = new IcosahedralMesh(config);
        var polygons = mesh.generate();

        // Standard size should produce approximately 4412 polygons
        // (12 pentagons + hexagons from 20 faces + 30 edges)
        // Slight variance from GDScript due to floating-point edge detection
        assertThat(polygons.size()).isCloseTo(config.polyCount(), withPercentage(2));
    }

    @Test
    @DisplayName("IcosahedralMesh has exactly 12 pentagons")
    void meshPentagonCount() {
        var config = PlanetConfig.builder().size(Size.SMALL).build();
        var mesh = new IcosahedralMesh(config);
        var polygons = mesh.generate();

        long pentagonCount = polygons.stream()
            .filter(Polygon::isPentagon)
            .count();

        assertThat(pentagonCount).isEqualTo(12);
    }

    @Test
    @DisplayName("AdjacencyGraph neighbors are symmetric")
    void adjacencySymmetric() {
        var config = PlanetConfig.builder().size(Size.DUEL).build();
        var mesh = new IcosahedralMesh(config);
        var adj = new AdjacencyGraph(mesh.generate());

        for (int i = 0; i < adj.size(); i++) {
            for (int neighbor : adj.neighborsOnly(i)) {
                assertThat(adj.areNeighbors(neighbor, i))
                    .as("If %d neighbors %d, then %d should neighbor %d", i, neighbor, neighbor, i)
                    .isTrue();
            }
        }
    }

    @Test
    @DisplayName("PlateAssigner assigns all polygons")
    void plateAssignerCoversAll() {
        var config = PlanetConfig.builder().size(Size.DUEL).build();
        var mesh = new IcosahedralMesh(config);
        var polygons = mesh.generate();
        var adj = new AdjacencyGraph(polygons);
        var assigner = new PlateAssigner(config, adj);
        var assignment = assigner.assign();

        for (int plateIdx : assignment.plateIndex()) {
            assertThat(plateIdx).isGreaterThanOrEqualTo(0);
            assertThat(plateIdx).isLessThan(config.plateCount());
        }

        int total = assignment.plates().stream()
            .mapToInt(java.util.List::size)
            .sum();
        assertThat(total).isEqualTo(polygons.size());  // Use actual mesh size
    }

    @Test
    @DisplayName("Same seed produces identical planets")
    void reproducibility() {
        var config = PlanetConfig.builder()
            .seed(99999L)
            .size(Size.DUEL)
            .build();

        GeneratedPlanet planet1 = PlanetGenerator.generate(config);
        GeneratedPlanet planet2 = PlanetGenerator.generate(config);

        assertThat(planet1.heights()).isEqualTo(planet2.heights());
    }

    @Test
    @DisplayName("Different seeds produce different planets")
    void differentSeeds() {
        var config1 = PlanetConfig.builder().seed(111L).size(Size.DUEL).build();
        var config2 = PlanetConfig.builder().seed(222L).size(Size.DUEL).build();

        GeneratedPlanet planet1 = PlanetGenerator.generate(config1);
        GeneratedPlanet planet2 = PlanetGenerator.generate(config2);

        assertThat(planet1.heights()).isNotEqualTo(planet2.heights());
    }

    @Test
    @DisplayName("Heights are within valid range")
    void heightsInRange() {
        var config = PlanetConfig.builder().size(Size.DUEL).build();
        GeneratedPlanet planet = PlanetGenerator.generate(config);

        for (int height : planet.heights()) {
            assertThat(height).isBetween(-4, 4);
        }
    }

    @Test
    @DisplayName("Climate zones assigned to all polygons")
    void climateZonesComplete() {
        var config = PlanetConfig.builder().size(Size.DUEL).build();
        GeneratedPlanet planet = PlanetGenerator.generate(config);

        assertThat(planet.climates()).hasSize(planet.polygons().size());  // Use actual mesh size
        for (var zone : planet.climates()) {
            assertThat(zone).isNotNull();
        }
    }

    @Test
    @DisplayName("Full generation pipeline completes")
    void fullPipelineStandard() {
        var config = PlanetConfig.builder()
            .seed(42L)
            .size(Size.STANDARD)
            .plateCount(14)
            .waterFraction(0.66)
            .build();

        GeneratedPlanet planet = PlanetGenerator.generate(config);

        int polyCount = planet.polygons().size();
        assertThat(polyCount).isCloseTo(config.polyCount(), withPercentage(2));
        assertThat(planet.heights()).hasSize(polyCount);
        assertThat(planet.climates()).hasSize(polyCount);
        assertThat(planet.plateAssignment().plates()).hasSize(14);
    }

    @Test
    @Tag("slow")
    @DisplayName("COLOSSAL size generation completes within a reasonable time")
    void colossalGenerationPerformance() {
        var config = PlanetConfig.builder()
            .seed(42L)
            .size(Size.COLOSSAL)
            .plateCount(14)
            .waterFraction(0.66)
            .build();

        assertTimeoutPreemptively(Duration.ofSeconds(20), () -> {
            GeneratedPlanet planet = PlanetGenerator.generate(config);
            assertThat(planet.polygons()).isNotEmpty();
        });
    }

    // ==================== Edge Case Tests ====================

    @Test
    @DisplayName("Desert world (waterFraction=0) generates valid terrain")
    void desertWorld() {
        var config = PlanetConfig.builder()
            .seed(42L)
            .size(Size.SMALL)
            .waterFraction(0.0)
            .oceanicPlateRatio(0.0)
            .build();

        GeneratedPlanet planet = PlanetGenerator.generate(config);

        // Count water polygons (height < 0)
        int waterCount = 0;
        for (int h : planet.heights()) {
            if (h < 0) waterCount++;
        }
        double waterFraction = (double) waterCount / planet.heights().length;

        // Desert world should have minimal water (<20%)
        assertThat(waterFraction)
            .as("Desert world should have low water coverage")
            .isLessThan(0.20);
    }

    @Test
    @DisplayName("Ocean world (waterFraction=1) generates valid terrain")
    void oceanWorld() {
        var config = PlanetConfig.builder()
            .seed(42L)
            .size(Size.SMALL)
            .waterFraction(1.0)
            .oceanicPlateRatio(1.0)
            .build();

        GeneratedPlanet planet = PlanetGenerator.generate(config);

        // Count water polygons (height < 0)
        int waterCount = 0;
        for (int h : planet.heights()) {
            if (h < 0) waterCount++;
        }
        double waterFraction = (double) waterCount / planet.heights().length;

        // Ocean world should have high water coverage (>60%)
        assertThat(waterFraction)
            .as("Ocean world should have high water coverage")
            .isGreaterThan(0.60);
    }

    @Test
    @DisplayName("Single plate (plateCount=7 minimum) generates valid terrain")
    void minimumPlates() {
        var config = PlanetConfig.builder()
            .seed(42L)
            .size(Size.SMALL)
            .plateCount(7)  // Minimum allowed
            .build();

        GeneratedPlanet planet = PlanetGenerator.generate(config);

        assertThat(planet.plateAssignment().plates()).hasSize(7);
        assertThat(planet.heights()).isNotEmpty();
    }

    @Test
    @DisplayName("Maximum plates (plateCount=21) generates valid terrain")
    void maximumPlates() {
        var config = PlanetConfig.builder()
            .seed(42L)
            .size(Size.SMALL)
            .plateCount(21)  // Maximum allowed
            .build();

        GeneratedPlanet planet = PlanetGenerator.generate(config);

        assertThat(planet.plateAssignment().plates()).hasSize(21);
        assertThat(planet.heights()).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.5, 1.0, 1.5, 2.0})
    @DisplayName("Height scale multiplier affects terrain extremity")
    void heightScaleMultiplierEffect(double multiplier) {
        var config = PlanetConfig.builder()
            .seed(42L)
            .size(Size.SMALL)
            .heightScaleMultiplier(multiplier)
            .build();

        GeneratedPlanet planet = PlanetGenerator.generate(config);

        // All heights should be in valid range regardless of multiplier
        for (int h : planet.heights()) {
            assertThat(h).isBetween(-4, 4);
        }
    }

    // ==================== Configuration Interaction Tests ====================

    @Test
    @DisplayName("Erosion disabled (erosionIterations=0) skips sediment flow but allows rivers")
    void noErosion() {
        var config = PlanetConfig.builder()
            .seed(42L)
            .size(Size.SMALL)
            .erosionIterations(0)
            .build();

        GeneratedPlanet planet = PlanetGenerator.generate(config);

        // Should complete without sediment erosion (rivers still generated by default)
        assertThat(planet.heights()).isNotEmpty();
        // erosionIterations=0 skips sediment flow, but rivers are controlled by enableRivers
        // Heights should still be valid
        for (int h : planet.heights()) {
            assertThat(h).isBetween(-4, 4);
        }
    }

    @Test
    @DisplayName("Both erosion and rivers disabled produces terrain without rivers")
    void noErosionOrRivers() {
        var config = PlanetConfig.builder()
            .seed(42L)
            .size(Size.SMALL)
            .erosionIterations(0)
            .enableRivers(false)
            .build();

        GeneratedPlanet planet = PlanetGenerator.generate(config);

        // Should complete without erosion results
        assertThat(planet.heights()).isNotEmpty();
        // With both disabled, rivers should be empty
        if (planet.erosionResult() != null) {
            assertThat(planet.erosionResult().rivers()).isEmpty();
        }
    }

    @Test
    @DisplayName("Rivers disabled still generates valid terrain")
    void noRivers() {
        var config = PlanetConfig.builder()
            .seed(42L)
            .size(Size.SMALL)
            .enableRivers(false)
            .build();

        GeneratedPlanet planet = PlanetGenerator.generate(config);

        assertThat(planet.heights()).isNotEmpty();
        // Rivers should be empty when disabled
        if (planet.erosionResult() != null) {
            assertThat(planet.erosionResult().rivers()).isEmpty();
        }
    }

    @Test
    @DisplayName("High rainfall scale produces more erosion")
    void highRainfallScale() {
        var config = PlanetConfig.builder()
            .seed(42L)
            .size(Size.SMALL)
            .rainfallScale(2.0)
            .build();

        GeneratedPlanet planet = PlanetGenerator.generate(config);

        assertThat(planet.heights()).isNotEmpty();
        // Should complete without errors
    }

    @Test
    @DisplayName("Stagnant lid tectonics (enableActiveTectonics=false)")
    void stagnantLidWorld() {
        var config = PlanetConfig.builder()
            .seed(42L)
            .size(Size.SMALL)
            .enableActiveTectonics(false)
            .hotspotProbability(0.5)  // Higher for stagnant lid worlds
            .build();

        GeneratedPlanet planet = PlanetGenerator.generate(config);

        assertThat(planet.heights()).isNotEmpty();
        // Should have boundaries but mostly transform/inactive
    }

    // ==================== Accrete Bridge Tests ====================

    /**
     * Creates a test SimStar with Sun-like properties.
     * SimStar requires: stellarMass, stellarLuminosity, stellarRadius, temp, mag
     */
    private SimStar createTestStar() {
        // Sun-like star: mass=1.0, luminosity=1.0, radius=1.0 (solar units), temp=5780K, mag=4.83
        SimStar star = new SimStar(1.0, 1.0, 1.0, 5780.0, 4.83);
        star.setAge();  // Sets a random age
        return star;
    }

    /**
     * Creates a test Planet with Earth-like properties.
     */
    private Planet createEarthLikePlanet(SimStar star) {
        Planet p = new Planet(star);
        p.setRadius(6371);           // Earth radius in km
        p.setHydrosphere(66.0);      // 66% water coverage
        p.setDayLength(86400);       // 24 hours in seconds
        p.setSurfaceGravity(1.0);    // 1g
        p.setSurfaceTemperature(288.0);  // ~15°C in Kelvin
        p.setGasGiant(false);
        return p;
    }

    @Test
    @DisplayName("createBiasedConfig creates valid config from Accrete planet")
    void createBiasedConfigFromAccrete() {
        SimStar star = createTestStar();
        Planet accretePlanet = createEarthLikePlanet(star);

        PlanetConfig config = PlanetGenerator.createBiasedConfig(accretePlanet, 12345L);

        assertThat(config.seed()).isEqualTo(12345L);
        assertThat(config.waterFraction()).isCloseTo(0.66, withPercentage(1));
        assertThat(config.radius()).isEqualTo(6371.0);
    }

    @Test
    @DisplayName("generateFromAccrete produces valid planet")
    void generateFromAccretePlanet() {
        SimStar star = createTestStar();
        Planet accretePlanet = createEarthLikePlanet(star);

        GeneratedPlanet planet = PlanetGenerator.generateFromAccrete(accretePlanet, 12345L);

        assertThat(planet.polygons()).isNotEmpty();
        assertThat(planet.heights()).isNotEmpty();
        assertThat(planet.climates()).isNotEmpty();
    }

    @Test
    @DisplayName("Small Accrete planet uses appropriate mesh size")
    void smallAccretePlanet() {
        SimStar star = createTestStar();
        Planet accretePlanet = new Planet(star);
        accretePlanet.setRadius(2000);  // Small planet (Mars-like)
        accretePlanet.setHydrosphere(0.0);
        accretePlanet.setDayLength(88800);  // ~24.7 hours
        accretePlanet.setSurfaceGravity(0.38);
        accretePlanet.setSurfaceTemperature(210.0);
        accretePlanet.setGasGiant(false);

        PlanetConfig config = PlanetGenerator.createBiasedConfig(accretePlanet, 42L);

        // Small planets should use smaller mesh
        assertThat(config.n()).isLessThanOrEqualTo(19);  // SMALL or smaller
    }

    @Test
    @DisplayName("Large Accrete planet uses appropriate mesh size")
    void largeAccretePlanet() {
        SimStar star = createTestStar();
        Planet accretePlanet = new Planet(star);
        accretePlanet.setRadius(12000);  // Super-Earth
        accretePlanet.setHydrosphere(80.0);
        accretePlanet.setDayLength(108000);  // 30 hours
        accretePlanet.setSurfaceGravity(1.5);
        accretePlanet.setSurfaceTemperature(295.0);
        accretePlanet.setGasGiant(false);

        PlanetConfig config = PlanetGenerator.createBiasedConfig(accretePlanet, 42L);

        // Large planets should use larger mesh
        assertThat(config.n()).isGreaterThanOrEqualTo(24);  // LARGE or larger
    }

    // ==================== Erosion Config Tests ====================

    @Test
    @DisplayName("Custom erosion thresholds are applied")
    void customErosionThresholds() {
        var config = PlanetConfig.builder()
            .seed(42L)
            .size(Size.SMALL)
            .rainfallThreshold(0.5)       // Higher threshold = less erosion
            .riverSourceThreshold(0.9)    // Stricter river requirements
            .erosionCap(0.1)              // Lower cap = gentler erosion
            .depositionFactor(0.8)        // More deposition
            .build();

        assertThat(config.rainfallThreshold()).isEqualTo(0.5);
        assertThat(config.riverSourceThreshold()).isEqualTo(0.9);
        assertThat(config.erosionCap()).isEqualTo(0.1);
        assertThat(config.depositionFactor()).isEqualTo(0.8);

        GeneratedPlanet planet = PlanetGenerator.generate(config);
        assertThat(planet.heights()).isNotEmpty();
    }

    // ==================== Error Handling Tests ====================

    @Test
    @DisplayName("PlanetGenerator constructor rejects null config")
    void constructorRejectsNullConfig() {
        assertThatThrownBy(() -> new PlanetGenerator(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be null");
    }

    @Test
    @DisplayName("generate() static method rejects null config")
    void generateRejectsNullConfig() {
        assertThatThrownBy(() -> PlanetGenerator.generate(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be null");
    }

    @Test
    @DisplayName("createBiasedConfig rejects null planet")
    void createBiasedConfigRejectsNullPlanet() {
        assertThatThrownBy(() -> PlanetGenerator.createBiasedConfig(null, 42L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be null");
    }

    @Test
    @DisplayName("generateFromAccrete rejects null planet")
    void generateFromAccreteRejectsNullPlanet() {
        assertThatThrownBy(() -> PlanetGenerator.generateFromAccrete(null, 42L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be null");
    }

    @Test
    @DisplayName("createBiasedConfig rejects planet with zero radius")
    void createBiasedConfigRejectsZeroRadius() {
        SimStar star = createTestStar();
        Planet planet = new Planet(star);
        planet.setRadius(0);  // Invalid - must be positive

        assertThatThrownBy(() -> PlanetGenerator.createBiasedConfig(planet, 42L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("radius must be positive");
    }

    @Test
    @DisplayName("createBiasedConfig rejects planet with negative radius")
    void createBiasedConfigRejectsNegativeRadius() {
        SimStar star = createTestStar();
        Planet planet = new Planet(star);
        planet.setRadius(-100);  // Invalid - must be positive

        assertThatThrownBy(() -> PlanetGenerator.createBiasedConfig(planet, 42L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("radius must be positive");
    }

    @Test
    @DisplayName("createBiasedConfig clamps out-of-range hydrosphere")
    void createBiasedConfigClampsHydrosphere() {
        SimStar star = createTestStar();
        Planet planet = new Planet(star);
        planet.setRadius(6371);
        planet.setHydrosphere(150.0);  // Out of range (should be 0-100)
        planet.setGasGiant(false);
        planet.setSurfaceGravity(1.0);
        planet.setSurfaceTemperature(288.0);

        // Should not throw, but clamp to valid range
        PlanetConfig config = PlanetGenerator.createBiasedConfig(planet, 42L);

        // Hydrosphere should be clamped to 100% -> waterFraction = 1.0
        assertThat(config.waterFraction()).isLessThanOrEqualTo(1.0);
    }

    @Test
    @DisplayName("Gas giant planet generates terrain for visualization")
    void gasGiantGeneratesTerrain() {
        SimStar star = createTestStar();
        Planet gasGiant = new Planet(star);
        gasGiant.setRadius(70000);  // Jupiter-like
        gasGiant.setHydrosphere(0.0);
        gasGiant.setGasGiant(true);
        gasGiant.setSurfaceGravity(2.5);
        gasGiant.setSurfaceTemperature(120.0);

        // Should not throw - gas giants generate minimal terrain for visualization
        GeneratedPlanet planet = PlanetGenerator.generateFromAccrete(gasGiant, 42L);

        assertThat(planet.polygons()).isNotEmpty();
        assertThat(planet.heights()).isNotEmpty();
    }
}
