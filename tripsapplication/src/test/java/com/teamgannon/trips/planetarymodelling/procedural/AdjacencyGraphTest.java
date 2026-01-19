package com.teamgannon.trips.planetarymodelling.procedural;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class AdjacencyGraphTest {

    private List<Polygon> polygons;
    private AdjacencyGraph adjacency;

    @BeforeEach
    void setUp() {
        var config = PlanetConfig.builder().size(PlanetConfig.Size.DUEL).build();
        var mesh = new IcosahedralMesh(config);
        polygons = mesh.generate();
        adjacency = new AdjacencyGraph(polygons);
    }

    @Test
    @DisplayName("Size matches polygon count")
    void sizeMatchesPolygonCount() {
        assertThat(adjacency.size()).isEqualTo(polygons.size());
    }

    @Test
    @DisplayName("neighbors() includes self as first element")
    void neighborsIncludesSelf() {
        for (int i = 0; i < adjacency.size(); i++) {
            int[] neighbors = adjacency.neighbors(i);
            assertThat(neighbors[0])
                .as("First neighbor should be self")
                .isEqualTo(i);
        }
    }

    @Test
    @DisplayName("neighborsOnly() excludes self")
    void neighborsOnlyExcludesSelf() {
        for (int i = 0; i < adjacency.size(); i++) {
            int[] neighbors = adjacency.neighborsOnly(i);
            for (int neighbor : neighbors) {
                assertThat(neighbor)
                    .as("neighborsOnly should not include self")
                    .isNotEqualTo(i);
            }
        }
    }

    @Test
    @DisplayName("Adjacency is symmetric")
    void adjacencyIsSymmetric() {
        for (int i = 0; i < adjacency.size(); i++) {
            for (int neighbor : adjacency.neighborsOnly(i)) {
                assertThat(adjacency.areNeighbors(neighbor, i))
                    .as("If %d neighbors %d, then %d should neighbor %d", i, neighbor, neighbor, i)
                    .isTrue();
            }
        }
    }

    @Test
    @DisplayName("Hexagons have 6 neighbors")
    void hexagonsHaveSixNeighbors() {
        for (int i = 0; i < polygons.size(); i++) {
            if (polygons.get(i).isHexagon()) {
                int[] neighbors = adjacency.neighborsOnly(i);
                assertThat(neighbors.length)
                    .as("Hexagon should have 6 neighbors")
                    .isEqualTo(6);
            }
        }
    }

    @Test
    @DisplayName("Pentagons have 5 neighbors")
    void pentagonsHaveFiveNeighbors() {
        for (int i = 0; i < polygons.size(); i++) {
            if (polygons.get(i).isPentagon()) {
                int[] neighbors = adjacency.neighborsOnly(i);
                assertThat(neighbors.length)
                    .as("Pentagon should have 5 neighbors")
                    .isEqualTo(5);
            }
        }
    }

    @Test
    @DisplayName("areNeighbors returns false for non-neighbors")
    void areNeighborsReturnsFalseForNonNeighbors() {
        // Find two non-adjacent polygons
        int[] neighbors0 = adjacency.neighborsOnly(0);
        Set<Integer> neighbor0Set = new HashSet<>();
        for (int n : neighbors0) {
            neighbor0Set.add(n);
        }

        // Find a polygon that is not a neighbor of 0
        for (int i = 1; i < adjacency.size(); i++) {
            if (!neighbor0Set.contains(i)) {
                assertThat(adjacency.areNeighbors(0, i))
                    .as("Polygon 0 and %d should not be neighbors", i)
                    .isFalse();
                break;
            }
        }
    }

    @Test
    @DisplayName("areNeighbors returns true for actual neighbors")
    void areNeighborsReturnsTrueForNeighbors() {
        int[] neighbors0 = adjacency.neighborsOnly(0);

        for (int neighbor : neighbors0) {
            assertThat(adjacency.areNeighbors(0, neighbor)).isTrue();
            assertThat(adjacency.areNeighbors(neighbor, 0)).isTrue();
        }
    }

    @Test
    @DisplayName("All polygons have at least one neighbor")
    void allPolygonsHaveNeighbors() {
        for (int i = 0; i < adjacency.size(); i++) {
            assertThat(adjacency.neighborsOnly(i).length)
                .as("Polygon %d should have neighbors", i)
                .isGreaterThan(0);
        }
    }

    @Test
    @DisplayName("Neighbor indices are valid")
    void neighborIndicesAreValid() {
        for (int i = 0; i < adjacency.size(); i++) {
            for (int neighbor : adjacency.neighborsOnly(i)) {
                assertThat(neighbor)
                    .as("Neighbor index should be valid")
                    .isGreaterThanOrEqualTo(0)
                    .isLessThan(adjacency.size());
            }
        }
    }

    @Test
    @DisplayName("No duplicate neighbors")
    void noDuplicateNeighbors() {
        for (int i = 0; i < adjacency.size(); i++) {
            int[] neighbors = adjacency.neighbors(i);
            Set<Integer> unique = new HashSet<>();
            for (int n : neighbors) {
                assertThat(unique.add(n))
                    .as("Polygon %d should not have duplicate neighbor %d", i, n)
                    .isTrue();
            }
        }
    }

    @ParameterizedTest
    @EnumSource(value = PlanetConfig.Size.class, names = { "TINY", "SMALL", "STANDARD" })
    @DisplayName("Neighbor counts stay within expected bounds across sizes")
    void neighborCountsAcrossSizes(PlanetConfig.Size size) {
        var config = PlanetConfig.builder().size(size).build();
        var mesh = new IcosahedralMesh(config);
        var polys = mesh.generate();
        var graph = new AdjacencyGraph(polys);

        int totalNeighbors = 0;
        int pentagonCount = 0;
        int pentagonCorrect = 0;
        int hexagonCount = 0;
        int hexagonCorrect = 0;

        for (int i = 0; i < polys.size(); i++) {
            int neighbors = graph.neighborsOnly(i).length;
            totalNeighbors += neighbors;
            if (polys.get(i).isPentagon()) {
                pentagonCount++;
                if (neighbors == 5) pentagonCorrect++;
            } else {
                hexagonCount++;
                if (neighbors == 6) hexagonCorrect++;
            }
        }

        double avgNeighbors = (double) totalNeighbors / polys.size();

        assertThat(avgNeighbors)
            .as("Average neighbor count should be close to 6")
            .isBetween(5.6, 6.1);

        if (pentagonCount > 0) {
            double pentagonAccuracy = (double) pentagonCorrect / pentagonCount;
            assertThat(pentagonAccuracy)
                .as("Pentagon neighbor count accuracy should be high")
                .isGreaterThan(0.95);
        }

        if (hexagonCount > 0) {
            double hexagonAccuracy = (double) hexagonCorrect / hexagonCount;
            assertThat(hexagonAccuracy)
                .as("Hexagon neighbor count accuracy should be high")
                .isGreaterThan(0.95);
        }
    }
}
