package com.teamgannon.trips.screenobjects;

import com.teamgannon.trips.jpa.model.CivilizationDisplayPreferences;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.util.List;

/**
 * Configuration and population of combo boxes for the Star Edit Dialog.
 * Contains all static combo box item definitions and setup logic.
 */
public class StarEditComboConfig {

    // Polities (civilizations)
    public static final List<String> POLITIES = List.of(
            CivilizationDisplayPreferences.TERRAN,
            CivilizationDisplayPreferences.DORNANI,
            CivilizationDisplayPreferences.KTOR,
            CivilizationDisplayPreferences.ARAKUR,
            CivilizationDisplayPreferences.HKHRKH,
            CivilizationDisplayPreferences.SLAASRIITHI,
            CivilizationDisplayPreferences.OTHER1,
            CivilizationDisplayPreferences.OTHER2,
            CivilizationDisplayPreferences.OTHER3,
            CivilizationDisplayPreferences.OTHER4,
            "NA"
    );

    // Fuel types
    public static final List<String> FUEL_TYPES = List.of(
            "H2", "Antimatter", "Gas Giant", "Water World", "NA"
    );

    // World types
    public static final List<String> WORLD_TYPES = List.of(
            "Green", "Grey", "Brown", "NA"
    );

    // Port classes
    public static final List<String> PORT_TYPES = List.of(
            "A", "B", "C", "D", "E", "NA"
    );

    // Military space-side types
    public static final List<String> MIL_SPACE_TYPES = List.of(
            "A", "B", "C", "D", "E", "NA"
    );

    // Military planet-side types
    public static final List<String> MIL_PLAN_TYPES = List.of(
            "A", "B", "C", "D", "E", "NA"
    );

    // Tech levels
    public static final List<String> TECH_TYPES = List.of(
            "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "NA"
    );

    // Population levels (with Unicode superscripts)
    public static final List<String> POPULATION_TYPES = List.of(
            "1s", "10s", "100s", "1000s",
            "10\u2074", "10\u2075", "10\u2076", "10\u2077", "10\u2078", "10\u2079",
            "NA"
    );

    // Product types
    public static final List<String> PRODUCT_TYPES = List.of(
            "Agricultural", "Industry", "Services", "Raw Materials", "Bio",
            "Fossil Fuel", "Finished Goods", "Hi Tech", "Unique",
            "Energy (Nuke, AM or Fuel Cells \nat high export levels", "NA"
    );

    private StarEditComboConfig() {
        // Utility class
    }

    /**
     * Configure a combo box with items, link to a text field, and set initial selection.
     *
     * @param comboBox  the combo box to configure
     * @param items     the items to add
     * @param textField the text field to update when selection changes
     * @param currentValue the current value to select (may be null)
     */
    public static void setupCombo(ComboBox<String> comboBox, List<String> items,
                                  TextField textField, String currentValue) {
        comboBox.getItems().addAll(items);
        comboBox.getSelectionModel().selectedItemProperty().addListener(
                (options, oldValue, newValue) -> textField.setText(newValue)
        );

        // Select current value if present, otherwise "NA"
        if (currentValue != null && !currentValue.isBlank() && items.contains(currentValue)) {
            comboBox.getSelectionModel().select(currentValue);
        } else {
            comboBox.getSelectionModel().select("NA");
        }
    }

    /**
     * Set up all fictional info combo boxes.
     *
     * @param polities   polity combo box
     * @param polityField polity text field
     * @param currentPolity current polity value
     * @param worlds     world type combo box
     * @param worldField world type text field
     * @param currentWorld current world type
     * @param fuel       fuel type combo box
     * @param fuelField  fuel type text field
     * @param currentFuel current fuel type
     * @param tech       tech level combo box
     * @param techField  tech level text field
     * @param currentTech current tech level
     * @param port       port type combo box
     * @param portField  port type text field
     * @param currentPort current port type
     * @param population population combo box
     * @param popField   population text field
     * @param currentPop current population
     * @param product    product combo box
     * @param prodField  product text field
     * @param currentProd current product
     * @param milSpace   military space combo box
     * @param milSpaceField military space text field
     * @param currentMilSpace current military space
     * @param milPlan    military planet combo box
     * @param milPlanField military planet text field
     * @param currentMilPlan current military planet
     */
    public static void setupAllCombos(
            ComboBox<String> polities, TextField polityField, String currentPolity,
            ComboBox<String> worlds, TextField worldField, String currentWorld,
            ComboBox<String> fuel, TextField fuelField, String currentFuel,
            ComboBox<String> tech, TextField techField, String currentTech,
            ComboBox<String> port, TextField portField, String currentPort,
            ComboBox<String> population, TextField popField, String currentPop,
            ComboBox<String> product, TextField prodField, String currentProd,
            ComboBox<String> milSpace, TextField milSpaceField, String currentMilSpace,
            ComboBox<String> milPlan, TextField milPlanField, String currentMilPlan
    ) {
        setupCombo(polities, POLITIES, polityField, currentPolity);
        setupCombo(worlds, WORLD_TYPES, worldField, currentWorld);
        setupCombo(fuel, FUEL_TYPES, fuelField, currentFuel);
        setupCombo(tech, TECH_TYPES, techField, currentTech);
        setupCombo(port, PORT_TYPES, portField, currentPort);
        setupCombo(population, POPULATION_TYPES, popField, currentPop);
        setupCombo(product, PRODUCT_TYPES, prodField, currentProd);
        setupCombo(milSpace, MIL_SPACE_TYPES, milSpaceField, currentMilSpace);
        setupCombo(milPlan, MIL_PLAN_TYPES, milPlanField, currentMilPlan);
    }
}
