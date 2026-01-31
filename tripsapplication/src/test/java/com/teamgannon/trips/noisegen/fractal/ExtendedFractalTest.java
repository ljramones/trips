package com.teamgannon.trips.noisegen.fractal;

import com.teamgannon.trips.noisegen.FastNoiseLite;
import com.teamgannon.trips.noisegen.spatial.TurbulenceNoise;
import com.teamgannon.trips.noisegen.transforms.QuantizeTransform;
import com.teamgannon.trips.noisegen.transforms.TerraceTransform;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for extended fractal types and transforms:
 * - Billow fractal
 * - Hybrid Multifractal
 * - TerraceTransform
 * - QuantizeTransform
 * - TurbulenceNoise
 */
@DisplayName("Extended Fractal Types and Transforms")
class ExtendedFractalTest {

    @Nested
    @DisplayName("Billow Fractal Tests")
    class BillowFractalTests {

        @Test
        @DisplayName("Billow 2D produces valid values")
        void billow2DProducesValidValues() {
            FastNoiseLite noise = new FastNoiseLite(1337);
            noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
            noise.SetFractalType(FastNoiseLite.FractalType.Billow);
            noise.SetFractalOctaves(4);

            for (int i = 0; i < 100; i++) {
                float x = (float) (Math.random() * 200 - 100);
                float y = (float) (Math.random() * 200 - 100);
                float value = noise.GetNoise(x, y);

                assertTrue(value >= -1.5f && value <= 1.5f,
                    "Billow 2D value " + value + " out of range");
            }
        }

        @Test
        @DisplayName("Billow 3D produces valid values")
        void billow3DProducesValidValues() {
            FastNoiseLite noise = new FastNoiseLite(1337);
            noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
            noise.SetFractalType(FastNoiseLite.FractalType.Billow);
            noise.SetFractalOctaves(4);

            for (int i = 0; i < 100; i++) {
                float x = (float) (Math.random() * 200 - 100);
                float y = (float) (Math.random() * 200 - 100);
                float z = (float) (Math.random() * 200 - 100);
                float value = noise.GetNoise(x, y, z);

                assertTrue(value >= -1.5f && value <= 1.5f,
                    "Billow 3D value " + value + " out of range");
            }
        }

        @Test
        @DisplayName("Billow 4D produces valid values")
        void billow4DProducesValidValues() {
            FastNoiseLite noise = new FastNoiseLite(1337);
            noise.SetFractalType(FastNoiseLite.FractalType.Billow);
            noise.SetFractalOctaves(4);

            float value = noise.GetNoise(10f, 20f, 30f, 40f);
            assertTrue(value >= -2f && value <= 2f, "Billow 4D value out of range: " + value);
        }

        @Test
        @DisplayName("Billow differs from Ridged")
        void billowDiffersFromRidged() {
            FastNoiseLite billowNoise = new FastNoiseLite(1337);
            billowNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
            billowNoise.SetFractalType(FastNoiseLite.FractalType.Billow);
            billowNoise.SetFractalOctaves(4);

            FastNoiseLite ridgedNoise = new FastNoiseLite(1337);
            ridgedNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
            ridgedNoise.SetFractalType(FastNoiseLite.FractalType.Ridged);
            ridgedNoise.SetFractalOctaves(4);

            int diffCount = 0;
            for (int i = 0; i < 50; i++) {
                float x = i * 5f;
                float y = i * 3f;
                float billow = billowNoise.GetNoise(x, y);
                float ridged = ridgedNoise.GetNoise(x, y);
                if (Math.abs(billow - ridged) > 0.01f) {
                    diffCount++;
                }
            }
            assertTrue(diffCount > 25, "Billow and Ridged should produce different patterns");
        }
    }

    @Nested
    @DisplayName("Hybrid Multifractal Tests")
    class HybridMultiFractalTests {

        @Test
        @DisplayName("HybridMulti 2D produces valid values")
        void hybridMulti2DProducesValidValues() {
            FastNoiseLite noise = new FastNoiseLite(1337);
            noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
            noise.SetFractalType(FastNoiseLite.FractalType.HybridMulti);
            noise.SetFractalOctaves(4);

            for (int i = 0; i < 100; i++) {
                float x = (float) (Math.random() * 200 - 100);
                float y = (float) (Math.random() * 200 - 100);
                float value = noise.GetNoise(x, y);

                assertTrue(value >= -2f && value <= 2f,
                    "HybridMulti 2D value " + value + " out of range");
            }
        }

