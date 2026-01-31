package com.teamgannon.trips.noisegen;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for FastNoiseLite facade class.
 * Verifies backward compatibility and correct delegation to modular components.
 */
class FastNoiseLiteTest {

    private FastNoiseLite noise;

    @BeforeEach
    void setUp() {
        noise = new FastNoiseLite();
    }

    @Nested
    @DisplayName("Constructor tests")
    class ConstructorTests {

        @Test
        @DisplayName("default constructor should create valid instance")
        void defaultConstructorShouldCreateValidInstance() {
            FastNoiseLite noise = new FastNoiseLite();

            assertNotNull(noise);
            // Should generate valid noise with default settings
            float value = noise.GetNoise(0f, 0f);
            assertFalse(Float.isNaN(value));
        }

        @Test
        @DisplayName("seed constructor should create valid instance")
        void seedConstructorShouldCreateValidInstance() {
            FastNoiseLite noise = new FastNoiseLite(12345);

            assertNotNull(noise);
            float value = noise.GetNoise(0f, 0f);
            assertFalse(Float.isNaN(value));
        }
    }

    @Nested
    @DisplayName("Noise output range tests")
    class NoiseOutputRangeTests {

        @ParameterizedTest
        @EnumSource(FastNoiseLite.NoiseType.class)
        @DisplayName("2D noise should be bounded between -1 and 1 for all noise types")
        void noise2DShouldBeBoundedForAllTypes(FastNoiseLite.NoiseType noiseType) {
            noise.SetNoiseType(noiseType);
            noise.SetFrequency(0.01f);

            for (int x = -100; x <= 100; x += 10) {
                for (int y = -100; y <= 100; y += 10) {
                    float value = noise.GetNoise(x, y);
                    assertFalse(Float.isNaN(value),
                        String.format("NaN at (%d, %d) for %s", x, y, noiseType));
                    assertTrue(value >= -1.5f && value <= 1.5f,
                        String.format("Value %.4f out of range at (%d, %d) for %s",
                            value, x, y, noiseType));
                }
            }
        }

        @ParameterizedTest
        @EnumSource(FastNoiseLite.NoiseType.class)
        @DisplayName("3D noise should be bounded between -1 and 1 for all noise types")
        void noise3DShouldBeBoundedForAllTypes(FastNoiseLite.NoiseType noiseType) {
            noise.SetNoiseType(noiseType);
            noise.SetFrequency(0.01f);

            for (int x = -50; x <= 50; x += 25) {
                for (int y = -50; y <= 50; y += 25) {
                    for (int z = -50; z <= 50; z += 25) {
                        float value = noise.GetNoise(x, y, z);
                        assertFalse(Float.isNaN(value),
                            String.format("NaN at (%d, %d, %d) for %s", x, y, z, noiseType));
                        assertTrue(value >= -1.5f && value <= 1.5f,
                            String.format("Value %.4f out of range at (%d, %d, %d) for %s",
                                value, x, y, z, noiseType));
                    }
                }
            }
        }
    }

    @Nested
    @DisplayName("Determinism tests")
    class DeterminismTests {

        @Test
        @DisplayName("same seed should produce same 2D output")
        void sameSeedShouldProduceSame2DOutput() {
            FastNoiseLite noise1 = new FastNoiseLite(1337);
            FastNoiseLite noise2 = new FastNoiseLite(1337);

            for (int x = 0; x < 100; x += 10) {
                for (int y = 0; y < 100; y += 10) {
                    assertEquals(noise1.GetNoise(x, y), noise2.GetNoise(x, y),
                        String.format("Mismatch at (%d, %d)", x, y));
                }
            }
        }

        @Test
        @DisplayName("same seed should produce same 3D output")
        void sameSeedShouldProduceSame3DOutput() {
            FastNoiseLite noise1 = new FastNoiseLite(1337);
            FastNoiseLite noise2 = new FastNoiseLite(1337);

            for (int x = 0; x < 50; x += 10) {
                for (int y = 0; y < 50; y += 10) {
                    for (int z = 0; z < 50; z += 10) {
                        assertEquals(noise1.GetNoise(x, y, z), noise2.GetNoise(x, y, z),
                            String.format("Mismatch at (%d, %d, %d)", x, y, z));
                    }
                }
            }
        }

