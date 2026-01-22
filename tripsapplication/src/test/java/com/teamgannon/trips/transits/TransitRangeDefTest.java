package com.teamgannon.trips.transits;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TransitRangeDef model class.
 * Tests cover property access, color conversion, and validation.
 */
class TransitRangeDefTest {

    private TransitRangeDef rangeDef;

    @BeforeEach
    void setUp() {
        rangeDef = new TransitRangeDef();
    }

    // =========================================================================
    // Basic Property Tests
    // =========================================================================

    @Nested
    @DisplayName("Basic Property Tests")
    class BasicPropertyTests {

        @Test
        @DisplayName("bandId can be set and retrieved")
        void bandIdProperty() {
            UUID id = UUID.randomUUID();
            rangeDef.setBandId(id);
            assertEquals(id, rangeDef.getBandId());
        }

        @Test
        @DisplayName("bandName can be set and retrieved")
        void bandNameProperty() {
            rangeDef.setBandName("Test Band");
            assertEquals("Test Band", rangeDef.getBandName());
        }

        @Test
        @DisplayName("enabled flag can be set and retrieved")
        void enabledProperty() {
            rangeDef.setEnabled(true);
            assertTrue(rangeDef.isEnabled());

            rangeDef.setEnabled(false);
            assertFalse(rangeDef.isEnabled());
        }

        @Test
        @DisplayName("upperRange can be set and retrieved")
        void upperRangeProperty() {
            rangeDef.setUpperRange(15.5);
            assertEquals(15.5, rangeDef.getUpperRange(), 0.001);
        }

        @Test
        @DisplayName("lowerRange can be set and retrieved")
        void lowerRangeProperty() {
            rangeDef.setLowerRange(3.2);
            assertEquals(3.2, rangeDef.getLowerRange(), 0.001);
        }

        @Test
        @DisplayName("lineWidth can be set and retrieved")
        void lineWidthProperty() {
            rangeDef.setLineWidth(2.0);
            assertEquals(2.0, rangeDef.getLineWidth(), 0.001);
        }

        @Test
        @DisplayName("color string can be set and retrieved")
        void colorStringProperty() {
            rangeDef.setColor("0xff0000ff");
            assertEquals("0xff0000ff", rangeDef.getColor());
        }
    }

    // =========================================================================
    // Color Conversion Tests
    // =========================================================================

    @Nested
    @DisplayName("Color Conversion Tests")
    class ColorConversionTests {

        @Test
        @DisplayName("setBandColor converts Color to string")
        void setBandColorConvertsToString() {
            rangeDef.setBandColor(Color.RED);

            assertNotNull(rangeDef.getColor());
            assertFalse(rangeDef.getColor().isEmpty());
        }

        @Test
        @DisplayName("getBandColor converts string back to Color")
        void getBandColorConvertsFromString() {
            rangeDef.setBandColor(Color.BLUE);
            Color result = rangeDef.getBandColor();

            assertNotNull(result);
            assertEquals(Color.BLUE.getRed(), result.getRed(), 0.001);
            assertEquals(Color.BLUE.getGreen(), result.getGreen(), 0.001);
            assertEquals(Color.BLUE.getBlue(), result.getBlue(), 0.001);
        }

        @Test
        @DisplayName("Color round-trip preserves color values")
        void colorRoundTripPreservesValues() {
            Color original = Color.rgb(128, 64, 192);
            rangeDef.setBandColor(original);
            Color result = rangeDef.getBandColor();

            assertEquals(original.getRed(), result.getRed(), 0.01);
            assertEquals(original.getGreen(), result.getGreen(), 0.01);
            assertEquals(original.getBlue(), result.getBlue(), 0.01);
        }

        @Test
        @DisplayName("Named colors convert correctly")
        void namedColorsConvert() {
            Color[] testColors = {Color.AQUAMARINE, Color.BLUEVIOLET, Color.GREEN, Color.YELLOW, Color.RED};

            for (Color color : testColors) {
                rangeDef.setBandColor(color);
                Color result = rangeDef.getBandColor();

                assertEquals(color.getRed(), result.getRed(), 0.001,
                        "Red component mismatch for " + color);
                assertEquals(color.getGreen(), result.getGreen(), 0.001,
                        "Green component mismatch for " + color);
                assertEquals(color.getBlue(), result.getBlue(), 0.001,
                        "Blue component mismatch for " + color);
            }
        }
    }

    // =========================================================================
    // Range Validation Tests
    // =========================================================================

    @Nested
    @DisplayName("Range Value Tests")
    class RangeValueTests {

        @Test
        @DisplayName("Valid range has lower less than upper")
        void validRangeHasLowerLessThanUpper() {
            rangeDef.setLowerRange(5.0);
            rangeDef.setUpperRange(10.0);

            assertTrue(rangeDef.getLowerRange() < rangeDef.getUpperRange());
        }

        @Test
        @DisplayName("Zero range is allowed")
        void zeroRangeAllowed() {
            rangeDef.setLowerRange(0.0);
            rangeDef.setUpperRange(5.0);

            assertEquals(0.0, rangeDef.getLowerRange());
        }

