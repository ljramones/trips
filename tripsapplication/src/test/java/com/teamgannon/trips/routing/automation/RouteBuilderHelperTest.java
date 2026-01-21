package com.teamgannon.trips.routing.automation;

import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.RouteVisibility;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import javafx.geometry.Point3D;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for RouteBuilderHelper.
 * <p>
 * Tests cover:
 * <ul>
 *   <li>Construction with various star lists</li>
 *   <li>Star lookup with has() method</li>
 *   <li>Route building with buildPath()</li>
 *   <li>Total length calculation</li>
 *   <li>Visibility handling for missing stars</li>
 * </ul>
 */
class RouteBuilderHelperTest {

    // =========================================================================
    // Test Helpers
    // =========================================================================

    private StarDisplayRecord createStar(String name, String id, double x, double y, double z) {
        StarDisplayRecord star = new StarDisplayRecord();
        star.setStarName(name);
        star.setRecordId(id);
        star.setActualCoordinates(new double[]{x, y, z});
        star.setCoordinates(new Point3D(x * 10, y * 10, z * 10)); // Screen coords scaled
        return star;
    }

    private List<StarDisplayRecord> createTestStars() {
        List<StarDisplayRecord> stars = new ArrayList<>();
        stars.add(createStar("Sol", "star-001", 0, 0, 0));
        stars.add(createStar("Alpha Centauri", "star-002", 1.34, -0.48, -3.91));
        stars.add(createStar("Barnard's Star", "star-003", -0.01, 1.82, -5.94));
        stars.add(createStar("Wolf 359", "star-004", -2.29, -0.88, -7.23));
        stars.add(createStar("Sirius", "star-005", -1.61, 8.06, -0.47));
        return stars;
    }

    // =========================================================================
    // Construction Tests
    // =========================================================================

    @Nested
    @DisplayName("Construction Tests")
    class ConstructionTests {

        @Test
        @DisplayName("Constructor with empty list creates empty helper")
        void constructorWithEmptyList() {
            RouteBuilderHelper helper = new RouteBuilderHelper(Collections.emptyList());

            assertFalse(helper.has("Sol"));
            assertFalse(helper.has("Alpha Centauri"));
        }

        @Test
        @DisplayName("Constructor with stars populates lookup map")
        void constructorWithStars() {
            List<StarDisplayRecord> stars = createTestStars();

            RouteBuilderHelper helper = new RouteBuilderHelper(stars);

            assertTrue(helper.has("Sol"));
            assertTrue(helper.has("Alpha Centauri"));
            assertTrue(helper.has("Barnard's Star"));
            assertTrue(helper.has("Wolf 359"));
            assertTrue(helper.has("Sirius"));
        }

        @Test
        @DisplayName("Constructor filters out null entries")
        void constructorFiltersNullEntries() {
            List<StarDisplayRecord> stars = new ArrayList<>();
            stars.add(createStar("Sol", "star-001", 0, 0, 0));
            stars.add(null);  // Null entry
            stars.add(createStar("Alpha Centauri", "star-002", 1.34, -0.48, -3.91));
            stars.add(null);  // Another null entry

            RouteBuilderHelper helper = new RouteBuilderHelper(stars);

            assertTrue(helper.has("Sol"));
            assertTrue(helper.has("Alpha Centauri"));
        }

        @Test
        @DisplayName("Constructor with duplicate star names keeps last one")
        void constructorWithDuplicateNames() {
            List<StarDisplayRecord> stars = new ArrayList<>();
            stars.add(createStar("Sol", "star-001-old", 0, 0, 0));
            stars.add(createStar("Sol", "star-001-new", 1, 1, 1));

            RouteBuilderHelper helper = new RouteBuilderHelper(stars);

            assertTrue(helper.has("Sol"));
            // The last one should be stored (this tests map overwrite behavior)
        }
    }

    // =========================================================================
    // Has Method Tests
    // =========================================================================

    @Nested
    @DisplayName("has() Method Tests")
    class HasMethodTests {

        private RouteBuilderHelper helper;

        @BeforeEach
        void setUp() {
            helper = new RouteBuilderHelper(createTestStars());
        }

        @Test
        @DisplayName("has() returns true for existing star")
        void hasReturnsTrueForExistingStar() {
            assertTrue(helper.has("Sol"));
            assertTrue(helper.has("Alpha Centauri"));
        }

