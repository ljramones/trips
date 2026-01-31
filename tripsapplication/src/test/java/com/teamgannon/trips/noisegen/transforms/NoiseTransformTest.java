package com.teamgannon.trips.noisegen.transforms;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for noise transform classes.
 */
class NoiseTransformTest {

    @Nested
    @DisplayName("RangeTransform tests")
    class RangeTransformTests {

        @Test
        @DisplayName("default constructor should map [-1,1] to [0,1]")
        void defaultConstructorShouldMapToZeroOne() {
            RangeTransform transform = new RangeTransform();

            assertEquals(0.0f, transform.apply(-1.0f), 0.0001f);
            assertEquals(0.5f, transform.apply(0.0f), 0.0001f);
            assertEquals(1.0f, transform.apply(1.0f), 0.0001f);
        }

        @Test
        @DisplayName("custom range mapping should work correctly")
        void customRangeMappingShouldWorkCorrectly() {
            RangeTransform transform = new RangeTransform(-1f, 1f, 0f, 255f);

            assertEquals(0.0f, transform.apply(-1.0f), 0.0001f);
            assertEquals(127.5f, transform.apply(0.0f), 0.0001f);
            assertEquals(255.0f, transform.apply(1.0f), 0.0001f);
        }

        @Test
        @DisplayName("normalize factory should create [0,1] transform")
        void normalizeFactoryShouldCreateZeroOneTransform() {
            RangeTransform transform = RangeTransform.normalize();

            assertEquals(0.0f, transform.apply(-1.0f), 0.0001f);
            assertEquals(1.0f, transform.apply(1.0f), 0.0001f);
        }

        @Test
        @DisplayName("toMax factory should scale correctly")
        void toMaxFactoryShouldScaleCorrectly() {
            RangeTransform transform = RangeTransform.toMax(100f);

            assertEquals(0.0f, transform.apply(-1.0f), 0.0001f);
            assertEquals(100.0f, transform.apply(1.0f), 0.0001f);
        }

        @Test
        @DisplayName("description should include range info")
        void descriptionShouldIncludeRangeInfo() {
            RangeTransform transform = new RangeTransform(-1f, 1f, 0f, 1f);
            assertTrue(transform.getDescription().contains("RangeTransform"));
        }
    }

    @Nested
    @DisplayName("PowerTransform tests")
    class PowerTransformTests {

        @Test
        @DisplayName("exponent 1.0 should not change values significantly")
        void exponentOneShouldNotChangeValues() {
            PowerTransform transform = new PowerTransform(1.0f);

            assertEquals(0.0f, transform.apply(0.0f), 0.01f);
            // At boundaries, values should be close
            assertTrue(Math.abs(transform.apply(0.5f) - 0.5f) < 0.1f);
        }

        @Test
        @DisplayName("exponent 2.0 should sharpen features")
        void exponentTwoShouldSharpenFeatures() {
            PowerTransform transform = new PowerTransform(2.0f);

            // Middle values should become smaller (closer to extremes)
            float original = 0.0f; // maps to 0.5 normalized
            float transformed = transform.apply(original);
            // After power 2 on normalized 0.5 -> 0.25, then back to signed -> -0.5
            assertTrue(transformed < original,
                    "Power transform should reduce middle values");
        }

        @Test
        @DisplayName("unsigned output should stay in [0,1]")
        void unsignedOutputShouldStayInZeroOne() {
            PowerTransform transform = new PowerTransform(2.0f, false);

            for (float input = -1.0f; input <= 1.0f; input += 0.1f) {
                float output = transform.apply(input);
                assertTrue(output >= 0.0f && output <= 1.0f,
                        String.format("Output %.4f should be in [0,1] for input %.4f", output, input));
            }
        }

        @Test
        @DisplayName("getExponent should return correct value")
        void getExponentShouldReturnCorrectValue() {
            PowerTransform transform = new PowerTransform(3.5f);
            assertEquals(3.5f, transform.getExponent(), 0.0001f);
        }
    }

    @Nested
    @DisplayName("RidgeTransform tests")
    class RidgeTransformTests {

        @Test
        @DisplayName("standard ridge should use absolute value")
        void standardRidgeShouldUseAbsoluteValue() {
            RidgeTransform transform = new RidgeTransform();

            assertEquals(0.5f, transform.apply(0.5f), 0.0001f);
            assertEquals(0.5f, transform.apply(-0.5f), 0.0001f);
            assertEquals(0.0f, transform.apply(0.0f), 0.0001f);
        }

