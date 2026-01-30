package com.teamgannon.trips.dialogs.nebula;

import com.teamgannon.trips.jpa.model.Nebula;
import com.teamgannon.trips.jpa.model.NebulaType;
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

/**
 * Dialog for creating and editing nebulae.
 * <p>
 * Provides controls for all nebula properties including:
 * - Identity (name, type, dataset)
 * - Position (x, y, z coordinates)
 * - Size (inner/outer radius)
 * - Colors (primary, secondary)
 * - Procedural parameters (radialPower, noiseStrength, noiseOctaves)
 * - Animation settings
 */
@Slf4j
public class NebulaEditorDialog extends Dialog<Nebula> {

    private final Nebula existingNebula;
    private final String datasetName;
    private final boolean isEditMode;

    // === IDENTITY SECTION ===
    private TextField nameField;
    private ComboBox<NebulaType> typeCombo;
    private TextField datasetField;
    private TextField catalogIdField;
    private TextField sourceCatalogField;

    // === POSITION SECTION ===
    private TextField centerXField;
    private TextField centerYField;
    private TextField centerZField;

    // === SIZE SECTION ===
    private TextField innerRadiusField;
    private TextField outerRadiusField;

    // === COLORS SECTION ===
    private ColorPicker primaryColorPicker;
    private ColorPicker secondaryColorPicker;
    private Slider opacitySlider;
    private Label opacityLabel;

    // === PROCEDURAL SECTION ===
    private Slider radialPowerSlider;
    private Label radialPowerLabel;
    private Slider noiseStrengthSlider;
    private Label noiseStrengthLabel;
    private Spinner<Integer> noiseOctavesSpinner;
    private TextField seedField;
    private TextField particleDensityField;
    private TextField numElementsOverrideField;

    // === ANIMATION SECTION ===
    private CheckBox enableAnimationCheck;
    private TextField angularSpeedField;

    // === NOTES SECTION ===
    private TextArea notesField;

    /**
     * Create a dialog for a new nebula.
     *
     * @param datasetName the dataset to add the nebula to
     */
    public NebulaEditorDialog(String datasetName) {
        this(null, datasetName);
    }

    /**
     * Create a dialog for editing an existing nebula.
     *
     * @param nebula      the nebula to edit (null for create mode)
     * @param datasetName the dataset name
     */
    public NebulaEditorDialog(Nebula nebula, String datasetName) {
        this.existingNebula = nebula;
        this.datasetName = datasetName;
        this.isEditMode = nebula != null;

        setTitle(isEditMode ? "Edit Nebula: " + nebula.getName() : "Create New Nebula");

        // Create tabbed pane
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // General tab
        Tab generalTab = new Tab("General");
        generalTab.setContent(createGeneralContent());
        tabPane.getTabs().add(generalTab);

        // Appearance tab
        Tab appearanceTab = new Tab("Appearance");
        appearanceTab.setContent(createAppearanceContent());
        tabPane.getTabs().add(appearanceTab);

        // Procedural tab
        Tab proceduralTab = new Tab("Procedural");
        proceduralTab.setContent(createProceduralContent());
        tabPane.getTabs().add(proceduralTab);

        VBox mainBox = new VBox(10);
        mainBox.setPadding(new Insets(10));
        mainBox.getChildren().addAll(tabPane, createButtonBar());

        this.getDialogPane().setContent(mainBox);
        this.getDialogPane().setPrefWidth(500);
        this.getDialogPane().setPrefHeight(480);

        DialogUtils.bindCloseHandler(this, this::handleClose);

        // Initialize values
        if (isEditMode) {
            populateFromNebula(nebula);
        } else {
            setDefaults();
        }
    }

    // ==================== GENERAL TAB ====================
    private VBox createGeneralContent() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        // Identity section
        vbox.getChildren().add(createIdentitySection());

        // Position section
        vbox.getChildren().add(createPositionSection());

        // Size section
        vbox.getChildren().add(createSizeSection());

