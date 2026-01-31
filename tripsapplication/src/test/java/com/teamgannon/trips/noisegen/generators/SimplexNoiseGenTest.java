package com.teamgannon.trips.noisegen.generators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SimplexNoiseGen class.
 */
class SimplexNoiseGenTest {

    private SimplexNoiseGen simplex2Gen;
    private SimplexNoiseGen simplex2SGen;

    @BeforeEach
    void setUp() {
        simplex2Gen = new SimplexNoiseGen(false);  // OpenSimplex2
        simplex2SGen = new SimplexNoiseGen(true);  // OpenSimplex2S
    }

    @Nested
    @DisplayName("OpenSimplex2 2D tests")
    class OpenSimplex2_2DTests {

        @Test
        @DisplayName("single2D should return values in bounded range")
        void single2DShouldReturnBoundedValues() {
            int seed = 1337;
            for (int x = -100; x <= 100; x += 5) {
                for (int y = -100; y <= 100; y += 5) {
                    float value = simplex2Gen.single2D(seed, x * 0.01f, y * 0.01f);
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

            float v1 = simplex2Gen.single2D(seed, x, y);
            float v2 = simplex2Gen.single2D(seed, x, y);

            assertEquals(v1, v2, "Same inputs should produce same output");
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 42, 1337, -1, Integer.MAX_VALUE, Integer.MIN_VALUE})
        @DisplayName("single2D should work with various seeds")
        void single2DShouldWorkWithVariousSeeds(int seed) {
            float value = simplex2Gen.single2D(seed, 50.0f, 50.0f);
            assertFalse(Float.isNaN(value));
            assertFalse(Float.isInfinite(value));
        }

        @Test
        @DisplayName("different positions should generally produce different values")
        void differentPositionsShouldProduceDifferentValues() {
            int seed = 1337;
            float[] values = new float[100];

            for (int i = 0; i < 100; i++) {
                values[i] = simplex2Gen.single2D(seed, i * 0.1f, i * 0.15f);
            }

            // Count unique values (with tolerance)
            int uniqueCount = 0;
            for (int i = 0; i < 100; i++) {
                boolean unique = true;
                for (int j = 0; j < i; j++) {
                    if (Math.abs(values[i] - values[j]) < 0.0001f) {
                        unique = false;
                        break;
                    }
                }
                if (unique) uniqueCount++;
            }

            assertTrue(uniqueCount > 50, "Most positions should produce unique values");
        }
    }

    @Nested
    @DisplayName("OpenSimplex2 3D tests")
    class OpenSimplex2_3DTests {

