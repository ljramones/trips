package com.teamgannon.trips.dataset.model;

import com.teamgannon.trips.dataset.enums.GridLines;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Link Tests")
class LinkTest {

    private Link link;

    @BeforeEach
    void setUp() {
        link = new Link();
    }

    @Nested
    @DisplayName("Default Values Tests")
    class DefaultValuesTests {

        @Test
        @DisplayName("should have displayLink true by default")
        void shouldHaveDisplayLinkTrueByDefault() {
            assertTrue(link.isDisplayLink());
        }

        @Test
        @DisplayName("should have default min distance of 1.0")
        void shouldHaveDefaultMinDistance() {
            assertEquals(1.0, link.getLinkMinDistance());
        }

        @Test
        @DisplayName("should have default max distance of 4.0")
        void shouldHaveDefaultMaxDistance() {
            assertEquals(4.0, link.getLinkMaxDistance());
        }

        @Test
        @DisplayName("should have dispLinkId false by default")
        void shouldHaveDispLinkIdFalseByDefault() {
            assertFalse(link.isDispLindId());
        }

        @Test
        @DisplayName("should have solid link style by default")
        void shouldHaveSolidLinkStyleByDefault() {
            assertEquals(GridLines.Solid, link.getLinkStyle());
        }

        @Test
        @DisplayName("should have default font descriptor")
        void shouldHaveDefaultFontDescriptor() {
            FontDescriptor font = link.getLinkFont();

            assertNotNull(font);
            assertEquals("Arial", font.getName());
            assertEquals(10, font.getSize());
        }
    }

    @Nested
    @DisplayName("Basic Properties Tests")
    class BasicPropertiesTests {

        @Test
        @DisplayName("should set and get displayLink")
        void shouldSetAndGetDisplayLink() {
            link.setDisplayLink(false);
            assertFalse(link.isDisplayLink());

            link.setDisplayLink(true);
            assertTrue(link.isDisplayLink());
        }

        @Test
        @DisplayName("should set and get link min distance")
        void shouldSetAndGetLinkMinDistance() {
            link.setLinkMinDistance(2.5);

            assertEquals(2.5, link.getLinkMinDistance());
        }

        @Test
        @DisplayName("should set and get link max distance")
        void shouldSetAndGetLinkMaxDistance() {
            link.setLinkMaxDistance(10.0);

            assertEquals(10.0, link.getLinkMaxDistance());
        }

        @Test
        @DisplayName("should set and get dispLinkId")
        void shouldSetAndGetDispLinkId() {
            link.setDispLindId(true);
            assertTrue(link.isDispLindId());

            link.setDispLindId(false);
            assertFalse(link.isDispLindId());
        }

        @Test
        @DisplayName("should set and get link style")
        void shouldSetAndGetLinkStyle() {
            link.setLinkStyle(GridLines.Dotted);
            assertEquals(GridLines.Dotted, link.getLinkStyle());

            link.setLinkStyle(GridLines.Solid);
            assertEquals(GridLines.Solid, link.getLinkStyle());
        }

        @Test
        @DisplayName("should set and get link font")
        void shouldSetAndGetLinkFont() {
            FontDescriptor customFont = new FontDescriptor("Courier", 14);
            link.setLinkFont(customFont);

            assertEquals("Courier", link.getLinkFont().getName());
            assertEquals(14, link.getLinkFont().getSize());
        }
    }

    @Nested
    @DisplayName("Color Conversion Tests")
    class ColorConversionTests {

        @Test
        @DisplayName("should convert Color to link color array")
        void shouldConvertColorToLinkColorArray() {
            link.setLinkColor(Color.RED);

            Color retrieved = link.getLinkColor();

            assertEquals(1.0, retrieved.getRed(), 0.001);
            assertEquals(0.0, retrieved.getGreen(), 0.001);
            assertEquals(0.0, retrieved.getBlue(), 0.001);
        }

        @Test
        @DisplayName("should convert link color array to Color")
        void shouldConvertLinkColorArrayToColor() {
            link.setLinkColor(Color.color(0.3, 0.4, 0.5));

            Color color = link.getLinkColor();

            assertEquals(0.3, color.getRed(), 0.001);
            assertEquals(0.4, color.getGreen(), 0.001);
            assertEquals(0.5, color.getBlue(), 0.001);
        }

        @Test
        @DisplayName("should handle pure red color")
        void shouldHandlePureRedColor() {
            link.setLinkColor(Color.RED);

            Color color = link.getLinkColor();

            assertEquals(1.0, color.getRed(), 0.001);
            assertEquals(0.0, color.getGreen(), 0.001);
            assertEquals(0.0, color.getBlue(), 0.001);
        }

        @Test
        @DisplayName("should handle pure green color")
        void shouldHandlePureGreenColor() {
            link.setLinkColor(Color.GREEN);

            Color color = link.getLinkColor();

            assertEquals(0.0, color.getRed(), 0.001);
            assertEquals(Color.GREEN.getGreen(), color.getGreen(), 0.001);
            assertEquals(0.0, color.getBlue(), 0.001);
        }

        @Test
        @DisplayName("should handle pure blue color")
        void shouldHandlePureBlueColor() {
            link.setLinkColor(Color.BLUE);

            Color color = link.getLinkColor();

            assertEquals(0.0, color.getRed(), 0.001);
            assertEquals(0.0, color.getGreen(), 0.001);
            assertEquals(1.0, color.getBlue(), 0.001);
        }

        @Test
        @DisplayName("should handle white color")
        void shouldHandleWhiteColor() {
            link.setLinkColor(Color.WHITE);

            Color color = link.getLinkColor();

            assertEquals(1.0, color.getRed(), 0.001);
            assertEquals(1.0, color.getGreen(), 0.001);
            assertEquals(1.0, color.getBlue(), 0.001);
        }

