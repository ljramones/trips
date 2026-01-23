package com.teamgannon.trips.stellarmodelling;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for StellarFactory.
 */
class StellarFactoryTest {

    private StellarFactory factory;

    @BeforeEach
    void setUp() {
        factory = StellarFactory.getFactory();
    }

    @Nested
    @DisplayName("Factory singleton tests")
    class FactorySingletonTests {

        @Test
        @DisplayName("getFactory should return non-null factory")
        void getFactoryShouldReturnNonNullFactory() {
            StellarFactory factory = StellarFactory.getFactory();

            assertNotNull(factory);
        }

        @Test
        @DisplayName("getFactory should reset and reinitialize")
        void getFactoryShouldResetAndReinitialize() {
            StellarFactory factory1 = StellarFactory.getFactory();
            StellarFactory factory2 = StellarFactory.getFactory();

            // Both should work correctly
            assertNotNull(factory1.getStellarClass("G"));
            assertNotNull(factory2.getStellarClass("G"));
        }
    }

    @Nested
    @DisplayName("GetStellarClass tests")
    class GetStellarClassTests {

        @ParameterizedTest
        @ValueSource(strings = {"O", "B", "A", "F", "G", "K", "M"})
        @DisplayName("should return classification for main sequence types")
        void shouldReturnClassificationForMainSequenceTypes(String type) {
            StellarClassification classification = factory.getStellarClass(type);

            assertNotNull(classification);
            assertEquals(StellarType.valueOf(type), classification.getStellarType());
        }

        @ParameterizedTest
        @ValueSource(strings = {"L", "T", "Y"})
        @DisplayName("should return classification for brown dwarf types")
        void shouldReturnClassificationForBrownDwarfTypes(String type) {
            StellarClassification classification = factory.getStellarClass(type);

            assertNotNull(classification);
            assertEquals(StellarType.valueOf(type), classification.getStellarType());
        }

        @ParameterizedTest
        @ValueSource(strings = {"D", "DA", "DB", "DO", "DQ", "DZ", "DC", "DX"})
        @DisplayName("should return classification for white dwarf types")
        void shouldReturnClassificationForWhiteDwarfTypes(String type) {
            StellarClassification classification = factory.getStellarClass(type);

            assertNotNull(classification);
            assertEquals(StellarType.valueOf(type), classification.getStellarType());
        }

        @ParameterizedTest
        @ValueSource(strings = {"WN", "WC", "WO"})
        @DisplayName("should return classification for Wolf-Rayet types")
        void shouldReturnClassificationForWolfRayetTypes(String type) {
            StellarClassification classification = factory.getStellarClass(type);

            assertNotNull(classification);
            assertEquals(StellarType.valueOf(type), classification.getStellarType());
        }

        @Test
        @DisplayName("should return null for unknown type")
        void shouldReturnNullForUnknownType() {
            StellarClassification classification = factory.getStellarClass("INVALID");

            assertNull(classification);
        }
    }

    @Nested
    @DisplayName("Classes method tests")
    class ClassesMethodTests {

        @ParameterizedTest
        @ValueSource(strings = {"O", "B", "A", "F", "G", "K", "M", "L", "T", "Y"})
        @DisplayName("should return true for valid classes")
        void shouldReturnTrueForValidClasses(String type) {
            assertTrue(factory.classes(type));
        }

        @Test
        @DisplayName("should return false for invalid class")
        void shouldReturnFalseForInvalidClass() {
            assertFalse(factory.classes("INVALID"));
        }

        @Test
        @DisplayName("should return false for null class")
        void shouldReturnFalseForNullClass() {
            assertFalse(factory.classes(null));
        }
    }

    @Nested
    @DisplayName("CreateFromData tests")
    class CreateFromDataTests {

        @Test
        @DisplayName("should create classification from data")
        void shouldCreateClassificationFromData() {
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

            StellarClassification classification = StellarFactory.createFromData(data);

            assertNotNull(classification);
            assertEquals(StellarType.G, classification.getStellarType());
            assertEquals(StarColor.G, classification.getStarColor());
            assertEquals("yellow", classification.getColor());
            assertEquals("yellowish white", classification.getChromacity());
            assertEquals(5200, classification.getLowerTemperature());
            assertEquals(6000, classification.getUpperTemperature());
            assertEquals(0.8, classification.getLowerMass(), 0.001);
            assertEquals(1.04, classification.getUpperMass(), 0.001);
            assertEquals(0.96, classification.getLowerRadius(), 0.001);
            assertEquals(1.15, classification.getUpperRadius(), 0.001);
            assertEquals(0.6, classification.getLowerLuminosity(), 0.001);
            assertEquals(1.5, classification.getUpperLuminosity(), 0.001);
            assertEquals(HydrogenLines.WEAK, classification.getLines());
            assertEquals(7.6, classification.getSequenceFraction(), 0.001);
        }

        @Test
        @DisplayName("should set chromaticity color from registry")
        void shouldSetChromaticityColorFromRegistry() {
            StellarTypeData data = StellarTypeData.builder()
                    .stellarType(StellarType.G)
                    .starColor(StarColor.G)
                    .chromaticityKey("G")
                    .build();

            StellarClassification classification = StellarFactory.createFromData(data);

            assertNotNull(classification.getStellarChromaticity());
            // G type color is 255,244,232
            Color expectedColor = StellarTypeRegistry.getChromaticityColor("G");
            assertEquals(expectedColor, classification.getStellarChromaticity());
        }
    }

    @Nested
    @DisplayName("Legacy factory method tests")
    class LegacyFactoryMethodTests {

