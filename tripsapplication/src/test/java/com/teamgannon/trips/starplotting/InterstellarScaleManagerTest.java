package com.teamgannon.trips.starplotting;

import com.teamgannon.trips.algorithms.Universe;
import com.teamgannon.trips.jpa.model.StarObject;
import javafx.geometry.Point3D;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for InterstellarScaleManager coordinate transformation and scaling.
 */
class InterstellarScaleManagerTest {

    private static final double EPS = 1e-9;
    private static final double LOOSE_EPS = 1e-6;

    private InterstellarScaleManager scaleManager;

    @BeforeEach
    void setUp() {
        scaleManager = new InterstellarScaleManager();
    }

    // ==================== Constructor Tests ====================

    @Nested
    @DisplayName("Constructor and Default Values")
    class ConstructorTests {

        @Test
        @DisplayName("Default constructor sets expected initial values")
        void defaultConstructorSetsInitialValues() {
            InterstellarScaleManager sm = new InterstellarScaleManager();

            assertEquals(1.0, sm.getZoomLevel(), EPS);
            assertEquals(1.0, sm.getBaseScalingFactor(), EPS);
            assertEquals(1.5, sm.getStarSizeMultiplier(), EPS);
            assertEquals(5.0, sm.getGridSpacingLY(), EPS);
            assertArrayEquals(new double[]{0, 0, 0}, sm.getCenterCoordinatesLY(), EPS);
        }

        @Test
        @DisplayName("Constructor with grid spacing sets custom value")
        void constructorWithGridSpacing() {
            InterstellarScaleManager sm = new InterstellarScaleManager(10.0);

            assertEquals(10.0, sm.getGridSpacingLY(), EPS);
        }
    }

    // ==================== Coordinate Transformation Tests ====================

    @Nested
    @DisplayName("Light-Year to Screen Transformation")
    class LyToScreenTests {

        @Test
        @DisplayName("Origin transforms to origin when centered at origin")
        void originToOrigin() {
            scaleManager.setCenterCoordinates(0, 0, 0);

            double[] result = scaleManager.lyToScreen(0, 0, 0);

            assertArrayEquals(new double[]{0, 0, 0}, result, EPS);
        }

        @Test
        @DisplayName("Positive coordinates scale correctly")
        void positiveCoordinatesScale() {
            scaleManager.setCenterCoordinates(0, 0, 0);
            // Default scaling factor is 1.0, so 10 LY should become 10 screen units

            double[] result = scaleManager.lyToScreen(10, 20, 30);

            assertEquals(10.0, result[0], EPS);
            assertEquals(20.0, result[1], EPS);
            assertEquals(30.0, result[2], EPS);
        }

        @Test
        @DisplayName("Negative coordinates scale correctly")
        void negativeCoordinatesScale() {
            scaleManager.setCenterCoordinates(0, 0, 0);

            double[] result = scaleManager.lyToScreen(-5, -10, -15);

            assertEquals(-5.0, result[0], EPS);
            assertEquals(-10.0, result[1], EPS);
            assertEquals(-15.0, result[2], EPS);
        }

        @Test
        @DisplayName("Center offset is subtracted from coordinates")
        void centerOffsetSubtracted() {
            scaleManager.setCenterCoordinates(10, 20, 30);

            double[] result = scaleManager.lyToScreen(15, 25, 35);

            assertEquals(5.0, result[0], EPS);
            assertEquals(5.0, result[1], EPS);
            assertEquals(5.0, result[2], EPS);
        }

        @Test
        @DisplayName("Point3D variant works correctly")
        void point3DVariant() {
            scaleManager.setCenterCoordinates(0, 0, 0);

            Point3D result = scaleManager.lyToScreenPoint(10, 20, 30);

            assertEquals(10.0, result.getX(), EPS);
            assertEquals(20.0, result.getY(), EPS);
            assertEquals(30.0, result.getZ(), EPS);
        }