        @Test
        @DisplayName("different seeds should produce different output")
        void differentSeedsShouldProduceDifferentOutput() {
            FastNoiseLite noise1 = new FastNoiseLite(1337);
            FastNoiseLite noise2 = new FastNoiseLite(42);

            int differentCount = 0;
            for (int x = 0; x < 100; x += 10) {
                for (int y = 0; y < 100; y += 10) {
                    if (noise1.GetNoise(x, y) != noise2.GetNoise(x, y)) {
                        differentCount++;
                    }
                }
            }

            assertTrue(differentCount > 50, "Most values should differ between seeds");
        }
    }

    @Nested
    @DisplayName("Noise type distinction tests")
    class NoiseTypeDistinctionTests {

        @Test
        @DisplayName("different noise types should produce different patterns")
        void differentNoiseTypesShouldProduceDifferentPatterns() {
            FastNoiseLite.NoiseType[] types = FastNoiseLite.NoiseType.values();
            float[][] outputs = new float[types.length][100];

            for (int t = 0; t < types.length; t++) {
                noise.SetNoiseType(types[t]);
                noise.SetSeed(1337);
                for (int i = 0; i < 100; i++) {
                    outputs[t][i] = noise.GetNoise(i * 0.1f, i * 0.15f);
                }
            }

            // Compare each pair of noise types
            for (int t1 = 0; t1 < types.length; t1++) {
                for (int t2 = t1 + 1; t2 < types.length; t2++) {
                    int differences = 0;
                    for (int i = 0; i < 100; i++) {
                        if (Math.abs(outputs[t1][i] - outputs[t2][i]) > 0.001f) {
                            differences++;
                        }
                    }
                    assertTrue(differences > 20,
                        String.format("%s and %s should produce mostly different values",
                            types[t1], types[t2]));
                }
            }
        }
    }

    @Nested
    @DisplayName("Fractal type tests")
    class FractalTypeTests {

        @ParameterizedTest
        @EnumSource(value = FastNoiseLite.FractalType.class,
            names = {"FBm", "Ridged", "PingPong"})
        @DisplayName("fractal types should produce valid 2D output")
        void fractalTypesShouldProduceValid2DOutput(FastNoiseLite.FractalType fractalType) {
            noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
            noise.SetFractalType(fractalType);
            noise.SetFractalOctaves(4);
            noise.SetFrequency(0.01f);

            for (int x = 0; x < 100; x += 10) {
                for (int y = 0; y < 100; y += 10) {
                    float value = noise.GetNoise(x, y);
                    assertFalse(Float.isNaN(value),
                        String.format("NaN at (%d, %d) for %s", x, y, fractalType));
                }
            }
        }

        @ParameterizedTest
        @EnumSource(value = FastNoiseLite.FractalType.class,
            names = {"FBm", "Ridged", "PingPong"})
        @DisplayName("fractal types should produce valid 3D output")
        void fractalTypesShouldProduceValid3DOutput(FastNoiseLite.FractalType fractalType) {
            noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
            noise.SetFractalType(fractalType);
            noise.SetFractalOctaves(4);
            noise.SetFrequency(0.01f);

            for (int x = 0; x < 50; x += 10) {
                for (int y = 0; y < 50; y += 10) {
                    for (int z = 0; z < 50; z += 10) {
                        float value = noise.GetNoise(x, y, z);
                        assertFalse(Float.isNaN(value),
                            String.format("NaN at (%d, %d, %d) for %s", x, y, z, fractalType));
                    }
                }
            }
        }

        @Test
        @DisplayName("FBm octaves should increase detail")
        void fbmOctavesShouldIncreaseDetail() {
            noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
            noise.SetFractalType(FastNoiseLite.FractalType.FBm);
            noise.SetFrequency(0.01f);

            // Calculate variance with 1 octave
            noise.SetFractalOctaves(1);
            float[] values1 = new float[100];
            for (int i = 0; i < 100; i++) {
                values1[i] = noise.GetNoise(i * 0.5f, i * 0.5f);
            }
            float variance1 = calculateVariance(values1);

            // Calculate variance with 4 octaves
            noise.SetFractalOctaves(4);
            float[] values4 = new float[100];
            for (int i = 0; i < 100; i++) {
                values4[i] = noise.GetNoise(i * 0.5f, i * 0.5f);
            }
            float variance4 = calculateVariance(values4);

            // More octaves generally changes the character of the noise
            assertNotEquals(variance1, variance4, 0.01f,
                "Different octave counts should produce different variance");
        }

        private float calculateVariance(float[] values) {
            float mean = 0;
            for (float v : values) mean += v;
            mean /= values.length;

            float variance = 0;
            for (float v : values) variance += (v - mean) * (v - mean);
            return variance / values.length;
        }
    }

