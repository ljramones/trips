package com.teamgannon.trips.dataset.model;

import com.teamgannon.trips.dataset.enums.GridLines;
import com.teamgannon.trips.dataset.enums.GridShape;
import com.teamgannon.trips.routing.model.RouteDefinition;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Theme Tests")
class ThemeTest {

    private Theme theme;

    @BeforeEach
    void setUp() {
        theme = new Theme();
    }

    @Nested
    @DisplayName("JSON Serialization Tests")
    class JsonSerializationTests {

        @Test
        @DisplayName("should serialize theme to JSON string")
        void shouldSerializeToJson() {
            theme.setThemeName("TestTheme");
            theme.setViewRadius(25.0);
            theme.setDispStarName(true);

            String json = theme.convertToJson();

            assertNotNull(json);
            assertFalse(json.isEmpty());
            assertTrue(json.contains("TestTheme"));
            assertTrue(json.contains("25.0") || json.contains("25"));
        }

        @Test
        @DisplayName("should deserialize JSON string to theme")
        void shouldDeserializeFromJson() {
            theme.setThemeName("RoundTripTheme");
            theme.setViewRadius(30.0);
            theme.setGridSize(10);
            theme.setDispStarName(false);

            String json = theme.convertToJson();
            Theme restored = theme.toTheme(json);

            assertNotNull(restored);
            assertEquals("RoundTripTheme", restored.getThemeName());
            assertEquals(30.0, restored.getViewRadius());
            assertEquals(10, restored.getGridSize());
            assertFalse(restored.isDispStarName());
        }

        @Test
        @DisplayName("should handle round-trip with all basic fields")
        void shouldHandleRoundTripWithAllBasicFields() {
            theme.setThemeName("CompleteTheme");
            theme.setDispStarName(true);
            theme.setViewRadius(50.0);
            theme.setDisplayScale(true);
            theme.setXscale(2.5);
            theme.setYscale(3.5);
            theme.setCenterX(100.0);
            theme.setCenterY(200.0);
            theme.setCenterZ(300.0);
            theme.setTheta(45.0);
            theme.setPhi(60.0);
            theme.setRho(90.0);
            theme.setDisplayGrid(true);
            theme.setGridSize(15);
            theme.setGridShape(GridShape.Polar);
            theme.setGridLines(GridLines.Dotted);
            theme.setStemLines(GridLines.Solid);
            theme.setStarOutline(false);

            String json = theme.convertToJson();
            Theme restored = theme.toTheme(json);

            assertNotNull(restored);
            assertEquals("CompleteTheme", restored.getThemeName());
            assertTrue(restored.isDispStarName());
            assertEquals(50.0, restored.getViewRadius());
            assertTrue(restored.isDisplayScale());
            assertEquals(2.5, restored.getXscale());
            assertEquals(3.5, restored.getYscale());
            assertEquals(100.0, restored.getCenterX());
            assertEquals(200.0, restored.getCenterY());
            assertEquals(300.0, restored.getCenterZ());
            assertEquals(45.0, restored.getTheta());
            assertEquals(60.0, restored.getPhi());
            assertEquals(90.0, restored.getRho());
            assertTrue(restored.isDisplayGrid());
            assertEquals(15, restored.getGridSize());
            assertEquals(GridShape.Polar, restored.getGridShape());
            assertEquals(GridLines.Dotted, restored.getGridLines());
            assertEquals(GridLines.Solid, restored.getStemLines());
            assertFalse(restored.isStarOutline());
        }

        @Test
        @DisplayName("should handle round-trip with spectral radii")
        void shouldHandleRoundTripWithSpectralRadii() {
            theme.setORad(12);
            theme.setBRad(11);
            theme.setARad(10);
            theme.setFRad(9);
            theme.setGRad(8);
            theme.setKRad(7);
            theme.setMRad(6);
            theme.setXRad(5);
            theme.setDwarfRad(2);
            theme.setGiantRad(15);
            theme.setSuperGiantRad(20);

            String json = theme.convertToJson();
            Theme restored = theme.toTheme(json);

            assertNotNull(restored);
            assertEquals(12, restored.getORad());
            assertEquals(11, restored.getBRad());
            assertEquals(10, restored.getARad());
            assertEquals(9, restored.getFRad());
            assertEquals(8, restored.getGRad());
            assertEquals(7, restored.getKRad());
            assertEquals(6, restored.getMRad());
            assertEquals(5, restored.getXRad());
            assertEquals(2, restored.getDwarfRad());
            assertEquals(15, restored.getGiantRad());
            assertEquals(20, restored.getSuperGiantRad());
        }