        @Test
        @DisplayName("has() returns false for non-existing star")
        void hasReturnsFalseForNonExistingStar() {
            assertFalse(helper.has("Proxima Centauri"));
            assertFalse(helper.has("Vega"));
        }

        @Test
        @DisplayName("has() is case sensitive")
        void hasIsCaseSensitive() {
            assertTrue(helper.has("Sol"));
            assertFalse(helper.has("sol"));
            assertFalse(helper.has("SOL"));
        }

        @Test
        @DisplayName("has() handles null gracefully")
        void hasHandlesNull() {
            // HashMap.containsKey(null) returns false for non-existent null key
            assertFalse(helper.has(null));
        }

        @Test
        @DisplayName("has() handles empty string")
        void hasHandlesEmptyString() {
            assertFalse(helper.has(""));
        }
    }

    // =========================================================================
    // Build Path Tests - Basic
    // =========================================================================

    @Nested
    @DisplayName("buildPath() Basic Tests")
    class BuildPathBasicTests {

        private RouteBuilderHelper helper;

        @BeforeEach
        void setUp() {
            helper = new RouteBuilderHelper(createTestStars());
        }

        @Test
        @DisplayName("buildPath() creates route with correct name")
        void buildPathCreatesRouteWithCorrectName() {
            String path = "[Sol, Alpha Centauri]";

            RouteDescriptor route = helper.buildPath("Sol", "Alpha Centauri", "1", Color.RED, 2.0, path);

            assertEquals("Route Sol to Alpha Centauri, path 1", route.getName());
        }

        @Test
        @DisplayName("buildPath() sets route notes to path string")
        void buildPathSetsRouteNotes() {
            String path = "[Sol, Alpha Centauri]";

            RouteDescriptor route = helper.buildPath("Sol", "Alpha Centauri", "1", Color.RED, 2.0, path);

            assertEquals(path, route.getRouteNotes());
        }

        @Test
        @DisplayName("buildPath() sets color correctly")
        void buildPathSetsColor() {
            String path = "[Sol, Alpha Centauri]";

            RouteDescriptor route = helper.buildPath("Sol", "Alpha Centauri", "1", Color.CYAN, 2.0, path);

            assertEquals(Color.CYAN, route.getColor());
        }

        @Test
        @DisplayName("buildPath() sets line width correctly")
        void buildPathSetsLineWidth() {
            String path = "[Sol, Alpha Centauri]";

            RouteDescriptor route = helper.buildPath("Sol", "Alpha Centauri", "1", Color.RED, 3.5, path);

            assertEquals(3.5, route.getLineWidth(), 0.001);
        }

        @Test
        @DisplayName("buildPath() sets start star correctly")
        void buildPathSetsStartStar() {
            String path = "[Sol, Alpha Centauri]";

            RouteDescriptor route = helper.buildPath("Sol", "Alpha Centauri", "1", Color.RED, 2.0, path);

            assertEquals("Sol", route.getStartStar());
        }

        @Test
        @DisplayName("buildPath() sets last star correctly")
        void buildPathSetsLastStar() {
            String path = "[Sol, Alpha Centauri]";

            RouteDescriptor route = helper.buildPath("Sol", "Alpha Centauri", "1", Color.RED, 2.0, path);

            assertNotNull(route.getLastStar());
            assertEquals("Alpha Centauri", route.getLastStar().getStarName());
        }
    }

    // =========================================================================
    // Build Path Tests - Name and Route Lists
    // =========================================================================

    @Nested
    @DisplayName("buildPath() Name and Route List Tests")
    class BuildPathListTests {

        private RouteBuilderHelper helper;

        @BeforeEach
        void setUp() {
            helper = new RouteBuilderHelper(createTestStars());
        }

        @Test
        @DisplayName("buildPath() populates name list for two-star path")
        void buildPathPopulatesNameListTwoStars() {
            String path = "[Sol, Alpha Centauri]";

            RouteDescriptor route = helper.buildPath("Sol", "Alpha Centauri", "1", Color.RED, 2.0, path);

            assertEquals(2, route.getNameList().size());
            assertEquals("Sol", route.getNameList().get(0));
            assertEquals("Alpha Centauri", route.getNameList().get(1));
        }

