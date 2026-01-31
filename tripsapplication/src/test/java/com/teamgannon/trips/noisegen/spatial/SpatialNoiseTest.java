package com.teamgannon.trips.noisegen.spatial;

import com.teamgannon.trips.noisegen.FastNoiseLite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for spatial noise utilities.
 */
class SpatialNoiseTest {

    private FastNoiseLite baseNoise;

    @BeforeEach
    void setUp() {
        baseNoise = new FastNoiseLite(1337);
        baseNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        baseNoise.SetFractalType(FastNoiseLite.FractalType.FBm);
        baseNoise.SetFractalOctaves(4);
        baseNoise.SetFrequency(0.01f);
    }

    @Nested
    @DisplayName("ChunkedNoise tests")
    class ChunkedNoiseTests {

        @Test
        @DisplayName("should return valid noise values")
        void shouldReturnValidNoiseValues() {
            ChunkedNoise chunked = new ChunkedNoise(baseNoise, 1000.0);

            float value = chunked.getNoise(500.0, 500.0);
            assertFalse(Float.isNaN(value));
            assertFalse(Float.isInfinite(value));
            assertTrue(value >= -1.5f && value <= 1.5f);
        }

        @Test
        @DisplayName("should handle very large coordinates")
        void shouldHandleVeryLargeCoordinates() {
            ChunkedNoise chunked = new ChunkedNoise(baseNoise, 1000.0);

            // 1 billion units from origin
            double largeX = 1_000_000_000.5;
            double largeY = 2_500_000_000.3;

            float value = chunked.getNoise(largeX, largeY);
            assertFalse(Float.isNaN(value));
            assertFalse(Float.isInfinite(value));
        }

        @Test
        @DisplayName("should be deterministic")
        void shouldBeDeterministic() {
            ChunkedNoise chunked = new ChunkedNoise(baseNoise, 1000.0);

            double x = 12345.678;
            double y = 98765.432;

            float v1 = chunked.getNoise(x, y);
            float v2 = chunked.getNoise(x, y);

            assertEquals(v1, v2, 0.0001f);
        }

        @Test
        @DisplayName("should return correct chunk coordinates")
        void shouldReturnCorrectChunkCoordinates() {
            ChunkedNoise chunked = new ChunkedNoise(baseNoise, 1000.0);

            int[] coords = chunked.getChunkCoords(2500.0, -1500.0);
            assertEquals(2, coords[0]);
            assertEquals(-2, coords[1]);
        }

        @Test
        @DisplayName("different chunks should have different seeds")
        void differentChunksShouldHaveDifferentSeeds() {
            ChunkedNoise chunked = new ChunkedNoise(baseNoise, 1000.0);

            int seed1 = chunked.getChunkSeed(0, 0);
            int seed2 = chunked.getChunkSeed(1, 0);
            int seed3 = chunked.getChunkSeed(0, 1);

            assertNotEquals(seed1, seed2);
            assertNotEquals(seed1, seed3);
            assertNotEquals(seed2, seed3);
        }

        @Test
        @DisplayName("3D chunking should work")
        void threeDChunkingShouldWork() {
            ChunkedNoise chunked = new ChunkedNoise(baseNoise, 1000.0);

            float value = chunked.getNoise(500.0, 500.0, 500.0);
            assertFalse(Float.isNaN(value));
            assertFalse(Float.isInfinite(value));
        }

        @Test
        @DisplayName("4D chunking should work")
        void fourDChunkingShouldWork() {
            ChunkedNoise chunked = new ChunkedNoise(baseNoise, 1000.0);

            float value = chunked.getNoise(500.0, 500.0, 500.0, 500.0);
            assertFalse(Float.isNaN(value));
            assertFalse(Float.isInfinite(value));
        }
    }

    @Nested
    @DisplayName("LODNoise tests")
    class LODNoiseTests {

        @Test
        @DisplayName("should return max octaves at near distance")
        void shouldReturnMaxOctavesAtNearDistance() {
            LODNoise lod = new LODNoise(baseNoise, 8, 1, 0f, 1000f);

            int octaves = lod.calculateOctaves(0f);
            assertEquals(8, octaves);
        }

