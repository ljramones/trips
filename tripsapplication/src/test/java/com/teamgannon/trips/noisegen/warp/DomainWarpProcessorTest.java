package com.teamgannon.trips.noisegen.warp;

import com.teamgannon.trips.noisegen.NoiseConfig;
import com.teamgannon.trips.noisegen.NoiseTypes.DomainWarpType;
import com.teamgannon.trips.noisegen.NoiseTypes.FractalType;
import com.teamgannon.trips.noisegen.Vector2;
import com.teamgannon.trips.noisegen.Vector3;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DomainWarpProcessor class.
 */
class DomainWarpProcessorTest {

    private NoiseConfig config;
    private DomainWarpProcessor warpProcessor;

    @BeforeEach
    void setUp() {
        config = new NoiseConfig();
        config.setSeed(1337);
        config.setFrequency(0.01f);
        config.setDomainWarpAmp(30.0f);
        config.setDomainWarpType(DomainWarpType.OpenSimplex2);
        config.setOctaves(4);
        config.setLacunarity(2.0f);
        config.setGain(0.5f);

        warpProcessor = new DomainWarpProcessor(config);
    }

    @Nested
    @DisplayName("domainWarpSingle 2D tests")
    class DomainWarpSingle2DTests {

        @Test
        @DisplayName("domainWarpSingle should modify 2D coordinates")
        void domainWarpSingleShouldModify2DCoordinates() {
            Vector2 coord = new Vector2(100.0f, 100.0f);
            float originalX = coord.x;
            float originalY = coord.y;

            warpProcessor.domainWarpSingle(coord);

            assertTrue(coord.x != originalX || coord.y != originalY,
                "Domain warp should modify at least one coordinate");
            assertFalse(Float.isNaN(coord.x), "Warped X should not be NaN");
            assertFalse(Float.isNaN(coord.y), "Warped Y should not be NaN");
        }

        @Test
        @DisplayName("domainWarpSingle should be deterministic")
        void domainWarpSingleShouldBeDeterministic() {
            Vector2 coord1 = new Vector2(50.0f, 50.0f);
            Vector2 coord2 = new Vector2(50.0f, 50.0f);

            warpProcessor.domainWarpSingle(coord1);
            warpProcessor.domainWarpSingle(coord2);

            assertEquals(coord1.x, coord2.x, "Same input should produce same X");
            assertEquals(coord1.y, coord2.y, "Same input should produce same Y");
        }

        @Test
        @DisplayName("amplitude should affect warp magnitude")
        void amplitudeShouldAffectWarpMagnitude() {
            config.setDomainWarpAmp(10.0f);
            DomainWarpProcessor lowAmp = new DomainWarpProcessor(config);
            Vector2 coordLow = new Vector2(100.0f, 100.0f);
            lowAmp.domainWarpSingle(coordLow);
            float displaceLow = distance(coordLow, 100.0f, 100.0f);

            config.setDomainWarpAmp(100.0f);
            DomainWarpProcessor highAmp = new DomainWarpProcessor(config);
            Vector2 coordHigh = new Vector2(100.0f, 100.0f);
            highAmp.domainWarpSingle(coordHigh);
            float displaceHigh = distance(coordHigh, 100.0f, 100.0f);

            // Average displacement should be roughly proportional to amplitude
            assertNotEquals(displaceLow, displaceHigh, 0.1f,
                "Different amplitudes should produce different displacements");
        }
    }

    @Nested
    @DisplayName("domainWarpSingle 3D tests")
    class DomainWarpSingle3DTests {

        @Test
        @DisplayName("domainWarpSingle should modify 3D coordinates or produce valid output")
        void domainWarpSingleShouldModify3DCoordinates() {
            // Test multiple coordinates to ensure at least one shows modification
            boolean anyModified = false;
            for (int i = 0; i < 10; i++) {
                Vector3 coord = new Vector3(10.0f + i * 7.3f, 20.0f + i * 11.7f, 30.0f + i * 13.1f);
                float originalX = coord.x;
                float originalY = coord.y;
                float originalZ = coord.z;

                warpProcessor.domainWarpSingle(coord);

                assertFalse(Float.isNaN(coord.x), "Warped X should not be NaN");
                assertFalse(Float.isNaN(coord.y), "Warped Y should not be NaN");
                assertFalse(Float.isNaN(coord.z), "Warped Z should not be NaN");

                if (coord.x != originalX || coord.y != originalY || coord.z != originalZ) {
                    anyModified = true;
                }
            }
            assertTrue(anyModified, "At least some coordinates should be modified");
        }