        @Test
        @DisplayName("Array variant handles null gracefully")
        void arrayVariantHandlesNull() {
            double[] result = scaleManager.lyToScreen(null);

            assertArrayEquals(new double[]{0, 0, 0}, result, EPS);
        }

        @Test
        @DisplayName("Array variant handles short array gracefully")
        void arrayVariantHandlesShortArray() {
            double[] result = scaleManager.lyToScreen(new double[]{1, 2});

            assertArrayEquals(new double[]{0, 0, 0}, result, EPS);
        }
    }

    @Nested
    @DisplayName("Screen to Light-Year Transformation")
    class ScreenToLyTests {

        @Test
        @DisplayName("Origin transforms to center coordinates")
        void originToCenter() {
            scaleManager.setCenterCoordinates(10, 20, 30);

            double[] result = scaleManager.screenToLY(0, 0, 0);

            assertEquals(10.0, result[0], EPS);
            assertEquals(20.0, result[1], EPS);
            assertEquals(30.0, result[2], EPS);
        }

        @Test
        @DisplayName("Positive screen coordinates convert correctly")
        void positiveScreenCoordinates() {
            scaleManager.setCenterCoordinates(0, 0, 0);

            double[] result = scaleManager.screenToLY(10, 20, 30);

            assertEquals(10.0, result[0], EPS);
            assertEquals(20.0, result[1], EPS);
            assertEquals(30.0, result[2], EPS);
        }
    }

    @Nested
    @DisplayName("Bidirectional Transformation Consistency")
    class BidirectionalTests {

        @Test
        @DisplayName("Round-trip LY -> Screen -> LY returns original")
        void roundTripLyScreenLy() {
            scaleManager.setCenterCoordinates(5, 10, 15);
            double[] original = {20, 30, 40};

            double[] screen = scaleManager.lyToScreen(original);
            double[] result = scaleManager.screenToLY(screen[0], screen[1], screen[2]);

            assertArrayEquals(original, result, LOOSE_EPS);
        }

        @Test
        @DisplayName("Round-trip Screen -> LY -> Screen returns original")
        void roundTripScreenLyScreen() {
            scaleManager.setCenterCoordinates(0, 0, 0);
            double[] originalScreen = {100, 200, 300};

            double[] ly = scaleManager.screenToLY(originalScreen[0], originalScreen[1], originalScreen[2]);
            double[] result = scaleManager.lyToScreen(ly);

            assertArrayEquals(originalScreen, result, LOOSE_EPS);
        }

        @Test
        @DisplayName("Round-trip works with non-zero center and zoom")
        void roundTripWithCenterAndZoom() {
            scaleManager.setCenterCoordinates(10, -5, 20);
            scaleManager.setZoomLevel(2.0);
            double[] original = {15, 10, 25};

            double[] screen = scaleManager.lyToScreen(original);
            double[] result = scaleManager.screenToLY(screen[0], screen[1], screen[2]);

            assertArrayEquals(original, result, LOOSE_EPS);
        }
    }

    // ==================== Distance Conversion Tests ====================

    @Nested
    @DisplayName("Distance Conversion")
    class DistanceConversionTests {

        @Test
        @DisplayName("LY distance converts to screen distance")
        void lyDistanceToScreen() {
            scaleManager.setZoomLevel(2.0);
            // With base factor 1.0 and zoom 2.0, effective factor is 2.0

            double result = scaleManager.lyDistanceToScreen(10.0);

            assertEquals(20.0, result, EPS);
        }

        @Test
        @DisplayName("Screen distance converts to LY distance")
        void screenDistanceToLy() {
            scaleManager.setZoomLevel(2.0);

            double result = scaleManager.screenDistanceToLY(20.0);

            assertEquals(10.0, result, EPS);
        }

        @Test
        @DisplayName("Distance calculation between two points")
        void distanceBetweenPoints() {
            double[] p1 = {0, 0, 0};
            double[] p2 = {3, 4, 0};

            double result = InterstellarScaleManager.distanceLY(p1, p2);

            assertEquals(5.0, result, EPS);
        }