        @Test
        @DisplayName("single3D should return values in bounded range")
        void single3DShouldReturnBoundedValues() {
            int seed = 1337;
            for (int x = -50; x <= 50; x += 10) {
                for (int y = -50; y <= 50; y += 10) {
                    for (int z = -50; z <= 50; z += 10) {
                        float value = simplex2Gen.single3D(seed, x * 0.01f, y * 0.01f, z * 0.01f);
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

            float v1 = simplex2Gen.single3D(seed, x, y, z);
            float v2 = simplex2Gen.single3D(seed, x, y, z);

            assertEquals(v1, v2, "Same inputs should produce same output");
        }

        @Test
        @DisplayName("3D should produce different results than 2D at same x,y")
        void threeDShouldDifferFromTwoD() {
            int seed = 1337;
            // Use non-integer coordinates to ensure non-zero noise values
            float x = 12.34f;
            float y = 56.78f;

            float value2D = simplex2Gen.single2D(seed, x, y);
            float value3D = simplex2Gen.single3D(seed, x, y, 0.5f);

            // 3D at z=0.5 should differ from 2D (different algorithms and z influence)
            assertNotEquals(value2D, value3D, 0.0001f,
                "3D and 2D should produce different values");
        }
    }

    @Nested
    @DisplayName("OpenSimplex2S tests")
    class OpenSimplex2STests {

        @Test
        @DisplayName("OpenSimplex2S 2D should return bounded values")
        void simplex2S_2DShouldReturnBoundedValues() {
            int seed = 1337;
            for (int x = -100; x <= 100; x += 10) {
                for (int y = -100; y <= 100; y += 10) {
                    float value = simplex2SGen.single2D(seed, x * 0.01f, y * 0.01f);
                    assertFalse(Float.isNaN(value),
                        String.format("NaN at (%d, %d)", x, y));
                    assertTrue(value >= -1.5f && value <= 1.5f,
                        String.format("Value %.4f out of range at (%d, %d)", value, x, y));
                }
            }
        }

        @Test
        @DisplayName("OpenSimplex2S 3D should return bounded values")
        void simplex2S_3DShouldReturnBoundedValues() {
            int seed = 1337;
            for (int x = -50; x <= 50; x += 25) {
                for (int y = -50; y <= 50; y += 25) {
                    for (int z = -50; z <= 50; z += 25) {
                        float value = simplex2SGen.single3D(seed, x * 0.01f, y * 0.01f, z * 0.01f);
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
        @DisplayName("OpenSimplex2S should produce different pattern than OpenSimplex2")
        void simplex2SShouldDifferFromSimplex2() {
            int seed = 1337;
            int differences = 0;

            for (int i = 0; i < 100; i++) {
                float x = i * 0.1f;
                float y = i * 0.15f;

                float v1 = simplex2Gen.single2D(seed, x, y);
                float v2 = simplex2SGen.single2D(seed, x, y);

                if (Math.abs(v1 - v2) > 0.001f) {
                    differences++;
                }
            }

            assertTrue(differences > 50, "OpenSimplex2 and OpenSimplex2S should differ");
        }
    }

    @Nested
    @DisplayName("Edge case tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle zero coordinates")
        void shouldHandleZeroCoordinates() {
            assertDoesNotThrow(() -> simplex2Gen.single2D(1337, 0, 0));
            assertDoesNotThrow(() -> simplex2Gen.single3D(1337, 0, 0, 0));
            assertDoesNotThrow(() -> simplex2SGen.single2D(1337, 0, 0));
            assertDoesNotThrow(() -> simplex2SGen.single3D(1337, 0, 0, 0));
        }

        @Test
        @DisplayName("should handle very large coordinates")
        void shouldHandleVeryLargeCoordinates() {
            float large = 1000000.0f;
            float value2D = simplex2Gen.single2D(1337, large, large);
            float value3D = simplex2Gen.single3D(1337, large, large, large);

            assertFalse(Float.isNaN(value2D));
            assertFalse(Float.isInfinite(value2D));
            assertFalse(Float.isNaN(value3D));
            assertFalse(Float.isInfinite(value3D));
        }

        @Test
        @DisplayName("should handle negative coordinates")
        void shouldHandleNegativeCoordinates() {
            float neg = -500.0f;
            float value2D = simplex2Gen.single2D(1337, neg, neg);
            float value3D = simplex2Gen.single3D(1337, neg, neg, neg);

            assertFalse(Float.isNaN(value2D));
            assertFalse(Float.isInfinite(value2D));
            assertFalse(Float.isNaN(value3D));
            assertFalse(Float.isInfinite(value3D));
        }

        @Test
        @DisplayName("should handle very small coordinates")
        void shouldHandleVerySmallCoordinates() {
            float tiny = 0.0001f;
            float value2D = simplex2Gen.single2D(1337, tiny, tiny);
            float value3D = simplex2Gen.single3D(1337, tiny, tiny, tiny);

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
        @DisplayName("noise should have smooth transitions")
        void noiseShouldHaveSmoothTransitions() {
            int seed = 1337;
            float scale = 0.1f;

            // Sample along a line and check for smooth transitions
            float maxDelta = 0;
            float prevValue = simplex2Gen.single2D(seed, 0, 0);

            for (int i = 1; i <= 100; i++) {
                float value = simplex2Gen.single2D(seed, i * scale, i * scale);
                float delta = Math.abs(value - prevValue);
                if (delta > maxDelta) maxDelta = delta;
                prevValue = value;
            }

            // For smooth noise, adjacent samples shouldn't differ by more than ~0.5
            // (depending on scale)
            assertTrue(maxDelta < 0.6f,
                String.format("Max delta %.4f is too large for smooth noise", maxDelta));
        }

        @Test
        @DisplayName("noise distribution should be roughly centered around zero")
        void noiseDistributionShouldBeCentered() {
            int seed = 1337;
            float sum = 0;
            int count = 1000;

            for (int i = 0; i < count; i++) {
                sum += simplex2Gen.single2D(seed, i * 0.1f, i * 0.15f);
            }

            float mean = sum / count;

            // Mean should be close to zero (within 0.1)
            assertTrue(Math.abs(mean) < 0.15f,
                String.format("Mean %.4f should be close to zero", mean));
        }
    }
}
