package com.teamgannon.trips.routing.model;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RouteCacheKey.
 * <p>
 * Tests cover:
 * <ul>
 *   <li>Equality and hash code contracts</li>
 *   <li>Distance normalization</li>
 *   <li>Exclusion set handling</li>
 *   <li>Star collection hashing</li>
 *   <li>Factory methods</li>
 * </ul>
 */
@DisplayName("RouteCacheKey")
class RouteCacheKeyTest {

    // =========================================================================
    // Test Helpers
    // =========================================================================

    private RouteFindingOptions createOptions(String origin, String destination,
                                               double upper, double lower, int paths) {
        return RouteFindingOptions.builder()
                .originStarName(origin)
                .destinationStarName(destination)
                .upperBound(upper)
                .lowerBound(lower)
                .numberPaths(paths)
                .starExclusions(new HashSet<>())
                .polityExclusions(new HashSet<>())
                .build();
    }

    private RouteFindingOptions createOptionsWithExclusions(String origin, String destination,
                                                             Set<String> starExcl, Set<String> polityExcl) {
        return RouteFindingOptions.builder()
                .originStarName(origin)
                .destinationStarName(destination)
                .upperBound(8.0)
                .lowerBound(3.0)
                .numberPaths(3)
                .starExclusions(starExcl)
                .polityExclusions(polityExcl)
                .build();
    }

    private StarDisplayRecord createStar(String name) {
        StarDisplayRecord star = new StarDisplayRecord();
        star.setStarName(name);
        star.setActualCoordinates(new double[]{0, 0, 0});
        return star;
    }

    // =========================================================================
    // Equality Tests
    // =========================================================================

    @Nested
    @DisplayName("Equality")
    class EqualityTests {

        @Test
        @DisplayName("Same parameters should be equal")
        void sameParametersShouldBeEqual() {
            RouteFindingOptions options1 = createOptions("Sol", "Alpha Centauri", 8.0, 3.0, 3);
            RouteFindingOptions options2 = createOptions("Sol", "Alpha Centauri", 8.0, 3.0, 3);

            RouteCacheKey key1 = RouteCacheKey.fromOptions(options1);
            RouteCacheKey key2 = RouteCacheKey.fromOptions(options2);

            assertEquals(key1, key2);
            assertEquals(key1.hashCode(), key2.hashCode());
        }

        @Test
        @DisplayName("Different origin should not be equal")
        void differentOriginShouldNotBeEqual() {
            RouteFindingOptions options1 = createOptions("Sol", "Alpha Centauri", 8.0, 3.0, 3);
            RouteFindingOptions options2 = createOptions("Barnard's Star", "Alpha Centauri", 8.0, 3.0, 3);

            RouteCacheKey key1 = RouteCacheKey.fromOptions(options1);
            RouteCacheKey key2 = RouteCacheKey.fromOptions(options2);

            assertNotEquals(key1, key2);
        }

        @Test
        @DisplayName("Different destination should not be equal")
        void differentDestinationShouldNotBeEqual() {
            RouteFindingOptions options1 = createOptions("Sol", "Alpha Centauri", 8.0, 3.0, 3);
            RouteFindingOptions options2 = createOptions("Sol", "Sirius", 8.0, 3.0, 3);

            RouteCacheKey key1 = RouteCacheKey.fromOptions(options1);
            RouteCacheKey key2 = RouteCacheKey.fromOptions(options2);

            assertNotEquals(key1, key2);
        }

        @Test
        @DisplayName("Different upper bound should not be equal")
        void differentUpperBoundShouldNotBeEqual() {
            RouteFindingOptions options1 = createOptions("Sol", "Alpha Centauri", 8.0, 3.0, 3);
            RouteFindingOptions options2 = createOptions("Sol", "Alpha Centauri", 10.0, 3.0, 3);

            RouteCacheKey key1 = RouteCacheKey.fromOptions(options1);
            RouteCacheKey key2 = RouteCacheKey.fromOptions(options2);

            assertNotEquals(key1, key2);
        }

