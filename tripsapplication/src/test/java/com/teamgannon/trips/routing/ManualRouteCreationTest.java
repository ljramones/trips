package com.teamgannon.trips.routing;

import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.config.application.model.AppViewPreferences;
import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.events.RoutingStatusEvent;
import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.routing.routemanagement.CurrentManualRoute;
import com.teamgannon.trips.routing.routemanagement.RouteBuilderUtils;
import com.teamgannon.trips.routing.routemanagement.RouteDisplay;
import com.teamgannon.trips.routing.routemanagement.RouteGraphicsUtil;
import com.teamgannon.trips.support.AlertFactory;
import com.teamgannon.trips.test.TestFXBase;
import javafx.geometry.Point3D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Cylinder;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.context.ApplicationEventPublisher;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for manual route creation workflow.
 * <p>
 * These tests verify the CurrentManualRoute class which manages
 * the step-by-step creation of routes by the user.
 * <p>
 * Tests cover:
 * <ul>
 *   <li>Starting a route from a star</li>
 *   <li>Adding segments to a route</li>
 *   <li>Finishing a route</li>
 *   <li>Removing segments (undo)</li>
 *   <li>Resetting a route</li>
 *   <li>Event publishing</li>
 *   <li>Route descriptor building</li>
 * </ul>
 */
@DisplayName("Manual Route Creation Tests")
class ManualRouteCreationTest {

    // =========================================================================
    // Test Helpers
    // =========================================================================

    private static StarDisplayRecord createStar(String name, double x, double y, double z) {
        StarDisplayRecord record = new StarDisplayRecord();
        record.setStarName(name);
        record.setX(x);
        record.setY(y);
        record.setZ(z);
        record.setActualCoordinates(new double[]{x, y, z});
        record.setCoordinates(new Point3D(x, y, z));
        record.setRecordId(UUID.randomUUID().toString());
        return record;
    }

    // =========================================================================
    // CurrentManualRoute Tests (using TestFX for headless JavaFX)
    // =========================================================================

    @Nested
    @DisplayName("CurrentManualRoute Tests")
    class CurrentManualRouteTests extends TestFXBase {

        private CurrentManualRoute currentManualRoute;
        private TripsContext tripsContext;
        private RouteDisplay routeDisplay;
        private RouteGraphicsUtil routeGraphicsUtil;
        private RouteBuilderUtils routeBuilderUtils;
        private ApplicationEventPublisher eventPublisher;
        private DataSetDescriptor dataSet;

        @Override
        public void start(Stage stage) {
            // Minimal scene required for TestFX
            stage.setScene(new Scene(new StackPane(), 100, 100));
            stage.show();
        }

        @BeforeEach
        void setUp() {
            // Create mocks
            tripsContext = mock(TripsContext.class);
            routeDisplay = mock(RouteDisplay.class);
            routeGraphicsUtil = mock(RouteGraphicsUtil.class);
            routeBuilderUtils = mock(RouteBuilderUtils.class);
            eventPublisher = mock(ApplicationEventPublisher.class);

            // Setup mock return values
            AppViewPreferences appViewPreferences = mock(AppViewPreferences.class);
            ColorPalette colorPalette = new ColorPalette();
            when(tripsContext.getAppViewPreferences()).thenReturn(appViewPreferences);
            when(appViewPreferences.getColorPalette()).thenReturn(colorPalette);

            // Mock CurrentPlot for route registration
            com.teamgannon.trips.config.application.model.CurrentPlot currentPlot =
                    mock(com.teamgannon.trips.config.application.model.CurrentPlot.class);
            when(tripsContext.getCurrentPlot()).thenReturn(currentPlot);

            // Create the object under test
            currentManualRoute = new CurrentManualRoute(
                    tripsContext,
                    routeDisplay,
                    routeGraphicsUtil,
                    routeBuilderUtils,
                    eventPublisher
            );

            // Create test data set
            dataSet = new DataSetDescriptor();
            dataSet.setDataSetName("test-dataset");
        }

        private RouteDescriptor createRouteDescriptor(String name, Color color) {
            return RouteDescriptor.builder()
                    .name(name)
                    .color(color)
                    .lineWidth(0.5)
                    .build();
        }

        @Test
        @DisplayName("startRoute initializes route correctly")
        void startRouteInitializesRouteCorrectly() {
            when(routeGraphicsUtil.createLabel(anyBoolean(), anyDouble())).thenReturn(new Label("5.0 LY"));
            when(routeGraphicsUtil.createLineSegment(any(), any(), anyDouble(), any(), any()))
                    .thenReturn(new Cylinder());

            StarDisplayRecord firstStar = createStar("Sol", 0, 0, 0);
            RouteDescriptor routeDescriptor = createRouteDescriptor("Test Route", Color.BLUE);

            currentManualRoute.startRoute(dataSet, routeDescriptor, firstStar);

            assertNotNull(currentManualRoute.getCurrentRoute());
            assertEquals(routeDescriptor, currentManualRoute.getCurrentRoute());
        }