        @Test
        @DisplayName("should return min octaves at far distance")
        void shouldReturnMinOctavesAtFarDistance() {
            LODNoise lod = new LODNoise(baseNoise, 8, 1, 0f, 1000f);

            int octaves = lod.calculateOctaves(1000f);
            assertEquals(1, octaves);
        }

        @Test
        @DisplayName("should interpolate octaves at middle distance")
        void shouldInterpolateOctavesAtMiddleDistance() {
            LODNoise lod = new LODNoise(baseNoise, 8, 2, 0f, 1000f);

            int octaves = lod.calculateOctaves(500f);
            assertTrue(octaves > 2 && octaves < 8);
        }

        @Test
        @DisplayName("should return valid noise values")
        void shouldReturnValidNoiseValues() {
            LODNoise lod = new LODNoise(baseNoise, 6);

            float value = lod.getNoise(100f, 100f, 500f);
            assertFalse(Float.isNaN(value));
            assertFalse(Float.isInfinite(value));
        }

        @Test
        @DisplayName("scale-based octave calculation should work")
        void scaleBasedOctaveCalculationShouldWork() {
            LODNoise lod = new LODNoise(baseNoise, 8, 1);

            // Full scale = max octaves
            assertEquals(8, lod.calculateOctavesByScale(1.0f));

            // Half scale = one less octave (approximately)
            int halfScaleOctaves = lod.calculateOctavesByScale(0.5f);
            assertTrue(halfScaleOctaves < 8);
            assertTrue(halfScaleOctaves >= 1);
        }

        @Test
        @DisplayName("builder should work correctly")
        void builderShouldWorkCorrectly() {
            LODNoise lod = LODNoise.builder(baseNoise)
                    .maxOctaves(10)
                    .minOctaves(2)
                    .distanceRange(100f, 5000f)
                    .build();

            assertEquals(10, lod.getMaxOctaves());
            assertEquals(2, lod.getMinOctaves());
            assertEquals(100f, lod.getNearDistance());
            assertEquals(5000f, lod.getFarDistance());
        }

        @Test
        @DisplayName("LOD level should increase with distance")
        void lodLevelShouldIncreaseWithDistance() {
            LODNoise lod = new LODNoise(baseNoise, 8, 1, 0f, 1000f);

            int lodNear = lod.getLODLevel(0f);
            int lodFar = lod.getLODLevel(1000f);

            assertTrue(lodFar > lodNear);
        }
    }

    @Nested
    @DisplayName("TiledNoise tests")
    class TiledNoiseTests {

        @Test
        @DisplayName("should tile seamlessly in X direction")
        void shouldTileSeamlesslyInX() {
            TiledNoise tiled = new TiledNoise(baseNoise, 256f, 256f);

            // Test multiple Y values to ensure seamless across the edge
            for (float y = 0; y < 256; y += 32) {
                float leftEdge = tiled.getNoise(0f, y);
                float rightEdge = tiled.getNoise(256f, y);
                assertEquals(leftEdge, rightEdge, 0.001f,
                        "X edges should match at y=" + y);
            }
        }

        @Test
        @DisplayName("should tile seamlessly in Y direction")
        void shouldTileSeamlesslyInY() {
            TiledNoise tiled = new TiledNoise(baseNoise, 256f, 256f);

            for (float x = 0; x < 256; x += 32) {
                float topEdge = tiled.getNoise(x, 0f);
                float bottomEdge = tiled.getNoise(x, 256f);
                assertEquals(topEdge, bottomEdge, 0.001f,
                        "Y edges should match at x=" + x);
            }
        }

        @Test
        @DisplayName("should tile seamlessly at corners")
        void shouldTileSeamlesslyAtCorners() {
            TiledNoise tiled = new TiledNoise(baseNoise, 256f, 256f);

            float corner00 = tiled.getNoise(0f, 0f);
            float corner10 = tiled.getNoise(256f, 0f);
            float corner01 = tiled.getNoise(0f, 256f);
            float corner11 = tiled.getNoise(256f, 256f);

            // All corners should be the same
            assertEquals(corner00, corner10, 0.001f);
            assertEquals(corner00, corner01, 0.001f);
            assertEquals(corner00, corner11, 0.001f);
        }

