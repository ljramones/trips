package com.teamgannon.trips.noisegen.generators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PerlinNoiseGen class.
 */
class PerlinNoiseGenTest {

    private PerlinNoiseGen perlinGen;

    @BeforeEach
    void setUp() {
        perlinGen = new PerlinNoiseGen();
    }

    @Nested
    @DisplayName("2D Perlin noise tests")
    class Perlin2DTests {

        @Test
        @DisplayName("single2D should return values in bounded range")
        void single2DShouldReturnBoundedValues() {
            int seed = 1337;
            for (int x = -100; x <= 100; x += 5) {
                for (int y = -100; y <= 100; y += 5) {
                    float value = perlinGen.single2D(seed, x * 0.01f, y * 0.01f);
                    assertFalse(Float.isNaN(value),
                        String.format("NaN at (%d, %d)", x, y));
                    assertTrue(value >= -1.5f && value <= 1.5f,
                        String.format("Value %.4f out of range at (%d, %d)", value, x, y));
                }
            }
        }

        @Test
        @DisplayName("single2D should be deterministic")
        void single2DShouldBeDeterministic() {
            int seed = 42;
            float x = 123.456f;
            float y = 789.012f;

            float v1 = perlinGen.single2D(seed, x, y);
            float v2 = perlinGen.single2D(seed, x, y);

            assertEquals(v1, v2, "Same inputs should produce same output");
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 42, 1337, -1, Integer.MAX_VALUE, Integer.MIN_VALUE})
        @DisplayName("single2D should work with various seeds")
        void single2DShouldWorkWithVariousSeeds(int seed) {
            float value = perlinGen.single2D(seed, 50.0f, 50.0f);
            assertFalse(Float.isNaN(value));
            assertFalse(Float.isInfinite(value));
        }

        @Test
        @DisplayName("noise should have smooth transitions in 2D")
        void noiseShouldHaveSmoothTransitions2D() {
            int seed = 1337;
            float scale = 0.1f;

            float maxDelta = 0;
            float prevValue = perlinGen.single2D(seed, 0, 0);

            for (int i = 1; i <= 100; i++) {
                float value = perlinGen.single2D(seed, i * scale, i * scale);
                float delta = Math.abs(value - prevValue);
                if (delta > maxDelta) maxDelta = delta;
                prevValue = value;
            }

            // Perlin noise should be smooth
            assertTrue(maxDelta < 0.5f,
                String.format("Max delta %.4f is too large for smooth noise", maxDelta));
        }

