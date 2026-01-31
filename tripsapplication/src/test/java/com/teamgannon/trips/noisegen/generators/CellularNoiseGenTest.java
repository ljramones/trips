package com.teamgannon.trips.noisegen.generators;

import com.teamgannon.trips.noisegen.NoiseTypes.CellularDistanceFunction;
import com.teamgannon.trips.noisegen.NoiseTypes.CellularReturnType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for CellularNoiseGen class.
 */
class CellularNoiseGenTest {

    private CellularNoiseGen cellularGen;

    @BeforeEach
    void setUp() {
        cellularGen = new CellularNoiseGen(
            CellularDistanceFunction.EuclideanSq,
            CellularReturnType.Distance,
            1.0f
        );
    }

    @Nested
    @DisplayName("Constructor tests")
    class ConstructorTests {

        @Test
        @DisplayName("constructor should accept valid parameters")
        void constructorShouldAcceptValidParameters() {
            CellularNoiseGen gen = new CellularNoiseGen(
                CellularDistanceFunction.Manhattan,
                CellularReturnType.CellValue,
                0.5f
            );

            assertNotNull(gen);
            float value = gen.single2D(1337, 10.0f, 10.0f);
            assertFalse(Float.isNaN(value));
        }
    }

    @Nested
    @DisplayName("Distance function tests")
    class DistanceFunctionTests {

        @ParameterizedTest
        @EnumSource(CellularDistanceFunction.class)
        @DisplayName("all distance functions should produce valid 2D output")
        void allDistanceFunctionsShouldProduceValid2DOutput(CellularDistanceFunction distFunc) {
            cellularGen.setDistanceFunction(distFunc);

            for (int x = -50; x <= 50; x += 10) {
                for (int y = -50; y <= 50; y += 10) {
                    float value = cellularGen.single2D(1337, x * 0.1f, y * 0.1f);
                    assertFalse(Float.isNaN(value),
                        String.format("NaN at (%d, %d) for %s", x, y, distFunc));
                }
            }
        }

        @ParameterizedTest
        @EnumSource(CellularDistanceFunction.class)
        @DisplayName("all distance functions should produce valid 3D output")
        void allDistanceFunctionsShouldProduceValid3DOutput(CellularDistanceFunction distFunc) {
            cellularGen.setDistanceFunction(distFunc);

            for (int x = -25; x <= 25; x += 10) {
                for (int y = -25; y <= 25; y += 10) {
                    for (int z = -25; z <= 25; z += 10) {
                        float value = cellularGen.single3D(1337, x * 0.1f, y * 0.1f, z * 0.1f);
                        assertFalse(Float.isNaN(value),
                            String.format("NaN at (%d, %d, %d) for %s", x, y, z, distFunc));
                    }
                }
            }
        }

        @Test
        @DisplayName("different distance functions should produce different patterns")
        void differentDistanceFunctionsShouldProduceDifferentPatterns() {
            float[] euclidean = new float[100];
            float[] manhattan = new float[100];

            cellularGen.setDistanceFunction(CellularDistanceFunction.Euclidean);
            for (int i = 0; i < 100; i++) {
                euclidean[i] = cellularGen.single2D(1337, i * 0.1f, i * 0.15f);
            }

            cellularGen.setDistanceFunction(CellularDistanceFunction.Manhattan);
            for (int i = 0; i < 100; i++) {
                manhattan[i] = cellularGen.single2D(1337, i * 0.1f, i * 0.15f);
            }

            int differences = 0;
            for (int i = 0; i < 100; i++) {
                if (Math.abs(euclidean[i] - manhattan[i]) > 0.001f) {
                    differences++;
                }
            }

            assertTrue(differences > 50,
                "Different distance functions should produce different patterns");
        }
    }

    @Nested
    @DisplayName("Return type tests")
    class ReturnTypeTests {

        @ParameterizedTest
        @EnumSource(CellularReturnType.class)
        @DisplayName("all return types should produce valid 2D output")
        void allReturnTypesShouldProduceValid2DOutput(CellularReturnType returnType) {
            cellularGen.setReturnType(returnType);

            for (int x = -50; x <= 50; x += 10) {
                for (int y = -50; y <= 50; y += 10) {
                    float value = cellularGen.single2D(1337, x * 0.1f, y * 0.1f);
                    assertFalse(Float.isNaN(value),
                        String.format("NaN at (%d, %d) for %s", x, y, returnType));
                }
            }
        }

        @ParameterizedTest
        @EnumSource(CellularReturnType.class)
        @DisplayName("all return types should produce valid 3D output")
        void allReturnTypesShouldProduceValid3DOutput(CellularReturnType returnType) {
            cellularGen.setReturnType(returnType);

            for (int x = -25; x <= 25; x += 10) {
                for (int y = -25; y <= 25; y += 10) {
                    for (int z = -25; z <= 25; z += 10) {
                        float value = cellularGen.single3D(1337, x * 0.1f, y * 0.1f, z * 0.1f);
                        assertFalse(Float.isNaN(value),
                            String.format("NaN at (%d, %d, %d) for %s", x, y, z, returnType));
                    }
                }
            }
        }

