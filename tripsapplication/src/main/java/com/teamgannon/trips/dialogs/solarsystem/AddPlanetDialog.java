package com.teamgannon.trips.dialogs.solarsystem;

import com.teamgannon.trips.jpa.model.ExoPlanet;
import com.teamgannon.trips.planetarymodelling.SolarSystemDescription;
import com.teamgannon.trips.utility.DialogUtils;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * Dialog for adding a new planet or moon to a solar system.
 * Includes all properties matching the PlanetPropertiesDialog:
 * - General (identity + habitability flags)
 * - Dynamics (orbital + physical properties)
 * - Climate
 * - Atmosphere
 * - Sci-Fi (world-building)
 */
@Slf4j
public class AddPlanetDialog extends Dialog<ExoPlanet> {

    private final SolarSystemDescription solarSystem;
    private final boolean isMoon;
    private final ExoPlanet parentPlanet;

    // === GENERAL/IDENTITY SECTION ===
    private TextField nameField;
    private ComboBox<String> planetTypeCombo;
    private TextField orbitalZoneField;

    // Classification checkboxes
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
    private TextField atmosphereTypeField;
    private TextArea atmosphereCompField;

    // === SCIENCE FICTION SECTION ===
    private TextField populationField;
    private TextField techLevelField;
    private TextField colonizationYearField;
    private TextField polityField;
    private TextField primaryResourceField;
    private TextField strategicImportanceField;
    private CheckBox colonizedCheck;
    private TextArea notesField;

    public AddPlanetDialog(SolarSystemDescription solarSystem, boolean isMoon, ExoPlanet parentPlanet) {
        this.solarSystem = solarSystem;
        this.isMoon = isMoon;
        this.parentPlanet = parentPlanet;

        if (isMoon) {
            setTitle("Add Moon to " + (parentPlanet != null ? parentPlanet.getName() : "Planet"));
        } else {
            setTitle("Add Planet to System");
        }

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

        // Sci-Fi tab (for world-building)
        Tab sciFiTab = new Tab("Sci-Fi");
        sciFiTab.setContent(createSciFiContent());
        tabPane.getTabs().add(sciFiTab);

        VBox mainBox = new VBox(10);
        mainBox.setPadding(new Insets(10));
        mainBox.getChildren().add(tabPane);

        // Buttons
        mainBox.getChildren().add(createButtonBar());

        this.getDialogPane().setContent(mainBox);
        this.getDialogPane().setPrefWidth(580);
        this.getDialogPane().setPrefHeight(500);

        DialogUtils.bindCloseHandler(this, this::handleClose);

        // Set defaults
        setDefaults();
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

        return vbox;
    }

    private TitledPane createIdentitySection() {
        GridPane grid = createGridPane();

        grid.add(createBoldLabel("Name:"), 0, 0);
        nameField = createTextField(200);
        nameField.setPromptText(isMoon ? "e.g., Europa" : "e.g., Kepler-442b");
        grid.add(nameField, 1, 0);

        grid.add(createBoldLabel("Planet Type:"), 2, 0);
        planetTypeCombo = new ComboBox<>();
        planetTypeCombo.getItems().addAll(
                "Terrestrial",
                "Gas Giant",
                "Ice Giant",
                "Super-Earth",
                "Mini-Neptune",
                "Hot Jupiter",
                "Ocean World",
                "Desert World",
                "Ice World",
                "Unknown"
        );
        planetTypeCombo.setValue("Terrestrial");
        grid.add(planetTypeCombo, 3, 0);

        grid.add(createBoldLabel("Orbital Zone:"), 0, 1);
        orbitalZoneField = createTextField(100);
        orbitalZoneField.setPromptText("1=inner, 2=mid, 3=outer");
        grid.add(orbitalZoneField, 1, 1);

        if (isMoon && parentPlanet != null) {
            grid.add(createBoldLabel("Parent Planet:"), 2, 1);
            TextField parentField = createTextField(150);
            parentField.setText(parentPlanet.getName());
            parentField.setEditable(false);
            parentField.setStyle("-fx-background-color: #e8e8e8;");
            grid.add(parentField, 3, 1);
        }

        TitledPane pane = new TitledPane("Identity", grid);
        pane.setCollapsible(false);
        return pane;
    }

