package com.teamgannon.trips.noisegen;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for NoiseUtils utility class.
 */
class NoiseUtilsTest {

    @Nested
    @DisplayName("FastMin/FastMax tests")
    class FastMinMaxTests {

        @Test
        @DisplayName("FastMin should return minimum of two values")
        void fastMinShouldReturnMinimum() {
            assertEquals(1.0f, NoiseUtils.FastMin(1.0f, 2.0f), 0.0001f);
            assertEquals(1.0f, NoiseUtils.FastMin(2.0f, 1.0f), 0.0001f);
            assertEquals(-5.0f, NoiseUtils.FastMin(-5.0f, 3.0f), 0.0001f);
        }

        @Test
        @DisplayName("FastMax should return maximum of two values")
        void fastMaxShouldReturnMaximum() {
            assertEquals(2.0f, NoiseUtils.FastMax(1.0f, 2.0f), 0.0001f);
            assertEquals(2.0f, NoiseUtils.FastMax(2.0f, 1.0f), 0.0001f);
            assertEquals(3.0f, NoiseUtils.FastMax(-5.0f, 3.0f), 0.0001f);
        }

        @Test
        @DisplayName("FastMin and FastMax should handle equal values")
        void shouldHandleEqualValues() {
            assertEquals(5.0f, NoiseUtils.FastMin(5.0f, 5.0f), 0.0001f);
            assertEquals(5.0f, NoiseUtils.FastMax(5.0f, 5.0f), 0.0001f);
        }
    }

    @Nested
    @DisplayName("FastAbs tests")
    class FastAbsTests {

        @ParameterizedTest
        @CsvSource({
            "5.0, 5.0",
            "-5.0, 5.0",
            "0.0, 0.0",
            "-0.0, 0.0",
            "3.14159, 3.14159",
            "-3.14159, 3.14159"
        })
        @DisplayName("FastAbs should return absolute value")
        void fastAbsShouldReturnAbsoluteValue(float input, float expected) {
            assertEquals(expected, NoiseUtils.FastAbs(input), 0.0001f);
        }
    }

    @Nested
    @DisplayName("FastSqrt tests")
    class FastSqrtTests {

        @ParameterizedTest
        @ValueSource(floats = {0.0f, 1.0f, 4.0f, 9.0f, 16.0f, 25.0f, 100.0f})
        @DisplayName("FastSqrt should return approximate square root")
        void fastSqrtShouldReturnApproximateSquareRoot(float input) {
            float expected = (float) Math.sqrt(input);
            float actual = NoiseUtils.FastSqrt(input);

            // FastSqrt uses a fast approximation, allow some tolerance
            assertEquals(expected, actual, expected * 0.02f + 0.001f,
                String.format("FastSqrt(%.1f) = %.4f, expected ~%.4f", input, actual, expected));
        }
    }

    @Nested
    @DisplayName("FastFloor tests")
    class FastFloorTests {

        @ParameterizedTest
        @CsvSource({
            "1.5, 1",
            "1.0, 1",
            "0.9, 0",
            "0.0, 0",
            "-0.1, -1",
            "-1.0, -2",
            "-1.5, -2",
            "5.999, 5",
            "-5.001, -6"
        })
        @DisplayName("FastFloor should floor to integer (note: differs from Math.floor for negative integers)")
        void fastFloorShouldFloorToInteger(float input, int expected) {
            assertEquals(expected, NoiseUtils.FastFloor(input));
        }
    }

    @Nested
    @DisplayName("FastRound tests")
    class FastRoundTests {

        @ParameterizedTest
        @CsvSource({
            "1.4, 1",
            "1.5, 2",
            "1.6, 2",
            "0.0, 0",
            "-0.4, 0",
            "-0.5, -1",
            "-0.6, -1",
            "-1.5, -2",
            "2.5, 3"
        })
        @DisplayName("FastRound should round to nearest integer (rounds away from zero for 0.5)")
        void fastRoundShouldRoundToNearestInteger(float input, int expected) {
            assertEquals(expected, NoiseUtils.FastRound(input));
        }
    }

    @Nested
    @DisplayName("Lerp tests")
    class LerpTests {

        @Test
        @DisplayName("Lerp at t=0 should return a")
        void lerpAtZeroShouldReturnA() {
            assertEquals(1.0f, NoiseUtils.Lerp(1.0f, 5.0f, 0.0f), 0.0001f);
        }

