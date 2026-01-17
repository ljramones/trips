package com.teamgannon.trips.planetarymodelling.procedural;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import com.teamgannon.trips.planetarymodelling.procedural.PlanetConfig.Size;
import com.teamgannon.trips.planetarymodelling.procedural.PlanetGenerator.GeneratedPlanet;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.data.Percentage.withPercentage;

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
}