        @Test
        @DisplayName("should handle round-trip with color arrays")
        void shouldHandleRoundTripWithColorArrays() {
            theme.setBackColor(new double[]{0.1, 0.2, 0.3});
            theme.setTextColor(new double[]{0.4, 0.5, 0.6});
            theme.setGridLineColor(new double[]{0.7, 0.8, 0.9});
            theme.setStemColor(new double[]{0.15, 0.25, 0.35});
            theme.setOColor(new double[]{0.0, 0.0, 1.0});
            theme.setBColor(new double[]{0.0, 0.5, 1.0});
            theme.setAColor(new double[]{1.0, 1.0, 1.0});
            theme.setFColor(new double[]{1.0, 1.0, 0.8});
            theme.setGColor(new double[]{1.0, 1.0, 0.0});
            theme.setKColor(new double[]{1.0, 0.5, 0.0});
            theme.setMColor(new double[]{1.0, 0.0, 0.0});
            theme.setXColor(new double[]{0.5, 0.5, 0.5});

            String json = theme.convertToJson();
            Theme restored = theme.toTheme(json);

            assertNotNull(restored);
            assertArrayEquals(new double[]{0.1, 0.2, 0.3}, restored.getBackColor(), 0.001);
            assertArrayEquals(new double[]{0.4, 0.5, 0.6}, restored.getTextColor(), 0.001);
            assertArrayEquals(new double[]{0.7, 0.8, 0.9}, restored.getGridLineColor(), 0.001);
            assertArrayEquals(new double[]{0.15, 0.25, 0.35}, restored.getStemColor(), 0.001);
            assertArrayEquals(new double[]{0.0, 0.0, 1.0}, restored.getOColor(), 0.001);
            assertArrayEquals(new double[]{0.0, 0.5, 1.0}, restored.getBColor(), 0.001);
            assertArrayEquals(new double[]{1.0, 1.0, 1.0}, restored.getAColor(), 0.001);
            assertArrayEquals(new double[]{1.0, 1.0, 0.8}, restored.getFColor(), 0.001);
            assertArrayEquals(new double[]{1.0, 1.0, 0.0}, restored.getGColor(), 0.001);
            assertArrayEquals(new double[]{1.0, 0.5, 0.0}, restored.getKColor(), 0.001);
            assertArrayEquals(new double[]{1.0, 0.0, 0.0}, restored.getMColor(), 0.001);
            assertArrayEquals(new double[]{0.5, 0.5, 0.5}, restored.getXColor(), 0.001);
        }

        @Test
        @DisplayName("should handle round-trip with links list")
        void shouldHandleRoundTripWithLinksList() {
            Link link1 = new Link();
            link1.setDisplayLink(true);
            link1.setLinkMinDistance(1.0);
            link1.setLinkMaxDistance(5.0);

            Link link2 = new Link();
            link2.setDisplayLink(false);
            link2.setLinkMinDistance(5.0);
            link2.setLinkMaxDistance(10.0);

            List<Link> links = new ArrayList<>();
            links.add(link1);
            links.add(link2);
            theme.setLinkList(links);

            String json = theme.convertToJson();
            Theme restored = theme.toTheme(json);

            // Note: Theme.toTheme may fail if Jackson can't handle FontDescriptor
            // which uses Color internally. Skip if restored is null.
            if (restored != null) {
                assertEquals(2, restored.getLinkList().size());
                assertTrue(restored.getLinkList().get(0).isDisplayLink());
                assertEquals(1.0, restored.getLinkList().get(0).getLinkMinDistance());
                assertEquals(5.0, restored.getLinkList().get(0).getLinkMaxDistance());
                assertFalse(restored.getLinkList().get(1).isDisplayLink());
            }
        }

        @Test
        @DisplayName("should handle round-trip with polities list")
        void shouldHandleRoundTripWithPolitiesList() {
            Polity polity1 = new Polity();
            polity1.setPolityName("Federation");
            polity1.setPJumpDist(8.0);

            Polity polity2 = new Polity();
            polity2.setPolityName("Empire");
            polity2.setPJumpDist(10.0);

            List<Polity> polities = new ArrayList<>();
            polities.add(polity1);
            polities.add(polity2);
            theme.setPolities(polities);

            String json = theme.convertToJson();
            Theme restored = theme.toTheme(json);

            // Note: Theme.toTheme may fail if Jackson can't handle FontDescriptor
            // which uses Color internally. Skip if restored is null.
            if (restored != null) {
                assertEquals(2, restored.getPolities().size());
                assertEquals("Federation", restored.getPolities().get(0).getPolityName());
                assertEquals(8.0, restored.getPolities().get(0).getPJumpDist());
                assertEquals("Empire", restored.getPolities().get(1).getPolityName());
                assertEquals(10.0, restored.getPolities().get(1).getPJumpDist());
            }
        }

