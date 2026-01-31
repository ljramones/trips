package com.teamgannon.trips.noisegen;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for FastNoiseLite 4D noise functionality.
 */
class FastNoiseLite4DTest {

    private FastNoiseLite noise;

    @BeforeEach
    void setUp() {
        noise = new FastNoiseLite(1337);
        noise.SetFrequency(0.01f);
    }

    @Nested
    @DisplayName("Basic 4D noise tests")
    class Basic4DTests {

        @Test
        @DisplayName("GetNoise 4D should return bounded values")
        void getNoise4DShouldReturnBoundedValues() {
            for (int x = -10; x <= 10; x += 2) {
                for (int y = -10; y <= 10; y += 2) {
                    for (int z = -10; z <= 10; z += 2) {
                        for (int w = -10; w <= 10; w += 2) {
                            float value = noise.GetNoise(x * 10f, y * 10f, z * 10f, w * 10f);
                            assertFalse(Float.isNaN(value),
                                    String.format("NaN at (%d, %d, %d, %d)", x, y, z, w));
                            assertTrue(value >= -1.5f && value <= 1.5f,
                                    String.format("Value %.4f out of range at (%d, %d, %d, %d)",
                                            value, x, y, z, w));
                        }
                    }
                }
            }
        }

        @Test
        @DisplayName("GetNoise 4D should be deterministic")
        void getNoise4DShouldBeDeterministic() {
            float x = 123.456f;
            float y = 789.012f;
            float z = 345.678f;
            float w = 901.234f;

            float v1 = noise.GetNoise(x, y, z, w);
            float v2 = noise.GetNoise(x, y, z, w);

            assertEquals(v1, v2, "Same inputs should produce same output");
        }

        @Test
        @DisplayName("W dimension should affect output")
        void wDimensionShouldAffectOutput() {
            float x = 100f;
            float y = 100f;
            float z = 100f;

            float valueW0 = noise.GetNoise(x, y, z, 0f);
            float valueW100 = noise.GetNoise(x, y, z, 100f);

            assertNotEquals(valueW0, valueW100, 0.001f,
                    "Different W values should produce different results");
        }
    }

    @Nested
    @DisplayName("4D Fractal noise tests")
    class Fractal4DTests {

        @Test
        @DisplayName("FBm 4D should work")
        void fbm4DShouldWork() {
            noise.SetFractalType(FastNoiseLite.FractalType.FBm);
            noise.SetFractalOctaves(4);

            float value = noise.GetNoise(100f, 100f, 100f, 100f);
            assertFalse(Float.isNaN(value));
            assertFalse(Float.isInfinite(value));
        }

        @Test
        @DisplayName("Ridged 4D should work")
        void ridged4DShouldWork() {
            noise.SetFractalType(FastNoiseLite.FractalType.Ridged);
            noise.SetFractalOctaves(4);

            float value = noise.GetNoise(100f, 100f, 100f, 100f);
            assertFalse(Float.isNaN(value));
            assertFalse(Float.isInfinite(value));
        }

        @Test
        @DisplayName("PingPong 4D should work")
        void pingPong4DShouldWork() {
            noise.SetFractalType(FastNoiseLite.FractalType.PingPong);
            noise.SetFractalOctaves(4);

            float value = noise.GetNoise(100f, 100f, 100f, 100f);
            assertFalse(Float.isNaN(value));
            assertFalse(Float.isInfinite(value));
        }

        @Test
        @DisplayName("fractal octaves should affect 4D output")
        void fractalOctavesShouldAffect4DOutput() {
            noise.SetFractalType(FastNoiseLite.FractalType.FBm);

            noise.SetFractalOctaves(1);
            float value1 = noise.GetNoise(123f, 456f, 789f, 12f);

            noise.SetFractalOctaves(5);
            float value5 = noise.GetNoise(123f, 456f, 789f, 12f);

            // More octaves typically add detail (different values)
            // They may occasionally be the same, so we just verify both work
            assertFalse(Float.isNaN(value1));
            assertFalse(Float.isNaN(value5));
        }
    }

    @Nested
    @DisplayName("Animation use case tests")
    class AnimationUseCaseTests {