        @Test
        @DisplayName("Decimal ranges work correctly")
        void decimalRangesWork() {
            rangeDef.setLowerRange(2.7);
            rangeDef.setUpperRange(8.3);

            assertEquals(2.7, rangeDef.getLowerRange(), 0.001);
            assertEquals(8.3, rangeDef.getUpperRange(), 0.001);
        }
    }

    // =========================================================================
    // Typical Usage Tests
    // =========================================================================

    @Nested
    @DisplayName("Typical Usage Scenarios")
    class TypicalUsageTests {

        @Test
        @DisplayName("Can create fully configured transit band")
        void createFullyConfiguredBand() {
            UUID id = UUID.randomUUID();

            rangeDef.setBandId(id);
            rangeDef.setBandName("Warp Drive Range");
            rangeDef.setEnabled(true);
            rangeDef.setLowerRange(3.0);
            rangeDef.setUpperRange(10.0);
            rangeDef.setLineWidth(1.5);
            rangeDef.setBandColor(Color.CYAN);

            assertEquals(id, rangeDef.getBandId());
            assertEquals("Warp Drive Range", rangeDef.getBandName());
            assertTrue(rangeDef.isEnabled());
            assertEquals(3.0, rangeDef.getLowerRange(), 0.001);
            assertEquals(10.0, rangeDef.getUpperRange(), 0.001);
            assertEquals(1.5, rangeDef.getLineWidth(), 0.001);
            assertEquals(Color.CYAN.getRed(), rangeDef.getBandColor().getRed(), 0.001);
        }

        @Test
        @DisplayName("Disabled band with valid range settings")
        void disabledBandWithValidSettings() {
            rangeDef.setEnabled(false);
            rangeDef.setLowerRange(0.0);
            rangeDef.setUpperRange(5.0);

            assertFalse(rangeDef.isEnabled());
            assertEquals(0.0, rangeDef.getLowerRange());
            assertEquals(5.0, rangeDef.getUpperRange());
        }

        @Test
        @DisplayName("Default line width from constants")
        void defaultLineWidthFromConstants() {
            rangeDef.setLineWidth(TransitConstants.DEFAULT_BAND_LINE_WIDTH);

            assertEquals(TransitConstants.DEFAULT_BAND_LINE_WIDTH, rangeDef.getLineWidth());
        }
    }

    // =========================================================================
    // Edge Case Tests
    // =========================================================================

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Null bandId is allowed")
        void nullBandIdAllowed() {
            rangeDef.setBandId(null);
            assertNull(rangeDef.getBandId());
        }

        @Test
        @DisplayName("Null bandName is allowed")
        void nullBandNameAllowed() {
            rangeDef.setBandName(null);
            assertNull(rangeDef.getBandName());
        }

        @Test
        @DisplayName("Empty bandName is allowed")
        void emptyBandNameAllowed() {
            rangeDef.setBandName("");
            assertEquals("", rangeDef.getBandName());
        }

        @Test
        @DisplayName("Negative range values are stored")
        void negativeRangeValuesStored() {
            rangeDef.setLowerRange(-5.0);
            rangeDef.setUpperRange(-1.0);

            assertEquals(-5.0, rangeDef.getLowerRange());
            assertEquals(-1.0, rangeDef.getUpperRange());
        }

        @Test
        @DisplayName("Very small line width is allowed")
        void verySmallLineWidthAllowed() {
            rangeDef.setLineWidth(0.001);
            assertEquals(0.001, rangeDef.getLineWidth(), 0.0001);
        }

        @Test
        @DisplayName("Maximum range values at constants limit")
        void maxRangeAtConstantsLimit() {
            rangeDef.setLowerRange(TransitConstants.RANGE_MIN);
            rangeDef.setUpperRange(TransitConstants.RANGE_MAX);

            assertEquals(TransitConstants.RANGE_MIN, rangeDef.getLowerRange());
            assertEquals(TransitConstants.RANGE_MAX, rangeDef.getUpperRange());
        }
    }

    // =========================================================================
    // Lombok Generated Tests
    // =========================================================================

    @Nested
    @DisplayName("Lombok Generated Methods")
    class LombokGeneratedTests {

        @Test
        @DisplayName("equals works for identical objects")
        void equalsForIdenticalObjects() {
            TransitRangeDef def1 = createTestDef();
            TransitRangeDef def2 = createTestDef();

            assertEquals(def1, def2);
        }

        @Test
        @DisplayName("hashCode is consistent with equals")
        void hashCodeConsistentWithEquals() {
            TransitRangeDef def1 = createTestDef();
            TransitRangeDef def2 = createTestDef();

            if (def1.equals(def2)) {
                assertEquals(def1.hashCode(), def2.hashCode());
            }
        }

        @Test
        @DisplayName("toString returns non-null string")
        void toStringReturnsNonNull() {
            rangeDef.setBandName("Test");
            String result = rangeDef.toString();

            assertNotNull(result);
            assertTrue(result.contains("Test") || result.contains("TransitRangeDef"));
        }

        private TransitRangeDef createTestDef() {
            TransitRangeDef def = new TransitRangeDef();
            def.setBandId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
            def.setBandName("Test");
            def.setEnabled(true);
            def.setLowerRange(0.0);
            def.setUpperRange(10.0);
            def.setLineWidth(1.0);
            def.setColor("0xffffffff");
            return def;
        }
    }
}