        @Test
        @DisplayName("startRoute publishes RoutingStatusEvent")
        void startRoutePublishesRoutingStatusEvent() {
            when(routeGraphicsUtil.createLabel(anyBoolean(), anyDouble())).thenReturn(new Label("5.0 LY"));
            when(routeGraphicsUtil.createLineSegment(any(), any(), anyDouble(), any(), any()))
                    .thenReturn(new Cylinder());

            StarDisplayRecord firstStar = createStar("Sol", 0, 0, 0);
            RouteDescriptor routeDescriptor = createRouteDescriptor("Test Route", Color.BLUE);

            currentManualRoute.startRoute(dataSet, routeDescriptor, firstStar);

            ArgumentCaptor<RoutingStatusEvent> eventCaptor = ArgumentCaptor.forClass(RoutingStatusEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());

            RoutingStatusEvent event = eventCaptor.getValue();
            assertTrue(event.isStatusFlag(), "Routing should be active after starting");
        }

        @Test
        @DisplayName("startRoute adds first coordinate")
        void startRouteAddsFirstCoordinate() {
            when(routeGraphicsUtil.createLabel(anyBoolean(), anyDouble())).thenReturn(new Label("5.0 LY"));
            when(routeGraphicsUtil.createLineSegment(any(), any(), anyDouble(), any(), any()))
                    .thenReturn(new Cylinder());

            StarDisplayRecord firstStar = createStar("Sol", 0, 0, 0);
            RouteDescriptor routeDescriptor = createRouteDescriptor("Test Route", Color.BLUE);

            currentManualRoute.startRoute(dataSet, routeDescriptor, firstStar);

            assertEquals(1, currentManualRoute.getNumberSegments());
        }

        @Test
        @DisplayName("startRoute activates manual routing mode")
        void startRouteActivatesManualRoutingMode() {
            when(routeGraphicsUtil.createLabel(anyBoolean(), anyDouble())).thenReturn(new Label("5.0 LY"));
            when(routeGraphicsUtil.createLineSegment(any(), any(), anyDouble(), any(), any()))
                    .thenReturn(new Cylinder());

            StarDisplayRecord firstStar = createStar("Sol", 0, 0, 0);
            RouteDescriptor routeDescriptor = createRouteDescriptor("Test Route", Color.BLUE);

            currentManualRoute.startRoute(dataSet, routeDescriptor, firstStar);

            verify(routeDisplay).setManualRoutingActive(true);
        }

        @Test
        @DisplayName("continueRoute adds segment to route")
        void continueRouteAddsSegmentToRoute() {
            when(routeGraphicsUtil.createLabel(anyBoolean(), anyDouble())).thenReturn(new Label("5.0 LY"));
            when(routeGraphicsUtil.createLineSegment(any(), any(), anyDouble(), any(), any()))
                    .thenReturn(new Cylinder());
            when(routeDisplay.isManualRoutingActive()).thenReturn(true);

            StarDisplayRecord firstStar = createStar("Sol", 0, 0, 0);
            RouteDescriptor routeDescriptor = createRouteDescriptor("Test Route", Color.BLUE);
            currentManualRoute.startRoute(dataSet, routeDescriptor, firstStar);

            StarDisplayRecord nextStar = createStar("Alpha Centauri", 4, 0, 0);
            currentManualRoute.continueRoute(nextStar);

            assertEquals(2, currentManualRoute.getNumberSegments());
        }

        @Test
        @DisplayName("finishRoute deactivates manual routing")
        void finishRouteDeactivatesManualRouting() {
            when(routeGraphicsUtil.createLabel(anyBoolean(), anyDouble())).thenReturn(new Label("5.0 LY"));
            when(routeGraphicsUtil.createLineSegment(any(), any(), anyDouble(), any(), any()))
                    .thenReturn(new Cylinder());
            when(routeDisplay.isManualRoutingActive()).thenReturn(true);

            StarDisplayRecord firstStar = createStar("Sol", 0, 0, 0);
            RouteDescriptor routeDescriptor = createRouteDescriptor("Test Route", Color.BLUE);
            currentManualRoute.startRoute(dataSet, routeDescriptor, firstStar);

            StarDisplayRecord endStar = createStar("Alpha Centauri", 4, 0, 0);
            currentManualRoute.finishRoute(endStar);

            verify(routeDisplay).setManualRoutingActive(false);
        }