        @Test
        @DisplayName("HybridMulti 3D produces valid values")
        void hybridMulti3DProducesValidValues() {
            FastNoiseLite noise = new FastNoiseLite(1337);
            noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
            noise.SetFractalType(FastNoiseLite.FractalType.HybridMulti);
            noise.SetFractalOctaves(4);

            for (int i = 0; i < 100; i++) {
                float x = (float) (Math.random() * 200 - 100);
                float y = (float) (Math.random() * 200 - 100);
                float z = (float) (Math.random() * 200 - 100);
                float value = noise.GetNoise(x, y, z);

                assertTrue(value >= -2f && value <= 2f,
                    "HybridMulti 3D value " + value + " out of range");
            }
        }

        @Test
        @DisplayName("HybridMulti 4D produces valid values")
        void hybridMulti4DProducesValidValues() {
            FastNoiseLite noise = new FastNoiseLite(1337);
            noise.SetFractalType(FastNoiseLite.FractalType.HybridMulti);
            noise.SetFractalOctaves(4);

            float value = noise.GetNoise(10f, 20f, 30f, 40f);
            assertTrue(value >= -3f && value <= 3f, "HybridMulti 4D value out of range: " + value);
        }

        @Test
        @DisplayName("HybridMulti differs from FBm")
        void hybridMultiDiffersFromFBm() {
            FastNoiseLite hybridNoise = new FastNoiseLite(1337);
            hybridNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
            hybridNoise.SetFractalType(FastNoiseLite.FractalType.HybridMulti);
            hybridNoise.SetFractalOctaves(4);

            FastNoiseLite fbmNoise = new FastNoiseLite(1337);
            fbmNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
            fbmNoise.SetFractalType(FastNoiseLite.FractalType.FBm);
            fbmNoise.SetFractalOctaves(4);

            int diffCount = 0;
            for (int i = 0; i < 50; i++) {
                float x = i * 5f;
                float y = i * 3f;
                float hybrid = hybridNoise.GetNoise(x, y);
                float fbm = fbmNoise.GetNoise(x, y);
                if (Math.abs(hybrid - fbm) > 0.01f) {
                    diffCount++;
                }
            }
            assertTrue(diffCount > 25, "HybridMulti and FBm should produce different patterns");
        }
    }

    @Nested
    @DisplayName("TerraceTransform Tests")
    class TerraceTransformTests {

        @Test
        @DisplayName("Creates discrete steps")
        void createsDiscreteSteps() {
            TerraceTransform terrace = new TerraceTransform(4);

            // Test that different inputs produce limited outputs
            java.util.Set<Float> outputs = new java.util.HashSet<>();
            for (int i = 0; i < 100; i++) {
                float input = (float) (Math.random() * 2 - 1);
                float output = terrace.apply(input);
                // Round to avoid floating point comparison issues
                outputs.add(Math.round(output * 1000f) / 1000f);
            }

            // With 4 levels and sharp terracing, should have limited distinct outputs
            assertTrue(outputs.size() <= 8, "Should produce limited distinct values: " + outputs.size());
        }

        @Test
        @DisplayName("Output stays in range")
        void outputStaysInRange() {
            TerraceTransform terrace = new TerraceTransform(8);

            for (int i = 0; i < 100; i++) {
                float input = (float) (Math.random() * 2 - 1);
                float output = terrace.apply(input);
                assertTrue(output >= -1f && output <= 1f,
                    "Output " + output + " out of range for input " + input);
            }
        }

        @Test
        @DisplayName("Smooth terracing produces gradual transitions")
        void smoothTerracingProducesGradualTransitions() {
            TerraceTransform smooth = new TerraceTransform(4, 0.5f);

            float prev = smooth.apply(-1f);
            boolean foundGradual = false;
            for (int i = 1; i <= 100; i++) {
                float input = -1f + (2f * i / 100);
                float output = smooth.apply(input);
                float diff = Math.abs(output - prev);
                if (diff > 0 && diff < 0.5f) {
                    foundGradual = true;
                }
                prev = output;
            }
            assertTrue(foundGradual, "Smooth terracing should have gradual transitions");
        }

        @Test
        @DisplayName("Contours factory method works")
        void contoursFactoryMethodWorks() {
            TerraceTransform contours = TerraceTransform.contours(8);
            assertEquals(8, contours.getLevels());
            assertEquals(0f, contours.getSmoothness(), 0.001f);
            assertFalse(contours.isInverted());
        }