        @Test
        @DisplayName("Lerp at t=1 should return b")
        void lerpAtOneShouldReturnB() {
            assertEquals(5.0f, NoiseUtils.Lerp(1.0f, 5.0f, 1.0f), 0.0001f);
        }

        @Test
        @DisplayName("Lerp at t=0.5 should return midpoint")
        void lerpAtHalfShouldReturnMidpoint() {
            assertEquals(3.0f, NoiseUtils.Lerp(1.0f, 5.0f, 0.5f), 0.0001f);
        }

        @Test
        @DisplayName("Lerp should handle negative values")
        void lerpShouldHandleNegativeValues() {
            assertEquals(0.0f, NoiseUtils.Lerp(-5.0f, 5.0f, 0.5f), 0.0001f);
        }
    }

    @Nested
    @DisplayName("InterpHermite tests")
    class InterpHermiteTests {

        @Test
        @DisplayName("InterpHermite at 0 should return 0")
        void interpHermiteAtZeroShouldReturnZero() {
            assertEquals(0.0f, NoiseUtils.InterpHermite(0.0f), 0.0001f);
        }

        @Test
        @DisplayName("InterpHermite at 1 should return 1")
        void interpHermiteAtOneShouldReturnOne() {
            assertEquals(1.0f, NoiseUtils.InterpHermite(1.0f), 0.0001f);
        }

        @Test
        @DisplayName("InterpHermite at 0.5 should return 0.5")
        void interpHermiteAtHalfShouldReturnHalf() {
            assertEquals(0.5f, NoiseUtils.InterpHermite(0.5f), 0.0001f);
        }

        @Test
        @DisplayName("InterpHermite curve should be smoother than linear")
        void interpHermiteShouldBeSmoother() {
            // At t=0.25, Hermite should be less than linear (0.25)
            float hermite025 = NoiseUtils.InterpHermite(0.25f);
            assertTrue(hermite025 < 0.25f, "Hermite at 0.25 should be less than 0.25");

            // At t=0.75, Hermite should be greater than linear (0.75)
            float hermite075 = NoiseUtils.InterpHermite(0.75f);
            assertTrue(hermite075 > 0.75f, "Hermite at 0.75 should be greater than 0.75");
        }
    }

    @Nested
    @DisplayName("InterpQuintic tests")
    class InterpQuinticTests {

        @Test
        @DisplayName("InterpQuintic at 0 should return 0")
        void interpQuinticAtZeroShouldReturnZero() {
            assertEquals(0.0f, NoiseUtils.InterpQuintic(0.0f), 0.0001f);
        }

        @Test
        @DisplayName("InterpQuintic at 1 should return 1")
        void interpQuinticAtOneShouldReturnOne() {
            assertEquals(1.0f, NoiseUtils.InterpQuintic(1.0f), 0.0001f);
        }

        @Test
        @DisplayName("InterpQuintic at 0.5 should return 0.5")
        void interpQuinticAtHalfShouldReturnHalf() {
            assertEquals(0.5f, NoiseUtils.InterpQuintic(0.5f), 0.0001f);
        }

        @Test
        @DisplayName("InterpQuintic should be even smoother than Hermite")
        void interpQuinticShouldBeSmoother() {
            // Quintic curve is flatter at endpoints than Hermite
            float quintic02 = NoiseUtils.InterpQuintic(0.2f);
            float hermite02 = NoiseUtils.InterpHermite(0.2f);

            // At 0.2, both should be below linear, but quintic should be smaller
            assertTrue(quintic02 < hermite02,
                String.format("Quintic (%.4f) should be less than Hermite (%.4f) at 0.2",
                    quintic02, hermite02));
        }
    }

    @Nested
    @DisplayName("CubicLerp tests")
    class CubicLerpTests {

        @Test
        @DisplayName("CubicLerp should interpolate smoothly between 4 points")
        void cubicLerpShouldInterpolateSmoothly() {
            float a = 0.0f;
            float b = 1.0f;
            float c = 1.0f;
            float d = 0.0f;

            // At t=0, should be closer to b
            float at0 = NoiseUtils.CubicLerp(a, b, c, d, 0.0f);
            // At t=1, should be closer to c
            float at1 = NoiseUtils.CubicLerp(a, b, c, d, 1.0f);

            // Values should be in reasonable range
            assertFalse(Float.isNaN(at0));
            assertFalse(Float.isNaN(at1));
        }

