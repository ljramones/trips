package com.teamgannon.trips.jpa.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for equals() and hashCode() implementations across all JPA entities.
 */
@DisplayName("JPA Entity Equals/HashCode Tests")
class JpaEntityEqualsHashCodeTest {

    @Nested
    @DisplayName("TripsPrefs")
    class TripsPrefsTests {

        @Test
        @DisplayName("should be equal when IDs match")
        void shouldBeEqualWhenIdsMatch() {
            TripsPrefs prefs1 = new TripsPrefs();
            prefs1.setId("prefs-123");

            TripsPrefs prefs2 = new TripsPrefs();
            prefs2.setId("prefs-123");

            assertEquals(prefs1, prefs2);
            assertEquals(prefs1.hashCode(), prefs2.hashCode());
        }

        @Test
        @DisplayName("should not be equal when IDs differ")
        void shouldNotBeEqualWhenIdsDiffer() {
            TripsPrefs prefs1 = new TripsPrefs();
            prefs1.setId("prefs-123");

            TripsPrefs prefs2 = new TripsPrefs();
            prefs2.setId("prefs-456");

            assertNotEquals(prefs1, prefs2);
        }

        @Test
        @DisplayName("should not be equal to null")
        void shouldNotBeEqualToNull() {
            TripsPrefs prefs = new TripsPrefs();
            prefs.setId("prefs-123");

            assertNotEquals(null, prefs);
        }

        @Test
        @DisplayName("should work in HashSet")
        void shouldWorkInHashSet() {
            TripsPrefs prefs1 = new TripsPrefs();
            prefs1.setId("prefs-123");

            TripsPrefs prefs2 = new TripsPrefs();
            prefs2.setId("prefs-123");

            TripsPrefs prefs3 = new TripsPrefs();
            prefs3.setId("prefs-456");

            Set<TripsPrefs> set = new HashSet<>();
            set.add(prefs1);
            set.add(prefs2); // Same ID, should not add
            set.add(prefs3);

            assertEquals(2, set.size());
        }
    }

    @Nested
    @DisplayName("TransitSettings")
    class TransitSettingsTests {

        @Test
        @DisplayName("should be equal when IDs match")
        void shouldBeEqualWhenIdsMatch() {
            TransitSettings settings1 = new TransitSettings();
            settings1.setId("transit-123");

            TransitSettings settings2 = new TransitSettings();
            settings2.setId("transit-123");

            assertEquals(settings1, settings2);
            assertEquals(settings1.hashCode(), settings2.hashCode());
        }

        @Test
        @DisplayName("should not be equal when IDs differ")
        void shouldNotBeEqualWhenIdsDiffer() {
            TransitSettings settings1 = new TransitSettings();
            settings1.setId("transit-123");

            TransitSettings settings2 = new TransitSettings();
            settings2.setId("transit-456");

            assertNotEquals(settings1, settings2);
        }

        @Test
        @DisplayName("should work in HashSet")
        void shouldWorkInHashSet() {
            TransitSettings settings1 = new TransitSettings();
            settings1.setId("transit-123");

            TransitSettings settings2 = new TransitSettings();
            settings2.setId("transit-123");

            Set<TransitSettings> set = new HashSet<>();
            set.add(settings1);
            set.add(settings2);

            assertEquals(1, set.size());
        }
    }

    @Nested
    @DisplayName("AsteroidBelt")
    class AsteroidBeltTests {

        @Test
        @DisplayName("should be equal when IDs match")
        void shouldBeEqualWhenIdsMatch() {
            UUID sharedId = UUID.randomUUID();

            AsteroidBelt belt1 = new AsteroidBelt();
            belt1.setId(sharedId);

            AsteroidBelt belt2 = new AsteroidBelt();
            belt2.setId(sharedId);

            assertEquals(belt1, belt2);
            assertEquals(belt1.hashCode(), belt2.hashCode());
        }

        @Test
        @DisplayName("should not be equal when IDs differ")
        void shouldNotBeEqualWhenIdsDiffer() {
            AsteroidBelt belt1 = new AsteroidBelt();
            belt1.setId(UUID.randomUUID());

            AsteroidBelt belt2 = new AsteroidBelt();
            belt2.setId(UUID.randomUUID());

            assertNotEquals(belt1, belt2);
        }

        @Test
        @DisplayName("should work in HashSet")
        void shouldWorkInHashSet() {
            UUID sharedId = UUID.randomUUID();

            AsteroidBelt belt1 = new AsteroidBelt();
            belt1.setId(sharedId);

            AsteroidBelt belt2 = new AsteroidBelt();
            belt2.setId(sharedId);

            AsteroidBelt belt3 = new AsteroidBelt();
            belt3.setId(UUID.randomUUID());

            Set<AsteroidBelt> set = new HashSet<>();
            set.add(belt1);
            set.add(belt2);
            set.add(belt3);

            assertEquals(2, set.size());
        }
    }

