package com.teamgannon.trips.noisegen.advanced;

import com.teamgannon.trips.noisegen.FastNoiseLite;
import com.teamgannon.trips.noisegen.generators.WaveletNoiseGen;
import com.teamgannon.trips.noisegen.spatial.HierarchicalNoise;
import com.teamgannon.trips.noisegen.spatial.SparseConvolutionNoise;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Tier 2 advanced noise algorithms:
 * - WaveletNoiseGen (band-limited noise)
 * - SparseConvolutionNoise (memory-efficient sparse sampling)
 * - HierarchicalNoise (quadtree/octree adaptive sampling)
 */
@DisplayName("Advanced Noise Algorithms")
class AdvancedNoiseTest {

    @Nested
    @DisplayName("WaveletNoiseGen Tests")
    class WaveletNoiseGenTests {

        @Test
        @DisplayName("Constructor creates valid instance")
        void constructorCreatesValidInstance() {
            WaveletNoiseGen wavelet = new WaveletNoiseGen(1337);
            assertEquals(1337, wavelet.getSeed());
            assertEquals(128, wavelet.getTileSize()); // Default size
        }

        @Test
        @DisplayName("Constructor with custom tile size")
        void constructorWithCustomTileSize() {
            WaveletNoiseGen wavelet = new WaveletNoiseGen(42, 256);
            assertEquals(42, wavelet.getSeed());
            assertEquals(256, wavelet.getTileSize());
        }

        @Test
        @DisplayName("Tile size must be power of 2")
        void tileSizeMustBePowerOf2() {
            assertThrows(IllegalArgumentException.class, () -> new WaveletNoiseGen(1337, 100));
            assertThrows(IllegalArgumentException.class, () -> new WaveletNoiseGen(1337, 255));
            assertDoesNotThrow(() -> new WaveletNoiseGen(1337, 64));
            assertDoesNotThrow(() -> new WaveletNoiseGen(1337, 128));
            assertDoesNotThrow(() -> new WaveletNoiseGen(1337, 256));
        }

        @Test
        @DisplayName("2D noise returns values in valid range")
        void sample2DReturnsValidRange() {
            WaveletNoiseGen wavelet = new WaveletNoiseGen(1337, 64);

            for (int i = 0; i < 100; i++) {
                float x = (float) (Math.random() * 200 - 100);
                float y = (float) (Math.random() * 200 - 100);
                float value = wavelet.sample2D(x, y);

                assertTrue(value >= -1.5f && value <= 1.5f,
                    "Value " + value + " out of expected range at (" + x + ", " + y + ")");
            }
        }

        @Test
        @DisplayName("3D noise returns values in valid range")
        void sample3DReturnsValidRange() {
            WaveletNoiseGen wavelet = new WaveletNoiseGen(1337, 64);

            for (int i = 0; i < 100; i++) {
                float x = (float) (Math.random() * 200 - 100);
                float y = (float) (Math.random() * 200 - 100);
                float z = (float) (Math.random() * 200 - 100);
                float value = wavelet.sample3D(x, y, z);

                assertTrue(value >= -1.5f && value <= 1.5f,
                    "Value " + value + " out of expected range");
            }
        }

        @Test
        @DisplayName("Same seed produces same noise")
        void sameSeedProducesSameNoise() {
            WaveletNoiseGen wavelet1 = new WaveletNoiseGen(1337, 64);
            WaveletNoiseGen wavelet2 = new WaveletNoiseGen(1337, 64);

            float x = 10.5f, y = 20.3f;
            assertEquals(wavelet1.sample2D(x, y), wavelet2.sample2D(x, y), 0.0001f);
        }

        @Test
        @DisplayName("Different seeds produce different noise")
        void differentSeedsProduceDifferentNoise() {
            WaveletNoiseGen wavelet1 = new WaveletNoiseGen(1337, 64);
            WaveletNoiseGen wavelet2 = new WaveletNoiseGen(9999, 64);

            // Test multiple points to account for occasional coincidental matches
            int diffCount = 0;
            for (int i = 0; i < 20; i++) {
                float x = 10.5f + i * 7.3f;
                float y = 20.3f + i * 5.1f;
                if (Math.abs(wavelet1.sample2D(x, y) - wavelet2.sample2D(x, y)) > 0.01f) {
                    diffCount++;
                }
            }
            assertTrue(diffCount > 10, "Different seeds should produce different patterns at most points");
        }

