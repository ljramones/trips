package com.teamgannon.trips.dataset.factories;

import com.teamgannon.trips.dataset.model.Link;
import com.teamgannon.trips.dataset.model.Polity;
import com.teamgannon.trips.file.chview.model.ChViewFile;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DataSetDescriptorFactory Tests")
class DataSetDescriptorFactoryTest {

    @Nested
    @DisplayName("getColor Method Tests")
    class GetColorMethodTests {

        @Test
        @DisplayName("should convert array to red color")
        void shouldConvertArrayToRedColor() {
            double[] colors = {1.0, 0.0, 0.0};

            Color result = DataSetDescriptorFactory.getColor(colors);

            assertEquals(1.0, result.getRed(), 0.001);
            assertEquals(0.0, result.getGreen(), 0.001);
            assertEquals(0.0, result.getBlue(), 0.001);
        }

        @Test
        @DisplayName("should convert array to green color")
        void shouldConvertArrayToGreenColor() {
            double[] colors = {0.0, 1.0, 0.0};

            Color result = DataSetDescriptorFactory.getColor(colors);

            assertEquals(0.0, result.getRed(), 0.001);
            assertEquals(1.0, result.getGreen(), 0.001);
            assertEquals(0.0, result.getBlue(), 0.001);
        }

        @Test
        @DisplayName("should convert array to blue color")
        void shouldConvertArrayToBlueColor() {
            double[] colors = {0.0, 0.0, 1.0};

            Color result = DataSetDescriptorFactory.getColor(colors);

            assertEquals(0.0, result.getRed(), 0.001);
            assertEquals(0.0, result.getGreen(), 0.001);
            assertEquals(1.0, result.getBlue(), 0.001);
        }

        @Test
        @DisplayName("should convert array to white color")
        void shouldConvertArrayToWhiteColor() {
            double[] colors = {1.0, 1.0, 1.0};

            Color result = DataSetDescriptorFactory.getColor(colors);

            assertEquals(1.0, result.getRed(), 0.001);
            assertEquals(1.0, result.getGreen(), 0.001);
            assertEquals(1.0, result.getBlue(), 0.001);
        }

        @Test
        @DisplayName("should convert array to black color")
        void shouldConvertArrayToBlackColor() {
            double[] colors = {0.0, 0.0, 0.0};

            Color result = DataSetDescriptorFactory.getColor(colors);

            assertEquals(0.0, result.getRed(), 0.001);
            assertEquals(0.0, result.getGreen(), 0.001);
            assertEquals(0.0, result.getBlue(), 0.001);
        }

        @Test
        @DisplayName("should convert array to custom color")
        void shouldConvertArrayToCustomColor() {
            double[] colors = {0.25, 0.50, 0.75};

            Color result = DataSetDescriptorFactory.getColor(colors);

            assertEquals(0.25, result.getRed(), 0.001);
            assertEquals(0.50, result.getGreen(), 0.001);
            assertEquals(0.75, result.getBlue(), 0.001);
        }

        @Test
        @DisplayName("should handle fractional values")
        void shouldHandleFractionalValues() {
            double[] colors = {0.123, 0.456, 0.789};

            Color result = DataSetDescriptorFactory.getColor(colors);

            assertEquals(0.123, result.getRed(), 0.001);
            assertEquals(0.456, result.getGreen(), 0.001);
            assertEquals(0.789, result.getBlue(), 0.001);
        }
    }

    @Nested
    @DisplayName("setColor Method Tests")
    class SetColorMethodTests {

        @Test
        @DisplayName("should convert red color to array")
        void shouldConvertRedColorToArray() {
            double[] result = DataSetDescriptorFactory.setColor(Color.RED);

            assertEquals(3, result.length);
            assertEquals(1.0, result[0], 0.001);
            assertEquals(0.0, result[1], 0.001);
            assertEquals(0.0, result[2], 0.001);
        }

        @Test
        @DisplayName("should convert green color to array")
        void shouldConvertGreenColorToArray() {
            // Note: Color.GREEN in JavaFX is (0, 0.5, 0), not (0, 1, 0)
            double[] result = DataSetDescriptorFactory.setColor(Color.GREEN);

            assertEquals(3, result.length);
            assertEquals(0.0, result[0], 0.001);
            assertEquals(Color.GREEN.getGreen(), result[1], 0.001);
            assertEquals(0.0, result[2], 0.001);
        }

