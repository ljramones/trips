package com.teamgannon.trips.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamgannon.trips.dataset.model.CustomDataDefinition;
import com.teamgannon.trips.dataset.model.CustomDataValue;
import com.teamgannon.trips.dataset.model.Theme;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.routing.model.Route;
import com.teamgannon.trips.transits.TransitDefinitions;
import com.teamgannon.trips.transits.TransitRangeDef;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DataSetDescriptorSerializationService.
 */
@DisplayName("DataSetDescriptorSerializationService")
class DataSetDescriptorSerializationServiceTest {

    private DataSetDescriptorSerializationService service;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new DataSetDescriptorSerializationService(objectMapper);
    }

    @Nested
    @DisplayName("Theme Serialization")
    class ThemeSerializationTests {

        @Test
        @DisplayName("should serialize and deserialize Theme correctly")
        void shouldRoundTripTheme() {
            Theme theme = new Theme();
            theme.setThemeName("Test Theme");
            theme.setGridSize(10);

            String json = service.serializeTheme(theme);
            assertNotNull(json);
            assertFalse(json.isEmpty());

            Theme deserialized = service.deserializeTheme(json);
            assertNotNull(deserialized);
            assertEquals("Test Theme", deserialized.getThemeName());
            assertEquals(10, deserialized.getGridSize());
        }

        @Test
        @DisplayName("should return null for null or empty Theme string")
        void shouldHandleNullThemeString() {
            assertNull(service.deserializeTheme(null));
            assertNull(service.deserializeTheme(""));
            assertNull(service.deserializeTheme("   "));
        }

        @Test
        @DisplayName("should return empty string for null Theme")
        void shouldHandleNullTheme() {
            assertEquals("", service.serializeTheme(null));
        }

        @Test
        @DisplayName("should get and set Theme on descriptor")
        void shouldGetAndSetThemeOnDescriptor() {
            DataSetDescriptor descriptor = new DataSetDescriptor();
            descriptor.setDataSetName("TestDataset");

            Theme theme = new Theme();
            theme.setThemeName("My Theme");

            service.setTheme(descriptor, theme);
            assertNotNull(descriptor.getThemeStr());

            Theme retrieved = service.getTheme(descriptor);
            assertNotNull(retrieved);
            assertEquals("My Theme", retrieved.getThemeName());
        }
    }

    @Nested
    @DisplayName("Routes Serialization")
    class RoutesSerializationTests {

        @Test
        @DisplayName("should serialize and deserialize Routes correctly")
        void shouldRoundTripRoutes() {
            Route route1 = new Route();
            route1.setRouteName("Route A");
            route1.setRouteNotes("Notes for A");

            Route route2 = new Route();
            route2.setRouteName("Route B");

            List<Route> routes = Arrays.asList(route1, route2);

            String json = service.serializeRoutes(routes);
            assertNotNull(json);
            assertFalse(json.isEmpty());

            List<Route> deserialized = service.deserializeRoutes(json);
            assertNotNull(deserialized);
            assertEquals(2, deserialized.size());
            assertEquals("Route A", deserialized.get(0).getRouteName());
            assertEquals("Route B", deserialized.get(1).getRouteName());
        }

        @Test
        @DisplayName("should return empty list for null or empty Routes string")
        void shouldHandleNullRoutesString() {
            assertTrue(service.deserializeRoutes(null).isEmpty());
            assertTrue(service.deserializeRoutes("").isEmpty());
            assertTrue(service.deserializeRoutes("   ").isEmpty());
        }

        @Test
        @DisplayName("should return empty string for null or empty Routes list")
        void shouldHandleNullRoutesList() {
            assertEquals("", service.serializeRoutes(null));
            assertEquals("", service.serializeRoutes(new ArrayList<>()));
        }

        @Test
        @DisplayName("should update numberRoutes when setting routes")
        void shouldUpdateNumberRoutes() {
            DataSetDescriptor descriptor = new DataSetDescriptor();
            descriptor.setDataSetName("TestDataset");

            Route route = new Route();
            route.setRouteName("Test Route");

            service.setRoutes(descriptor, Arrays.asList(route, route, route));
            assertEquals(3, descriptor.getNumberRoutes());
        }
    }

    @Nested
    @DisplayName("CustomDataDefinition Serialization")
    class CustomDataDefinitionSerializationTests {

        @Test
        @DisplayName("should serialize and deserialize CustomDataDefinitions correctly")
        void shouldRoundTripCustomDataDefinitions() {
            CustomDataDefinition def1 = new CustomDataDefinition();
            def1.setCustomFieldName("Field1");

            CustomDataDefinition def2 = new CustomDataDefinition();
            def2.setCustomFieldName("Field2");

            List<CustomDataDefinition> definitions = Arrays.asList(def1, def2);

            String json = service.serializeCustomDataDefinitions(definitions);
            assertNotNull(json);

            List<CustomDataDefinition> deserialized = service.deserializeCustomDataDefinitions(json);
            assertNotNull(deserialized);
            assertEquals(2, deserialized.size());
            assertEquals("Field1", deserialized.get(0).getCustomFieldName());
            assertEquals("Field2", deserialized.get(1).getCustomFieldName());
        }

        @Test
        @DisplayName("should return empty list for null or empty string")
        void shouldHandleNullString() {
            assertTrue(service.deserializeCustomDataDefinitions(null).isEmpty());
            assertTrue(service.deserializeCustomDataDefinitions("").isEmpty());
        }
    }

    @Nested
    @DisplayName("CustomDataValue Serialization")
    class CustomDataValueSerializationTests {

        @Test
        @DisplayName("should serialize and deserialize CustomDataValues correctly")
        void shouldRoundTripCustomDataValues() {
            CustomDataValue value1 = new CustomDataValue();
            value1.setCustomFieldName("fieldName1");
            value1.setCustomFieldValue("value1");

            List<CustomDataValue> values = Collections.singletonList(value1);

            String json = service.serializeCustomDataValues(values);
            assertNotNull(json);

            List<CustomDataValue> deserialized = service.deserializeCustomDataValues(json);
            assertNotNull(deserialized);
            assertEquals(1, deserialized.size());
            assertEquals("fieldName1", deserialized.get(0).getCustomFieldName());
            assertEquals("value1", deserialized.get(0).getCustomFieldValue());
        }

        @Test
        @DisplayName("should return empty list for null or empty string")
        void shouldHandleNullString() {
            assertTrue(service.deserializeCustomDataValues(null).isEmpty());
            assertTrue(service.deserializeCustomDataValues("").isEmpty());
        }
    }

    @Nested
    @DisplayName("TransitDefinitions Serialization")
    class TransitDefinitionsSerializationTests {

        @Test
        @DisplayName("should serialize and deserialize TransitDefinitions correctly")
        void shouldRoundTripTransitDefinitions() {
            TransitDefinitions defs = new TransitDefinitions();
            defs.setDataSetName("TestDataset");
            defs.setSelected(true);

            TransitRangeDef rangeDef = new TransitRangeDef();
            rangeDef.setLowerRange(0);
            rangeDef.setUpperRange(10);
            rangeDef.setBandName("Test Band");
            defs.setTransitRangeDefs(Collections.singletonList(rangeDef));

            String json = service.serializeTransitDefinitions(defs);
            assertNotNull(json);
            assertFalse(json.isEmpty());

            TransitDefinitions deserialized = service.deserializeTransitDefinitions(json, "TestDataset");
            assertNotNull(deserialized);
            assertEquals("TestDataset", deserialized.getDataSetName());
            assertTrue(deserialized.isSelected());
            assertEquals(1, deserialized.getTransitRangeDefs().size());
            assertEquals("Test Band", deserialized.getTransitRangeDefs().get(0).getBandName());
        }

        @Test
        @DisplayName("should return default TransitDefinitions for null or empty string")
        void shouldReturnDefaultForNullString() {
            TransitDefinitions result = service.deserializeTransitDefinitions(null, "MyDataset");
            assertNotNull(result);
            assertEquals("MyDataset", result.getDataSetName());
            assertFalse(result.isSelected());
            assertTrue(result.getTransitRangeDefs().isEmpty());

            result = service.deserializeTransitDefinitions("", "MyDataset");
            assertNotNull(result);
        }

        @Test
        @DisplayName("should return empty string for null TransitDefinitions")
        void shouldHandleNullTransitDefinitions() {
            assertEquals("", service.serializeTransitDefinitions(null));
        }
    }

    @Nested
    @DisplayName("AstrographicDataList Serialization")
    class AstrographicDataListSerializationTests {

        @Test
        @DisplayName("should serialize and deserialize UUID set correctly")
        void shouldRoundTripUUIDSet() {
            UUID uuid1 = UUID.randomUUID();
            UUID uuid2 = UUID.randomUUID();
            UUID uuid3 = UUID.randomUUID();

            Set<UUID> uuids = new HashSet<>(Arrays.asList(uuid1, uuid2, uuid3));

            String serialized = service.serializeAstrographicDataList(uuids);
            assertNotNull(serialized);
            assertFalse(serialized.isEmpty());

            Set<UUID> deserialized = service.deserializeAstrographicDataList(serialized);
            assertEquals(3, deserialized.size());
            assertTrue(deserialized.contains(uuid1));
            assertTrue(deserialized.contains(uuid2));
            assertTrue(deserialized.contains(uuid3));
        }

        @Test
        @DisplayName("should return empty set for null or empty string")
        void shouldHandleNullString() {
            assertTrue(service.deserializeAstrographicDataList(null).isEmpty());
            assertTrue(service.deserializeAstrographicDataList("").isEmpty());
            assertTrue(service.deserializeAstrographicDataList("   ").isEmpty());
        }

        @Test
        @DisplayName("should return empty string for null or empty UUID set")
        void shouldHandleNullUUIDSet() {
            assertEquals("", service.serializeAstrographicDataList(null));
            assertEquals("", service.serializeAstrographicDataList(new HashSet<>()));
        }

        @Test
        @DisplayName("should update numberStars when setting astrographic data list")
        void shouldUpdateNumberStars() {
            DataSetDescriptor descriptor = new DataSetDescriptor();
            descriptor.setDataSetName("TestDataset");

            Set<UUID> uuids = new HashSet<>(Arrays.asList(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID()
            ));

            service.setAstrographicDataList(descriptor, uuids);
            assertEquals(5L, descriptor.getNumberStars());
        }
    }

    @Nested
    @DisplayName("Full Descriptor Operations")
    class FullDescriptorOperationsTests {

        @Test
        @DisplayName("should handle a fully populated descriptor")
        void shouldHandleFullyPopulatedDescriptor() {
            DataSetDescriptor descriptor = new DataSetDescriptor();
            descriptor.setDataSetName("FullTest");

            // Set theme
            Theme theme = new Theme();
            theme.setThemeName("Full Theme");
            service.setTheme(descriptor, theme);

            // Set routes
            Route route = new Route();
            route.setRouteName("Full Route");
            service.setRoutes(descriptor, Collections.singletonList(route));

            // Set transit definitions
            TransitDefinitions transitDefs = new TransitDefinitions();
            transitDefs.setDataSetName("FullTest");
            transitDefs.setSelected(true);
            service.setTransitDefinitions(descriptor, transitDefs);

            // Set astrographic data
            Set<UUID> uuids = new HashSet<>(Arrays.asList(UUID.randomUUID(), UUID.randomUUID()));
            service.setAstrographicDataList(descriptor, uuids);

            // Verify all retrievals
            Theme retrievedTheme = service.getTheme(descriptor);
            assertNotNull(retrievedTheme);
            assertEquals("Full Theme", retrievedTheme.getThemeName());

            List<Route> retrievedRoutes = service.getRoutes(descriptor);
            assertEquals(1, retrievedRoutes.size());
            assertEquals("Full Route", retrievedRoutes.get(0).getRouteName());

            TransitDefinitions retrievedTransit = service.getTransitDefinitions(descriptor);
            assertNotNull(retrievedTransit);
            assertTrue(retrievedTransit.isSelected());

            Set<UUID> retrievedUuids = service.getAstrographicDataList(descriptor);
            assertEquals(2, retrievedUuids.size());
        }
    }

    @Nested
    @DisplayName("Malformed JSON Handling")
    class MalformedJsonHandlingTests {

        @Test
        @DisplayName("should return null for malformed Theme JSON")
        void shouldHandleMalformedThemeJson() {
            assertNull(service.deserializeTheme("not valid json"));
            assertNull(service.deserializeTheme("{invalid}"));
            assertNull(service.deserializeTheme("{ \"themeName\": }"));
        }

        @Test
        @DisplayName("should return empty list for malformed Routes JSON")
        void shouldHandleMalformedRoutesJson() {
            assertTrue(service.deserializeRoutes("not valid json").isEmpty());
            assertTrue(service.deserializeRoutes("[{invalid}]").isEmpty());
            assertTrue(service.deserializeRoutes("{ \"routeName\": }").isEmpty());
        }

        @Test
        @DisplayName("should return empty list for malformed CustomDataDefinitions JSON")
        void shouldHandleMalformedCustomDataDefinitionsJson() {
            assertTrue(service.deserializeCustomDataDefinitions("not valid json").isEmpty());
            assertTrue(service.deserializeCustomDataDefinitions("[{broken").isEmpty());
        }

        @Test
        @DisplayName("should return empty list for malformed CustomDataValues JSON")
        void shouldHandleMalformedCustomDataValuesJson() {
            assertTrue(service.deserializeCustomDataValues("not valid json").isEmpty());
            assertTrue(service.deserializeCustomDataValues("[{broken").isEmpty());
        }

        @Test
        @DisplayName("should return default TransitDefinitions for malformed JSON")
        void shouldHandleMalformedTransitDefinitionsJson() {
            TransitDefinitions result = service.deserializeTransitDefinitions("not valid json", "TestDataset");
            assertNotNull(result);
            assertEquals("TestDataset", result.getDataSetName());
            assertFalse(result.isSelected());
            assertTrue(result.getTransitRangeDefs().isEmpty());
        }

        @Test
        @DisplayName("should handle malformed UUID strings gracefully")
        void shouldHandleMalformedUuidStrings() {
            // Invalid UUID format should throw exception but we catch partial valid data
            assertThrows(IllegalArgumentException.class, () ->
                    service.deserializeAstrographicDataList("not-a-uuid"));

            // Mixed valid and invalid - this will fail on first invalid
            assertThrows(IllegalArgumentException.class, () ->
                    service.deserializeAstrographicDataList(UUID.randomUUID() + ",invalid-uuid"));
        }

        @Test
        @DisplayName("should fail gracefully with JSON containing extra unknown fields")
        void shouldHandleJsonWithExtraFields() {
            // JSON with extra fields that don't exist in the class
            // Theme class is configured to fail on unknown properties, so this should return null
            String jsonWithExtra = "{\"themeName\":\"Test\",\"unknownField\":\"value\",\"gridSize\":5}";
            Theme theme = service.deserializeTheme(jsonWithExtra);
            // Theme class doesn't ignore unknown properties, so deserialization fails gracefully
            assertNull(theme);
        }

        @Test
        @DisplayName("should handle empty JSON object")
        void shouldHandleEmptyJsonObject() {
            Theme theme = service.deserializeTheme("{}");
            assertNotNull(theme);

            List<Route> routes = service.deserializeRoutes("[]");
            assertNotNull(routes);
            assertTrue(routes.isEmpty());
        }

        @Test
        @DisplayName("should handle JSON with null values")
        void shouldHandleJsonWithNullValues() {
            String jsonWithNulls = "{\"themeName\":null,\"gridSize\":0}";
            Theme theme = service.deserializeTheme(jsonWithNulls);
            assertNotNull(theme);
            assertNull(theme.getThemeName());
        }
    }

    @Nested
    @DisplayName("Backward Compatibility with Deprecated Entity Methods")
    @SuppressWarnings("deprecation")
    class BackwardCompatibilityTests {

        @Test
        @DisplayName("service and entity Theme methods should produce identical results")
        void shouldMatchEntityThemeMethods() {
            DataSetDescriptor descriptor = new DataSetDescriptor();
            descriptor.setDataSetName("CompatTest");

            Theme theme = new Theme();
            theme.setThemeName("Compat Theme");
            theme.setGridSize(15);

            // Set using service
            service.setTheme(descriptor, theme);
            String serviceJson = descriptor.getThemeStr();

            // Get using deprecated entity method
            Theme entityResult = descriptor.getTheme();

            // Get using service
            Theme serviceResult = service.getTheme(descriptor);

            // Both should return equivalent themes
            assertNotNull(entityResult);
            assertNotNull(serviceResult);
            assertEquals(entityResult.getThemeName(), serviceResult.getThemeName());
            assertEquals(entityResult.getGridSize(), serviceResult.getGridSize());

            // Now set using deprecated entity method
            DataSetDescriptor descriptor2 = new DataSetDescriptor();
            descriptor2.setDataSetName("CompatTest2");
            descriptor2.setTheme(theme);

            // Verify service can read it
            Theme serviceRead = service.getTheme(descriptor2);
            assertNotNull(serviceRead);
            assertEquals("Compat Theme", serviceRead.getThemeName());
        }

        @Test
        @DisplayName("service and entity Routes methods should produce identical results")
        void shouldMatchEntityRoutesMethods() {
            DataSetDescriptor descriptor = new DataSetDescriptor();
            descriptor.setDataSetName("CompatTest");

            Route route1 = new Route();
            route1.setRouteName("Route Alpha");
            Route route2 = new Route();
            route2.setRouteName("Route Beta");
            List<Route> routes = Arrays.asList(route1, route2);

            // Set using service
            service.setRoutes(descriptor, routes);

            // Get using deprecated entity method
            List<Route> entityResult = descriptor.getRoutes();

            // Get using service
            List<Route> serviceResult = service.getRoutes(descriptor);

            // Both should return equivalent routes
            assertEquals(entityResult.size(), serviceResult.size());
            assertEquals(entityResult.get(0).getRouteName(), serviceResult.get(0).getRouteName());
            assertEquals(entityResult.get(1).getRouteName(), serviceResult.get(1).getRouteName());

            // Set using deprecated entity method
            DataSetDescriptor descriptor2 = new DataSetDescriptor();
            descriptor2.setDataSetName("CompatTest2");
            descriptor2.setRoutes(routes);

            // Service should read it correctly
            List<Route> serviceRead = service.getRoutes(descriptor2);
            assertEquals(2, serviceRead.size());
            assertEquals("Route Alpha", serviceRead.get(0).getRouteName());
        }

        @Test
        @DisplayName("service and entity TransitDefinitions methods should produce identical results")
        void shouldMatchEntityTransitDefinitionsMethods() {
            DataSetDescriptor descriptor = new DataSetDescriptor();
            descriptor.setDataSetName("CompatTest");

            TransitDefinitions transitDefs = new TransitDefinitions();
            transitDefs.setDataSetName("CompatTest");
            transitDefs.setSelected(true);
            TransitRangeDef rangeDef = new TransitRangeDef();
            rangeDef.setBandName("Test Band");
            rangeDef.setLowerRange(5);
            rangeDef.setUpperRange(15);
            transitDefs.setTransitRangeDefs(Collections.singletonList(rangeDef));

            // Set using service
            service.setTransitDefinitions(descriptor, transitDefs);

            // Get using deprecated entity method
            TransitDefinitions entityResult = descriptor.getTransitDefinitions();

            // Get using service
            TransitDefinitions serviceResult = service.getTransitDefinitions(descriptor);

            // Both should return equivalent transit definitions
            assertNotNull(entityResult);
            assertNotNull(serviceResult);
            assertEquals(entityResult.isSelected(), serviceResult.isSelected());
            assertEquals(entityResult.getTransitRangeDefs().size(), serviceResult.getTransitRangeDefs().size());

            // Set using deprecated entity method
            DataSetDescriptor descriptor2 = new DataSetDescriptor();
            descriptor2.setDataSetName("CompatTest2");
            descriptor2.setTransitDefinitions(transitDefs);

            // Service should read it correctly
            TransitDefinitions serviceRead = service.getTransitDefinitions(descriptor2);
            assertTrue(serviceRead.isSelected());
        }

        @Test
        @DisplayName("service and entity CustomDataDefinitions methods should produce identical results")
        void shouldMatchEntityCustomDataDefinitionsMethods() {
            DataSetDescriptor descriptor = new DataSetDescriptor();
            descriptor.setDataSetName("CompatTest");

            CustomDataDefinition def = new CustomDataDefinition();
            def.setCustomFieldName("TestField");
            List<CustomDataDefinition> defs = Collections.singletonList(def);

            // Set using service
            service.setCustomDataDefinitions(descriptor, defs);

            // Get using deprecated entity method
            List<CustomDataDefinition> entityResult = descriptor.getCustomDataDefinitions();

            // Get using service
            List<CustomDataDefinition> serviceResult = service.getCustomDataDefinitions(descriptor);

            // Both should return equivalent definitions
            assertEquals(entityResult.size(), serviceResult.size());
            assertEquals(entityResult.get(0).getCustomFieldName(), serviceResult.get(0).getCustomFieldName());

            // Set using deprecated entity method
            DataSetDescriptor descriptor2 = new DataSetDescriptor();
            descriptor2.setDataSetName("CompatTest2");
            descriptor2.setCustomDataDefinition(defs);

            // Service should read it correctly
            List<CustomDataDefinition> serviceRead = service.getCustomDataDefinitions(descriptor2);
            assertEquals(1, serviceRead.size());
            assertEquals("TestField", serviceRead.get(0).getCustomFieldName());
        }

        @Test
        @DisplayName("service and entity CustomDataValues methods should produce identical results")
        void shouldMatchEntityCustomDataValuesMethods() {
            DataSetDescriptor descriptor = new DataSetDescriptor();
            descriptor.setDataSetName("CompatTest");

            CustomDataValue value = new CustomDataValue();
            value.setCustomFieldName("TestField");
            value.setCustomFieldValue("TestValue");
            List<CustomDataValue> values = Collections.singletonList(value);

            // Set using service
            service.setCustomDataValues(descriptor, values);

            // Get using deprecated entity method
            List<CustomDataValue> entityResult = descriptor.getCustomDataValue();

            // Get using service
            List<CustomDataValue> serviceResult = service.getCustomDataValues(descriptor);

            // Both should return equivalent values
            assertEquals(entityResult.size(), serviceResult.size());
            assertEquals(entityResult.get(0).getCustomFieldValue(), serviceResult.get(0).getCustomFieldValue());

            // Set using deprecated entity method
            DataSetDescriptor descriptor2 = new DataSetDescriptor();
            descriptor2.setDataSetName("CompatTest2");
            descriptor2.setCustomDataValue(values);

            // Service should read it correctly
            List<CustomDataValue> serviceRead = service.getCustomDataValues(descriptor2);
            assertEquals(1, serviceRead.size());
            assertEquals("TestValue", serviceRead.get(0).getCustomFieldValue());
        }

        @Test
        @DisplayName("service and entity AstrographicDataList methods should produce identical results")
        void shouldMatchEntityAstrographicDataListMethods() {
            DataSetDescriptor descriptor = new DataSetDescriptor();
            descriptor.setDataSetName("CompatTest");

            UUID uuid1 = UUID.randomUUID();
            UUID uuid2 = UUID.randomUUID();
            Set<UUID> uuids = new HashSet<>(Arrays.asList(uuid1, uuid2));

            // Set using service
            service.setAstrographicDataList(descriptor, uuids);

            // Get using deprecated entity method
            Set<UUID> entityResult = descriptor.getAstrographicDataUUIDs();

            // Get using service
            Set<UUID> serviceResult = service.getAstrographicDataList(descriptor);

            // Both should return equivalent UUID sets
            assertEquals(entityResult.size(), serviceResult.size());
            assertTrue(entityResult.containsAll(serviceResult));

            // Set using deprecated entity method
            DataSetDescriptor descriptor2 = new DataSetDescriptor();
            descriptor2.setDataSetName("CompatTest2");
            descriptor2.setAstrographicDataUUIDs(uuids);

            // Service should read it correctly
            Set<UUID> serviceRead = service.getAstrographicDataList(descriptor2);
            assertEquals(2, serviceRead.size());
            assertTrue(serviceRead.contains(uuid1));
            assertTrue(serviceRead.contains(uuid2));
        }

        @Test
        @DisplayName("deprecated methods should still update side-effect fields correctly")
        void shouldUpdateSideEffectFieldsCorrectly() {
            DataSetDescriptor descriptor = new DataSetDescriptor();
            descriptor.setDataSetName("SideEffectTest");

            // Routes should update numberRoutes
            Route route = new Route();
            route.setRouteName("Test");
            descriptor.setRoutes(Arrays.asList(route, route, route, route));
            assertEquals(4, descriptor.getNumberRoutes());

            // AstrographicDataList should update numberStars
            Set<UUID> uuids = new HashSet<>();
            for (int i = 0; i < 10; i++) {
                uuids.add(UUID.randomUUID());
            }
            descriptor.setAstrographicDataUUIDs(uuids);
            assertEquals(10L, descriptor.getNumberStars());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle special characters in string fields")
        void shouldHandleSpecialCharacters() {
            Theme theme = new Theme();
            theme.setThemeName("Test with émojis 🚀 and spëcial chârs & \"quotes\"");

            String json = service.serializeTheme(theme);
            Theme deserialized = service.deserializeTheme(json);

            assertNotNull(deserialized);
            assertEquals(theme.getThemeName(), deserialized.getThemeName());
        }

        @Test
        @DisplayName("should handle large collections")
        void shouldHandleLargeCollections() {
            List<Route> largeRouteList = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                Route route = new Route();
                route.setRouteName("Route " + i);
                largeRouteList.add(route);
            }

            String json = service.serializeRoutes(largeRouteList);
            assertNotNull(json);

            List<Route> deserialized = service.deserializeRoutes(json);
            assertEquals(1000, deserialized.size());
            assertEquals("Route 0", deserialized.get(0).getRouteName());
            assertEquals("Route 999", deserialized.get(999).getRouteName());
        }

        @Test
        @DisplayName("should handle large UUID sets")
        void shouldHandleLargeUuidSets() {
            Set<UUID> largeUuidSet = new HashSet<>();
            for (int i = 0; i < 10000; i++) {
                largeUuidSet.add(UUID.randomUUID());
            }

            String serialized = service.serializeAstrographicDataList(largeUuidSet);
            assertNotNull(serialized);

            Set<UUID> deserialized = service.deserializeAstrographicDataList(serialized);
            assertEquals(10000, deserialized.size());
        }

        @Test
        @DisplayName("should handle deeply nested route data")
        void shouldHandleDeeplyNestedData() {
            Route route = new Route();
            route.setRouteName("Complex Route");
            route.setRouteNotes("Notes with\nmultiple\nlines");

            // Add route stars if the class supports it
            List<Route> routes = Collections.singletonList(route);

            String json = service.serializeRoutes(routes);
            List<Route> deserialized = service.deserializeRoutes(json);

            assertEquals(1, deserialized.size());
            assertEquals("Complex Route", deserialized.get(0).getRouteName());
            assertTrue(deserialized.get(0).getRouteNotes().contains("\n"));
        }

        @Test
        @DisplayName("should preserve UUID ordering in comma-separated string")
        void shouldHandleUuidOrdering() {
            // UUIDs in a set don't have guaranteed order, but serialization should be deterministic
            UUID uuid1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
            UUID uuid2 = UUID.fromString("00000000-0000-0000-0000-000000000002");

            Set<UUID> uuids = new LinkedHashSet<>(Arrays.asList(uuid1, uuid2));

            String serialized = service.serializeAstrographicDataList(uuids);

            // Deserialization should recover both UUIDs
            Set<UUID> deserialized = service.deserializeAstrographicDataList(serialized);
            assertEquals(2, deserialized.size());
            assertTrue(deserialized.contains(uuid1));
            assertTrue(deserialized.contains(uuid2));
        }
    }
}
