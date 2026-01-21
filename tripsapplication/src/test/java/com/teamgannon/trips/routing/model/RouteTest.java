package com.teamgannon.trips.routing.model;

import org.junit.jupiter.api.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Route model class.
 * <p>
 * Tests cover:
 * <ul>
 *   <li>Default initialization</li>
 *   <li>Property setters and getters</li>
 *   <li>JSON serialization/deserialization</li>
 *   <li>List operations</li>
 * </ul>
 */
class RouteTest {

    private Route route;

    @BeforeEach
    void setUp() {
        route = new Route();
    }

    // =========================================================================
    // Initialization Tests
    // =========================================================================

    @Nested
    @DisplayName("Initialization Tests")
    class InitializationTests {

        @Test
        @DisplayName("New Route has empty routeStars list")
        void newRouteHasEmptyRouteStarsList() {
            assertNotNull(route.getRouteStars());
            assertTrue(route.getRouteStars().isEmpty());
        }

        @Test
        @DisplayName("New Route has empty routeStarNames list")
        void newRouteHasEmptyRouteStarNamesList() {
            assertNotNull(route.getRouteStarNames());
            assertTrue(route.getRouteStarNames().isEmpty());
        }

        @Test
        @DisplayName("New Route has empty routeLengths list")
        void newRouteHasEmptyRouteLengthsList() {
            assertNotNull(route.getRouteLengths());
            assertTrue(route.getRouteLengths().isEmpty());
        }

        @Test
        @DisplayName("New Route has null UUID initially")
        void newRouteHasNullUUID() {
            assertNull(route.getUuid());
        }

        @Test
        @DisplayName("New Route has zero lineWidth")
        void newRouteHasZeroLineWidth() {
            assertEquals(0.0, route.getLineWidth(), 0.001);
        }
    }

    // =========================================================================
    // Property Tests
    // =========================================================================

    @Nested
    @DisplayName("Property Tests")
    class PropertyTests {

        @Test
        @DisplayName("setRouteName and getRouteName work correctly")
        void routeNameProperty() {
            route.setRouteName("Test Route");
            assertEquals("Test Route", route.getRouteName());
        }

        @Test
        @DisplayName("setUuid and getUuid work correctly")
        void uuidProperty() {
            UUID testUuid = UUID.randomUUID();
            route.setUuid(testUuid);
            assertEquals(testUuid, route.getUuid());
        }

        @Test
        @DisplayName("setLineWidth and getLineWidth work correctly")
        void lineWidthProperty() {
            route.setLineWidth(2.5);
            assertEquals(2.5, route.getLineWidth(), 0.001);
        }

        @Test
        @DisplayName("setRouteColor and getRouteColor work correctly")
        void routeColorProperty() {
            route.setRouteColor("0xff0000ff");
            assertEquals("0xff0000ff", route.getRouteColor());
        }

        @Test
        @DisplayName("setRouteNotes and getRouteNotes work correctly")
        void routeNotesProperty() {
            route.setRouteNotes("These are test notes");
            assertEquals("These are test notes", route.getRouteNotes());
        }

        @Test
        @DisplayName("setStartingStar and getStartingStar work correctly")
        void startingStarProperty() {
            route.setStartingStar("Sol");
            assertEquals("Sol", route.getStartingStar());
        }

        @Test
        @DisplayName("setTotalLength and getTotalLength work correctly")
        void totalLengthProperty() {
            route.setTotalLength(42.5);
            assertEquals(42.5, route.getTotalLength(), 0.001);
        }
    }

    // =========================================================================
    // List Operations Tests
    // =========================================================================

    @Nested
    @DisplayName("List Operations Tests")
    class ListOperationsTests {

        @Test
        @DisplayName("Can add stars to routeStars list")
        void canAddStarsToRouteStarsList() {
            route.getRouteStars().add("star-001");
            route.getRouteStars().add("star-002");

            assertEquals(2, route.getRouteStars().size());
            assertEquals("star-001", route.getRouteStars().get(0));
            assertEquals("star-002", route.getRouteStars().get(1));
        }

        @Test
        @DisplayName("Can add star names to routeStarNames list")
        void canAddStarNamesToRouteStarNamesList() {
            route.getRouteStarNames().add("Sol");
            route.getRouteStarNames().add("Alpha Centauri");

            assertEquals(2, route.getRouteStarNames().size());
            assertTrue(route.getRouteStarNames().contains("Sol"));
            assertTrue(route.getRouteStarNames().contains("Alpha Centauri"));
        }

        @Test
        @DisplayName("Can add lengths to routeLengths list")
        void canAddLengthsToRouteLengthsList() {
            route.getRouteLengths().add(4.37);
            route.getRouteLengths().add(3.8);

            assertEquals(2, route.getRouteLengths().size());
            assertEquals(4.37, route.getRouteLengths().get(0), 0.01);
            assertEquals(3.8, route.getRouteLengths().get(1), 0.01);
        }

        @Test
        @DisplayName("Can replace entire routeStars list")
        void canReplaceRouteStarsList() {
            route.setRouteStars(Arrays.asList("star-001", "star-002", "star-003"));

            assertEquals(3, route.getRouteStars().size());
        }
    }

    // =========================================================================
    // JSON Serialization Tests
    // =========================================================================

    @Nested
    @DisplayName("JSON Serialization Tests")
    class JsonSerializationTests {

