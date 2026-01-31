package com.teamgannon.trips.noisegen.fractal;

import com.teamgannon.trips.noisegen.NoiseConfig;
import com.teamgannon.trips.noisegen.generators.NoiseGenerator;
import com.teamgannon.trips.noisegen.generators.SimplexNoiseGen;
import com.teamgannon.trips.noisegen.generators.PerlinNoiseGen;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for FractalProcessor class.
 */
class FractalProcessorTest {

    private NoiseConfig config;
    private NoiseGenerator simplexGen;
    private NoiseGenerator perlinGen;
    private FractalProcessor processor;

    @BeforeEach
    void setUp() {
        config = new NoiseConfig();
        config.setSeed(1337);
        config.setOctaves(4);
        config.setLacunarity(2.0f);
        config.setGain(0.5f);
        config.setWeightedStrength(0.0f);
        config.setPingPongStrength(2.0f);

        simplexGen = new SimplexNoiseGen(false);
        perlinGen = new PerlinNoiseGen();
        processor = new FractalProcessor(config, simplexGen);
    }

    @Nested
    @DisplayName("FBm (Fractional Brownian motion) tests")
    class FBmTests {

        @Test
        @DisplayName("genFractalFBm2D should return bounded values")
        void genFractalFBm2DShouldReturnBoundedValues() {
            for (int x = -100; x <= 100; x += 10) {
                for (int y = -100; y <= 100; y += 10) {
                    float value = processor.genFractalFBm2D(x * 0.01f, y * 0.01f);
                    assertFalse(Float.isNaN(value),
                        String.format("NaN at (%d, %d)", x, y));
                    assertTrue(value >= -2.0f && value <= 2.0f,
                        String.format("Value %.4f out of range at (%d, %d)", value, x, y));
                }
            }
        }