        @Test
        @DisplayName("should handle black color")
        void shouldHandleBlackColor() {
            link.setLinkColor(Color.BLACK);

            Color color = link.getLinkColor();

            assertEquals(0.0, color.getRed(), 0.001);
            assertEquals(0.0, color.getGreen(), 0.001);
            assertEquals(0.0, color.getBlue(), 0.001);
        }

        @Test
        @DisplayName("should handle custom color with fractional values")
        void shouldHandleCustomColorWithFractionalValues() {
            Color customColor = Color.color(0.111, 0.222, 0.333);
            link.setLinkColor(customColor);

            Color color = link.getLinkColor();

            assertEquals(0.111, color.getRed(), 0.001);
            assertEquals(0.222, color.getGreen(), 0.001);
            assertEquals(0.333, color.getBlue(), 0.001);
        }

        @Test
        @DisplayName("should round-trip color correctly")
        void shouldRoundTripColorCorrectly() {
            Color original = Color.color(0.15, 0.35, 0.55);
            link.setLinkColor(original);
            Color retrieved = link.getLinkColor();

            assertEquals(original.getRed(), retrieved.getRed(), 0.0001);
            assertEquals(original.getGreen(), retrieved.getGreen(), 0.0001);
            assertEquals(original.getBlue(), retrieved.getBlue(), 0.0001);
        }

        @Test
        @DisplayName("should handle multiple color changes")
        void shouldHandleMultipleColorChanges() {
            link.setLinkColor(Color.RED);
            assertEquals(1.0, link.getLinkColor().getRed(), 0.001);

            link.setLinkColor(Color.BLUE);
            assertEquals(1.0, link.getLinkColor().getBlue(), 0.001);
            assertEquals(0.0, link.getLinkColor().getRed(), 0.001);

            link.setLinkColor(Color.color(0.5, 0.5, 0.5));
            assertEquals(0.5, link.getLinkColor().getRed(), 0.001);
            assertEquals(0.5, link.getLinkColor().getGreen(), 0.001);
            assertEquals(0.5, link.getLinkColor().getBlue(), 0.001);
        }
    }

    @Nested
    @DisplayName("Distance Range Tests")
    class DistanceRangeTests {

        @Test
        @DisplayName("should allow min distance less than max distance")
        void shouldAllowMinDistanceLessThanMaxDistance() {
            link.setLinkMinDistance(2.0);
            link.setLinkMaxDistance(8.0);

            assertEquals(2.0, link.getLinkMinDistance());
            assertEquals(8.0, link.getLinkMaxDistance());
            assertTrue(link.getLinkMinDistance() < link.getLinkMaxDistance());
        }

        @Test
        @DisplayName("should allow very small distance range")
        void shouldAllowVerySmallDistanceRange() {
            link.setLinkMinDistance(4.9);
            link.setLinkMaxDistance(5.1);

            assertEquals(4.9, link.getLinkMinDistance());
            assertEquals(5.1, link.getLinkMaxDistance());
        }

        @Test
        @DisplayName("should allow zero min distance")
        void shouldAllowZeroMinDistance() {
            link.setLinkMinDistance(0.0);

            assertEquals(0.0, link.getLinkMinDistance());
        }

        @Test
        @DisplayName("should allow large max distance")
        void shouldAllowLargeMaxDistance() {
            link.setLinkMaxDistance(100.0);

            assertEquals(100.0, link.getLinkMaxDistance());
        }
    }

    @Nested
    @DisplayName("Complete Link Configuration Tests")
    class CompleteLinkConfigurationTests {

        @Test
        @DisplayName("should configure complete link for short range")
        void shouldConfigureCompleteLinkForShortRange() {
            link.setDisplayLink(true);
            link.setLinkMinDistance(0.0);
            link.setLinkMaxDistance(3.0);
            link.setDispLindId(true);
            link.setLinkStyle(GridLines.Solid);
            link.setLinkColor(Color.CYAN);
            link.setLinkFont(new FontDescriptor("Arial", 8));

            assertTrue(link.isDisplayLink());
            assertEquals(0.0, link.getLinkMinDistance());
            assertEquals(3.0, link.getLinkMaxDistance());
            assertTrue(link.isDispLindId());
            assertEquals(GridLines.Solid, link.getLinkStyle());
            assertEquals(0.0, link.getLinkColor().getRed(), 0.001);
            assertEquals(1.0, link.getLinkColor().getGreen(), 0.001);
            assertEquals(1.0, link.getLinkColor().getBlue(), 0.001);
        }

        @Test
        @DisplayName("should configure complete link for medium range")
        void shouldConfigureCompleteLinkForMediumRange() {
            link.setDisplayLink(true);
            link.setLinkMinDistance(3.0);
            link.setLinkMaxDistance(7.0);
            link.setDispLindId(false);
            link.setLinkStyle(GridLines.Dotted);
            link.setLinkColor(Color.YELLOW);
            link.setLinkFont(new FontDescriptor("Verdana", 10));

            assertTrue(link.isDisplayLink());
            assertEquals(3.0, link.getLinkMinDistance());
            assertEquals(7.0, link.getLinkMaxDistance());
            assertFalse(link.isDispLindId());
            assertEquals(GridLines.Dotted, link.getLinkStyle());
        }

        @Test
        @DisplayName("should configure hidden link")
        void shouldConfigureHiddenLink() {
            link.setDisplayLink(false);
            link.setLinkMinDistance(7.0);
            link.setLinkMaxDistance(12.0);

            assertFalse(link.isDisplayLink());
            assertEquals(7.0, link.getLinkMinDistance());
            assertEquals(12.0, link.getLinkMaxDistance());
        }
    }
}