        @Test
        @DisplayName("CubicLerp at t=0.5 should consider all four points")
        void cubicLerpAtHalfShouldConsiderAllPoints() {
            float result = NoiseUtils.CubicLerp(0.0f, 1.0f, 2.0f, 3.0f, 0.5f);
            assertFalse(Float.isNaN(result));
            // Should be somewhere around the middle values
            assertTrue(result >= 0.0f && result <= 3.0f,
                "Result should be within the range of input values");
        }
    }

    @Nested
    @DisplayName("PingPong tests")
    class PingPongTests {

        @Test
        @DisplayName("PingPong should oscillate for positive values")
        void pingPongShouldOscillate() {
            // For positive values: t=0 -> 0, t=1 -> 1, t=2 -> 0
            assertEquals(0.0f, NoiseUtils.PingPong(0.0f), 0.0001f);
            assertEquals(0.5f, NoiseUtils.PingPong(0.5f), 0.0001f);
            assertEquals(1.0f, NoiseUtils.PingPong(1.0f), 0.0001f);
            assertEquals(0.5f, NoiseUtils.PingPong(1.5f), 0.0001f);
            assertEquals(0.0f, NoiseUtils.PingPong(2.0f), 0.0001f);
        }

        @Test
        @DisplayName("PingPong result should always be finite")
        void pingPongResultShouldAlwaysBeFinite() {
            // Note: FastNoiseLite PingPong can return values outside [0,1] for negative inputs
            for (float t = 0.0f; t <= 10.0f; t += 0.1f) {
                float result = NoiseUtils.PingPong(t);
                assertFalse(Float.isNaN(result), String.format("PingPong(%.1f) should not be NaN", t));
                assertFalse(Float.isInfinite(result), String.format("PingPong(%.1f) should not be infinite", t));
            }
        }

        @Test
        @DisplayName("PingPong for positive values should be bounded between 0 and 1")
        void pingPongForPositiveValuesShouldBeBounded() {
            for (float t = 0.0f; t <= 10.0f; t += 0.1f) {
                float result = NoiseUtils.PingPong(t);
                assertTrue(result >= 0.0f && result <= 1.0f,
                    String.format("PingPong(%.1f) = %.4f should be in [0,1]", t, result));
            }
        }
    }

    @Nested
    @DisplayName("Hash function tests")
    class HashFunctionTests {

        @Test
        @DisplayName("2D Hash should return consistent results")
        void hash2DShouldReturnConsistentResults() {
            int seed = 1337;
            int xPrimed = 123 * NoiseUtils.PrimeX;
            int yPrimed = 456 * NoiseUtils.PrimeY;

            int hash1 = NoiseUtils.Hash(seed, xPrimed, yPrimed);
            int hash2 = NoiseUtils.Hash(seed, xPrimed, yPrimed);

            assertEquals(hash1, hash2, "Hash should be deterministic");
        }

        @Test
        @DisplayName("3D Hash should return consistent results")
        void hash3DShouldReturnConsistentResults() {
            int seed = 1337;
            int xPrimed = 123 * NoiseUtils.PrimeX;
            int yPrimed = 456 * NoiseUtils.PrimeY;
            int zPrimed = 789 * NoiseUtils.PrimeZ;

            int hash1 = NoiseUtils.Hash(seed, xPrimed, yPrimed, zPrimed);
            int hash2 = NoiseUtils.Hash(seed, xPrimed, yPrimed, zPrimed);

            assertEquals(hash1, hash2, "Hash should be deterministic");
        }

        @Test
        @DisplayName("Different seeds should produce different hashes")
        void differentSeedsShouldProduceDifferentHashes() {
            int xPrimed = 123 * NoiseUtils.PrimeX;
            int yPrimed = 456 * NoiseUtils.PrimeY;

            int hash1 = NoiseUtils.Hash(1337, xPrimed, yPrimed);
            int hash2 = NoiseUtils.Hash(42, xPrimed, yPrimed);

            assertNotEquals(hash1, hash2, "Different seeds should produce different hashes");
        }

        @Test
        @DisplayName("Different coordinates should produce different hashes")
        void differentCoordinatesShouldProduceDifferentHashes() {
            int seed = 1337;

            int hash1 = NoiseUtils.Hash(seed, 100 * NoiseUtils.PrimeX, 200 * NoiseUtils.PrimeY);
            int hash2 = NoiseUtils.Hash(seed, 101 * NoiseUtils.PrimeX, 200 * NoiseUtils.PrimeY);

            assertNotEquals(hash1, hash2, "Different coordinates should produce different hashes");
        }
    }