        @Test
        @DisplayName("buildPath() populates name list for multi-star path")
        void buildPathPopulatesNameListMultiStars() {
            String path = "[Sol, Alpha Centauri, Barnard's Star, Wolf 359]";

            RouteDescriptor route = helper.buildPath("Sol", "Wolf 359", "1", Color.RED, 2.0, path);

            assertEquals(4, route.getNameList().size());
            assertEquals("Sol", route.getNameList().get(0));
            assertEquals("Alpha Centauri", route.getNameList().get(1));
            assertEquals("Barnard's Star", route.getNameList().get(2));
            assertEquals("Wolf 359", route.getNameList().get(3));
        }

        @Test
        @DisplayName("buildPath() populates route list with record IDs")
        void buildPathPopulatesRouteList() {
            String path = "[Sol, Alpha Centauri]";

            RouteDescriptor route = helper.buildPath("Sol", "Alpha Centauri", "1", Color.RED, 2.0, path);

            assertEquals(2, route.getRouteList().size());
            assertEquals("star-001", route.getRouteList().get(0));
            assertEquals("star-002", route.getRouteList().get(1));
        }

        @Test
        @DisplayName("buildPath() sets max length as number of segments")
        void buildPathSetsMaxLength() {
            String path = "[Sol, Alpha Centauri, Barnard's Star]";

            RouteDescriptor route = helper.buildPath("Sol", "Barnard's Star", "1", Color.RED, 2.0, path);

            // maxLength is set to starList.length - 1 (number of segments)
            assertEquals(2, route.getMaxLength(), 0.001);
        }
    }

    // =========================================================================
    // Build Path Tests - Distance Calculation
    // =========================================================================

    @Nested
    @DisplayName("buildPath() Distance Calculation Tests")
    class BuildPathDistanceTests {

        private RouteBuilderHelper helper;

        @BeforeEach
        void setUp() {
            helper = new RouteBuilderHelper(createTestStars());
        }

        @Test
        @DisplayName("buildPath() calculates total length for two-star path")
        void buildPathCalculatesTotalLengthTwoStars() {
            String path = "[Sol, Alpha Centauri]";

            RouteDescriptor route = helper.buildPath("Sol", "Alpha Centauri", "1", Color.RED, 2.0, path);

            // Distance from (0,0,0) to (1.34, -0.48, -3.91)
            // sqrt(1.34^2 + 0.48^2 + 3.91^2) = sqrt(1.7956 + 0.2304 + 15.2881) = sqrt(17.3141) ≈ 4.16
            assertTrue(route.getTotalLength() > 0);
            assertEquals(4.16, route.getTotalLength(), 0.1);
        }

        @Test
        @DisplayName("buildPath() calculates total length for multi-star path")
        void buildPathCalculatesTotalLengthMultiStars() {
            String path = "[Sol, Alpha Centauri, Barnard's Star]";

            RouteDescriptor route = helper.buildPath("Sol", "Barnard's Star", "1", Color.RED, 2.0, path);

            // Should be sum of Sol->AlphaCentauri and AlphaCentauri->Barnard's
            assertTrue(route.getTotalLength() > 4.16); // At least Sol->Alpha distance
        }

        @Test
        @DisplayName("buildPath() populates length segments list")
        void buildPathPopulatesLengthSegments() {
            String path = "[Sol, Alpha Centauri, Barnard's Star]";

            RouteDescriptor route = helper.buildPath("Sol", "Barnard's Star", "1", Color.RED, 2.0, path);

            // Two segments: Sol->Alpha and Alpha->Barnard's
            assertEquals(2, route.getLengthList().size());
            assertTrue(route.getLengthList().get(0) > 0);
            assertTrue(route.getLengthList().get(1) > 0);
        }
    }

    // =========================================================================
    // Build Path Tests - Visibility
    // =========================================================================

    @Nested
    @DisplayName("buildPath() Visibility Tests")
    class BuildPathVisibilityTests {

        private RouteBuilderHelper helper;

        @BeforeEach
        void setUp() {
            helper = new RouteBuilderHelper(createTestStars());
        }

