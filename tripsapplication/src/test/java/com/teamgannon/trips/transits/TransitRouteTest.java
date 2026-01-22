package com.teamgannon.trips.transits;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import javafx.geometry.Point3D;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TransitRoute model class.
 * Tests cover builder pattern, property access, and derived values.
 */
class TransitRouteTest {

    private StarDisplayRecord sourceRecord;
    private StarDisplayRecord targetRecord;

    @BeforeEach
    void setUp() {
        sourceRecord = createStarRecord("Sol", 0, 0, 0);
        targetRecord = createStarRecord("Alpha Centauri", 4.37, 0, 0);
    }

    private StarDisplayRecord createStarRecord(String name, double x, double y, double z) {
        StarDisplayRecord record = new StarDisplayRecord();
        record.setStarName(name);
        record.setActualCoordinates(new double[]{x, y, z});
        record.setCoordinates(new Point3D(x, y, z));
        record.setRecordId("id-" + name);
        return record;
    }

    // =========================================================================
    // Builder Tests
    // =========================================================================

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Builder creates TransitRoute with all properties")
        void builderCreatesFullRoute() {
            TransitRoute route = TransitRoute.builder()
                    .good(true)
                    .source(sourceRecord)
                    .target(targetRecord)
                    .distance(4.37)
                    .lineWeight(1.5)
                    .color(Color.CYAN)
                    .build();

            assertTrue(route.isGood());
            assertEquals(sourceRecord, route.getSource());
            assertEquals(targetRecord, route.getTarget());
            assertEquals(4.37, route.getDistance(), 0.001);
            assertEquals(1.5, route.getLineWeight(), 0.001);
            assertEquals(Color.CYAN, route.getColor());
        }

        @Test
        @DisplayName("Builder with good=false creates invalid route")
        void builderCreatesInvalidRoute() {
            TransitRoute route = TransitRoute.builder()
                    .good(false)
                    .build();

            assertFalse(route.isGood());
            assertNull(route.getSource());
            assertNull(route.getTarget());
        }