    private TitledPane createHabitabilitySection() {
        GridPane grid = createGridPane();
        grid.setHgap(20);

        habitableCheck = new CheckBox("Habitable");
        grid.add(habitableCheck, 0, 0);

        earthlikeCheck = new CheckBox("Earthlike");
        grid.add(earthlikeCheck, 1, 0);

        gasGiantCheck = new CheckBox("Gas Giant");
        grid.add(gasGiantCheck, 2, 0);

        tidallyLockedCheck = new CheckBox("Tidally Locked");
        grid.add(tidallyLockedCheck, 3, 0);

        habitableJovianCheck = new CheckBox("Habitable Jovian");
        grid.add(habitableJovianCheck, 0, 1);

        habitableMoonCheck = new CheckBox("Habitable Moon");
        grid.add(habitableMoonCheck, 1, 1);

        greenhouseCheck = new CheckBox("Greenhouse Effect");
        grid.add(greenhouseCheck, 2, 1);

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
        semiMajorAxisField.setPromptText(isMoon ? "e.g., 0.003" : "e.g., 1.0");
        grid.add(semiMajorAxisField, 1, 0);

        grid.add(createBoldLabel("Eccentricity:"), 2, 0);
        eccentricityField = createTextField(120);
        eccentricityField.setPromptText("0.0 - 0.99");
        grid.add(eccentricityField, 3, 0);

        grid.add(createBoldLabel("Inclination (deg):"), 0, 1);
        inclinationField = createTextField(120);
        inclinationField.setPromptText("e.g., 0.0");
        grid.add(inclinationField, 1, 1);

        grid.add(createBoldLabel("Arg. of Periapsis (deg):"), 2, 1);
        omegaField = createTextField(120);
        omegaField.setPromptText("e.g., 0.0");
        grid.add(omegaField, 3, 1);

        grid.add(createBoldLabel("Lon. Asc. Node (deg):"), 0, 2);
        lonAscNodeField = createTextField(120);
        lonAscNodeField.setPromptText("e.g., 0.0");
        grid.add(lonAscNodeField, 1, 2);

        grid.add(createBoldLabel("Orbital Period (days):"), 2, 2);
        orbitalPeriodField = createTextField(120);
        orbitalPeriodField.setPromptText(isMoon ? "e.g., 3.5" : "e.g., 365.25");
        grid.add(orbitalPeriodField, 3, 2);

        TitledPane pane = new TitledPane("Orbital Parameters", grid);
        pane.setCollapsible(false);
        return pane;
    }

    private TitledPane createPhysicalSection() {
        GridPane grid = createGridPane();

        grid.add(createBoldLabel("Radius (Earth radii):"), 0, 0);
        radiusField = createTextField(120);
        radiusField.setPromptText(isMoon ? "e.g., 0.25" : "e.g., 1.0");
        grid.add(radiusField, 1, 0);

        grid.add(createBoldLabel("Mass (Earth masses):"), 2, 0);
        massField = createTextField(120);
        massField.setPromptText(isMoon ? "e.g., 0.01" : "e.g., 1.0");
        grid.add(massField, 3, 0);

        grid.add(createBoldLabel("Density (g/cm³):"), 0, 1);
        densityField = createTextField(120);
        densityField.setPromptText("e.g., 5.5");
        grid.add(densityField, 1, 1);

        grid.add(createBoldLabel("Core Radius:"), 2, 1);
        coreRadiusField = createTextField(120);
        grid.add(coreRadiusField, 3, 1);

        grid.add(createBoldLabel("Axial Tilt (deg):"), 0, 2);
        axialTiltField = createTextField(120);
        axialTiltField.setPromptText("e.g., 23.5");
        grid.add(axialTiltField, 1, 2);

        grid.add(createBoldLabel("Day Length (hours):"), 2, 2);
        dayLengthField = createTextField(120);
        dayLengthField.setPromptText("e.g., 24.0");
        grid.add(dayLengthField, 3, 2);

        grid.add(createBoldLabel("Surface Gravity (g):"), 0, 3);
        surfaceGravityField = createTextField(120);
        surfaceGravityField.setPromptText("e.g., 1.0");
        grid.add(surfaceGravityField, 1, 3);

        grid.add(createBoldLabel("Surface Accel (m/s²):"), 2, 3);
        surfaceAccelField = createTextField(120);
        surfaceAccelField.setPromptText("e.g., 9.8");
        grid.add(surfaceAccelField, 3, 3);

        grid.add(createBoldLabel("Escape Velocity (km/s):"), 0, 4);
        escapeVelocityField = createTextField(120);
        escapeVelocityField.setPromptText("e.g., 11.2");
        grid.add(escapeVelocityField, 1, 4);

        TitledPane pane = new TitledPane("Physical Properties", grid);
        pane.setCollapsible(false);
        return pane;
    }

