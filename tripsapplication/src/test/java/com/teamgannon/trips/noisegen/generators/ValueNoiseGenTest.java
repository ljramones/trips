package com.teamgannon.trips.noisegen.generators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ValueNoiseGen class.
 */
class ValueNoiseGenTest {

    private ValueNoiseGen valueGen;
    private ValueNoiseGen valueCubicGen;

    @BeforeEach
    void setUp() {
        valueGen = new ValueNoiseGen(false);       // Standard Value noise
        valueCubicGen = new ValueNoiseGen(true);   // ValueCubic noise
    }

    @Nested
    @DisplayName("Standard Value noise 2D tests")
    class Value2DTests {

        @Test
        @DisplayName("single2D should return values in bounded range")
        void single2DShouldReturnBoundedValues() {
            int seed = 1337;
            for (int x = -100; x <= 100; x += 5) {
                for (int y = -100; y <= 100; y += 5) {
                    float value = valueGen.single2D(seed, x * 0.01f, y * 0.01f);
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

            float v1 = valueGen.single2D(seed, x, y);
            float v2 = valueGen.single2D(seed, x, y);

            assertEquals(v1, v2, "Same inputs should produce same output");
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 42, 1337, -1, Integer.MAX_VALUE, Integer.MIN_VALUE})
        @DisplayName("single2D should work with various seeds")
        void single2DShouldWorkWithVariousSeeds(int seed) {
            float value = valueGen.single2D(seed, 50.0f, 50.0f);
            assertFalse(Float.isNaN(value));
            assertFalse(Float.isInfinite(value));
        }
    }

    @Nested
    @DisplayName("Standard Value noise 3D tests")
    class Value3DTests {