        @Test
        @DisplayName("should convert blue color to array")
        void shouldConvertBlueColorToArray() {
            double[] result = DataSetDescriptorFactory.setColor(Color.BLUE);

            assertEquals(3, result.length);
            assertEquals(0.0, result[0], 0.001);
            assertEquals(0.0, result[1], 0.001);
            assertEquals(1.0, result[2], 0.001);
        }

        @Test
        @DisplayName("should convert white color to array")
        void shouldConvertWhiteColorToArray() {
            double[] result = DataSetDescriptorFactory.setColor(Color.WHITE);

            assertEquals(3, result.length);
            assertEquals(1.0, result[0], 0.001);
            assertEquals(1.0, result[1], 0.001);
            assertEquals(1.0, result[2], 0.001);
        }

        @Test
        @DisplayName("should convert black color to array")
        void shouldConvertBlackColorToArray() {
            double[] result = DataSetDescriptorFactory.setColor(Color.BLACK);

            assertEquals(3, result.length);
            assertEquals(0.0, result[0], 0.001);
            assertEquals(0.0, result[1], 0.001);
            assertEquals(0.0, result[2], 0.001);
        }

        @Test
        @DisplayName("should convert custom color to array")
        void shouldConvertCustomColorToArray() {
            Color customColor = Color.color(0.33, 0.66, 0.99);

            double[] result = DataSetDescriptorFactory.setColor(customColor);

            assertEquals(3, result.length);
            assertEquals(0.33, result[0], 0.001);
            assertEquals(0.66, result[1], 0.001);
            assertEquals(0.99, result[2], 0.001);
        }

        @Test
        @DisplayName("should convert cyan color to array")
        void shouldConvertCyanColorToArray() {
            double[] result = DataSetDescriptorFactory.setColor(Color.CYAN);

            assertEquals(3, result.length);
            assertEquals(0.0, result[0], 0.001);
            assertEquals(1.0, result[1], 0.001);
            assertEquals(1.0, result[2], 0.001);
        }

        @Test
        @DisplayName("should convert magenta color to array")
        void shouldConvertMagentaColorToArray() {
            double[] result = DataSetDescriptorFactory.setColor(Color.MAGENTA);

            assertEquals(3, result.length);
            assertEquals(1.0, result[0], 0.001);
            assertEquals(0.0, result[1], 0.001);
            assertEquals(1.0, result[2], 0.001);
        }

        @Test
        @DisplayName("should convert yellow color to array")
        void shouldConvertYellowColorToArray() {
            double[] result = DataSetDescriptorFactory.setColor(Color.YELLOW);

            assertEquals(3, result.length);
            assertEquals(1.0, result[0], 0.001);
            assertEquals(1.0, result[1], 0.001);
            assertEquals(0.0, result[2], 0.001);
        }
    }

    @Nested
    @DisplayName("Color Round-Trip Tests")
    class ColorRoundTripTests {

        @Test
        @DisplayName("should round-trip red color")
        void shouldRoundTripRedColor() {
            Color original = Color.RED;
            double[] array = DataSetDescriptorFactory.setColor(original);
            Color restored = DataSetDescriptorFactory.getColor(array);

            assertEquals(original.getRed(), restored.getRed(), 0.0001);
            assertEquals(original.getGreen(), restored.getGreen(), 0.0001);
            assertEquals(original.getBlue(), restored.getBlue(), 0.0001);
        }

        @Test
        @DisplayName("should round-trip blue color")
        void shouldRoundTripBlueColor() {
            Color original = Color.BLUE;
            double[] array = DataSetDescriptorFactory.setColor(original);
            Color restored = DataSetDescriptorFactory.getColor(array);

            assertEquals(original.getRed(), restored.getRed(), 0.0001);
            assertEquals(original.getGreen(), restored.getGreen(), 0.0001);
            assertEquals(original.getBlue(), restored.getBlue(), 0.0001);
        }

        @Test
        @DisplayName("should round-trip custom color")
        void shouldRoundTripCustomColor() {
            Color original = Color.color(0.12345, 0.54321, 0.98765);
            double[] array = DataSetDescriptorFactory.setColor(original);
            Color restored = DataSetDescriptorFactory.getColor(array);

            assertEquals(original.getRed(), restored.getRed(), 0.0001);
            assertEquals(original.getGreen(), restored.getGreen(), 0.0001);
            assertEquals(original.getBlue(), restored.getBlue(), 0.0001);
        }