    // ==================== CLIMATE TAB ====================
    private VBox createClimateContent() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        GridPane grid = createGridPane();

        grid.add(createBoldLabel("Hydrosphere (%):"), 0, 0);
        hydrosphereField = createTextField(120);
        hydrosphereField.setPromptText("e.g., 70");
        grid.add(hydrosphereField, 1, 0);

        grid.add(createBoldLabel("Cloud Cover (%):"), 2, 0);
        cloudCoverField = createTextField(120);
        cloudCoverField.setPromptText("e.g., 50");
        grid.add(cloudCoverField, 3, 0);

        grid.add(createBoldLabel("Ice Cover (%):"), 0, 1);
        iceCoverField = createTextField(120);
        iceCoverField.setPromptText("e.g., 10");
        grid.add(iceCoverField, 1, 1);

        grid.add(createBoldLabel("Albedo:"), 2, 1);
        albedoField = createTextField(120);
        albedoField.setPromptText("e.g., 0.3");
        grid.add(albedoField, 3, 1);

        grid.add(createBoldLabel("Surface Pressure (atm):"), 0, 2);
        surfacePressureField = createTextField(120);
        surfacePressureField.setPromptText("e.g., 1.0");
        grid.add(surfacePressureField, 1, 2);

        grid.add(createBoldLabel("Volatile Gas Inventory:"), 2, 2);
        volatileGasField = createTextField(120);
        grid.add(volatileGasField, 3, 2);

        grid.add(createBoldLabel("Min Molecular Weight:"), 0, 3);
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
        surfaceTempField.setPromptText("e.g., 288");
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

        grid.add(createBoldLabel("Exospheric Temp (K):"), 2, 2);
        exosphericTempField = createTextField(100);
        grid.add(exosphericTempField, 3, 2);

        grid.add(createBoldLabel("Boiling Point (K):"), 0, 3);
        boilingPointField = createTextField(100);
        grid.add(boilingPointField, 1, 3);

        grid.add(createBoldLabel("Greenhouse Rise (K):"), 2, 3);
        greenhouseRiseField = createTextField(100);
        grid.add(greenhouseRiseField, 3, 3);