        @Test
        @DisplayName("3D distance calculation")
        void distance3D() {
            double[] p1 = {1, 2, 3};
            double[] p2 = {4, 6, 3};

            double result = InterstellarScaleManager.distanceLY(p1, p2);

            assertEquals(5.0, result, EPS); // sqrt(9 + 16 + 0) = 5
        }
    }

    // ==================== Star Radius Calculation Tests ====================

    @Nested
    @DisplayName("Star Radius Calculation")
    class StarRadiusTests {

        @Test
        @DisplayName("Bright star (low magnitude) gets larger radius")
        void brightStarLargerRadius() {
            double brightRadius = scaleManager.calculateStarRadius(-1.0); // Very bright
            double dimRadius = scaleManager.calculateStarRadius(10.0);    // Dim

            assertTrue(brightRadius > dimRadius,
                    "Bright star should have larger radius than dim star");
        }

        @Test
        @DisplayName("Invalid magnitude returns default radius")
        void invalidMagnitudeReturnsDefault() {
            double nanResult = scaleManager.calculateStarRadius(Double.NaN);
            double zeroResult = scaleManager.calculateStarRadius(0.0);

            // Default is 3.0 * 1.5 multiplier = 4.5
            assertEquals(4.5, nanResult, EPS);
            assertEquals(4.5, zeroResult, EPS);
        }

        @Test
        @DisplayName("Radius is within expected bounds")
        void radiusWithinBounds() {
            for (double mag = -2; mag <= 15; mag += 1) {
                double radius = scaleManager.calculateStarRadius(mag);
                assertTrue(radius >= 1.0 * 1.5, "Radius should be >= min * multiplier");
                assertTrue(radius <= 7.0 * 1.5, "Radius should be <= max * multiplier");
            }
        }

        @Test
        @DisplayName("Luminosity-based radius calculation")
        void luminosityBasedRadius() {
            double sunRadius = scaleManager.calculateStarRadiusFromLuminosity(1.0);    // Sun
            double brightRadius = scaleManager.calculateStarRadiusFromLuminosity(100.0); // 100x Sun
            double dimRadius = scaleManager.calculateStarRadiusFromLuminosity(0.01);    // 0.01x Sun

            assertTrue(brightRadius > sunRadius, "Brighter star should be larger");
            assertTrue(sunRadius > dimRadius, "Sun should be larger than dim star");
        }

        @Test
        @DisplayName("Invalid luminosity returns default radius")
        void invalidLuminosityReturnsDefault() {
            double negResult = scaleManager.calculateStarRadiusFromLuminosity(-1.0);
            double zeroResult = scaleManager.calculateStarRadiusFromLuminosity(0.0);
            double nanResult = scaleManager.calculateStarRadiusFromLuminosity(Double.NaN);

            assertEquals(4.5, negResult, EPS);
            assertEquals(4.5, zeroResult, EPS);
            assertEquals(4.5, nanResult, EPS);
        }

        @Test
        @DisplayName("Combined method prefers luminosity over magnitude")
        void combinedMethodPrefersLuminosity() {
            // If both are valid, luminosity should be used
            double withBoth = scaleManager.calculateStarRadius(100.0, -1.0); // High lum, bright mag
            double lumOnly = scaleManager.calculateStarRadiusFromLuminosity(100.0);

            assertEquals(lumOnly, withBoth, EPS);
        }

        @Test
        @DisplayName("Combined method falls back to magnitude")
        void combinedMethodFallsBackToMagnitude() {
            double withMagOnly = scaleManager.calculateStarRadius(0.0, 5.0); // No lum, valid mag
            double magOnly = scaleManager.calculateStarRadius(5.0);

            assertEquals(magOnly, withMagOnly, EPS);
        }

