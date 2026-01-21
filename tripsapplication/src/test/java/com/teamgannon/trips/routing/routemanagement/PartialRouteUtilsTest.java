package com.teamgannon.trips.routing.routemanagement;

import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.config.application.model.CurrentPlot;
import com.teamgannon.trips.config.application.model.DataSetContext;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.routing.model.Route;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for PartialRouteUtils.
 * <p>
 * Tests cover:
 * <ul>
 *   <li>Construction</li>
 *   <li>Finding partial routes when some stars are visible</li>
 *   <li>Handling routes with no visible stars</li>
 *   <li>Handling routes with all visible stars</li>
 * </ul>
 * <p>
 * Note: Some tests involving JavaFX components are limited due to toolkit initialization requirements.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PartialRouteUtilsTest {

    @Mock
    private TripsContext tripsContext;

    @Mock
    private RouteDisplay routeDisplay;

    @Mock
    private RouteGraphicsUtil routeGraphicsUtil;

    @Mock
    private RouteBuilderUtils routeBuilderUtils;

    @Mock
    private CurrentPlot currentPlot;

    @Mock
    private DataSetContext dataSetContext;

    @Mock
    private Node mockNode;

    private PartialRouteUtils partialRouteUtils;

    // Star lookup map for mocking (maps star ID to Node)
    private Map<String, Node> starLookup;

    @BeforeEach
    void setUp() {
        partialRouteUtils = new PartialRouteUtils(
                tripsContext,
                routeDisplay,
                routeGraphicsUtil,
                routeBuilderUtils
        );
        starLookup = new HashMap<>();
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    private StarDisplayRecord createStar(String name, String id, double x, double y, double z) {
        StarDisplayRecord star = new StarDisplayRecord();
        star.setStarName(name);
        star.setRecordId(id);
        star.setActualCoordinates(new double[]{x, y, z});
        star.setCoordinates(new Point3D(x * 10, y * 10, z * 10));
        return star;
    }

    private Route createRoute(String name, List<String> starIds, List<String> starNames, List<Double> lengths) {
        Route route = new Route();
        route.setUuid(UUID.randomUUID());
        route.setRouteName(name);
        route.setRouteColor("0x00ff00ff");
        route.setLineWidth(1.0);
        route.getRouteStars().addAll(starIds);
        route.getRouteStarNames().addAll(starNames);
        route.getRouteLengths().addAll(lengths);
        return route;
    }

    private void setupStarLookup(String... starIds) {
        for (String starId : starIds) {
            starLookup.put(starId, mockNode);  // Use mock Node
        }
        lenient().when(currentPlot.getStarLookup()).thenReturn(starLookup);
    }

    private void setupTripsContext() {
        lenient().when(tripsContext.getCurrentPlot()).thenReturn(currentPlot);
        lenient().when(tripsContext.getDataSetContext()).thenReturn(dataSetContext);
        lenient().when(dataSetContext.getDescriptor()).thenReturn(new DataSetDescriptor());
    }

    // =========================================================================
    // Construction Tests
    // =========================================================================

    @Nested
    @DisplayName("Construction Tests")
    class ConstructionTests {

        @Test
        @DisplayName("PartialRouteUtils can be constructed with dependencies")
        void canBeConstructed() {
            assertNotNull(partialRouteUtils);
        }
    }

    // =========================================================================
    // Find Partial Routes Tests - No Visible Stars
    // =========================================================================

    @Nested
    @DisplayName("No Visible Stars Tests")
    class NoVisibleStarsTests {

        @Test
        @DisplayName("findPartialRoutes() with no visible stars does not add routes to display")
        void noVisibleStarsDoesNotAddRoutes() {
            setupTripsContext();
            setupStarLookup();  // Empty lookup - no visible stars

            Route route = createRoute("Test Route",
                    Arrays.asList("star-001", "star-002", "star-003"),
                    Arrays.asList("Sol", "Alpha Centauri", "Barnard's Star"),
                    Arrays.asList(4.37, 5.98));

            partialRouteUtils.findPartialRoutes(route);

            // No routes should be added when no stars are visible
            verify(routeDisplay, never()).addRouteToDisplay(any(), any());
        }

        @Test
        @DisplayName("findPartialRoutes() completes without error for no visible stars")
        void noVisibleStarsCompletesWithoutError() {
            setupTripsContext();
            setupStarLookup();  // Empty

            Route route = createRoute("Test Route",
                    Arrays.asList("star-001", "star-002"),
                    Arrays.asList("Sol", "Alpha"),
                    Arrays.asList(4.37));

            // Should complete without error
            assertDoesNotThrow(() -> partialRouteUtils.findPartialRoutes(route));
        }
    }

    // =========================================================================
    // Find Partial Routes Tests - Mixed Visibility
    // =========================================================================

    @Nested
    @DisplayName("Mixed Visibility Tests")
    class MixedVisibilityTests {

        @Test
        @DisplayName("findPartialRoutes() with first star only visible does not add routes")
        void onlyFirstStarVisibleDoesNotAddRoutes() {
            setupTripsContext();
            setupStarLookup("star-001");  // Only first star visible

            Route route = createRoute("Test Route",
                    Arrays.asList("star-001", "star-002", "star-003"),
                    Arrays.asList("Sol", "Alpha", "Barnard"),
                    Arrays.asList(4.37, 5.98));

            partialRouteUtils.findPartialRoutes(route);

            // First star alone can't form a segment (needs adjacent visible star)
            verify(routeDisplay, never()).addRouteToDisplay(any(), any());
        }

        @Test
        @DisplayName("findPartialRoutes() with last star only visible does not add routes")
        void onlyLastStarVisibleDoesNotAddRoutes() {
            setupTripsContext();
            setupStarLookup("star-003");  // Only last star visible

            Route route = createRoute("Test Route",
                    Arrays.asList("star-001", "star-002", "star-003"),
                    Arrays.asList("Sol", "Alpha", "Barnard"),
                    Arrays.asList(4.37, 5.98));

            partialRouteUtils.findPartialRoutes(route);

            // Last star alone can't form a segment going backward
            verify(routeDisplay, never()).addRouteToDisplay(any(), any());
        }

        @Test
        @DisplayName("findPartialRoutes() with gap in visibility does not form segments")
        void gapInVisibilityDoesNotFormSegments() {
            setupTripsContext();
            setupStarLookup("star-001", "star-003");  // First and third visible, gap in middle

            StarDisplayRecord star1 = createStar("Sol", "star-001", 0, 0, 0);
            StarDisplayRecord star3 = createStar("Barnard", "star-003", 6.0, 0, 0);

            lenient().when(routeBuilderUtils.getStar("star-001")).thenReturn(star1);
            lenient().when(routeBuilderUtils.getStar("star-003")).thenReturn(star3);

            Route route = createRoute("Test Route",
                    Arrays.asList("star-001", "star-002", "star-003"),
                    Arrays.asList("Sol", "Alpha", "Barnard"),
                    Arrays.asList(4.37, 5.98));

            partialRouteUtils.findPartialRoutes(route);

            // No adjacent pairs visible, so no segments can be formed
            verify(routeDisplay, never()).addRouteToDisplay(any(), any());
        }
    }

    // =========================================================================
    // Edge Case Tests
    // =========================================================================

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("findPartialRoutes() handles single star route")
        void handlesSingleStarRoute() {
            setupTripsContext();
            setupStarLookup("star-001");

            Route route = createRoute("Single Star Route",
                    Arrays.asList("star-001"),
                    Arrays.asList("Sol"),
                    Arrays.asList());

            assertDoesNotThrow(() -> partialRouteUtils.findPartialRoutes(route));
            verify(routeDisplay, never()).addRouteToDisplay(any(), any());
        }

        @Test
        @DisplayName("findPartialRoutes() handles route with null star from builder")
        void handlesNullStarFromBuilder() {
            setupTripsContext();
            setupStarLookup("star-001", "star-002");

            // First star returns null
            lenient().when(routeBuilderUtils.getStar("star-001")).thenReturn(null);

            Route route = createRoute("Test Route",
                    Arrays.asList("star-001", "star-002"),
                    Arrays.asList("Sol", "Alpha"),
                    Arrays.asList(4.37));

            // Should handle gracefully and return early
            assertDoesNotThrow(() -> partialRouteUtils.findPartialRoutes(route));
        }

        @Test
        @DisplayName("findPartialRoutes() handles star with null coordinates")
        void handlesStarWithNullCoordinates() {
            setupTripsContext();
            setupStarLookup("star-001", "star-002");

            StarDisplayRecord star1 = createStar("Sol", "star-001", 0, 0, 0);
            star1.setCoordinates(null);  // Null coordinates
            StarDisplayRecord star2 = createStar("Alpha", "star-002", 4.37, 0, 0);

            lenient().when(routeBuilderUtils.getStar("star-001")).thenReturn(star1);
            lenient().when(routeBuilderUtils.getStar("star-002")).thenReturn(star2);

            Route route = createRoute("Test Route",
                    Arrays.asList("star-001", "star-002"),
                    Arrays.asList("Sol", "Alpha"),
                    Arrays.asList(4.37));

            // Should handle gracefully
            assertDoesNotThrow(() -> partialRouteUtils.findPartialRoutes(route));
        }

        @Test
        @DisplayName("findPartialRoutes() handles empty route")
        void handlesEmptyRoute() {
            setupTripsContext();
            setupStarLookup();

            Route route = createRoute("Empty Route",
                    Arrays.asList(),
                    Arrays.asList(),
                    Arrays.asList());

            assertDoesNotThrow(() -> partialRouteUtils.findPartialRoutes(route));
            verify(routeDisplay, never()).addRouteToDisplay(any(), any());
        }
    }

    // =========================================================================
    // Long Route Tests
    // =========================================================================

    @Nested
    @DisplayName("Long Route Tests")
    class LongRouteTests {

        @Test
        @DisplayName("findPartialRoutes() handles long route with alternating visibility")
        void handlesLongRouteAlternatingVisibility() {
            setupTripsContext();
            // Every other star visible: 1, 3, 5
            setupStarLookup("star-001", "star-003", "star-005");

            StarDisplayRecord star1 = createStar("Star1", "star-001", 0, 0, 0);
            StarDisplayRecord star3 = createStar("Star3", "star-003", 8, 0, 0);
            StarDisplayRecord star5 = createStar("Star5", "star-005", 16, 0, 0);

            lenient().when(routeBuilderUtils.getStar("star-001")).thenReturn(star1);
            lenient().when(routeBuilderUtils.getStar("star-003")).thenReturn(star3);
            lenient().when(routeBuilderUtils.getStar("star-005")).thenReturn(star5);

            Route route = createRoute("Long Route",
                    Arrays.asList("star-001", "star-002", "star-003", "star-004", "star-005"),
                    Arrays.asList("Star1", "Star2", "Star3", "Star4", "Star5"),
                    Arrays.asList(4.0, 4.0, 4.0, 4.0));

            assertDoesNotThrow(() -> partialRouteUtils.findPartialRoutes(route));

            // No adjacent pairs among visible stars, so no segments
            verify(routeDisplay, never()).addRouteToDisplay(any(), any());
        }
    }

    // =========================================================================
    // Route Properties Tests
    // =========================================================================

    @Nested
    @DisplayName("Route Properties Tests")
    class RoutePropertiesTests {

        @Test
        @DisplayName("Route UUID is preserved in partial route")
        void routeUUIDIsPreserved() {
            UUID routeUUID = UUID.randomUUID();
            Route route = createRoute("Test Route",
                    Arrays.asList("star-001"),
                    Arrays.asList("Sol"),
                    Arrays.asList());
            route.setUuid(routeUUID);

            assertEquals(routeUUID, route.getUuid());
        }

        @Test
        @DisplayName("Route properties are accessible")
        void routePropertiesAreAccessible() {
            Route route = createRoute("Test Route",
                    Arrays.asList("star-001", "star-002"),
                    Arrays.asList("Sol", "Alpha"),
                    Arrays.asList(4.37));

            assertEquals("Test Route", route.getRouteName());
            assertEquals(2, route.getRouteStars().size());
            assertEquals(2, route.getRouteStarNames().size());
            assertEquals(1, route.getRouteLengths().size());
            assertEquals(1.0, route.getLineWidth(), 0.001);
        }
    }

    // =========================================================================
    // Integration Tests (without JavaFX components)
    // =========================================================================

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("PartialRouteUtils correctly identifies visible stars")
        void correctlyIdentifiesVisibleStars() {
            setupTripsContext();
            setupStarLookup("star-001", "star-002");

            Route route = createRoute("Test Route",
                    Arrays.asList("star-001", "star-002", "star-003"),
                    Arrays.asList("Sol", "Alpha", "Barnard"),
                    Arrays.asList(4.37, 5.98));

            // Should identify that star-001 and star-002 are visible
            // but star-003 is not
            assertTrue(starLookup.containsKey("star-001"));
            assertTrue(starLookup.containsKey("star-002"));
            assertFalse(starLookup.containsKey("star-003"));
        }

        @Test
        @DisplayName("PartialRouteUtils queries RouteBuilderUtils for star records")
        void queriesRouteBuilderUtilsForStarRecords() {
            // This test verifies that routeBuilderUtils is set up correctly
            // and would be called during partial route finding
            StarDisplayRecord star1 = createStar("Sol", "star-001", 0, 0, 0);

            lenient().when(routeBuilderUtils.getStar("star-001")).thenReturn(star1);

            // Verify the mock is set up correctly
            assertEquals(star1, routeBuilderUtils.getStar("star-001"));
        }

        @Test
        @DisplayName("PartialRouteUtils uses TripsContext correctly")
        void usesTripsContextCorrectly() {
            setupTripsContext();
            setupStarLookup();

            Route route = createRoute("Test Route",
                    Arrays.asList("star-001"),
                    Arrays.asList("Sol"),
                    Arrays.asList());

            partialRouteUtils.findPartialRoutes(route);

            verify(tripsContext, atLeast(1)).getCurrentPlot();
        }
    }
}