        @Test
        @DisplayName("genFractalFBm3D should return bounded values")
        void genFractalFBm3DShouldReturnBoundedValues() {
            for (int x = -50; x <= 50; x += 25) {
                for (int y = -50; y <= 50; y += 25) {
                    for (int z = -50; z <= 50; z += 25) {
                        float value = processor.genFractalFBm3D(x * 0.01f, y * 0.01f, z * 0.01f);
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
        @DisplayName("FBm should be deterministic")
        void fbmShouldBeDeterministic() {
            float v1 = processor.genFractalFBm2D(50.0f, 50.0f);
            float v2 = processor.genFractalFBm2D(50.0f, 50.0f);

            assertEquals(v1, v2, "Same inputs should produce same output");
        }

        @Test
        @DisplayName("FBm octaves should affect output")
        void fbmOctavesShouldAffectOutput() {
            // Use coordinates that produce non-zero noise
            float x = 12.34f;
            float y = 56.78f;

            config.setOctaves(2);
            FractalProcessor proc2 = new FractalProcessor(config, simplexGen);
            float v2oct = proc2.genFractalFBm2D(x, y);

            config.setOctaves(6);
            FractalProcessor proc6 = new FractalProcessor(config, simplexGen);
            float v6oct = proc6.genFractalFBm2D(x, y);

            assertNotEquals(v2oct, v6oct, 0.001f,
                "Different octave counts should produce different results");
        }

        @Test
        @DisplayName("FBm lacunarity should affect output")
        void fbmLacunarityShouldAffectOutput() {
            float x = 12.34f;
            float y = 56.78f;

            config.setLacunarity(2.0f);
            FractalProcessor proc2 = new FractalProcessor(config, simplexGen);
            float vLac2 = proc2.genFractalFBm2D(x, y);

            config.setLacunarity(3.0f);
            FractalProcessor proc3 = new FractalProcessor(config, simplexGen);
            float vLac3 = proc3.genFractalFBm2D(x, y);

            assertNotEquals(vLac2, vLac3, 0.001f,
                "Different lacunarity should produce different results");
        }

        @Test
        @DisplayName("FBm gain should affect output")
        void fbmGainShouldAffectOutput() {
            float x = 12.34f;
            float y = 56.78f;

            config.setGain(0.3f);
            FractalProcessor procLow = new FractalProcessor(config, simplexGen);
            float vGainLow = procLow.genFractalFBm2D(x, y);

            config.setGain(0.7f);
            FractalProcessor procHigh = new FractalProcessor(config, simplexGen);
            float vGainHigh = procHigh.genFractalFBm2D(x, y);

            assertNotEquals(vGainLow, vGainHigh, 0.001f,
                "Different gain should produce different results");
        }
    }

    @Nested
    @DisplayName("Ridged fractal tests")
    class RidgedTests {

        @Test
        @DisplayName("genFractalRidged2D should return bounded values")
        void genFractalRidged2DShouldReturnBoundedValues() {
            for (int x = -100; x <= 100; x += 10) {
                for (int y = -100; y <= 100; y += 10) {
                    float value = processor.genFractalRidged2D(x * 0.01f, y * 0.01f);
                    assertFalse(Float.isNaN(value),
                        String.format("NaN at (%d, %d)", x, y));
                    assertTrue(value >= -2.0f && value <= 2.0f,
                        String.format("Value %.4f out of range at (%d, %d)", value, x, y));
                }
            }
        }

        @Test
        @DisplayName("genFractalRidged3D should return bounded values")
        void genFractalRidged3DShouldReturnBoundedValues() {
            for (int x = -50; x <= 50; x += 25) {
                for (int y = -50; y <= 50; y += 25) {
                    for (int z = -50; z <= 50; z += 25) {
                        float value = processor.genFractalRidged3D(x * 0.01f, y * 0.01f, z * 0.01f);
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
        @DisplayName("Ridged should be deterministic")
        void ridgedShouldBeDeterministic() {
            float v1 = processor.genFractalRidged2D(50.0f, 50.0f);
            float v2 = processor.genFractalRidged2D(50.0f, 50.0f);

            assertEquals(v1, v2, "Same inputs should produce same output");
        }

        @Test
        @DisplayName("Ridged should produce different pattern than FBm")
        void ridgedShouldProduceDifferentPatternThanFBm() {
            int differences = 0;

            for (int i = 0; i < 100; i++) {
                float fbm = processor.genFractalFBm2D(i * 0.1f, i * 0.15f);
                float ridged = processor.genFractalRidged2D(i * 0.1f, i * 0.15f);

                if (Math.abs(fbm - ridged) > 0.01f) {
                    differences++;
                }
            }

            assertTrue(differences > 50,
                "Ridged and FBm should produce mostly different values");
        }
    }

    @Nested
    @DisplayName("PingPong fractal tests")
    class PingPongTests {

        @Test
        @DisplayName("genFractalPingPong2D should return bounded values")
        void genFractalPingPong2DShouldReturnBoundedValues() {
            for (int x = -100; x <= 100; x += 10) {
                for (int y = -100; y <= 100; y += 10) {
                    float value = processor.genFractalPingPong2D(x * 0.01f, y * 0.01f);
                    assertFalse(Float.isNaN(value),
                        String.format("NaN at (%d, %d)", x, y));
                    assertTrue(value >= -2.0f && value <= 2.0f,
                        String.format("Value %.4f out of range at (%d, %d)", value, x, y));
                }
            }
        }

        @Test
        @DisplayName("genFractalPingPong3D should return bounded values")
        void genFractalPingPong3DShouldReturnBoundedValues() {
            for (int x = -50; x <= 50; x += 25) {
                for (int y = -50; y <= 50; y += 25) {
                    for (int z = -50; z <= 50; z += 25) {
                        float value = processor.genFractalPingPong3D(x * 0.01f, y * 0.01f, z * 0.01f);
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
        @DisplayName("PingPong should be deterministic")
        void pingPongShouldBeDeterministic() {
            float v1 = processor.genFractalPingPong2D(50.0f, 50.0f);
            float v2 = processor.genFractalPingPong2D(50.0f, 50.0f);

            assertEquals(v1, v2, "Same inputs should produce same output");
        }

        @Test
        @DisplayName("PingPong strength should affect output")
        void pingPongStrengthShouldAffectOutput() {
            config.setPingPongStrength(1.0f);
            FractalProcessor procLow = new FractalProcessor(config, simplexGen);
            float vLow = procLow.genFractalPingPong2D(50.0f, 50.0f);

            config.setPingPongStrength(4.0f);
            FractalProcessor procHigh = new FractalProcessor(config, simplexGen);
            float vHigh = procHigh.genFractalPingPong2D(50.0f, 50.0f);

            assertNotEquals(vLow, vHigh, 0.01f,
                "Different ping pong strength should produce different results");
        }

        @Test
        @DisplayName("PingPong should produce different pattern than FBm")
        void pingPongShouldProduceDifferentPatternThanFBm() {
            int differences = 0;

            for (int i = 0; i < 100; i++) {
                float fbm = processor.genFractalFBm2D(i * 0.1f, i * 0.15f);
                float pingPong = processor.genFractalPingPong2D(i * 0.1f, i * 0.15f);

                if (Math.abs(fbm - pingPong) > 0.01f) {
                    differences++;
                }
            }

            assertTrue(differences > 50,
                "PingPong and FBm should produce mostly different values");
        }
    }

    @Nested
    @DisplayName("WeightedStrength tests")
    class WeightedStrengthTests {

        @Test
        @DisplayName("weighted strength should affect FBm output")
        void weightedStrengthShouldAffectFBmOutput() {
            float x = 12.34f;
            float y = 56.78f;

            config.setWeightedStrength(0.0f);
            FractalProcessor proc0 = new FractalProcessor(config, simplexGen);
            float v0 = proc0.genFractalFBm2D(x, y);

            config.setWeightedStrength(0.8f);
            FractalProcessor proc5 = new FractalProcessor(config, simplexGen);
            float v5 = proc5.genFractalFBm2D(x, y);

            assertNotEquals(v0, v5, 0.0001f,
                "Different weighted strength should produce different FBm results");
        }

        @Test
        @DisplayName("weighted strength should affect Ridged output")
        void weightedStrengthShouldAffectRidgedOutput() {
            float x = 12.34f;
            float y = 56.78f;

            config.setWeightedStrength(0.0f);
            FractalProcessor proc0 = new FractalProcessor(config, simplexGen);
            float v0 = proc0.genFractalRidged2D(x, y);

            config.setWeightedStrength(0.8f);
            FractalProcessor proc5 = new FractalProcessor(config, simplexGen);
            float v5 = proc5.genFractalRidged2D(x, y);

            assertNotEquals(v0, v5, 0.0001f,
                "Different weighted strength should produce different Ridged results");
        }
    }

    @Nested
    @DisplayName("Different generator tests")
    class DifferentGeneratorTests {

        @Test
        @DisplayName("FBm with different generators should produce different results")
        void fbmWithDifferentGeneratorsShouldProduceDifferentResults() {
            FractalProcessor simplexProc = new FractalProcessor(config, simplexGen);
            FractalProcessor perlinProc = new FractalProcessor(config, perlinGen);

            int differences = 0;
            for (int i = 0; i < 100; i++) {
                float simplex = simplexProc.genFractalFBm2D(i * 0.1f, i * 0.15f);
                float perlin = perlinProc.genFractalFBm2D(i * 0.1f, i * 0.15f);

                if (Math.abs(simplex - perlin) > 0.01f) {
                    differences++;
                }
            }

            assertTrue(differences > 50,
                "Different base generators should produce different FBm results");
        }
    }

    @Nested
    @DisplayName("Edge case tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle zero coordinates")
        void shouldHandleZeroCoordinates() {
            assertDoesNotThrow(() -> processor.genFractalFBm2D(0, 0));
            assertDoesNotThrow(() -> processor.genFractalFBm3D(0, 0, 0));
            assertDoesNotThrow(() -> processor.genFractalRidged2D(0, 0));
            assertDoesNotThrow(() -> processor.genFractalRidged3D(0, 0, 0));
            assertDoesNotThrow(() -> processor.genFractalPingPong2D(0, 0));
            assertDoesNotThrow(() -> processor.genFractalPingPong3D(0, 0, 0));
        }

        @Test
        @DisplayName("should handle large coordinates")
        void shouldHandleLargeCoordinates() {
            float large = 10000.0f;

            assertFalse(Float.isNaN(processor.genFractalFBm2D(large, large)));
            assertFalse(Float.isNaN(processor.genFractalFBm3D(large, large, large)));
            assertFalse(Float.isNaN(processor.genFractalRidged2D(large, large)));
            assertFalse(Float.isNaN(processor.genFractalRidged3D(large, large, large)));
            assertFalse(Float.isNaN(processor.genFractalPingPong2D(large, large)));
            assertFalse(Float.isNaN(processor.genFractalPingPong3D(large, large, large)));
        }

        @Test
        @DisplayName("should handle single octave")
        void shouldHandleSingleOctave() {
            config.setOctaves(1);
            FractalProcessor singleOctave = new FractalProcessor(config, simplexGen);

            assertFalse(Float.isNaN(singleOctave.genFractalFBm2D(50.0f, 50.0f)));
            assertFalse(Float.isNaN(singleOctave.genFractalRidged2D(50.0f, 50.0f)));
            assertFalse(Float.isNaN(singleOctave.genFractalPingPong2D(50.0f, 50.0f)));
        }
    }
}