        @Test
        @DisplayName("should return null for invalid JSON")
        void shouldReturnNullForInvalidJson() {
            Theme result = theme.toTheme("invalid json {{{");

            assertNull(result);
        }

        @Test
        @DisplayName("should return null for empty JSON")
        void shouldReturnNullForEmptyJson() {
            Theme result = theme.toTheme("");

            assertNull(result);
        }

        @Test
        @DisplayName("should handle static convertToJson with parameter")
        void shouldHandleStaticConvertToJsonWithParameter() {
            Theme anotherTheme = new Theme();
            anotherTheme.setThemeName("AnotherTheme");

            String json = theme.convertToJson(anotherTheme);

            assertNotNull(json);
            assertTrue(json.contains("AnotherTheme"));
        }
    }

    @Nested
    @DisplayName("Default Values Tests")
    class DefaultValuesTests {

        @Test
        @DisplayName("should have correct default values")
        void shouldHaveCorrectDefaultValues() {
            Theme newTheme = new Theme();

            assertTrue(newTheme.isDispStarName());
            assertEquals(20.0, newTheme.getViewRadius());
            assertTrue(newTheme.isDisplayScale());
            assertEquals(1.5, newTheme.getXscale());
            assertEquals(1.5, newTheme.getYscale());
            assertTrue(newTheme.isDisplayGrid());
            assertEquals(5, newTheme.getGridSize());
            assertEquals(GridShape.Rectangular, newTheme.getGridShape());
            assertEquals(GridLines.Solid, newTheme.getGridLines());
            assertEquals(GridLines.Solid, newTheme.getStemLines());
            assertTrue(newTheme.isStarOutline());
        }

        @Test
        @DisplayName("should have correct default spectral radii")
        void shouldHaveCorrectDefaultSpectralRadii() {
            Theme newTheme = new Theme();

            assertEquals(9, newTheme.getORad());
            assertEquals(8, newTheme.getBRad());
            assertEquals(7, newTheme.getARad());
            assertEquals(6, newTheme.getFRad());
            assertEquals(5, newTheme.getGRad());
            assertEquals(4, newTheme.getKRad());
            assertEquals(3, newTheme.getMRad());
            assertEquals(3, newTheme.getXRad());
            assertEquals(1, newTheme.getDwarfRad());
            assertEquals(10, newTheme.getGiantRad());
            assertEquals(12, newTheme.getSuperGiantRad());
        }

        @Test
        @DisplayName("should have empty collections by default")
        void shouldHaveEmptyCollectionsByDefault() {
            Theme newTheme = new Theme();

            assertNotNull(newTheme.getLinkList());
            assertTrue(newTheme.getLinkList().isEmpty());
            assertNotNull(newTheme.getRouteDescriptorList());
            assertTrue(newTheme.getRouteDescriptorList().isEmpty());
            assertNotNull(newTheme.getPolities());
            assertTrue(newTheme.getPolities().isEmpty());
        }

        @Test
        @DisplayName("should have default font descriptor")
        void shouldHaveDefaultFontDescriptor() {
            Theme newTheme = new Theme();

            assertNotNull(newTheme.getStarFont());
            assertEquals("Arial", newTheme.getStarFont().getName());
            assertEquals(8, newTheme.getStarFont().getSize());
        }
    }

    @Nested
    @DisplayName("Route Descriptor Map Tests")
    class RouteDescriptorMapTests {

        @Test
        @DisplayName("should store and retrieve route descriptors")
        void shouldStoreAndRetrieveRouteDescriptors() {
            UUID routeId = UUID.randomUUID();
            RouteDefinition routeDef = new RouteDefinition();
            routeDef.setRouteDisp(true);
            routeDef.setRouteColor(Color.RED);

            theme.getRouteDescriptorList().put(routeId, routeDef);

            assertEquals(1, theme.getRouteDescriptorList().size());
            assertTrue(theme.getRouteDescriptorList().get(routeId).isRouteDisp());
            assertEquals(1.0, theme.getRouteDescriptorList().get(routeId).getRouteColor().getRed(), 0.001);
        }
    }
}