    @Nested
    @DisplayName("GraphColorsPersist")
    class GraphColorsPersistTests {

        @Test
        @DisplayName("should be equal when IDs match")
        void shouldBeEqualWhenIdsMatch() {
            GraphColorsPersist colors1 = new GraphColorsPersist();
            colors1.setId("colors-123");

            GraphColorsPersist colors2 = new GraphColorsPersist();
            colors2.setId("colors-123");

            assertEquals(colors1, colors2);
            assertEquals(colors1.hashCode(), colors2.hashCode());
        }

        @Test
        @DisplayName("should not be equal when IDs differ")
        void shouldNotBeEqualWhenIdsDiffer() {
            GraphColorsPersist colors1 = new GraphColorsPersist();
            colors1.setId("colors-123");

            GraphColorsPersist colors2 = new GraphColorsPersist();
            colors2.setId("colors-456");

            assertNotEquals(colors1, colors2);
        }

        @Test
        @DisplayName("should work in HashSet")
        void shouldWorkInHashSet() {
            GraphColorsPersist colors1 = new GraphColorsPersist();
            colors1.setId("colors-123");

            GraphColorsPersist colors2 = new GraphColorsPersist();
            colors2.setId("colors-123");

            Set<GraphColorsPersist> set = new HashSet<>();
            set.add(colors1);
            set.add(colors2);

            assertEquals(1, set.size());
        }
    }

    @Nested
    @DisplayName("GraphEnablesPersist")
    class GraphEnablesPersistTests {

        @Test
        @DisplayName("should be equal when IDs match")
        void shouldBeEqualWhenIdsMatch() {
            GraphEnablesPersist enables1 = new GraphEnablesPersist();
            enables1.setId("enables-123");

            GraphEnablesPersist enables2 = new GraphEnablesPersist();
            enables2.setId("enables-123");

            assertEquals(enables1, enables2);
            assertEquals(enables1.hashCode(), enables2.hashCode());
        }

        @Test
        @DisplayName("should not be equal when IDs differ")
        void shouldNotBeEqualWhenIdsDiffer() {
            GraphEnablesPersist enables1 = new GraphEnablesPersist();
            enables1.setId("enables-123");

            GraphEnablesPersist enables2 = new GraphEnablesPersist();
            enables2.setId("enables-456");

            assertNotEquals(enables1, enables2);
        }

        @Test
        @DisplayName("should work in HashSet")
        void shouldWorkInHashSet() {
            GraphEnablesPersist enables1 = new GraphEnablesPersist();
            enables1.setId("enables-123");

            GraphEnablesPersist enables2 = new GraphEnablesPersist();
            enables2.setId("enables-123");

            Set<GraphEnablesPersist> set = new HashSet<>();
            set.add(enables1);
            set.add(enables2);

            assertEquals(1, set.size());
        }
    }

    @Nested
    @DisplayName("StarDetailsPersist")
    class StarDetailsPersistTests {

        @Test
        @DisplayName("should be equal when IDs match")
        void shouldBeEqualWhenIdsMatch() {
            StarDetailsPersist details1 = new StarDetailsPersist();
            details1.setId("details-123");

            StarDetailsPersist details2 = new StarDetailsPersist();
            details2.setId("details-123");

            assertEquals(details1, details2);
            assertEquals(details1.hashCode(), details2.hashCode());
        }

        @Test
        @DisplayName("should not be equal when IDs differ")
        void shouldNotBeEqualWhenIdsDiffer() {
            StarDetailsPersist details1 = new StarDetailsPersist();
            details1.setId("details-123");

            StarDetailsPersist details2 = new StarDetailsPersist();
            details2.setId("details-456");

            assertNotEquals(details1, details2);
        }

        @Test
        @DisplayName("should work in HashSet")
        void shouldWorkInHashSet() {
            StarDetailsPersist details1 = new StarDetailsPersist();
            details1.setId("details-123");

            StarDetailsPersist details2 = new StarDetailsPersist();
            details2.setId("details-123");

            Set<StarDetailsPersist> set = new HashSet<>();
            set.add(details1);
            set.add(details2);

            assertEquals(1, set.size());
        }
    }

    @Nested
    @DisplayName("CivilizationDisplayPreferences")
    class CivilizationDisplayPreferencesTests {

        @Test
        @DisplayName("should be equal when IDs match")
        void shouldBeEqualWhenIdsMatch() {
            CivilizationDisplayPreferences prefs1 = new CivilizationDisplayPreferences();
            String sharedId = prefs1.getId();

            CivilizationDisplayPreferences prefs2 = new CivilizationDisplayPreferences();
            prefs2.setId(sharedId);

            assertEquals(prefs1, prefs2);
            assertEquals(prefs1.hashCode(), prefs2.hashCode());
        }