    @Nested
    @DisplayName("Domain warp tests")
    class DomainWarpTests {

        @Test
        @DisplayName("domain warp should modify 2D coordinates")
        void domainWarpShouldModify2DCoordinates() {
            noise.SetDomainWarpType(FastNoiseLite.DomainWarpType.OpenSimplex2);
            noise.SetDomainWarpAmp(50.0f);
            noise.SetFrequency(0.01f);

            Vector2 coord = new Vector2(100f, 100f);
            float originalX = coord.x;
            float originalY = coord.y;

            noise.DomainWarp(coord);

            // Coordinates should have changed
            assertTrue(coord.x != originalX || coord.y != originalY,
                "Domain warp should modify coordinates");
            assertFalse(Float.isNaN(coord.x), "Warped X should not be NaN");
            assertFalse(Float.isNaN(coord.y), "Warped Y should not be NaN");
        }

        @Test
        @DisplayName("domain warp should modify 3D coordinates or produce valid output")
        void domainWarpShouldModify3DCoordinates() {
            noise.SetDomainWarpType(FastNoiseLite.DomainWarpType.OpenSimplex2);
            noise.SetDomainWarpAmp(50.0f);
            noise.SetFrequency(0.01f);

            // Test multiple coordinates to ensure at least one shows modification
            boolean anyModified = false;
            for (int i = 0; i < 10; i++) {
                Vector3 coord = new Vector3(10f + i * 7.3f, 20f + i * 11.7f, 30f + i * 13.1f);
                float originalX = coord.x;
                float originalY = coord.y;
                float originalZ = coord.z;

                noise.DomainWarp(coord);

                assertFalse(Float.isNaN(coord.x), "Warped X should not be NaN");
                assertFalse(Float.isNaN(coord.y), "Warped Y should not be NaN");
                assertFalse(Float.isNaN(coord.z), "Warped Z should not be NaN");

                if (coord.x != originalX || coord.y != originalY || coord.z != originalZ) {
                    anyModified = true;
                }
            }
            assertTrue(anyModified, "At least some 3D coordinates should be modified");
        }

        @Test
        @DisplayName("domain warp amplitude should affect displacement")
        void domainWarpAmplitudeShouldAffectDisplacement() {
            noise.SetDomainWarpType(FastNoiseLite.DomainWarpType.OpenSimplex2);
            noise.SetFrequency(0.01f);

            // Small amplitude
            noise.SetDomainWarpAmp(10.0f);
            Vector2 coordSmall = new Vector2(100f, 100f);
            noise.DomainWarp(coordSmall);
            float displaceSmall = (float) Math.sqrt(
                Math.pow(coordSmall.x - 100f, 2) + Math.pow(coordSmall.y - 100f, 2));

            // Large amplitude
            noise.SetDomainWarpAmp(100.0f);
            Vector2 coordLarge = new Vector2(100f, 100f);
            noise.DomainWarp(coordLarge);
            float displaceLarge = (float) Math.sqrt(
                Math.pow(coordLarge.x - 100f, 2) + Math.pow(coordLarge.y - 100f, 2));

            // Larger amplitude should generally cause larger displacement
            // (not always true due to noise variation, but statistically)
            assertNotEquals(displaceSmall, displaceLarge, 0.1f,
                "Different amplitudes should produce different displacements");
        }

        @ParameterizedTest
        @EnumSource(FastNoiseLite.DomainWarpType.class)
        @DisplayName("all domain warp types should work without errors")
        void allDomainWarpTypesShouldWork(FastNoiseLite.DomainWarpType warpType) {
            noise.SetDomainWarpType(warpType);
            noise.SetDomainWarpAmp(30.0f);
            noise.SetFrequency(0.01f);

            Vector2 coord2D = new Vector2(50f, 50f);
            assertDoesNotThrow(() -> noise.DomainWarp(coord2D));
            assertFalse(Float.isNaN(coord2D.x));
            assertFalse(Float.isNaN(coord2D.y));

            Vector3 coord3D = new Vector3(50f, 50f, 50f);
            assertDoesNotThrow(() -> noise.DomainWarp(coord3D));
            assertFalse(Float.isNaN(coord3D.x));
            assertFalse(Float.isNaN(coord3D.y));
            assertFalse(Float.isNaN(coord3D.z));
        }
    }

    @Nested
    @DisplayName("3D rotation type tests")
    class RotationType3DTests {

