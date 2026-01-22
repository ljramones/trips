package com.teamgannon.trips.graphics;

import com.teamgannon.trips.algorithms.Universe;
import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.config.application.model.ApplicationPreferences;
import com.teamgannon.trips.config.application.model.CurrentPlot;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.search.SearchContext;
import com.teamgannon.trips.service.StarService;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.*;
import org.springframework.context.ApplicationEventPublisher;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for PlotManager.
 * <p>
 * Tests cover:
 * <ul>
 *   <li>Star filtering and validation</li>
 *   <li>Coordinate bounds checking</li>
 *   <li>Dataset lookup</li>
 *   <li>Color utility methods</li>
 * </ul>
 * <p>
 * Note: Full integration tests for plotStars/drawAstrographicData
 * require extensive mocking and are better suited for integration testing.
 */
class PlotManagerTest {

    private static boolean javaFxInitialized = false;
    private PlotManager plotManager;
    private TripsContext tripsContext;
    private StarService starService;
    private ApplicationEventPublisher eventPublisher;

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> {});
            javaFxInitialized = true;
        } catch (IllegalStateException e) {
            javaFxInitialized = true;
        } catch (Exception e) {
            System.out.println("JavaFX not available: " + e.getMessage());
            javaFxInitialized = false;
        }
    }

    @BeforeEach
    void setUp() {
        tripsContext = mock(TripsContext.class);
        starService = mock(StarService.class);
        eventPublisher = mock(ApplicationEventPublisher.class);

        // Setup minimal mocks for PlotManager construction
        CurrentPlot currentPlot = new CurrentPlot();
        SearchContext searchContext = mock(SearchContext.class);
        ApplicationPreferences appPreferences = mock(ApplicationPreferences.class);

        when(tripsContext.getCurrentPlot()).thenReturn(currentPlot);
        when(tripsContext.getSearchContext()).thenReturn(searchContext);
        when(tripsContext.getAppPreferences()).thenReturn(appPreferences);
        when(appPreferences.getGridsize()).thenReturn(10);

        plotManager = new PlotManager(tripsContext, starService, eventPublisher);
    }

    // =========================================================================
    // Color Utility Tests
    // =========================================================================

    @Nested
    @DisplayName("Color Utility Tests")
    class ColorUtilityTests {

        @Test
        @DisplayName("Get color with valid RGB array returns correct color")
        void getColorWithValidRGBArray() {
            double[] colors = {0.5, 0.25, 0.75};

            Color result = PlotManager.getColor(colors);

            assertEquals(0.5, result.getRed(), 0.001);
            assertEquals(0.25, result.getGreen(), 0.001);
            assertEquals(0.75, result.getBlue(), 0.001);
        }

        @Test
        @DisplayName("Get color with zeros returns black")
        void getColorWithZerosReturnsBlack() {
            double[] colors = {0.0, 0.0, 0.0};

            Color result = PlotManager.getColor(colors);

            assertEquals(Color.BLACK, result);
        }

        @Test
        @DisplayName("Get color with ones returns white")
        void getColorWithOnesReturnsWhite() {
            double[] colors = {1.0, 1.0, 1.0};

            Color result = PlotManager.getColor(colors);

            assertEquals(Color.WHITE, result);
        }

        @Test
        @DisplayName("Get color with red values returns red")
        void getColorWithRedValues() {
            double[] colors = {1.0, 0.0, 0.0};

            Color result = PlotManager.getColor(colors);

            assertEquals(Color.RED, result);
        }
    }

    // =========================================================================
    // Star Validation Tests (using reflection for private method)
    // =========================================================================

    @Nested
    @DisplayName("Star Validation Tests")
    class StarValidationTests {

        @Test
        @DisplayName("Filter removes stars with null coordinates")
        void filterRemovesStarsWithNullCoordinates() throws Exception {
            List<StarObject> stars = new ArrayList<>();
            stars.add(createValidStar("valid-star", 1, 2, 3));
            stars.add(createStarWithNullCoordinates("null-coords"));
            stars.add(createValidStar("another-valid", 4, 5, 6));

            List<StarObject> result = invokeFilterValidStars(stars);

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Filter removes stars with empty spectral class")
        void filterRemovesStarsWithEmptySpectralClass() throws Exception {
            List<StarObject> stars = new ArrayList<>();
            stars.add(createValidStar("valid-star", 1, 2, 3));
            stars.add(createStarWithEmptySpectralClass("no-spectral", 1, 2, 3));

            List<StarObject> result = invokeFilterValidStars(stars);

            assertEquals(1, result.size());
            assertEquals("valid-star", result.get(0).getDisplayName());
        }

        @Test
        @DisplayName("Filter removes stars outside universe bounds")
        void filterRemovesStarsOutsideBounds() throws Exception {
            List<StarObject> stars = new ArrayList<>();
            stars.add(createValidStar("in-bounds", 100, 100, 100));
            // Create star outside bounds (Universe.boxWidth, etc. are the limits)
            stars.add(createValidStar("out-of-x", Universe.boxWidth + 1, 100, 100));
            stars.add(createValidStar("out-of-y", 100, Universe.boxHeight + 1, 100));
            stars.add(createValidStar("out-of-z", 100, 100, Universe.boxDepth + 1));

            List<StarObject> result = invokeFilterValidStars(stars);

            assertEquals(1, result.size());
            assertEquals("in-bounds", result.get(0).getDisplayName());
        }

        @Test
        @DisplayName("Filter handles empty list")
        void filterHandlesEmptyList() throws Exception {
            List<StarObject> stars = new ArrayList<>();

            List<StarObject> result = invokeFilterValidStars(stars);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Filter handles all invalid stars")
        void filterHandlesAllInvalidStars() throws Exception {
            List<StarObject> stars = new ArrayList<>();
            stars.add(createStarWithNullCoordinates("null-1"));
            stars.add(createStarWithEmptySpectralClass("no-spec", 1, 2, 3));

            List<StarObject> result = invokeFilterValidStars(stars);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Filter preserves order of valid stars")
        void filterPreservesOrderOfValidStars() throws Exception {
            List<StarObject> stars = new ArrayList<>();
            stars.add(createValidStar("first", 1, 1, 1));
            stars.add(createStarWithNullCoordinates("invalid"));
            stars.add(createValidStar("second", 2, 2, 2));
            stars.add(createValidStar("third", 3, 3, 3));

            List<StarObject> result = invokeFilterValidStars(stars);

            assertEquals(3, result.size());
            assertEquals("first", result.get(0).getDisplayName());
            assertEquals("second", result.get(1).getDisplayName());
            assertEquals("third", result.get(2).getDisplayName());
        }

        @Test
        @DisplayName("Filter handles stars with short coordinate arrays")
        void filterHandlesStarsWithShortCoordinateArrays() throws Exception {
            List<StarObject> stars = new ArrayList<>();
            stars.add(createValidStar("valid", 1, 2, 3));
            stars.add(createStarWithShortCoordinates("short-coords"));

            List<StarObject> result = invokeFilterValidStars(stars);

            assertEquals(1, result.size());
        }
    }

    // =========================================================================
    // Drawable Bounds Tests (using reflection for private method)
    // =========================================================================

    @Nested
    @DisplayName("Drawable Bounds Tests")
    class DrawableBoundsTests {

        @Test
        @DisplayName("Star within bounds is drawable")
        void starWithinBoundsIsDrawable() throws Exception {
            StarObject star = createValidStar("test", 100, 100, 100);

            boolean result = invokeDrawable(star);

            assertTrue(result);
        }

        @Test
        @DisplayName("Star at origin is drawable")
        void starAtOriginIsDrawable() throws Exception {
            StarObject star = createValidStar("test", 0, 0, 0);

            boolean result = invokeDrawable(star);

            assertTrue(result);
        }

        @Test
        @DisplayName("Star exceeding X bound is not drawable")
        void starExceedingXBoundIsNotDrawable() throws Exception {
            StarObject star = createValidStar("test", Universe.boxWidth + 1, 100, 100);

            boolean result = invokeDrawable(star);

            assertFalse(result);
        }

        @Test
        @DisplayName("Star exceeding Y bound is not drawable")
        void starExceedingYBoundIsNotDrawable() throws Exception {
            StarObject star = createValidStar("test", 100, Universe.boxHeight + 1, 100);

            boolean result = invokeDrawable(star);

            assertFalse(result);
        }

        @Test
        @DisplayName("Star exceeding Z bound is not drawable")
        void starExceedingZBoundIsNotDrawable() throws Exception {
            StarObject star = createValidStar("test", 100, 100, Universe.boxDepth + 1);

            boolean result = invokeDrawable(star);

            assertFalse(result);
        }

        @Test
        @DisplayName("Star exactly at bounds is drawable")
        void starExactlyAtBoundsIsDrawable() throws Exception {
            StarObject star = createValidStar("test", Universe.boxWidth, Universe.boxHeight, Universe.boxDepth);

            boolean result = invokeDrawable(star);

            assertTrue(result);
        }

        @Test
        @DisplayName("Star with negative coordinates is drawable")
        void starWithNegativeCoordinatesIsDrawable() throws Exception {
            StarObject star = createValidStar("test", -100, -100, -100);

            boolean result = invokeDrawable(star);

            assertTrue(result);
        }
    }

    // =========================================================================
    // Dataset Lookup Tests (using reflection for private method)
    // =========================================================================

    @Nested
    @DisplayName("Dataset Lookup Tests")
    class DatasetLookupTests {

        @Test
        @DisplayName("Find dataset by name returns correct descriptor")
        void findDatasetByNameReturnsCorrectDescriptor() throws Exception {
            List<DataSetDescriptor> datasets = new ArrayList<>();
            DataSetDescriptor desc1 = createDataSetDescriptor("Dataset A");
            DataSetDescriptor desc2 = createDataSetDescriptor("Dataset B");
            DataSetDescriptor desc3 = createDataSetDescriptor("Dataset C");
            datasets.add(desc1);
            datasets.add(desc2);
            datasets.add(desc3);

            DataSetDescriptor result = invokeFindFromDataSet("Dataset B", datasets);

            assertSame(desc2, result);
        }

        @Test
        @DisplayName("Find non-existent dataset returns null")
        void findNonExistentDatasetReturnsNull() throws Exception {
            List<DataSetDescriptor> datasets = new ArrayList<>();
            datasets.add(createDataSetDescriptor("Dataset A"));

            DataSetDescriptor result = invokeFindFromDataSet("Non-Existent", datasets);

            assertNull(result);
        }

        @Test
        @DisplayName("Find in empty list returns null")
        void findInEmptyListReturnsNull() throws Exception {
            List<DataSetDescriptor> datasets = new ArrayList<>();

            DataSetDescriptor result = invokeFindFromDataSet("Any", datasets);

            assertNull(result);
        }

        @Test
        @DisplayName("Find with exact name match is case sensitive")
        void findWithExactNameMatchIsCaseSensitive() throws Exception {
            List<DataSetDescriptor> datasets = new ArrayList<>();
            datasets.add(createDataSetDescriptor("Dataset"));

            DataSetDescriptor resultLower = invokeFindFromDataSet("dataset", datasets);
            DataSetDescriptor resultExact = invokeFindFromDataSet("Dataset", datasets);

            assertNull(resultLower);
            assertNotNull(resultExact);
        }
    }

    // =========================================================================
    // Route Visibility Tests
    // =========================================================================

    @Nested
    @DisplayName("Route Visibility Tests")
    class RouteVisibilityTests {

        @Test
        @DisplayName("Get route visibility returns empty map when no routes")
        void getRouteVisibilityReturnsEmptyMapWhenNoRoutes() {
            var visibility = plotManager.getRouteVisibility();

            assertTrue(visibility.isEmpty());
        }
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    private StarObject createValidStar(String name, double x, double y, double z) {
        StarObject star = mock(StarObject.class);
        when(star.getDisplayName()).thenReturn(name);
        when(star.getCoordinates()).thenReturn(new double[]{x, y, z});
        when(star.getOrthoSpectralClass()).thenReturn("G2V");
        when(star.getSpectralClass()).thenReturn("G2V");
        return star;
    }

    private StarObject createStarWithNullCoordinates(String name) {
        StarObject star = mock(StarObject.class);
        when(star.getDisplayName()).thenReturn(name);
        when(star.getCoordinates()).thenReturn(null);
        when(star.getOrthoSpectralClass()).thenReturn("G2V");
        return star;
    }

    private StarObject createStarWithShortCoordinates(String name) {
        StarObject star = mock(StarObject.class);
        when(star.getDisplayName()).thenReturn(name);
        when(star.getCoordinates()).thenReturn(new double[]{1, 2}); // Only 2 elements
        when(star.getOrthoSpectralClass()).thenReturn("G2V");
        return star;
    }

    private StarObject createStarWithEmptySpectralClass(String name, double x, double y, double z) {
        StarObject star = mock(StarObject.class);
        when(star.getDisplayName()).thenReturn(name);
        when(star.getCoordinates()).thenReturn(new double[]{x, y, z});
        when(star.getOrthoSpectralClass()).thenReturn("");
        return star;
    }

    private DataSetDescriptor createDataSetDescriptor(String name) {
        DataSetDescriptor descriptor = mock(DataSetDescriptor.class);
        when(descriptor.getDataSetName()).thenReturn(name);
        return descriptor;
    }

    // Reflection helpers to test private methods

    @SuppressWarnings("unchecked")
    private List<StarObject> invokeFilterValidStars(List<StarObject> stars) throws Exception {
        Method method = PlotManager.class.getDeclaredMethod("filterValidStars", List.class);
        method.setAccessible(true);
        return (List<StarObject>) method.invoke(plotManager, stars);
    }

    private boolean invokeDrawable(StarObject star) throws Exception {
        Method method = PlotManager.class.getDeclaredMethod("drawable", StarObject.class);
        method.setAccessible(true);
        return (boolean) method.invoke(plotManager, star);
    }

    private DataSetDescriptor invokeFindFromDataSet(String selected, List<DataSetDescriptor> datasets) throws Exception {
        Method method = PlotManager.class.getDeclaredMethod("findFromDataSet", String.class, List.class);
        method.setAccessible(true);
        return (DataSetDescriptor) method.invoke(plotManager, selected, datasets);
    }

    private void assumeJavaFxAvailable() {
        Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");
    }

    private <T> T runOnFxThread(java.util.concurrent.Callable<T> callable) throws Exception {
        if (Platform.isFxApplicationThread()) {
            return callable.call();
        }

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<T> result = new AtomicReference<>();
        AtomicReference<Exception> exception = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                result.set(callable.call());
            } catch (Exception e) {
                exception.set(e);
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(30, TimeUnit.SECONDS), "JavaFX operation timed out");

        if (exception.get() != null) {
            throw exception.get();
        }

        return result.get();
    }
}