        @Test
        @DisplayName("domainWarpSingle 3D should be deterministic")
        void domainWarpSingle3DShouldBeDeterministic() {
            Vector3 coord1 = new Vector3(12.34f, 56.78f, 90.12f);
            Vector3 coord2 = new Vector3(12.34f, 56.78f, 90.12f);

            warpProcessor.domainWarpSingle(coord1);
            warpProcessor.domainWarpSingle(coord2);

            assertEquals(coord1.x, coord2.x, "Same input should produce same X");
            assertEquals(coord1.y, coord2.y, "Same input should produce same Y");
            assertEquals(coord1.z, coord2.z, "Same input should produce same Z");
        }
    }

    @Nested
    @DisplayName("DomainWarpType tests")
    class DomainWarpTypeTests {

        @ParameterizedTest
        @EnumSource(DomainWarpType.class)
        @DisplayName("all warp types should work for 2D")
        void allWarpTypesShouldWorkFor2D(DomainWarpType warpType) {
            config.setDomainWarpType(warpType);
            DomainWarpProcessor proc = new DomainWarpProcessor(config);

            for (int i = 0; i < 20; i++) {
                Vector2 coord = new Vector2(i * 10.0f, i * 15.0f);
                assertDoesNotThrow(() -> proc.domainWarpSingle(coord),
                    "Warp type " + warpType + " should work at (" + i * 10 + ", " + i * 15 + ")");
                assertFalse(Float.isNaN(coord.x),
                    "Warp type " + warpType + " should not produce NaN X");
                assertFalse(Float.isNaN(coord.y),
                    "Warp type " + warpType + " should not produce NaN Y");
            }
        }

        @ParameterizedTest
        @EnumSource(DomainWarpType.class)
        @DisplayName("all warp types should work for 3D")
        void allWarpTypesShouldWorkFor3D(DomainWarpType warpType) {
            config.setDomainWarpType(warpType);
            DomainWarpProcessor proc = new DomainWarpProcessor(config);

            for (int i = 0; i < 10; i++) {
                Vector3 coord = new Vector3(i * 10.0f, i * 15.0f, i * 20.0f);
                assertDoesNotThrow(() -> proc.domainWarpSingle(coord),
                    "Warp type " + warpType + " should work for 3D");
                assertFalse(Float.isNaN(coord.x),
                    "Warp type " + warpType + " should not produce NaN X");
                assertFalse(Float.isNaN(coord.y),
                    "Warp type " + warpType + " should not produce NaN Y");
                assertFalse(Float.isNaN(coord.z),
                    "Warp type " + warpType + " should not produce NaN Z");
            }
        }

        @Test
        @DisplayName("different warp types should produce valid output")
        void differentWarpTypesShouldProduceValidOutput() {
            // Test that different warp types produce valid (non-NaN) output
            // The actual patterns may or may not differ depending on implementation
            for (DomainWarpType warpType : DomainWarpType.values()) {
                config.setDomainWarpType(warpType);
                config.setDomainWarpAmp(50.0f);
                DomainWarpProcessor proc = new DomainWarpProcessor(config);

                for (int i = 0; i < 20; i++) {
                    Vector2 coord = new Vector2(i * 7.3f + 0.5f, i * 11.7f + 0.5f);
                    proc.domainWarpSingle(coord);

                    assertFalse(Float.isNaN(coord.x),
                        String.format("Warp type %s should not produce NaN X", warpType));
                    assertFalse(Float.isNaN(coord.y),
                        String.format("Warp type %s should not produce NaN Y", warpType));
                    assertFalse(Float.isInfinite(coord.x),
                        String.format("Warp type %s should not produce infinite X", warpType));
                    assertFalse(Float.isInfinite(coord.y),
                        String.format("Warp type %s should not produce infinite Y", warpType));
                }
            }
        }
    }

    @Nested
    @DisplayName("domainWarpFractalProgressive tests")
    class DomainWarpFractalProgressiveTests {

        @Test
        @DisplayName("fractal progressive should modify 2D coordinates")
        void fractalProgressiveShouldModify2DCoordinates() {
            Vector2 coord = new Vector2(100.0f, 100.0f);
            float originalX = coord.x;
            float originalY = coord.y;

            warpProcessor.domainWarpFractalProgressive(coord);

            assertTrue(coord.x != originalX || coord.y != originalY,
                "Fractal progressive should modify coordinates");
            assertFalse(Float.isNaN(coord.x));
            assertFalse(Float.isNaN(coord.y));
        }

