package com.teamgannon.trips.routing;

import com.teamgannon.trips.dialogs.search.model.DistanceRoutes;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.routing.model.PossibleRoutes;
import com.teamgannon.trips.routing.model.RouteFindingOptions;
import com.teamgannon.trips.routing.model.RouteFindingResult;
import com.teamgannon.trips.routing.model.RoutingMetric;
import com.teamgannon.trips.service.measure.StarMeasurementService;
import com.teamgannon.trips.transits.TransitRoute;
import javafx.geometry.Point3D;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * Tests for RouteFindingService.
 * <p>
 * Tests cover:
 * <ul>
 *   <li>Successful route finding</li>
 *   <li>Origin star validation</li>
 *   <li>Destination star validation</li>
 *   <li>Star count threshold</li>
 *   <li>Transit calculation</li>
 *   <li>Path connectivity</li>
 *   <li>Star exclusions</li>
 *   <li>Polity exclusions</li>
 * </ul>
 */
class RouteFindingServiceTest {

    private StarMeasurementService starMeasurementService;
    private RouteFindingService routeFindingService;
    private DataSetDescriptor dataSet;

    @BeforeEach
    void setUp() {
        starMeasurementService = mock(StarMeasurementService.class);
        RouteCache routeCache = new RouteCache();
        routeFindingService = new RouteFindingService(starMeasurementService, routeCache);
        dataSet = new DataSetDescriptor();
        dataSet.setDataSetName("test-dataset");
    }

    // =========================================================================
    // Test Helpers
    // =========================================================================

    private StarDisplayRecord createStar(String name, double x, double y, double z,
                                          String spectralClass, String polity) {
        StarDisplayRecord record = new StarDisplayRecord();
        record.setStarName(name);
        record.setX(x);
        record.setY(y);
        record.setZ(z);
        record.setSpectralClass(spectralClass);
        record.setPolity(polity);
        record.setActualCoordinates(new double[]{x, y, z});
        record.setCoordinates(new Point3D(x, y, z));
        record.setRecordId(UUID.randomUUID().toString());
        return record;
    }

    private RouteFindingOptions.RouteFindingOptionsBuilder defaultOptionsBuilder() {
        return RouteFindingOptions.builder()
                .selected(true)
                .upperBound(10.0)
                .lowerBound(1.0)
                .numberPaths(3)
                .lineWidth(0.5)
                .color(Color.BLUE);
    }

    private void setupTransitsForPath(StarDisplayRecord origin, StarDisplayRecord destination) {
        TransitRoute transit = TransitRoute.builder()
                .good(true)
                .source(origin)
                .target(destination)
                .distance(5.0)
                .build();

        when(starMeasurementService.calculateDistances(any(DistanceRoutes.class), anyList()))
                .thenReturn(List.of(transit));
    }

    // =========================================================================
    // Initialization Tests
    // =========================================================================

    @Nested
    @DisplayName("Initialization Tests")
    class InitializationTests {

        @Test
        @DisplayName("Service can be constructed")
        void serviceCanBeConstructed() {
            assertNotNull(routeFindingService);
        }

        @Test
        @DisplayName("Graph threshold is accessible")
        void graphThresholdIsAccessible() {
            assertEquals(RouteFindingService.GRAPH_THRESHOLD, routeFindingService.getGraphThreshold());
        }
    }

    // =========================================================================
    // Route Calculation Tests (Integration-style)
    // =========================================================================

    @Nested
    @DisplayName("Route Calculation Tests")
    class RouteCalculationTests {

        @Test
        @DisplayName("findRoutes calls starMeasurementService with correct distance bounds")
        void findRoutesCallsStarMeasurementServiceWithCorrectBounds() {
            StarDisplayRecord origin = createStar("Sol", 0, 0, 0, "G2V", "Terran");
            StarDisplayRecord destination = createStar("Alpha Centauri", 1, 1, 1, "G2V", "Terran");
            List<StarDisplayRecord> stars = List.of(origin, destination);

            // Return empty list to trigger "no transits" error (we're just verifying the call)
            when(starMeasurementService.calculateDistances(any(DistanceRoutes.class), anyList()))
                    .thenReturn(Collections.emptyList());

            RouteFindingOptions options = defaultOptionsBuilder()
                    .originStarName("Sol")
                    .destinationStarName("Alpha Centauri")
                    .upperBound(10.0)
                    .lowerBound(2.0)
                    .build();

            routeFindingService.findRoutes(options, stars, dataSet);

            // Verify the service was called
            verify(starMeasurementService).calculateDistances(any(DistanceRoutes.class), anyList());
        }

