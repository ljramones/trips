package com.teamgannon.trips.transits;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TransitConstants.
 * Verifies constant values and utility class behavior.
 */
class TransitConstantsTest {

    // =========================================================================
    // Range Configuration Constants
    // =========================================================================

    @Nested
    @DisplayName("Range Configuration Constants")
    class RangeConfigurationTests {

        @Test
        @DisplayName("RANGE_MIN is zero")
        void rangeMinIsZero() {
            assertEquals(0.0, TransitConstants.RANGE_MIN);
        }

        @Test
        @DisplayName("RANGE_MAX is 20 light years")
        void rangeMaxIsTwenty() {
            assertEquals(20.0, TransitConstants.RANGE_MAX);
        }

        @Test
        @DisplayName("RANGE_MAJOR_TICK is 5 light years")
        void rangeMajorTickIsFive() {
            assertEquals(5.0, TransitConstants.RANGE_MAJOR_TICK);
        }

        @Test
        @DisplayName("RANGE_MINOR_TICK_COUNT is 5")
        void rangeMinorTickCountIsFive() {
            assertEquals(5, TransitConstants.RANGE_MINOR_TICK_COUNT);
        }

        @Test
        @DisplayName("Range configuration forms valid slider setup")
        void rangeConfigurationIsValid() {
            // Verify that the range configuration makes sense for a slider
            assertTrue(TransitConstants.RANGE_MIN < TransitConstants.RANGE_MAX,
                    "RANGE_MIN should be less than RANGE_MAX");
            assertTrue(TransitConstants.RANGE_MAJOR_TICK > 0,
                    "RANGE_MAJOR_TICK should be positive");
            assertTrue(TransitConstants.RANGE_MINOR_TICK_COUNT > 0,
                    "RANGE_MINOR_TICK_COUNT should be positive");
            assertEquals(0, (int) (TransitConstants.RANGE_MAX % TransitConstants.RANGE_MAJOR_TICK),
                    "RANGE_MAX should be evenly divisible by RANGE_MAJOR_TICK");
        }
    }

    // =========================================================================
    // Line Width Constants
    // =========================================================================

    @Nested
    @DisplayName("Line Width Constants")
    class LineWidthTests {

        @Test
        @DisplayName("DEFAULT_LINE_WIDTH is 1.0")
        void defaultLineWidthIsOne() {
            assertEquals(1.0, TransitConstants.DEFAULT_LINE_WIDTH);
        }

        @Test
        @DisplayName("DEFAULT_BAND_LINE_WIDTH is 0.5")
        void defaultBandLineWidthIsHalf() {
            assertEquals(0.5, TransitConstants.DEFAULT_BAND_LINE_WIDTH);
        }

        @Test
        @DisplayName("Line widths are positive")
        void lineWidthsArePositive() {
            assertTrue(TransitConstants.DEFAULT_LINE_WIDTH > 0,
                    "DEFAULT_LINE_WIDTH should be positive");
            assertTrue(TransitConstants.DEFAULT_BAND_LINE_WIDTH > 0,
                    "DEFAULT_BAND_LINE_WIDTH should be positive");
        }

        @Test
        @DisplayName("Band line width is thinner than default")
        void bandLineWidthIsThinnerThanDefault() {
            assertTrue(TransitConstants.DEFAULT_BAND_LINE_WIDTH <= TransitConstants.DEFAULT_LINE_WIDTH,
                    "DEFAULT_BAND_LINE_WIDTH should be less than or equal to DEFAULT_LINE_WIDTH");
        }
    }

    // =========================================================================
    // 3D Rendering Constants
    // =========================================================================

    @Nested
    @DisplayName("3D Rendering Constants")
    class RenderingTests {

        @Test
        @DisplayName("LABEL_ANCHOR_SPHERE_RADIUS is 1.0")
        void labelAnchorSphereRadiusIsOne() {
            assertEquals(1.0, TransitConstants.LABEL_ANCHOR_SPHERE_RADIUS);
        }

        @Test
        @DisplayName("Sphere radius is positive")
        void sphereRadiusIsPositive() {
            assertTrue(TransitConstants.LABEL_ANCHOR_SPHERE_RADIUS > 0,
                    "LABEL_ANCHOR_SPHERE_RADIUS should be positive");
        }
    }

    // =========================================================================
    // Label Positioning Constants
    // =========================================================================

    @Nested
    @DisplayName("Label Positioning Constants")
    class LabelPositioningTests {

        @Test
        @DisplayName("LABEL_PADDING is 20.0")
        void labelPaddingIsTwenty() {
            assertEquals(20.0, TransitConstants.LABEL_PADDING);
        }

        @Test
        @DisplayName("LABEL_EDGE_MARGIN is 5.0")
        void labelEdgeMarginIsFive() {
            assertEquals(5.0, TransitConstants.LABEL_EDGE_MARGIN);
        }

        @Test
        @DisplayName("Label positioning values are positive")
        void labelPositioningValuesArePositive() {
            assertTrue(TransitConstants.LABEL_PADDING > 0,
                    "LABEL_PADDING should be positive");
            assertTrue(TransitConstants.LABEL_EDGE_MARGIN > 0,
                    "LABEL_EDGE_MARGIN should be positive");
        }

        @Test
        @DisplayName("LABEL_PADDING is larger than LABEL_EDGE_MARGIN")
        void paddingLargerThanMargin() {
            assertTrue(TransitConstants.LABEL_PADDING > TransitConstants.LABEL_EDGE_MARGIN,
                    "LABEL_PADDING should be larger than LABEL_EDGE_MARGIN");
        }
    }

    // =========================================================================
    // Utility Class Tests
    // =========================================================================

    @Nested
    @DisplayName("Utility Class Behavior")
    class UtilityClassTests {

        @Test
        @DisplayName("TransitConstants has private constructor")
        void hasPrivateConstructor() throws NoSuchMethodException {
            Constructor<TransitConstants> constructor = TransitConstants.class.getDeclaredConstructor();
            assertFalse(constructor.canAccess(null),
                    "Constructor should be private");
        }

        @Test
        @DisplayName("TransitConstants is final class")
        void isFinalClass() {
            assertTrue(java.lang.reflect.Modifier.isFinal(TransitConstants.class.getModifiers()),
                    "TransitConstants should be a final class");
        }
    }
}