        @Test
        @DisplayName("createOClass should return O classification")
        void createOClassShouldReturnOClassification() {
            StellarClassification classification = StellarFactory.createOClass();

            assertNotNull(classification);
            assertEquals(StellarType.O, classification.getStellarType());
            assertEquals(StarColor.O, classification.getStarColor());
            assertEquals("blue", classification.getColor());
        }

        @Test
        @DisplayName("createBClass should return B classification")
        void createBClassShouldReturnBClassification() {
            StellarClassification classification = StellarFactory.createBClass();

            assertNotNull(classification);
            assertEquals(StellarType.B, classification.getStellarType());
            assertEquals(StarColor.B, classification.getStarColor());
        }

        @Test
        @DisplayName("createAClass should return A classification")
        void createAClassShouldReturnAClassification() {
            StellarClassification classification = StellarFactory.createAClass();

            assertNotNull(classification);
            assertEquals(StellarType.A, classification.getStellarType());
            assertEquals(StarColor.A, classification.getStarColor());
        }

        @Test
        @DisplayName("createFClass should return F classification")
        void createFClassShouldReturnFClassification() {
            StellarClassification classification = StellarFactory.createFClass();

            assertNotNull(classification);
            assertEquals(StellarType.F, classification.getStellarType());
            assertEquals(StarColor.F, classification.getStarColor());
        }

        @Test
        @DisplayName("createGClass should return G classification")
        void createGClassShouldReturnGClassification() {
            StellarClassification classification = StellarFactory.createGClass();

            assertNotNull(classification);
            assertEquals(StellarType.G, classification.getStellarType());
            assertEquals(StarColor.G, classification.getStarColor());
            assertEquals("yellow", classification.getColor());
        }

        @Test
        @DisplayName("createKClass should return K classification")
        void createKClassShouldReturnKClassification() {
            StellarClassification classification = StellarFactory.createKClass();

            assertNotNull(classification);
            assertEquals(StellarType.K, classification.getStellarType());
            assertEquals(StarColor.K, classification.getStarColor());
        }

        @Test
        @DisplayName("createMClass should return M classification")
        void createMClassShouldReturnMClassification() {
            StellarClassification classification = StellarFactory.createMClass();

            assertNotNull(classification);
            assertEquals(StellarType.M, classification.getStellarType());
            assertEquals(StarColor.M, classification.getStarColor());
            assertEquals("red", classification.getColor());
        }
    }

    @Nested
    @DisplayName("Classification property consistency tests")
    class ClassificationPropertyConsistencyTests {

        @Test
        @DisplayName("factory and legacy methods should produce equivalent results")
        void factoryAndLegacyMethodsShouldProduceEquivalentResults() {
            StellarClassification fromFactory = factory.getStellarClass("G");
            StellarClassification fromLegacy = StellarFactory.createGClass();

            assertEquals(fromFactory.getStellarType(), fromLegacy.getStellarType());
            assertEquals(fromFactory.getStarColor(), fromLegacy.getStarColor());
            assertEquals(fromFactory.getColor(), fromLegacy.getColor());
            assertEquals(fromFactory.getLowerTemperature(), fromLegacy.getLowerTemperature());
            assertEquals(fromFactory.getUpperTemperature(), fromLegacy.getUpperTemperature());
        }

        @Test
        @DisplayName("all classifications should have chromaticity color set")
        void allClassificationsShouldHaveChromaticityColorSet() {
            String[] types = {"O", "B", "A", "F", "G", "K", "M"};

            for (String type : types) {
                StellarClassification classification = factory.getStellarClass(type);
                assertNotNull(classification.getStellarChromaticity(),
                        "Type " + type + " should have chromaticity color");
            }
        }

        @Test
        @DisplayName("temperature ranges should be valid")
        void temperatureRangesShouldBeValid() {
            String[] types = {"O", "B", "A", "F", "G", "K", "M"};

            for (String type : types) {
                StellarClassification classification = factory.getStellarClass(type);
                assertTrue(classification.getLowerTemperature() > 0,
                        "Type " + type + " should have positive lower temperature");
                assertTrue(classification.getUpperTemperature() > classification.getLowerTemperature(),
                        "Type " + type + " upper temp should be > lower temp");
            }
        }
    }

    @Nested
    @DisplayName("Backwards compatibility tests")
    class BackwardsCompatibilityTests {

        @Test
        @DisplayName("G class should have Sun-like properties")
        void gClassShouldHaveSunLikeProperties() {
            StellarClassification gClass = factory.getStellarClass("G");

            // Verify Sun-like properties
            assertEquals("yellow", gClass.getColor());
            assertTrue(gClass.getLowerMass() <= 1.0 && gClass.getUpperMass() >= 1.0,
                    "G class mass range should include 1.0 solar masses");
            assertTrue(gClass.getLowerRadius() <= 1.0 && gClass.getUpperRadius() >= 1.0,
                    "G class radius range should include 1.0 solar radii");
        }

        @Test
        @DisplayName("O class should be hottest main sequence type")
        void oClassShouldBeHottestMainSequenceType() {
            StellarClassification oClass = factory.getStellarClass("O");
            StellarClassification bClass = factory.getStellarClass("B");

            assertTrue(oClass.getLowerTemperature() > bClass.getLowerTemperature());
            assertTrue(oClass.getUpperTemperature() > bClass.getUpperTemperature());
        }

        @Test
        @DisplayName("M class should have highest sequence fraction")
        void mClassShouldHaveHighestSequenceFraction() {
            StellarClassification mClass = factory.getStellarClass("M");
            StellarClassification gClass = factory.getStellarClass("G");

            assertTrue(mClass.getSequenceFraction() > gClass.getSequenceFraction());
            assertTrue(mClass.getSequenceFraction() > 70, "M class should be ~76% of main sequence");
        }
    }
}