        @Test
        @DisplayName("noise at integer coordinates should be zero or near zero")
        void noiseAtIntegerCoordinatesShouldBeNearZero() {
            // Classic Perlin property: gradient dot product at integer is 0
            // This might not hold for all implementations, but let's check
            int seed = 1337;

            // Test several integer positions
            for (int x = 0; x < 10; x++) {
                for (int y = 0; y < 10; y++) {
                    float value = perlinGen.single2D(seed, (float) x, (float) y);
                    // Values at lattice points should be small (close to 0)
                    assertTrue(Math.abs(value) < 0.5f,
                        String.format("Value at integer (%d, %d) = %.4f should be small",
                            x, y, value));
                }
            }
        }
    }

    @Nested
    @DisplayName("3D Perlin noise tests")
    class Perlin3DTests {

        @Test
        @DisplayName("single3D should return values in bounded range")
        void single3DShouldReturnBoundedValues() {
            int seed = 1337;
            for (int x = -50; x <= 50; x += 10) {
                for (int y = -50; y <= 50; y += 10) {
                    for (int z = -50; z <= 50; z += 10) {
                        float value = perlinGen.single3D(seed, x * 0.01f, y * 0.01f, z * 0.01f);
                        assertFalse(Float.isNaN(value),
                            String.format("NaN at (%d, %d, %d)", x, y, z));
                        assertTrue(value >= -1.5f && value <= 1.5f,
                            String.format("Value %.4f out of range at (%d, %d, %d)",
                                value, x, y, z));
                    }
                }
            }
        }

        @Test
        @DisplayName("single3D should be deterministic")
        void single3DShouldBeDeterministic() {
            int seed = 42;
            float x = 12.3f;
            float y = 45.6f;
            float z = 78.9f;

            float v1 = perlinGen.single3D(seed, x, y, z);
            float v2 = perlinGen.single3D(seed, x, y, z);

            assertEquals(v1, v2, "Same inputs should produce same output");
        }

        @Test
        @DisplayName("3D noise should extend 2D patterns")
        void threeDNoiseShouldExtend2DPatterns() {
            int seed = 1337;
            // Use non-integer coordinates for more meaningful comparison
            float x = 12.34f;
            float y = 56.78f;

            // Varying z should change the 2D slice
            float valueZ0 = perlinGen.single3D(seed, x, y, 0.25f);
            float valueZ1 = perlinGen.single3D(seed, x, y, 1.75f);

            assertNotEquals(valueZ0, valueZ1, 0.001f,
                "Different Z values should produce different results");
        }

        @Test
        @DisplayName("noise should have smooth transitions in 3D")
        void noiseShouldHaveSmoothTransitions3D() {
            int seed = 1337;
            float scale = 0.1f;

            float maxDelta = 0;
            float prevValue = perlinGen.single3D(seed, 0, 0, 0);

            for (int i = 1; i <= 50; i++) {
                float value = perlinGen.single3D(seed, i * scale, i * scale, i * scale);
                float delta = Math.abs(value - prevValue);
                if (delta > maxDelta) maxDelta = delta;
                prevValue = value;
            }

            assertTrue(maxDelta < 0.5f,
                String.format("Max delta %.4f is too large for smooth 3D noise", maxDelta));
        }
    }

    @Nested
    @DisplayName("Different seeds tests")
    class DifferentSeedsTests {

        @Test
        @DisplayName("different seeds should produce different 2D patterns")
        void differentSeedsShouldProduceDifferent2DPatterns() {
            int differences = 0;

            for (int i = 0; i < 100; i++) {
                float v1 = perlinGen.single2D(1337, i * 0.1f, i * 0.15f);
                float v2 = perlinGen.single2D(42, i * 0.1f, i * 0.15f);

                if (Math.abs(v1 - v2) > 0.001f) {
                    differences++;
                }
            }

            assertTrue(differences > 50,
                "Different seeds should produce mostly different values");
        }

        @Test
        @DisplayName("different seeds should produce different 3D patterns")
        void differentSeedsShouldProduceDifferent3DPatterns() {
            int differences = 0;

            for (int i = 0; i < 50; i++) {
                float v1 = perlinGen.single3D(1337, i * 0.1f, i * 0.15f, i * 0.2f);
                float v2 = perlinGen.single3D(42, i * 0.1f, i * 0.15f, i * 0.2f);

                if (Math.abs(v1 - v2) > 0.001f) {
                    differences++;
                }
            }

            assertTrue(differences > 25,
                "Different seeds should produce mostly different values in 3D");
        }
    }

    @Nested
    @DisplayName("Edge case tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle zero coordinates")
        void shouldHandleZeroCoordinates() {
            assertDoesNotThrow(() -> perlinGen.single2D(1337, 0, 0));
            assertDoesNotThrow(() -> perlinGen.single3D(1337, 0, 0, 0));
        }

        @Test
        @DisplayName("should handle large coordinates")
        void shouldHandleLargeCoordinates() {
            float large = 100000.0f;
            float value2D = perlinGen.single2D(1337, large, large);
            float value3D = perlinGen.single3D(1337, large, large, large);

            assertFalse(Float.isNaN(value2D));
            assertFalse(Float.isInfinite(value2D));
            assertFalse(Float.isNaN(value3D));
            assertFalse(Float.isInfinite(value3D));
        }

        @Test
        @DisplayName("should handle negative coordinates")
        void shouldHandleNegativeCoordinates() {
            float neg = -1000.0f;
            float value2D = perlinGen.single2D(1337, neg, neg);
            float value3D = perlinGen.single3D(1337, neg, neg, neg);

            assertFalse(Float.isNaN(value2D));
            assertFalse(Float.isInfinite(value2D));
            assertFalse(Float.isNaN(value3D));
            assertFalse(Float.isInfinite(value3D));
        }

        @Test
        @DisplayName("should handle very small fractional coordinates")
        void shouldHandleVerySmallFractionalCoordinates() {
            float tiny = 0.00001f;
            float value2D = perlinGen.single2D(1337, tiny, tiny);
            float value3D = perlinGen.single3D(1337, tiny, tiny, tiny);

            assertFalse(Float.isNaN(value2D));
            assertFalse(Float.isInfinite(value2D));
            assertFalse(Float.isNaN(value3D));
            assertFalse(Float.isInfinite(value3D));
        }
    }

    @Nested
    @DisplayName("Noise quality tests")
    class NoiseQualityTests {

        @Test
        @DisplayName("noise distribution should be roughly centered around zero")
        void noiseDistributionShouldBeCentered() {
            int seed = 1337;
            float sum = 0;
            int count = 1000;

            for (int i = 0; i < count; i++) {
                sum += perlinGen.single2D(seed, i * 0.1f, i * 0.15f);
            }

            float mean = sum / count;

            // Mean should be close to zero
            assertTrue(Math.abs(mean) < 0.15f,
                String.format("Mean %.4f should be close to zero", mean));
        }

        @Test
        @DisplayName("noise should use full range")
        void noiseShouldUseFullRange() {
            int seed = 1337;
            float min = Float.MAX_VALUE;
            float max = Float.MIN_VALUE;

            for (int i = 0; i < 1000; i++) {
                float value = perlinGen.single2D(seed, i * 0.1f, i * 0.15f);
                if (value < min) min = value;
                if (value > max) max = value;
            }

            // Should use significant portion of [-1, 1] range
            assertTrue(min < -0.3f, String.format("Min %.4f should be well negative", min));
            assertTrue(max > 0.3f, String.format("Max %.4f should be well positive", max));
        }
    }
}