        @Test
        @DisplayName("Different lower bound should not be equal")
        void differentLowerBoundShouldNotBeEqual() {
            RouteFindingOptions options1 = createOptions("Sol", "Alpha Centauri", 8.0, 3.0, 3);
            RouteFindingOptions options2 = createOptions("Sol", "Alpha Centauri", 8.0, 2.0, 3);

            RouteCacheKey key1 = RouteCacheKey.fromOptions(options1);
            RouteCacheKey key2 = RouteCacheKey.fromOptions(options2);

            assertNotEquals(key1, key2);
        }

        @Test
        @DisplayName("Different number of paths should not be equal")
        void differentNumberPathsShouldNotBeEqual() {
            RouteFindingOptions options1 = createOptions("Sol", "Alpha Centauri", 8.0, 3.0, 3);
            RouteFindingOptions options2 = createOptions("Sol", "Alpha Centauri", 8.0, 3.0, 5);

            RouteCacheKey key1 = RouteCacheKey.fromOptions(options1);
            RouteCacheKey key2 = RouteCacheKey.fromOptions(options2);

            assertNotEquals(key1, key2);
        }

        @Test
        @DisplayName("Reflexive equality")
        void reflexiveEquality() {
            RouteFindingOptions options = createOptions("Sol", "Alpha Centauri", 8.0, 3.0, 3);
            RouteCacheKey key = RouteCacheKey.fromOptions(options);

            assertEquals(key, key);
        }

        @Test
        @DisplayName("Null should not be equal")
        void nullShouldNotBeEqual() {
            RouteFindingOptions options = createOptions("Sol", "Alpha Centauri", 8.0, 3.0, 3);
            RouteCacheKey key = RouteCacheKey.fromOptions(options);

            assertNotEquals(null, key);
        }

        @Test
        @DisplayName("Different type should not be equal")
        void differentTypeShouldNotBeEqual() {
            RouteFindingOptions options = createOptions("Sol", "Alpha Centauri", 8.0, 3.0, 3);
            RouteCacheKey key = RouteCacheKey.fromOptions(options);

            assertNotEquals("not a cache key", key);
        }
    }

    // =========================================================================
    // Distance Normalization Tests
    // =========================================================================

    @Nested
    @DisplayName("Distance Normalization")
    class DistanceNormalizationTests {

        @Test
        @DisplayName("Small floating point differences should be equal")
        void smallFloatingPointDifferencesShouldBeEqual() {
            // 8.001 and 8.004 should both normalize to 800 (8.00)
            RouteFindingOptions options1 = createOptions("Sol", "Alpha Centauri", 8.001, 3.0, 3);
            RouteFindingOptions options2 = createOptions("Sol", "Alpha Centauri", 8.004, 3.0, 3);

            RouteCacheKey key1 = RouteCacheKey.fromOptions(options1);
            RouteCacheKey key2 = RouteCacheKey.fromOptions(options2);

            assertEquals(key1, key2);
        }

        @Test
        @DisplayName("Larger differences should not be equal")
        void largerDifferencesShouldNotBeEqual() {
            // 8.01 and 8.02 should normalize to different values
            RouteFindingOptions options1 = createOptions("Sol", "Alpha Centauri", 8.01, 3.0, 3);
            RouteFindingOptions options2 = createOptions("Sol", "Alpha Centauri", 8.02, 3.0, 3);

            RouteCacheKey key1 = RouteCacheKey.fromOptions(options1);
            RouteCacheKey key2 = RouteCacheKey.fromOptions(options2);

            assertNotEquals(key1, key2);
        }

        @Test
        @DisplayName("Getters return normalized values")
        void gettersReturnNormalizedValues() {
            RouteFindingOptions options = createOptions("Sol", "Alpha Centauri", 8.567, 3.123, 3);
            RouteCacheKey key = RouteCacheKey.fromOptions(options);

            assertEquals(8.57, key.getUpperBound(), 0.001);
            assertEquals(3.12, key.getLowerBound(), 0.001);
        }
    }

    // =========================================================================
    // Exclusion Set Tests
    // =========================================================================

    @Nested
    @DisplayName("Exclusion Sets")
    class ExclusionSetTests {

