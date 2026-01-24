package com.teamgannon.trips.dataset.model;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Polity Tests")
class PolityTest {

    private Polity polity;

    @BeforeEach
    void setUp() {
        polity = new Polity();
    }

    @Nested
    @DisplayName("Basic Properties Tests")
    class BasicPropertiesTests {

        @Test
        @DisplayName("should set and get polity name")
        void shouldSetAndGetPolityName() {
            polity.setPolityName("Terran Federation");

            assertEquals("Terran Federation", polity.getPolityName());
        }

        @Test
        @DisplayName("should set and get id")
        void shouldSetAndGetId() {
            UUID id = UUID.randomUUID();
            polity.setId(id);

            assertEquals(id, polity.getId());
        }

        @Test
        @DisplayName("should have default jump distance of 7.5")
        void shouldHaveDefaultJumpDistance() {
            assertEquals(7.5, polity.getPJumpDist());
        }

        @Test
        @DisplayName("should set and get jump distance")
        void shouldSetAndGetJumpDistance() {
            polity.setPJumpDist(10.0);

            assertEquals(10.0, polity.getPJumpDist());
        }

        @Test
        @DisplayName("should have default font descriptor")
        void shouldHaveDefaultFontDescriptor() {
            FontDescriptor font = polity.getPolityFont();

            assertNotNull(font);
            assertEquals("Arial", font.getName());
            assertEquals(10, font.getSize());
        }

        @Test
        @DisplayName("should set and get font descriptor")
        void shouldSetAndGetFontDescriptor() {
            FontDescriptor customFont = new FontDescriptor("Helvetica", 12);
            polity.setPolityFont(customFont);

            assertEquals("Helvetica", polity.getPolityFont().getName());
            assertEquals(12, polity.getPolityFont().getSize());
        }
    }

    @Nested
    @DisplayName("Color Conversion Tests")
    class ColorConversionTests {

        @Test
        @DisplayName("should convert Color to polity color array")
        void shouldConvertColorToPolityColorArray() {
            Color red = Color.RED;
            polity.setPolityColor(red);

            double[] colorArray = polity.getPolityColor().getRed() == 1.0 ? new double[]{1.0, 0.0, 0.0} : null;

            Color retrieved = polity.getPolityColor();
            assertEquals(1.0, retrieved.getRed(), 0.001);
            assertEquals(0.0, retrieved.getGreen(), 0.001);
            assertEquals(0.0, retrieved.getBlue(), 0.001);
        }

        @Test
        @DisplayName("should convert polity color array to Color")
        void shouldConvertPolityColorArrayToColor() {
            polity.setPolityColor(Color.color(0.5, 0.6, 0.7));

            Color color = polity.getPolityColor();

            assertEquals(0.5, color.getRed(), 0.001);
            assertEquals(0.6, color.getGreen(), 0.001);
            assertEquals(0.7, color.getBlue(), 0.001);
        }

        @Test
        @DisplayName("should handle pure red color")
        void shouldHandlePureRedColor() {
            polity.setPolityColor(Color.RED);

            Color color = polity.getPolityColor();

            assertEquals(1.0, color.getRed(), 0.001);
            assertEquals(0.0, color.getGreen(), 0.001);
            assertEquals(0.0, color.getBlue(), 0.001);
        }

        @Test
        @DisplayName("should handle pure green color")
        void shouldHandlePureGreenColor() {
            polity.setPolityColor(Color.GREEN);

            Color color = polity.getPolityColor();

            assertEquals(0.0, color.getRed(), 0.001);
            // Note: Color.GREEN in JavaFX is actually (0, 0.5, 0) not (0, 1, 0)
            assertEquals(Color.GREEN.getGreen(), color.getGreen(), 0.001);
            assertEquals(0.0, color.getBlue(), 0.001);
        }

        @Test
        @DisplayName("should handle pure blue color")
        void shouldHandlePureBlueColor() {
            polity.setPolityColor(Color.BLUE);

            Color color = polity.getPolityColor();

            assertEquals(0.0, color.getRed(), 0.001);
            assertEquals(0.0, color.getGreen(), 0.001);
            assertEquals(1.0, color.getBlue(), 0.001);
        }