        @Test
        @DisplayName("should round-trip multiple colors")
        void shouldRoundTripMultipleColors() {
            Color[] testColors = {
                    Color.RED, Color.GREEN, Color.BLUE,
                    Color.CYAN, Color.MAGENTA, Color.YELLOW,
                    Color.WHITE, Color.BLACK,
                    Color.color(0.1, 0.2, 0.3),
                    Color.color(0.9, 0.8, 0.7)
            };

            for (Color original : testColors) {
                double[] array = DataSetDescriptorFactory.setColor(original);
                Color restored = DataSetDescriptorFactory.getColor(array);

                assertEquals(original.getRed(), restored.getRed(), 0.0001,
                        "Red component mismatch for " + original);
                assertEquals(original.getGreen(), restored.getGreen(), 0.0001,
                        "Green component mismatch for " + original);
                assertEquals(original.getBlue(), restored.getBlue(), 0.0001,
                        "Blue component mismatch for " + original);
            }
        }
    }

    @Nested
    @DisplayName("createLinks Method Tests")
    class CreateLinksMethodTests {

        @Test
        @DisplayName("should return empty list when chViewFile is null")
        void shouldReturnEmptyListWhenChViewFileIsNull() {
            List<Link> result = DataSetDescriptorFactory.createLinks(null);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should return empty list for new ChViewFile")
        void shouldReturnEmptyListForNewChViewFile() {
            ChViewFile chViewFile = new ChViewFile();

            List<Link> result = DataSetDescriptorFactory.createLinks(chViewFile);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("should handle boundary value 0.0")
        void shouldHandleBoundaryValueZero() {
            double[] colors = {0.0, 0.0, 0.0};

            Color result = DataSetDescriptorFactory.getColor(colors);

            assertEquals(0.0, result.getRed());
            assertEquals(0.0, result.getGreen());
            assertEquals(0.0, result.getBlue());
        }

        @Test
        @DisplayName("should handle boundary value 1.0")
        void shouldHandleBoundaryValueOne() {
            double[] colors = {1.0, 1.0, 1.0};

            Color result = DataSetDescriptorFactory.getColor(colors);

            assertEquals(1.0, result.getRed());
            assertEquals(1.0, result.getGreen());
            assertEquals(1.0, result.getBlue());
        }

        @Test
        @DisplayName("should handle very small values")
        void shouldHandleVerySmallValues() {
            double[] colors = {0.001, 0.002, 0.003};

            Color result = DataSetDescriptorFactory.getColor(colors);

            assertEquals(0.001, result.getRed(), 0.0001);
            assertEquals(0.002, result.getGreen(), 0.0001);
            assertEquals(0.003, result.getBlue(), 0.0001);
        }

        @Test
        @DisplayName("should handle values close to 1.0")
        void shouldHandleValuesCloseToOne() {
            double[] colors = {0.999, 0.998, 0.997};

            Color result = DataSetDescriptorFactory.getColor(colors);

            assertEquals(0.999, result.getRed(), 0.0001);
            assertEquals(0.998, result.getGreen(), 0.0001);
            assertEquals(0.997, result.getBlue(), 0.0001);
        }
    }

    @Nested
    @DisplayName("Spectral Color Tests")
    class SpectralColorTests {

        @Test
        @DisplayName("should represent O class star color (blue)")
        void shouldRepresentOClassStarColor() {
            // O class stars are blue
            double[] oColor = {0.6, 0.7, 1.0};

            Color result = DataSetDescriptorFactory.getColor(oColor);

            assertTrue(result.getBlue() > result.getRed());
            assertTrue(result.getBlue() > result.getGreen());
        }

        @Test
        @DisplayName("should represent G class star color (yellow)")
        void shouldRepresentGClassStarColor() {
            // G class stars (like our Sun) are yellow
            double[] gColor = {1.0, 1.0, 0.0};

            Color result = DataSetDescriptorFactory.getColor(gColor);

            assertEquals(1.0, result.getRed(), 0.001);
            assertEquals(1.0, result.getGreen(), 0.001);
            assertEquals(0.0, result.getBlue(), 0.001);
        }

        @Test
        @DisplayName("should represent M class star color (red)")
        void shouldRepresentMClassStarColor() {
            // M class stars are red
            double[] mColor = {1.0, 0.3, 0.0};

            Color result = DataSetDescriptorFactory.getColor(mColor);

            assertTrue(result.getRed() > result.getGreen());
            assertTrue(result.getRed() > result.getBlue());
        }

        @Test
        @DisplayName("should represent K class star color (orange)")
        void shouldRepresentKClassStarColor() {
            // K class stars are orange
            double[] kColor = {1.0, 0.6, 0.2};

            Color result = DataSetDescriptorFactory.getColor(kColor);

            assertTrue(result.getRed() > result.getGreen());
            assertTrue(result.getGreen() > result.getBlue());
        }
    }
}