        @Test
        @DisplayName("Star size multiplier affects radius")
        void starSizeMultiplierAffectsRadius() {
            double normalRadius = scaleManager.calculateStarRadius(5.0);

            scaleManager.setStarSizeMultiplier(3.0);
            double largerRadius = scaleManager.calculateStarRadius(5.0);

            assertEquals(normalRadius * 2.0, largerRadius, LOOSE_EPS);
        }
    }

    // ==================== Zoom Tests ====================

    @Nested
    @DisplayName("Zoom Functionality")
    class ZoomTests {

        @Test
        @DisplayName("Zoom in increases effective scale")
        void zoomInIncreasesScale() {
            double initialFactor = scaleManager.getEffectiveScalingFactor();

            scaleManager.zoom(2.0);

            assertEquals(initialFactor * 2.0, scaleManager.getEffectiveScalingFactor(), EPS);
        }

        @Test
        @DisplayName("Zoom out decreases effective scale")
        void zoomOutDecreasesScale() {
            double initialFactor = scaleManager.getEffectiveScalingFactor();

            scaleManager.zoom(0.5);

            assertEquals(initialFactor * 0.5, scaleManager.getEffectiveScalingFactor(), EPS);
        }

        @Test
        @DisplayName("Zoom is clamped to minimum")
        void zoomClampedToMin() {
            scaleManager.setMinZoom(0.5);

            scaleManager.zoom(0.01); // Try to zoom way out

            assertEquals(0.5, scaleManager.getZoomLevel(), EPS);
        }

        @Test
        @DisplayName("Zoom is clamped to maximum")
        void zoomClampedToMax() {
            scaleManager.setMaxZoom(5.0);

            scaleManager.zoom(100.0); // Try to zoom way in

            assertEquals(5.0, scaleManager.getZoomLevel(), EPS);
        }

        @Test
        @DisplayName("Reset zoom returns to 1.0")
        void resetZoom() {
            scaleManager.zoom(3.0);
            scaleManager.resetZoom();

            assertEquals(1.0, scaleManager.getZoomLevel(), EPS);
        }

        @Test
        @DisplayName("Zoom affects coordinate transformation")
        void zoomAffectsTransformation() {
            scaleManager.setCenterCoordinates(0, 0, 0);
            double[] normalResult = scaleManager.lyToScreen(10, 0, 0);

            scaleManager.setZoomLevel(2.0);
            double[] zoomedResult = scaleManager.lyToScreen(10, 0, 0);

            assertEquals(normalResult[0] * 2.0, zoomedResult[0], EPS);
        }
    }

    // ==================== Scaling Calculation Tests ====================

    @Nested
    @DisplayName("Scaling Calculation from Stars")
    class ScalingCalculationTests {

        @Test
        @DisplayName("Calculates scaling from star list")
        void calculatesScalingFromStars() {
            List<StarObject> stars = createTestStars();

            scaleManager.calculateScalingFromStars(stars, new double[]{0, 0, 0});

            assertTrue(scaleManager.getBaseScalingFactor() > 0);
            assertEquals(-10.0, scaleManager.getMinX(), EPS);
            assertEquals(10.0, scaleManager.getMaxX(), EPS);
        }

        @Test
        @DisplayName("Empty star list logs warning and keeps defaults")
        void emptyStarListKeepsDefaults() {
            double initialFactor = scaleManager.getBaseScalingFactor();

            scaleManager.calculateScalingFromStars(new ArrayList<>(), new double[]{0, 0, 0});

            assertEquals(initialFactor, scaleManager.getBaseScalingFactor(), EPS);
        }

        @Test
        @DisplayName("Scaling factor fits data to screen height")
        void scalingFactorFitsToScreenHeight() {
            List<StarObject> stars = createTestStars(); // Range is 20 LY (from -10 to +10)

            scaleManager.calculateScalingFromStars(stars, new double[]{0, 0, 0});

            // Expected: SCREEN_HEIGHT / maxRange = 680 / 20 = 34
            assertEquals(Universe.boxHeight / 20.0, scaleManager.getBaseScalingFactor(), LOOSE_EPS);
        }