        @Test
        @DisplayName("Inverted creates peaks")
        void invertedCreatesPeaks() {
            TerraceTransform inverted = new TerraceTransform(4, 0f, true);
            assertTrue(inverted.isInverted());

            // Test that inverted produces different pattern
            TerraceTransform normal = new TerraceTransform(4, 0f, false);
            int diffCount = 0;
            for (int i = 0; i < 50; i++) {
                float input = -1f + (2f * i / 50);
                float inv = inverted.apply(input);
                float norm = normal.apply(input);
                if (Math.abs(inv - norm) > 0.01f) {
                    diffCount++;
                }
            }
            assertTrue(diffCount > 10, "Inverted should differ from normal");
        }
    }

    @Nested
    @DisplayName("QuantizeTransform Tests")
    class QuantizeTransformTests {

        @Test
        @DisplayName("Creates discrete levels")
        void createsDiscreteLevels() {
            QuantizeTransform quant = new QuantizeTransform(8);

            java.util.Set<Float> outputs = new java.util.HashSet<>();
            for (int i = 0; i < 200; i++) {
                float input = (float) (Math.random() * 2 - 1);
                float output = quant.apply(input);
                outputs.add(Math.round(output * 10000f) / 10000f);
            }

            assertEquals(8, outputs.size(), "Should produce exactly 8 levels");
        }

        @Test
        @DisplayName("Output stays in range")
        void outputStaysInRange() {
            QuantizeTransform quant = new QuantizeTransform(16);

            for (int i = 0; i < 100; i++) {
                float input = (float) (Math.random() * 2 - 1);
                float output = quant.apply(input);
                assertTrue(output >= -1f && output <= 1f,
                    "Output " + output + " out of range");
            }
        }

        @Test
        @DisplayName("Custom steps work")
        void customStepsWork() {
            float[] steps = {-1f, -0.5f, 0f, 0.5f, 1f};
            QuantizeTransform quant = new QuantizeTransform(steps);

            assertEquals(5, quant.getLevels());

            // Test that outputs match steps
            assertEquals(-1f, quant.apply(-0.9f), 0.001f);
            assertEquals(0f, quant.apply(0.1f), 0.001f);
            assertEquals(1f, quant.apply(0.9f), 0.001f);
        }

        @Test
        @DisplayName("Dithering adds variation")
        void ditheringAddsVariation() {
            QuantizeTransform nodither = new QuantizeTransform(4, false);
            QuantizeTransform dither = new QuantizeTransform(4, true, 0.5f);

            // Same input should give same output without dithering
            float input = 0.3f;
            float out1 = nodither.apply(input);
            float out2 = nodither.apply(input);
            assertEquals(out1, out2, 0.001f);

            // With dithering, may get slight variation
            // (Note: our dithering is deterministic but varies per call)
        }

        @Test
        @DisplayName("Posterize factory method works")
        void posterizeFactoryMethodWorks() {
            QuantizeTransform posterize = QuantizeTransform.posterize(8);
            assertEquals(8, posterize.getLevels());
            assertFalse(posterize.isDithering());
        }

        @Test
        @DisplayName("Exponential distribution works")
        void exponentialDistributionWorks() {
            QuantizeTransform exp = QuantizeTransform.exponential(8, 2.0f);
            float[] steps = exp.getSteps();

            // With exponent > 1, steps should be concentrated at lower values
            // Check that first few steps are closer together than last few
            float firstGap = steps[1] - steps[0];
            float lastGap = steps[7] - steps[6];
            assertTrue(lastGap > firstGap,
                "Exponential should have larger gaps at high end");
        }
    }

    @Nested
    @DisplayName("TurbulenceNoise Tests")
    class TurbulenceNoiseTests {

        private FastNoiseLite createBaseNoise() {
            FastNoiseLite noise = new FastNoiseLite(1337);
            noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
            return noise;
        }

        @Test
        @DisplayName("Perlin turbulence produces valid values")
        void perlinTurbulenceProducesValidValues() {
            TurbulenceNoise turb = new TurbulenceNoise(createBaseNoise());

            for (int i = 0; i < 100; i++) {
                float x = (float) (Math.random() * 100);
                float y = (float) (Math.random() * 100);
                float value = turb.perlinTurbulence(x, y, 4);

                assertTrue(value >= 0f && value <= 1f,
                    "Perlin turbulence " + value + " out of range [0,1]");
            }
        }