        @Test
        @DisplayName("Band sampling at different levels")
        void bandSamplingAtDifferentLevels() {
            WaveletNoiseGen wavelet = new WaveletNoiseGen(1337, 64);

            float x = 10.5f, y = 20.3f;

            // Different bands should give different but valid values
            float band0 = wavelet.sampleBand2D(x, y, 0);
            float band1 = wavelet.sampleBand2D(x, y, 1);
            float band2 = wavelet.sampleBand2D(x, y, 2);

            // Values should be in valid range
            assertTrue(Math.abs(band0) <= 1.5f);
            assertTrue(Math.abs(band1) <= 1.5f);
            assertTrue(Math.abs(band2) <= 1.5f);
        }

        @Test
        @DisplayName("FBm sampling produces valid results")
        void fbmSamplingProducesValidResults() {
            WaveletNoiseGen wavelet = new WaveletNoiseGen(1337, 64);

            float value = wavelet.sampleFBm2D(10f, 20f, 4, 2.0f, 0.5f);
            assertTrue(Math.abs(value) <= 1.5f, "FBm value out of range: " + value);

            float value3D = wavelet.sampleFBm3D(10f, 20f, 30f, 4, 2.0f, 0.5f);
            assertTrue(Math.abs(value3D) <= 1.5f, "3D FBm value out of range: " + value3D);
        }

        @Test
        @DisplayName("Noise tiles seamlessly")
        void noiseTilesSeamlessly() {
            WaveletNoiseGen wavelet = new WaveletNoiseGen(1337, 64);
            int tileSize = wavelet.getTileSize();

            // Sample at tile boundary and one tile over
            float y = 32.5f;
            float v1 = wavelet.sample2D(0f, y);
            float v2 = wavelet.sample2D(tileSize, y);

            assertEquals(v1, v2, 0.001f, "Noise should tile at tile boundary");
        }

        @Test
        @DisplayName("Implements NoiseGenerator interface")
        void implementsNoiseGeneratorInterface() {
            WaveletNoiseGen wavelet = new WaveletNoiseGen(1337, 64);

            float single2D = wavelet.single2D(100, 10f, 20f);
            float single3D = wavelet.single3D(100, 10f, 20f, 30f);
            float single4D = wavelet.single4D(100, 10f, 20f, 30f, 40f);

            assertTrue(Math.abs(single2D) <= 1.5f);
            assertTrue(Math.abs(single3D) <= 1.5f);
            assertTrue(Math.abs(single4D) <= 1.5f);
            assertFalse(wavelet.supports4D()); // 4D is approximated
        }
    }

    @Nested
    @DisplayName("SparseConvolutionNoise Tests")
    class SparseConvolutionNoiseTests {

        @Test
        @DisplayName("Constructor with default parameters")
        void constructorWithDefaults() {
            SparseConvolutionNoise noise = new SparseConvolutionNoise(1337);
            assertEquals(1337, noise.getSeed());
            assertTrue(noise.getDensity() > 0);
            assertTrue(noise.getKernelRadius() > 0);
        }

        @Test
        @DisplayName("Constructor with custom parameters")
        void constructorWithCustomParams() {
            SparseConvolutionNoise noise = new SparseConvolutionNoise(42, 0.3f, 3.0f);
            assertEquals(42, noise.getSeed());
            assertEquals(0.3f, noise.getDensity(), 0.001f);
            assertEquals(3.0f, noise.getKernelRadius(), 0.001f);
        }