        private List<StarObject> createTestStars() {
            List<StarObject> stars = new ArrayList<>();
            stars.add(createStarAt(-10, -10, -10));
            stars.add(createStarAt(10, 10, 10));
            stars.add(createStarAt(0, 0, 0));
            return stars;
        }

        private StarObject createStarAt(double x, double y, double z) {
            StarObject star = new StarObject();
            star.setX(x);
            star.setY(y);
            star.setZ(z);
            return star;
        }
    }

    // ==================== Grid Scale Tests ====================

    @Nested
    @DisplayName("Grid Scale Calculation")
    class GridScaleTests {

        @Test
        @DisplayName("Grid spacing converts correctly to screen units")
        void gridSpacingConvertsToScreen() {
            scaleManager.setGridSpacingLY(5.0);
            scaleManager.setZoomLevel(2.0);
            // With base factor 1.0 and zoom 2.0, 5 LY = 10 screen units

            double result = scaleManager.getGridSpacingScreen();

            assertEquals(10.0, result, EPS);
        }

        @Test
        @DisplayName("Scale grid values appropriate for small ranges")
        void scaleGridValuesSmallRange() {
            // Simulate a small system (< 10 LY range)
            List<StarObject> stars = new ArrayList<>();
            stars.add(createStarAt(-3, -3, -3));
            stars.add(createStarAt(3, 3, 3));
            scaleManager.calculateScalingFromStars(stars, new double[]{0, 0, 0});

            double[] gridValues = scaleManager.getScaleGridLYValues();

            assertArrayEquals(new double[]{1, 2, 5, 10}, gridValues);
        }

        @Test
        @DisplayName("Scale grid values appropriate for large ranges")
        void scaleGridValuesLargeRange() {
            // Simulate a large system (> 100 LY range)
            List<StarObject> stars = new ArrayList<>();
            stars.add(createStarAt(-100, -100, -100));
            stars.add(createStarAt(100, 100, 100));
            scaleManager.calculateScalingFromStars(stars, new double[]{0, 0, 0});

            double[] gridValues = scaleManager.getScaleGridLYValues();

            assertArrayEquals(new double[]{50, 100, 150, 200}, gridValues);
        }

        private StarObject createStarAt(double x, double y, double z) {
            StarObject star = new StarObject();
            star.setX(x);
            star.setY(y);
            star.setZ(z);
            return star;
        }
    }

    // ==================== Center Coordinate Tests ====================

    @Nested
    @DisplayName("Center Coordinate Handling")
    class CenterCoordinateTests {

        @Test
        @DisplayName("Set center coordinates from values")
        void setCenterFromValues() {
            scaleManager.setCenterCoordinates(10, 20, 30);

            assertArrayEquals(new double[]{10, 20, 30}, scaleManager.getCenterCoordinatesLY(), EPS);
        }

        @Test
        @DisplayName("Set center coordinates from StarObject")
        void setCenterFromStarObject() {
            StarObject star = new StarObject();
            star.setX(5.5);
            star.setY(10.5);
            star.setZ(15.5);

            scaleManager.setCenterCoordinates(star);

            assertArrayEquals(new double[]{5.5, 10.5, 15.5}, scaleManager.getCenterCoordinatesLY(), EPS);
        }

        @Test
        @DisplayName("Reset center to origin")
        void resetCenterToOrigin() {
            scaleManager.setCenterCoordinates(100, 200, 300);
            scaleManager.resetCenterToOrigin();

            assertArrayEquals(new double[]{0, 0, 0}, scaleManager.getCenterCoordinatesLY(), EPS);
        }
    }

    // ==================== Bounds Checking Tests ====================

    @Nested
    @DisplayName("Screen Bounds Checking")
    class BoundsCheckingTests {

