package com.teamgannon.trips.stellarmodelling;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for StellarTypeData record and builder.
 */
class StellarTypeDataTest {

    @Nested
    @DisplayName("Builder tests")
    class BuilderTests {

        @Test
        @DisplayName("should create data with all fields set")
        void shouldCreateDataWithAllFieldsSet() {
            StellarTypeData data = StellarTypeData.builder()
                    .stellarType(StellarType.G)
                    .starColor(StarColor.G)
                    .chromaticityKey("G")
                    .color("yellow")
                    .chromacity("yellowish white")
                    .temperatureRange(5200, 6000)
                    .massRange(0.8, 1.04)
                    .radiusRange(0.96, 1.15)
                    .luminosityRange(0.6, 1.5)
                    .hydrogenLines(HydrogenLines.WEAK)
                    .sequenceFraction(7.6)
                    .build();

            assertEquals(StellarType.G, data.stellarType());
            assertEquals(StarColor.G, data.starColor());
            assertEquals("G", data.chromaticityKey());
            assertEquals("yellow", data.color());
            assertEquals("yellowish white", data.chromacity());
            assertEquals(5200, data.lowerTemperature());
            assertEquals(6000, data.upperTemperature());
            assertEquals(0.8, data.lowerMass(), 0.001);
            assertEquals(1.04, data.upperMass(), 0.001);
            assertEquals(0.96, data.lowerRadius(), 0.001);
            assertEquals(1.15, data.upperRadius(), 0.001);
            assertEquals(0.6, data.lowerLuminosity(), 0.001);
            assertEquals(1.5, data.upperLuminosity(), 0.001);
            assertEquals(HydrogenLines.WEAK, data.hydrogenLines());
            assertEquals(7.6, data.sequenceFraction(), 0.001);
        }

        @Test
        @DisplayName("should allow chaining builder methods")
        void shouldAllowChainingBuilderMethods() {
            StellarTypeData.Builder builder = StellarTypeData.builder();

            // Verify chaining returns the same builder
            assertSame(builder, builder.stellarType(StellarType.O));
            assertSame(builder, builder.starColor(StarColor.O));
            assertSame(builder, builder.chromaticityKey("O"));
            assertSame(builder, builder.color("blue"));
            assertSame(builder, builder.chromacity("blue"));
            assertSame(builder, builder.temperatureRange(30000, 70000));
            assertSame(builder, builder.massRange(16.0, 400));
            assertSame(builder, builder.radiusRange(6.6, 45.0));
            assertSame(builder, builder.luminosityRange(30000.0, 10000000.0));
            assertSame(builder, builder.hydrogenLines(HydrogenLines.WEAK));
            assertSame(builder, builder.sequenceFraction(0.00003));
        }

        @Test
        @DisplayName("should create new builder each time")
        void shouldCreateNewBuilderEachTime() {
            StellarTypeData.Builder builder1 = StellarTypeData.builder();
            StellarTypeData.Builder builder2 = StellarTypeData.builder();

            assertNotSame(builder1, builder2);
        }

        @Test
        @DisplayName("should handle null values")
        void shouldHandleNullValues() {
            StellarTypeData data = StellarTypeData.builder()
                    .stellarType(null)
                    .starColor(null)
                    .chromaticityKey(null)
                    .color(null)
                    .chromacity(null)
                    .hydrogenLines(null)
                    .build();

            assertNull(data.stellarType());
            assertNull(data.starColor());
            assertNull(data.chromaticityKey());
            assertNull(data.color());
            assertNull(data.chromacity());
            assertNull(data.hydrogenLines());
        }
    }

    @Nested
    @DisplayName("Record equality tests")
    class RecordEqualityTests {

        @Test
        @DisplayName("should be equal when all fields match")
        void shouldBeEqualWhenAllFieldsMatch() {
            StellarTypeData data1 = createTestData();
            StellarTypeData data2 = createTestData();

            assertEquals(data1, data2);
            assertEquals(data1.hashCode(), data2.hashCode());
        }

        @Test
        @DisplayName("should not be equal when fields differ")
        void shouldNotBeEqualWhenFieldsDiffer() {
            StellarTypeData data1 = StellarTypeData.builder()
                    .stellarType(StellarType.G)
                    .starColor(StarColor.G)
                    .build();

            StellarTypeData data2 = StellarTypeData.builder()
                    .stellarType(StellarType.K)
                    .starColor(StarColor.K)
                    .build();

            assertNotEquals(data1, data2);
        }

        private StellarTypeData createTestData() {
            return StellarTypeData.builder()
                    .stellarType(StellarType.G)
                    .starColor(StarColor.G)
                    .chromaticityKey("G")
                    .color("yellow")
                    .chromacity("yellowish white")
                    .temperatureRange(5200, 6000)
                    .massRange(0.8, 1.04)
                    .radiusRange(0.96, 1.15)
                    .luminosityRange(0.6, 1.5)
                    .hydrogenLines(HydrogenLines.WEAK)
                    .sequenceFraction(7.6)
                    .build();
        }
    }

    @Nested
    @DisplayName("Temperature range tests")
    class TemperatureRangeTests {

        @Test
        @DisplayName("should store temperature range correctly")
        void shouldStoreTemperatureRangeCorrectly() {
            StellarTypeData data = StellarTypeData.builder()
                    .temperatureRange(3700, 5200)
                    .build();

            assertEquals(3700, data.lowerTemperature());
            assertEquals(5200, data.upperTemperature());
        }

        @Test
        @DisplayName("should handle extreme temperature values")
        void shouldHandleExtremeTemperatureValues() {
            StellarTypeData data = StellarTypeData.builder()
                    .temperatureRange(500, 200000)
                    .build();

            assertEquals(500, data.lowerTemperature());
            assertEquals(200000, data.upperTemperature());
        }
    }

    @Nested
    @DisplayName("Mass range tests")
    class MassRangeTests {

        @Test
        @DisplayName("should store mass range correctly")
        void shouldStoreMassRangeCorrectly() {
            StellarTypeData data = StellarTypeData.builder()
                    .massRange(0.08, 400)
                    .build();

            assertEquals(0.08, data.lowerMass(), 0.001);
            assertEquals(400, data.upperMass(), 0.001);
        }

        @Test
        @DisplayName("should handle very small mass values")
        void shouldHandleVerySmallMassValues() {
            StellarTypeData data = StellarTypeData.builder()
                    .massRange(0.040, 0.065)
                    .build();

            assertEquals(0.040, data.lowerMass(), 0.0001);
            assertEquals(0.065, data.upperMass(), 0.0001);
        }
    }

    @Nested
    @DisplayName("Luminosity range tests")
    class LuminosityRangeTests {

        @Test
        @DisplayName("should store luminosity range correctly")
        void shouldStoreLuminosityRangeCorrectly() {
            StellarTypeData data = StellarTypeData.builder()
                    .luminosityRange(0.001, 10000000.0)
                    .build();

            assertEquals(0.001, data.lowerLuminosity(), 0.0001);
            assertEquals(10000000.0, data.upperLuminosity(), 0.001);
        }

        @Test
        @DisplayName("should handle very small luminosity values")
        void shouldHandleVerySmallLuminosityValues() {
            StellarTypeData data = StellarTypeData.builder()
                    .luminosityRange(0.00001, 0.00006)
                    .build();

            assertEquals(0.00001, data.lowerLuminosity(), 0.000001);
            assertEquals(0.00006, data.upperLuminosity(), 0.000001);
        }
    }
}
