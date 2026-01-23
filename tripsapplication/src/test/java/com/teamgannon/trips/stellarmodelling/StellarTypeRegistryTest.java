package com.teamgannon.trips.stellarmodelling;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for StellarTypeRegistry.
 */
class StellarTypeRegistryTest {

    @Nested
    @DisplayName("Registry access tests")
    class RegistryAccessTests {

        @Test
        @DisplayName("should return data for valid stellar type")
        void shouldReturnDataForValidStellarType() {
            StellarTypeData data = StellarTypeRegistry.get("G");

            assertNotNull(data);
            assertEquals(StellarType.G, data.stellarType());
        }

        @Test
        @DisplayName("should return null for unknown stellar type")
        void shouldReturnNullForUnknownStellarType() {
            StellarTypeData data = StellarTypeRegistry.get("INVALID");

            assertNull(data);
        }

        @Test
        @DisplayName("should contain all main sequence types")
        void shouldContainAllMainSequenceTypes() {
            assertTrue(StellarTypeRegistry.contains("O"));
            assertTrue(StellarTypeRegistry.contains("B"));
            assertTrue(StellarTypeRegistry.contains("A"));
            assertTrue(StellarTypeRegistry.contains("F"));
            assertTrue(StellarTypeRegistry.contains("G"));
            assertTrue(StellarTypeRegistry.contains("K"));
            assertTrue(StellarTypeRegistry.contains("M"));
        }

        @Test
        @DisplayName("should contain brown dwarf types")
        void shouldContainBrownDwarfTypes() {
            assertTrue(StellarTypeRegistry.contains("L"));
            assertTrue(StellarTypeRegistry.contains("T"));
            assertTrue(StellarTypeRegistry.contains("Y"));
        }

        @Test
        @DisplayName("should contain white dwarf types")
        void shouldContainWhiteDwarfTypes() {
            assertTrue(StellarTypeRegistry.contains("D"));
            assertTrue(StellarTypeRegistry.contains("DA"));
            assertTrue(StellarTypeRegistry.contains("DB"));
            assertTrue(StellarTypeRegistry.contains("DO"));
            assertTrue(StellarTypeRegistry.contains("DQ"));
            assertTrue(StellarTypeRegistry.contains("DZ"));
            assertTrue(StellarTypeRegistry.contains("DC"));
            assertTrue(StellarTypeRegistry.contains("DX"));
        }

        @Test
        @DisplayName("should contain Wolf-Rayet types")
        void shouldContainWolfRayetTypes() {
            assertTrue(StellarTypeRegistry.contains("WN"));
            assertTrue(StellarTypeRegistry.contains("WC"));
            assertTrue(StellarTypeRegistry.contains("WO"));
        }

        @Test
        @DisplayName("should contain special types")
        void shouldContainSpecialTypes() {
            assertTrue(StellarTypeRegistry.contains("C"));
            assertTrue(StellarTypeRegistry.contains("S"));
            assertTrue(StellarTypeRegistry.contains("Q"));
            assertTrue(StellarTypeRegistry.contains("Unk"));
        }
    }

    @Nested
    @DisplayName("GetAllTypes tests")
    class GetAllTypesTests {

        @Test
        @DisplayName("should return all registered types")
        void shouldReturnAllRegisteredTypes() {
            Set<String> types = StellarTypeRegistry.getAllTypes();

            assertNotNull(types);
            assertFalse(types.isEmpty());
            assertTrue(types.size() >= 25); // At least 25 types registered
        }

        @Test
        @DisplayName("should return unmodifiable set")
        void shouldReturnUnmodifiableSet() {
            Set<String> types = StellarTypeRegistry.getAllTypes();

            assertThrows(UnsupportedOperationException.class, () -> types.add("NEW"));
        }
    }

    @Nested
    @DisplayName("GetAllData tests")
    class GetAllDataTests {

        @Test
        @DisplayName("should return all registered data")
        void shouldReturnAllRegisteredData() {
            Collection<StellarTypeData> allData = StellarTypeRegistry.getAllData();

            assertNotNull(allData);
            assertFalse(allData.isEmpty());
        }

        @Test
        @DisplayName("should return unmodifiable collection")
        void shouldReturnUnmodifiableCollection() {
            Collection<StellarTypeData> allData = StellarTypeRegistry.getAllData();

            assertThrows(UnsupportedOperationException.class, () ->
                    allData.add(StellarTypeData.builder().build()));
        }