        @Test
        @DisplayName("continueRoute without starting does nothing")
        void continueRouteWithoutStartingDoesNothing() {
            // Mock the static AlertFactory to prevent blocking dialog
            try (MockedStatic<AlertFactory> mockedAlert = mockStatic(AlertFactory.class)) {
                when(routeDisplay.isManualRoutingActive()).thenReturn(false);
                StarDisplayRecord star = createStar("Sol", 0, 0, 0);

                // Should not throw (shows error alert but we mock it)
                assertDoesNotThrow(() -> currentManualRoute.continueRoute(star));

                // Verify the error alert was called
                mockedAlert.verify(() -> AlertFactory.showErrorAlert("Routing", "start a route first"));
            }
        }

        @Test
        @DisplayName("finishRoute without active routing does nothing")
        void finishRouteWithoutActiveRoutingDoesNothing() {
            when(routeDisplay.isManualRoutingActive()).thenReturn(false);

            assertDoesNotThrow(() -> currentManualRoute.finishRoute());
        }
    }

    // =========================================================================
    // RouteDescriptor Unit Tests (don't require JavaFX runtime for most)
    // =========================================================================

    @Nested
    @DisplayName("RouteDescriptor Direct Tests")
    class RouteDescriptorDirectTests {

        @Test
        @DisplayName("RouteDescriptor builds with correct defaults")
        void routeDescriptorBuildsWithCorrectDefaults() {
            RouteDescriptor descriptor = RouteDescriptor.builder()
                    .name("Test Route")
                    .build();

            assertNotNull(descriptor.getId());
            assertNotNull(descriptor.getColor());
            assertNotNull(descriptor.getRouteList());
            assertNotNull(descriptor.getRouteCoordinates());
        }

        @Test
        @DisplayName("getRouteSegments returns correct segments")
        void getRouteSegmentsReturnsCorrectSegments() {
            RouteDescriptor descriptor = RouteDescriptor.builder()
                    .name("Test Route")
                    .build();

            descriptor.getRouteCoordinates().add(new Point3D(0, 0, 0));
            descriptor.getRouteCoordinates().add(new Point3D(4, 0, 0));
            descriptor.getRouteCoordinates().add(new Point3D(8, 0, 0));

            var segments = descriptor.getRouteSegments();

            assertEquals(2, segments.size());
        }

        @Test
        @DisplayName("Route descriptor accumulates coordinates")
        void routeDescriptorAccumulatesCoordinates() {
            RouteDescriptor descriptor = RouteDescriptor.builder()
                    .name("Test Route")
                    .build();

            descriptor.addLineSegment(new Point3D(0, 0, 0));
            descriptor.addLineSegment(new Point3D(4, 0, 0));
            descriptor.addLineSegment(new Point3D(8, 0, 0));

            assertEquals(3, descriptor.getRouteCoordinates().size());
        }

        @Test
        @DisplayName("Route descriptor accumulates lengths")
        void routeDescriptorAccumulatesLengths() {
            RouteDescriptor descriptor = RouteDescriptor.builder()
                    .name("Test Route")
                    .build();

            descriptor.addLengthSegment(4.0);
            descriptor.addLengthSegment(3.5);
            descriptor.addLengthSegment(5.2);

            assertEquals(3, descriptor.getLengthList().size());
            assertEquals(4.0, descriptor.getLengthList().get(0), 0.01);
        }

        @Test
        @DisplayName("findTotalLength sums all lengths")
        void findTotalLengthSumsAllLengths() {
            RouteDescriptor descriptor = RouteDescriptor.builder()
                    .name("Test Route")
                    .build();

            descriptor.getLengthList().add(4.0);
            descriptor.getLengthList().add(3.5);
            descriptor.getLengthList().add(5.2);

            double total = descriptor.findTotalLength();

            assertEquals(12.7, total, 0.01);
        }

        @Test
        @DisplayName("clear resets route descriptor")
        void clearResetsRouteDescriptor() {
            RouteDescriptor descriptor = RouteDescriptor.builder()
                    .name("Test Route")
                    .color(Color.RED)
                    .build();

            descriptor.getRouteCoordinates().add(new Point3D(0, 0, 0));
            descriptor.getRouteCoordinates().add(new Point3D(4, 0, 0));
            descriptor.getRouteList().add("star-id-1");

            descriptor.clear();

            assertEquals("", descriptor.getName());
            assertTrue(descriptor.getRouteList().isEmpty());
            assertTrue(descriptor.getRouteCoordinates().isEmpty());
        }

        @Test
        @DisplayName("RouteDescriptor has correct line width")
        void routeDescriptorHasCorrectLineWidth() {
            RouteDescriptor descriptor = RouteDescriptor.builder()
                    .name("Test Route")
                    .lineWidth(2.5)
                    .build();

            assertEquals(2.5, descriptor.getLineWidth(), 0.01);
        }