        @Test
        @DisplayName("inverted ridge should create valleys")
        void invertedRidgeShouldCreateValleys() {
            RidgeTransform transform = new RidgeTransform(true);

            // Inversion: 1.0 - abs(value)
            assertEquals(0.5f, transform.apply(0.5f), 0.0001f);
            assertEquals(0.5f, transform.apply(-0.5f), 0.0001f);
            assertEquals(1.0f, transform.apply(0.0f), 0.0001f); // Valley at zero
        }

        @Test
        @DisplayName("ridge with power should sharpen features")
        void ridgeWithPowerShouldSharpenFeatures() {
            RidgeTransform standard = new RidgeTransform(false, 1.0f);
            RidgeTransform sharp = new RidgeTransform(false, 2.0f);

            float input = 0.5f;
            float standardOutput = standard.apply(input);
            float sharpOutput = sharp.apply(input);

            // Power 2 should make 0.5 -> 0.25
            assertTrue(sharpOutput < standardOutput,
                    "Sharp ridge should have smaller values in middle");
        }

        @Test
        @DisplayName("output should be in [0,1] range")
        void outputShouldBeInZeroOneRange() {
            RidgeTransform transform = new RidgeTransform();

            for (float input = -1.0f; input <= 1.0f; input += 0.1f) {
                float output = transform.apply(input);
                assertTrue(output >= 0.0f && output <= 1.0f,
                        String.format("Output %.4f should be in [0,1] for input %.4f", output, input));
            }
        }
    }

    @Nested
    @DisplayName("TurbulenceTransform tests")
    class TurbulenceTransformTests {

        @Test
        @DisplayName("standard turbulence should use absolute value")
        void standardTurbulenceShouldUseAbsoluteValue() {
            TurbulenceTransform transform = new TurbulenceTransform();

            assertEquals(0.5f, transform.apply(0.5f), 0.0001f);
            assertEquals(0.5f, transform.apply(-0.5f), 0.0001f);
            assertEquals(0.0f, transform.apply(0.0f), 0.0001f);
        }

        @Test
        @DisplayName("scale and offset should be applied")
        void scaleAndOffsetShouldBeApplied() {
            TurbulenceTransform transform = new TurbulenceTransform(2.0f, 0.1f);

            // |0.5| * 2.0 + 0.1 = 1.1
            assertEquals(1.1f, transform.apply(0.5f), 0.0001f);
            assertEquals(1.1f, transform.apply(-0.5f), 0.0001f);
        }
    }

    @Nested
    @DisplayName("ClampTransform tests")
    class ClampTransformTests {

        @Test
        @DisplayName("default clamp should use [-1,1] range")
        void defaultClampShouldUseStandardRange() {
            ClampTransform transform = new ClampTransform();

            assertEquals(-1.0f, transform.apply(-2.0f), 0.0001f);
            assertEquals(1.0f, transform.apply(2.0f), 0.0001f);
            assertEquals(0.5f, transform.apply(0.5f), 0.0001f);
        }

        @Test
        @DisplayName("custom clamp should work correctly")
        void customClampShouldWorkCorrectly() {
            ClampTransform transform = new ClampTransform(0f, 1f);

            assertEquals(0.0f, transform.apply(-0.5f), 0.0001f);
            assertEquals(1.0f, transform.apply(1.5f), 0.0001f);
            assertEquals(0.5f, transform.apply(0.5f), 0.0001f);
        }

        @Test
        @DisplayName("getMin and getMax should return correct values")
        void getMinAndMaxShouldReturnCorrectValues() {
            ClampTransform transform = new ClampTransform(-2f, 3f);
            assertEquals(-2f, transform.getMin(), 0.0001f);
            assertEquals(3f, transform.getMax(), 0.0001f);
        }
    }

    @Nested
    @DisplayName("InvertTransform tests")
    class InvertTransformTests {

        @Test
        @DisplayName("should negate values")
        void shouldNegateValues() {
            InvertTransform transform = new InvertTransform();

            assertEquals(-0.5f, transform.apply(0.5f), 0.0001f);
            assertEquals(0.5f, transform.apply(-0.5f), 0.0001f);
            assertEquals(0.0f, transform.apply(0.0f), 0.0001f);
        }

        @Test
        @DisplayName("double inversion should restore original")
        void doubleInversionShouldRestoreOriginal() {
            InvertTransform transform = new InvertTransform();

            float original = 0.75f;
            float inverted = transform.apply(original);
            float restored = transform.apply(inverted);

            assertEquals(original, restored, 0.0001f);
        }
    }

    @Nested
    @DisplayName("ChainedTransform tests")
    class ChainedTransformTests {

        @Test
        @DisplayName("empty chain should return input unchanged")
        void emptyChainShouldReturnInputUnchanged() {
            ChainedTransform chain = new ChainedTransform();

            assertEquals(0.5f, chain.apply(0.5f), 0.0001f);
            assertEquals(-0.75f, chain.apply(-0.75f), 0.0001f);
        }