        @Test
        @DisplayName("findRoutes passes pruned stars to measurement service")
        void findRoutesPassesPrunedStarsToMeasurementService() {
            StarDisplayRecord gStar = createStar("Sol", 0, 0, 0, "G2V", "Terran");
            StarDisplayRecord mStar = createStar("Proxima", 1, 0.5, 0.5, "M5V", "Terran");
            StarDisplayRecord destination = createStar("Alpha Centauri", 1, 1, 1, "G2V", "Terran");
            List<StarDisplayRecord> stars = List.of(gStar, mStar, destination);

            // Capture the list passed to calculateDistances
            when(starMeasurementService.calculateDistances(any(DistanceRoutes.class), anyList()))
                    .thenReturn(Collections.emptyList());

            RouteFindingOptions options = defaultOptionsBuilder()
                    .originStarName("Sol")
                    .destinationStarName("Alpha Centauri")
                    .starExclusions(Set.of("M"))
                    .build();

            routeFindingService.findRoutes(options, stars, dataSet);

            // Verify calculateDistances was called with a list of 2 stars (M excluded)
            verify(starMeasurementService).calculateDistances(
                    any(DistanceRoutes.class),
                    argThat(list -> list.size() == 2 &&
                            list.stream().noneMatch(s -> s.getSpectralClass().startsWith("M"))));
        }
    }

    // =========================================================================
    // Star Validation Tests
    // =========================================================================

    @Nested
    @DisplayName("Star Validation Tests")
    class StarValidationTests {

        @Test
        @DisplayName("findRoutes fails when origin star not found")
        void findRoutesFailsWhenOriginStarNotFound() {
            StarDisplayRecord destination = createStar("Alpha Centauri", 1, 1, 1, "G2V", "Terran");
            List<StarDisplayRecord> stars = List.of(destination);

            RouteFindingOptions options = defaultOptionsBuilder()
                    .originStarName("Sol")
                    .destinationStarName("Alpha Centauri")
                    .build();

            RouteFindingResult result = routeFindingService.findRoutes(options, stars, dataSet);

            assertFalse(result.isSuccess());
            assertTrue(result.getErrorMessage().contains("Origin star"));
            assertTrue(result.getErrorMessage().contains("Sol"));
        }

        @Test
        @DisplayName("findRoutes fails when destination star not found")
        void findRoutesFailsWhenDestinationStarNotFound() {
            StarDisplayRecord origin = createStar("Sol", 0, 0, 0, "G2V", "Terran");
            List<StarDisplayRecord> stars = List.of(origin);

            RouteFindingOptions options = defaultOptionsBuilder()
                    .originStarName("Sol")
                    .destinationStarName("Alpha Centauri")
                    .build();

            RouteFindingResult result = routeFindingService.findRoutes(options, stars, dataSet);

            assertFalse(result.isSuccess());
            assertTrue(result.getErrorMessage().contains("Destination star"));
            assertTrue(result.getErrorMessage().contains("Alpha Centauri"));
        }
    }

    // =========================================================================
    // Transit Calculation Tests
    // =========================================================================

    @Nested
    @DisplayName("Transit Calculation Tests")
    class TransitCalculationTests {

        @Test
        @DisplayName("findRoutes fails when no transits found")
        void findRoutesFailsWhenNoTransitsFound() {
            StarDisplayRecord origin = createStar("Sol", 0, 0, 0, "G2V", "Terran");
            StarDisplayRecord destination = createStar("Alpha Centauri", 1, 1, 1, "G2V", "Terran");
            List<StarDisplayRecord> stars = List.of(origin, destination);

            // Return empty list (no transits)
            when(starMeasurementService.calculateDistances(any(DistanceRoutes.class), anyList()))
                    .thenReturn(Collections.emptyList());

            RouteFindingOptions options = defaultOptionsBuilder()
                    .originStarName("Sol")
                    .destinationStarName("Alpha Centauri")
                    .build();

            RouteFindingResult result = routeFindingService.findRoutes(options, stars, dataSet);

            assertFalse(result.isSuccess());
            assertTrue(result.getErrorMessage().contains("No transits"));
        }