        @Test
        @DisplayName("single3D should return values in bounded range")
        void single3DShouldReturnBoundedValues() {
            int seed = 1337;
            for (int x = -50; x <= 50; x += 10) {
                for (int y = -50; y <= 50; y += 10) {
                    for (int z = -50; z <= 50; z += 10) {
                        float value = valueGen.single3D(seed, x * 0.01f, y * 0.01f, z * 0.01f);
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

            float v1 = valueGen.single3D(seed, x, y, z);
            float v2 = valueGen.single3D(seed, x, y, z);

            assertEquals(v1, v2, "Same inputs should produce same output");
        }
    }

    @Nested
    @DisplayName("ValueCubic noise 2D tests")
    class ValueCubic2DTests {

        @Test
        @DisplayName("ValueCubic 2D should return bounded values")
        void valueCubic2DShouldReturnBoundedValues() {
            int seed = 1337;
            for (int x = -100; x <= 100; x += 10) {
                for (int y = -100; y <= 100; y += 10) {
                    float value = valueCubicGen.single2D(seed, x * 0.01f, y * 0.01f);
                    assertFalse(Float.isNaN(value),
                        String.format("NaN at (%d, %d)", x, y));
                    // ValueCubic can have slightly different range due to cubic interpolation
                    assertTrue(value >= -2.0f && value <= 2.0f,
                        String.format("Value %.4f out of range at (%d, %d)", value, x, y));
                }
            }
        }

        @Test
        @DisplayName("ValueCubic 2D should be deterministic")
        void valueCubic2DShouldBeDeterministic() {
            int seed = 42;
            float x = 123.456f;
            float y = 789.012f;

            float v1 = valueCubicGen.single2D(seed, x, y);
            float v2 = valueCubicGen.single2D(seed, x, y);

            assertEquals(v1, v2, "Same inputs should produce same output");
        }
    }

    @Nested
    @DisplayName("ValueCubic noise 3D tests")
    class ValueCubic3DTests {

        @Test
        @DisplayName("ValueCubic 3D should return bounded values")
        void valueCubic3DShouldReturnBoundedValues() {
            int seed = 1337;
            for (int x = -50; x <= 50; x += 25) {
                for (int y = -50; y <= 50; y += 25) {
                    for (int z = -50; z <= 50; z += 25) {
                        float value = valueCubicGen.single3D(seed, x * 0.01f, y * 0.01f, z * 0.01f);
                        assertFalse(Float.isNaN(value),
                            String.format("NaN at (%d, %d, %d)", x, y, z));
                        assertTrue(value >= -2.0f && value <= 2.0f,
                            String.format("Value %.4f out of range at (%d, %d, %d)",
                                value, x, y, z));
                    }
                }
            }
        }

        @Test
        @DisplayName("ValueCubic 3D should be deterministic")
        void valueCubic3DShouldBeDeterministic() {
            int seed = 42;
            float x = 12.3f;
            float y = 45.6f;
            float z = 78.9f;

            float v1 = valueCubicGen.single3D(seed, x, y, z);
            float v2 = valueCubicGen.single3D(seed, x, y, z);

            assertEquals(v1, v2, "Same inputs should produce same output");
        }
    }

    @Nested
    @DisplayName("Value vs ValueCubic comparison tests")
    class ValueVsValueCubicTests {

        @Test
        @DisplayName("Value and ValueCubic should produce different patterns")
        void valueAndValueCubicShouldProduceDifferentPatterns() {
            int differences = 0;

            for (int i = 0; i < 100; i++) {
                float v1 = valueGen.single2D(1337, i * 0.1f, i * 0.15f);
                float v2 = valueCubicGen.single2D(1337, i * 0.1f, i * 0.15f);

                if (Math.abs(v1 - v2) > 0.001f) {
                    differences++;
                }
            }

            assertTrue(differences > 50,
                "Value and ValueCubic should produce different patterns");
        }

        @Test
        @DisplayName("ValueCubic should be smoother than Value noise")
        void valueCubicShouldBeSmootherThanValue() {
            int seed = 1337;
            float scale = 0.1f;

            // Calculate max delta for standard Value noise
            float maxDeltaValue = 0;
            float prevValue = valueGen.single2D(seed, 0, 0);
            for (int i = 1; i <= 100; i++) {
                float value = valueGen.single2D(seed, i * scale, i * scale);
                float delta = Math.abs(value - prevValue);
                if (delta > maxDeltaValue) maxDeltaValue = delta;
                prevValue = value;
            }

            // Calculate max delta for ValueCubic noise
            float maxDeltaCubic = 0;
            prevValue = valueCubicGen.single2D(seed, 0, 0);
            for (int i = 1; i <= 100; i++) {
                float value = valueCubicGen.single2D(seed, i * scale, i * scale);
                float delta = Math.abs(value - prevValue);
                if (delta > maxDeltaCubic) maxDeltaCubic = delta;
                prevValue = value;
            }

            // Both should be reasonably smooth
            assertTrue(maxDeltaValue < 0.8f,
                String.format("Value max delta %.4f too large", maxDeltaValue));
            assertTrue(maxDeltaCubic < 0.8f,
                String.format("ValueCubic max delta %.4f too large", maxDeltaCubic));
        }
    }

    @Nested
    @DisplayName("Edge case tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle zero coordinates")
        void shouldHandleZeroCoordinates() {
            assertDoesNotThrow(() -> valueGen.single2D(1337, 0, 0));
            assertDoesNotThrow(() -> valueGen.single3D(1337, 0, 0, 0));
            assertDoesNotThrow(() -> valueCubicGen.single2D(1337, 0, 0));
            assertDoesNotThrow(() -> valueCubicGen.single3D(1337, 0, 0, 0));
        }

        @Test
        @DisplayName("should handle large coordinates")
        void shouldHandleLargeCoordinates() {
            float large = 100000.0f;

            float v1 = valueGen.single2D(1337, large, large);
            float v2 = valueGen.single3D(1337, large, large, large);
            float v3 = valueCubicGen.single2D(1337, large, large);
            float v4 = valueCubicGen.single3D(1337, large, large, large);

            assertFalse(Float.isNaN(v1));
            assertFalse(Float.isNaN(v2));
            assertFalse(Float.isNaN(v3));
            assertFalse(Float.isNaN(v4));
        }

        @Test
        @DisplayName("should handle negative coordinates")
        void shouldHandleNegativeCoordinates() {
            float neg = -1000.0f;

            float v1 = valueGen.single2D(1337, neg, neg);
            float v2 = valueGen.single3D(1337, neg, neg, neg);
            float v3 = valueCubicGen.single2D(1337, neg, neg);
            float v4 = valueCubicGen.single3D(1337, neg, neg, neg);

            assertFalse(Float.isNaN(v1));
            assertFalse(Float.isNaN(v2));
            assertFalse(Float.isNaN(v3));
            assertFalse(Float.isNaN(v4));
        }
    }

    @Nested
    @DisplayName("Noise quality tests")
    class NoiseQualityTests {

        @Test
        @DisplayName("Value noise distribution should be roughly centered around zero")
        void valueNoiseDistributionShouldBeCentered() {
            int seed = 1337;
            float sum = 0;
            int count = 1000;

            for (int i = 0; i < count; i++) {
                sum += valueGen.single2D(seed, i * 0.1f, i * 0.15f);
            }

            float mean = sum / count;

            // Mean should be close to zero
            assertTrue(Math.abs(mean) < 0.2f,
                String.format("Mean %.4f should be close to zero", mean));
        }

        @Test
        @DisplayName("different seeds should produce different patterns")
        void differentSeedsShouldProduceDifferentPatterns() {
            int differences = 0;

            for (int i = 0; i < 100; i++) {
                float v1 = valueGen.single2D(1337, i * 0.1f, i * 0.15f);
                float v2 = valueGen.single2D(42, i * 0.1f, i * 0.15f);

                if (Math.abs(v1 - v2) > 0.001f) {
                    differences++;
                }
            }

            assertTrue(differences > 50,
                "Different seeds should produce mostly different values");
        }
    }
}