    @Nested
    @DisplayName("ValCoord tests")
    class ValCoordTests {

        @Test
        @DisplayName("2D ValCoord should return values in [-1, 1]")
        void valCoord2DShouldReturnBoundedValues() {
            int seed = 1337;
            for (int x = -100; x <= 100; x += 10) {
                for (int y = -100; y <= 100; y += 10) {
                    int xPrimed = x * NoiseUtils.PrimeX;
                    int yPrimed = y * NoiseUtils.PrimeY;
                    float value = NoiseUtils.ValCoord(seed, xPrimed, yPrimed);
                    assertTrue(value >= -1.0f && value <= 1.0f,
                        String.format("ValCoord at (%d, %d) = %.4f should be in [-1,1]",
                            x, y, value));
                }
            }
        }

        @Test
        @DisplayName("3D ValCoord should return values in [-1, 1]")
        void valCoord3DShouldReturnBoundedValues() {
            int seed = 1337;
            for (int x = -50; x <= 50; x += 25) {
                for (int y = -50; y <= 50; y += 25) {
                    for (int z = -50; z <= 50; z += 25) {
                        int xPrimed = x * NoiseUtils.PrimeX;
                        int yPrimed = y * NoiseUtils.PrimeY;
                        int zPrimed = z * NoiseUtils.PrimeZ;
                        float value = NoiseUtils.ValCoord(seed, xPrimed, yPrimed, zPrimed);
                        assertTrue(value >= -1.0f && value <= 1.0f,
                            String.format("ValCoord at (%d, %d, %d) = %.4f should be in [-1,1]",
                                x, y, z, value));
                    }
                }
            }
        }
    }

    @Nested
    @DisplayName("GradCoord tests")
    class GradCoordTests {

        @Test
        @DisplayName("2D GradCoord should return finite values")
        void gradCoord2DShouldReturnFiniteValues() {
            int seed = 1337;
            for (int x = -10; x <= 10; x++) {
                for (int y = -10; y <= 10; y++) {
                    int xPrimed = x * NoiseUtils.PrimeX;
                    int yPrimed = y * NoiseUtils.PrimeY;
                    float value = NoiseUtils.GradCoord(seed, xPrimed, yPrimed, 0.5f, 0.5f);
                    assertFalse(Float.isNaN(value),
                        String.format("GradCoord at (%d, %d) should not be NaN", x, y));
                    assertFalse(Float.isInfinite(value),
                        String.format("GradCoord at (%d, %d) should not be infinite", x, y));
                }
            }
        }

        @Test
        @DisplayName("3D GradCoord should return finite values")
        void gradCoord3DShouldReturnFiniteValues() {
            int seed = 1337;
            for (int x = -5; x <= 5; x++) {
                for (int y = -5; y <= 5; y++) {
                    for (int z = -5; z <= 5; z++) {
                        int xPrimed = x * NoiseUtils.PrimeX;
                        int yPrimed = y * NoiseUtils.PrimeY;
                        int zPrimed = z * NoiseUtils.PrimeZ;
                        float value = NoiseUtils.GradCoord(seed, xPrimed, yPrimed, zPrimed,
                            0.5f, 0.5f, 0.5f);
                        assertFalse(Float.isNaN(value),
                            String.format("GradCoord at (%d, %d, %d) should not be NaN",
                                x, y, z));
                        assertFalse(Float.isInfinite(value),
                            String.format("GradCoord at (%d, %d, %d) should not be infinite",
                                x, y, z));
                    }
                }
            }
        }
    }

    @Nested
    @DisplayName("Prime constants tests")
    class PrimeConstantsTests {

        @Test
        @DisplayName("Prime constants should be non-zero")
        void primeConstantsShouldBeNonZero() {
            assertNotEquals(0, NoiseUtils.PrimeX);
            assertNotEquals(0, NoiseUtils.PrimeY);
            assertNotEquals(0, NoiseUtils.PrimeZ);
        }

        @Test
        @DisplayName("Prime constants should be different from each other")
        void primeConstantsShouldBeDifferent() {
            assertNotEquals(NoiseUtils.PrimeX, NoiseUtils.PrimeY);
            assertNotEquals(NoiseUtils.PrimeY, NoiseUtils.PrimeZ);
            assertNotEquals(NoiseUtils.PrimeX, NoiseUtils.PrimeZ);
        }
    }
}