        @Test
        @DisplayName("all data should have stellar type set")
        void allDataShouldHaveStellarTypeSet() {
            Collection<StellarTypeData> allData = StellarTypeRegistry.getAllData();

            for (StellarTypeData data : allData) {
                assertNotNull(data.stellarType(), "Each data entry should have a stellar type");
            }
        }
    }

    @Nested
    @DisplayName("Chromaticity map tests")
    class ChromaticityMapTests {

        @Test
        @DisplayName("should return chromaticity map")
        void shouldReturnChromaticityMap() {
            Map<String, String> map = StellarTypeRegistry.getChromaticityMap();

            assertNotNull(map);
            assertFalse(map.isEmpty());
        }

        @Test
        @DisplayName("should return unmodifiable chromaticity map")
        void shouldReturnUnmodifiableChromaticityMap() {
            Map<String, String> map = StellarTypeRegistry.getChromaticityMap();

            assertThrows(UnsupportedOperationException.class, () -> map.put("NEW", "255,255,255"));
        }

        @Test
        @DisplayName("chromaticity map should contain main types")
        void chromaticityMapShouldContainMainTypes() {
            Map<String, String> map = StellarTypeRegistry.getChromaticityMap();

            assertTrue(map.containsKey("O"));
            assertTrue(map.containsKey("G"));
            assertTrue(map.containsKey("M"));
            assertTrue(map.containsKey("DA"));
            assertTrue(map.containsKey("WN"));
        }
    }

    @Nested
    @DisplayName("Color parsing tests")
    class ColorParsingTests {

        @Test
        @DisplayName("should parse valid RGB string")
        void shouldParseValidRgbString() {
            Color color = StellarTypeRegistry.parseColor("255,128,64");

            assertEquals(1.0, color.getRed(), 0.01);
            assertEquals(0.5, color.getGreen(), 0.01);
            assertEquals(0.25, color.getBlue(), 0.01);
        }

        @Test
        @DisplayName("should return fallback for invalid RGB string")
        void shouldReturnFallbackForInvalidRgbString() {
            Color color = StellarTypeRegistry.parseColor("invalid");

            assertEquals(Color.MEDIUMVIOLETRED, color);
        }

        @Test
        @DisplayName("should return fallback for partial RGB string")
        void shouldReturnFallbackForPartialRgbString() {
            Color color = StellarTypeRegistry.parseColor("255,128");

            assertEquals(Color.MEDIUMVIOLETRED, color);
        }

        @Test
        @DisplayName("should return fallback for non-numeric RGB values")
        void shouldReturnFallbackForNonNumericRgbValues() {
            Color color = StellarTypeRegistry.parseColor("red,green,blue");

            assertEquals(Color.MEDIUMVIOLETRED, color);
        }
    }

    @Nested
    @DisplayName("GetChromaticityColor tests")
    class GetChromaticityColorTests {

        @Test
        @DisplayName("should return color for known type")
        void shouldReturnColorForKnownType() {
            Color color = StellarTypeRegistry.getChromaticityColor("G");

            assertNotNull(color);
            // G type should be yellowish (255,244,232)
            assertTrue(color.getRed() > 0.9);
            assertTrue(color.getGreen() > 0.9);
            assertTrue(color.getBlue() > 0.8);
        }

        @Test
        @DisplayName("should return unknown color for invalid type")
        void shouldReturnUnknownColorForInvalidType() {
            Color knownUnknown = StellarTypeRegistry.getChromaticityColor("Unknown");
            Color invalidType = StellarTypeRegistry.getChromaticityColor("INVALID_TYPE");

            assertEquals(knownUnknown, invalidType);
        }

        @ParameterizedTest
        @ValueSource(strings = {"O", "B", "A", "F", "G", "K", "M"})
        @DisplayName("should return valid color for main sequence types")
        void shouldReturnValidColorForMainSequenceTypes(String type) {
            Color color = StellarTypeRegistry.getChromaticityColor(type);

            assertNotNull(color);
            assertTrue(color.getRed() >= 0 && color.getRed() <= 1);
            assertTrue(color.getGreen() >= 0 && color.getGreen() <= 1);
            assertTrue(color.getBlue() >= 0 && color.getBlue() <= 1);
        }
    }