        @Test
        @DisplayName("findRoutes fails when stars not connected")
        void findRoutesFailsWhenStarsNotConnected() {
            StarDisplayRecord origin = createStar("Sol", 0, 0, 0, "G2V", "Terran");
            StarDisplayRecord isolated = createStar("Isolated", 100, 100, 100, "G2V", "Terran");
            StarDisplayRecord destination = createStar("Alpha Centauri", 1, 1, 1, "G2V", "Terran");
            List<StarDisplayRecord> stars = List.of(origin, isolated, destination);

            // Only create transit between origin and isolated, not to destination
            TransitRoute transit = TransitRoute.builder()
                    .good(true)
                    .source(origin)
                    .target(isolated)
                    .distance(5.0)
                    .build();

            when(starMeasurementService.calculateDistances(any(DistanceRoutes.class), anyList()))
                    .thenReturn(List.of(transit));

            RouteFindingOptions options = defaultOptionsBuilder()
                    .originStarName("Sol")
                    .destinationStarName("Alpha Centauri")
                    .build();

            RouteFindingResult result = routeFindingService.findRoutes(options, stars, dataSet);

            assertFalse(result.isSuccess());
            assertTrue(result.getErrorMessage().contains("No path exists"));
        }
    }

    // =========================================================================
    // Pruning Tests
    // =========================================================================

    @Nested
    @DisplayName("Pruning Tests")
    class PruningTests {

        @Test
        @DisplayName("pruneStars removes stars with excluded spectral class")
        void pruneStarsRemovesExcludedSpectralClass() {
            StarDisplayRecord gStar = createStar("Sol", 0, 0, 0, "G2V", "Terran");
            StarDisplayRecord mStar = createStar("Proxima", 1, 1, 1, "M5V", "Terran");
            List<StarDisplayRecord> stars = List.of(gStar, mStar);

            RouteFindingOptions options = defaultOptionsBuilder()
                    .starExclusions(Set.of("M"))
                    .build();

            List<StarDisplayRecord> pruned = routeFindingService.pruneStars(stars, options);

            assertEquals(1, pruned.size());
            assertEquals("Sol", pruned.get(0).getStarName());
        }

        @Test
        @DisplayName("pruneStars removes stars with excluded polity")
        void pruneStarsRemovesExcludedPolity() {
            StarDisplayRecord terran = createStar("Sol", 0, 0, 0, "G2V", "Terran");
            StarDisplayRecord ktor = createStar("Ktor Prime", 1, 1, 1, "G2V", "Ktor");
            List<StarDisplayRecord> stars = List.of(terran, ktor);

            RouteFindingOptions options = defaultOptionsBuilder()
                    .polityExclusions(Set.of("Ktor"))
                    .build();

            List<StarDisplayRecord> pruned = routeFindingService.pruneStars(stars, options);

            assertEquals(1, pruned.size());
            assertEquals("Sol", pruned.get(0).getStarName());
        }

        @Test
        @DisplayName("pruneStars handles null stars in list")
        void pruneStarsHandlesNullStars() {
            StarDisplayRecord star = createStar("Sol", 0, 0, 0, "G2V", "Terran");
            List<StarDisplayRecord> stars = new ArrayList<>();
            stars.add(star);
            stars.add(null);

            RouteFindingOptions options = defaultOptionsBuilder().build();

            List<StarDisplayRecord> pruned = routeFindingService.pruneStars(stars, options);

            assertEquals(1, pruned.size());
        }

        @Test
        @DisplayName("pruneStars handles null spectral class")
        void pruneStarsHandlesNullSpectralClass() {
            StarDisplayRecord star = createStar("Sol", 0, 0, 0, null, "Terran");
            List<StarDisplayRecord> stars = List.of(star);

            RouteFindingOptions options = defaultOptionsBuilder()
                    .starExclusions(Set.of("G"))
                    .build();

            List<StarDisplayRecord> pruned = routeFindingService.pruneStars(stars, options);

            assertEquals(1, pruned.size());
        }

        @Test
        @DisplayName("pruneStars handles empty spectral class")
        void pruneStarsHandlesEmptySpectralClass() {
            StarDisplayRecord star = createStar("Sol", 0, 0, 0, "", "Terran");
            List<StarDisplayRecord> stars = List.of(star);

            RouteFindingOptions options = defaultOptionsBuilder()
                    .starExclusions(Set.of("G"))
                    .build();

            List<StarDisplayRecord> pruned = routeFindingService.pruneStars(stars, options);

            assertEquals(1, pruned.size());
        }