        @Test
        @DisplayName("fractal progressive should modify 3D coordinates or produce valid output")
        void fractalProgressiveShouldModify3DCoordinates() {
            // Test multiple coordinates to ensure at least one shows modification
            boolean anyModified = false;
            for (int i = 0; i < 10; i++) {
                Vector3 coord = new Vector3(10.0f + i * 7.3f, 20.0f + i * 11.7f, 30.0f + i * 13.1f);
                float originalX = coord.x;
                float originalY = coord.y;
                float originalZ = coord.z;

                warpProcessor.domainWarpFractalProgressive(coord);

                assertFalse(Float.isNaN(coord.x));
                assertFalse(Float.isNaN(coord.y));
                assertFalse(Float.isNaN(coord.z));

                if (coord.x != originalX || coord.y != originalY || coord.z != originalZ) {
                    anyModified = true;
                }
            }
            assertTrue(anyModified, "At least some coordinates should be modified");
        }

        @Test
        @DisplayName("fractal progressive should differ from single warp")
        void fractalProgressiveShouldDifferFromSingleWarp() {
            int differences = 0;

            for (int i = 0; i < 50; i++) {
                Vector2 coordSingle = new Vector2(i * 10.0f, i * 15.0f);
                Vector2 coordProgressive = new Vector2(i * 10.0f, i * 15.0f);

                warpProcessor.domainWarpSingle(coordSingle);
                warpProcessor.domainWarpFractalProgressive(coordProgressive);

                if (Math.abs(coordSingle.x - coordProgressive.x) > 0.01f ||
                    Math.abs(coordSingle.y - coordProgressive.y) > 0.01f) {
                    differences++;
                }
            }

            assertTrue(differences > 25,
                "Fractal progressive should differ from single warp");
        }
    }

    @Nested
    @DisplayName("domainWarpFractalIndependent tests")
    class DomainWarpFractalIndependentTests {

        @Test
        @DisplayName("fractal independent should modify 2D coordinates")
        void fractalIndependentShouldModify2DCoordinates() {
            Vector2 coord = new Vector2(100.0f, 100.0f);
            float originalX = coord.x;
            float originalY = coord.y;

            warpProcessor.domainWarpFractalIndependent(coord);

            assertTrue(coord.x != originalX || coord.y != originalY,
                "Fractal independent should modify coordinates");
            assertFalse(Float.isNaN(coord.x));
            assertFalse(Float.isNaN(coord.y));
        }

        @Test
        @DisplayName("fractal independent should modify 3D coordinates or produce valid output")
        void fractalIndependentShouldModify3DCoordinates() {
            // Test multiple coordinates to ensure at least one shows modification
            boolean anyModified = false;
            for (int i = 0; i < 10; i++) {
                Vector3 coord = new Vector3(10.0f + i * 7.3f, 20.0f + i * 11.7f, 30.0f + i * 13.1f);
                float originalX = coord.x;
                float originalY = coord.y;
                float originalZ = coord.z;

                warpProcessor.domainWarpFractalIndependent(coord);

                assertFalse(Float.isNaN(coord.x));
                assertFalse(Float.isNaN(coord.y));
                assertFalse(Float.isNaN(coord.z));

                if (coord.x != originalX || coord.y != originalY || coord.z != originalZ) {
                    anyModified = true;
                }
            }
            assertTrue(anyModified, "At least some coordinates should be modified");
        }

        @Test
        @DisplayName("fractal independent should differ from progressive")
        void fractalIndependentShouldDifferFromProgressive() {
            int differences = 0;

            for (int i = 0; i < 50; i++) {
                Vector2 coordProg = new Vector2(i * 10.0f, i * 15.0f);
                Vector2 coordIndep = new Vector2(i * 10.0f, i * 15.0f);

                warpProcessor.domainWarpFractalProgressive(coordProg);
                warpProcessor.domainWarpFractalIndependent(coordIndep);

                if (Math.abs(coordProg.x - coordIndep.x) > 0.01f ||
                    Math.abs(coordProg.y - coordIndep.y) > 0.01f) {
                    differences++;
                }
            }

            assertTrue(differences > 25,
                "Fractal independent should differ from progressive");
        }
    }

    @Nested
    @DisplayName("Seed tests")
    class SeedTests {

