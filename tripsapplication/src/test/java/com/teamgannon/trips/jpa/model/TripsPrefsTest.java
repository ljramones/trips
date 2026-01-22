package com.teamgannon.trips.jpa.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TripsPrefs entity.
 */
class TripsPrefsTest {

    @Test
    void testDefaultValues() {
        TripsPrefs prefs = new TripsPrefs();

        assertNull(prefs.getId());
        assertNull(prefs.getDatasetName());
        assertFalse(prefs.isSkipStartupDialog(), "skipStartupDialog should default to false");
    }

    @Test
    void testSetAndGetId() {
        TripsPrefs prefs = new TripsPrefs();
        prefs.setId("main");

        assertEquals("main", prefs.getId());
    }

    @Test
    void testSetAndGetDatasetName() {
        TripsPrefs prefs = new TripsPrefs();
        prefs.setDatasetName("TestDataset");

        assertEquals("TestDataset", prefs.getDatasetName());
    }

    @Test
    void testSetAndGetSkipStartupDialog() {
        TripsPrefs prefs = new TripsPrefs();

        // Default is false (show dialog)
        assertFalse(prefs.isSkipStartupDialog());

        // User selects "Don't show again" -> set to true (skip dialog)
        prefs.setSkipStartupDialog(true);
        assertTrue(prefs.isSkipStartupDialog());

        // Can be reset to show dialog again
        prefs.setSkipStartupDialog(false);
        assertFalse(prefs.isSkipStartupDialog());
    }

    @Test
    void testSkipStartupDialogSemantics() {
        TripsPrefs prefs = new TripsPrefs();

        // When skipStartupDialog is false, we SHOW the dialog
        prefs.setSkipStartupDialog(false);
        boolean shouldShowDialog = !prefs.isSkipStartupDialog();
        assertTrue(shouldShowDialog, "Dialog should be shown when skipStartupDialog is false");

        // When skipStartupDialog is true, we SKIP the dialog
        prefs.setSkipStartupDialog(true);
        shouldShowDialog = !prefs.isSkipStartupDialog();
        assertFalse(shouldShowDialog, "Dialog should be skipped when skipStartupDialog is true");
    }

    @Test
    void testEqualsWithSameId() {
        TripsPrefs prefs1 = new TripsPrefs();
        prefs1.setId("main");
        prefs1.setDatasetName("Dataset1");

        TripsPrefs prefs2 = new TripsPrefs();
        prefs2.setId("main");
        prefs2.setDatasetName("Dataset2"); // Different dataset name

        // Entities with same ID should be equal (JPA semantics)
        assertEquals(prefs1, prefs2);
    }

    @Test
    void testEqualsWithDifferentId() {
        TripsPrefs prefs1 = new TripsPrefs();
        prefs1.setId("main1");

        TripsPrefs prefs2 = new TripsPrefs();
        prefs2.setId("main2");

        assertNotEquals(prefs1, prefs2);
    }

    @Test
    void testEqualsWithNullId() {
        TripsPrefs prefs1 = new TripsPrefs();
        prefs1.setId(null);

        TripsPrefs prefs2 = new TripsPrefs();
        prefs2.setId(null);

        // Two entities with null IDs are not equal to each other
        assertNotEquals(prefs1, prefs2);
    }

    @Test
    void testHashCodeConsistency() {
        TripsPrefs prefs = new TripsPrefs();
        prefs.setId("main");

        int hashCode1 = prefs.hashCode();
        int hashCode2 = prefs.hashCode();

        assertEquals(hashCode1, hashCode2, "hashCode should be consistent");
    }

    @Test
    void testToString() {
        TripsPrefs prefs = new TripsPrefs();
        prefs.setId("main");
        prefs.setDatasetName("TestDataset");
        prefs.setSkipStartupDialog(true);

        String toString = prefs.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("main") || toString.contains("id"),
                "toString should contain id information");
    }
}