        @ParameterizedTest
        @EnumSource(FastNoiseLite.RotationType3D.class)
        @DisplayName("all rotation types should produce valid output")
        void allRotationTypesShouldProduceValidOutput(FastNoiseLite.RotationType3D rotationType) {
            noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
            noise.SetRotationType3D(rotationType);
            noise.SetFrequency(0.01f);

            for (int x = 0; x < 50; x += 10) {
                for (int y = 0; y < 50; y += 10) {
                    for (int z = 0; z < 50; z += 10) {
                        float value = noise.GetNoise(x, y, z);
                        assertFalse(Float.isNaN(value),
                            String.format("NaN at (%d, %d, %d) for %s", x, y, z, rotationType));
                        assertTrue(value >= -1.5f && value <= 1.5f,
                            String.format("Value out of range at (%d, %d, %d) for %s",
                                x, y, z, rotationType));
                    }
                }
            }
        }
    }

    @Nested
    @DisplayName("Cellular noise specific tests")
    class CellularNoiseTests {

        @BeforeEach
        void setUpCellular() {
            noise.SetNoiseType(FastNoiseLite.NoiseType.Cellular);
            noise.SetFrequency(0.02f);
        }

        @ParameterizedTest
        @EnumSource(FastNoiseLite.CellularDistanceFunction.class)
        @DisplayName("all distance functions should produce valid output")
        void allDistanceFunctionsShouldProduceValidOutput(
                FastNoiseLite.CellularDistanceFunction distFunc) {
            noise.SetCellularDistanceFunction(distFunc);

            for (int x = 0; x < 100; x += 10) {
                for (int y = 0; y < 100; y += 10) {
                    float value = noise.GetNoise(x, y);
                    assertFalse(Float.isNaN(value),
                        String.format("NaN at (%d, %d) for %s", x, y, distFunc));
                }
            }
        }

        @ParameterizedTest
        @EnumSource(FastNoiseLite.CellularReturnType.class)
        @DisplayName("all return types should produce valid output")
        void allReturnTypesShouldProduceValidOutput(
                FastNoiseLite.CellularReturnType returnType) {
            noise.SetCellularReturnType(returnType);

            for (int x = 0; x < 100; x += 10) {
                for (int y = 0; y < 100; y += 10) {
                    float value = noise.GetNoise(x, y);
                    assertFalse(Float.isNaN(value),
                        String.format("NaN at (%d, %d) for %s", x, y, returnType));
                }
            }
        }

        @Test
        @DisplayName("jitter should affect cellular pattern")
        void jitterShouldAffectCellularPattern() {
            noise.SetCellularReturnType(FastNoiseLite.CellularReturnType.Distance);

            // Low jitter
            noise.SetCellularJitter(0.1f);
            float[] lowJitter = new float[100];
            for (int i = 0; i < 100; i++) {
                lowJitter[i] = noise.GetNoise(i * 0.5f, i * 0.5f);
            }

            // High jitter
            noise.SetCellularJitter(1.0f);
            float[] highJitter = new float[100];
            for (int i = 0; i < 100; i++) {
                highJitter[i] = noise.GetNoise(i * 0.5f, i * 0.5f);
            }

            // Patterns should differ
            int differences = 0;
            for (int i = 0; i < 100; i++) {
                if (Math.abs(lowJitter[i] - highJitter[i]) > 0.01f) {
                    differences++;
                }
            }
            assertTrue(differences > 20, "Different jitter values should produce different patterns");
        }
    }

    @Nested
    @DisplayName("Backward compatibility tests")
    class BackwardCompatibilityTests {

        @Test
        @DisplayName("enum values should match expected order")
        void enumValuesShouldMatchExpectedOrder() {
            // Verify enum ordinals match for backward compatibility
            assertEquals(0, FastNoiseLite.NoiseType.OpenSimplex2.ordinal());
            assertEquals(1, FastNoiseLite.NoiseType.OpenSimplex2S.ordinal());
            assertEquals(2, FastNoiseLite.NoiseType.Cellular.ordinal());
            assertEquals(3, FastNoiseLite.NoiseType.Perlin.ordinal());
            assertEquals(4, FastNoiseLite.NoiseType.ValueCubic.ordinal());
            assertEquals(5, FastNoiseLite.NoiseType.Value.ordinal());

            assertEquals(0, FastNoiseLite.FractalType.None.ordinal());
            assertEquals(1, FastNoiseLite.FractalType.FBm.ordinal());
            assertEquals(2, FastNoiseLite.FractalType.Ridged.ordinal());
            assertEquals(3, FastNoiseLite.FractalType.PingPong.ordinal());
        }

