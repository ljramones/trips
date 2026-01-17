package com.teamgannon.trips.planetarymodelling.procedural;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class PlateAssignerTest {

    private PlanetConfig config;
    private List<Polygon> polygons;
    private AdjacencyGraph adjacency;

    @BeforeEach
    void setUp() {
        config = PlanetConfig.builder()
            .seed(12345L)
            .size(PlanetConfig.Size.DUEL)
            .plateCount(10)
            .build();
        var mesh = new IcosahedralMesh(config);
        polygons = mesh.generate();
        adjacency = new AdjacencyGraph(polygons);
    }

    @Test
    @DisplayName("All polygons are assigned to a plate")
    void allPolygonsAssigned() {
        var assigner = new PlateAssigner(config, adjacency);
        var assignment = assigner.assign();

        for (int i = 0; i < assignment.plateIndex().length; i++) {
            assertThat(assignment.plateIndex()[i])
                .as("Polygon %d should be assigned", i)
                .isGreaterThanOrEqualTo(0)
                .isLessThan(config.plateCount());
        }
    }

    @Test
    @DisplayName("Plate index length matches polygon count")
    void plateIndexLengthMatchesPolygonCount() {
        var assigner = new PlateAssigner(config, adjacency);
        var assignment = assigner.assign();

        assertThat(assignment.plateIndex().length).isEqualTo(polygons.size());
    }

    @Test
    @DisplayName("Plates list has correct size")
    void platesListHasCorrectSize() {
        var assigner = new PlateAssigner(config, adjacency);
        var assignment = assigner.assign();

        assertThat(assignment.plates()).hasSize(config.plateCount());
    }

    @Test
    @DisplayName("Sum of plate sizes equals polygon count")
    void plateSizesSumToPolygonCount() {
        var assigner = new PlateAssigner(config, adjacency);
        var assignment = assigner.assign();

        int total = assignment.plates().stream()
            .mapToInt(List::size)
            .sum();

        assertThat(total).isEqualTo(polygons.size());
    }

    @Test
    @DisplayName("Each polygon appears in exactly one plate")
    void eachPolygonInExactlyOnePlate() {
        var assigner = new PlateAssigner(config, adjacency);
        var assignment = assigner.assign();

        Set<Integer> seen = new HashSet<>();
        for (List<Integer> plate : assignment.plates()) {
            for (int polyIdx : plate) {
                assertThat(seen.add(polyIdx))
                    .as("Polygon %d should appear in only one plate", polyIdx)
                    .isTrue();
            }
        }
    }

    @Test
    @DisplayName("Plate index matches plates list")
    void plateIndexMatchesPlatesList() {
        var assigner = new PlateAssigner(config, adjacency);
        var assignment = assigner.assign();

        for (int plateIdx = 0; plateIdx < assignment.plates().size(); plateIdx++) {
            for (int polyIdx : assignment.plates().get(plateIdx)) {
                assertThat(assignment.plateIndex()[polyIdx])
                    .as("Polygon %d should be in plate %d", polyIdx, plateIdx)
                    .isEqualTo(plateIdx);
            }
        }
    }

    @Test
    @DisplayName("Adjacent plates are tracked")
    void adjacentPlatesTracked() {
        var assigner = new PlateAssigner(config, adjacency);
        var assignment = assigner.assign();

        assertThat(assignment.adjacentPlates()).hasSize(config.plateCount());

        // Adjacent plates should be symmetric
        for (int p = 0; p < config.plateCount(); p++) {
            for (int adj : assignment.adjacentPlates().get(p)) {
                assertThat(assignment.adjacentPlates().get(adj))
                    .as("Adjacent plates should be symmetric")
                    .contains(p);
            }
        }
    }

    @Test
    @DisplayName("Same seed produces identical assignment")
    void reproducibility() {
        var assigner1 = new PlateAssigner(config, adjacency);
        var assigner2 = new PlateAssigner(config, adjacency);

        var assignment1 = assigner1.assign();
        var assignment2 = assigner2.assign();

        assertThat(assignment1.plateIndex()).isEqualTo(assignment2.plateIndex());
    }

    @Test
    @DisplayName("Different seeds produce different assignments")
    void differentSeeds() {
        var config1 = PlanetConfig.builder()
            .seed(111L)
            .size(PlanetConfig.Size.DUEL)
            .plateCount(10)
            .build();
        var config2 = PlanetConfig.builder()
            .seed(222L)
            .size(PlanetConfig.Size.DUEL)
            .plateCount(10)
            .build();

        var mesh1 = new IcosahedralMesh(config1);
        var mesh2 = new IcosahedralMesh(config2);
        var adj1 = new AdjacencyGraph(mesh1.generate());
        var adj2 = new AdjacencyGraph(mesh2.generate());

        var assignment1 = new PlateAssigner(config1, adj1).assign();
        var assignment2 = new PlateAssigner(config2, adj2).assign();

        assertThat(assignment1.plateIndex()).isNotEqualTo(assignment2.plateIndex());
    }

    @ParameterizedTest
    @ValueSource(ints = {7, 10, 14, 18, 21})
    @DisplayName("Works with different plate counts")
    void differentPlateCounts(int plateCount) {
        var testConfig = PlanetConfig.builder()
            .seed(42L)
            .size(PlanetConfig.Size.DUEL)
            .plateCount(plateCount)
            .build();
        var mesh = new IcosahedralMesh(testConfig);
        var adj = new AdjacencyGraph(mesh.generate());

        var assigner = new PlateAssigner(testConfig, adj);
        var assignment = assigner.assign();

        assertThat(assignment.plates()).hasSize(plateCount);

        // All plates should have at least one polygon
        for (List<Integer> plate : assignment.plates()) {
            assertThat(plate)
                .as("Each plate should have at least one polygon")
                .isNotEmpty();
        }
    }

    @Test
    @DisplayName("Plates are contiguous (each polygon has at least one same-plate neighbor)")
    void platesAreContiguous() {
        var assigner = new PlateAssigner(config, adjacency);
        var assignment = assigner.assign();
        int[] plateIndex = assignment.plateIndex();

        for (int i = 0; i < plateIndex.length; i++) {
            int myPlate = plateIndex[i];
            int[] neighbors = adjacency.neighborsOnly(i);

            boolean hasSamePlateNeighbor = false;
            for (int neighbor : neighbors) {
                if (plateIndex[neighbor] == myPlate) {
                    hasSamePlateNeighbor = true;
                    break;
                }
            }

            // For plates with more than 1 polygon, each should have a same-plate neighbor
            // (Single-polygon plates are allowed at boundaries)
            if (assignment.plates().get(myPlate).size() > 1) {
                assertThat(hasSamePlateNeighbor)
                    .as("Polygon %d in plate %d should have a same-plate neighbor", i, myPlate)
                    .isTrue();
            }
        }
    }
}