        @Test
        @DisplayName("Builder defaults are null/zero/false")
        void builderDefaults() {
            TransitRoute route = TransitRoute.builder().build();

            assertFalse(route.isGood());
            assertNull(route.getSource());
            assertNull(route.getTarget());
            assertEquals(0.0, route.getDistance());
            assertEquals(0.0, route.getLineWeight());
            assertNull(route.getColor());
        }
    }

    // =========================================================================
    // Property Access Tests
    // =========================================================================

    @Nested
    @DisplayName("Property Access Tests")
    class PropertyAccessTests {

        @Test
        @DisplayName("isGood returns correct value")
        void isGoodReturnsCorrectValue() {
            TransitRoute goodRoute = TransitRoute.builder().good(true).build();
            TransitRoute badRoute = TransitRoute.builder().good(false).build();

            assertTrue(goodRoute.isGood());
            assertFalse(badRoute.isGood());
        }

        @Test
        @DisplayName("source and target are accessible")
        void sourceAndTargetAccessible() {
            TransitRoute route = TransitRoute.builder()
                    .source(sourceRecord)
                    .target(targetRecord)
                    .build();

            assertSame(sourceRecord, route.getSource());
            assertSame(targetRecord, route.getTarget());
        }

        @Test
        @DisplayName("distance is accessible")
        void distanceAccessible() {
            TransitRoute route = TransitRoute.builder()
                    .distance(8.5)
                    .build();

            assertEquals(8.5, route.getDistance(), 0.001);
        }

        @Test
        @DisplayName("lineWeight is accessible")
        void lineWeightAccessible() {
            TransitRoute route = TransitRoute.builder()
                    .lineWeight(2.5)
                    .build();

            assertEquals(2.5, route.getLineWeight(), 0.001);
        }

        @Test
        @DisplayName("color is accessible")
        void colorAccessible() {
            TransitRoute route = TransitRoute.builder()
                    .color(Color.MAGENTA)
                    .build();

            assertEquals(Color.MAGENTA, route.getColor());
        }
    }

    // =========================================================================
    // Endpoint Tests
    // =========================================================================

    @Nested
    @DisplayName("Endpoint Tests")
    class EndpointTests {

        @Test
        @DisplayName("getSourceEndpoint returns source coordinates")
        void sourceEndpointReturnsCoordinates() {
            TransitRoute route = TransitRoute.builder()
                    .source(sourceRecord)
                    .target(targetRecord)
                    .build();

            Point3D endpoint = route.getSourceEndpoint();

            assertNotNull(endpoint);
            assertEquals(0.0, endpoint.getX(), 0.001);
            assertEquals(0.0, endpoint.getY(), 0.001);
            assertEquals(0.0, endpoint.getZ(), 0.001);
        }

        @Test
        @DisplayName("getTargetEndpoint returns target coordinates")
        void targetEndpointReturnsCoordinates() {
            TransitRoute route = TransitRoute.builder()
                    .source(sourceRecord)
                    .target(targetRecord)
                    .build();

            Point3D endpoint = route.getTargetEndpoint();

            assertNotNull(endpoint);
            assertEquals(4.37, endpoint.getX(), 0.001);
            assertEquals(0.0, endpoint.getY(), 0.001);
            assertEquals(0.0, endpoint.getZ(), 0.001);
        }

        @Test
        @DisplayName("Endpoints with 3D coordinates")
        void endpointsWithThreeDCoordinates() {
            StarDisplayRecord source = createStarRecord("Star1", 1, 2, 3);
            StarDisplayRecord target = createStarRecord("Star2", 4, 5, 6);

            TransitRoute route = TransitRoute.builder()
                    .source(source)
                    .target(target)
                    .build();

            assertEquals(new Point3D(1, 2, 3), route.getSourceEndpoint());
            assertEquals(new Point3D(4, 5, 6), route.getTargetEndpoint());
        }
    }

    // =========================================================================
    // Name Tests
    // =========================================================================

    @Nested
    @DisplayName("Name Tests")
    class NameTests {

        @Test
        @DisplayName("getName returns source,target format")
        void getNameReturnsFormattedString() {
            TransitRoute route = TransitRoute.builder()
                    .source(sourceRecord)
                    .target(targetRecord)
                    .build();

            String name = route.getName();

            assertEquals("Sol,Alpha Centauri", name);
        }

        @Test
        @DisplayName("getName is not null when stars are set")
        void getNameNotNull() {
            TransitRoute route = TransitRoute.builder()
                    .source(sourceRecord)
                    .target(targetRecord)
                    .build();

            assertNotNull(route.getName());
        }

        @Test
        @DisplayName("getName reflects star names")
        void getNameReflectsStarNames() {
            StarDisplayRecord star1 = createStarRecord("Proxima Centauri", 0, 0, 0);
            StarDisplayRecord star2 = createStarRecord("Barnard's Star", 0, 0, 0);

            TransitRoute route = TransitRoute.builder()
                    .source(star1)
                    .target(star2)
                    .build();

            assertTrue(route.getName().contains("Proxima Centauri"));
            assertTrue(route.getName().contains("Barnard's Star"));
        }
    }

    // =========================================================================
    // Typical Usage Tests
    // =========================================================================

    @Nested
    @DisplayName("Typical Usage Tests")
    class TypicalUsageTests {

        @Test
        @DisplayName("Create valid transit route with all properties")
        void createValidTransitRoute() {
            TransitRoute route = TransitRoute.builder()
                    .good(true)
                    .source(sourceRecord)
                    .target(targetRecord)
                    .distance(4.37)
                    .lineWeight(TransitConstants.DEFAULT_LINE_WIDTH)
                    .color(Color.GREEN)
                    .build();

            assertTrue(route.isGood());
            assertEquals(4.37, route.getDistance(), 0.01);
            assertEquals("Sol,Alpha Centauri", route.getName());
            assertNotNull(route.getSourceEndpoint());
            assertNotNull(route.getTargetEndpoint());
        }

        @Test
        @DisplayName("Create invalid route marker")
        void createInvalidRouteMarker() {
            TransitRoute invalidRoute = TransitRoute.builder()
                    .good(false)
                    .build();

            assertFalse(invalidRoute.isGood());
        }

        @Test
        @DisplayName("Route with custom color")
        void routeWithCustomColor() {
            Color customColor = Color.rgb(128, 64, 192);

            TransitRoute route = TransitRoute.builder()
                    .source(sourceRecord)
                    .target(targetRecord)
                    .color(customColor)
                    .build();

            assertEquals(customColor, route.getColor());
        }
    }

    // =========================================================================
    // Edge Case Tests
    // =========================================================================

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Zero distance route")
        void zeroDistanceRoute() {
            StarDisplayRecord sameStar = createStarRecord("Sol", 0, 0, 0);

            TransitRoute route = TransitRoute.builder()
                    .source(sameStar)
                    .target(sameStar)
                    .distance(0.0)
                    .build();

            assertEquals(0.0, route.getDistance());
        }

        @Test
        @DisplayName("Very long distance route")
        void veryLongDistanceRoute() {
            StarDisplayRecord farStar = createStarRecord("Distant", 1000, 2000, 3000);

            TransitRoute route = TransitRoute.builder()
                    .source(sourceRecord)
                    .target(farStar)
                    .distance(3741.657)
                    .build();

            assertEquals(3741.657, route.getDistance(), 0.001);
        }

        @Test
        @DisplayName("Negative coordinates handled")
        void negativeCoordinatesHandled() {
            StarDisplayRecord negStar = createStarRecord("NegativeStar", -5, -10, -15);

            TransitRoute route = TransitRoute.builder()
                    .source(sourceRecord)
                    .target(negStar)
                    .build();

            Point3D endpoint = route.getTargetEndpoint();
            assertEquals(-5, endpoint.getX(), 0.001);
            assertEquals(-10, endpoint.getY(), 0.001);
            assertEquals(-15, endpoint.getZ(), 0.001);
        }

        @Test
        @DisplayName("Zero line weight")
        void zeroLineWeight() {
            TransitRoute route = TransitRoute.builder()
                    .lineWeight(0.0)
                    .build();

            assertEquals(0.0, route.getLineWeight());
        }

        @Test
        @DisplayName("Transparent color")
        void transparentColor() {
            Color transparent = Color.rgb(255, 0, 0, 0.0);

            TransitRoute route = TransitRoute.builder()
                    .color(transparent)
                    .build();

            assertEquals(0.0, route.getColor().getOpacity(), 0.001);
        }
    }

    // =========================================================================
    // Lombok Generated Tests
    // =========================================================================

    @Nested
    @DisplayName("Lombok Generated Methods")
    class LombokGeneratedTests {

        @Test
        @DisplayName("setters work correctly")
        void settersWork() {
            TransitRoute route = TransitRoute.builder().build();

            route.setGood(true);
            route.setSource(sourceRecord);
            route.setTarget(targetRecord);
            route.setDistance(5.0);
            route.setLineWeight(2.0);
            route.setColor(Color.BLUE);

            assertTrue(route.isGood());
            assertEquals(sourceRecord, route.getSource());
            assertEquals(targetRecord, route.getTarget());
            assertEquals(5.0, route.getDistance());
            assertEquals(2.0, route.getLineWeight());
            assertEquals(Color.BLUE, route.getColor());
        }

        @Test
        @DisplayName("toString returns non-null string")
        void toStringReturnsNonNull() {
            TransitRoute route = TransitRoute.builder()
                    .source(sourceRecord)
                    .target(targetRecord)
                    .distance(4.37)
                    .build();

            String result = route.toString();

            assertNotNull(result);
        }

        @Test
        @DisplayName("equals works for identical routes")
        void equalsForIdenticalRoutes() {
            TransitRoute route1 = TransitRoute.builder()
                    .good(true)
                    .source(sourceRecord)
                    .target(targetRecord)
                    .distance(4.37)
                    .lineWeight(1.0)
                    .color(Color.RED)
                    .build();

            TransitRoute route2 = TransitRoute.builder()
                    .good(true)
                    .source(sourceRecord)
                    .target(targetRecord)
                    .distance(4.37)
                    .lineWeight(1.0)
                    .color(Color.RED)
                    .build();

            assertEquals(route1, route2);
        }

        @Test
        @DisplayName("hashCode is consistent with equals")
        void hashCodeConsistentWithEquals() {
            TransitRoute route1 = TransitRoute.builder()
                    .good(true)
                    .source(sourceRecord)
                    .target(targetRecord)
                    .distance(4.37)
                    .build();

            TransitRoute route2 = TransitRoute.builder()
                    .good(true)
                    .source(sourceRecord)
                    .target(targetRecord)
                    .distance(4.37)
                    .build();

            if (route1.equals(route2)) {
                assertEquals(route1.hashCode(), route2.hashCode());
            }
        }
    }
}