        TitledPane pane = new TitledPane("Temperature", grid);
        pane.setCollapsible(false);
        return pane;
    }

    private TitledPane createAtmosphereCompSection() {
        GridPane grid = createGridPane();

        grid.add(createBoldLabel("Atmosphere Type:"), 0, 0);
        atmosphereTypeField = createTextField(200);
        atmosphereTypeField.setPromptText("e.g., Earth-like");
        grid.add(atmosphereTypeField, 1, 0, 3, 1);

        grid.add(createBoldLabel("Composition:"), 0, 1);
        atmosphereCompField = new TextArea();
        atmosphereCompField.setPrefRowCount(3);
        atmosphereCompField.setPrefWidth(350);
        atmosphereCompField.setPromptText("Format: N2:780;O2:210;Ar:9...");
        grid.add(atmosphereCompField, 1, 1, 3, 1);

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
        populationField.setPromptText("e.g., 0");
        grid.add(populationField, 1, 0);

        colonizedCheck = new CheckBox("Colonized");
        grid.add(colonizedCheck, 2, 0);

        grid.add(createBoldLabel("Tech Level:"), 0, 1);
        techLevelField = createTextField(150);
        techLevelField.setPromptText("e.g., 0-15");
        grid.add(techLevelField, 1, 1);

        grid.add(createBoldLabel("Colonization Year:"), 2, 1);
        colonizationYearField = createTextField(100);
        grid.add(colonizationYearField, 3, 1);

        grid.add(createBoldLabel("Polity:"), 0, 2);
        polityField = createTextField(150);
        polityField.setPromptText("e.g., Federation");
        grid.add(polityField, 1, 2);

        grid.add(createBoldLabel("Strategic Importance:"), 2, 2);
        strategicImportanceField = createTextField(100);
        strategicImportanceField.setPromptText("1-10");
        grid.add(strategicImportanceField, 3, 2);

        grid.add(createBoldLabel("Primary Resource:"), 0, 3);
        primaryResourceField = createTextField(150);
        primaryResourceField.setPromptText("e.g., Water, Minerals");
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

        Button createBtn = new Button(isMoon ? "Create Moon" : "Create Planet");
        createBtn.setDefaultButton(true);
        createBtn.setOnAction(this::handleCreate);

        hbox.getChildren().addAll(cancelBtn, createBtn);
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

    private void setDefaults() {
        // Set some reasonable defaults for orbital parameters
        eccentricityField.setText("0.0");
        inclinationField.setText("0.0");
        omegaField.setText("0.0");
        lonAscNodeField.setText("0.0");

        if (isMoon) {
            semiMajorAxisField.setText("0.003");
            orbitalPeriodField.setText("3.5");
            radiusField.setText("0.25");
            massField.setText("0.01");
        } else {
            semiMajorAxisField.setText("1.0");
            orbitalPeriodField.setText("365.0");
            radiusField.setText("1.0");
            massField.setText("1.0");
        }

        // Physical defaults
        densityField.setText("5.5");
        axialTiltField.setText("23.5");
        dayLengthField.setText("24.0");
        surfaceGravityField.setText("1.0");
        surfaceAccelField.setText("9.8");
        escapeVelocityField.setText("11.2");

        // Climate defaults
        surfaceTempField.setText("288");
        surfacePressureField.setText("1.0");
        albedoField.setText("0.3");
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

    // ==================== HANDLERS ====================
    private void handleCreate(ActionEvent event) {
        // Validate required fields
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            showError("Name is required");
            return;
        }

        Double sma = parseDouble(semiMajorAxisField.getText());
        if (sma == null || sma <= 0) {
            showError("Valid semi-major axis is required");
            return;
        }

        // Create the new ExoPlanet
        ExoPlanet planet = new ExoPlanet();
        planet.setId(UUID.randomUUID().toString());
        planet.setName(name);
        planet.setPlanetType(planetTypeCombo.getValue());
        planet.setOrbitalZone(parseInteger(orbitalZoneField.getText()));

        // Set solar system info
        if (solarSystem != null) {
            planet.setSolarSystemId(solarSystem.getSolarSystemId());
            if (solarSystem.getStarDisplayRecord() != null) {
                planet.setStarName(solarSystem.getStarDisplayRecord().getStarName());
                planet.setHostStarId(solarSystem.getStarDisplayRecord().getRecordId());
            }
        }

        // Moon-specific settings
        if (isMoon && parentPlanet != null) {
            planet.setIsMoon(true);
            planet.setParentPlanetId(parentPlanet.getId());
        } else {
            planet.setIsMoon(false);
        }

        // Classification flags
        planet.setHabitable(habitableCheck.isSelected());
        planet.setEarthlike(earthlikeCheck.isSelected());
        planet.setGasGiant(gasGiantCheck.isSelected());
        planet.setHabitableJovian(habitableJovianCheck.isSelected());
        planet.setHabitableMoon(habitableMoonCheck.isSelected());
        planet.setGreenhouseEffect(greenhouseCheck.isSelected());
        planet.setTidallyLocked(tidallyLockedCheck.isSelected());

        // Orbital parameters
        planet.setSemiMajorAxis(sma);
        planet.setEccentricity(parseDouble(eccentricityField.getText()));
        planet.setInclination(parseDouble(inclinationField.getText()));
        planet.setOmega(parseDouble(omegaField.getText()));
        planet.setLongitudeOfAscendingNode(parseDouble(lonAscNodeField.getText()));
        planet.setOrbitalPeriod(parseDouble(orbitalPeriodField.getText()));

        // Physical parameters
        planet.setRadius(parseDouble(radiusField.getText()));
        planet.setMass(parseDouble(massField.getText()));
        planet.setDensity(parseDouble(densityField.getText()));
        planet.setCoreRadius(parseDouble(coreRadiusField.getText()));
        planet.setAxialTilt(parseDouble(axialTiltField.getText()));
        planet.setDayLength(parseDouble(dayLengthField.getText()));
        planet.setSurfaceGravity(parseDouble(surfaceGravityField.getText()));
        planet.setSurfaceAcceleration(parseDouble(surfaceAccelField.getText()));
        planet.setEscapeVelocity(parseDouble(escapeVelocityField.getText()));

        // Climate parameters
        planet.setHydrosphere(parseDouble(hydrosphereField.getText()));
        planet.setCloudCover(parseDouble(cloudCoverField.getText()));
        planet.setIceCover(parseDouble(iceCoverField.getText()));
        planet.setAlbedo(parseDouble(albedoField.getText()));
        planet.setSurfacePressure(parseDouble(surfacePressureField.getText()));
        planet.setVolatileGasInventory(parseDouble(volatileGasField.getText()));
        planet.setMinimumMolecularWeight(parseDouble(minMolWeightField.getText()));

        // Atmosphere/Temperature parameters
        planet.setSurfaceTemperature(parseDouble(surfaceTempField.getText()));
        planet.setHighTemperature(parseDouble(highTempField.getText()));
        planet.setLowTemperature(parseDouble(lowTempField.getText()));
        planet.setMaxTemperature(parseDouble(maxTempField.getText()));
        planet.setMinTemperature(parseDouble(minTempField.getText()));
        planet.setExosphericTemperature(parseDouble(exosphericTempField.getText()));
        planet.setBoilingPoint(parseDouble(boilingPointField.getText()));
        planet.setGreenhouseRise(parseDouble(greenhouseRiseField.getText()));
        planet.setAtmosphereType(atmosphereTypeField.getText().trim());
        planet.setAtmosphereComposition(atmosphereCompField.getText().trim());

        // Also set tempCalculated for display purposes
        planet.setTempCalculated(parseDouble(surfaceTempField.getText()));

        // Sci-Fi parameters
        planet.setPopulation(parseLong(populationField.getText()));
        planet.setTechLevel(parseInteger(techLevelField.getText()));
        planet.setColonizationYear(parseInteger(colonizationYearField.getText()));
        planet.setPolity(polityField.getText().trim());
        planet.setPrimaryResource(primaryResourceField.getText().trim());
        planet.setStrategicImportance(parseInteger(strategicImportanceField.getText()));
        planet.setColonized(colonizedCheck.isSelected());
        planet.setNotes(notesField.getText().trim());

        // Mark as user-created
        planet.setPlanetStatus("User-Created");
        planet.setDetectionType("User-Created");

        log.info("Created new {}: {} with SMA={} AU",
                isMoon ? "moon" : "planet", name, sma);

        setResult(planet);
        closeDialog();
    }

    private void handleCancel(ActionEvent event) {
        setResult(null);
        closeDialog();
    }

    private void handleClose(WindowEvent event) {
        setResult(null);
    }

    private void closeDialog() {
        // Use window hide to ensure dialog closes properly
        if (getDialogPane().getScene() != null && getDialogPane().getScene().getWindow() != null) {
            getDialogPane().getScene().getWindow().hide();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
