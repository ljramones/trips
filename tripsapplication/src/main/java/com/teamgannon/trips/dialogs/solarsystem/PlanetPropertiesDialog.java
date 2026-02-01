package com.teamgannon.trips.dialogs.solarsystem;

import com.teamgannon.trips.jpa.model.ExoPlanet;
import com.teamgannon.trips.planetarymodelling.PlanetDescription;
import com.teamgannon.trips.utility.DialogUtils;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Enhanced dialog for viewing and editing exoplanet properties.
 * Displays all rich properties from the unified ExoPlanet entity including:
 * - General properties and habitability flags
 * - Orbital dynamics
 * - Climate properties
 * - Atmospheric properties
 * - Science fiction properties (for world-building)
 *
 * Changes to orbital parameters trigger a redraw of the solar system visualization.
 */
@Slf4j
public class PlanetPropertiesDialog extends Dialog<PlanetEditResult> {

    private final ExoPlanet planet;
    private final List<PlanetDescription> siblingPlanets;

    // Original values for change detection
    private final Double origSemiMajorAxis;
    private final Double origEccentricity;
    private final Double origInclination;
    private final Double origOmega;
    private final Double origOrbitalPeriod;

    // Original ring values for change detection
    private final Boolean origHasRings;
    private final String origRingType;
    private final Double origRingInnerRadius;
    private final Double origRingOuterRadius;

    // === GENERAL/IDENTITY SECTION ===
    private TextField nameField;
    private TextField statusField;
    private TextField discoveredField;
    private TextField starNameField;
    private TextField parentPlanetField;
    private TextField planetTypeField;
    private TextField orbitalZoneField;

    // Classification checkboxes
    private CheckBox isMoonCheck;
    private CheckBox habitableCheck;
    private CheckBox earthlikeCheck;
    private CheckBox gasGiantCheck;
    private CheckBox habitableJovianCheck;
    private CheckBox habitableMoonCheck;
    private CheckBox greenhouseCheck;
    private CheckBox tidallyLockedCheck;

    // === ORBITAL/DYNAMICS SECTION ===
    private TextField semiMajorAxisField;
    private TextField eccentricityField;
    private TextField inclinationField;
    private TextField omegaField;
    private TextField lonAscNodeField;
    private TextField orbitalPeriodField;
    private TextField radiusField;
    private TextField massField;
    private TextField densityField;
    private TextField coreRadiusField;
    private TextField axialTiltField;
    private TextField dayLengthField;
    private TextField surfaceGravityField;
    private TextField surfaceAccelField;
    private TextField escapeVelocityField;

    // === CLIMATE SECTION ===
    private TextField hydrosphereField;
    private TextField cloudCoverField;
    private TextField iceCoverField;
    private TextField albedoField;
    private TextField surfacePressureField;
    private TextField volatileGasField;
    private TextField minMolWeightField;

    // === ATMOSPHERE/TEMPERATURE SECTION ===
    private TextField surfaceTempField;
    private TextField highTempField;
    private TextField lowTempField;
    private TextField maxTempField;
    private TextField minTempField;
    private TextField exosphericTempField;
    private TextField boilingPointField;
    private TextField greenhouseRiseField;
    private TextField tempCalculatedField;
    private TextField tempMeasuredField;
    private TextField atmosphereTypeField;
    private TextArea atmosphereCompField;
    private TextField moleculesField;

    // === RING SYSTEM SECTION ===
    private CheckBox hasRingsCheck;
    private ComboBox<String> ringTypeCombo;
    private TextField ringInnerRadiusField;
    private TextField ringOuterRadiusField;
    private TextField ringThicknessField;
    private TextField ringInclinationField;
    private TextField ringPrimaryColorField;
    private TextField ringSecondaryColorField;

    // === PROCEDURAL/METADATA SECTION ===
    private TextField starSpTypeField;
    private TextField proceduralSeedField;
    private TextField proceduralSourceField;

    // === SCIENCE FICTION SECTION ===
    private TextField populationField;
    private TextField techLevelField;
    private TextField colonizationYearField;
    private TextField polityField;
    private TextField primaryResourceField;
    private TextField strategicImportanceField;
    private CheckBox colonizedCheck;
    private TextArea notesField;

    // Validation label
    private Label validationLabel;

    // Parent planet name for moons (passed in, since we need to look it up externally)
    private final String parentPlanetName;

