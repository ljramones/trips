package com.teamgannon.trips.noisegen.generators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Simplex4DNoiseGen class.
 */
class Simplex4DNoiseGenTest {

    private Simplex4DNoiseGen simplex4D;

    @BeforeEach
    void setUp() {
        simplex4D = new Simplex4DNoiseGen();
    }

    @Nested
    @DisplayName("4D Simplex noise tests")
    class Simplex4DTests {

        @Test
        @DisplayName("single4D should return values in bounded range")
        void single4DShouldReturnBoundedValues() {
            int seed = 1337;
            for (int x = -20; x <= 20; x += 5) {
                for (int y = -20; y <= 20; y += 5) {
                    for (int z = -20; z <= 20; z += 5) {
                        for (int w = -20; w <= 20; w += 5) {
                            float value = simplex4D.single4D(seed,
                                    x * 0.1f, y * 0.1f, z * 0.1f, w * 0.1f);
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
        @DisplayName("single4D should be deterministic")
        void single4DShouldBeDeterministic() {
            int seed = 42;
            float x = 12.34f;
            float y = 56.78f;
            float z = 90.12f;
            float w = 34.56f;

            float v1 = simplex4D.single4D(seed, x, y, z, w);
            float v2 = simplex4D.single4D(seed, x, y, z, w);

            assertEquals(v1, v2, "Same inputs should produce same output");
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 42, 1337, -1, Integer.MAX_VALUE, Integer.MIN_VALUE})
        @DisplayName("single4D should work with various seeds")
        void single4DShouldWorkWithVariousSeeds(int seed) {
            float value = simplex4D.single4D(seed, 50.0f, 50.0f, 50.0f, 50.0f);
            assertFalse(Float.isNaN(value));
            assertFalse(Float.isInfinite(value));
        }

        @Test
        @DisplayName("W dimension should affect output")
        void wDimensionShouldAffectOutput() {
            int seed = 1337;
            float x = 12.34f;
            float y = 56.78f;
            float z = 90.12f;

            float valueW0 = simplex4D.single4D(seed, x, y, z, 0.25f);
            float valueW1 = simplex4D.single4D(seed, x, y, z, 1.75f);

            assertNotEquals(valueW0, valueW1, 0.001f,
                    "Different W values should produce different results");
        }

        @Test
        @DisplayName("noise should have smooth transitions in 4D")
        void noiseShouldHaveSmoothTransitions4D() {
            int seed = 1337;
            float scale = 0.1f;

            float maxDelta = 0;
            float prevValue = simplex4D.single4D(seed, 0, 0, 0, 0);

            for (int i = 1; i <= 30; i++) {
                float value = simplex4D.single4D(seed,
                        i * scale, i * scale, i * scale, i * scale);
                float delta = Math.abs(value - prevValue);
                if (delta > maxDelta) maxDelta = delta;
                prevValue = value;
            }

            assertTrue(maxDelta < 0.6f,
                    String.format("Max delta %.4f is too large for smooth 4D noise", maxDelta));
        }
    }

    @Nested
    @DisplayName("Different seeds tests")
    class DifferentSeedsTests {

        @Test
        @DisplayName("different seeds should produce different 4D patterns")
        void differentSeedsShouldProduceDifferent4DPatterns() {
            int differences = 0;

            for (int i = 0; i < 30; i++) {
                float v1 = simplex4D.single4D(1337, i * 0.1f, i * 0.15f, i * 0.2f, i * 0.25f);
                float v2 = simplex4D.single4D(42, i * 0.1f, i * 0.15f, i * 0.2f, i * 0.25f);

                if (Math.abs(v1 - v2) > 0.001f) {
                    differences++;
                }
            }

            assertTrue(differences > 15,
                    "Different seeds should produce mostly different values in 4D");
        }
    }

    @Nested
    @DisplayName("Edge case tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle zero coordinates")
        void shouldHandleZeroCoordinates() {
            assertDoesNotThrow(() -> simplex4D.single4D(1337, 0, 0, 0, 0));
        }

        @Test
        @DisplayName("should handle large coordinates")
        void shouldHandleLargeCoordinates() {
            float large = 10000.0f;
            float value = simplex4D.single4D(1337, large, large, large, large);

            assertFalse(Float.isNaN(value));
            assertFalse(Float.isInfinite(value));
        }

        @Test
        @DisplayName("should handle negative coordinates")
        void shouldHandleNegativeCoordinates() {
            float neg = -1000.0f;
            float value = simplex4D.single4D(1337, neg, neg, neg, neg);

            assertFalse(Float.isNaN(value));
            assertFalse(Float.isInfinite(value));
        }

        @Test
        @DisplayName("should handle very small fractional coordinates")
        void shouldHandleVerySmallFractionalCoordinates() {
            float tiny = 0.00001f;
            float value = simplex4D.single4D(1337, tiny, tiny, tiny, tiny);

            assertFalse(Float.isNaN(value));
            assertFalse(Float.isInfinite(value));
        }
    }

    @Nested
    @DisplayName("Interface compliance tests")
    class InterfaceComplianceTests {

        @Test
        @DisplayName("supports4D should return true")
        void supports4DShouldReturnTrue() {
            assertTrue(simplex4D.supports4D());
        }

        @Test
        @DisplayName("single2D should work as 4D projection")
        void single2DShouldWorkAs4DProjection() {
            float value = simplex4D.single2D(1337, 12.34f, 56.78f);
            assertFalse(Float.isNaN(value));
            assertFalse(Float.isInfinite(value));
        }

        @Test
        @DisplayName("single3D should work as 4D projection")
        void single3DShouldWorkAs4DProjection() {
            float value = simplex4D.single3D(1337, 12.34f, 56.78f, 90.12f);
            assertFalse(Float.isNaN(value));
            assertFalse(Float.isInfinite(value));
        }
    }

    @Nested
    @DisplayName("Animation use case tests")
    class AnimationUseCaseTests {

        @Test
        @DisplayName("W as time should create smooth animation")
        void wAsTimeShouldCreateSmoothAnimation() {
            int seed = 1337;
            float x = 5.5f;
            float y = 7.3f;
            float z = 2.1f;

            float maxDelta = 0;
            float prevValue = simplex4D.single4D(seed, x, y, z, 0);

            // Simulate time progression
            for (int t = 1; t <= 100; t++) {
                float time = t * 0.05f;
                float value = simplex4D.single4D(seed, x, y, z, time);
                float delta = Math.abs(value - prevValue);
                if (delta > maxDelta) maxDelta = delta;
                prevValue = value;
            }

            // Animation should be smooth
            assertTrue(maxDelta < 0.3f,
                    String.format("Max time delta %.4f is too large for smooth animation", maxDelta));
        }

        @Test
        @DisplayName("looping animation using circular path in W plane")
        void loopingAnimationUsingCircularPath() {
            int seed = 1337;
            float x = 5.5f;
            float y = 7.3f;
            float z = 2.1f;
            float radius = 2.0f;

            // Get values at start and end of a loop
            float startW = (float) Math.sin(0) * radius;
            float endW = (float) Math.sin(Math.PI * 2) * radius;

            float startValue = simplex4D.single4D(seed, x, y, z, startW);
            float endValue = simplex4D.single4D(seed, x, y, z, endW);

            // Start and end should be very close (loop complete)
            assertEquals(startValue, endValue, 0.01f,
                    "Looping animation should return to starting value");
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
            int count = 500;

            for (int i = 0; i < count; i++) {
                sum += simplex4D.single4D(seed,
                        i * 0.1f, i * 0.15f, i * 0.2f, i * 0.25f);
            }

            float mean = sum / count;

            // Mean should be close to zero
            assertTrue(Math.abs(mean) < 0.2f,
                    String.format("Mean %.4f should be close to zero", mean));
        }

        @Test
        @DisplayName("noise should use full range")
        void noiseShouldUseFullRange() {
            int seed = 1337;
            float min = Float.MAX_VALUE;
            float max = Float.MIN_VALUE;

            for (int i = 0; i < 500; i++) {
                float value = simplex4D.single4D(seed,
                        i * 0.1f, i * 0.15f, i * 0.2f, i * 0.25f);
                if (value < min) min = value;
                if (value > max) max = value;
            }

            // Should use significant portion of [-1, 1] range
            assertTrue(min < -0.3f, String.format("Min %.4f should be well negative", min));
            assertTrue(max > 0.3f, String.format("Max %.4f should be well positive", max));
        }
    }
}