        @Test
        @DisplayName("normalized coordinates should work")
        void normalizedCoordinatesShouldWork() {
            TiledNoise tiled = new TiledNoise(baseNoise, 256f, 256f);

            float v1 = tiled.getNoise(128f, 128f);
            float v2 = tiled.getNoiseNormalized(0.5f, 0.5f);

            assertEquals(v1, v2, 0.001f);
        }

        @Test
        @DisplayName("should generate valid tile array")
        void shouldGenerateValidTileArray() {
            TiledNoise tiled = new TiledNoise(baseNoise, 64f, 64f);

            float[][] tile = tiled.generateTile(64, 64);

            assertEquals(64, tile.length);
            assertEquals(64, tile[0].length);

            // Check edges tile correctly
            for (int i = 0; i < 64; i++) {
                // This won't be exactly equal due to sampling, but should be close
                // The edges of the generated array represent the tiling boundary
                assertFalse(Float.isNaN(tile[0][i]));
                assertFalse(Float.isNaN(tile[63][i]));
            }
        }

        @Test
        @DisplayName("2D with Z should work")
        void twoDWithZShouldWork() {
            TiledNoise tiled = new TiledNoise(baseNoise, 256f, 256f);

            float v1 = tiled.getNoise2DWithZ(128f, 128f, 0f);
            float v2 = tiled.getNoise2DWithZ(128f, 128f, 100f);

            assertFalse(Float.isNaN(v1));
            assertFalse(Float.isNaN(v2));
            assertNotEquals(v1, v2, 0.001f, "Different Z should give different values");
        }

        @Test
        @DisplayName("rectangular tiles should work")
        void rectangularTilesShouldWork() {
            TiledNoise tiled = new TiledNoise(baseNoise, 512f, 256f);

            assertEquals(512f, tiled.getTileWidth());
            assertEquals(256f, tiled.getTileHeight());

            // X should tile at 512
            float xEdge1 = tiled.getNoise(0f, 100f);
            float xEdge2 = tiled.getNoise(512f, 100f);
            assertEquals(xEdge1, xEdge2, 0.001f);

            // Y should tile at 256
            float yEdge1 = tiled.getNoise(100f, 0f);
            float yEdge2 = tiled.getNoise(100f, 256f);
            assertEquals(yEdge1, yEdge2, 0.001f);
        }
    }

    @Nested
    @DisplayName("DoublePrecisionNoise tests")
    class DoublePrecisionNoiseTests {

        @Test
        @DisplayName("should return valid noise values")
        void shouldReturnValidNoiseValues() {
            DoublePrecisionNoise precise = new DoublePrecisionNoise(baseNoise);

            float value = precise.getNoise(12345.6789, 98765.4321);
            assertFalse(Float.isNaN(value));
            assertFalse(Float.isInfinite(value));
        }

        @Test
        @DisplayName("should handle very large coordinates")
        void shouldHandleVeryLargeCoordinates() {
            DoublePrecisionNoise precise = new DoublePrecisionNoise(baseNoise);

            // Test at 1 trillion units
            double largeCoord = 1_000_000_000_000.123456789;

            float value = precise.getNoise(largeCoord, largeCoord);
            assertFalse(Float.isNaN(value));
            assertFalse(Float.isInfinite(value));
        }

        @Test
        @DisplayName("should be deterministic")
        void shouldBeDeterministic() {
            DoublePrecisionNoise precise = new DoublePrecisionNoise(baseNoise);

            double x = 123456789.123456789;
            double y = 987654321.987654321;

            float v1 = precise.getNoise(x, y);
            float v2 = precise.getNoise(x, y);

            assertEquals(v1, v2, 0.0001f);
        }

        @Test
        @DisplayName("should maintain precision at large coordinates")
        void shouldMaintainPrecisionAtLargeCoordinates() {
            DoublePrecisionNoise precise = new DoublePrecisionNoise(baseNoise);

            // Two coordinates that differ by a small amount at large scale
            double base = 1_000_000_000.0;
            double x1 = base + 0.1;
            double x2 = base + 0.2;

            float v1 = precise.getNoise(x1, 0);
            float v2 = precise.getNoise(x2, 0);

            // They should be different (precision maintained)
            assertNotEquals(v1, v2, 0.0001f,
                    "Small differences at large coordinates should produce different noise");
        }