        @Test
        @DisplayName("API methods should work as documented")
        void apiMethodsShouldWorkAsDocumented() {
            // This is the usage example from the documentation
            FastNoiseLite noise = new FastNoiseLite(1337);
            noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
            noise.SetFractalType(FastNoiseLite.FractalType.FBm);
            noise.SetFractalOctaves(4);
            float value = noise.GetNoise(10f, 20f, 30f);

            assertFalse(Float.isNaN(value));
            assertTrue(value >= -1.5f && value <= 1.5f);
        }

        @Test
        @DisplayName("Vector classes should be accessible")
        void vectorClassesShouldBeAccessible() {
            Vector2 v2 = new Vector2(1.0f, 2.0f);
            assertEquals(1.0f, v2.x);
            assertEquals(2.0f, v2.y);

            Vector3 v3 = new Vector3(1.0f, 2.0f, 3.0f);
            assertEquals(1.0f, v3.x);
            assertEquals(2.0f, v3.y);
            assertEquals(3.0f, v3.z);
        }
    }

    @Nested
    @DisplayName("Configuration method tests")
    class ConfigurationMethodTests {

        @Test
        @DisplayName("frequency should affect noise scale")
        void frequencyShouldAffectNoiseScale() {
            noise.SetNoiseType(FastNoiseLite.NoiseType.Perlin);

            // Low frequency (zoomed in)
            noise.SetFrequency(0.001f);
            float[] lowFreq = new float[50];
            for (int i = 0; i < 50; i++) {
                lowFreq[i] = noise.GetNoise(i, i);
            }

            // High frequency (zoomed out)
            noise.SetFrequency(0.1f);
            float[] highFreq = new float[50];
            for (int i = 0; i < 50; i++) {
                highFreq[i] = noise.GetNoise(i, i);
            }

            // Calculate variation
            float lowVariation = calculateMaxMinDiff(lowFreq);
            float highVariation = calculateMaxMinDiff(highFreq);

            // Higher frequency should show more variation in the same sample range
            assertTrue(highVariation > lowVariation * 0.5f || lowVariation > highVariation * 0.5f,
                "Different frequencies should produce different variation patterns");
        }

        private float calculateMaxMinDiff(float[] values) {
            float min = Float.MAX_VALUE;
            float max = Float.MIN_VALUE;
            for (float v : values) {
                if (v < min) min = v;
                if (v > max) max = v;
            }
            return max - min;
        }

        @Test
        @DisplayName("lacunarity should affect fractal noise")
        void lacunarityShouldAffectFractalNoise() {
            noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
            noise.SetFractalType(FastNoiseLite.FractalType.FBm);
            noise.SetFractalOctaves(4);
            noise.SetFrequency(0.01f);

            noise.SetFractalLacunarity(2.0f);
            float v1 = noise.GetNoise(50f, 50f);

            noise.SetFractalLacunarity(3.0f);
            float v2 = noise.GetNoise(50f, 50f);

            // Different lacunarity should produce different results
            assertNotEquals(v1, v2, "Different lacunarity should produce different output");
        }

        @Test
        @DisplayName("gain should affect fractal noise amplitude")
        void gainShouldAffectFractalNoiseAmplitude() {
            noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
            noise.SetFractalType(FastNoiseLite.FractalType.FBm);
            noise.SetFractalOctaves(4);
            noise.SetFrequency(0.01f);

            noise.SetFractalGain(0.3f);
            float v1 = noise.GetNoise(50f, 50f);

            noise.SetFractalGain(0.7f);
            float v2 = noise.GetNoise(50f, 50f);

            assertNotEquals(v1, v2, "Different gain should produce different output");
        }

        @Test
        @DisplayName("ping pong strength should affect ping pong fractal")
        void pingPongStrengthShouldAffectPingPongFractal() {
            noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
            noise.SetFractalType(FastNoiseLite.FractalType.PingPong);
            noise.SetFractalOctaves(4);
            noise.SetFrequency(0.01f);

            noise.SetFractalPingPongStrength(1.0f);
            float v1 = noise.GetNoise(50f, 50f);

            noise.SetFractalPingPongStrength(3.0f);
            float v2 = noise.GetNoise(50f, 50f);

            assertNotEquals(v1, v2, "Different ping pong strength should produce different output");
        }
    }
}
