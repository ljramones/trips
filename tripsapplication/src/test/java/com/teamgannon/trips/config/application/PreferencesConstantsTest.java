package com.teamgannon.trips.config.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link PreferencesConstants}.
 */
class PreferencesConstantsTest {

    @Nested
    @DisplayName("Constants Values Tests")
    class ConstantsValuesTests {

        @Test
        @DisplayName("MAIN_PREFS_ID should be 'main'")
        void mainPrefsIdShouldBeMain() {
            assertEquals("main", PreferencesConstants.MAIN_PREFS_ID);
        }

        @Test
        @DisplayName("CIVILIZATION_STORAGE_TAG should be 'Main'")
        void civilizationStorageTagShouldBeMain() {
            assertEquals("Main", PreferencesConstants.CIVILIZATION_STORAGE_TAG);
        }

        @Test
        @DisplayName("DEFAULT_VISIBLE_LABELS should be 30")
        void defaultVisibleLabelsShouldBe30() {
            assertEquals(30, PreferencesConstants.DEFAULT_VISIBLE_LABELS);
        }

        @Test
        @DisplayName("DEFAULT_GRID_LINE_WIDTH should be 0.5")
        void defaultGridLineWidthShouldBe0Point5() {
            assertEquals(0.5, PreferencesConstants.DEFAULT_GRID_LINE_WIDTH, 0.001);
        }

        @Test
        @DisplayName("DEFAULT_STEM_LINE_WIDTH should be 0.5")
        void defaultStemLineWidthShouldBe0Point5() {
            assertEquals(0.5, PreferencesConstants.DEFAULT_STEM_LINE_WIDTH, 0.001);
        }

        @Test
        @DisplayName("DEFAULT_ROUTE_LINE_WIDTH should be 2.0")
        void defaultRouteLineWidthShouldBe2Point0() {
            assertEquals(2.0, PreferencesConstants.DEFAULT_ROUTE_LINE_WIDTH, 0.001);
        }
    }

    @Nested
    @DisplayName("Constants Design Tests")
    class ConstantsDesignTests {

        @Test
        @DisplayName("Class should be final")
        void classShouldBeFinal() {
            assertTrue(Modifier.isFinal(PreferencesConstants.class.getModifiers()),
                    "PreferencesConstants should be a final class");
        }

        @Test
        @DisplayName("Constructor should be private")
        void constructorShouldBePrivate() throws NoSuchMethodException {
            Constructor<PreferencesConstants> constructor =
                    PreferencesConstants.class.getDeclaredConstructor();
            assertTrue(Modifier.isPrivate(constructor.getModifiers()),
                    "Constructor should be private to prevent instantiation");
        }

        @Test
        @DisplayName("Constants should be static final")
        void constantsShouldBeStaticFinal() throws NoSuchFieldException {
            int mainPrefsModifiers = PreferencesConstants.class
                    .getDeclaredField("MAIN_PREFS_ID").getModifiers();
            assertTrue(Modifier.isStatic(mainPrefsModifiers) && Modifier.isFinal(mainPrefsModifiers),
                    "MAIN_PREFS_ID should be static final");

            int civilizationModifiers = PreferencesConstants.class
                    .getDeclaredField("CIVILIZATION_STORAGE_TAG").getModifiers();
            assertTrue(Modifier.isStatic(civilizationModifiers) && Modifier.isFinal(civilizationModifiers),
                    "CIVILIZATION_STORAGE_TAG should be static final");

            int visibleLabelsModifiers = PreferencesConstants.class
                    .getDeclaredField("DEFAULT_VISIBLE_LABELS").getModifiers();
            assertTrue(Modifier.isStatic(visibleLabelsModifiers) && Modifier.isFinal(visibleLabelsModifiers),
                    "DEFAULT_VISIBLE_LABELS should be static final");
        }
    }

    @Nested
    @DisplayName("Constants Validity Tests")
    class ConstantsValidityTests {

        @Test
        @DisplayName("MAIN_PREFS_ID should not be null or empty")
        void mainPrefsIdShouldNotBeNullOrEmpty() {
            assertNotNull(PreferencesConstants.MAIN_PREFS_ID);
            assertFalse(PreferencesConstants.MAIN_PREFS_ID.isEmpty());
        }

        @Test
        @DisplayName("CIVILIZATION_STORAGE_TAG should not be null or empty")
        void civilizationStorageTagShouldNotBeNullOrEmpty() {
            assertNotNull(PreferencesConstants.CIVILIZATION_STORAGE_TAG);
            assertFalse(PreferencesConstants.CIVILIZATION_STORAGE_TAG.isEmpty());
        }

        @Test
        @DisplayName("DEFAULT_VISIBLE_LABELS should be positive")
        void defaultVisibleLabelsShouldBePositive() {
            assertTrue(PreferencesConstants.DEFAULT_VISIBLE_LABELS > 0,
                    "Default visible labels must be positive");
        }

        @Test
        @DisplayName("Line width defaults should be positive")
        void lineWidthDefaultsShouldBePositive() {
            assertTrue(PreferencesConstants.DEFAULT_GRID_LINE_WIDTH > 0,
                    "Default grid line width must be positive");
            assertTrue(PreferencesConstants.DEFAULT_STEM_LINE_WIDTH > 0,
                    "Default stem line width must be positive");
            assertTrue(PreferencesConstants.DEFAULT_ROUTE_LINE_WIDTH > 0,
                    "Default route line width must be positive");
        }
    }
}