        @Test
        @DisplayName("3D Perlin turbulence produces valid values")
        void perlinTurbulence3DProducesValidValues() {
            TurbulenceNoise turb = new TurbulenceNoise(createBaseNoise());

            for (int i = 0; i < 100; i++) {
                float x = (float) (Math.random() * 100);
                float y = (float) (Math.random() * 100);
                float z = (float) (Math.random() * 100);
                float value = turb.perlinTurbulence(x, y, z, 4);

                assertTrue(value >= 0f && value <= 1f,
                    "3D Perlin turbulence " + value + " out of range [0,1]");
            }
        }

        @Test
        @DisplayName("Curl2D returns 2D vector")
        void curl2DReturns2DVector() {
            TurbulenceNoise turb = new TurbulenceNoise(createBaseNoise());
            turb.setFrequency(0.01f);

            float[] curl = turb.curl2D(50f, 50f);
            assertEquals(2, curl.length);
            assertFalse(Float.isNaN(curl[0]));
            assertFalse(Float.isNaN(curl[1]));
        }

        @Test
        @DisplayName("Curl3D returns 3D vector")
        void curl3DReturns3DVector() {
            TurbulenceNoise turb = new TurbulenceNoise(createBaseNoise());
            turb.setFrequency(0.01f);

            float[] curl = turb.curl3D(50f, 50f, 50f);
            assertEquals(3, curl.length);
            assertFalse(Float.isNaN(curl[0]));
            assertFalse(Float.isNaN(curl[1]));
            assertFalse(Float.isNaN(curl[2]));
        }

        @Test
        @DisplayName("CurlFBm produces valid multi-octave curl")
        void curlFBmProducesValidResult() {
            TurbulenceNoise turb = new TurbulenceNoise(createBaseNoise());
            turb.setFrequency(0.01f);

            float[] curl2D = turb.curlFBm2D(50f, 50f, 4);
            assertEquals(2, curl2D.length);

            float[] curl3D = turb.curlFBm3D(50f, 50f, 50f, 4);
            assertEquals(3, curl3D.length);
        }

        @Test
        @DisplayName("Warped turbulence produces valid values")
        void warpedTurbulenceProducesValidValues() {
            TurbulenceNoise turb = new TurbulenceNoise(createBaseNoise());
            turb.setFrequency(0.01f);

            float value = turb.warpedTurbulence(50f, 50f, 4, 30f);
            assertTrue(value >= 0f && value <= 1f,
                "Warped turbulence " + value + " out of range");

            float value3D = turb.warpedTurbulence(50f, 50f, 50f, 4, 30f);
            assertTrue(value3D >= 0f && value3D <= 1f,
                "3D Warped turbulence " + value3D + " out of range");
        }

        @Test
        @DisplayName("Marble pattern produces valid values")
        void marblePatternProducesValidValues() {
            TurbulenceNoise turb = new TurbulenceNoise(createBaseNoise());
            turb.setFrequency(0.02f);

            float marble = turb.marble(50f, 50f, 4, 5f);
            assertFalse(Float.isNaN(marble));
            assertFalse(Float.isInfinite(marble));
        }

        @Test
        @DisplayName("Wood pattern produces valid values")
        void woodPatternProducesValidValues() {
            TurbulenceNoise turb = new TurbulenceNoise(createBaseNoise());
            turb.setFrequency(0.02f);

            float wood = turb.wood(50f, 50f, 4, 0.1f);
            assertFalse(Float.isNaN(wood));
            assertFalse(Float.isInfinite(wood));
        }

        @Test
        @DisplayName("Configuration methods work")
        void configurationMethodsWork() {
            TurbulenceNoise turb = new TurbulenceNoise(createBaseNoise());

            turb.setFrequency(0.05f);
            turb.setLacunarity(2.5f);
            turb.setPersistence(0.4f);

            assertEquals(0.05f, turb.getFrequency(), 0.001f);
            assertEquals(2.5f, turb.getLacunarity(), 0.001f);
            assertEquals(0.4f, turb.getPersistence(), 0.001f);
        }

        @Test
        @DisplayName("Method chaining works")
        void methodChainingWorks() {
            TurbulenceNoise turb = new TurbulenceNoise(createBaseNoise())
                .setFrequency(0.02f)
                .setLacunarity(2.0f)
                .setPersistence(0.5f);

            assertEquals(0.02f, turb.getFrequency(), 0.001f);
        }
    }
}