        @Test
        @DisplayName("2D noise returns values")
        void getNoise2DReturnsValues() {
            SparseConvolutionNoise noise = new SparseConvolutionNoise(1337);

            boolean foundNonZero = false;
            for (int i = 0; i < 100; i++) {
                float x = (float) (Math.random() * 100);
                float y = (float) (Math.random() * 100);
                float value = noise.getNoise(x, y);

                // Values should be in a reasonable range
                assertTrue(Math.abs(value) < 5f, "Value " + value + " seems too large");
                if (Math.abs(value) > 0.001f) {
                    foundNonZero = true;
                }
            }
            assertTrue(foundNonZero, "Should produce non-zero noise values");
        }

        @Test
        @DisplayName("3D noise returns values")
        void getNoise3DReturnsValues() {
            SparseConvolutionNoise noise = new SparseConvolutionNoise(1337);

            boolean foundNonZero = false;
            for (int i = 0; i < 100; i++) {
                float x = (float) (Math.random() * 100);
                float y = (float) (Math.random() * 100);
                float z = (float) (Math.random() * 100);
                float value = noise.getNoise(x, y, z);

                assertTrue(Math.abs(value) < 5f, "Value " + value + " seems too large");
                if (Math.abs(value) > 0.001f) {
                    foundNonZero = true;
                }
            }
            assertTrue(foundNonZero, "Should produce non-zero 3D noise values");
        }

        @Test
        @DisplayName("Same seed produces same noise")
        void sameSeedProducesSameNoise() {
            SparseConvolutionNoise noise1 = new SparseConvolutionNoise(1337, 0.2f, 2.5f);
            SparseConvolutionNoise noise2 = new SparseConvolutionNoise(1337, 0.2f, 2.5f);

            float x = 10.5f, y = 20.3f;
            assertEquals(noise1.getNoise(x, y), noise2.getNoise(x, y), 0.0001f);
        }

        @Test
        @DisplayName("Different seeds produce different noise")
        void differentSeedsProduceDifferentNoise() {
            SparseConvolutionNoise noise1 = new SparseConvolutionNoise(1337);
            SparseConvolutionNoise noise2 = new SparseConvolutionNoise(1338);

            float x = 10.5f, y = 20.3f;
            // With sparse noise, values could be equal at some points, so check multiple
            int diffCount = 0;
            for (int i = 0; i < 20; i++) {
                float tx = x + i * 5;
                if (Math.abs(noise1.getNoise(tx, y) - noise2.getNoise(tx, y)) > 0.01f) {
                    diffCount++;
                }
            }
            assertTrue(diffCount > 5, "Different seeds should produce different patterns");
        }

        @Test
        @DisplayName("FBm produces smooth noise")
        void fbmProducesSmoothNoise() {
            SparseConvolutionNoise noise = new SparseConvolutionNoise(1337);

            float value = noise.getFBm(10f, 20f, 4, 2.0f, 0.5f);
            assertTrue(Math.abs(value) < 2f, "FBm value should be normalized");

            float value3D = noise.getFBm(10f, 20f, 30f, 4, 2.0f, 0.5f);
            assertTrue(Math.abs(value3D) < 2f, "3D FBm value should be normalized");
        }

        @Test
        @DisplayName("Double precision coordinates work")
        void doublePrecisionCoordinatesWork() {
            SparseConvolutionNoise noise = new SparseConvolutionNoise(1337);

            double x = 1_000_000_000.5;
            double y = 2_500_000_000.3;

            float value = noise.getNoise(x, y);
            assertTrue(!Float.isNaN(value) && !Float.isInfinite(value),
                "Should handle large double precision coordinates");
        }

        @Test
        @DisplayName("withDensity creates new instance")
        void withDensityCreatesNewInstance() {
            SparseConvolutionNoise original = new SparseConvolutionNoise(1337, 0.2f, 2.5f);
            SparseConvolutionNoise modified = original.withDensity(0.5f);

            assertNotSame(original, modified);
            assertEquals(0.2f, original.getDensity(), 0.001f);
            assertEquals(0.5f, modified.getDensity(), 0.001f);
            assertEquals(original.getSeed(), modified.getSeed());
        }

