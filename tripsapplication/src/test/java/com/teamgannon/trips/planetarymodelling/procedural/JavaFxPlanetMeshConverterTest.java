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

    // ===========================================
    // MeshConversionOptions Tests
    // ===========================================

    @Test
    void testMeshConversionOptionsDefaults() {
        var options = JavaFxPlanetMeshConverter.MeshConversionOptions.defaults();

        assertFalse(options.useAveraging(), "Default should not use averaging");
        assertFalse(options.useNormals(), "Default should not use normals");
        assertFalse(options.groupByValue(), "Default should not group by value");
        assertNull(options.preciseHeights(), "Default should have no precise heights");
        assertNull(options.adjacency(), "Default should have no adjacency");
    }

    @Test
    void testMeshConversionOptionsSmooth() {
        PlanetConfig config = PlanetConfig.builder()
            .seed(1L)
            .size(PlanetConfig.Size.TINY)
            .build();
        AdjacencyGraph adj = new AdjacencyGraph(new IcosahedralMesh(config).generate());

        var options = JavaFxPlanetMeshConverter.MeshConversionOptions.smooth(adj);

        assertTrue(options.useAveraging(), "Smooth should use averaging");
        assertTrue(options.useNormals(), "Smooth should use normals");
        assertFalse(options.groupByValue(), "Smooth should not group by value");
        assertNotNull(options.adjacency(), "Smooth should have adjacency");
    }

    @Test
    void testMeshConversionOptionsByHeight() {
        PlanetConfig config = PlanetConfig.builder()
            .seed(1L)
            .size(PlanetConfig.Size.TINY)
            .build();
        AdjacencyGraph adj = new AdjacencyGraph(new IcosahedralMesh(config).generate());

        var options = JavaFxPlanetMeshConverter.MeshConversionOptions.byHeight(adj);

        assertTrue(options.useAveraging(), "ByHeight should use averaging");
        assertTrue(options.useNormals(), "ByHeight should use normals");
        assertTrue(options.groupByValue(), "ByHeight should group by value");
    }

    @Test
    void testMeshConversionOptionsWithPreciseHeights() {
        PlanetConfig config = PlanetConfig.builder()
            .seed(1L)
            .size(PlanetConfig.Size.TINY)
            .build();
        List<Polygon> polys = new IcosahedralMesh(config).generate();
        AdjacencyGraph adj = new AdjacencyGraph(polys);
        double[] precise = new double[polys.size()];

        var options = JavaFxPlanetMeshConverter.MeshConversionOptions.withPreciseHeights(precise, adj);

        assertTrue(options.useAveraging());
        assertTrue(options.useNormals());
        assertTrue(options.groupByValue());
        assertNotNull(options.preciseHeights());
        assertEquals(polys.size(), options.preciseHeights().length);
    }

    // ===========================================
    // Basic Conversion Tests
    // ===========================================

    @Test
    void testConvertProducesValidMesh() {
        PlanetConfig config = PlanetConfig.builder()
            .seed(123L)
            .size(PlanetConfig.Size.TINY)
            .build();

        List<Polygon> polygons = new IcosahedralMesh(config).generate();
        int[] heights = new int[polygons.size()];

        var mesh = JavaFxPlanetMeshConverter.convert(polygons, heights, 1.0);

        assertNotNull(mesh, "Mesh should not be null");
        assertTrue(mesh.getPoints().size() > 0, "Mesh should have points");
        assertTrue(mesh.getFaces().size() > 0, "Mesh should have faces");
        assertTrue(mesh.getTexCoords().size() > 0, "Mesh should have tex coords");
    }

    @Test
    void testConvertSmoothProducesValidMesh() {
        PlanetConfig config = PlanetConfig.builder()
            .seed(456L)
            .size(PlanetConfig.Size.TINY)
            .build();

        List<Polygon> polygons = new IcosahedralMesh(config).generate();
        double[] preciseHeights = new double[polygons.size()];
        for (int i = 0; i < preciseHeights.length; i++) {
            preciseHeights[i] = Math.sin(i * 0.1) * 2.0;  // Varying heights
        }

        var mesh = JavaFxPlanetMeshConverter.convertSmooth(polygons, preciseHeights, 1.0);

        assertNotNull(mesh);
        assertTrue(mesh.getPoints().size() > 0);
        assertTrue(mesh.getFaces().size() > 0);
    }

    @Test
    void testConvertUnifiedWithOptions() {
        PlanetConfig config = PlanetConfig.builder()
            .seed(789L)
            .size(PlanetConfig.Size.TINY)
            .build();

        List<Polygon> polygons = new IcosahedralMesh(config).generate();
        AdjacencyGraph adj = new AdjacencyGraph(polygons);
        int[] heights = new int[polygons.size()];

        var options = JavaFxPlanetMeshConverter.MeshConversionOptions.smooth(adj);
        var mesh = JavaFxPlanetMeshConverter.convertUnified(polygons, heights, 1.0, options);

        assertNotNull(mesh);
        assertTrue(mesh.getPoints().size() > 0);
    }

    @Test
    void testConvertUnifiedByHeightProducesMultipleMeshes() {
        PlanetConfig config = PlanetConfig.builder()
            .seed(111L)
            .size(PlanetConfig.Size.TINY)
            .build();

        List<Polygon> polygons = new IcosahedralMesh(config).generate();
        AdjacencyGraph adj = new AdjacencyGraph(polygons);
        int[] heights = new int[polygons.size()];

        // Assign varied heights
        for (int i = 0; i < heights.length; i++) {
            heights[i] = (i % 5) - 2;  // Range -2 to +2
        }

        var options = JavaFxPlanetMeshConverter.MeshConversionOptions.byHeight(adj);
        var meshes = JavaFxPlanetMeshConverter.convertUnifiedByHeight(polygons, heights, 1.0, options);

        assertNotNull(meshes);
        assertTrue(meshes.size() >= 3, "Should have multiple height levels");

        // Each height level should have a mesh
        for (var mesh : meshes.values()) {
            assertNotNull(mesh);
            assertTrue(mesh.getPoints().size() > 0);
        }
    }

    // ===========================================
    // Color and Material Tests
    // ===========================================

    @Test
    void testGetColorForHeight() {
        // Test all height levels
        for (int h = -4; h <= 4; h++) {
            var color = JavaFxPlanetMeshConverter.getColorForHeight(h);
            assertNotNull(color, "Color for height " + h + " should not be null");
        }

        // Deep ocean should be dark blue
        var deepOcean = JavaFxPlanetMeshConverter.getColorForHeight(-4);
        assertTrue(deepOcean.getBlue() > deepOcean.getRed(), "Deep ocean should be blue");
        assertTrue(deepOcean.getBlue() > deepOcean.getGreen(), "Deep ocean should be blue");

        // High mountains should be dark
        var mountains = JavaFxPlanetMeshConverter.getColorForHeight(4);
        assertTrue(mountains.getBrightness() < 0.5, "High mountains should be dark");
    }

    @Test
    void testGetColorForPreciseHeight() {
        // Test interpolation
        var color_0 = JavaFxPlanetMeshConverter.getColorForPreciseHeight(0.0);
        var color_05 = JavaFxPlanetMeshConverter.getColorForPreciseHeight(0.5);
        var color_1 = JavaFxPlanetMeshConverter.getColorForPreciseHeight(1.0);

        assertNotNull(color_0);
        assertNotNull(color_05);
        assertNotNull(color_1);

        // Colors should be different (interpolation working)
        assertFalse(color_0.equals(color_1), "Different heights should produce different colors");
    }

    @Test
    void testGetColorForRainfall() {
        // Test all rainfall buckets
        for (int bucket = 0; bucket <= 7; bucket++) {
            var color = JavaFxPlanetMeshConverter.getColorForRainfall(bucket);
            assertNotNull(color, "Color for bucket " + bucket + " should not be null");
        }

        // Dry should be brownish
        var dry = JavaFxPlanetMeshConverter.getColorForRainfall(0);
        assertTrue(dry.getRed() > dry.getBlue(), "Dry should be brownish");

        // Wet should be bluish
        var wet = JavaFxPlanetMeshConverter.getColorForRainfall(7);
        assertTrue(wet.getBlue() > wet.getRed(), "Wet should be bluish");
    }

    @Test
    void testGetColorForNormalizedRainfall() {
        var dry = JavaFxPlanetMeshConverter.getColorForNormalizedRainfall(0.0);
        var mid = JavaFxPlanetMeshConverter.getColorForNormalizedRainfall(0.5);
        var wet = JavaFxPlanetMeshConverter.getColorForNormalizedRainfall(1.0);

        assertNotNull(dry);
        assertNotNull(mid);
        assertNotNull(wet);

        // Interpolation should produce smooth gradient
        assertFalse(dry.equals(wet), "Extremes should be different");
    }

    @Test
    void testCreateMaterialForHeight() {
        for (int h = -4; h <= 4; h++) {
            var material = JavaFxPlanetMeshConverter.createMaterialForHeight(h);
            assertNotNull(material, "Material for height " + h + " should not be null");
            assertNotNull(material.getDiffuseColor(), "Material should have diffuse color");
            assertNotNull(material.getSpecularColor(), "Material should have specular color");
        }
    }

    @Test
    void testCreateMaterialForRainfall() {
        for (int bucket = 0; bucket <= 7; bucket++) {
            var material = JavaFxPlanetMeshConverter.createMaterialForRainfall(bucket);
            assertNotNull(material, "Material for bucket " + bucket + " should not be null");
            assertNotNull(material.getDiffuseColor());
        }
    }

    @Test
    void testCreateTerrainMaterial() {
        PlanetConfig config = PlanetConfig.builder()
            .seed(222L)
            .size(PlanetConfig.Size.TINY)
            .build();

        List<Polygon> polygons = new IcosahedralMesh(config).generate();
        int[] heights = new int[polygons.size()];

        var material = JavaFxPlanetMeshConverter.createTerrainMaterial(polygons, heights);

        assertNotNull(material);
        assertNotNull(material.getDiffuseColor());
        assertNotNull(material.getSpecularColor());
    }

    @Test
    void testCreateSmoothTerrainMaterial() {
        PlanetConfig config = PlanetConfig.builder()
            .seed(333L)
            .size(PlanetConfig.Size.TINY)
            .build();

        List<Polygon> polygons = new IcosahedralMesh(config).generate();
        double[] preciseHeights = new double[polygons.size()];

        var material = JavaFxPlanetMeshConverter.createSmoothTerrainMaterial(polygons, preciseHeights);

        assertNotNull(material);
        assertNotNull(material.getDiffuseColor());
    }

    // ===========================================
    // Rainfall Heatmap Tests
    // ===========================================

    @Test
    void testConvertByRainfall() {
        PlanetConfig config = PlanetConfig.builder()
            .seed(444L)
            .size(PlanetConfig.Size.TINY)
            .build();

        List<Polygon> polygons = new IcosahedralMesh(config).generate();
        int[] heights = new int[polygons.size()];
        double[] rainfall = new double[polygons.size()];

        // Create varied rainfall
        for (int i = 0; i < rainfall.length; i++) {
            rainfall[i] = Math.random() * 10.0;
        }

        var meshes = JavaFxPlanetMeshConverter.convertByRainfall(polygons, heights, rainfall, 1.0);

        assertNotNull(meshes);
        assertFalse(meshes.isEmpty(), "Should have rainfall buckets");

        // Buckets should be in range 0-7
        for (int bucket : meshes.keySet()) {
            assertTrue(bucket >= 0 && bucket <= 7, "Bucket should be in range 0-7");
        }
    }

    @Test
    void testConvertByRainfallWithNullRainfall() {
        PlanetConfig config = PlanetConfig.builder()
            .seed(555L)
            .size(PlanetConfig.Size.TINY)
            .build();

        List<Polygon> polygons = new IcosahedralMesh(config).generate();
        int[] heights = new int[polygons.size()];

        var meshes = JavaFxPlanetMeshConverter.convertByRainfall(polygons, heights, null, 1.0);

        assertNotNull(meshes);
        assertTrue(meshes.isEmpty(), "Should be empty with null rainfall");
    }

    @Test
    void testConvertByRainfallWithEmptyRainfall() {
        PlanetConfig config = PlanetConfig.builder()
            .seed(666L)
            .size(PlanetConfig.Size.TINY)
            .build();

        List<Polygon> polygons = new IcosahedralMesh(config).generate();
        int[] heights = new int[polygons.size()];
        double[] rainfall = new double[0];

        var meshes = JavaFxPlanetMeshConverter.convertByRainfall(polygons, heights, rainfall, 1.0);

        assertNotNull(meshes);
        assertTrue(meshes.isEmpty(), "Should be empty with empty rainfall");
    }

    // ===========================================
    // Edge Case Tests
    // ===========================================

    @Test
    void testConvertHandlesExtremeHeights() {
        PlanetConfig config = PlanetConfig.builder()
            .seed(777L)
            .size(PlanetConfig.Size.TINY)
            .build();

        List<Polygon> polygons = new IcosahedralMesh(config).generate();
        int[] heights = new int[polygons.size()];

        // Set extreme heights
        heights[0] = -4;  // Deep ocean
        heights[1] = 4;   // High mountains

        var mesh = JavaFxPlanetMeshConverter.convert(polygons, heights, 1.0);

        assertNotNull(mesh);
        assertTrue(mesh.getPoints().size() > 0);
    }

    @Test
    void testScaleFactorAffectsSize() {
        PlanetConfig config = PlanetConfig.builder()
            .seed(888L)
            .size(PlanetConfig.Size.TINY)
            .build();

        List<Polygon> polygons = new IcosahedralMesh(config).generate();
        int[] heights = new int[polygons.size()];

        var meshSmall = JavaFxPlanetMeshConverter.convert(polygons, heights, 1.0);
        var meshLarge = JavaFxPlanetMeshConverter.convert(polygons, heights, 2.0);

        double radiusSmall = maxRadius(meshSmall);
        double radiusLarge = maxRadius(meshLarge);

        assertTrue(radiusLarge > radiusSmall, "Larger scale should produce larger mesh");
        assertEquals(radiusSmall * 2.0, radiusLarge, 0.01, "Scale 2.0 should double radius");
    }
}