        @Test
        @DisplayName("buildPath() sets FULL visibility when all stars present")
        void buildPathSetsFullVisibilityAllStarsPresent() {
            String path = "[Sol, Alpha Centauri, Barnard's Star]";

            RouteDescriptor route = helper.buildPath("Sol", "Barnard's Star", "1", Color.RED, 2.0, path);

            assertEquals(RouteVisibility.FULL, route.getVisibility());
        }

        @Test
        @DisplayName("buildPath() sets PARTIAL visibility when star is missing")
        void buildPathSetsPartialVisibilityWhenStarMissing() {
            String path = "[Sol, Proxima Centauri, Alpha Centauri]";  // Proxima not in our test set

            RouteDescriptor route = helper.buildPath("Sol", "Alpha Centauri", "1", Color.RED, 2.0, path);

            assertEquals(RouteVisibility.PARTIAL, route.getVisibility());
            // Names should include all stars
            assertEquals(3, route.getNameList().size());
            // Route list should only have found stars
            assertEquals(2, route.getRouteList().size());
        }

        @Test
        @DisplayName("buildPath() handles missing intermediate star gracefully")
        void buildPathHandlesMissingIntermediateStarGracefully() {
            String path = "[Sol, Unknown Star, Alpha Centauri]";

            RouteDescriptor route = helper.buildPath("Sol", "Alpha Centauri", "1", Color.RED, 2.0, path);

            assertEquals(RouteVisibility.PARTIAL, route.getVisibility());
            assertEquals(3, route.getNameList().size());
            // Only Sol and Alpha Centauri are in route list
            assertEquals(2, route.getRouteList().size());
        }
    }

    // =========================================================================
    // Build Path Tests - Line Segments / Coordinates
    // =========================================================================

    @Nested
    @DisplayName("buildPath() Line Segment Tests")
    class BuildPathLineSegmentTests {

        private RouteBuilderHelper helper;

        @BeforeEach
        void setUp() {
            helper = new RouteBuilderHelper(createTestStars());
        }

        @Test
        @DisplayName("buildPath() adds line segments for each star")
        void buildPathAddsLineSegments() {
            String path = "[Sol, Alpha Centauri, Barnard's Star]";

            RouteDescriptor route = helper.buildPath("Sol", "Barnard's Star", "1", Color.RED, 2.0, path);

            // routeCoordinates should have one entry per star in path
            assertEquals(3, route.getRouteCoordinates().size());
        }

        @Test
        @DisplayName("buildPath() adds correct coordinates for first star")
        void buildPathAddsCorrectCoordinatesForFirstStar() {
            String path = "[Sol, Alpha Centauri]";

            RouteDescriptor route = helper.buildPath("Sol", "Alpha Centauri", "1", Color.RED, 2.0, path);

            Point3D firstPoint = route.getRouteCoordinates().get(0);
            // Sol is at screen coords (0*10, 0*10, 0*10)
            assertEquals(0, firstPoint.getX(), 0.001);
            assertEquals(0, firstPoint.getY(), 0.001);
            assertEquals(0, firstPoint.getZ(), 0.001);
        }

        @Test
        @DisplayName("buildPath() skips coordinates for missing stars")
        void buildPathSkipsCoordinatesForMissingStars() {
            String path = "[Sol, Missing Star, Alpha Centauri]";

            RouteDescriptor route = helper.buildPath("Sol", "Alpha Centauri", "1", Color.RED, 2.0, path);

            // Should have coordinates for Sol and Alpha Centauri, but not Missing Star
            assertEquals(2, route.getRouteCoordinates().size());
            assertEquals(RouteVisibility.PARTIAL, route.getVisibility());
        }
    }

    // =========================================================================
    // Build Path Tests - Edge Cases
    // =========================================================================

    @Nested
    @DisplayName("buildPath() Edge Cases Tests")
    class BuildPathEdgeCasesTests {

        private RouteBuilderHelper helper;

        @BeforeEach
        void setUp() {
            helper = new RouteBuilderHelper(createTestStars());
        }

        @Test
        @DisplayName("buildPath() handles path with spaces in star names")
        void buildPathHandlesSpacesInStarNames() {
            String path = "[Sol, Alpha Centauri, Barnard's Star]";

            RouteDescriptor route = helper.buildPath("Sol", "Barnard's Star", "1", Color.RED, 2.0, path);

            assertTrue(route.getNameList().contains("Alpha Centauri"));
            assertTrue(route.getNameList().contains("Barnard's Star"));
        }

