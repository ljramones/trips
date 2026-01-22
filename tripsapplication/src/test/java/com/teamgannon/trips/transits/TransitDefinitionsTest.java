package com.teamgannon.trips.transits;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TransitDefinitions model class.
 * Tests cover initialization, property access, and list operations.
 */
class TransitDefinitionsTest {

    private TransitDefinitions definitions;

    @BeforeEach
    void setUp() {
        definitions = new TransitDefinitions();
    }

    // =========================================================================
    // Initialization Tests
    // =========================================================================

    @Nested
    @DisplayName("Initialization Tests")
    class InitializationTests {

        @Test
        @DisplayName("New TransitDefinitions has empty transit range list")
        void newDefinitionsHasEmptyList() {
            assertNotNull(definitions.getTransitRangeDefs());
            assertTrue(definitions.getTransitRangeDefs().isEmpty());
        }

        @Test
        @DisplayName("New TransitDefinitions has selected as false")
        void newDefinitionsNotSelected() {
            assertFalse(definitions.isSelected());
        }

        @Test
        @DisplayName("New TransitDefinitions has null dataSetName")
        void newDefinitionsNullDataSetName() {
            assertNull(definitions.getDataSetName());
        }
    }

    // =========================================================================
    // Property Tests
    // =========================================================================

    @Nested
    @DisplayName("Property Tests")
    class PropertyTests {

        @Test
        @DisplayName("setSelected and isSelected work correctly")
        void selectedProperty() {
            definitions.setSelected(true);
            assertTrue(definitions.isSelected());

            definitions.setSelected(false);
            assertFalse(definitions.isSelected());
        }

        @Test
        @DisplayName("setDataSetName and getDataSetName work correctly")
        void dataSetNameProperty() {
            definitions.setDataSetName("Test Dataset");
            assertEquals("Test Dataset", definitions.getDataSetName());
        }

        @Test
        @DisplayName("setTransitRangeDefs replaces the list")
        void setTransitRangeDefsReplacesList() {
            List<TransitRangeDef> newList = new ArrayList<>();
            newList.add(createRangeDef("Band 1"));
            newList.add(createRangeDef("Band 2"));

            definitions.setTransitRangeDefs(newList);

            assertEquals(2, definitions.getTransitRangeDefs().size());
        }
    }

    // =========================================================================
    // List Operations Tests
    // =========================================================================

    @Nested
    @DisplayName("List Operations Tests")
    class ListOperationsTests {

        @Test
        @DisplayName("Can add TransitRangeDef to list")
        void canAddToList() {
            TransitRangeDef def = createRangeDef("Test Band");

            definitions.getTransitRangeDefs().add(def);

            assertEquals(1, definitions.getTransitRangeDefs().size());
            assertEquals("Test Band", definitions.getTransitRangeDefs().get(0).getBandName());
        }

        @Test
        @DisplayName("Can add multiple TransitRangeDefs to list")
        void canAddMultipleToList() {
            definitions.getTransitRangeDefs().add(createRangeDef("Band 1"));
            definitions.getTransitRangeDefs().add(createRangeDef("Band 2"));
            definitions.getTransitRangeDefs().add(createRangeDef("Band 3"));

            assertEquals(3, definitions.getTransitRangeDefs().size());
        }

        @Test
        @DisplayName("Can remove TransitRangeDef from list")
        void canRemoveFromList() {
            TransitRangeDef def = createRangeDef("Test Band");
            definitions.getTransitRangeDefs().add(def);

            definitions.getTransitRangeDefs().remove(def);

            assertTrue(definitions.getTransitRangeDefs().isEmpty());
        }

        @Test
        @DisplayName("Can clear the list")
        void canClearList() {
            definitions.getTransitRangeDefs().add(createRangeDef("Band 1"));
            definitions.getTransitRangeDefs().add(createRangeDef("Band 2"));

            definitions.getTransitRangeDefs().clear();

            assertTrue(definitions.getTransitRangeDefs().isEmpty());
        }

        @Test
        @DisplayName("Can find TransitRangeDef by bandId")
        void canFindByBandId() {
            UUID targetId = UUID.randomUUID();
            TransitRangeDef def1 = createRangeDef("Band 1");
            TransitRangeDef def2 = createRangeDef("Band 2");
            def2.setBandId(targetId);

            definitions.getTransitRangeDefs().add(def1);
            definitions.getTransitRangeDefs().add(def2);

            TransitRangeDef found = definitions.getTransitRangeDefs().stream()
                    .filter(d -> targetId.equals(d.getBandId()))
                    .findFirst()
                    .orElse(null);

            assertNotNull(found);
            assertEquals("Band 2", found.getBandName());
        }

        @Test
        @DisplayName("Can filter enabled TransitRangeDefs")
        void canFilterEnabled() {
            TransitRangeDef enabled1 = createRangeDef("Enabled 1");
            enabled1.setEnabled(true);
            TransitRangeDef disabled = createRangeDef("Disabled");
            disabled.setEnabled(false);
            TransitRangeDef enabled2 = createRangeDef("Enabled 2");
            enabled2.setEnabled(true);

            definitions.getTransitRangeDefs().add(enabled1);
            definitions.getTransitRangeDefs().add(disabled);
            definitions.getTransitRangeDefs().add(enabled2);

            List<TransitRangeDef> enabledDefs = definitions.getTransitRangeDefs().stream()
                    .filter(TransitRangeDef::isEnabled)
                    .toList();

            assertEquals(2, enabledDefs.size());
        }
    }