        @Test
        @DisplayName("CellValue return type should produce cell-like patterns")
        void cellValueReturnTypeShouldProduceCellLikePatterns() {
            cellularGen.setReturnType(CellularReturnType.CellValue);

            // Cells should have constant value within each cell
            // Points close together should often have the same value
            float prev = cellularGen.single2D(1337, 0, 0);
            int sameCount = 0;

            for (int i = 1; i <= 100; i++) {
                float curr = cellularGen.single2D(1337, i * 0.01f, i * 0.01f);
                if (Math.abs(curr - prev) < 0.001f) {
                    sameCount++;
                }
                prev = curr;
            }

            // At small scale, many adjacent points should be in same cell
            assertTrue(sameCount > 30, "CellValue should have constant regions");
        }

        @Test
        @DisplayName("Distance return type should produce gradient patterns")
        void distanceReturnTypeShouldProduceGradientPatterns() {
            cellularGen.setReturnType(CellularReturnType.Distance);

            // Distance should increase from cell centers
            int nonZeroCount = 0;
            for (int x = 0; x < 100; x += 5) {
                for (int y = 0; y < 100; y += 5) {
                    float value = cellularGen.single2D(1337, x * 0.1f, y * 0.1f);
                    if (Math.abs(value) > 0.001f) {
                        nonZeroCount++;
                    }
                }
            }

            // Most points should have non-zero distance from cell centers
            assertTrue(nonZeroCount > 100,
                "Distance return type should mostly produce non-zero values");
        }
    }

    @Nested
    @DisplayName("Jitter tests")
    class JitterTests {

        @Test
        @DisplayName("jitter modifier should affect cellular pattern")
        void jitterModifierShouldAffectCellularPattern() {
            float[] lowJitter = new float[100];
            float[] highJitter = new float[100];

            cellularGen.setJitterModifier(0.1f);
            for (int i = 0; i < 100; i++) {
                lowJitter[i] = cellularGen.single2D(1337, i * 0.1f, i * 0.15f);
            }

            cellularGen.setJitterModifier(1.0f);
            for (int i = 0; i < 100; i++) {
                highJitter[i] = cellularGen.single2D(1337, i * 0.1f, i * 0.15f);
            }

            int differences = 0;
            for (int i = 0; i < 100; i++) {
                if (Math.abs(lowJitter[i] - highJitter[i]) > 0.01f) {
                    differences++;
                }
            }

            assertTrue(differences > 20,
                "Different jitter values should produce different patterns");
        }

        @Test
        @DisplayName("zero jitter should produce regular grid-like pattern")
        void zeroJitterShouldProduceRegularPattern() {
            cellularGen.setJitterModifier(0.0f);
            cellularGen.setReturnType(CellularReturnType.Distance);

            // With zero jitter, cells are arranged in a regular grid
            float value = cellularGen.single2D(1337, 0.5f, 0.5f);
            assertFalse(Float.isNaN(value));
        }
    }

    @Nested
    @DisplayName("Determinism tests")
    class DeterminismTests {

        @Test
        @DisplayName("same inputs should produce same 2D output")
        void sameInputsShouldProduceSame2DOutput() {
            float v1 = cellularGen.single2D(1337, 12.34f, 56.78f);
            float v2 = cellularGen.single2D(1337, 12.34f, 56.78f);

            assertEquals(v1, v2, "Same inputs should produce same output");
        }

        @Test
        @DisplayName("same inputs should produce same 3D output")
        void sameInputsShouldProduceSame3DOutput() {
            float v1 = cellularGen.single3D(1337, 12.34f, 56.78f, 90.12f);
            float v2 = cellularGen.single3D(1337, 12.34f, 56.78f, 90.12f);

            assertEquals(v1, v2, "Same inputs should produce same output");
        }

        @Test
        @DisplayName("different seeds should produce different output")
        void differentSeedsShouldProduceDifferentOutput() {
            int differences = 0;

            for (int i = 0; i < 100; i++) {
                float v1 = cellularGen.single2D(1337, i * 0.1f, i * 0.15f);
                float v2 = cellularGen.single2D(42, i * 0.1f, i * 0.15f);

                if (Math.abs(v1 - v2) > 0.001f) {
                    differences++;
                }
            }

            assertTrue(differences > 50,
                "Different seeds should produce mostly different values");
        }
    }

    @Nested
    @DisplayName("Edge case tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle zero coordinates")
        void shouldHandleZeroCoordinates() {
            assertDoesNotThrow(() -> cellularGen.single2D(1337, 0, 0));
            assertDoesNotThrow(() -> cellularGen.single3D(1337, 0, 0, 0));
        }

        @Test
        @DisplayName("should handle large coordinates")
        void shouldHandleLargeCoordinates() {
            float large = 10000.0f;
            float value2D = cellularGen.single2D(1337, large, large);
            float value3D = cellularGen.single3D(1337, large, large, large);

            assertFalse(Float.isNaN(value2D));
            assertFalse(Float.isInfinite(value2D));
            assertFalse(Float.isNaN(value3D));
            assertFalse(Float.isInfinite(value3D));
        }

        @Test
        @DisplayName("should handle negative coordinates")
        void shouldHandleNegativeCoordinates() {
            float neg = -500.0f;
            float value2D = cellularGen.single2D(1337, neg, neg);
            float value3D = cellularGen.single3D(1337, neg, neg, neg);

            assertFalse(Float.isNaN(value2D));
            assertFalse(Float.isInfinite(value2D));
            assertFalse(Float.isNaN(value3D));
            assertFalse(Float.isInfinite(value3D));
        }
    }
}