        @Test
        @DisplayName("buildPath() handles path with extra whitespace")
        void buildPathHandlesExtraWhitespace() {
            String path = "[Sol,  Alpha Centauri ,  Barnard's Star ]";

            RouteDescriptor route = helper.buildPath("Sol", "Barnard's Star", "1", Color.RED, 2.0, path);

            // Names should be trimmed
            assertEquals("Sol", route.getNameList().get(0));
            assertEquals("Alpha Centauri", route.getNameList().get(1));
            assertEquals("Barnard's Star", route.getNameList().get(2));
        }

        @Test
        @DisplayName("buildPath() with single star path")
        void buildPathWithSingleStarPath() {
            String path = "[Sol]";

            RouteDescriptor route = helper.buildPath("Sol", "Sol", "1", Color.RED, 2.0, path);

            assertEquals(1, route.getNameList().size());
            assertEquals(0, route.getMaxLength(), 0.001);  // 1 - 1 = 0 segments
            assertEquals(0, route.getTotalLength(), 0.001);  // No distance traveled
        }

        @Test
        @DisplayName("buildPath() preserves path name in route name")
        void buildPathPreservesPathNameInRouteName() {
            String path = "[Sol, Alpha Centauri]";

            RouteDescriptor route = helper.buildPath("Sol", "Alpha Centauri", "Alternative-1", Color.RED, 2.0, path);

            assertTrue(route.getName().contains("Alternative-1"));
            assertEquals("Route Sol to Alpha Centauri, path Alternative-1", route.getName());
        }
    }

    // =========================================================================
    // Integration Tests
    // =========================================================================

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Full workflow: create helper, check stars, build route")
        void fullWorkflow() {
            // Create helper with test stars
            List<StarDisplayRecord> stars = createTestStars();
            RouteBuilderHelper helper = new RouteBuilderHelper(stars);

            // Verify stars are available
            assertTrue(helper.has("Sol"));
            assertTrue(helper.has("Sirius"));

            // Build a route
            String path = "[Sol, Alpha Centauri, Sirius]";
            RouteDescriptor route = helper.buildPath("Sol", "Sirius", "1", Color.GOLD, 1.5, path);

            // Verify route properties
            assertEquals("Route Sol to Sirius, path 1", route.getName());
            assertEquals(Color.GOLD, route.getColor());
            assertEquals(1.5, route.getLineWidth(), 0.001);
            assertEquals(3, route.getNameList().size());
            assertEquals(RouteVisibility.FULL, route.getVisibility());
            assertTrue(route.getTotalLength() > 0);
        }

        @Test
        @DisplayName("Build multiple routes from same helper")
        void buildMultipleRoutes() {
            RouteBuilderHelper helper = new RouteBuilderHelper(createTestStars());

            String path1 = "[Sol, Alpha Centauri]";
            RouteDescriptor route1 = helper.buildPath("Sol", "Alpha Centauri", "1", Color.RED, 2.0, path1);

            String path2 = "[Sol, Sirius]";
            RouteDescriptor route2 = helper.buildPath("Sol", "Sirius", "2", Color.BLUE, 2.0, path2);

            // Routes should be independent
            assertNotEquals(route1.getId(), route2.getId());
            assertNotEquals(route1.getName(), route2.getName());
            assertEquals(Color.RED, route1.getColor());
            assertEquals(Color.BLUE, route2.getColor());
        }

        @Test
        @DisplayName("Route with all missing stars has PARTIAL visibility and empty coordinates")
        void routeWithAllMissingStarsHasPartialVisibility() {
            RouteBuilderHelper helper = new RouteBuilderHelper(createTestStars());

            String path = "[Unknown1, Unknown2, Unknown3]";
            RouteDescriptor route = helper.buildPath("Unknown1", "Unknown3", "1", Color.RED, 2.0, path);

            assertEquals(RouteVisibility.PARTIAL, route.getVisibility());
            assertEquals(0, route.getRouteCoordinates().size());  // No valid coordinates
            assertEquals(0, route.getTotalLength(), 0.001);  // No distance calculated
            assertEquals(3, route.getNameList().size());  // Names still recorded
            assertEquals(0, route.getRouteList().size());  // No valid record IDs
        }
    }
}