        @Test
        @DisplayName("convertToJson returns non-empty string for valid route")
        void convertToJsonReturnsNonEmptyString() {
            route.setUuid(UUID.randomUUID());
            route.setRouteName("Test Route");
            route.setStartingStar("Sol");
            route.getRouteStars().add("star-001");
            route.getRouteStars().add("star-002");

            String json = route.convertToJson();

            assertNotNull(json);
            assertFalse(json.isEmpty());
            assertTrue(json.contains("Test Route"));
            assertTrue(json.contains("Sol"));
        }

        @Test
        @DisplayName("convertToJson with parameter returns same result as instance method")
        void convertToJsonWithParameterReturnsSameResult() {
            route.setRouteName("Test Route");
            route.setUuid(UUID.randomUUID());

            String json1 = route.convertToJson();
            String json2 = route.convertToJson(route);

            assertEquals(json1, json2);
        }

        @Test
        @DisplayName("convertToJson for empty route returns valid JSON")
        void convertToJsonForEmptyRouteReturnsValidJson() {
            String json = route.convertToJson();

            assertNotNull(json);
            assertTrue(json.startsWith("{"));
            assertTrue(json.endsWith("}"));
        }

        @Test
        @DisplayName("convertToJson for list returns JSON array")
        void convertToJsonForListReturnsJsonArray() {
            Route route1 = new Route();
            route1.setRouteName("Route 1");
            Route route2 = new Route();
            route2.setRouteName("Route 2");

            String json = route.convertToJson(List.of(route1, route2));

            assertNotNull(json);
            assertTrue(json.startsWith("["));
            assertTrue(json.endsWith("]"));
            assertTrue(json.contains("Route 1"));
            assertTrue(json.contains("Route 2"));
        }
    }

    // =========================================================================
    // JSON Deserialization Tests
    // =========================================================================

    @Nested
    @DisplayName("JSON Deserialization Tests")
    class JsonDeserializationTests {

        @Test
        @DisplayName("toRoute deserializes JSON list correctly")
        void toRouteDeserializesJsonListCorrectly() {
            Route route1 = new Route();
            route1.setUuid(UUID.randomUUID());
            route1.setRouteName("Route 1");
            route1.setStartingStar("Sol");
            route1.setLineWidth(0.5);

            Route route2 = new Route();
            route2.setUuid(UUID.randomUUID());
            route2.setRouteName("Route 2");
            route2.setStartingStar("Alpha Centauri");
            route2.setLineWidth(1.0);

            String json = route.convertToJson(List.of(route1, route2));
            List<Route> deserializedRoutes = route.toRoute(json);

            assertNotNull(deserializedRoutes);
            assertEquals(2, deserializedRoutes.size());
            assertEquals("Route 1", deserializedRoutes.get(0).getRouteName());
            assertEquals("Route 2", deserializedRoutes.get(1).getRouteName());
        }

        @Test
        @DisplayName("toRoute returns null for invalid JSON")
        void toRouteReturnsNullForInvalidJson() {
            List<Route> result = route.toRoute("not valid json");

            assertNull(result);
        }

        @Test
        @DisplayName("Round-trip serialization preserves all fields")
        void roundTripSerializationPreservesAllFields() {
            Route original = new Route();
            original.setUuid(UUID.randomUUID());
            original.setRouteName("Test Route");
            original.setRouteColor("0xff0000ff");
            original.setRouteNotes("Test notes");
            original.setStartingStar("Sol");
            original.setLineWidth(2.0);
            original.setTotalLength(12.5);
            original.getRouteStars().addAll(Arrays.asList("star-001", "star-002"));
            original.getRouteStarNames().addAll(Arrays.asList("Sol", "Alpha Centauri"));
            original.getRouteLengths().add(4.37);

            String json = original.convertToJson(List.of(original));
            List<Route> deserialized = route.toRoute(json);

            assertNotNull(deserialized);
            assertEquals(1, deserialized.size());
            Route restored = deserialized.get(0);

            assertEquals(original.getUuid(), restored.getUuid());
            assertEquals(original.getRouteName(), restored.getRouteName());
            assertEquals(original.getRouteColor(), restored.getRouteColor());
            assertEquals(original.getRouteNotes(), restored.getRouteNotes());
            assertEquals(original.getStartingStar(), restored.getStartingStar());
            assertEquals(original.getLineWidth(), restored.getLineWidth(), 0.001);
            assertEquals(original.getTotalLength(), restored.getTotalLength(), 0.001);
            assertEquals(original.getRouteStars(), restored.getRouteStars());
            assertEquals(original.getRouteStarNames(), restored.getRouteStarNames());
            assertEquals(original.getRouteLengths(), restored.getRouteLengths());
        }
    }

    // =========================================================================
    // Edge Cases Tests
    // =========================================================================

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Route with null route name serializes correctly")
        void routeWithNullRouteNameSerializesCorrectly() {
            route.setRouteName(null);

            String json = route.convertToJson();

            assertNotNull(json);
            assertTrue(json.contains("\"routeName\":null"));
        }

        @Test
        @DisplayName("Empty list converts to empty JSON array")
        void emptyListConvertsToEmptyJsonArray() {
            String json = route.convertToJson(List.of());

            assertEquals("[]", json);
        }

        @Test
        @DisplayName("toRoute with empty JSON array returns empty list")
        void toRouteWithEmptyJsonArrayReturnsEmptyList() {
            List<Route> result = route.toRoute("[]");

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }
}