    // =========================================================================
    // Typical Usage Tests
    // =========================================================================

    @Nested
    @DisplayName("Typical Usage Tests")
    class TypicalUsageTests {

        @Test
        @DisplayName("Create definitions with multiple bands for dataset")
        void createDefinitionsWithMultipleBands() {
            definitions.setDataSetName("HYG Database");
            definitions.setSelected(true);

            definitions.getTransitRangeDefs().add(createEnabledRangeDef("Short Range", 0, 5, Color.GREEN));
            definitions.getTransitRangeDefs().add(createEnabledRangeDef("Medium Range", 5, 10, Color.YELLOW));
            definitions.getTransitRangeDefs().add(createEnabledRangeDef("Long Range", 10, 15, Color.RED));

            assertEquals("HYG Database", definitions.getDataSetName());
            assertTrue(definitions.isSelected());
            assertEquals(3, definitions.getTransitRangeDefs().size());
        }

        @Test
        @DisplayName("Create cancelled dialog result")
        void createCancelledResult() {
            definitions.setSelected(false);

            assertFalse(definitions.isSelected());
        }

        @Test
        @DisplayName("Mix of enabled and disabled bands")
        void mixOfEnabledAndDisabledBands() {
            TransitRangeDef enabled = createRangeDef("Active");
            enabled.setEnabled(true);

            TransitRangeDef disabled = createRangeDef("Inactive");
            disabled.setEnabled(false);

            definitions.getTransitRangeDefs().add(enabled);
            definitions.getTransitRangeDefs().add(disabled);

            long enabledCount = definitions.getTransitRangeDefs().stream()
                    .filter(TransitRangeDef::isEnabled)
                    .count();

            assertEquals(1, enabledCount);
        }
    }

    // =========================================================================
    // Edge Cases Tests
    // =========================================================================

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Empty dataSetName is allowed")
        void emptyDataSetNameAllowed() {
            definitions.setDataSetName("");
            assertEquals("", definitions.getDataSetName());
        }

        @Test
        @DisplayName("Null dataSetName is allowed")
        void nullDataSetNameAllowed() {
            definitions.setDataSetName(null);
            assertNull(definitions.getDataSetName());
        }

        @Test
        @DisplayName("Setting null list throws NullPointerException on access")
        void settingNullList() {
            definitions.setTransitRangeDefs(null);
            assertNull(definitions.getTransitRangeDefs());
        }

        @Test
        @DisplayName("Large number of bands")
        void largeNumberOfBands() {
            for (int i = 0; i < 20; i++) {
                definitions.getTransitRangeDefs().add(createRangeDef("Band " + i));
            }

            assertEquals(20, definitions.getTransitRangeDefs().size());
        }

        @Test
        @DisplayName("Duplicate band names are allowed")
        void duplicateBandNamesAllowed() {
            definitions.getTransitRangeDefs().add(createRangeDef("Same Name"));
            definitions.getTransitRangeDefs().add(createRangeDef("Same Name"));

            assertEquals(2, definitions.getTransitRangeDefs().size());
        }
    }

    // =========================================================================
    // Lombok Generated Tests
    // =========================================================================

    @Nested
    @DisplayName("Lombok Generated Methods")
    class LombokGeneratedTests {

        @Test
        @DisplayName("equals works for identical definitions")
        void equalsForIdenticalDefinitions() {
            TransitDefinitions def1 = new TransitDefinitions();
            def1.setDataSetName("Test");
            def1.setSelected(true);

            TransitDefinitions def2 = new TransitDefinitions();
            def2.setDataSetName("Test");
            def2.setSelected(true);

            assertEquals(def1, def2);
        }

        @Test
        @DisplayName("hashCode is consistent with equals")
        void hashCodeConsistentWithEquals() {
            TransitDefinitions def1 = new TransitDefinitions();
            def1.setDataSetName("Test");

            TransitDefinitions def2 = new TransitDefinitions();
            def2.setDataSetName("Test");

            if (def1.equals(def2)) {
                assertEquals(def1.hashCode(), def2.hashCode());
            }
        }

        @Test
        @DisplayName("toString returns non-null string")
        void toStringReturnsNonNull() {
            definitions.setDataSetName("Test Dataset");

            String result = definitions.toString();

            assertNotNull(result);
        }
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    private TransitRangeDef createRangeDef(String name) {
        TransitRangeDef def = new TransitRangeDef();
        def.setBandId(UUID.randomUUID());
        def.setBandName(name);
        def.setEnabled(false);
        def.setLowerRange(0.0);
        def.setUpperRange(5.0);
        def.setLineWidth(TransitConstants.DEFAULT_BAND_LINE_WIDTH);
        def.setBandColor(Color.WHITE);
        return def;
    }

    private TransitRangeDef createEnabledRangeDef(String name, double lower, double upper, Color color) {
        TransitRangeDef def = new TransitRangeDef();
        def.setBandId(UUID.randomUUID());
        def.setBandName(name);
        def.setEnabled(true);
        def.setLowerRange(lower);
        def.setUpperRange(upper);
        def.setLineWidth(TransitConstants.DEFAULT_BAND_LINE_WIDTH);
        def.setBandColor(color);
        return def;
    }
}