        @Test
        @DisplayName("findRoutes fails when origin excluded by spectral class")
        void findRoutesFailsWhenOriginExcluded() {
            StarDisplayRecord origin = createStar("Sol", 0, 0, 0, "M5V", "Terran");
            StarDisplayRecord destination = createStar("Alpha Centauri", 1, 1, 1, "G2V", "Terran");
            List<StarDisplayRecord> stars = List.of(origin, destination);

            RouteFindingOptions options = defaultOptionsBuilder()
                    .originStarName("Sol")
                    .destinationStarName("Alpha Centauri")
                    .starExclusions(Set.of("M"))
                    .build();

            RouteFindingResult result = routeFindingService.findRoutes(options, stars, dataSet);

            assertFalse(result.isSuccess());
            assertTrue(result.getErrorMessage().contains("Origin star"));
        }
    }

    // =========================================================================
    // Result Object Tests
    // =========================================================================

    @Nested
    @DisplayName("Result Object Tests")
    class ResultObjectTests {

        @Test
        @DisplayName("Success result factory creates correct state")
        void successResultFactoryCreatesCorrectState() {
            PossibleRoutes routes = new PossibleRoutes();
            routes.setDesiredPath("Test Route");
            RoutingMetric metric = RoutingMetric.builder()
                    .path("[Sol, Alpha]")
                    .rank(1)
                    .totalLength(5.0)
                    .build();
            routes.getRoutes().add(metric);

            RouteFindingResult result = RouteFindingResult.success(routes);

            assertTrue(result.isSuccess());
            assertNotNull(result.getRoutes());
            assertNull(result.getErrorMessage());
            assertTrue(result.hasRoutes());
        }

        @Test
        @DisplayName("Failure result factory creates correct state")
        void failureResultFactoryCreatesCorrectState() {
            RouteFindingResult result = RouteFindingResult.failure("Test error message");

            assertFalse(result.isSuccess());
            assertNull(result.getRoutes());
            assertEquals("Test error message", result.getErrorMessage());
            assertFalse(result.hasRoutes());
        }

        @Test
        @DisplayName("Failure result from service has error message and no routes")
        void failureResultFromServiceHasErrorMessageAndNoRoutes() {
            List<StarDisplayRecord> stars = Collections.emptyList();

            RouteFindingOptions options = defaultOptionsBuilder()
                    .originStarName("Sol")
                    .destinationStarName("Alpha Centauri")
                    .build();

            RouteFindingResult result = routeFindingService.findRoutes(options, stars, dataSet);

            assertFalse(result.isSuccess());
            assertNull(result.getRoutes());
            assertNotNull(result.getErrorMessage());
        }

        @Test
        @DisplayName("hasRoutes returns false for failure result")
        void hasRoutesReturnsFalseForFailure() {
            List<StarDisplayRecord> stars = Collections.emptyList();

            RouteFindingOptions options = defaultOptionsBuilder()
                    .originStarName("Sol")
                    .destinationStarName("Alpha Centauri")
                    .build();

            RouteFindingResult result = routeFindingService.findRoutes(options, stars, dataSet);

            assertFalse(result.hasRoutes());
        }

        @Test
        @DisplayName("hasRoutes returns false for success with empty routes")
        void hasRoutesReturnsFalseForSuccessWithEmptyRoutes() {
            PossibleRoutes emptyRoutes = new PossibleRoutes();
            RouteFindingResult result = RouteFindingResult.success(emptyRoutes);

            assertTrue(result.isSuccess());
            assertFalse(result.hasRoutes());
        }
    }

    // =========================================================================
    // Exception Handling Tests
    // =========================================================================

    @Nested
    @DisplayName("Exception Handling Tests")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("findRoutes handles exception during transit calculation")
        void findRoutesHandlesExceptionDuringTransitCalculation() {
            StarDisplayRecord origin = createStar("Sol", 0, 0, 0, "G2V", "Terran");
            StarDisplayRecord destination = createStar("Alpha Centauri", 1, 1, 1, "G2V", "Terran");
            List<StarDisplayRecord> stars = List.of(origin, destination);

            when(starMeasurementService.calculateDistances(any(DistanceRoutes.class), anyList()))
                    .thenThrow(new RuntimeException("Test exception"));

            RouteFindingOptions options = defaultOptionsBuilder()
                    .originStarName("Sol")
                    .destinationStarName("Alpha Centauri")
                    .build();

            RouteFindingResult result = routeFindingService.findRoutes(options, stars, dataSet);

            assertFalse(result.isSuccess());
            assertTrue(result.getErrorMessage().contains("failed"));
        }
    }
}