        @Test
        @DisplayName("Origin is within bounds")
        void originWithinBounds() {
            assertTrue(scaleManager.isWithinScreenBounds(0, 0, 0));
        }

        @Test
        @DisplayName("Points within half-width are in bounds")
        void pointsWithinHalfWidthInBounds() {
            double halfWidth = InterstellarScaleManager.SCREEN_WIDTH / 2;
            double halfHeight = InterstellarScaleManager.SCREEN_HEIGHT / 2;
            double halfDepth = InterstellarScaleManager.SCREEN_DEPTH / 2;

            assertTrue(scaleManager.isWithinScreenBounds(halfWidth - 1, halfHeight - 1, halfDepth - 1));
            assertTrue(scaleManager.isWithinScreenBounds(-halfWidth + 1, -halfHeight + 1, -halfDepth + 1));
        }

        @Test
        @DisplayName("Points outside bounds are detected")
        void pointsOutsideBoundsDetected() {
            double farOut = 10000;

            assertFalse(scaleManager.isWithinScreenBounds(farOut, 0, 0));
            assertFalse(scaleManager.isWithinScreenBounds(0, farOut, 0));
            assertFalse(scaleManager.isWithinScreenBounds(0, 0, farOut));
        }
    }

    // ==================== Utility Method Tests ====================

    @Nested
    @DisplayName("Utility Methods")
    class UtilityMethodTests {

        @Test
        @DisplayName("Scale description is human readable")
        void scaleDescriptionHumanReadable() {
            scaleManager.setZoomLevel(2.0);

            String description = scaleManager.getScaleDescription();

            assertNotNull(description);
            assertTrue(description.contains("light-years"));
            assertTrue(description.contains("zoom"));
        }

        @Test
        @DisplayName("toString provides useful information")
        void toStringProvidesInfo() {
            scaleManager.setCenterCoordinates(10, 20, 30);
            scaleManager.setZoomLevel(1.5);

            String result = scaleManager.toString();

            assertTrue(result.contains("center"));
            assertTrue(result.contains("10.00"));
            assertTrue(result.contains("zoom"));
            assertTrue(result.contains("1.50"));
        }

        @Test
        @DisplayName("Range calculations are correct")
        void rangeCalculations() {
            List<StarObject> stars = new ArrayList<>();
            stars.add(createStarAt(-5, -10, -15));
            stars.add(createStarAt(15, 20, 25));
            scaleManager.calculateScalingFromStars(stars, new double[]{0, 0, 0});

            assertEquals(20.0, scaleManager.getXRange(), EPS);
            assertEquals(30.0, scaleManager.getYRange(), EPS);
            assertEquals(40.0, scaleManager.getZRange(), EPS);
        }

        private StarObject createStarAt(double x, double y, double z) {
            StarObject star = new StarObject();
            star.setX(x);
            star.setY(y);
            star.setZ(z);
            return star;
        }
    }

    // ==================== Edge Case Tests ====================

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Zero scaling factor handled in inverse transform")
        void zeroScalingFactorHandled() {
            // Force zero scaling factor scenario
            scaleManager.setZoomLevel(0.0);

            double[] result = scaleManager.screenToLY(100, 100, 100);

            // Should return zeros or center, not throw exception
            assertNotNull(result);
            assertEquals(3, result.length);
        }

        @Test
        @DisplayName("Very large coordinates transform without overflow")
        void veryLargeCoordinates() {
            double huge = 1e10;

            double[] result = scaleManager.lyToScreen(huge, huge, huge);

            assertFalse(Double.isNaN(result[0]));
            assertFalse(Double.isInfinite(result[0]));
        }

        @Test
        @DisplayName("Very small coordinates transform correctly")
        void verySmallCoordinates() {
            double tiny = 1e-10;

            double[] result = scaleManager.lyToScreen(tiny, tiny, tiny);

            assertFalse(Double.isNaN(result[0]));
        }
    }
}