    @Nested
    @DisplayName("Constant data validation tests")
    class ConstantDataValidationTests {

        @Test
        @DisplayName("O class should have correct properties")
        void oClassShouldHaveCorrectProperties() {
            StellarTypeData data = StellarTypeRegistry.O_CLASS;

            assertEquals(StellarType.O, data.stellarType());
            assertEquals(StarColor.O, data.starColor());
            assertEquals("blue", data.color());
            assertTrue(data.upperTemperature() >= 30000);
            assertEquals(HydrogenLines.WEAK, data.hydrogenLines());
        }

        @Test
        @DisplayName("G class should have Sun-like properties")
        void gClassShouldHaveSunLikeProperties() {
            StellarTypeData data = StellarTypeRegistry.G_CLASS;

            assertEquals(StellarType.G, data.stellarType());
            assertEquals("yellow", data.color());
            assertTrue(data.lowerTemperature() >= 5000 && data.lowerTemperature() <= 5500);
            assertTrue(data.upperTemperature() >= 5800 && data.upperTemperature() <= 6200);
            // Sun is about 1 solar mass
            assertTrue(data.lowerMass() <= 1.0 && data.upperMass() >= 1.0);
        }

        @Test
        @DisplayName("M class should be most common")
        void mClassShouldBeMostCommon() {
            StellarTypeData data = StellarTypeRegistry.M_CLASS;

            assertEquals(StellarType.M, data.stellarType());
            assertEquals("red", data.color());
            // M class stars are about 76% of main sequence
            assertTrue(data.sequenceFraction() > 70);
        }

        @Test
        @DisplayName("white dwarf types should have high temperature range")
        void whiteDwarfTypesShouldHaveHighTemperatureRange() {
            StellarTypeData da = StellarTypeRegistry.DA_CLASS;
            StellarTypeData db = StellarTypeRegistry.DB_CLASS;

            assertTrue(da.upperTemperature() >= 40000);
            assertTrue(db.upperTemperature() >= 40000);
            assertEquals("white", da.color());
            assertEquals("white", db.color());
        }

        @Test
        @DisplayName("Wolf-Rayet types should be hot and luminous")
        void wolfRayetTypesShouldBeHotAndLuminous() {
            StellarTypeData wn = StellarTypeRegistry.WN_CLASS;
            StellarTypeData wc = StellarTypeRegistry.WC_CLASS;

            assertTrue(wn.upperTemperature() >= 100000);
            assertTrue(wc.upperTemperature() >= 100000);
            assertTrue(wn.upperLuminosity() >= 100000);
            assertTrue(wc.upperLuminosity() >= 100000);
        }

        @Test
        @DisplayName("brown dwarf types should be cool")
        void brownDwarfTypesShouldBeCool() {
            StellarTypeData l = StellarTypeRegistry.L_CLASS;
            StellarTypeData t = StellarTypeRegistry.T_CLASS;
            StellarTypeData y = StellarTypeRegistry.Y_CLASS;

            assertTrue(l.upperTemperature() <= 2500);
            assertTrue(t.upperTemperature() <= 1500);
            assertTrue(y.upperTemperature() <= 800);
        }
    }

    @Nested
    @DisplayName("Temperature ordering tests")
    class TemperatureOrderingTests {

        @Test
        @DisplayName("main sequence types should have decreasing temperature O to M")
        void mainSequenceTypesShouldHaveDecreasingTemperature() {
            StellarTypeData o = StellarTypeRegistry.O_CLASS;
            StellarTypeData b = StellarTypeRegistry.B_CLASS;
            StellarTypeData a = StellarTypeRegistry.A_CLASS;
            StellarTypeData f = StellarTypeRegistry.F_CLASS;
            StellarTypeData g = StellarTypeRegistry.G_CLASS;
            StellarTypeData k = StellarTypeRegistry.K_CLASS;
            StellarTypeData m = StellarTypeRegistry.M_CLASS;

            assertTrue(o.lowerTemperature() > b.lowerTemperature());
            assertTrue(b.lowerTemperature() > a.lowerTemperature());
            assertTrue(a.lowerTemperature() > f.lowerTemperature());
            assertTrue(f.lowerTemperature() > g.lowerTemperature());
            assertTrue(g.lowerTemperature() > k.lowerTemperature());
            assertTrue(k.lowerTemperature() > m.lowerTemperature());
        }
    }
}