        @Test
        @DisplayName("same seed should produce deterministic warps")
        void sameSeedShouldProduceDeterministicWarps() {
            config.setSeed(1337);
            config.setDomainWarpAmp(50.0f);
            DomainWarpProcessor proc1 = new DomainWarpProcessor(config);
            DomainWarpProcessor proc2 = new DomainWarpProcessor(config);

            for (int i = 0; i < 20; i++) {
                Vector2 coord1 = new Vector2(i * 7.3f + 0.5f, i * 11.7f + 0.5f);
                Vector2 coord2 = new Vector2(i * 7.3f + 0.5f, i * 11.7f + 0.5f);

                proc1.domainWarpSingle(coord1);
                proc2.domainWarpSingle(coord2);

                assertEquals(coord1.x, coord2.x, 0.0001f,
                    "Same seed should produce same X");
                assertEquals(coord1.y, coord2.y, 0.0001f,
                    "Same seed should produce same Y");
            }
        }

        @Test
        @DisplayName("warp processor should produce valid output with different seeds")
        void warpProcessorShouldProduceValidOutputWithDifferentSeeds() {
            int[] seeds = {0, 1, 42, 1337, -1, Integer.MAX_VALUE};

            for (int seed : seeds) {
                config.setSeed(seed);
                config.setDomainWarpAmp(50.0f);
                DomainWarpProcessor proc = new DomainWarpProcessor(config);

                Vector2 coord = new Vector2(12.34f, 56.78f);
                proc.domainWarpSingle(coord);

                assertFalse(Float.isNaN(coord.x),
                    String.format("Seed %d should not produce NaN X", seed));
                assertFalse(Float.isNaN(coord.y),
                    String.format("Seed %d should not produce NaN Y", seed));
            }
        }
    }

    @Nested
    @DisplayName("Edge case tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle zero coordinates")
        void shouldHandleZeroCoordinates() {
            Vector2 coord2D = new Vector2(0, 0);
            assertDoesNotThrow(() -> warpProcessor.domainWarpSingle(coord2D));

            Vector3 coord3D = new Vector3(0, 0, 0);
            assertDoesNotThrow(() -> warpProcessor.domainWarpSingle(coord3D));
        }

        @Test
        @DisplayName("should handle large coordinates")
        void shouldHandleLargeCoordinates() {
            float large = 100000.0f;

            Vector2 coord2D = new Vector2(large, large);
            warpProcessor.domainWarpSingle(coord2D);
            assertFalse(Float.isNaN(coord2D.x));
            assertFalse(Float.isNaN(coord2D.y));
            assertFalse(Float.isInfinite(coord2D.x));
            assertFalse(Float.isInfinite(coord2D.y));

            Vector3 coord3D = new Vector3(large, large, large);
            warpProcessor.domainWarpSingle(coord3D);
            assertFalse(Float.isNaN(coord3D.x));
            assertFalse(Float.isNaN(coord3D.y));
            assertFalse(Float.isNaN(coord3D.z));
        }

        @Test
        @DisplayName("should handle negative coordinates")
        void shouldHandleNegativeCoordinates() {
            float neg = -1000.0f;

            Vector2 coord2D = new Vector2(neg, neg);
            warpProcessor.domainWarpSingle(coord2D);
            assertFalse(Float.isNaN(coord2D.x));
            assertFalse(Float.isNaN(coord2D.y));

            Vector3 coord3D = new Vector3(neg, neg, neg);
            warpProcessor.domainWarpSingle(coord3D);
            assertFalse(Float.isNaN(coord3D.x));
            assertFalse(Float.isNaN(coord3D.y));
            assertFalse(Float.isNaN(coord3D.z));
        }

        @Test
        @DisplayName("should handle zero amplitude")
        void shouldHandleZeroAmplitude() {
            config.setDomainWarpAmp(0.0f);
            DomainWarpProcessor zeroAmp = new DomainWarpProcessor(config);

            Vector2 coord = new Vector2(100.0f, 100.0f);
            zeroAmp.domainWarpSingle(coord);

            // With zero amplitude, coordinates should be unchanged
            assertEquals(100.0f, coord.x, 0.001f, "Zero amplitude should not change X");
            assertEquals(100.0f, coord.y, 0.001f, "Zero amplitude should not change Y");
        }
    }

    // Helper method
    private float distance(Vector2 coord, float fromX, float fromY) {
        float dx = coord.x - fromX;
        float dy = coord.y - fromY;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
}