        @Test
        @DisplayName("Same exclusions should be equal")
        void sameExclusionsShouldBeEqual() {
            Set<String> starExcl = new HashSet<>(Arrays.asList("M", "L", "T"));
            Set<String> polityExcl = new HashSet<>(Collections.singletonList("Terran"));

            RouteFindingOptions options1 = createOptionsWithExclusions("Sol", "Alpha Centauri", starExcl, polityExcl);
            RouteFindingOptions options2 = createOptionsWithExclusions("Sol", "Alpha Centauri",
                    new HashSet<>(Arrays.asList("L", "M", "T")), // Different order
                    new HashSet<>(Collections.singletonList("Terran")));

            RouteCacheKey key1 = RouteCacheKey.fromOptions(options1);
            RouteCacheKey key2 = RouteCacheKey.fromOptions(options2);

            assertEquals(key1, key2);
            assertEquals(key1.hashCode(), key2.hashCode());
        }

        @Test
        @DisplayName("Different star exclusions should not be equal")
        void differentStarExclusionsShouldNotBeEqual() {
            Set<String> starExcl1 = new HashSet<>(Arrays.asList("M", "L"));
            Set<String> starExcl2 = new HashSet<>(Arrays.asList("M", "T"));

            RouteFindingOptions options1 = createOptionsWithExclusions("Sol", "Alpha Centauri", starExcl1, new HashSet<>());
            RouteFindingOptions options2 = createOptionsWithExclusions("Sol", "Alpha Centauri", starExcl2, new HashSet<>());

            RouteCacheKey key1 = RouteCacheKey.fromOptions(options1);
            RouteCacheKey key2 = RouteCacheKey.fromOptions(options2);

            assertNotEquals(key1, key2);
        }

        @Test
        @DisplayName("Different polity exclusions should not be equal")
        void differentPolityExclusionsShouldNotBeEqual() {
            Set<String> polityExcl1 = new HashSet<>(Collections.singletonList("Terran"));
            Set<String> polityExcl2 = new HashSet<>(Collections.singletonList("Klingon"));

            RouteFindingOptions options1 = createOptionsWithExclusions("Sol", "Alpha Centauri", new HashSet<>(), polityExcl1);
            RouteFindingOptions options2 = createOptionsWithExclusions("Sol", "Alpha Centauri", new HashSet<>(), polityExcl2);

            RouteCacheKey key1 = RouteCacheKey.fromOptions(options1);
            RouteCacheKey key2 = RouteCacheKey.fromOptions(options2);

            assertNotEquals(key1, key2);
        }

        @Test
        @DisplayName("Null exclusions should be handled")
        void nullExclusionsShouldBeHandled() {
            RouteFindingOptions options = RouteFindingOptions.builder()
                    .originStarName("Sol")
                    .destinationStarName("Alpha Centauri")
                    .upperBound(8.0)
                    .lowerBound(3.0)
                    .numberPaths(3)
                    .starExclusions(null)
                    .polityExclusions(null)
                    .build();

            assertDoesNotThrow(() -> RouteCacheKey.fromOptions(options));
        }
    }

    // =========================================================================
    // Star Collection Hash Tests
    // =========================================================================

    @Nested
    @DisplayName("Star Collection Hash")
    class StarCollectionHashTests {

        @Test
        @DisplayName("Same stars should produce same hash")
        void sameStarsShouldProduceSameHash() {
            List<StarDisplayRecord> stars1 = Arrays.asList(
                    createStar("Sol"),
                    createStar("Alpha Centauri"),
                    createStar("Barnard's Star")
            );
            List<StarDisplayRecord> stars2 = Arrays.asList(
                    createStar("Sol"),
                    createStar("Alpha Centauri"),
                    createStar("Barnard's Star")
            );

            RouteFindingOptions options = createOptions("Sol", "Alpha Centauri", 8.0, 3.0, 3);

            RouteCacheKey key1 = RouteCacheKey.fromOptionsWithStars(options, stars1);
            RouteCacheKey key2 = RouteCacheKey.fromOptionsWithStars(options, stars2);

            assertEquals(key1, key2);
            assertEquals(key1.hashCode(), key2.hashCode());
        }