        return vbox;
    }

    private TitledPane createIdentitySection() {
        GridPane grid = createGridPane();

        grid.add(createBoldLabel("Name:"), 0, 0);
        nameField = createTextField(200);
        nameField.setPromptText("e.g., Orion Nebula");
        grid.add(nameField, 1, 0);

        grid.add(createBoldLabel("Type:"), 2, 0);
        typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(NebulaType.values());
        typeCombo.setValue(NebulaType.EMISSION);
        typeCombo.setOnAction(e -> applyTypeDefaults());
        grid.add(typeCombo, 3, 0);

        grid.add(createBoldLabel("Dataset:"), 0, 1);
        datasetField = createTextField(200);
        datasetField.setText(datasetName);
        datasetField.setEditable(false);
        datasetField.setStyle("-fx-background-color: #e8e8e8;");
        grid.add(datasetField, 1, 1);

        grid.add(createBoldLabel("Catalog ID:"), 0, 2);
        catalogIdField = createTextField(100);
        catalogIdField.setPromptText("e.g., M42, NGC 1976");
        grid.add(catalogIdField, 1, 2);

        grid.add(createBoldLabel("Source:"), 2, 2);
        sourceCatalogField = createTextField(100);
        sourceCatalogField.setPromptText("e.g., Messier, User");
        grid.add(sourceCatalogField, 3, 2);

        TitledPane pane = new TitledPane("Identity", grid);
        pane.setCollapsible(false);
        return pane;
    }

    private TitledPane createPositionSection() {
        GridPane grid = createGridPane();

        grid.add(createBoldLabel("X (light-years):"), 0, 0);
        centerXField = createTextField(100);
        centerXField.setPromptText("e.g., 10.5");
        grid.add(centerXField, 1, 0);

        grid.add(createBoldLabel("Y (light-years):"), 2, 0);
        centerYField = createTextField(100);
        centerYField.setPromptText("e.g., -5.2");
        grid.add(centerYField, 3, 0);

        grid.add(createBoldLabel("Z (light-years):"), 0, 1);
        centerZField = createTextField(100);
        centerZField.setPromptText("e.g., 3.0");
        grid.add(centerZField, 1, 1);

        TitledPane pane = new TitledPane("Position (relative to Sol)", grid);
        pane.setCollapsible(false);
        return pane;
    }

    private TitledPane createSizeSection() {
        GridPane grid = createGridPane();

        grid.add(createBoldLabel("Inner Radius (ly):"), 0, 0);
        innerRadiusField = createTextField(100);
        innerRadiusField.setPromptText("e.g., 0 or 1.0");
        grid.add(innerRadiusField, 1, 0);

        grid.add(createBoldLabel("Outer Radius (ly):"), 2, 0);
        outerRadiusField = createTextField(100);
        outerRadiusField.setPromptText("e.g., 5.0");
        grid.add(outerRadiusField, 3, 0);

        TitledPane pane = new TitledPane("Size", grid);
        pane.setCollapsible(false);
        return pane;
    }

    // ==================== APPEARANCE TAB ====================
    private VBox createAppearanceContent() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        // Colors section
        vbox.getChildren().add(createColorsSection());

        // Animation section
        vbox.getChildren().add(createAnimationSection());

        // Notes section
        vbox.getChildren().add(createNotesSection());

        return vbox;
    }

    private TitledPane createColorsSection() {
        GridPane grid = createGridPane();

        grid.add(createBoldLabel("Primary Color:"), 0, 0);
        primaryColorPicker = new ColorPicker(Color.rgb(255, 100, 150));
        grid.add(primaryColorPicker, 1, 0);

        grid.add(createBoldLabel("Secondary Color:"), 2, 0);
        secondaryColorPicker = new ColorPicker(Color.rgb(100, 200, 255));
        grid.add(secondaryColorPicker, 3, 0);

        grid.add(createBoldLabel("Opacity:"), 0, 1);
        opacitySlider = new Slider(0.1, 1.0, 0.7);
        opacitySlider.setShowTickLabels(true);
        opacitySlider.setShowTickMarks(true);
        opacitySlider.setMajorTickUnit(0.2);
        opacitySlider.setPrefWidth(150);
        opacityLabel = new Label(String.format("%.2f", opacitySlider.getValue()));
        opacitySlider.valueProperty().addListener((obs, old, val) ->
                opacityLabel.setText(String.format("%.2f", val.doubleValue())));
        HBox opacityBox = new HBox(5, opacitySlider, opacityLabel);
        grid.add(opacityBox, 1, 1, 3, 1);

        TitledPane pane = new TitledPane("Colors", grid);
        pane.setCollapsible(false);
        return pane;
    }

    private TitledPane createAnimationSection() {
        GridPane grid = createGridPane();

        enableAnimationCheck = new CheckBox("Enable Animation");
        enableAnimationCheck.setSelected(true);
        grid.add(enableAnimationCheck, 0, 0, 2, 1);

        grid.add(createBoldLabel("Angular Speed:"), 0, 1);
        angularSpeedField = createTextField(100);
        angularSpeedField.setPromptText("e.g., 0.0003");
        grid.add(angularSpeedField, 1, 1);

        TitledPane pane = new TitledPane("Animation", grid);
        pane.setCollapsible(false);
        return pane;
    }

    private TitledPane createNotesSection() {
        GridPane grid = createGridPane();

        notesField = new TextArea();
        notesField.setPrefRowCount(3);
        notesField.setPrefWidth(400);
        notesField.setPromptText("Optional notes about this nebula...");
        grid.add(notesField, 0, 0);

        TitledPane pane = new TitledPane("Notes", grid);
        pane.setCollapsible(false);
        return pane;
    }

    // ==================== PROCEDURAL TAB ====================
    private VBox createProceduralContent() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        // Procedural parameters section
        vbox.getChildren().add(createProceduralParamsSection());

        // Particle density section
        vbox.getChildren().add(createParticleDensitySection());

        return vbox;
    }

    private TitledPane createProceduralParamsSection() {
        GridPane grid = createGridPane();

        // Radial power (0.3 = dense core, 0.7 = shell-like)
        grid.add(createBoldLabel("Radial Power:"), 0, 0);
        radialPowerSlider = new Slider(0.1, 1.0, 0.4);
        radialPowerSlider.setShowTickLabels(true);
        radialPowerSlider.setMajorTickUnit(0.2);
        radialPowerSlider.setPrefWidth(150);
        radialPowerLabel = new Label(String.format("%.2f", radialPowerSlider.getValue()));
        radialPowerSlider.valueProperty().addListener((obs, old, val) ->
                radialPowerLabel.setText(String.format("%.2f", val.doubleValue())));
        HBox radialBox = new HBox(5, radialPowerSlider, radialPowerLabel);
        grid.add(radialBox, 1, 0, 3, 1);

        Label radialHint = new Label("Low = dense core, High = shell-like");
        radialHint.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");
        grid.add(radialHint, 1, 1, 3, 1);

        // Noise strength (filament intensity)
        grid.add(createBoldLabel("Noise Strength:"), 0, 2);
        noiseStrengthSlider = new Slider(0.0, 1.0, 0.4);
        noiseStrengthSlider.setShowTickLabels(true);
        noiseStrengthSlider.setMajorTickUnit(0.2);
        noiseStrengthSlider.setPrefWidth(150);
        noiseStrengthLabel = new Label(String.format("%.2f", noiseStrengthSlider.getValue()));
        noiseStrengthSlider.valueProperty().addListener((obs, old, val) ->
                noiseStrengthLabel.setText(String.format("%.2f", val.doubleValue())));
        HBox noiseBox = new HBox(5, noiseStrengthSlider, noiseStrengthLabel);
        grid.add(noiseBox, 1, 2, 3, 1);

        Label noiseHint = new Label("Controls filamentary structure intensity");
        noiseHint.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");
        grid.add(noiseHint, 1, 3, 3, 1);

        // Noise octaves (detail level)
        grid.add(createBoldLabel("Noise Octaves:"), 0, 4);
        noiseOctavesSpinner = new Spinner<>(1, 6, 3);
        noiseOctavesSpinner.setEditable(true);
        noiseOctavesSpinner.setPrefWidth(80);
        grid.add(noiseOctavesSpinner, 1, 4);

        Label octavesHint = new Label("Detail level (1-6)");
        octavesHint.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");
        grid.add(octavesHint, 2, 4, 2, 1);

        // Seed
        grid.add(createBoldLabel("Seed:"), 0, 5);
        seedField = createTextField(120);
        seedField.setPromptText("Random if empty");
        grid.add(seedField, 1, 5);

        Button randomSeedBtn = new Button("Randomize");
        randomSeedBtn.setOnAction(e -> seedField.setText(String.valueOf(System.currentTimeMillis())));
        grid.add(randomSeedBtn, 2, 5);

        TitledPane pane = new TitledPane("Procedural Generation", grid);
        pane.setCollapsible(false);
        return pane;
    }

    private TitledPane createParticleDensitySection() {
        GridPane grid = createGridPane();

        grid.add(createBoldLabel("Particle Density:"), 0, 0);
        particleDensityField = createTextField(100);
        particleDensityField.setPromptText("e.g., 100.0");
        grid.add(particleDensityField, 1, 0);

        Label densityHint = new Label("Particles per cubic light-year");
        densityHint.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");
        grid.add(densityHint, 2, 0, 2, 1);

        grid.add(createBoldLabel("Particle Override:"), 0, 1);
        numElementsOverrideField = createTextField(100);
        numElementsOverrideField.setPromptText("Leave empty for auto");
        grid.add(numElementsOverrideField, 1, 1);

        Label overrideHint = new Label("Explicit count (overrides density)");
        overrideHint.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");
        grid.add(overrideHint, 2, 1, 2, 1);

        TitledPane pane = new TitledPane("Particle Count", grid);
        pane.setCollapsible(false);
        return pane;
    }

    // ==================== BUTTON BAR ====================
    private HBox createButtonBar() {
        HBox hbox = new HBox(10);
        hbox.setAlignment(Pos.CENTER_RIGHT);
        hbox.setPadding(new Insets(10, 0, 0, 0));

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(this::handleCancel);

        Button saveBtn = new Button(isEditMode ? "Save" : "Create");
        saveBtn.setDefaultButton(true);
        saveBtn.setOnAction(this::handleSave);

        hbox.getChildren().addAll(cancelBtn, saveBtn);
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
        typeCombo.setValue(NebulaType.EMISSION);
        centerXField.setText("0.0");
        centerYField.setText("0.0");
        centerZField.setText("0.0");
        innerRadiusField.setText("0.5");
        outerRadiusField.setText("5.0");
        particleDensityField.setText("100.0");
        angularSpeedField.setText("0.0003");
        sourceCatalogField.setText("User");
        seedField.setText(String.valueOf(System.currentTimeMillis()));

        applyTypeDefaults();
    }

    private void applyTypeDefaults() {
        NebulaType type = typeCombo.getValue();
        if (type == null) return;

        // Update colors based on type
        try {
            primaryColorPicker.setValue(Color.web(type.getDefaultPrimaryColor()));
            secondaryColorPicker.setValue(Color.web(type.getDefaultSecondaryColor()));
        } catch (Exception e) {
            log.warn("Could not parse type default colors", e);
        }

        // Update procedural params based on type
        radialPowerSlider.setValue(type.getDefaultRadialPower());
        noiseStrengthSlider.setValue(type.getDefaultNoiseStrength());
        noiseOctavesSpinner.getValueFactory().setValue(type.getDefaultNoiseOctaves());
    }

    private void populateFromNebula(Nebula nebula) {
        nameField.setText(nebula.getName());
        typeCombo.setValue(nebula.getType());
        datasetField.setText(nebula.getDataSetName());
        catalogIdField.setText(nebula.getCatalogId() != null ? nebula.getCatalogId() : "");
        sourceCatalogField.setText(nebula.getSourceCatalog() != null ? nebula.getSourceCatalog() : "");

        centerXField.setText(String.valueOf(nebula.getCenterX()));
        centerYField.setText(String.valueOf(nebula.getCenterY()));
        centerZField.setText(String.valueOf(nebula.getCenterZ()));

        innerRadiusField.setText(String.valueOf(nebula.getInnerRadius()));
        outerRadiusField.setText(String.valueOf(nebula.getOuterRadius()));

        try {
            primaryColorPicker.setValue(Color.web(nebula.getPrimaryColor()));
        } catch (Exception e) {
            primaryColorPicker.setValue(Color.rgb(255, 100, 150));
        }

        try {
            secondaryColorPicker.setValue(Color.web(nebula.getSecondaryColor()));
        } catch (Exception e) {
            secondaryColorPicker.setValue(Color.rgb(100, 200, 255));
        }

        opacitySlider.setValue(nebula.getOpacity());
        radialPowerSlider.setValue(nebula.getRadialPower());
        noiseStrengthSlider.setValue(nebula.getNoiseStrength());
        noiseOctavesSpinner.getValueFactory().setValue(nebula.getNoiseOctaves());
        seedField.setText(String.valueOf(nebula.getSeed()));
        particleDensityField.setText(String.valueOf(nebula.getParticleDensity()));

        if (nebula.getNumElementsOverride() != null) {
            numElementsOverrideField.setText(String.valueOf(nebula.getNumElementsOverride()));
        }

        enableAnimationCheck.setSelected(nebula.isEnableAnimation());
        angularSpeedField.setText(String.valueOf(nebula.getBaseAngularSpeed()));

        notesField.setText(nebula.getNotes() != null ? nebula.getNotes() : "");
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

    private String colorToHex(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    // ==================== HANDLERS ====================
    private void handleSave(ActionEvent event) {
        // Validate required fields
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            showError("Name is required");
            return;
        }

        Double outerRadius = parseDouble(outerRadiusField.getText());
        if (outerRadius == null || outerRadius <= 0) {
            showError("Valid outer radius is required");
            return;
        }

        Double innerRadius = parseDouble(innerRadiusField.getText());
        if (innerRadius == null || innerRadius < 0) {
            innerRadius = 0.0;
        }

        if (innerRadius >= outerRadius) {
            showError("Inner radius must be less than outer radius");
            return;
        }

        Double centerX = parseDouble(centerXField.getText());
        Double centerY = parseDouble(centerYField.getText());
        Double centerZ = parseDouble(centerZField.getText());

        if (centerX == null || centerY == null || centerZ == null) {
            showError("Valid position coordinates are required");
            return;
        }

        // Create or update nebula
        Nebula nebula;
        if (isEditMode) {
            nebula = existingNebula;
        } else {
            nebula = new Nebula(name, typeCombo.getValue(), datasetName,
                    centerX, centerY, centerZ, outerRadius);
        }

        // Set all properties
        nebula.setName(name);
        nebula.setType(typeCombo.getValue());
        nebula.setCenterX(centerX);
        nebula.setCenterY(centerY);
        nebula.setCenterZ(centerZ);
        nebula.setInnerRadius(innerRadius);
        nebula.setOuterRadius(outerRadius);

        nebula.setPrimaryColor(colorToHex(primaryColorPicker.getValue()));
        nebula.setSecondaryColor(colorToHex(secondaryColorPicker.getValue()));
        nebula.setOpacity(opacitySlider.getValue());

        nebula.setRadialPower(radialPowerSlider.getValue());
        nebula.setNoiseStrength(noiseStrengthSlider.getValue());
        nebula.setNoiseOctaves(noiseOctavesSpinner.getValue());

        Long seed = parseLong(seedField.getText());
        nebula.setSeed(seed != null ? seed : System.currentTimeMillis());

        Double density = parseDouble(particleDensityField.getText());
        nebula.setParticleDensity(density != null ? density : 100.0);

        Integer override = parseInteger(numElementsOverrideField.getText());
        nebula.setNumElementsOverride(override);

        nebula.setEnableAnimation(enableAnimationCheck.isSelected());
        Double angularSpeed = parseDouble(angularSpeedField.getText());
        nebula.setBaseAngularSpeed(angularSpeed != null ? angularSpeed : 0.0003);

        String catalogId = catalogIdField.getText().trim();
        nebula.setCatalogId(catalogId.isEmpty() ? null : catalogId);

        String sourceCatalog = sourceCatalogField.getText().trim();
        nebula.setSourceCatalog(sourceCatalog.isEmpty() ? "User" : sourceCatalog);

        String notes = notesField.getText().trim();
        nebula.setNotes(notes.isEmpty() ? null : notes);

        log.info("{} nebula '{}' at ({}, {}, {}) with radius {}",
                isEditMode ? "Updated" : "Created",
                name, centerX, centerY, centerZ, outerRadius);

        setResult(nebula);
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