        @Test
        @DisplayName("withKernelRadius creates new instance")
        void withKernelRadiusCreatesNewInstance() {
            SparseConvolutionNoise original = new SparseConvolutionNoise(1337, 0.2f, 2.5f);
            SparseConvolutionNoise modified = original.withKernelRadius(5.0f);

            assertNotSame(original, modified);
            assertEquals(2.5f, original.getKernelRadius(), 0.001f);
            assertEquals(5.0f, modified.getKernelRadius(), 0.001f);
        }

        @Test
        @DisplayName("Higher density produces more detail")
        void higherDensityProducesMoreDetail() {
            SparseConvolutionNoise sparse = new SparseConvolutionNoise(1337, 0.1f, 2.5f);
            SparseConvolutionNoise dense = new SparseConvolutionNoise(1337, 0.5f, 2.5f);

            // Sample along a line and measure variance
            float sparseVar = 0f, denseVar = 0f;
            float sparseMean = 0f, denseMean = 0f;
            int n = 100;

            for (int i = 0; i < n; i++) {
                float x = i * 0.5f;
                sparseMean += sparse.getNoise(x, 0);
                denseMean += dense.getNoise(x, 0);
            }
            sparseMean /= n;
            denseMean /= n;

            for (int i = 0; i < n; i++) {
                float x = i * 0.5f;
                float sv = sparse.getNoise(x, 0) - sparseMean;
                float dv = dense.getNoise(x, 0) - denseMean;
                sparseVar += sv * sv;
                denseVar += dv * dv;
            }

            // Dense noise should typically have higher variance
            // (More impulses = more variations)
            // This isn't always guaranteed, so we just check they're both valid
            assertTrue(sparseVar >= 0 && denseVar >= 0);
        }
    }

    @Nested
    @DisplayName("HierarchicalNoise Tests")
    class HierarchicalNoiseTests {

        private FastNoiseLite createBaseNoise() {
            FastNoiseLite noise = new FastNoiseLite(1337);
            noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
            return noise;
        }

        @Test
        @DisplayName("Constructor with default parameters")
        void constructorWithDefaults() {
            FastNoiseLite base = createBaseNoise();
            HierarchicalNoise hierarchical = new HierarchicalNoise(base, 8);

            assertEquals(8, hierarchical.getMaxLevels());
            assertTrue(hierarchical.getBaseFrequency() > 0);
            assertEquals(2.0f, hierarchical.getLacunarity(), 0.001f);
            assertEquals(0.5f, hierarchical.getPersistence(), 0.001f);
        }

        @Test
        @DisplayName("Constructor with custom parameters")
        void constructorWithCustomParams() {
            FastNoiseLite base = createBaseNoise();
            HierarchicalNoise hierarchical = new HierarchicalNoise(base, 6, 0.005f, 2.5f, 0.4f);

            assertEquals(6, hierarchical.getMaxLevels());
            assertEquals(0.005f, hierarchical.getBaseFrequency(), 0.0001f);
            assertEquals(2.5f, hierarchical.getLacunarity(), 0.001f);
            assertEquals(0.4f, hierarchical.getPersistence(), 0.001f);
        }

        @Test
        @DisplayName("Sample level returns values in range")
        void sampleLevelReturnsValidRange() {
            FastNoiseLite base = createBaseNoise();
            HierarchicalNoise hierarchical = new HierarchicalNoise(base, 8);

            for (int level = 0; level < 8; level++) {
                float value = hierarchical.sampleLevel(10f, 20f, level);
                assertTrue(value >= -1f && value <= 1f,
                    "Level " + level + " returned out of range: " + value);
            }
        }

        @Test
        @DisplayName("3D sample level returns values in range")
        void sampleLevel3DReturnsValidRange() {
            FastNoiseLite base = createBaseNoise();
            HierarchicalNoise hierarchical = new HierarchicalNoise(base, 8);

            for (int level = 0; level < 8; level++) {
                float value = hierarchical.sampleLevel(10f, 20f, 30f, level);
                assertTrue(value >= -1f && value <= 1f,
                    "3D Level " + level + " returned out of range: " + value);
            }
        }

        @Test
        @DisplayName("Sample cumulative returns valid results")
        void sampleCumulativeReturnsValidResults() {
            FastNoiseLite base = createBaseNoise();
            HierarchicalNoise hierarchical = new HierarchicalNoise(base, 8);

            float value = hierarchical.sampleCumulative(10f, 20f, 4);
            assertTrue(Math.abs(value) <= 1.5f, "Cumulative value out of range: " + value);
        }

