package com.teamgannon.trips.routing.routemanagement;

import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.RouteVisibility;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import javafx.geometry.Point3D;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Tests for CurrentManualRoute.
 * <p>
 * Tests cover:
 * <ul>
 *   <li>Construction and setup</li>
 *   <li>Route descriptor management</li>
 *   <li>Helper method behaviors</li>
 * </ul>
 * <p>
 * Note: Tests involving full routing workflows are limited due to JavaFX toolkit requirements.
 * Integration tests should be performed with a proper JavaFX test framework.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CurrentManualRouteTest {

    @Mock
    private TripsContext tripsContext;

    @Mock
    private RouteDisplay routeDisplay;

    @Mock
    private RouteGraphicsUtil routeGraphicsUtil;

    @Mock
    private RouteBuilderUtils routeBuilderUtils;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private CurrentManualRoute currentManualRoute;

    @BeforeEach
    void setUp() {
        currentManualRoute = new CurrentManualRoute(
                tripsContext,
                routeDisplay,
                routeGraphicsUtil,
                routeBuilderUtils,
                eventPublisher
        );
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

    private RouteDescriptor createRouteDescriptor(String name) {
        return RouteDescriptor.builder()
                .name(name)
                .color(Color.CYAN)
                .lineWidth(1.0)
                .startStar("Sol")
                .routeCoordinates(new ArrayList<>())
                .routeList(new ArrayList<>())
                .visibility(RouteVisibility.FULL)
                .build();
    }

    private DataSetDescriptor createDataSetDescriptor() {
        DataSetDescriptor descriptor = new DataSetDescriptor();
        descriptor.setDataSetName("Test Dataset");
        return descriptor;
    }

    // =========================================================================
    // Construction Tests
    // =========================================================================

    @Nested
    @DisplayName("Construction Tests")
    class ConstructionTests {

        @Test
        @DisplayName("CurrentManualRoute can be constructed with dependencies")
        void canBeConstructed() {
            assertNotNull(currentManualRoute);
        }

        @Test
        @DisplayName("Constructor sets tripsContext")
        void constructorSetsTripsContext() {
            assertEquals(tripsContext, currentManualRoute.getTripsContext());
        }

        @Test
        @DisplayName("Constructor sets routeDisplay")
        void constructorSetsRouteDisplay() {
            assertEquals(routeDisplay, currentManualRoute.getRouteDisplay());
        }

        @Test
        @DisplayName("Constructor sets eventPublisher")
        void constructorSetsEventPublisher() {
            assertEquals(eventPublisher, currentManualRoute.getEventPublisher());
        }

        @Test
        @DisplayName("Initial currentRoute is null")
        void initialCurrentRouteIsNull() {
            assertNull(currentManualRoute.getCurrentRoute());
        }

        @Test
        @DisplayName("currentRouteNodePoints starts empty")
        void currentRouteNodePointsStartsEmpty() {
            assertTrue(currentManualRoute.getCurrentRouteNodePoints().isEmpty());
        }

        @Test
        @DisplayName("starNameStack starts empty")
        void starNameStackStartsEmpty() {
            assertTrue(currentManualRoute.getStarNameStack().isEmpty());
        }
    }

    // =========================================================================
    // Setup Tests
    // =========================================================================

    @Nested
    @DisplayName("Setup Tests")
    class SetupTests {

        @Test
        @DisplayName("setup() sets current route")
        void setupSetsCurrentRoute() {
            RouteDescriptor routeDescriptor = createRouteDescriptor("Test Route");
            DataSetDescriptor dataSetDescriptor = createDataSetDescriptor();

            currentManualRoute.setup(dataSetDescriptor, routeDescriptor);

            assertEquals(routeDescriptor, currentManualRoute.getCurrentRoute());
        }

        @Test
        @DisplayName("setup() sets descriptor on route")
        void setupSetsDescriptorOnRoute() {
            RouteDescriptor routeDescriptor = createRouteDescriptor("Test Route");
            DataSetDescriptor dataSetDescriptor = createDataSetDescriptor();

            currentManualRoute.setup(dataSetDescriptor, routeDescriptor);

            assertEquals(dataSetDescriptor, routeDescriptor.getDescriptor());
        }

        @Test
        @DisplayName("setup() activates manual routing")
        void setupActivatesManualRouting() {
            RouteDescriptor routeDescriptor = createRouteDescriptor("Test Route");
            DataSetDescriptor dataSetDescriptor = createDataSetDescriptor();

            currentManualRoute.setup(dataSetDescriptor, routeDescriptor);

            verify(routeDisplay).setManualRoutingActive(true);
        }
    }

    // =========================================================================
    // Helper Method Tests (no JavaFX initialization required)
    // =========================================================================

    @Nested
    @DisplayName("Helper Method Tests")
    class HelperMethodTests {

        @Test
        @DisplayName("getRouteId() returns null when no route")
        void getRouteIdReturnsNullWhenNoRoute() {
            assertNull(currentManualRoute.getRouteId());
        }

        @Test
        @DisplayName("getRouteId() returns UUID when route exists")
        void getRouteIdReturnsUUIDWhenRouteExists() {
            RouteDescriptor routeDescriptor = createRouteDescriptor("Test Route");
            DataSetDescriptor dataSetDescriptor = createDataSetDescriptor();
            currentManualRoute.setup(dataSetDescriptor, routeDescriptor);

            UUID routeId = currentManualRoute.getRouteId();

            assertNotNull(routeId);
            assertEquals(routeDescriptor.getId(), routeId);
        }

        @Test
        @DisplayName("getNumberSegments() returns coordinate count after setup")
        void getNumberSegmentsReturnsCoordinateCountAfterSetup() {
            RouteDescriptor routeDescriptor = createRouteDescriptor("Test Route");
            DataSetDescriptor dataSetDescriptor = createDataSetDescriptor();
            currentManualRoute.setup(dataSetDescriptor, routeDescriptor);

            int segments = currentManualRoute.getNumberSegments();

            // After setup (no starts added yet), should be 0
            assertEquals(0, segments);
        }

        @Test
        @DisplayName("getRouteColor() returns route color after setup")
        void getRouteColorReturnsRouteColorAfterSetup() {
            RouteDescriptor routeDescriptor = createRouteDescriptor("Test Route");
            routeDescriptor.setColor(Color.MAGENTA);
            DataSetDescriptor dataSetDescriptor = createDataSetDescriptor();
            currentManualRoute.setup(dataSetDescriptor, routeDescriptor);

            Color color = currentManualRoute.getRouteColor();

            assertEquals(Color.MAGENTA, color);
        }

        @Test
        @DisplayName("getLineWidth() returns route line width after setup")
        void getLineWidthReturnsRouteLineWidthAfterSetup() {
            RouteDescriptor routeDescriptor = createRouteDescriptor("Test Route");
            routeDescriptor.setLineWidth(2.5);
            DataSetDescriptor dataSetDescriptor = createDataSetDescriptor();
            currentManualRoute.setup(dataSetDescriptor, routeDescriptor);

            double lineWidth = currentManualRoute.getLineWidth();

            assertEquals(2.5, lineWidth, 0.001);
        }
    }

    // =========================================================================
    // Route Descriptor Management Tests
    // =========================================================================

    @Nested
    @DisplayName("Route Descriptor Management Tests")
    class RouteDescriptorManagementTests {

        @Test
        @DisplayName("getCurrentRoute() returns null before setup")
        void getCurrentRouteReturnsNullBeforeSetup() {
            assertNull(currentManualRoute.getCurrentRoute());
        }

        @Test
        @DisplayName("getCurrentRoute() returns route after setup")
        void getCurrentRouteReturnsRouteAfterSetup() {
            RouteDescriptor routeDescriptor = createRouteDescriptor("Test Route");
            DataSetDescriptor dataSetDescriptor = createDataSetDescriptor();
            currentManualRoute.setup(dataSetDescriptor, routeDescriptor);

            assertEquals(routeDescriptor, currentManualRoute.getCurrentRoute());
        }

        @Test
        @DisplayName("setCurrentRoute() updates current route")
        void setCurrentRouteUpdatesCurrentRoute() {
            RouteDescriptor routeDescriptor1 = createRouteDescriptor("Route 1");
            RouteDescriptor routeDescriptor2 = createRouteDescriptor("Route 2");

            currentManualRoute.setCurrentRoute(routeDescriptor1);
            assertEquals(routeDescriptor1, currentManualRoute.getCurrentRoute());

            currentManualRoute.setCurrentRoute(routeDescriptor2);
            assertEquals(routeDescriptor2, currentManualRoute.getCurrentRoute());
        }
    }

    // =========================================================================
    // Dependency Interaction Tests
    // =========================================================================

    @Nested
    @DisplayName("Dependency Interaction Tests")
    class DependencyInteractionTests {

        @Test
        @DisplayName("setup() calls routeDisplay.setManualRoutingActive(true)")
        void setupCallsSetManualRoutingActive() {
            RouteDescriptor routeDescriptor = createRouteDescriptor("Test Route");
            DataSetDescriptor dataSetDescriptor = createDataSetDescriptor();

            currentManualRoute.setup(dataSetDescriptor, routeDescriptor);

            verify(routeDisplay, times(1)).setManualRoutingActive(true);
        }

        @Test
        @DisplayName("Multiple setup calls each activate routing")
        void multipleSetupCallsEachActivateRouting() {
            RouteDescriptor route1 = createRouteDescriptor("Route 1");
            RouteDescriptor route2 = createRouteDescriptor("Route 2");
            DataSetDescriptor dataSetDescriptor = createDataSetDescriptor();

            currentManualRoute.setup(dataSetDescriptor, route1);
            currentManualRoute.setup(dataSetDescriptor, route2);

            verify(routeDisplay, times(2)).setManualRoutingActive(true);
        }
    }

    // =========================================================================
    // Star Name Stack Tests
    // =========================================================================

    @Nested
    @DisplayName("Star Name Stack Tests")
    class StarNameStackTests {

        @Test
        @DisplayName("getStarNameStack() returns empty stack initially")
        void getStarNameStackReturnsEmptyStackInitially() {
            assertTrue(currentManualRoute.getStarNameStack().isEmpty());
        }

        @Test
        @DisplayName("Star stack can be manipulated directly")
        void starStackCanBeManipulatedDirectly() {
            StarDisplayRecord star = createStar("Sol", "star-001", 0, 0, 0);

            currentManualRoute.getStarNameStack().push(star);

            assertEquals(1, currentManualRoute.getStarNameStack().size());
            assertEquals(star, currentManualRoute.getStarNameStack().peek());
        }
    }

    // =========================================================================
    // Route Node Points Tests
    // =========================================================================

    @Nested
    @DisplayName("Route Node Points Tests")
    class RouteNodePointsTests {

        @Test
        @DisplayName("getCurrentRouteNodePoints() returns empty list initially")
        void getCurrentRouteNodePointsReturnsEmptyListInitially() {
            assertTrue(currentManualRoute.getCurrentRouteNodePoints().isEmpty());
        }

        @Test
        @DisplayName("currentRouteNodePoints can be modified directly")
        void currentRouteNodePointsCanBeModifiedDirectly() {
            // Can add objects to the list
            currentManualRoute.getCurrentRouteNodePoints().add(null);
            assertEquals(1, currentManualRoute.getCurrentRouteNodePoints().size());
        }
    }

    // =========================================================================
    // Edge Case Tests
    // =========================================================================

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("getRouteColor() throws when no route set")
        void getRouteColorThrowsWhenNoRouteSet() {
            assertThrows(NullPointerException.class, () -> currentManualRoute.getRouteColor());
        }

        @Test
        @DisplayName("getLineWidth() throws when no route set")
        void getLineWidthThrowsWhenNoRouteSet() {
            assertThrows(NullPointerException.class, () -> currentManualRoute.getLineWidth());
        }

        @Test
        @DisplayName("getNumberSegments() throws when no route set")
        void getNumberSegmentsThrowsWhenNoRouteSet() {
            assertThrows(NullPointerException.class, () -> currentManualRoute.getNumberSegments());
        }

        @Test
        @DisplayName("setup() with null route descriptor throws")
        void setupWithNullRouteDescriptorThrows() {
            DataSetDescriptor dataSetDescriptor = createDataSetDescriptor();

            // Will throw when trying to call setDescriptor on null
            assertThrows(NullPointerException.class,
                () -> currentManualRoute.setup(dataSetDescriptor, null));
        }
    }

    // =========================================================================
    // Contract Tests
    // =========================================================================

    @Nested
    @DisplayName("Contract Tests")
    class ContractTests {

        @Test
        @DisplayName("All dependencies are stored correctly")
        void allDependenciesAreStoredCorrectly() {
            assertEquals(tripsContext, currentManualRoute.getTripsContext());
            assertEquals(routeDisplay, currentManualRoute.getRouteDisplay());
            assertEquals(routeGraphicsUtil, currentManualRoute.getRouteGraphicsUtil());
            assertEquals(routeBuilderUtils, currentManualRoute.getRouteBuilderUtils());
            assertEquals(eventPublisher, currentManualRoute.getEventPublisher());
        }

        @Test
        @DisplayName("Route descriptor color is preserved")
        void routeDescriptorColorIsPreserved() {
            RouteDescriptor routeDescriptor = createRouteDescriptor("Test Route");
            routeDescriptor.setColor(Color.GOLD);
            DataSetDescriptor dataSetDescriptor = createDataSetDescriptor();

            currentManualRoute.setup(dataSetDescriptor, routeDescriptor);

            assertEquals(Color.GOLD, currentManualRoute.getRouteColor());
        }

        @Test
        @DisplayName("Route descriptor line width is preserved")
        void routeDescriptorLineWidthIsPreserved() {
            RouteDescriptor routeDescriptor = createRouteDescriptor("Test Route");
            routeDescriptor.setLineWidth(3.0);
            DataSetDescriptor dataSetDescriptor = createDataSetDescriptor();

            currentManualRoute.setup(dataSetDescriptor, routeDescriptor);

            assertEquals(3.0, currentManualRoute.getLineWidth(), 0.001);
        }
    }
}
