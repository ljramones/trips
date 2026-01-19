package com.teamgannon.trips.dialogs.solarsystem;

import com.teamgannon.trips.planetarymodelling.procedural.AdjacencyGraph;
import com.teamgannon.trips.planetarymodelling.procedural.BoundaryDetector;
import com.teamgannon.trips.planetarymodelling.procedural.BoundaryDetector.BoundaryType;
import com.teamgannon.trips.planetarymodelling.procedural.ClimateCalculator;
import com.teamgannon.trips.planetarymodelling.procedural.ElevationCalculator;
import com.teamgannon.trips.planetarymodelling.procedural.GenerationProgressListener;
import com.teamgannon.trips.planetarymodelling.procedural.JavaFxPlanetMeshConverter;
import com.teamgannon.trips.planetarymodelling.procedural.PlateAssigner;
import com.teamgannon.trips.planetarymodelling.procedural.PlanetConfig;
import com.teamgannon.trips.planetarymodelling.procedural.PlanetGenerator;
import com.teamgannon.trips.planetarymodelling.procedural.PlanetGenerator.GeneratedPlanet;
import com.teamgannon.trips.planetarymodelling.procedural.Polygon;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

/**
 * Dialog for viewing and interactively generating procedural planet terrain.
 * Features a 3D viewer with a side panel for generation parameters and visualization controls.
 */
@Slf4j
public class ProceduralPlanetViewerDialog extends Dialog<Void> {

    private static final double SCENE_WIDTH = 550;
    private static final double SCENE_HEIGHT = 380;
    private static final double SIDE_PANEL_WIDTH = 240;
    private static final double PLANET_SCALE = 1.0;
    private static final double INITIAL_CAMERA_DISTANCE = -4.0;

    private final SubScene subScene;
    private final PerspectiveCamera camera;
    private final Group world;
    private final Group planetGroup;