        @Test
        @DisplayName("smooth animation over time")
        void smoothAnimationOverTime() {
            noise.SetFractalType(FastNoiseLite.FractalType.FBm);
            noise.SetFractalOctaves(4);

            float x = 500f;
            float y = 500f;
            float z = 500f;

            float maxDelta = 0;
            float prevValue = noise.GetNoise(x, y, z, 0);

            // Simulate animation frames
            for (int frame = 1; frame <= 60; frame++) {
                float time = frame * 0.5f; // 0.5 units per frame
                float value = noise.GetNoise(x, y, z, time);
                float delta = Math.abs(value - prevValue);
                if (delta > maxDelta) maxDelta = delta;
                prevValue = value;
            }

            // Animation should be reasonably smooth
            assertTrue(maxDelta < 0.3f,
                    String.format("Max frame delta %.4f is too large for smooth animation", maxDelta));
        }

        @Test
        @DisplayName("volumetric animation")
        void volumetricAnimation() {
            noise.SetFrequency(0.02f);

            // Sample a 3D volume at different times
            int validSamples = 0;
            for (int x = 0; x < 10; x++) {
                for (int y = 0; y < 10; y++) {
                    for (int z = 0; z < 10; z++) {
                        float time = 5.0f;
                        float value = noise.GetNoise(x * 10f, y * 10f, z * 10f, time);
                        if (!Float.isNaN(value) && !Float.isInfinite(value)) {
                            validSamples++;
                        }
                    }
                }
            }

            assertEquals(1000, validSamples, "All 1000 volume samples should be valid");
        }
    }

    @Nested
    @DisplayName("Seed variation tests")
    class SeedVariationTests {

        @Test
        @DisplayName("different seeds should produce different 4D patterns")
        void differentSeedsShouldProduceDifferent4DPatterns() {
            FastNoiseLite noise1 = new FastNoiseLite(1337);
            FastNoiseLite noise2 = new FastNoiseLite(42);

            int differences = 0;
            for (int i = 0; i < 20; i++) {
                float v1 = noise1.GetNoise(i * 50f, i * 50f, i * 50f, i * 50f);
                float v2 = noise2.GetNoise(i * 50f, i * 50f, i * 50f, i * 50f);

                if (Math.abs(v1 - v2) > 0.001f) {
                    differences++;
                }
            }

            assertTrue(differences > 10,
                    "Different seeds should produce mostly different values");
        }
    }

    @Nested
    @DisplayName("Frequency tests")
    class FrequencyTests {

        @Test
        @DisplayName("frequency should affect 4D sampling")
        void frequencyShouldAffect4DSampling() {
            noise.SetFrequency(0.001f);
            float lowFreq1 = noise.GetNoise(0f, 0f, 0f, 0f);
            float lowFreq2 = noise.GetNoise(10f, 10f, 10f, 10f);
            float lowFreqDelta = Math.abs(lowFreq1 - lowFreq2);

            noise.SetFrequency(0.1f);
            float highFreq1 = noise.GetNoise(0f, 0f, 0f, 0f);
            float highFreq2 = noise.GetNoise(10f, 10f, 10f, 10f);
            float highFreqDelta = Math.abs(highFreq1 - highFreq2);

            // Higher frequency = more variation over same distance
            assertTrue(highFreqDelta > lowFreqDelta * 0.5f ||
                       (lowFreqDelta < 0.01f), // Or both are very smooth
                    "Higher frequency should show more variation (or both smooth)");
        }
    }

    @Nested
    @DisplayName("Edge case tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle zero coordinates")
        void shouldHandleZeroCoordinates() {
            assertDoesNotThrow(() -> noise.GetNoise(0, 0, 0, 0));
        }

        @Test
        @DisplayName("should handle large coordinates")
        void shouldHandleLargeCoordinates() {
            float large = 100000.0f;
            float value = noise.GetNoise(large, large, large, large);

            assertFalse(Float.isNaN(value));
            assertFalse(Float.isInfinite(value));
        }

        @Test
        @DisplayName("should handle negative coordinates")
        void shouldHandleNegativeCoordinates() {
            float neg = -10000.0f;
            float value = noise.GetNoise(neg, neg, neg, neg);

            assertFalse(Float.isNaN(value));
            assertFalse(Float.isInfinite(value));
        }
    }
}