        @Test
        @DisplayName("RouteDescriptor has correct color")
        void routeDescriptorHasCorrectColor() {
            RouteDescriptor descriptor = RouteDescriptor.builder()
                    .name("Test Route")
                    .color(Color.CYAN)
                    .build();

            assertEquals(Color.CYAN, descriptor.getColor());
        }

        @Test
        @DisplayName("RouteDescriptor mutateCoordinates modifies positions")
        void routeDescriptorMutateCoordinatesModifiesPositions() {
            RouteDescriptor descriptor = RouteDescriptor.builder()
                    .name("Test Route")
                    .build();

            descriptor.getRouteCoordinates().add(new Point3D(0, 0, 0));
            descriptor.getRouteCoordinates().add(new Point3D(4, 0, 0));

            com.teamgannon.trips.routing.model.RouteSegment segment =
                    new com.teamgannon.trips.routing.model.RouteSegment();
            segment.setIndexA(0);
            segment.setIndexB(1);

            descriptor.mutateCoordinates(segment);

            // Coordinates should have been shifted by 2
            assertEquals(2.0, descriptor.getRouteCoordinates().get(0).getX(), 0.01);
            assertEquals(6.0, descriptor.getRouteCoordinates().get(1).getX(), 0.01);
        }
    }

    // =========================================================================
    // RouteDescriptor JavaFX Tests (using TestFX for headless JavaFX)
    // =========================================================================

    @Nested
    @DisplayName("RouteDescriptor JavaFX Tests")
    class RouteDescriptorJavaFxTests extends TestFXBase {

        @Override
        public void start(Stage stage) {
            // Minimal scene required for TestFX
            stage.setScene(new Scene(new StackPane(), 100, 100));
            stage.show();
        }

        @Test
        @DisplayName("addLink populates route correctly")
        void addLinkPopulatesRouteCorrectly() {
            RouteDescriptor descriptor = RouteDescriptor.builder()
                    .name("Test Route")
                    .build();

            StarDisplayRecord star1 = createStar("Sol", 0, 0, 0);
            StarDisplayRecord star2 = createStar("Alpha", 4, 0, 0);

            descriptor.addLink(star1, new Point3D(0, 0, 0), 0, null, null);
            descriptor.addLink(star2, new Point3D(4, 0, 0), 4.0, new Cylinder(), new Label("4.0 LY"));

            assertEquals(2, descriptor.getRouteCoordinates().size());
            assertEquals(1, descriptor.getLengthList().size());
            assertEquals(4.0, descriptor.getTotalLength(), 0.01);
        }

        @Test
        @DisplayName("removeLast removes segment correctly")
        void removeLastRemovesSegmentCorrectly() {
            RouteDescriptor descriptor = RouteDescriptor.builder()
                    .name("Test Route")
                    .build();

            StarDisplayRecord star1 = createStar("Sol", 0, 0, 0);
            StarDisplayRecord star2 = createStar("Alpha", 4, 0, 0);
            StarDisplayRecord star3 = createStar("Sirius", 8, 0, 0);

            descriptor.addLink(star1, new Point3D(0, 0, 0), 0, null, null);
            descriptor.addLink(star2, new Point3D(4, 0, 0), 4.0, new Cylinder(), new Label("4.0"));
            descriptor.addLink(star3, new Point3D(8, 0, 0), 4.0, new Cylinder(), new Label("4.0"));

            assertEquals(3, descriptor.getRouteCoordinates().size());

            boolean allRemoved = descriptor.removeLast();

            assertFalse(allRemoved);
            assertEquals(2, descriptor.getRouteCoordinates().size());
            assertEquals("Alpha", descriptor.getLastStar().getStarName());
        }

        @Test
        @DisplayName("toRoute creates correct Route object")
        void toRouteCreatesCorrectRouteObject() {
            RouteDescriptor descriptor = RouteDescriptor.builder()
                    .name("Test Route")
                    .color(Color.BLUE)
                    .lineWidth(1.5)
                    .routeNotes("Test notes")
                    .startStar("Sol")
                    .build();

            descriptor.getRouteList().add("star-id-1");
            descriptor.getRouteList().add("star-id-2");
            descriptor.getNameList().add("Sol");
            descriptor.getNameList().add("Alpha");
            descriptor.getLengthList().add(4.0);

            var route = descriptor.toRoute();

            assertEquals("Test Route", route.getRouteName());
            assertEquals(Color.BLUE.toString(), route.getRouteColor());
            assertEquals(1.5, route.getLineWidth(), 0.01);
            assertEquals("Test notes", route.getRouteNotes());
            assertEquals("Sol", route.getStartingStar());
            assertEquals(2, route.getRouteStars().size());
        }
    }
}