    public PlanetPropertiesDialog(@NotNull ExoPlanet planet, List<PlanetDescription> siblingPlanets, String parentPlanetName) {
        this.planet = planet;
        this.siblingPlanets = siblingPlanets;
        this.parentPlanetName = parentPlanetName;

        // Store original orbital values for change detection
        this.origSemiMajorAxis = planet.getSemiMajorAxis();
        this.origEccentricity = planet.getEccentricity();
        this.origInclination = planet.getInclination();
        this.origOmega = planet.getOmega();
        this.origOrbitalPeriod = planet.getOrbitalPeriod();

        // Store original ring values for change detection
        this.origHasRings = planet.getHasRings();
        this.origRingType = planet.getRingType();
        this.origRingInnerRadius = planet.getRingInnerRadiusAU();
        this.origRingOuterRadius = planet.getRingOuterRadiusAU();

        setTitle("Planet Properties: " + planet.getName());

        // Create tabbed pane for organized sections
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // General tab (identity + habitability)
        Tab generalTab = new Tab("General");
        generalTab.setContent(createGeneralContent());
        tabPane.getTabs().add(generalTab);

        // Dynamics tab (orbital + physical)
        Tab dynamicsTab = new Tab("Dynamics");
        dynamicsTab.setContent(createDynamicsContent());
        tabPane.getTabs().add(dynamicsTab);

        // Climate tab
        Tab climateTab = new Tab("Climate");
        climateTab.setContent(createClimateContent());
        tabPane.getTabs().add(climateTab);

        // Atmosphere tab
        Tab atmosphereTab = new Tab("Atmosphere");
        atmosphereTab.setContent(createAtmosphereContent());
        tabPane.getTabs().add(atmosphereTab);

        // Rings tab
        Tab ringsTab = new Tab("Rings");
        ringsTab.setContent(createRingsContent());
        tabPane.getTabs().add(ringsTab);

        // Sci-Fi tab (for world-building)
        Tab sciFiTab = new Tab("Sci-Fi");
        sciFiTab.setContent(createSciFiContent());
        tabPane.getTabs().add(sciFiTab);

        VBox mainBox = new VBox(10);
        mainBox.setPadding(new Insets(10));
        mainBox.getChildren().add(tabPane);

        // Validation message
        validationLabel = new Label("");
        validationLabel.setTextFill(Color.ORANGE);
        mainBox.getChildren().add(validationLabel);

        // Buttons
        mainBox.getChildren().add(createButtonBar());

        this.getDialogPane().setContent(mainBox);
        this.getDialogPane().setPrefWidth(680);
        this.getDialogPane().setPrefHeight(600);

        DialogUtils.bindCloseHandler(this, this::handleClose);

        // Populate fields with current values
        populateFields();
    }

    // ==================== GENERAL TAB ====================
    private VBox createGeneralContent() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        // Identity section
        TitledPane identityPane = createIdentitySection();
        vbox.getChildren().add(identityPane);

        // Habitability section
        TitledPane habitabilityPane = createHabitabilitySection();
        vbox.getChildren().add(habitabilityPane);

        // Procedural metadata section
        TitledPane proceduralPane = createProceduralSection();
        vbox.getChildren().add(proceduralPane);