        @Test
        @DisplayName("should handle white color")
        void shouldHandleWhiteColor() {
            polity.setPolityColor(Color.WHITE);

            Color color = polity.getPolityColor();

            assertEquals(1.0, color.getRed(), 0.001);
            assertEquals(1.0, color.getGreen(), 0.001);
            assertEquals(1.0, color.getBlue(), 0.001);
        }

        @Test
        @DisplayName("should handle black color")
        void shouldHandleBlackColor() {
            polity.setPolityColor(Color.BLACK);

            Color color = polity.getPolityColor();

            assertEquals(0.0, color.getRed(), 0.001);
            assertEquals(0.0, color.getGreen(), 0.001);
            assertEquals(0.0, color.getBlue(), 0.001);
        }

        @Test
        @DisplayName("should handle custom color with fractional values")
        void shouldHandleCustomColorWithFractionalValues() {
            Color customColor = Color.color(0.123, 0.456, 0.789);
            polity.setPolityColor(customColor);

            Color color = polity.getPolityColor();

            assertEquals(0.123, color.getRed(), 0.001);
            assertEquals(0.456, color.getGreen(), 0.001);
            assertEquals(0.789, color.getBlue(), 0.001);
        }

        @Test
        @DisplayName("should round-trip color correctly")
        void shouldRoundTripColorCorrectly() {
            Color original = Color.color(0.25, 0.50, 0.75);
            polity.setPolityColor(original);
            Color retrieved = polity.getPolityColor();

            assertEquals(original.getRed(), retrieved.getRed(), 0.0001);
            assertEquals(original.getGreen(), retrieved.getGreen(), 0.0001);
            assertEquals(original.getBlue(), retrieved.getBlue(), 0.0001);
        }

        @Test
        @DisplayName("should handle multiple color changes")
        void shouldHandleMultipleColorChanges() {
            polity.setPolityColor(Color.RED);
            assertEquals(1.0, polity.getPolityColor().getRed(), 0.001);

            polity.setPolityColor(Color.BLUE);
            assertEquals(1.0, polity.getPolityColor().getBlue(), 0.001);
            assertEquals(0.0, polity.getPolityColor().getRed(), 0.001);

            polity.setPolityColor(Color.color(0.3, 0.3, 0.3));
            assertEquals(0.3, polity.getPolityColor().getRed(), 0.001);
            assertEquals(0.3, polity.getPolityColor().getGreen(), 0.001);
            assertEquals(0.3, polity.getPolityColor().getBlue(), 0.001);
        }
    }

    @Nested
    @DisplayName("Complete Polity Configuration Tests")
    class CompletePolityConfigurationTests {

        @Test
        @DisplayName("should configure complete polity")
        void shouldConfigureCompletePolity() {
            UUID id = UUID.randomUUID();
            polity.setId(id);
            polity.setPolityName("Ktor Hegemony");
            polity.setPolityColor(Color.color(0.8, 0.2, 0.2));
            polity.setPJumpDist(12.5);
            polity.setPolityFont(new FontDescriptor("Times New Roman", 14));

            assertEquals(id, polity.getId());
            assertEquals("Ktor Hegemony", polity.getPolityName());
            assertEquals(0.8, polity.getPolityColor().getRed(), 0.001);
            assertEquals(12.5, polity.getPJumpDist());
            assertEquals("Times New Roman", polity.getPolityFont().getName());
            assertEquals(14, polity.getPolityFont().getSize());
        }

        @Test
        @DisplayName("should have different polities with different properties")
        void shouldHaveDifferentPolitiesWithDifferentProperties() {
            Polity terran = new Polity();
            terran.setPolityName("Terran Republic");
            terran.setPolityColor(Color.BLUE);
            terran.setPJumpDist(8.0);

            Polity ktor = new Polity();
            ktor.setPolityName("Ktor");
            ktor.setPolityColor(Color.RED);
            ktor.setPJumpDist(10.0);

            Polity hkhrkh = new Polity();
            hkhrkh.setPolityName("Hkh'Rkh");
            hkhrkh.setPolityColor(Color.color(0.5, 0.0, 0.5));
            hkhrkh.setPJumpDist(7.0);

            // Verify all are distinct
            assertNotEquals(terran.getPolityName(), ktor.getPolityName());
            assertNotEquals(terran.getPJumpDist(), ktor.getPJumpDist());
            assertNotEquals(terran.getPolityColor().getRed(), ktor.getPolityColor().getRed(), 0.001);
        }
    }
}