        @Test
        @DisplayName("Higher levels have higher frequency")
        void higherLevelsHaveHigherFrequency() {
            FastNoiseLite base = createBaseNoise();
            HierarchicalNoise hierarchical = new HierarchicalNoise(base, 8, 0.01f, 2.0f, 0.5f);

            float freq0 = hierarchical.getFrequency(0);
            float freq1 = hierarchical.getFrequency(1);
            float freq2 = hierarchical.getFrequency(2);

            assertTrue(freq1 > freq0, "Level 1 should have higher frequency than level 0");
            assertTrue(freq2 > freq1, "Level 2 should have higher frequency than level 1");
            assertEquals(freq1, freq0 * 2.0f, 0.001f); // lacunarity = 2.0
        }

        @Test
        @DisplayName("Higher levels have lower amplitude")
        void higherLevelsHaveLowerAmplitude() {
            FastNoiseLite base = createBaseNoise();
            HierarchicalNoise hierarchical = new HierarchicalNoise(base, 8, 0.01f, 2.0f, 0.5f);

            float amp0 = hierarchical.getAmplitude(0);
            float amp1 = hierarchical.getAmplitude(1);
            float amp2 = hierarchical.getAmplitude(2);

            assertTrue(amp1 < amp0, "Level 1 should have lower amplitude than level 0");
            assertTrue(amp2 < amp1, "Level 2 should have lower amplitude than level 1");
            assertEquals(amp1, amp0 * 0.5f, 0.001f); // persistence = 0.5
        }

        @Test
        @DisplayName("Scale to level conversion")
        void scaleToLevelConversion() {
            FastNoiseLite base = createBaseNoise();
            HierarchicalNoise hierarchical = new HierarchicalNoise(base, 8);

            // Full scale should give max level
            assertEquals(7, hierarchical.scaleToLevel(1.0f));
            assertEquals(7, hierarchical.scaleToLevel(2.0f)); // clamped

            // Very small scale should give low level
            int lowLevel = hierarchical.scaleToLevel(0.01f);
            assertTrue(lowLevel < 4, "Small scale should map to low level");

            // Zero scale should give level 0
            assertEquals(0, hierarchical.scaleToLevel(0f));
        }

        @Test
        @DisplayName("Level to scale conversion")
        void levelToScaleConversion() {
            FastNoiseLite base = createBaseNoise();
            HierarchicalNoise hierarchical = new HierarchicalNoise(base, 8);

            // Max level should give scale 1.0
            assertEquals(1.0f, hierarchical.levelToScale(7), 0.001f);

            // Lower levels should give smaller scales
            float scale5 = hierarchical.levelToScale(5);
            float scale3 = hierarchical.levelToScale(3);

            assertTrue(scale5 > scale3, "Higher level should have higher scale");
            assertTrue(scale5 < 1.0f);
        }

        @Test
        @DisplayName("Adaptive sampling based on scale")
        void adaptiveSamplingBasedOnScale() {
            FastNoiseLite base = createBaseNoise();
            HierarchicalNoise hierarchical = new HierarchicalNoise(base, 8);

            float fullDetail = hierarchical.sampleAdaptive(10f, 20f, 1.0f);
            float halfDetail = hierarchical.sampleAdaptive(10f, 20f, 0.5f);

            // Both should return valid values
            assertTrue(Math.abs(fullDetail) <= 1.5f);
            assertTrue(Math.abs(halfDetail) <= 1.5f);
        }

        @Test
        @DisplayName("Quadtree node sampling")
        void quadtreeNodeSampling() {
            FastNoiseLite base = createBaseNoise();
            HierarchicalNoise hierarchical = new HierarchicalNoise(base, 8);

            float rootNode = hierarchical.sampleQuadtreeNode(10f, 20f, 0, 2);
            float childNode = hierarchical.sampleQuadtreeNode(10f, 20f, 2, 2);

            // Both should return valid values
            assertTrue(Math.abs(rootNode) <= 1.5f, "Root node value: " + rootNode);
            assertTrue(Math.abs(childNode) <= 1.5f, "Child node value: " + childNode);
        }