        @Test
        @DisplayName("Different order should produce same hash")
        void differentOrderShouldProduceSameHash() {
            List<StarDisplayRecord> stars1 = Arrays.asList(
                    createStar("Sol"),
                    createStar("Alpha Centauri"),
                    createStar("Barnard's Star")
            );
            List<StarDisplayRecord> stars2 = Arrays.asList(
                    createStar("Barnard's Star"),
                    createStar("Sol"),
                    createStar("Alpha Centauri")
            );

            RouteFindingOptions options = createOptions("Sol", "Alpha Centauri", 8.0, 3.0, 3);

            RouteCacheKey key1 = RouteCacheKey.fromOptionsWithStars(options, stars1);
            RouteCacheKey key2 = RouteCacheKey.fromOptionsWithStars(options, stars2);

            assertEquals(key1, key2);
        }

        @Test
        @DisplayName("Different stars should produce different hash")
        void differentStarsShouldProduceDifferentHash() {
            List<StarDisplayRecord> stars1 = Arrays.asList(
                    createStar("Sol"),
                    createStar("Alpha Centauri")
            );
            List<StarDisplayRecord> stars2 = Arrays.asList(
                    createStar("Sol"),
                    createStar("Sirius")
            );

            RouteFindingOptions options = createOptions("Sol", "Alpha Centauri", 8.0, 3.0, 3);

            RouteCacheKey key1 = RouteCacheKey.fromOptionsWithStars(options, stars1);
            RouteCacheKey key2 = RouteCacheKey.fromOptionsWithStars(options, stars2);

            assertNotEquals(key1, key2);
        }

        @Test
        @DisplayName("Key without stars should differ from key with stars")
        void keyWithoutStarsShouldDifferFromKeyWithStars() {
            List<StarDisplayRecord> stars = Arrays.asList(
                    createStar("Sol"),
                    createStar("Alpha Centauri")
            );

            RouteFindingOptions options = createOptions("Sol", "Alpha Centauri", 8.0, 3.0, 3);

            RouteCacheKey keyWithoutStars = RouteCacheKey.fromOptions(options);
            RouteCacheKey keyWithStars = RouteCacheKey.fromOptionsWithStars(options, stars);

            assertNotEquals(keyWithoutStars, keyWithStars);
        }
    }

    // =========================================================================
    // ToString Tests
    // =========================================================================

    @Nested
    @DisplayName("ToString")
    class ToStringTests {

        @Test
        @DisplayName("toString contains key information")
        void toStringContainsKeyInformation() {
            RouteFindingOptions options = createOptions("Sol", "Alpha Centauri", 8.0, 3.0, 3);
            RouteCacheKey key = RouteCacheKey.fromOptions(options);

            String str = key.toString();

            assertTrue(str.contains("Sol"));
            assertTrue(str.contains("Alpha Centauri"));
            assertTrue(str.contains("8.00"));
            assertTrue(str.contains("3.00"));
            assertTrue(str.contains("paths=3"));
        }

        @Test
        @DisplayName("toString includes stars hash when present")
        void toStringIncludesStarsHashWhenPresent() {
            List<StarDisplayRecord> stars = Arrays.asList(createStar("Sol"), createStar("Alpha Centauri"));
            RouteFindingOptions options = createOptions("Sol", "Alpha Centauri", 8.0, 3.0, 3);
            RouteCacheKey key = RouteCacheKey.fromOptionsWithStars(options, stars);

            String str = key.toString();

            assertTrue(str.contains("stars="));
        }
    }

    // =========================================================================
    // Getter Tests
    // =========================================================================

    @Nested
    @DisplayName("Getters")
    class GetterTests {

        @Test
        @DisplayName("Getters return correct values")
        void gettersReturnCorrectValues() {
            RouteFindingOptions options = createOptions("Sol", "Alpha Centauri", 8.5, 3.5, 5);
            RouteCacheKey key = RouteCacheKey.fromOptions(options);

            assertEquals("Sol", key.getOriginStarName());
            assertEquals("Alpha Centauri", key.getDestinationStarName());
            assertEquals(8.5, key.getUpperBound(), 0.01);
            assertEquals(3.5, key.getLowerBound(), 0.01);
            assertEquals(5, key.getNumberPaths());
        }
    }
}