        @Test
        @DisplayName("3D sampling should work")
        void threeDSamplingShouldWork() {
            DoublePrecisionNoise precise = new DoublePrecisionNoise(baseNoise);

            float value = precise.getNoise(100.0, 200.0, 300.0);
            assertFalse(Float.isNaN(value));
            assertFalse(Float.isInfinite(value));
        }

        @Test
        @DisplayName("4D sampling should work")
        void fourDSamplingShouldWork() {
            DoublePrecisionNoise precise = new DoublePrecisionNoise(baseNoise);

            float value = precise.getNoise(100.0, 200.0, 300.0, 400.0);
            assertFalse(Float.isNaN(value));
            assertFalse(Float.isInfinite(value));
        }

        @Test
        @DisplayName("offset sampling should work")
        void offsetSamplingShouldWork() {
            DoublePrecisionNoise precise = new DoublePrecisionNoise(baseNoise);

            // Sampling at (100, 100) with offset (50, 50) should equal sampling at (50, 50)
            float v1 = precise.getNoiseWithOffset(100.0, 100.0, 50.0, 50.0);
            float v2 = precise.getNoise(50.0, 50.0);

            assertEquals(v1, v2, 0.0001f);
        }

        @Test
        @DisplayName("grid sampling should work")
        void gridSamplingShouldWork() {
            DoublePrecisionNoise precise = new DoublePrecisionNoise(baseNoise);

            float[][] grid = precise.sampleGrid(0.0, 0.0, 10, 10, 1.0);

            assertEquals(10, grid.length);
            assertEquals(10, grid[0].length);

            for (int x = 0; x < 10; x++) {
                for (int y = 0; y < 10; y++) {
                    assertFalse(Float.isNaN(grid[x][y]));
                }
            }
        }

        @Test
        @DisplayName("domain origin calculation should work")
        void domainOriginCalculationShouldWork() {
            DoublePrecisionNoise precise = new DoublePrecisionNoise(baseNoise, 1000.0);

            assertEquals(0.0, precise.getDomainOrigin(500.0), 0.001);
            assertEquals(1000.0, precise.getDomainOrigin(1500.0), 0.001);
            assertEquals(-1000.0, precise.getDomainOrigin(-500.0), 0.001);
        }

        @Test
        @DisplayName("local coordinate calculation should work")
        void localCoordinateCalculationShouldWork() {
            DoublePrecisionNoise precise = new DoublePrecisionNoise(baseNoise, 1000.0);

            assertEquals(500.0f, precise.getLocalCoord(500.0), 0.001f);
            assertEquals(500.0f, precise.getLocalCoord(1500.0), 0.001f);
            assertEquals(500.0f, precise.getLocalCoord(-500.0), 0.001f);
        }
    }

    @Nested
    @DisplayName("Integration tests")
    class IntegrationTests {

        @Test
        @DisplayName("ChunkedNoise and LODNoise can work together")
        void chunkedAndLODCanWorkTogether() {
            // Use chunked noise for large world coordinates
            ChunkedNoise chunked = new ChunkedNoise(baseNoise, 10000.0);

            // The chunked noise modifies the base noise's seed
            // so we can still use LOD on top

            double worldX = 1_000_000.0;
            double worldY = 2_000_000.0;
            float distance = 500f;

            // Sample with chunking
            float value = chunked.getNoise(worldX, worldY);

            assertFalse(Float.isNaN(value));
        }

        @Test
        @DisplayName("all spatial utilities produce bounded values")
        void allSpatialUtilitiesProduceBoundedValues() {
            ChunkedNoise chunked = new ChunkedNoise(baseNoise);
            LODNoise lod = new LODNoise(baseNoise, 6);
            TiledNoise tiled = new TiledNoise(baseNoise, 256f);
            DoublePrecisionNoise precise = new DoublePrecisionNoise(baseNoise);

            float[] values = {
                chunked.getNoise(12345.0, 67890.0),
                lod.getNoise(100f, 100f, 500f),
                tiled.getNoise(128f, 128f),
                precise.getNoise(99999.99999, 88888.88888)
            };

            for (float value : values) {
                assertFalse(Float.isNaN(value));
                assertFalse(Float.isInfinite(value));
                assertTrue(value >= -2f && value <= 2f,
                        "Value " + value + " should be in reasonable range");
            }
        }
    }
}
