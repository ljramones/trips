package com.teamgannon.trips.planetarymodelling.procedural;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for JavaFxPlanetMeshConverter vertex averaging.
 */
class JavaFxPlanetMeshConverterTest {
    private static final double TEST_SCALE = 1.0;
    private static final double EXPECTED_HEIGHT_SCALE = 0.02;
    private static final double RADIUS_TOLERANCE = 0.001;

    @Test
    void testVertexSharingWithAveraging() {
        // Generate a small planet to test vertex sharing
        PlanetConfig config = PlanetConfig.builder()
            .seed(12345L)
            .size(PlanetConfig.Size.TINY)  // Small for fast test
            .build();

        PlanetGenerator.GeneratedPlanet planet = PlanetGenerator.generate(config);

        assertNotNull(planet.adjacency(), "Adjacency graph should be present");
        assertTrue(planet.polygons().size() > 100, "Should have polygons");

        // This will print debug output showing vertex sharing stats
        var meshes = JavaFxPlanetMeshConverter.convertByHeightWithAveraging(
            planet.polygons(),
            planet.heights(),
            planet.adjacency(),
            1.0
        );

        assertFalse(meshes.isEmpty(), "Should produce meshes");

        // Verify we got multiple height levels
        assertTrue(meshes.size() >= 3, "Should have multiple height levels");
    }

    @Test
    void testVertexSharingStatistics() {
        // Generate planet and check that vertices are properly shared
        PlanetConfig config = PlanetConfig.builder()
            .seed(42L)
            .size(PlanetConfig.Size.SMALL)
            .build();

        PlanetGenerator.GeneratedPlanet planet = PlanetGenerator.generate(config);

        System.out.println("\n=== Vertex Sharing Test ===");
        System.out.println("Polygon count: " + planet.polygons().size());

        // Convert with averaging - debug output will show sharing stats
        var meshes = JavaFxPlanetMeshConverter.convertByHeightWithAveraging(
            planet.polygons(),
            planet.heights(),
            planet.adjacency(),
            1.0
        );

        System.out.println("Height levels: " + meshes.size());
        System.out.println("=== End Test ===\n");
    }

    @Test
    void testPreciseHeightAveraging() {
        // Test that preciseHeights path works correctly
        PlanetConfig config = PlanetConfig.builder()
            .seed(999L)
            .size(PlanetConfig.Size.SMALL)
            .build();

        PlanetGenerator.GeneratedPlanet planet = PlanetGenerator.generate(config);
        double[] preciseHeights = planet.preciseHeights();

        System.out.println("\n=== Precise Height Averaging Test ===");
        System.out.println("Polygon count: " + planet.polygons().size());
        System.out.println("PreciseHeights available: " + (preciseHeights != null && preciseHeights.length > 0));

        assertNotNull(preciseHeights, "Precise heights should be available");
        assertTrue(preciseHeights.length > 0, "Precise heights should have data");

        // Convert with precise heights averaging
        var meshes = JavaFxPlanetMeshConverter.convertByHeightWithAveraging(
            planet.polygons(),
            planet.heights(),
            planet.adjacency(),
            1.0,
            preciseHeights  // Pass precise heights for smoother averaging
        );

        assertFalse(meshes.isEmpty(), "Should produce meshes");
        System.out.println("Height levels: " + meshes.size());
        System.out.println("=== End Precise Test ===\n");
    }

    @Test
    void testReliefDisplacementBounds() {
        PlanetConfig config = PlanetConfig.builder()
            .seed(7L)
            .size(PlanetConfig.Size.TINY)
            .build();

        List<Polygon> polygons = new IcosahedralMesh(config).generate();

        int[] highHeights = new int[polygons.size()];
        int[] lowHeights = new int[polygons.size()];
        for (int i = 0; i < polygons.size(); i++) {
            highHeights[i] = 4;
            lowHeights[i] = -4;
        }

        double expectedHighRadius = TEST_SCALE * (1.0 + 4 * EXPECTED_HEIGHT_SCALE);
        double expectedLowRadius = TEST_SCALE * (1.0 - 4 * EXPECTED_HEIGHT_SCALE);

        double maxRadius = maxRadius(JavaFxPlanetMeshConverter.convert(polygons, highHeights, TEST_SCALE));
        double minRadius = minRadius(JavaFxPlanetMeshConverter.convert(polygons, lowHeights, TEST_SCALE));

        assertEquals(expectedHighRadius, maxRadius, RADIUS_TOLERANCE,
            "Max radius should match expected relief scaling");
        assertEquals(expectedLowRadius, minRadius, RADIUS_TOLERANCE,
            "Min radius should match expected relief scaling");
    }

    @Test
    void testReliefSensitivity() {
        PlanetConfig config = PlanetConfig.builder()
            .seed(8L)
            .size(PlanetConfig.Size.TINY)
            .build();

        List<Polygon> polygons = new IcosahedralMesh(config).generate();
        int[] heights = new int[polygons.size()];
        for (int i = 0; i < polygons.size(); i++) {
            heights[i] = (i % 2 == 0) ? 4 : -4;
        }

        double maxRadius = maxRadius(JavaFxPlanetMeshConverter.convert(polygons, heights, TEST_SCALE));
        double minRadius = minRadius(JavaFxPlanetMeshConverter.convert(polygons, heights, TEST_SCALE));

        assertTrue(maxRadius - minRadius > 0.1,
            "Relief should create a noticeable radius spread");
    }

    private double maxRadius(javafx.scene.shape.TriangleMesh mesh) {
        var points = mesh.getPoints();
        double max = 0.0;
        for (int i = 0; i < points.size(); i += 3) {
            double x = points.get(i);
            double y = points.get(i + 1);
            double z = points.get(i + 2);
            double r = Math.sqrt(x * x + y * y + z * z);
            if (r > max) {
                max = r;
            }
        }
        return max;
    }

    private double minRadius(javafx.scene.shape.TriangleMesh mesh) {
        var points = mesh.getPoints();
        double min = Double.MAX_VALUE;
        for (int i = 0; i < points.size(); i += 3) {
            double x = points.get(i);
            double y = points.get(i + 1);
            double z = points.get(i + 2);
            double r = Math.sqrt(x * x + y * y + z * z);
            if (r < min) {
                min = r;
            }
        }
        return min;
    }
}