    // Rotation transforms
    private final Rotate rotateX = new Rotate(25, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(25, Rotate.Y_AXIS);

    // Mouse drag state
    private double mouseX, mouseY;
    private double mouseOldX, mouseOldY;

    // Rendering options
    private boolean showWireframe = false;
    private boolean showRivers = false;
    private boolean useColorByHeight = true;
    private boolean showRainfallHeatmap = false;
    private boolean useSmoothTerrain = false;
    private boolean showAtmosphere = true;
    private boolean showPlateBoundaries = false;
    private boolean showClimateZones = false;
    private boolean autoRotate = false;
    private boolean showLakes = true;
    private boolean useFlowAccumulationRivers = true;

    // Animation
    private Timeline rotationAnimation;

    // Atmosphere visualization
    private Sphere atmosphereSphere;

    // Generated planet data (mutable - changes on regeneration)
    private GeneratedPlanet generatedPlanet;
    private final String planetName;
    private double[] rainfall;
    private double[] preciseHeights;
    private AdjacencyGraph adjacency;
    private PlateAssigner.PlateAssignment plateAssignment;
    private BoundaryDetector.BoundaryAnalysis boundaryAnalysis;

    // Generation parameters (editable)
    private long currentSeed;
    private int currentPlateCount;
    private double currentWaterFraction;
    private int currentErosionIterations;
    private double currentRiverThreshold;
    private double currentHeightScale;
    private boolean currentUseContinuousHeights;
    private double currentReliefMin;
    private double currentReliefMax;
    private PlanetConfig.Size currentSize;
    private ClimateCalculator.ClimateModel currentClimateModel;

    // UI controls that need updating after regeneration
    private TextField seedField;
    private Spinner<Integer> plateSpinner;
    private Slider waterSlider;
    private Label waterLabel;
    private Spinner<Integer> erosionSpinner;
    private Slider riverSlider;
    private Label riverLabel;
    private Slider heightSlider;
    private Label heightLabel;
    private CheckBox continuousHeightsCheckBox;
    private Spinner<Double> reliefMinSpinner;
    private Spinner<Double> reliefMaxSpinner;
    private ComboBox<PlanetConfig.Size> sizeCombo;
    private ComboBox<ClimateCalculator.ClimateModel> climateCombo;
    private Button regenerateButton;
    private ProgressBar progressBar;
    private Label progressLabel;
    private Label infoPolygonsLabel;
    private Label infoRiversLabel;
    private Label infoPlatesLabel;
    private CheckBox riversCheckBox;
    private CheckBox lakesCheckBox;
    private CheckBox flowRiversCheckBox;
    private CheckBox rainfallCheckBox;
    private CheckBox smoothCheckBox;
    private CheckBox plateBoundariesCheckBox;
    private CheckBox climateZonesCheckBox;

    /**
     * Create a new procedural planet viewer dialog.
     *
     * @param planetName The name of the planet being viewed
     * @param planet     The generated planet data
     */
    public ProceduralPlanetViewerDialog(String planetName, GeneratedPlanet planet) {
        this.planetName = planetName;
        this.generatedPlanet = planet;
        updatePlanetData(planet);

        // Initialize generation parameters from current planet config
        PlanetConfig config = planet.config();
        this.currentSeed = config != null ? config.seed() : System.nanoTime();
        this.currentPlateCount = config != null ? config.plateCount() : 12;
        this.currentWaterFraction = config != null ? config.waterFraction() : 0.66;
        this.currentErosionIterations = config != null ? config.erosionIterations() : 5;
        this.currentRiverThreshold = config != null ? config.riverSourceThreshold() : 0.7;
        this.currentHeightScale = config != null ? config.heightScaleMultiplier() : 1.0;
        this.currentUseContinuousHeights = config != null && config.useContinuousHeights();
        this.currentReliefMin = config != null ? config.continuousReliefMin() : -4.0;
        this.currentReliefMax = config != null ? config.continuousReliefMax() : 4.0;
        this.currentSize = config != null ? deriveSizeFromN(config.n()) : PlanetConfig.Size.STANDARD;
        this.currentClimateModel = config != null ? config.climateModel() : ClimateCalculator.ClimateModel.SIMPLE_LATITUDE;

        setTitle("Terrain: " + planetName);
        setResizable(false);  // Fixed size dialog

        // Create 3D scene
        world = new Group();
        planetGroup = new Group();
        world.getChildren().add(planetGroup);

        // Apply rotation transforms to world
        world.getTransforms().addAll(rotateX, rotateY);

        // Create camera
        camera = new PerspectiveCamera(true);
        camera.setNearClip(0.01);
        camera.setFarClip(100);
        camera.setTranslateZ(INITIAL_CAMERA_DISTANCE);

        // Create SubScene
        subScene = new SubScene(world, SCENE_WIDTH, SCENE_HEIGHT, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.rgb(10, 10, 25));
        subScene.setCamera(camera);

        // Add lighting
        addLighting();

        // Render the planet mesh
        renderPlanet();

        // Add atmosphere glow
        createAtmosphere();

        // Set up mouse interaction
        setupMouseHandlers();

        // Wrap SubScene in a simple container with fixed size
        StackPane viewPane = new StackPane();
        viewPane.getChildren().add(subScene);
        viewPane.setMinSize(SCENE_WIDTH, SCENE_HEIGHT);
        viewPane.setMaxSize(SCENE_WIDTH, SCENE_HEIGHT);
        viewPane.setPrefSize(SCENE_WIDTH, SCENE_HEIGHT);

        // Create side panel
        VBox sidePanel = createSidePanel();
        sidePanel.setMinWidth(SIDE_PANEL_WIDTH);
        sidePanel.setPrefWidth(SIDE_PANEL_WIDTH);

        // Main layout: 3D view on left, side panel on right
        HBox mainLayout = new HBox(10);
        mainLayout.setPadding(new Insets(10));
        mainLayout.getChildren().addAll(viewPane, sidePanel);

        getDialogPane().setContent(mainLayout);
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        // Set dialog size - use preferred size to prevent it from expanding to fill screen
        double dialogWidth = SCENE_WIDTH + SIDE_PANEL_WIDTH + 60;
        double dialogHeight = SCENE_HEIGHT + 80;
        getDialogPane().setPrefWidth(dialogWidth);
        getDialogPane().setPrefHeight(dialogHeight);
        getDialogPane().setMaxHeight(dialogHeight + 50);  // Allow slight expansion but not full screen

        // Stop animation when dialog closes
        setOnCloseRequest(event -> stopAnimation());
        setResultConverter(button -> {
            stopAnimation();
            return null;
        });

        log.info("Created procedural planet viewer for: {}", planetName);
    }

    /**
     * Update local references when planet data changes.
     */
    private void updatePlanetData(GeneratedPlanet planet) {
        this.generatedPlanet = planet;
        this.rainfall = planet.rainfall();
        this.preciseHeights = planet.preciseHeights();
        this.adjacency = planet.adjacency();
        this.plateAssignment = planet.plateAssignment();
        this.boundaryAnalysis = planet.boundaryAnalysis();
    }

    /**
     * Create the side panel with all control sections.
     */
    private VBox createSidePanel() {
        VBox sidePanel = new VBox(10);
        sidePanel.setPadding(new Insets(5));

        // Use a ScrollPane in case content is too tall
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox content = new VBox(10);
        content.setPadding(new Insets(5));

        content.getChildren().addAll(
            createGenerationSection(),
            new Separator(),
            createViewSection(),
            new Separator(),
            createOverlaysSection(),
            new Separator(),
            createRenderSection(),
            new Separator(),
            createInfoSection(),
            new Separator(),
            createLegendSection()
        );

        scrollPane.setContent(content);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        sidePanel.getChildren().add(scrollPane);

        return sidePanel;
    }

    // Standard style for labels in side panel
    private static final String LABEL_STYLE = "-fx-text-fill: #333333; -fx-font-size: 11;";
    private static final String SMALL_LABEL_STYLE = "-fx-text-fill: #333333; -fx-font-size: 10;";
    private static final String INFO_LABEL_STYLE = "-fx-text-fill: #555555; -fx-font-size: 10;";

    /**
     * Create the GENERATION section with parameter controls.
     */
    private TitledPane createGenerationSection() {
        VBox content = new VBox(8);
        content.setPadding(new Insets(5));

        // Seed
        HBox seedRow = new HBox(5);
        seedRow.setAlignment(Pos.CENTER_LEFT);
        Label seedLabel = new Label("Seed:");
        seedLabel.setStyle(LABEL_STYLE);
        seedField = new TextField(String.valueOf(currentSeed));
        seedField.setPrefWidth(100);
        seedField.setStyle("-fx-font-size: 10;");
        Button randomizeButton = new Button("🎲");
        randomizeButton.setTooltip(new Tooltip("Generate random seed"));
        randomizeButton.setOnAction(e -> {
            currentSeed = new Random().nextLong();
            seedField.setText(String.valueOf(currentSeed));
        });
        seedRow.getChildren().addAll(seedLabel, seedField, randomizeButton);

        // Size
        HBox sizeRow = new HBox(5);
        sizeRow.setAlignment(Pos.CENTER_LEFT);
        Label sizeLabel = new Label("Size:");
        sizeLabel.setStyle(LABEL_STYLE);
        sizeCombo = new ComboBox<>();
        sizeCombo.getItems().addAll(PlanetConfig.Size.values());
        sizeCombo.setValue(currentSize);
        sizeCombo.setPrefWidth(120);
        sizeCombo.setStyle("-fx-font-size: 10;");
        sizeRow.getChildren().addAll(sizeLabel, sizeCombo);

        // Plate count
        HBox plateRow = new HBox(5);
        plateRow.setAlignment(Pos.CENTER_LEFT);
        Label platesLabel = new Label("Plates:");
        platesLabel.setStyle(LABEL_STYLE);
        plateSpinner = new Spinner<>(7, 21, currentPlateCount);
        plateSpinner.setPrefWidth(70);
        plateSpinner.setEditable(true);
        plateSpinner.setStyle("-fx-font-size: 10;");
        plateRow.getChildren().addAll(platesLabel, plateSpinner);

        // Water fraction
        VBox waterBox = new VBox(2);
        HBox waterHeader = new HBox(5);
        waterHeader.setAlignment(Pos.CENTER_LEFT);
        waterLabel = new Label(String.format("Water: %.0f%%", currentWaterFraction * 100));
        waterLabel.setStyle(LABEL_STYLE);
        waterHeader.getChildren().add(waterLabel);
        waterSlider = new Slider(0, 1, currentWaterFraction);
        waterSlider.setShowTickMarks(true);
        waterSlider.setMajorTickUnit(0.25);
        waterSlider.valueProperty().addListener((obs, old, val) -> {
            currentWaterFraction = val.doubleValue();
            waterLabel.setText(String.format("Water: %.0f%%", currentWaterFraction * 100));
        });
        waterBox.getChildren().addAll(waterHeader, waterSlider);

        // Erosion iterations
        HBox erosionRow = new HBox(5);
        erosionRow.setAlignment(Pos.CENTER_LEFT);
        Label erosionLabel = new Label("Erosion:");
        erosionLabel.setStyle(LABEL_STYLE);
        erosionSpinner = new Spinner<>(0, 10, currentErosionIterations);
        erosionSpinner.setPrefWidth(70);
        erosionSpinner.setEditable(true);
        erosionSpinner.setStyle("-fx-font-size: 10;");
        erosionRow.getChildren().addAll(erosionLabel, erosionSpinner);

        // River threshold
        VBox riverBox = new VBox(2);
        HBox riverHeader = new HBox(5);
        riverHeader.setAlignment(Pos.CENTER_LEFT);
        riverLabel = new Label(String.format("River Thresh: %.2f", currentRiverThreshold));
        riverLabel.setStyle(LABEL_STYLE);
        riverHeader.getChildren().add(riverLabel);
        riverSlider = new Slider(0.1, 1.0, currentRiverThreshold);
        riverSlider.setShowTickMarks(true);
        riverSlider.setMajorTickUnit(0.2);
        riverSlider.valueProperty().addListener((obs, old, val) -> {
            currentRiverThreshold = val.doubleValue();
            riverLabel.setText(String.format("River Thresh: %.2f", currentRiverThreshold));
        });
        riverBox.getChildren().addAll(riverHeader, riverSlider);

        // Height scale
        VBox heightBox = new VBox(2);
        HBox heightHeader = new HBox(5);
        heightHeader.setAlignment(Pos.CENTER_LEFT);
        heightLabel = new Label(String.format("Height Scale: %.1f", currentHeightScale));
        heightLabel.setStyle(LABEL_STYLE);
        heightHeader.getChildren().add(heightLabel);
        heightSlider = new Slider(0.5, 3.0, currentHeightScale);
        heightSlider.setShowTickMarks(true);
        heightSlider.setMajorTickUnit(0.5);
        heightSlider.valueProperty().addListener((obs, old, val) -> {
            currentHeightScale = val.doubleValue();
            heightLabel.setText(String.format("Height Scale: %.1f", currentHeightScale));
        });
        heightBox.getChildren().addAll(heightHeader, heightSlider);

        // Continuous heights
        VBox continuousBox = new VBox(4);
        continuousHeightsCheckBox = new CheckBox("Continuous Heights");
        continuousHeightsCheckBox.setSelected(currentUseContinuousHeights);

        HBox reliefRow = new HBox(5);
        reliefRow.setAlignment(Pos.CENTER_LEFT);
        Label reliefLabel = new Label("Relief:");
        reliefLabel.setStyle(LABEL_STYLE);
        reliefMinSpinner = new Spinner<>(
            new SpinnerValueFactory.DoubleSpinnerValueFactory(-6.0, 0.0, currentReliefMin, 0.1));
        reliefMinSpinner.setPrefWidth(70);
        reliefMinSpinner.setEditable(true);
        reliefMinSpinner.setStyle("-fx-font-size: 10;");
        reliefMaxSpinner = new Spinner<>(
            new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 6.0, currentReliefMax, 0.1));
        reliefMaxSpinner.setPrefWidth(70);
        reliefMaxSpinner.setEditable(true);
        reliefMaxSpinner.setStyle("-fx-font-size: 10;");
        reliefRow.getChildren().addAll(reliefLabel, reliefMinSpinner, reliefMaxSpinner);

        continuousHeightsCheckBox.selectedProperty().addListener((obs, old, val) -> {
            reliefMinSpinner.setDisable(!val);
            reliefMaxSpinner.setDisable(!val);
        });
        reliefMinSpinner.setDisable(!currentUseContinuousHeights);
        reliefMaxSpinner.setDisable(!currentUseContinuousHeights);

        continuousBox.getChildren().addAll(continuousHeightsCheckBox, reliefRow);

        // Climate model
        HBox climateRow = new HBox(5);
        climateRow.setAlignment(Pos.CENTER_LEFT);
        Label climateLabel = new Label("Climate:");
        climateLabel.setStyle(LABEL_STYLE);
        climateCombo = new ComboBox<>();
        climateCombo.getItems().addAll(ClimateCalculator.ClimateModel.values());
        climateCombo.setValue(currentClimateModel);
        climateCombo.setPrefWidth(120);
        climateCombo.setStyle("-fx-font-size: 10;");
        climateRow.getChildren().addAll(climateLabel, climateCombo);

        // Regenerate button
        regenerateButton = new Button("Regenerate");
        regenerateButton.setMaxWidth(Double.MAX_VALUE);
        regenerateButton.setStyle("-fx-font-weight: bold;");
        regenerateButton.setOnAction(e -> regeneratePlanet());

        // Progress bar (initially hidden)
        progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setVisible(false);
        progressBar.setManaged(false);

        progressLabel = new Label("");
        progressLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #666666;");
        progressLabel.setVisible(false);
        progressLabel.setManaged(false);

        content.getChildren().addAll(
            seedRow,
            sizeRow,
            plateRow,
            waterBox,
            erosionRow,
            riverBox,
            heightBox,
            continuousBox,
            climateRow,
            regenerateButton,
            progressBar,
            progressLabel
        );

        TitledPane pane = new TitledPane("Generation", content);
        pane.setExpanded(true);
        pane.setCollapsible(true);
        return pane;
    }