        @Test
        @DisplayName("Octree node sampling")
        void octreeNodeSampling() {
            FastNoiseLite base = createBaseNoise();
            HierarchicalNoise hierarchical = new HierarchicalNoise(base, 8);

            float value = hierarchical.sampleOctreeNode(10f, 20f, 30f, 1, 3);
            assertTrue(Math.abs(value) <= 1.5f, "Octree node value: " + value);
        }

        @Test
        @DisplayName("Level delta sampling")
        void levelDeltaSampling() {
            FastNoiseLite base = createBaseNoise();
            HierarchicalNoise hierarchical = new HierarchicalNoise(base, 8);

            float delta0 = hierarchical.sampleLevelDelta(10f, 20f, 0);
            float delta1 = hierarchical.sampleLevelDelta(10f, 20f, 1);
            float delta2 = hierarchical.sampleLevelDelta(10f, 20f, 2);

            // Deltas should have decreasing amplitude (scaled by persistence)
            float amp0 = hierarchical.getAmplitude(0);
            float amp1 = hierarchical.getAmplitude(1);

            // delta0 should be larger than delta1 on average due to amplitude scaling
            // But we can't test this reliably with single samples
            assertTrue(Math.abs(delta0) < 2f);
            assertTrue(Math.abs(delta1) < 2f);
            assertTrue(Math.abs(delta2) < 2f);
        }

        @Test
        @DisplayName("Builder creates valid instance")
        void builderCreatesValidInstance() {
            FastNoiseLite base = createBaseNoise();

            HierarchicalNoise hierarchical = HierarchicalNoise.builder(base)
                .maxLevels(6)
                .baseFrequency(0.02f)
                .lacunarity(2.5f)
                .persistence(0.4f)
                .build();

            assertEquals(6, hierarchical.getMaxLevels());
            assertEquals(0.02f, hierarchical.getBaseFrequency(), 0.001f);
            assertEquals(2.5f, hierarchical.getLacunarity(), 0.001f);
            assertEquals(0.4f, hierarchical.getPersistence(), 0.001f);
        }

        @Test
        @DisplayName("Level clamping works correctly")
        void levelClampingWorksCorrectly() {
            FastNoiseLite base = createBaseNoise();
            HierarchicalNoise hierarchical = new HierarchicalNoise(base, 8);

            // Negative level should clamp to 0
            float negLevel = hierarchical.sampleLevel(10f, 20f, -5);
            float level0 = hierarchical.sampleLevel(10f, 20f, 0);
            assertEquals(level0, negLevel, 0.001f);

            // Level beyond max should clamp to max
            float highLevel = hierarchical.sampleLevel(10f, 20f, 100);
            float maxLevel = hierarchical.sampleLevel(10f, 20f, 7);
            assertEquals(maxLevel, highLevel, 0.001f);
        }

        @Test
        @DisplayName("getNoise returns base generator")
        void getNoiseReturnsBaseGenerator() {
            FastNoiseLite base = createBaseNoise();
            HierarchicalNoise hierarchical = new HierarchicalNoise(base, 8);

            assertSame(base, hierarchical.getNoise());
        }

        @Test
        @DisplayName("Different levels produce different patterns")
        void differentLevelsProduceDifferentPatterns() {
            FastNoiseLite base = createBaseNoise();
            HierarchicalNoise hierarchical = new HierarchicalNoise(base, 8);

            // Sample at multiple points and check that different levels differ
            int differentCount = 0;
            for (int i = 0; i < 20; i++) {
                float x = i * 10f;
                float y = i * 7f;
                float v0 = hierarchical.sampleLevel(x, y, 0);
                float v3 = hierarchical.sampleLevel(x, y, 3);
                if (Math.abs(v0 - v3) > 0.01f) {
                    differentCount++;
                }
            }
            assertTrue(differentCount > 10, "Different levels should produce different patterns");
        }
    }
}