        return vbox;
    }

    private TitledPane createProceduralSection() {
        GridPane grid = createGridPane();

        grid.add(createBoldLabel("Procedural Source:"), 0, 0);
        proceduralSourceField = createTextField(200);
        proceduralSourceField.setPromptText("e.g., stargen, accrete, manual");
        grid.add(proceduralSourceField, 1, 0);

        grid.add(createBoldLabel("Procedural Seed:"), 2, 0);
        proceduralSeedField = createTextField(150);
        proceduralSeedField.setPromptText("Seed for reproducibility");
        grid.add(proceduralSeedField, 3, 0);

        TitledPane pane = new TitledPane("Procedural Metadata", grid);
        pane.setCollapsible(false);
        return pane;
    }

    private TitledPane createIdentitySection() {
        GridPane grid = createGridPane();

        grid.add(createBoldLabel("Name:"), 0, 0);
        nameField = createTextField(200);
        grid.add(nameField, 1, 0);

        grid.add(createBoldLabel("Planet Type:"), 2, 0);
        planetTypeField = createTextField(150);
        grid.add(planetTypeField, 3, 0);

        grid.add(createBoldLabel("Status:"), 0, 1);
        statusField = createTextField(200);
        grid.add(statusField, 1, 1);

        grid.add(createBoldLabel("Orbital Zone:"), 2, 1);
        orbitalZoneField = createTextField(150);
        grid.add(orbitalZoneField, 3, 1);

        grid.add(createBoldLabel("Discovered:"), 0, 2);
        discoveredField = createTextField(200);
        grid.add(discoveredField, 1, 2);

        grid.add(createBoldLabel("Host Star:"), 0, 3);
        starNameField = createTextField(200);
        starNameField.setEditable(false);
        starNameField.setStyle("-fx-background-color: #e8e8e8;");
        grid.add(starNameField, 1, 3);

        grid.add(createBoldLabel("Parent Planet:"), 2, 3);
        parentPlanetField = createTextField(150);
        parentPlanetField.setEditable(false);
        parentPlanetField.setStyle("-fx-background-color: #e8e8e8;");
        grid.add(parentPlanetField, 3, 3);

        grid.add(createBoldLabel("Star Spectral Type:"), 0, 4);
        starSpTypeField = createTextField(200);
        starSpTypeField.setEditable(false);
        starSpTypeField.setStyle("-fx-background-color: #e8e8e8;");
        grid.add(starSpTypeField, 1, 4);

        TitledPane pane = new TitledPane("Identity", grid);
        pane.setCollapsible(false);
        return pane;
    }

    private TitledPane createHabitabilitySection() {
        GridPane grid = createGridPane();
        grid.setHgap(20);

        isMoonCheck = new CheckBox("Is Moon");
        grid.add(isMoonCheck, 0, 0);

        habitableCheck = new CheckBox("Habitable");
        grid.add(habitableCheck, 1, 0);

        earthlikeCheck = new CheckBox("Earthlike");
        grid.add(earthlikeCheck, 2, 0);

        gasGiantCheck = new CheckBox("Gas Giant");
        grid.add(gasGiantCheck, 3, 0);

        habitableJovianCheck = new CheckBox("Habitable Jovian");
        grid.add(habitableJovianCheck, 0, 1);

        habitableMoonCheck = new CheckBox("Habitable Moon");
        grid.add(habitableMoonCheck, 1, 1);

        greenhouseCheck = new CheckBox("Greenhouse Effect");
        grid.add(greenhouseCheck, 2, 1);

        tidallyLockedCheck = new CheckBox("Tidally Locked");
        grid.add(tidallyLockedCheck, 3, 1);

        TitledPane pane = new TitledPane("Classification Flags", grid);
        pane.setCollapsible(false);
        return pane;
    }

    // ==================== DYNAMICS TAB ====================
    private VBox createDynamicsContent() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        // Orbital parameters section
        TitledPane orbitalPane = createOrbitalSection();
        vbox.getChildren().add(orbitalPane);

        // Physical properties section
        TitledPane physicalPane = createPhysicalSection();
        vbox.getChildren().add(physicalPane);

        return vbox;
    }

    private TitledPane createOrbitalSection() {
        GridPane grid = createGridPane();

        grid.add(createBoldLabel("Semi-major Axis (AU):"), 0, 0);
        semiMajorAxisField = createTextField(120);
        semiMajorAxisField.textProperty().addListener((obs, old, newVal) -> validateOrbits());
        grid.add(semiMajorAxisField, 1, 0);

        grid.add(createBoldLabel("Eccentricity:"), 2, 0);
        eccentricityField = createTextField(120);
        grid.add(eccentricityField, 3, 0);

        grid.add(createBoldLabel("Inclination (°):"), 0, 1);
        inclinationField = createTextField(120);
        grid.add(inclinationField, 1, 1);

        grid.add(createBoldLabel("Arg. Periapsis (°):"), 2, 1);
        omegaField = createTextField(120);
        grid.add(omegaField, 3, 1);

        grid.add(createBoldLabel("Lon. Asc. Node (°):"), 0, 2);
        lonAscNodeField = createTextField(120);
        grid.add(lonAscNodeField, 1, 2);

        grid.add(createBoldLabel("Period (days):"), 2, 2);
        orbitalPeriodField = createTextField(120);
        grid.add(orbitalPeriodField, 3, 2);

        TitledPane pane = new TitledPane("Orbital Parameters", grid);
        pane.setCollapsible(false);
        return pane;
    }

    private TitledPane createPhysicalSection() {
        GridPane grid = createGridPane();

        grid.add(createBoldLabel("Radius (R⊕):"), 0, 0);
        radiusField = createTextField(120);
        grid.add(radiusField, 1, 0);

        grid.add(createBoldLabel("Mass (M⊕):"), 2, 0);
        massField = createTextField(120);
        grid.add(massField, 3, 0);

        grid.add(createBoldLabel("Density (g/cm³):"), 0, 1);
        densityField = createTextField(120);
        grid.add(densityField, 1, 1);

        grid.add(createBoldLabel("Core Radius:"), 2, 1);
        coreRadiusField = createTextField(120);
        grid.add(coreRadiusField, 3, 1);

        grid.add(createBoldLabel("Axial Tilt (°):"), 0, 2);
        axialTiltField = createTextField(120);
        grid.add(axialTiltField, 1, 2);

        grid.add(createBoldLabel("Day Length (h):"), 2, 2);
        dayLengthField = createTextField(120);
        grid.add(dayLengthField, 3, 2);

        grid.add(createBoldLabel("Surface Gravity (g):"), 0, 3);
        surfaceGravityField = createTextField(120);
        grid.add(surfaceGravityField, 1, 3);

        grid.add(createBoldLabel("Surf. Accel (m/s²):"), 2, 3);
        surfaceAccelField = createTextField(120);
        grid.add(surfaceAccelField, 3, 3);

        grid.add(createBoldLabel("Escape Vel. (km/s):"), 0, 4);
        escapeVelocityField = createTextField(120);
        grid.add(escapeVelocityField, 1, 4);

        TitledPane pane = new TitledPane("Physical Properties", grid);
        pane.setCollapsible(false);
        return pane;
    }

    // ==================== RINGS TAB ====================
    private VBox createRingsContent() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        GridPane grid = createGridPane();

        hasRingsCheck = new CheckBox("Has Ring System");
        hasRingsCheck.selectedProperty().addListener((obs, old, newVal) -> {
            boolean enabled = newVal;
            ringTypeCombo.setDisable(!enabled);
            ringInnerRadiusField.setDisable(!enabled);
            ringOuterRadiusField.setDisable(!enabled);
            ringThicknessField.setDisable(!enabled);
            ringInclinationField.setDisable(!enabled);
            ringPrimaryColorField.setDisable(!enabled);
            ringSecondaryColorField.setDisable(!enabled);
        });
        grid.add(hasRingsCheck, 0, 0, 2, 1);

        grid.add(createBoldLabel("Ring Type:"), 2, 0);
        ringTypeCombo = new ComboBox<>();
        ringTypeCombo.getItems().addAll("SATURN", "URANUS", "NEPTUNE", "CUSTOM");
        ringTypeCombo.setValue("SATURN");
        ringTypeCombo.setPrefWidth(120);
        grid.add(ringTypeCombo, 3, 0);

        grid.add(createBoldLabel("Inner Radius (AU):"), 0, 1);
        ringInnerRadiusField = createTextField(130);
        ringInnerRadiusField.setPromptText("e.g., 0.0004");
        grid.add(ringInnerRadiusField, 1, 1);

        grid.add(createBoldLabel("Outer Radius (AU):"), 2, 1);
        ringOuterRadiusField = createTextField(130);
        ringOuterRadiusField.setPromptText("e.g., 0.0014");
        grid.add(ringOuterRadiusField, 3, 1);

        grid.add(createBoldLabel("Thickness:"), 0, 2);
        ringThicknessField = createTextField(130);
        ringThicknessField.setPromptText("0.01 = thin");
        grid.add(ringThicknessField, 1, 2);

        grid.add(createBoldLabel("Inclination (°):"), 2, 2);
        ringInclinationField = createTextField(130);
        ringInclinationField.setPromptText("0 = equatorial");
        grid.add(ringInclinationField, 3, 2);

        grid.add(createBoldLabel("Primary Color:"), 0, 3);
        ringPrimaryColorField = createTextField(130);
        ringPrimaryColorField.setPromptText("#E6DCC8");
        grid.add(ringPrimaryColorField, 1, 3);

        grid.add(createBoldLabel("Secondary Color:"), 2, 3);
        ringSecondaryColorField = createTextField(130);
        ringSecondaryColorField.setPromptText("#B4AA96");
        grid.add(ringSecondaryColorField, 3, 3);

        // Initially disable ring fields if no rings
        boolean hasRings = Boolean.TRUE.equals(planet.getHasRings());
        ringTypeCombo.setDisable(!hasRings);
        ringInnerRadiusField.setDisable(!hasRings);
        ringOuterRadiusField.setDisable(!hasRings);
        ringThicknessField.setDisable(!hasRings);
        ringInclinationField.setDisable(!hasRings);
        ringPrimaryColorField.setDisable(!hasRings);
        ringSecondaryColorField.setDisable(!hasRings);

        TitledPane ringPane = new TitledPane("Ring System Properties", grid);
        ringPane.setCollapsible(false);
        vbox.getChildren().add(ringPane);

        return vbox;
    }

    // ==================== CLIMATE TAB ====================
    private VBox createClimateContent() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        GridPane grid = createGridPane();

        grid.add(createBoldLabel("Hydrosphere (%):"), 0, 0);
        hydrosphereField = createTextField(120);
        grid.add(hydrosphereField, 1, 0);

        grid.add(createBoldLabel("Cloud Cover (%):"), 2, 0);
        cloudCoverField = createTextField(120);
        grid.add(cloudCoverField, 3, 0);

        grid.add(createBoldLabel("Ice Cover (%):"), 0, 1);
        iceCoverField = createTextField(120);
        grid.add(iceCoverField, 1, 1);

        grid.add(createBoldLabel("Albedo:"), 2, 1);
        albedoField = createTextField(120);
        grid.add(albedoField, 3, 1);

        grid.add(createBoldLabel("Surf. Pressure (atm):"), 0, 2);
        surfacePressureField = createTextField(120);
        grid.add(surfacePressureField, 1, 2);

        grid.add(createBoldLabel("Volatile Gas Inv.:"), 2, 2);
        volatileGasField = createTextField(120);
        grid.add(volatileGasField, 3, 2);

        grid.add(createBoldLabel("Min Mol. Weight:"), 0, 3);
        minMolWeightField = createTextField(120);
        grid.add(minMolWeightField, 1, 3);

        TitledPane climatePane = new TitledPane("Climate Properties", grid);
        climatePane.setCollapsible(false);
        vbox.getChildren().add(climatePane);

        return vbox;
    }

    // ==================== ATMOSPHERE TAB ====================
    private VBox createAtmosphereContent() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        // Temperature section
        TitledPane tempPane = createTemperatureSection();
        vbox.getChildren().add(tempPane);

        // Atmosphere composition section
        TitledPane atmoPane = createAtmosphereCompSection();
        vbox.getChildren().add(atmoPane);

        return vbox;
    }

    private TitledPane createTemperatureSection() {
        GridPane grid = createGridPane();

        grid.add(createBoldLabel("Surface Temp (K):"), 0, 0);
        surfaceTempField = createTextField(100);
        grid.add(surfaceTempField, 1, 0);

        grid.add(createBoldLabel("High Temp (K):"), 2, 0);
        highTempField = createTextField(100);
        grid.add(highTempField, 3, 0);

        grid.add(createBoldLabel("Low Temp (K):"), 0, 1);
        lowTempField = createTextField(100);
        grid.add(lowTempField, 1, 1);

        grid.add(createBoldLabel("Max Temp (K):"), 2, 1);
        maxTempField = createTextField(100);
        grid.add(maxTempField, 3, 1);

        grid.add(createBoldLabel("Min Temp (K):"), 0, 2);
        minTempField = createTextField(100);
        grid.add(minTempField, 1, 2);

        grid.add(createBoldLabel("Exospheric (K):"), 2, 2);
        exosphericTempField = createTextField(100);
        grid.add(exosphericTempField, 3, 2);

        grid.add(createBoldLabel("Boiling Pt (K):"), 0, 3);
        boilingPointField = createTextField(100);
        grid.add(boilingPointField, 1, 3);

        grid.add(createBoldLabel("Greenhouse (K):"), 2, 3);
        greenhouseRiseField = createTextField(100);
        grid.add(greenhouseRiseField, 3, 3);

        grid.add(createBoldLabel("Calculated (K):"), 0, 4);
        tempCalculatedField = createTextField(100);
        grid.add(tempCalculatedField, 1, 4);

        grid.add(createBoldLabel("Measured (K):"), 2, 4);
        tempMeasuredField = createTextField(100);
        grid.add(tempMeasuredField, 3, 4);

        TitledPane pane = new TitledPane("Temperature", grid);
        pane.setCollapsible(false);
        return pane;
    }

    private TitledPane createAtmosphereCompSection() {
        GridPane grid = createGridPane();

        grid.add(createBoldLabel("Atmosphere Type:"), 0, 0);
        atmosphereTypeField = createTextField(200);
        grid.add(atmosphereTypeField, 1, 0, 3, 1);

        grid.add(createBoldLabel("Composition:"), 0, 1);
        atmosphereCompField = new TextArea();
        atmosphereCompField.setPrefRowCount(3);
        atmosphereCompField.setPrefWidth(350);
        atmosphereCompField.setPromptText("Format: N2:780;O2:210;Ar:9...");
        grid.add(atmosphereCompField, 1, 1, 3, 1);

        grid.add(createBoldLabel("Molecules:"), 0, 2);
        moleculesField = createTextField(350);
        moleculesField.setPromptText("Detected molecules (e.g., H2O, CO2, CH4)");
        grid.add(moleculesField, 1, 2, 3, 1);

        TitledPane pane = new TitledPane("Atmosphere Composition", grid);
        pane.setCollapsible(false);
        return pane;
    }

    // ==================== SCI-FI TAB ====================
    private VBox createSciFiContent() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        GridPane grid = createGridPane();

        grid.add(createBoldLabel("Population:"), 0, 0);
        populationField = createTextField(150);
        grid.add(populationField, 1, 0);

        colonizedCheck = new CheckBox("Colonized");
        grid.add(colonizedCheck, 2, 0);

        grid.add(createBoldLabel("Tech Level:"), 0, 1);
        techLevelField = createTextField(150);
        grid.add(techLevelField, 1, 1);

        grid.add(createBoldLabel("Colony Year:"), 2, 1);
        colonizationYearField = createTextField(100);
        grid.add(colonizationYearField, 3, 1);

        grid.add(createBoldLabel("Polity:"), 0, 2);
        polityField = createTextField(150);
        grid.add(polityField, 1, 2);

        grid.add(createBoldLabel("Importance:"), 2, 2);
        strategicImportanceField = createTextField(100);
        grid.add(strategicImportanceField, 3, 2);

        grid.add(createBoldLabel("Resource:"), 0, 3);
        primaryResourceField = createTextField(150);
        grid.add(primaryResourceField, 1, 3);

        TitledPane sciFiPane = new TitledPane("Science Fiction Properties", grid);
        sciFiPane.setCollapsible(false);
        vbox.getChildren().add(sciFiPane);

        // Notes section
        GridPane notesGrid = createGridPane();
        notesGrid.add(createBoldLabel("Notes:"), 0, 0);
        notesField = new TextArea();
        notesField.setPrefRowCount(4);
        notesField.setPrefWidth(450);
        notesGrid.add(notesField, 0, 1, 4, 1);

        TitledPane notesPane = new TitledPane("Notes", notesGrid);
        notesPane.setCollapsible(false);
        vbox.getChildren().add(notesPane);

        return vbox;
    }

    // ==================== BUTTON BAR ====================
    private HBox createButtonBar() {
        HBox hbox = new HBox(10);
        hbox.setAlignment(Pos.CENTER_RIGHT);
        hbox.setPadding(new Insets(10, 0, 0, 0));

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(this::handleCancel);

        Button okBtn = new Button("OK");
        okBtn.setDefaultButton(true);
        okBtn.setOnAction(this::handleOk);

        hbox.getChildren().addAll(cancelBtn, okBtn);
        return hbox;
    }

    // ==================== UTILITY METHODS ====================
    private GridPane createGridPane() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));
        return grid;
    }

    private TextField createTextField(double width) {
        TextField field = new TextField();
        field.setPrefWidth(width);
        return field;
    }

    private Label createBoldLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("System", FontWeight.BOLD, 12));
        return label;
    }

    // ==================== POPULATE FIELDS ====================
    private void populateFields() {
        // === GENERAL/IDENTITY ===
        nameField.setText(safeString(planet.getName()));
        statusField.setText(safeString(planet.getPlanetStatus()));
        discoveredField.setText(planet.getDiscovered() != null ? planet.getDiscovered().toString() : "");
        starNameField.setText(safeString(planet.getStarName()));
        parentPlanetField.setText(safeString(parentPlanetName));
        planetTypeField.setText(safeString(planet.getPlanetType()));
        orbitalZoneField.setText(planet.getOrbitalZone() != null ? planet.getOrbitalZone().toString() : "");

        // Classification flags
        isMoonCheck.setSelected(Boolean.TRUE.equals(planet.getIsMoon()));
        habitableCheck.setSelected(Boolean.TRUE.equals(planet.getHabitable()));
        earthlikeCheck.setSelected(Boolean.TRUE.equals(planet.getEarthlike()));
        gasGiantCheck.setSelected(Boolean.TRUE.equals(planet.getGasGiant()));
        habitableJovianCheck.setSelected(Boolean.TRUE.equals(planet.getHabitableJovian()));
        habitableMoonCheck.setSelected(Boolean.TRUE.equals(planet.getHabitableMoon()));
        greenhouseCheck.setSelected(Boolean.TRUE.equals(planet.getGreenhouseEffect()));
        tidallyLockedCheck.setSelected(Boolean.TRUE.equals(planet.getTidallyLocked()));

        // Star spectral type (read-only)
        starSpTypeField.setText(safeString(planet.getStarSpType()));

        // Procedural metadata
        proceduralSourceField.setText(safeString(planet.getProceduralSource()));
        proceduralSeedField.setText(planet.getProceduralSeed() != null ? planet.getProceduralSeed().toString() : "");

        // === ORBITAL ===
        semiMajorAxisField.setText(formatDouble(planet.getSemiMajorAxis()));
        eccentricityField.setText(formatDouble(planet.getEccentricity()));
        inclinationField.setText(formatDouble(planet.getInclination()));
        omegaField.setText(formatDouble(planet.getOmega()));
        lonAscNodeField.setText(formatDouble(planet.getLongitudeOfAscendingNode()));
        orbitalPeriodField.setText(formatDouble(planet.getOrbitalPeriod()));

        // === PHYSICAL ===
        radiusField.setText(formatDouble(planet.getRadius()));
        massField.setText(formatDouble(planet.getMass()));
        densityField.setText(formatDouble(planet.getDensity()));
        coreRadiusField.setText(formatDouble(planet.getCoreRadius()));
        axialTiltField.setText(formatDouble(planet.getAxialTilt()));
        dayLengthField.setText(formatDouble(planet.getDayLength()));
        surfaceGravityField.setText(formatDouble(planet.getSurfaceGravity()));
        surfaceAccelField.setText(formatDouble(planet.getSurfaceAcceleration()));
        escapeVelocityField.setText(formatDouble(planet.getEscapeVelocity()));

        // === CLIMATE ===
        hydrosphereField.setText(formatDouble(planet.getHydrosphere()));
        cloudCoverField.setText(formatDouble(planet.getCloudCover()));
        iceCoverField.setText(formatDouble(planet.getIceCover()));
        albedoField.setText(formatDouble(planet.getAlbedo()));
        surfacePressureField.setText(formatDouble(planet.getSurfacePressure()));
        volatileGasField.setText(formatDouble(planet.getVolatileGasInventory()));
        minMolWeightField.setText(formatDouble(planet.getMinimumMolecularWeight()));

        // === ATMOSPHERE/TEMPERATURE ===
        surfaceTempField.setText(formatDouble(planet.getSurfaceTemperature()));
        highTempField.setText(formatDouble(planet.getHighTemperature()));
        lowTempField.setText(formatDouble(planet.getLowTemperature()));
        maxTempField.setText(formatDouble(planet.getMaxTemperature()));
        minTempField.setText(formatDouble(planet.getMinTemperature()));
        exosphericTempField.setText(formatDouble(planet.getExosphericTemperature()));
        boilingPointField.setText(formatDouble(planet.getBoilingPoint()));
        greenhouseRiseField.setText(formatDouble(planet.getGreenhouseRise()));
        tempCalculatedField.setText(formatDouble(planet.getTempCalculated()));
        tempMeasuredField.setText(formatDouble(planet.getTempMeasured()));
        atmosphereTypeField.setText(safeString(planet.getAtmosphereType()));
        atmosphereCompField.setText(safeString(planet.getAtmosphereComposition()));
        moleculesField.setText(safeString(planet.getMolecules()));

        // === RING SYSTEM ===
        hasRingsCheck.setSelected(Boolean.TRUE.equals(planet.getHasRings()));
        if (planet.getRingType() != null && !planet.getRingType().isEmpty()) {
            ringTypeCombo.setValue(planet.getRingType().toUpperCase());
        } else {
            ringTypeCombo.setValue("SATURN");
        }
        ringInnerRadiusField.setText(formatDouble(planet.getRingInnerRadiusAU()));
        ringOuterRadiusField.setText(formatDouble(planet.getRingOuterRadiusAU()));
        ringThicknessField.setText(formatDouble(planet.getRingThickness()));
        ringInclinationField.setText(formatDouble(planet.getRingInclination()));
        ringPrimaryColorField.setText(safeString(planet.getRingPrimaryColor()));
        ringSecondaryColorField.setText(safeString(planet.getRingSecondaryColor()));

        // === SCI-FI ===
        populationField.setText(planet.getPopulation() != null ? planet.getPopulation().toString() : "");
        techLevelField.setText(planet.getTechLevel() != null ? planet.getTechLevel().toString() : "");
        colonizationYearField.setText(planet.getColonizationYear() != null ? planet.getColonizationYear().toString() : "");
        polityField.setText(safeString(planet.getPolity()));
        primaryResourceField.setText(safeString(planet.getPrimaryResource()));
        strategicImportanceField.setText(planet.getStrategicImportance() != null ? planet.getStrategicImportance().toString() : "");
        colonizedCheck.setSelected(Boolean.TRUE.equals(planet.getColonized()));
        notesField.setText(safeString(planet.getNotes()));
    }

    private String safeString(String s) {
        return s != null ? s : "";
    }

    private String formatDouble(Double value) {
        if (value == null) return "";
        if (Math.abs(value) > 1e10 || (value != 0 && Math.abs(value) < 0.0001)) {
            return "%.4g".formatted(value);
        }
        return "%.4f".formatted(value);
    }

    // ==================== PARSE HELPERS ====================
    private Double parseDouble(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInteger(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Long parseLong(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        try {
            return Long.parseLong(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ==================== VALIDATION ====================
    private void validateOrbits() {
        Double newSMA = parseDouble(semiMajorAxisField.getText());
        if (newSMA == null || newSMA <= 0) {
            validationLabel.setText("");
            return;
        }

        // Check if any sibling planet has a similar orbit
        for (PlanetDescription sibling : siblingPlanets) {
            if (sibling.getName().equals(planet.getName())) {
                continue;  // Skip self
            }
            double sibSMA = sibling.getSemiMajorAxis();
            if (sibSMA > 0 && Math.abs(newSMA - sibSMA) / sibSMA < 0.05) {
                validationLabel.setText("Warning: Orbit overlaps with " + sibling.getName() +
                        " (SMA: " + "%.4f".formatted(sibSMA) + " AU)");
                return;
            }
        }
        validationLabel.setText("");
    }

    // ==================== HANDLERS ====================
    private void handleOk(ActionEvent event) {
        // === UPDATE GENERAL/IDENTITY ===
        planet.setName(nameField.getText().trim());
        planet.setPlanetStatus(statusField.getText().trim());
        planet.setDiscovered(parseInteger(discoveredField.getText()));
        planet.setPlanetType(planetTypeField.getText().trim());
        planet.setOrbitalZone(parseInteger(orbitalZoneField.getText()));

        // Classification flags
        planet.setIsMoon(isMoonCheck.isSelected());
        planet.setHabitable(habitableCheck.isSelected());
        planet.setEarthlike(earthlikeCheck.isSelected());
        planet.setGasGiant(gasGiantCheck.isSelected());
        planet.setHabitableJovian(habitableJovianCheck.isSelected());
        planet.setHabitableMoon(habitableMoonCheck.isSelected());
        planet.setGreenhouseEffect(greenhouseCheck.isSelected());
        planet.setTidallyLocked(tidallyLockedCheck.isSelected());

        // Procedural metadata (starSpType is read-only, not saved)
        String procSource = proceduralSourceField.getText().trim();
        planet.setProceduralSource(procSource.isEmpty() ? null : procSource);
        planet.setProceduralSeed(parseLong(proceduralSeedField.getText()));

        // === UPDATE ORBITAL ===
        planet.setSemiMajorAxis(parseDouble(semiMajorAxisField.getText()));
        planet.setEccentricity(parseDouble(eccentricityField.getText()));
        planet.setInclination(parseDouble(inclinationField.getText()));
        planet.setOmega(parseDouble(omegaField.getText()));
        planet.setLongitudeOfAscendingNode(parseDouble(lonAscNodeField.getText()));
        planet.setOrbitalPeriod(parseDouble(orbitalPeriodField.getText()));

        // === UPDATE PHYSICAL ===
        planet.setRadius(parseDouble(radiusField.getText()));
        planet.setMass(parseDouble(massField.getText()));
        planet.setDensity(parseDouble(densityField.getText()));
        planet.setCoreRadius(parseDouble(coreRadiusField.getText()));
        planet.setAxialTilt(parseDouble(axialTiltField.getText()));
        planet.setDayLength(parseDouble(dayLengthField.getText()));
        planet.setSurfaceGravity(parseDouble(surfaceGravityField.getText()));
        planet.setSurfaceAcceleration(parseDouble(surfaceAccelField.getText()));
        planet.setEscapeVelocity(parseDouble(escapeVelocityField.getText()));

        // === UPDATE CLIMATE ===
        planet.setHydrosphere(parseDouble(hydrosphereField.getText()));
        planet.setCloudCover(parseDouble(cloudCoverField.getText()));
        planet.setIceCover(parseDouble(iceCoverField.getText()));
        planet.setAlbedo(parseDouble(albedoField.getText()));
        planet.setSurfacePressure(parseDouble(surfacePressureField.getText()));
        planet.setVolatileGasInventory(parseDouble(volatileGasField.getText()));
        planet.setMinimumMolecularWeight(parseDouble(minMolWeightField.getText()));

        // === UPDATE ATMOSPHERE/TEMPERATURE ===
        planet.setSurfaceTemperature(parseDouble(surfaceTempField.getText()));
        planet.setHighTemperature(parseDouble(highTempField.getText()));
        planet.setLowTemperature(parseDouble(lowTempField.getText()));
        planet.setMaxTemperature(parseDouble(maxTempField.getText()));
        planet.setMinTemperature(parseDouble(minTempField.getText()));
        planet.setExosphericTemperature(parseDouble(exosphericTempField.getText()));
        planet.setBoilingPoint(parseDouble(boilingPointField.getText()));
        planet.setGreenhouseRise(parseDouble(greenhouseRiseField.getText()));
        planet.setTempCalculated(parseDouble(tempCalculatedField.getText()));
        planet.setTempMeasured(parseDouble(tempMeasuredField.getText()));
        planet.setAtmosphereType(atmosphereTypeField.getText().trim());
        planet.setAtmosphereComposition(atmosphereCompField.getText().trim());
        String molecules = moleculesField.getText().trim();
        planet.setMolecules(molecules.isEmpty() ? null : molecules);

        // === UPDATE RING SYSTEM ===
        planet.setHasRings(hasRingsCheck.isSelected());
        if (hasRingsCheck.isSelected()) {
            planet.setRingType(ringTypeCombo.getValue());
            planet.setRingInnerRadiusAU(parseDouble(ringInnerRadiusField.getText()));
            planet.setRingOuterRadiusAU(parseDouble(ringOuterRadiusField.getText()));
            planet.setRingThickness(parseDouble(ringThicknessField.getText()));
            planet.setRingInclination(parseDouble(ringInclinationField.getText()));
            String primaryColor = ringPrimaryColorField.getText().trim();
            planet.setRingPrimaryColor(primaryColor.isEmpty() ? null : primaryColor);
            String secondaryColor = ringSecondaryColorField.getText().trim();
            planet.setRingSecondaryColor(secondaryColor.isEmpty() ? null : secondaryColor);
        } else {
            // Clear ring values if rings are disabled
            planet.setRingType(null);
            planet.setRingInnerRadiusAU(null);
            planet.setRingOuterRadiusAU(null);
            planet.setRingThickness(null);
            planet.setRingInclination(null);
            planet.setRingPrimaryColor(null);
            planet.setRingSecondaryColor(null);
        }

        // === UPDATE SCI-FI ===
        planet.setPopulation(parseLong(populationField.getText()));
        planet.setTechLevel(parseInteger(techLevelField.getText()));
        planet.setColonizationYear(parseInteger(colonizationYearField.getText()));
        planet.setPolity(polityField.getText().trim());
        planet.setPrimaryResource(primaryResourceField.getText().trim());
        planet.setStrategicImportance(parseInteger(strategicImportanceField.getText()));
        planet.setColonized(colonizedCheck.isSelected());
        planet.setNotes(notesField.getText().trim());

        // Determine if anything changed (include ring changes as orbital changes to trigger redraw)
        boolean orbitalChanged = hasOrbitalChanged() || hasRingsChanged();

        log.info("Planet edit completed: orbitalChanged={}", orbitalChanged);

        setResult(PlanetEditResult.changed(planet, orbitalChanged));
    }

    private boolean hasOrbitalChanged() {
        return !nullSafeEquals(origSemiMajorAxis, planet.getSemiMajorAxis()) ||
               !nullSafeEquals(origEccentricity, planet.getEccentricity()) ||
               !nullSafeEquals(origInclination, planet.getInclination()) ||
               !nullSafeEquals(origOmega, planet.getOmega());
    }

    private boolean hasRingsChanged() {
        boolean currentHasRings = Boolean.TRUE.equals(planet.getHasRings());
        boolean originalHasRings = Boolean.TRUE.equals(origHasRings);

        if (currentHasRings != originalHasRings) {
            return true;  // Ring status changed
        }

        if (!currentHasRings) {
            return false;  // No rings before or after, no change
        }

        // Ring exists, check if parameters changed
        return !stringEquals(origRingType, planet.getRingType()) ||
               !nullSafeEquals(origRingInnerRadius, planet.getRingInnerRadiusAU()) ||
               !nullSafeEquals(origRingOuterRadius, planet.getRingOuterRadiusAU());
    }

    private boolean stringEquals(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    private boolean nullSafeEquals(Double a, Double b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return Math.abs(a - b) < 0.0000001;
    }

    private void handleCancel(ActionEvent event) {
        setResult(PlanetEditResult.unchanged(planet));
    }

    private void handleClose(WindowEvent event) {
        setResult(PlanetEditResult.unchanged(planet));
    }
}