    /**
     * Create the VIEW section with zoom and rotation controls.
     */
    private TitledPane createViewSection() {
        VBox content = new VBox(8);
        content.setPadding(new Insets(5));

        // Zoom slider
        VBox zoomBox = new VBox(2);
        Label zoomLabel = new Label("Zoom:");
        zoomLabel.setStyle(LABEL_STYLE);
        Slider zoomSlider = new Slider(-8, -1.5, INITIAL_CAMERA_DISTANCE);
        zoomSlider.setShowTickMarks(true);
        zoomSlider.setMajorTickUnit(2);
        zoomSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            camera.setTranslateZ(newVal.doubleValue());
        });
        zoomBox.getChildren().addAll(zoomLabel, zoomSlider);

        // Auto-rotate checkbox
        CheckBox autoRotateCheckBox = new CheckBox("Auto-spin");
        autoRotateCheckBox.setStyle(LABEL_STYLE);
        autoRotateCheckBox.setSelected(autoRotate);
        autoRotateCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            setAutoRotate(newVal);
        });

        // Reset view button
        Button resetButton = new Button("Reset View");
        resetButton.setMaxWidth(Double.MAX_VALUE);
        resetButton.setOnAction(e -> {
            resetView();
            autoRotateCheckBox.setSelected(false);
            setAutoRotate(false);
        });

        content.getChildren().addAll(zoomBox, autoRotateCheckBox, resetButton);

        TitledPane pane = new TitledPane("View", content);
        pane.setExpanded(true);
        pane.setCollapsible(true);
        return pane;
    }

    /**
     * Create the OVERLAYS section with toggle options.
     */
    private TitledPane createOverlaysSection() {
        VBox content = new VBox(5);
        content.setPadding(new Insets(5));

        // Rivers checkbox
        int riverCount = generatedPlanet.rivers() != null ? generatedPlanet.rivers().size() : 0;
        riversCheckBox = new CheckBox("Rivers (" + riverCount + ")");
        riversCheckBox.setStyle(LABEL_STYLE);
        riversCheckBox.setSelected(showRivers);
        riversCheckBox.setDisable(riverCount == 0);
        riversCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            showRivers = newVal;
            renderPlanet();
        });

        // Lakes checkbox
        boolean hasLakes = hasLakes();
        lakesCheckBox = new CheckBox("Lakes");
        lakesCheckBox.setStyle(LABEL_STYLE);
        lakesCheckBox.setSelected(showLakes);
        lakesCheckBox.setDisable(!hasLakes);
        lakesCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            showLakes = newVal;
            renderPlanet();
        });

        // Flow-scaled rivers checkbox
        boolean hasFlow = hasFlowAccumulation();
        flowRiversCheckBox = new CheckBox("Flow-Scaled Rivers");
        flowRiversCheckBox.setStyle(LABEL_STYLE);
        flowRiversCheckBox.setSelected(useFlowAccumulationRivers);
        flowRiversCheckBox.setDisable(!hasFlow);
        flowRiversCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            useFlowAccumulationRivers = newVal;
            renderPlanet();
        });

        // Plate boundaries checkbox
        boolean hasPlateData = plateAssignment != null && boundaryAnalysis != null;
        plateBoundariesCheckBox = new CheckBox("Plate Boundaries");
        plateBoundariesCheckBox.setStyle(LABEL_STYLE);
        plateBoundariesCheckBox.setSelected(showPlateBoundaries);
        plateBoundariesCheckBox.setDisable(!hasPlateData);
        plateBoundariesCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            showPlateBoundaries = newVal;
            renderPlanet();
        });

        // Climate zones checkbox
        boolean hasClimateData = generatedPlanet.climates() != null && generatedPlanet.climates().length > 0;
        climateZonesCheckBox = new CheckBox("Climate Zones");
        climateZonesCheckBox.setStyle(LABEL_STYLE);
        climateZonesCheckBox.setSelected(showClimateZones);
        climateZonesCheckBox.setDisable(!hasClimateData);
        climateZonesCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            showClimateZones = newVal;
            renderPlanet();
        });

        // Atmosphere checkbox
        CheckBox atmosphereCheckBox = new CheckBox("Atmosphere");
        atmosphereCheckBox.setStyle(LABEL_STYLE);
        atmosphereCheckBox.setSelected(showAtmosphere);
        atmosphereCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            showAtmosphere = newVal;
            updateAtmosphere();
        });

        content.getChildren().addAll(
            riversCheckBox, lakesCheckBox, flowRiversCheckBox,
            plateBoundariesCheckBox, climateZonesCheckBox, atmosphereCheckBox);

        TitledPane pane = new TitledPane("Overlays", content);
        pane.setExpanded(true);
        pane.setCollapsible(true);
        return pane;
    }

    /**
     * Create the RENDER section with display mode options.
     */
    private TitledPane createRenderSection() {
        VBox content = new VBox(5);
        content.setPadding(new Insets(5));

        // Render mode radio buttons
        ToggleGroup renderGroup = new ToggleGroup();

        RadioButton terrainRadio = new RadioButton("Terrain Colors");
        terrainRadio.setStyle(LABEL_STYLE);
        terrainRadio.setToggleGroup(renderGroup);
        terrainRadio.setSelected(useColorByHeight);

        boolean hasRainfall = rainfall != null && rainfall.length > 0;
        RadioButton rainfallRadio = new RadioButton("Rainfall Heatmap");
        rainfallRadio.setStyle(LABEL_STYLE);
        rainfallRadio.setToggleGroup(renderGroup);
        rainfallRadio.setSelected(showRainfallHeatmap);
        rainfallRadio.setDisable(!hasRainfall);

        renderGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == terrainRadio) {
                useColorByHeight = true;
                showRainfallHeatmap = false;
            } else if (newVal == rainfallRadio) {
                useColorByHeight = false;
                showRainfallHeatmap = true;
            }
            renderPlanet();
        });

        // Smooth terrain checkbox
        boolean hasPreciseHeights = preciseHeights != null && preciseHeights.length > 0;
        smoothCheckBox = new CheckBox("Smooth Terrain");
        smoothCheckBox.setStyle(LABEL_STYLE);
        smoothCheckBox.setSelected(useSmoothTerrain);
        smoothCheckBox.setDisable(!hasPreciseHeights);
        smoothCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            useSmoothTerrain = newVal;
            renderPlanet();
        });

        // Wireframe checkbox
        CheckBox wireframeCheckBox = new CheckBox("Wireframe");
        wireframeCheckBox.setStyle(LABEL_STYLE);
        wireframeCheckBox.setSelected(showWireframe);
        wireframeCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            showWireframe = newVal;
            renderPlanet();
        });

        content.getChildren().addAll(terrainRadio, rainfallRadio, new Separator(), smoothCheckBox, wireframeCheckBox);

        TitledPane pane = new TitledPane("Render", content);
        pane.setExpanded(true);
        pane.setCollapsible(true);
        return pane;
    }

    /**
     * Create the INFO section with stats and export options.
     */
    private TitledPane createInfoSection() {
        VBox content = new VBox(5);
        content.setPadding(new Insets(5));

        // Stats
        int polyCount = generatedPlanet.polygons().size();
        int riverCount = generatedPlanet.rivers() != null ? generatedPlanet.rivers().size() : 0;
        int plateCount = plateAssignment != null ? plateAssignment.plates().size() : 0;

        infoPolygonsLabel = new Label("Polygons: " + polyCount);
        infoPolygonsLabel.setStyle(INFO_LABEL_STYLE);

        infoRiversLabel = new Label("Rivers: " + riverCount);
        infoRiversLabel.setStyle(INFO_LABEL_STYLE);

        infoPlatesLabel = new Label("Plates: " + plateCount);
        infoPlatesLabel.setStyle(INFO_LABEL_STYLE);

        // Save screenshot button
        Button saveButton = new Button("Save Screenshot");
        saveButton.setMaxWidth(Double.MAX_VALUE);
        saveButton.setOnAction(e -> saveScreenshot());

        content.getChildren().addAll(infoPolygonsLabel, infoRiversLabel, infoPlatesLabel,
            new Separator(), saveButton);

        TitledPane pane = new TitledPane("Info", content);
        pane.setExpanded(true);
        pane.setCollapsible(true);
        return pane;
    }

    /**
     * Regenerate the planet with current parameter values.
     */
    private void regeneratePlanet() {
        // Read current values from UI
        try {
            currentSeed = Long.parseLong(seedField.getText().trim());
        } catch (NumberFormatException e) {
            currentSeed = System.nanoTime();
            seedField.setText(String.valueOf(currentSeed));
        }

        currentPlateCount = plateSpinner.getValue();
        currentWaterFraction = waterSlider.getValue();
        currentErosionIterations = erosionSpinner.getValue();
        currentRiverThreshold = riverSlider.getValue();
        currentHeightScale = heightSlider.getValue();
        currentUseContinuousHeights = continuousHeightsCheckBox.isSelected();
        currentReliefMin = reliefMinSpinner.getValue();
        currentReliefMax = reliefMaxSpinner.getValue();
        currentSize = sizeCombo.getValue();
        currentClimateModel = climateCombo.getValue();

        // Show progress UI
        regenerateButton.setDisable(true);
        progressBar.setProgress(0);
        progressBar.setVisible(true);
        progressBar.setManaged(true);
        progressLabel.setText("Starting...");
        progressLabel.setVisible(true);
        progressLabel.setManaged(true);

        // Build config
        PlanetConfig config = PlanetConfig.builder()
            .seed(currentSeed)
            .size(currentSize)
            .plateCount(currentPlateCount)
            .waterFraction(currentWaterFraction)
            .erosionIterations(currentErosionIterations)
            .riverSourceThreshold(currentRiverThreshold)
            .heightScaleMultiplier(currentHeightScale)
            .useContinuousHeights(currentUseContinuousHeights)
            .continuousReliefMin(currentReliefMin)
            .continuousReliefMax(currentReliefMax)
            .climateModel(currentClimateModel)
            .build();

        // Create progress listener
        GenerationProgressListener listener = new GenerationProgressListener() {
            @Override
            public void onPhaseStarted(GenerationProgressListener.Phase phase, String description) {
                Platform.runLater(() -> progressLabel.setText(description));
            }

            @Override
            public void onProgressUpdate(GenerationProgressListener.Phase phase, double progress) {
                double overall = GenerationProgressListener.calculateOverallProgress(phase, progress);
                Platform.runLater(() -> progressBar.setProgress(overall));
            }

            @Override
            public void onPhaseCompleted(GenerationProgressListener.Phase phase) {
                // Nothing special needed
            }

            @Override
            public void onGenerationCompleted() {
                Platform.runLater(() -> {
                    progressLabel.setText("Complete!");
                    progressBar.setProgress(1.0);
                });
            }

            @Override
            public void onGenerationError(GenerationProgressListener.Phase phase, Exception error) {
                Platform.runLater(() -> {
                    progressLabel.setText("Error: " + error.getMessage());
                    log.error("Generation error in phase {}: {}", phase, error.getMessage());
                });
            }
        };

        // Run generation in background thread
        CompletableFuture.supplyAsync(() -> PlanetGenerator.generate(config, listener))
            .thenAccept(newPlanet -> Platform.runLater(() -> {
                // Update planet data
                updatePlanetData(newPlanet);

                // Re-render
                createAtmosphere();
                renderPlanet();

                // Update info labels
                updateInfoLabels();

                // Update checkbox states based on new data
                updateControlStates();

                // Hide progress UI
                regenerateButton.setDisable(false);
                progressBar.setVisible(false);
                progressBar.setManaged(false);
                progressLabel.setVisible(false);
                progressLabel.setManaged(false);

                log.info("Regenerated planet with seed={}, size={}, plates={}, water={:.0f}%",
                    currentSeed, currentSize, currentPlateCount, currentWaterFraction * 100);
            }))
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    progressLabel.setText("Error: " + ex.getMessage());
                    regenerateButton.setDisable(false);
                    log.error("Failed to regenerate planet", ex);
                });
                return null;
            });
    }

    /**
     * Update info labels after regeneration.
     */
    private void updateInfoLabels() {
        int polyCount = generatedPlanet.polygons().size();
        int riverCount = generatedPlanet.rivers() != null ? generatedPlanet.rivers().size() : 0;
        int plateCount = plateAssignment != null ? plateAssignment.plates().size() : 0;

        infoPolygonsLabel.setText("Polygons: " + polyCount);
        infoRiversLabel.setText("Rivers: " + riverCount);
        infoPlatesLabel.setText("Plates: " + plateCount);
    }

    /**
     * Update control states based on new planet data availability.
     */
    private void updateControlStates() {
        int riverCount = generatedPlanet.rivers() != null ? generatedPlanet.rivers().size() : 0;
        riversCheckBox.setText("Rivers (" + riverCount + ")");
        riversCheckBox.setDisable(riverCount == 0);

        lakesCheckBox.setDisable(!hasLakes());
        flowRiversCheckBox.setDisable(!hasFlowAccumulation());

        boolean hasPlateData = plateAssignment != null && boundaryAnalysis != null;
        plateBoundariesCheckBox.setDisable(!hasPlateData);

        boolean hasClimateData = generatedPlanet.climates() != null && generatedPlanet.climates().length > 0;
        climateZonesCheckBox.setDisable(!hasClimateData);

        boolean hasPreciseHeights = preciseHeights != null && preciseHeights.length > 0;
        smoothCheckBox.setDisable(!hasPreciseHeights);
    }

    private boolean hasLakes() {
        boolean[] lakeMask = generatedPlanet.lakeMask();
        if (lakeMask == null) {
            return false;
        }
        for (boolean isLake : lakeMask) {
            if (isLake) {
                return true;
            }
        }
        return false;
    }

    private boolean hasFlowAccumulation() {
        double[] accumulation = generatedPlanet.flowAccumulation();
        return accumulation != null && accumulation.length > 0;
    }

    /**
     * Add ambient and directional lighting to the scene.
     */
    private void addLighting() {
        // Ambient light for overall illumination
        AmbientLight ambientLight = new AmbientLight(Color.rgb(60, 60, 70));
        world.getChildren().add(ambientLight);

        // Main directional light (sun-like)
        PointLight sunLight = new PointLight(Color.rgb(255, 250, 240));
        sunLight.setTranslateX(5);
        sunLight.setTranslateY(-3);
        sunLight.setTranslateZ(-8);
        world.getChildren().add(sunLight);

        // Fill light from opposite side (softer)
        PointLight fillLight = new PointLight(Color.rgb(100, 110, 140));
        fillLight.setTranslateX(-3);
        fillLight.setTranslateY(2);
        fillLight.setTranslateZ(-5);
        world.getChildren().add(fillLight);
    }

    /**
     * Create an atmosphere glow effect around the planet.
     */
    private void createAtmosphere() {
        // Remove existing atmosphere if any
        if (atmosphereSphere != null) {
            world.getChildren().remove(atmosphereSphere);
        }

        if (!showAtmosphere) {
            return;
        }

        // Create atmosphere sphere (5% larger than planet)
        double atmosphereRadius = PLANET_SCALE * 1.05;
        atmosphereSphere = new Sphere(atmosphereRadius);

        // Atmosphere color varies based on planet water content
        double waterFraction = generatedPlanet.config() != null
            ? generatedPlanet.config().waterFraction()
            : 0.66;

        Color atmosphereColor;
        if (waterFraction > 0.5) {
            atmosphereColor = Color.rgb(100, 150, 255, 0.12);
        } else if (waterFraction > 0.2) {
            atmosphereColor = Color.rgb(135, 180, 255, 0.08);
        } else {
            atmosphereColor = Color.rgb(180, 200, 230, 0.05);
        }

        PhongMaterial atmosphereMaterial = new PhongMaterial();
        atmosphereMaterial.setDiffuseColor(atmosphereColor);
        atmosphereMaterial.setSpecularColor(Color.TRANSPARENT);
        atmosphereSphere.setMaterial(atmosphereMaterial);
        atmosphereSphere.setCullFace(CullFace.NONE);

        world.getChildren().add(atmosphereSphere);
    }

    /**
     * Toggle atmosphere visibility.
     */
    private void updateAtmosphere() {
        if (atmosphereSphere != null) {
            atmosphereSphere.setVisible(showAtmosphere);
        }
    }

    /**
     * Render the planet terrain mesh.
     */
    private void renderPlanet() {
        planetGroup.getChildren().clear();

        List<Polygon> polygons = generatedPlanet.polygons();
        int[] heights = generatedPlanet.heights();
        boolean[] lakeMask = generatedPlanet.lakeMask();
        int[] renderHeights = heights;
        double[] renderPreciseHeights = preciseHeights;

        if (showLakes && lakeMask != null && lakeMask.length == heights.length) {
            renderHeights = heights.clone();
            for (int i = 0; i < lakeMask.length; i++) {
                if (lakeMask[i]) {
                    renderHeights[i] = ElevationCalculator.COASTAL;
                }
            }
            if (preciseHeights != null && preciseHeights.length == heights.length) {
                renderPreciseHeights = preciseHeights.clone();
                for (int i = 0; i < lakeMask.length; i++) {
                    if (lakeMask[i]) {
                        renderPreciseHeights[i] = ElevationCalculator.COASTAL;
                    }
                }
            }
        }

        if (showRainfallHeatmap && rainfall != null && rainfall.length > 0) {
            Map<Integer, TriangleMesh> meshByRainfall = JavaFxPlanetMeshConverter.convertByRainfall(
                polygons, renderHeights, rainfall, PLANET_SCALE);

            for (Map.Entry<Integer, TriangleMesh> entry : meshByRainfall.entrySet()) {
                int bucket = entry.getKey();
                TriangleMesh mesh = entry.getValue();

                MeshView meshView = new MeshView(mesh);
                meshView.setMaterial(JavaFxPlanetMeshConverter.createMaterialForRainfall(bucket));
                meshView.setCullFace(CullFace.BACK);
                meshView.setDrawMode(showWireframe ? DrawMode.LINE : DrawMode.FILL);

                planetGroup.getChildren().add(meshView);
            }

        } else if (useColorByHeight) {
            Map<Integer, TriangleMesh> meshByHeight = adjacency != null
                ? JavaFxPlanetMeshConverter.convertByHeightWithAveraging(
                    polygons, renderHeights, adjacency, PLANET_SCALE, renderPreciseHeights)
                : JavaFxPlanetMeshConverter.convertByHeight(polygons, renderHeights, PLANET_SCALE);

            for (Map.Entry<Integer, TriangleMesh> entry : meshByHeight.entrySet()) {
                int height = entry.getKey();
                TriangleMesh mesh = entry.getValue();

                MeshView meshView = new MeshView(mesh);
                meshView.setMaterial(JavaFxPlanetMeshConverter.createMaterialForHeight(height));
                meshView.setCullFace(CullFace.BACK);
                meshView.setDrawMode(showWireframe ? DrawMode.LINE : DrawMode.FILL);

                planetGroup.getChildren().add(meshView);
            }

        } else {
            TriangleMesh mesh;
            PhongMaterial material;

            if (useSmoothTerrain && preciseHeights != null && preciseHeights.length > 0) {
                mesh = JavaFxPlanetMeshConverter.convertSmooth(polygons, renderPreciseHeights, PLANET_SCALE);
                material = JavaFxPlanetMeshConverter.createSmoothTerrainMaterial(polygons, renderPreciseHeights);
            } else {
                mesh = JavaFxPlanetMeshConverter.convert(polygons, renderHeights, PLANET_SCALE);
                material = JavaFxPlanetMeshConverter.createTerrainMaterial(polygons, renderHeights);
            }

            MeshView meshView = new MeshView(mesh);
            meshView.setMaterial(material);
            meshView.setCullFace(CullFace.BACK);
            meshView.setDrawMode(showWireframe ? DrawMode.LINE : DrawMode.FILL);

            planetGroup.getChildren().add(meshView);
        }

        // Add rivers if enabled
        if (showRivers && generatedPlanet.rivers() != null && !generatedPlanet.rivers().isEmpty()) {
            addRivers();
        }

        // Add plate boundaries if enabled
        if (showPlateBoundaries) {
            addPlateBoundaries();
        }

        // Add climate zone indicators if enabled
        if (showClimateZones) {
            addClimateZones();
        }
    }

    /**
     * Add river visualization as gradient-colored lines.
     */
    private void addRivers() {
        List<List<Integer>> rivers = generatedPlanet.rivers();
        List<Polygon> polygons = generatedPlanet.polygons();
        boolean[] frozenTerminus = generatedPlanet.frozenRiverTerminus();
        int[] heights = generatedPlanet.heights();
        double[] flowAccumulation = useFlowAccumulationRivers ? generatedPlanet.flowAccumulation() : null;

        for (int riverIdx = 0; riverIdx < rivers.size(); riverIdx++) {
            List<Integer> river = rivers.get(riverIdx);
            if (river.size() < 2) continue;

            boolean isFrozen = frozenTerminus != null && riverIdx < frozenTerminus.length && frozenTerminus[riverIdx];

            double[] flowValues = calculateFlowValues(river, flowAccumulation);
            double maxFlow = 0.0;
            for (double flowValue : flowValues) {
                if (flowValue > maxFlow) maxFlow = flowValue;
            }
            if (maxFlow <= 0) maxFlow = 1.0;

            for (int i = 0; i < river.size() - 1; i++) {
                int polyIdx1 = river.get(i);
                int polyIdx2 = river.get(i + 1);

                if (polyIdx1 < 0 || polyIdx1 >= polygons.size() ||
                    polyIdx2 < 0 || polyIdx2 >= polygons.size()) {
                    continue;
                }

                double flowRatio = flowValues[i + 1] / maxFlow;

                int height1 = heights[polyIdx1];
                int height2 = heights[polyIdx2];
                double avgHeight = (height1 + height2) / 2.0;
                double displacement = 1.0 + avgHeight * 0.025;

                Point3D p1 = toPoint3D(polygons.get(polyIdx1).center().normalize()
                    .scalarMultiply(PLANET_SCALE * displacement * 1.003));
                Point3D p2 = toPoint3D(polygons.get(polyIdx2).center().normalize()
                    .scalarMultiply(PLANET_SCALE * displacement * 1.003));

                javafx.scene.shape.Cylinder riverSegment = createFlowBasedRiverSegment(
                    p1, p2, flowRatio, isFrozen);
                planetGroup.getChildren().add(riverSegment);
            }
        }
    }

    private double[] calculateFlowValues(List<Integer> river, double[] flowAccumulation) {
        double[] flow = new double[river.size()];

        if (flowAccumulation != null && flowAccumulation.length > 0) {
            for (int i = 0; i < river.size(); i++) {
                int polyIdx = river.get(i);
                flow[i] = polyIdx >= 0 && polyIdx < flowAccumulation.length
                    ? flowAccumulation[polyIdx]
                    : 0.0;
            }
            return flow;
        }

        double cumulative = 0.0;
        double baseFlowPerSegment = 0.5;
        for (int i = 0; i < river.size(); i++) {
            int polyIdx = river.get(i);
            double contribution = baseFlowPerSegment;
            if (rainfall != null && polyIdx >= 0 && polyIdx < rainfall.length) {
                contribution += rainfall[polyIdx] * 0.5;
            }
            cumulative += contribution;
            flow[i] = cumulative;
        }

        return flow;
    }

    private javafx.scene.shape.Cylinder createFlowBasedRiverSegment(
            Point3D start, Point3D end, double flowRatio, boolean frozen) {

        Point3D midpoint = start.midpoint(end);
        double length = start.distance(end);

        double minRadius = 0.002;
        double maxRadius = 0.008;
        double radius = minRadius + Math.sqrt(flowRatio) * (maxRadius - minRadius);

        javafx.scene.shape.Cylinder cylinder = new javafx.scene.shape.Cylinder(radius, length);

        PhongMaterial material = new PhongMaterial();
        Color riverColor;

        if (frozen) {
            Color sourceColor = Color.rgb(135, 206, 250);
            Color terminusColor = Color.rgb(224, 255, 255);
            riverColor = sourceColor.interpolate(terminusColor, flowRatio);
        } else {
            Color sourceColor = Color.rgb(100, 180, 255);
            Color mouthColor = Color.rgb(0, 80, 160);
            riverColor = sourceColor.interpolate(mouthColor, flowRatio);
        }

        material.setDiffuseColor(riverColor);
        material.setSpecularColor(Color.WHITE.deriveColor(0, 1, 0.5, 1));
        material.setSpecularPower(25.0);
        cylinder.setMaterial(material);

        cylinder.setTranslateX(midpoint.getX());
        cylinder.setTranslateY(midpoint.getY());
        cylinder.setTranslateZ(midpoint.getZ());

        Point3D direction = end.subtract(start).normalize();
        Point3D yAxis = new Point3D(0, 1, 0);
        Point3D rotationAxis = yAxis.crossProduct(direction);
        double rotationAngle = Math.acos(Math.max(-1, Math.min(1, yAxis.dotProduct(direction))));

        if (rotationAxis.magnitude() > 0.0001) {
            Rotate rotate = new Rotate(Math.toDegrees(rotationAngle), rotationAxis);
            cylinder.getTransforms().add(rotate);
        }

        return cylinder;
    }

    /**
     * Add plate boundary visualization.
     */
    private void addPlateBoundaries() {
        if (plateAssignment == null || boundaryAnalysis == null || adjacency == null) {
            return;
        }

        List<Polygon> polygons = generatedPlanet.polygons();
        int[] plateIndex = plateAssignment.plateIndex();
        int[] heights = generatedPlanet.heights();

        java.util.Set<String> drawnEdges = new java.util.HashSet<>();

        for (int polyIdx = 0; polyIdx < polygons.size(); polyIdx++) {
            int plate1 = plateIndex[polyIdx];
            int[] neighbors = adjacency.neighborsOnly(polyIdx);

            for (int neighborIdx : neighbors) {
                int plate2 = plateIndex[neighborIdx];

                if (plate1 == plate2) continue;

                String edgeKey = Math.min(polyIdx, neighborIdx) + "-" + Math.max(polyIdx, neighborIdx);
                if (drawnEdges.contains(edgeKey)) continue;
                drawnEdges.add(edgeKey);

                BoundaryDetector.PlatePair pair = new BoundaryDetector.PlatePair(plate1, plate2);
                BoundaryType boundaryType = boundaryAnalysis.boundaries().get(pair);
                if (boundaryType == null) {
                    boundaryType = BoundaryType.INACTIVE;
                }

                Polygon poly1 = polygons.get(polyIdx);
                Polygon poly2 = polygons.get(neighborIdx);

                double height1 = heights[polyIdx];
                double height2 = heights[neighborIdx];
                double avgHeight = (height1 + height2) / 2.0;
                double displacement = 1.0 + avgHeight * 0.025;

                Point3D p1 = toPoint3D(poly1.center().normalize()
                    .scalarMultiply(PLANET_SCALE * displacement * 1.004));
                Point3D p2 = toPoint3D(poly2.center().normalize()
                    .scalarMultiply(PLANET_SCALE * displacement * 1.004));

                javafx.scene.shape.Cylinder boundarySegment = createBoundarySegment(p1, p2, boundaryType);
                planetGroup.getChildren().add(boundarySegment);
            }
        }
    }

    private javafx.scene.shape.Cylinder createBoundarySegment(Point3D start, Point3D end, BoundaryType type) {
        Point3D midpoint = start.midpoint(end);
        double length = start.distance(end);

        double radius = (type == BoundaryType.CONVERGENT || type == BoundaryType.DIVERGENT) ? 0.004 : 0.003;
        javafx.scene.shape.Cylinder cylinder = new javafx.scene.shape.Cylinder(radius, length);

        PhongMaterial material = new PhongMaterial();
        Color boundaryColor = switch (type) {
            case CONVERGENT -> Color.rgb(220, 60, 60);
            case DIVERGENT -> Color.rgb(60, 200, 180);
            case TRANSFORM -> Color.rgb(220, 180, 60);
            case INACTIVE -> Color.rgb(120, 120, 120);
        };

        material.setDiffuseColor(boundaryColor);
        material.setSpecularColor(Color.WHITE.deriveColor(0, 1, 0.3, 1));
        cylinder.setMaterial(material);

        cylinder.setTranslateX(midpoint.getX());
        cylinder.setTranslateY(midpoint.getY());
        cylinder.setTranslateZ(midpoint.getZ());

        Point3D direction = end.subtract(start).normalize();
        Point3D yAxis = new Point3D(0, 1, 0);
        Point3D rotationAxis = yAxis.crossProduct(direction);
        double rotationAngle = Math.acos(Math.max(-1, Math.min(1, yAxis.dotProduct(direction))));

        if (rotationAxis.magnitude() > 0.0001) {
            Rotate rotate = new Rotate(Math.toDegrees(rotationAngle), rotationAxis);
            cylinder.getTransforms().add(rotate);
        }

        return cylinder;
    }

    /**
     * Add climate zone visualization.
     */
    private void addClimateZones() {
        if (generatedPlanet.climates() == null || generatedPlanet.climates().length == 0) {
            return;
        }

        addLatitudeRing(30.0, Color.rgb(255, 200, 100, 0.4));
        addLatitudeRing(-30.0, Color.rgb(255, 200, 100, 0.4));
        addLatitudeRing(60.0, Color.rgb(100, 200, 255, 0.5));
        addLatitudeRing(-60.0, Color.rgb(100, 200, 255, 0.5));
    }

    private void addLatitudeRing(double latitudeDegrees, Color color) {
        double latRad = Math.toRadians(latitudeDegrees);
        double ringRadius = PLANET_SCALE * Math.cos(latRad) * 1.008;
        double y = PLANET_SCALE * Math.sin(latRad) * 1.008;

        int segments = 72;
        for (int i = 0; i < segments; i++) {
            double angle = 2.0 * Math.PI * i / segments;
            double x = ringRadius * Math.cos(angle);
            double z = ringRadius * Math.sin(angle);

            Sphere dot = new Sphere(0.008);
            PhongMaterial material = new PhongMaterial(color);
            dot.setMaterial(material);
            dot.setTranslateX(x);
            dot.setTranslateY(y);
            dot.setTranslateZ(z);

            planetGroup.getChildren().add(dot);
        }
    }

    private Point3D toPoint3D(org.hipparchus.geometry.euclidean.threed.Vector3D v) {
        return new Point3D(v.getX(), v.getY(), v.getZ());
    }

    /**
     * Derive the Size enum value from the n subdivision level.
     */
    private static PlanetConfig.Size deriveSizeFromN(int n) {
        for (PlanetConfig.Size size : PlanetConfig.Size.values()) {
            if (size.n == n) {
                return size;
            }
        }
        // If no exact match, find closest
        PlanetConfig.Size closest = PlanetConfig.Size.STANDARD;
        int minDiff = Integer.MAX_VALUE;
        for (PlanetConfig.Size size : PlanetConfig.Size.values()) {
            int diff = Math.abs(size.n - n);
            if (diff < minDiff) {
                minDiff = diff;
                closest = size;
            }
        }
        return closest;
    }

    /**
     * Set up mouse handlers for rotation and zoom.
     */
    private void setupMouseHandlers() {
        subScene.setOnMousePressed(event -> {
            mouseX = event.getSceneX();
            mouseY = event.getSceneY();
            mouseOldX = event.getSceneX();
            mouseOldY = event.getSceneY();
        });

        subScene.setOnMouseDragged(event -> {
            mouseOldX = mouseX;
            mouseOldY = mouseY;
            mouseX = event.getSceneX();
            mouseY = event.getSceneY();

            double deltaX = mouseX - mouseOldX;
            double deltaY = mouseY - mouseOldY;

            double modifier = 0.3;

            if (event.isPrimaryButtonDown()) {
                rotateY.setAngle(rotateY.getAngle() + deltaX * modifier);
                rotateX.setAngle(rotateX.getAngle() - deltaY * modifier);

                if (rotateX.getAngle() > 85) rotateX.setAngle(85);
                if (rotateX.getAngle() < -85) rotateX.setAngle(-85);
            }
        });

        subScene.setOnScroll(event -> {
            double deltaY = event.getDeltaY();
            double newZ = camera.getTranslateZ() + deltaY * 0.01;
            newZ = Math.max(-8, Math.min(-1.5, newZ));
            camera.setTranslateZ(newZ);
        });
    }

    /**
     * Reset the view to initial state.
     */
    private void resetView() {
        rotateX.setAngle(25);
        rotateY.setAngle(25);
        camera.setTranslateZ(INITIAL_CAMERA_DISTANCE);
    }

    private void initializeAnimation() {
        rotationAnimation = new Timeline(
            new KeyFrame(Duration.millis(30), event -> {
                if (autoRotate) {
                    rotateY.setAngle(rotateY.getAngle() + 0.3);
                }
            })
        );
        rotationAnimation.setCycleCount(Animation.INDEFINITE);
    }

    private void setAutoRotate(boolean enabled) {
        this.autoRotate = enabled;
        if (enabled) {
            if (rotationAnimation == null) {
                initializeAnimation();
            }
            rotationAnimation.play();
        } else {
            if (rotationAnimation != null) {
                rotationAnimation.pause();
            }
        }
    }

    private void stopAnimation() {
        if (rotationAnimation != null) {
            rotationAnimation.stop();
        }
    }

    /**
     * Create the LEGEND section showing color mappings.
     */
    private TitledPane createLegendSection() {
        VBox content = new VBox(3);
        content.setPadding(new Insets(5));

        // Elevation legend
        Label elevTitle = new Label("Elevation");
        elevTitle.setStyle("-fx-text-fill: #333333; -fx-font-weight: bold; -fx-font-size: 10;");
        content.getChildren().add(elevTitle);

        String[][] legendItems = {
            {"Snow Peak", "#FFFFFF"},
            {"Mountain", "#8B7355"},
            {"Highland", "#9B8B5B"},
            {"Lowland", "#6B8E23"},
            {"Coast", "#90B060"},
            {"Shallow", "#5090C0"},
            {"Ocean", "#3070A0"},
            {"Deep Sea", "#204080"},
            {"Abyss", "#102050"}
        };

        for (String[] item : legendItems) {
            HBox row = new HBox(5);
            row.setAlignment(Pos.CENTER_LEFT);

            Rectangle colorBox = new Rectangle(14, 10);
            colorBox.setFill(Color.web(item[1]));
            colorBox.setStroke(Color.gray(0.5));
            colorBox.setStrokeWidth(0.5);

            Label label = new Label(item[0]);
            label.setStyle("-fx-text-fill: #555555; -fx-font-size: 9;");

            row.getChildren().addAll(colorBox, label);
            content.getChildren().add(row);
        }

        // Plate boundary legend (if plate data exists)
        if (plateAssignment != null && boundaryAnalysis != null) {
            content.getChildren().add(new Separator());
            Label plateTitle = new Label("Boundaries");
            plateTitle.setStyle("-fx-text-fill: #333333; -fx-font-weight: bold; -fx-font-size: 10;");
            content.getChildren().add(plateTitle);

            String[][] plateItems = {
                {"Convergent", "#DC3C3C"},
                {"Divergent", "#3CC8B4"},
                {"Transform", "#DCB43C"},
                {"Inactive", "#787878"}
            };

            for (String[] item : plateItems) {
                HBox row = new HBox(5);
                row.setAlignment(Pos.CENTER_LEFT);

                Rectangle colorBox = new Rectangle(14, 4);
                colorBox.setFill(Color.web(item[1]));

                Label label = new Label(item[0]);
                label.setStyle("-fx-text-fill: #555555; -fx-font-size: 9;");

                row.getChildren().addAll(colorBox, label);
                content.getChildren().add(row);
            }
        }

        TitledPane pane = new TitledPane("Legend", content);
        pane.setExpanded(false);  // Collapsed by default
        pane.setCollapsible(true);
        return pane;
    }

    /**
     * Save the current view as a PNG screenshot.
     */
    private void saveScreenshot() {
        boolean wasRotating = autoRotate;
        if (wasRotating) {
            setAutoRotate(false);
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Planet Screenshot");
        fileChooser.setInitialFileName(planetName.replaceAll("[^a-zA-Z0-9]", "_") + "_terrain.png");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("PNG Image", "*.png")
        );

        File file = fileChooser.showSaveDialog(getDialogPane().getScene().getWindow());
        if (file != null) {
            try {
                WritableImage image = subScene.snapshot(null, null);
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
                log.info("Saved screenshot to: {}", file.getAbsolutePath());
            } catch (IOException e) {
                log.error("Failed to save screenshot: {}", e.getMessage());
            }
        }

        if (wasRotating) {
            setAutoRotate(true);
        }
    }
}