        @Test
        @DisplayName("should not be equal when IDs differ")
        void shouldNotBeEqualWhenIdsDiffer() {
            CivilizationDisplayPreferences prefs1 = new CivilizationDisplayPreferences();
            CivilizationDisplayPreferences prefs2 = new CivilizationDisplayPreferences();

            // Each gets a unique UUID in constructor
            assertNotEquals(prefs1, prefs2);
        }

        @Test
        @DisplayName("should work in HashSet")
        void shouldWorkInHashSet() {
            CivilizationDisplayPreferences prefs1 = new CivilizationDisplayPreferences();
            String sharedId = prefs1.getId();

            CivilizationDisplayPreferences prefs2 = new CivilizationDisplayPreferences();
            prefs2.setId(sharedId);

            CivilizationDisplayPreferences prefs3 = new CivilizationDisplayPreferences();

            Set<CivilizationDisplayPreferences> set = new HashSet<>();
            set.add(prefs1);
            set.add(prefs2);
            set.add(prefs3);

            assertEquals(2, set.size());
        }
    }

    @Nested
    @DisplayName("DataSetDescriptor")
    class DataSetDescriptorTests {

        @Test
        @DisplayName("should be equal when dataSetNames match")
        void shouldBeEqualWhenNamesMatch() {
            DataSetDescriptor desc1 = new DataSetDescriptor();
            desc1.setDataSetName("TestDataset");

            DataSetDescriptor desc2 = new DataSetDescriptor();
            desc2.setDataSetName("TestDataset");

            assertEquals(desc1, desc2);
            assertEquals(desc1.hashCode(), desc2.hashCode());
        }

        @Test
        @DisplayName("should not be equal when dataSetNames differ")
        void shouldNotBeEqualWhenNamesDiffer() {
            DataSetDescriptor desc1 = new DataSetDescriptor();
            desc1.setDataSetName("Dataset1");

            DataSetDescriptor desc2 = new DataSetDescriptor();
            desc2.setDataSetName("Dataset2");

            assertNotEquals(desc1, desc2);
        }

        @Test
        @DisplayName("should work in HashSet")
        void shouldWorkInHashSet() {
            DataSetDescriptor desc1 = new DataSetDescriptor();
            desc1.setDataSetName("TestDataset");

            DataSetDescriptor desc2 = new DataSetDescriptor();
            desc2.setDataSetName("TestDataset");

            DataSetDescriptor desc3 = new DataSetDescriptor();
            desc3.setDataSetName("OtherDataset");

            Set<DataSetDescriptor> set = new HashSet<>();
            set.add(desc1);
            set.add(desc2);
            set.add(desc3);

            assertEquals(2, set.size());
        }
    }

    @Nested
    @DisplayName("Null ID Handling")
    class NullIdHandlingTests {

        @Test
        @DisplayName("TripsPrefs with null IDs should not be equal")
        void tripsPrefsNullIds() {
            TripsPrefs prefs1 = new TripsPrefs();
            prefs1.setId(null);

            TripsPrefs prefs2 = new TripsPrefs();
            prefs2.setId(null);

            assertNotEquals(prefs1, prefs2);
        }

        @Test
        @DisplayName("TransitSettings with null IDs should not be equal")
        void transitSettingsNullIds() {
            TransitSettings settings1 = new TransitSettings();
            settings1.setId(null);

            TransitSettings settings2 = new TransitSettings();
            settings2.setId(null);

            assertNotEquals(settings1, settings2);
        }

        @Test
        @DisplayName("AsteroidBelt with null IDs should not be equal")
        void asteroidBeltNullIds() {
            AsteroidBelt belt1 = new AsteroidBelt();
            belt1.setId(null);

            AsteroidBelt belt2 = new AsteroidBelt();
            belt2.setId(null);

            assertNotEquals(belt1, belt2);
        }

        @Test
        @DisplayName("GraphColorsPersist with null IDs should not be equal")
        void graphColorsPersistNullIds() {
            GraphColorsPersist colors1 = new GraphColorsPersist();
            colors1.setId(null);

            GraphColorsPersist colors2 = new GraphColorsPersist();
            colors2.setId(null);

            assertNotEquals(colors1, colors2);
        }

        @Test
        @DisplayName("GraphEnablesPersist with null IDs should not be equal")
        void graphEnablesPersistNullIds() {
            GraphEnablesPersist enables1 = new GraphEnablesPersist();
            enables1.setId(null);

            GraphEnablesPersist enables2 = new GraphEnablesPersist();
            enables2.setId(null);

            assertNotEquals(enables1, enables2);
        }

        @Test
        @DisplayName("StarDetailsPersist with null IDs should not be equal")
        void starDetailsPersistNullIds() {
            StarDetailsPersist details1 = new StarDetailsPersist();
            details1.setId(null);

            StarDetailsPersist details2 = new StarDetailsPersist();
            details2.setId(null);

            assertNotEquals(details1, details2);
        }
    }
}
