package com.teamgannon.trips.planetarymodelling.procedural;

import com.teamgannon.trips.planetarymodelling.procedural.BoundaryDetector.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class BoundaryDetectorTest {

    private PlanetConfig config;
    private PlateAssigner.PlateAssignment plateAssignment;
    private BoundaryDetector detector;

    @BeforeEach
    void setUp() {
        config = PlanetConfig.builder()
            .seed(12345L)
            .size(PlanetConfig.Size.DUEL)
            .plateCount(10)
            .build();
        var mesh = new IcosahedralMesh(config);
        var polygons = mesh.generate();
        var adjacency = new AdjacencyGraph(polygons);
        var assigner = new PlateAssigner(config, adjacency);
        plateAssignment = assigner.assign();
        detector = new BoundaryDetector(config, plateAssignment);
    }

    @Test
    @DisplayName("analyze() returns non-null result")
    void analyzeReturnsResult() {
        var analysis = detector.analyze();

        assertThat(analysis).isNotNull();
        assertThat(analysis.plateTypes()).isNotNull();
        assertThat(analysis.boundaries()).isNotNull();
        assertThat(analysis.sizeOrder()).isNotNull();
    }

    @Test
    @DisplayName("All plates have a type assigned")
    void allPlatesHaveType() {
        var analysis = detector.analyze();

        assertThat(analysis.plateTypes()).hasSize(config.plateCount());
        for (PlateType type : analysis.plateTypes()) {
            assertThat(type).isNotNull();
        }
    }

    @Test
    @DisplayName("Plate types are either oceanic or continental")
    void plateTypesAreValid() {
        var analysis = detector.analyze();

        for (PlateType type : analysis.plateTypes()) {
            assertThat(type).isIn(PlateType.OCEANIC, PlateType.CONTINENTAL);
        }
    }

    @Test
    @DisplayName("Size order contains all plate indices")
    void sizeOrderContainsAllPlates() {
        var analysis = detector.analyze();

        assertThat(analysis.sizeOrder()).hasSize(config.plateCount());
        assertThat(analysis.sizeOrder()).containsExactlyInAnyOrderElementsOf(
            java.util.stream.IntStream.range(0, config.plateCount())
                .boxed()
                .toList()
        );
    }

    @Test
    @DisplayName("Size order is sorted by descending plate size")
    void sizeOrderIsSorted() {
        var analysis = detector.analyze();
        List<List<Integer>> plates = plateAssignment.plates();

        for (int i = 0; i < analysis.sizeOrder().size() - 1; i++) {
            int plate1 = analysis.sizeOrder().get(i);
            int plate2 = analysis.sizeOrder().get(i + 1);

            assertThat(plates.get(plate1).size())
                .as("Plate %d should be >= plate %d in size", plate1, plate2)
                .isGreaterThanOrEqualTo(plates.get(plate2).size());
        }
    }

    @Test
    @DisplayName("Largest plate is oceanic (water world constraint)")
    void largestPlateIsOceanic() {
        var analysis = detector.analyze();

        int largestPlate = analysis.sizeOrder().get(0);
        assertThat(analysis.plateTypes()[largestPlate])
            .as("Largest plate should be oceanic")
            .isEqualTo(PlateType.OCEANIC);
    }

    @Test
    @DisplayName("All adjacent plate pairs have a boundary type")
    void allAdjacentPairsHaveBoundary() {
        var analysis = detector.analyze();

        for (int p = 0; p < config.plateCount(); p++) {
            for (int adj : plateAssignment.adjacentPlates().get(p)) {
                if (adj > p) {
                    PlatePair pair = new PlatePair(p, adj);
                    assertThat(analysis.boundaries().get(pair))
                        .as("Boundary between plate %d and %d should exist", p, adj)
                        .isNotNull();
                }
            }
        }
    }

    @Test
    @DisplayName("Boundary types are valid")
    void boundaryTypesAreValid() {
        var analysis = detector.analyze();

        for (BoundaryType type : analysis.boundaries().values()) {
            assertThat(type).isIn(
                BoundaryType.CONVERGENT,
                BoundaryType.DIVERGENT,
                BoundaryType.TRANSFORM
            );
        }
    }

    @Test
    @DisplayName("PlatePair normalizes order")
    void platePairNormalizesOrder() {
        var pair1 = new PlatePair(3, 7);
        var pair2 = new PlatePair(7, 3);

        assertThat(pair1).isEqualTo(pair2);
        assertThat(pair1.plate1()).isEqualTo(3);
        assertThat(pair1.plate2()).isEqualTo(7);
    }

    @Test
    @DisplayName("getPlateType returns correct type")
    void getPlateTypeReturnsCorrectType() {
        detector.analyze();

        for (int p = 0; p < config.plateCount(); p++) {
            PlateType type = detector.getPlateType(p);
            assertThat(type).isNotNull();
        }
    }

    @Test
    @DisplayName("getBoundary returns correct boundary type")
    void getBoundaryReturnsCorrectType() {
        var analysis = detector.analyze();

        for (int p = 0; p < config.plateCount(); p++) {
            for (int adj : plateAssignment.adjacentPlates().get(p)) {
                BoundaryType type = detector.getBoundary(p, adj);
                assertThat(type)
                    .as("getBoundary(%d, %d) should match analysis", p, adj)
                    .isEqualTo(analysis.boundaries().get(new PlatePair(p, adj)));
            }
        }
    }

    @Test
    @DisplayName("Same seed produces identical analysis")
    void reproducibility() {
        var detector1 = new BoundaryDetector(config, plateAssignment);
        var detector2 = new BoundaryDetector(config, plateAssignment);

        var analysis1 = detector1.analyze();
        var analysis2 = detector2.analyze();

        assertThat(analysis1.plateTypes()).isEqualTo(analysis2.plateTypes());
        assertThat(analysis1.boundaries()).isEqualTo(analysis2.boundaries());
        assertThat(analysis1.sizeOrder()).isEqualTo(analysis2.sizeOrder());
    }

    @Test
    @DisplayName("At least 30% of surface is oceanic")
    void minimumOceanicCoverage() {
        var analysis = detector.analyze();
        List<List<Integer>> plates = plateAssignment.plates();

        int totalPolygons = plates.stream().mapToInt(List::size).sum();
        int oceanicPolygons = 0;

        for (int p = 0; p < config.plateCount(); p++) {
            if (analysis.plateTypes()[p] == PlateType.OCEANIC) {
                oceanicPolygons += plates.get(p).size();
            }
        }

        double oceanicFraction = (double) oceanicPolygons / totalPolygons;
        assertThat(oceanicFraction)
            .as("At least 30%% of surface should be oceanic")
            .isGreaterThanOrEqualTo(0.30);
    }

    @Test
    @DisplayName("Both oceanic and continental plates exist")
    void bothPlateTypesExist() {
        var analysis = detector.analyze();

        boolean hasOceanic = false;
        boolean hasContinental = false;

        for (PlateType type : analysis.plateTypes()) {
            if (type == PlateType.OCEANIC) hasOceanic = true;
            if (type == PlateType.CONTINENTAL) hasContinental = true;
        }

        assertThat(hasOceanic).as("Should have oceanic plates").isTrue();
        assertThat(hasContinental).as("Should have continental plates").isTrue();
    }
}