        @Test
        @DisplayName("single transform chain should work")
        void singleTransformChainShouldWork() {
            ChainedTransform chain = new ChainedTransform(new InvertTransform());

            assertEquals(-0.5f, chain.apply(0.5f), 0.0001f);
        }

        @Test
        @DisplayName("multiple transforms should be applied in order")
        void multipleTransformsShouldBeAppliedInOrder() {
            // Chain: ridge -> invert
            ChainedTransform chain = new ChainedTransform(
                    new RidgeTransform(),
                    new InvertTransform()
            );

            // Input 0.5 -> ridge |0.5| = 0.5 -> invert -0.5
            assertEquals(-0.5f, chain.apply(0.5f), 0.0001f);
            // Input -0.5 -> ridge |-0.5| = 0.5 -> invert -0.5
            assertEquals(-0.5f, chain.apply(-0.5f), 0.0001f);
        }

        @Test
        @DisplayName("add should append transform to chain")
        void addShouldAppendTransformToChain() {
            ChainedTransform chain = new ChainedTransform(new RidgeTransform());
            assertEquals(1, chain.size());

            chain.add(new InvertTransform());
            assertEquals(2, chain.size());

            // Verify the chain works
            assertEquals(-0.5f, chain.apply(0.5f), 0.0001f);
        }

        @Test
        @DisplayName("prepend should add transform at beginning")
        void prependShouldAddTransformAtBeginning() {
            ChainedTransform chain = new ChainedTransform(new RidgeTransform());
            chain.prepend(new InvertTransform());

            // Order: invert -> ridge
            // Input 0.5 -> invert -0.5 -> ridge |-0.5| = 0.5
            assertEquals(0.5f, chain.apply(0.5f), 0.0001f);
        }

        @Test
        @DisplayName("complex chain should work correctly")
        void complexChainShouldWorkCorrectly() {
            // Chain: ridge -> range [0,1] to [0,255]
            ChainedTransform chain = new ChainedTransform(
                    new RidgeTransform(),
                    new RangeTransform(0f, 1f, 0f, 255f)
            );

            // Input -1.0 -> ridge 1.0 -> range 255
            assertEquals(255.0f, chain.apply(-1.0f), 0.5f);
            // Input 0.0 -> ridge 0.0 -> range 0
            assertEquals(0.0f, chain.apply(0.0f), 0.5f);
        }

        @Test
        @DisplayName("description should show chain contents")
        void descriptionShouldShowChainContents() {
            ChainedTransform chain = new ChainedTransform(
                    new RidgeTransform(),
                    new InvertTransform()
            );

            String desc = chain.getDescription();
            assertTrue(desc.contains("ChainedTransform"));
            assertTrue(desc.contains("RidgeTransform"));
            assertTrue(desc.contains("InvertTransform"));
            assertTrue(desc.contains("->"));
        }

        @Test
        @DisplayName("isEmpty should work correctly")
        void isEmptyShouldWorkCorrectly() {
            ChainedTransform empty = new ChainedTransform();
            assertTrue(empty.isEmpty());

            ChainedTransform notEmpty = new ChainedTransform(new InvertTransform());
            assertFalse(notEmpty.isEmpty());
        }
    }

    @Nested
    @DisplayName("Integration tests")
    class IntegrationTests {

        @Test
        @DisplayName("terrain generation pipeline")
        void terrainGenerationPipeline() {
            // Typical terrain pipeline: ridge -> power -> range to [0, 100]
            ChainedTransform terrain = new ChainedTransform(
                    new RidgeTransform(true),  // Valleys at zero crossings
                    new PowerTransform(1.5f, false),  // Sharpen slightly
                    new RangeTransform(0f, 1f, 0f, 100f)  // Height in meters
            );

            // Test a few noise values
            float height1 = terrain.apply(0.0f);  // At zero crossing (valley)
            float height2 = terrain.apply(0.8f);  // Near edge

            // Zero crossing should be high (inverted ridge valley = peak)
            assertTrue(height1 > height2,
                    "Zero crossing should be higher than edges for inverted ridge");
        }

        @Test
        @DisplayName("color mapping pipeline")
        void colorMappingPipeline() {
            // Map noise to grayscale color value
            ChainedTransform color = new ChainedTransform(
                    RangeTransform.normalize(),  // [-1,1] -> [0,1]
                    new RangeTransform(0f, 1f, 0f, 255f)  // [0,1] -> [0,255]
            );

            assertEquals(0.0f, color.apply(-1.0f), 0.5f);
            assertEquals(127.5f, color.apply(0.0f), 0.5f);
            assertEquals(255.0f, color.apply(1.0f), 0.5f);
        }
    }
}
