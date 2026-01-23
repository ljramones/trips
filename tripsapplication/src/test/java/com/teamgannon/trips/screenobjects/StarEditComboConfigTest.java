package com.teamgannon.trips.screenobjects;

import com.teamgannon.trips.jpa.model.CivilizationDisplayPreferences;
import javafx.application.Platform;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for StarEditComboConfig.
 */
class StarEditComboConfigTest {

    private static boolean javaFxInitialized = false;

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> {});
            javaFxInitialized = true;
        } catch (IllegalStateException e) {
            // Already initialized
            javaFxInitialized = true;
        } catch (Exception e) {
            System.out.println("JavaFX not available, some tests will be skipped: " + e.getMessage());
            javaFxInitialized = false;
        }
    }

    @Nested
    @DisplayName("Static list tests")
    class StaticListTests {

        @Test
        @DisplayName("POLITIES should contain all civilization types")
        void politiesShouldContainAllCivilizationTypes() {
            assertTrue(StarEditComboConfig.POLITIES.contains(CivilizationDisplayPreferences.TERRAN));
            assertTrue(StarEditComboConfig.POLITIES.contains(CivilizationDisplayPreferences.DORNANI));
            assertTrue(StarEditComboConfig.POLITIES.contains(CivilizationDisplayPreferences.KTOR));
            assertTrue(StarEditComboConfig.POLITIES.contains(CivilizationDisplayPreferences.ARAKUR));
            assertTrue(StarEditComboConfig.POLITIES.contains("NA"));
        }

        @Test
        @DisplayName("POLITIES should have 11 entries")
        void politiesShouldHave11Entries() {
            assertEquals(11, StarEditComboConfig.POLITIES.size());
        }

        @Test
        @DisplayName("FUEL_TYPES should contain expected values")
        void fuelTypesShouldContainExpectedValues() {
            assertTrue(StarEditComboConfig.FUEL_TYPES.contains("H2"));
            assertTrue(StarEditComboConfig.FUEL_TYPES.contains("Antimatter"));
            assertTrue(StarEditComboConfig.FUEL_TYPES.contains("Gas Giant"));
            assertTrue(StarEditComboConfig.FUEL_TYPES.contains("Water World"));
            assertTrue(StarEditComboConfig.FUEL_TYPES.contains("NA"));
            assertEquals(5, StarEditComboConfig.FUEL_TYPES.size());
        }

        @Test
        @DisplayName("WORLD_TYPES should contain expected values")
        void worldTypesShouldContainExpectedValues() {
            assertTrue(StarEditComboConfig.WORLD_TYPES.contains("Green"));
            assertTrue(StarEditComboConfig.WORLD_TYPES.contains("Grey"));
            assertTrue(StarEditComboConfig.WORLD_TYPES.contains("Brown"));
            assertTrue(StarEditComboConfig.WORLD_TYPES.contains("NA"));
            assertEquals(4, StarEditComboConfig.WORLD_TYPES.size());
        }

        @Test
        @DisplayName("PORT_TYPES should contain A through E and NA")
        void portTypesShouldContainAToEAndNA() {
            assertTrue(StarEditComboConfig.PORT_TYPES.contains("A"));
            assertTrue(StarEditComboConfig.PORT_TYPES.contains("B"));
            assertTrue(StarEditComboConfig.PORT_TYPES.contains("C"));
            assertTrue(StarEditComboConfig.PORT_TYPES.contains("D"));
            assertTrue(StarEditComboConfig.PORT_TYPES.contains("E"));
            assertTrue(StarEditComboConfig.PORT_TYPES.contains("NA"));
            assertEquals(6, StarEditComboConfig.PORT_TYPES.size());
        }

        @Test
        @DisplayName("TECH_TYPES should contain 1-9, A-E, and NA")
        void techTypesShouldContainAllTechLevels() {
            assertTrue(StarEditComboConfig.TECH_TYPES.contains("1"));
            assertTrue(StarEditComboConfig.TECH_TYPES.contains("9"));
            assertTrue(StarEditComboConfig.TECH_TYPES.contains("A"));
            assertTrue(StarEditComboConfig.TECH_TYPES.contains("E"));
            assertTrue(StarEditComboConfig.TECH_TYPES.contains("NA"));
            assertEquals(15, StarEditComboConfig.TECH_TYPES.size());
        }

        @Test
        @DisplayName("POPULATION_TYPES should contain population ranges")
        void populationTypesShouldContainPopulationRanges() {
            assertTrue(StarEditComboConfig.POPULATION_TYPES.contains("1s"));
            assertTrue(StarEditComboConfig.POPULATION_TYPES.contains("10s"));
            assertTrue(StarEditComboConfig.POPULATION_TYPES.contains("1000s"));
            assertTrue(StarEditComboConfig.POPULATION_TYPES.contains("NA"));
            // Should contain unicode superscript numbers like 10⁴
            assertTrue(StarEditComboConfig.POPULATION_TYPES.stream()
                    .anyMatch(p -> p.contains("\u2074"))); // ⁴
            assertEquals(11, StarEditComboConfig.POPULATION_TYPES.size());
        }

        @Test
        @DisplayName("PRODUCT_TYPES should contain industry types")
        void productTypesShouldContainIndustryTypes() {
            assertTrue(StarEditComboConfig.PRODUCT_TYPES.contains("Agricultural"));
            assertTrue(StarEditComboConfig.PRODUCT_TYPES.contains("Industry"));
            assertTrue(StarEditComboConfig.PRODUCT_TYPES.contains("Services"));
            assertTrue(StarEditComboConfig.PRODUCT_TYPES.contains("Hi Tech"));
            assertTrue(StarEditComboConfig.PRODUCT_TYPES.contains("NA"));
            assertEquals(11, StarEditComboConfig.PRODUCT_TYPES.size());
        }

        @Test
        @DisplayName("MIL_SPACE_TYPES should equal PORT_TYPES structure")
        void milSpaceTypesShouldEqualPortTypesStructure() {
            assertEquals(StarEditComboConfig.PORT_TYPES.size(), StarEditComboConfig.MIL_SPACE_TYPES.size());
            assertTrue(StarEditComboConfig.MIL_SPACE_TYPES.contains("A"));
            assertTrue(StarEditComboConfig.MIL_SPACE_TYPES.contains("NA"));
        }

        @Test
        @DisplayName("MIL_PLAN_TYPES should equal PORT_TYPES structure")
        void milPlanTypesShouldEqualPortTypesStructure() {
            assertEquals(StarEditComboConfig.PORT_TYPES.size(), StarEditComboConfig.MIL_PLAN_TYPES.size());
            assertTrue(StarEditComboConfig.MIL_PLAN_TYPES.contains("A"));
            assertTrue(StarEditComboConfig.MIL_PLAN_TYPES.contains("NA"));
        }

        @Test
        @DisplayName("All lists should end with NA")
        void allListsShouldEndWithNA() {
            assertEquals("NA", StarEditComboConfig.POLITIES.get(StarEditComboConfig.POLITIES.size() - 1));
            assertEquals("NA", StarEditComboConfig.FUEL_TYPES.get(StarEditComboConfig.FUEL_TYPES.size() - 1));
            assertEquals("NA", StarEditComboConfig.WORLD_TYPES.get(StarEditComboConfig.WORLD_TYPES.size() - 1));
            assertEquals("NA", StarEditComboConfig.PORT_TYPES.get(StarEditComboConfig.PORT_TYPES.size() - 1));
            assertEquals("NA", StarEditComboConfig.TECH_TYPES.get(StarEditComboConfig.TECH_TYPES.size() - 1));
            assertEquals("NA", StarEditComboConfig.POPULATION_TYPES.get(StarEditComboConfig.POPULATION_TYPES.size() - 1));
            assertEquals("NA", StarEditComboConfig.PRODUCT_TYPES.get(StarEditComboConfig.PRODUCT_TYPES.size() - 1));
        }
    }

    @Nested
    @DisplayName("setupCombo tests")
    class SetupComboTests {

        @Test
        @DisplayName("should populate combo box with items")
        void shouldPopulateComboBoxWithItems() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicReference<Integer> itemCount = new AtomicReference<>();

            runOnFxThread(() -> {
                ComboBox<String> combo = new ComboBox<>();
                TextField field = new TextField();
                StarEditComboConfig.setupCombo(combo, StarEditComboConfig.FUEL_TYPES, field, null);
                itemCount.set(combo.getItems().size());
            });

            assertEquals(5, itemCount.get());
        }

        @Test
        @DisplayName("should select NA when current value is null")
        void shouldSelectNAWhenCurrentValueIsNull() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicReference<String> selected = new AtomicReference<>();

            runOnFxThread(() -> {
                ComboBox<String> combo = new ComboBox<>();
                TextField field = new TextField();
                StarEditComboConfig.setupCombo(combo, StarEditComboConfig.WORLD_TYPES, field, null);
                selected.set(combo.getSelectionModel().getSelectedItem());
            });

            assertEquals("NA", selected.get());
        }

        @Test
        @DisplayName("should select current value when present in list")
        void shouldSelectCurrentValueWhenPresentInList() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicReference<String> selected = new AtomicReference<>();

            runOnFxThread(() -> {
                ComboBox<String> combo = new ComboBox<>();
                TextField field = new TextField();
                StarEditComboConfig.setupCombo(combo, StarEditComboConfig.WORLD_TYPES, field, "Green");
                selected.set(combo.getSelectionModel().getSelectedItem());
            });

            assertEquals("Green", selected.get());
        }

        @Test
        @DisplayName("should select NA when current value is not in list")
        void shouldSelectNAWhenCurrentValueIsNotInList() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicReference<String> selected = new AtomicReference<>();

            runOnFxThread(() -> {
                ComboBox<String> combo = new ComboBox<>();
                TextField field = new TextField();
                StarEditComboConfig.setupCombo(combo, StarEditComboConfig.WORLD_TYPES, field, "Unknown Type");
                selected.set(combo.getSelectionModel().getSelectedItem());
            });

            assertEquals("NA", selected.get());
        }

        @Test
        @DisplayName("should update text field when selection changes")
        void shouldUpdateTextFieldWhenSelectionChanges() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicReference<String> fieldText = new AtomicReference<>();

            runOnFxThread(() -> {
                ComboBox<String> combo = new ComboBox<>();
                TextField field = new TextField();
                StarEditComboConfig.setupCombo(combo, StarEditComboConfig.FUEL_TYPES, field, null);

                // Change selection
                combo.getSelectionModel().select("Antimatter");
                fieldText.set(field.getText());
            });

            assertEquals("Antimatter", fieldText.get());
        }

        @Test
        @DisplayName("should handle blank current value as NA")
        void shouldHandleBlankCurrentValueAsNA() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicReference<String> selected = new AtomicReference<>();

            runOnFxThread(() -> {
                ComboBox<String> combo = new ComboBox<>();
                TextField field = new TextField();
                StarEditComboConfig.setupCombo(combo, StarEditComboConfig.PORT_TYPES, field, "   ");
                selected.set(combo.getSelectionModel().getSelectedItem());
            });

            assertEquals("NA", selected.get());
        }
    }

    @Nested
    @DisplayName("setupAllCombos tests")
    class SetupAllCombosTests {

        @Test
        @DisplayName("should setup all 9 combo boxes")
        void shouldSetupAll9ComboBoxes() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicReference<Boolean> allPopulated = new AtomicReference<>(false);

            runOnFxThread(() -> {
                ComboBox<String> polities = new ComboBox<>();
                ComboBox<String> worlds = new ComboBox<>();
                ComboBox<String> fuel = new ComboBox<>();
                ComboBox<String> tech = new ComboBox<>();
                ComboBox<String> port = new ComboBox<>();
                ComboBox<String> population = new ComboBox<>();
                ComboBox<String> product = new ComboBox<>();
                ComboBox<String> milSpace = new ComboBox<>();
                ComboBox<String> milPlan = new ComboBox<>();

                TextField polityField = new TextField();
                TextField worldField = new TextField();
                TextField fuelField = new TextField();
                TextField techField = new TextField();
                TextField portField = new TextField();
                TextField popField = new TextField();
                TextField prodField = new TextField();
                TextField milSpaceField = new TextField();
                TextField milPlanField = new TextField();

                StarEditComboConfig.setupAllCombos(
                        polities, polityField, "Terran",
                        worlds, worldField, "Green",
                        fuel, fuelField, "H2",
                        tech, techField, "5",
                        port, portField, "A",
                        population, popField, "1000s",
                        product, prodField, "Industry",
                        milSpace, milSpaceField, "B",
                        milPlan, milPlanField, "C"
                );

                boolean populated = !polities.getItems().isEmpty() &&
                        !worlds.getItems().isEmpty() &&
                        !fuel.getItems().isEmpty() &&
                        !tech.getItems().isEmpty() &&
                        !port.getItems().isEmpty() &&
                        !population.getItems().isEmpty() &&
                        !product.getItems().isEmpty() &&
                        !milSpace.getItems().isEmpty() &&
                        !milPlan.getItems().isEmpty();

                allPopulated.set(populated);
            });

            assertTrue(allPopulated.get());
        }

        @Test
        @DisplayName("should select correct values for all combos")
        void shouldSelectCorrectValuesForAllCombos() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            AtomicReference<String> politySelected = new AtomicReference<>();
            AtomicReference<String> worldSelected = new AtomicReference<>();
            AtomicReference<String> fuelSelected = new AtomicReference<>();

            runOnFxThread(() -> {
                ComboBox<String> polities = new ComboBox<>();
                ComboBox<String> worlds = new ComboBox<>();
                ComboBox<String> fuel = new ComboBox<>();
                ComboBox<String> tech = new ComboBox<>();
                ComboBox<String> port = new ComboBox<>();
                ComboBox<String> population = new ComboBox<>();
                ComboBox<String> product = new ComboBox<>();
                ComboBox<String> milSpace = new ComboBox<>();
                ComboBox<String> milPlan = new ComboBox<>();

                TextField polityField = new TextField();
                TextField worldField = new TextField();
                TextField fuelField = new TextField();
                TextField techField = new TextField();
                TextField portField = new TextField();
                TextField popField = new TextField();
                TextField prodField = new TextField();
                TextField milSpaceField = new TextField();
                TextField milPlanField = new TextField();

                StarEditComboConfig.setupAllCombos(
                        polities, polityField, "Terran",
                        worlds, worldField, "Brown",
                        fuel, fuelField, "Gas Giant",
                        tech, techField, null,
                        port, portField, null,
                        population, popField, null,
                        product, prodField, null,
                        milSpace, milSpaceField, null,
                        milPlan, milPlanField, null
                );

                politySelected.set(polities.getSelectionModel().getSelectedItem());
                worldSelected.set(worlds.getSelectionModel().getSelectedItem());
                fuelSelected.set(fuel.getSelectionModel().getSelectedItem());
            });

            assertEquals("Terran", politySelected.get());
            assertEquals("Brown", worldSelected.get());
            assertEquals("Gas Giant", fuelSelected.get());
        }
    }

    private void runOnFxThread(Runnable action) throws Exception {
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Exception> exception = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Exception e) {
                exception.set(e);
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(10, TimeUnit.SECONDS), "JavaFX operation timed out");

        if (exception.get() != null) {
            throw exception.get();
        }
    }
}
