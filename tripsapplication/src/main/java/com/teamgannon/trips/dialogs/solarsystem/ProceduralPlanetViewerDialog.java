package com.teamgannon.trips.dialogs.solarsystem;

import com.teamgannon.trips.planetary.modelling.procedural.ClimateCalculator;
import com.teamgannon.trips.planetary.modelling.procedural.ElevationCalculator;
import com.teamgannon.trips.planetary.modelling.procedural.GenerationProgressListener;
import com.teamgannon.trips.planetary.modelling.procedural.JavaFxPlanetMeshConverter;
import com.teamgannon.trips.planetary.modelling.procedural.JavaFxPlanetMeshConverter.TerrainType;
import com.teamgannon.trips.planetary.modelling.procedural.PlanetConfig;
import com.teamgannon.trips.planetary.modelling.procedural.PlanetGenerator;
import com.teamgannon.trips.planetary.modelling.procedural.PlanetGenerator.GeneratedPlanet;
import com.teamgannon.trips.planetary.modelling.procedural.Polygon;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

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
    private final BiConsumer<GeneratedPlanet, PlanetConfig> onRegenerated;

    // Rotation transforms
    private final Rotate rotateX = new Rotate(25, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(25, Rotate.Y_AXIS);
    private final Rotate axialTiltRotate = new Rotate(0, Rotate.X_AXIS);
    private final Rotate spinRotate = new Rotate(0, Rotate.Y_AXIS);

    /**
     * Mouse / scroll / keyboard / auto-spin controller — owns the input and
     * idle-rotation Timeline. Extracted in Phase 4.2 alongside the rest of the
     * procedural-planet-viewer decomposition.
     */
    private final PlanetCameraController cameraController;

    // Rendering options
    private boolean showWireframe = false;
    private boolean showRivers = false;
    private boolean useColorByHeight = true;
    private boolean showRainfallHeatmap = false;
    private boolean useSmoothTerrain = false;
    // Atmosphere visibility moved into PlanetAtmosphereRenderer (Phase 4.2).
    private boolean showPlateBoundaries = false;
    private boolean showClimateZones = false;
    private boolean showLakes = true;
    private boolean useFlowAccumulationRivers = true;
    // Pole-marker visibility moved into PlanetPoleMarker (Phase 4.2).

    /**
     * Renders + toggles the atmosphere shell. Extracted in Phase 4.2.
     */
    private final PlanetAtmosphereRenderer atmosphereRenderer;

    private final String planetName;
    private final PlanetGenerationSession session;

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
    private Slider axialTiltSlider;
    private Label axialTiltLabel;
    private Slider seasonalOffsetSlider;
    private Label seasonalOffsetLabel;
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
    private CheckBox poleMarkerCheckBox;

    /** Renders + toggles the pole-marker spheres. Extracted in Phase 4.2. */
    private PlanetPoleMarker poleMarker;

    /**
     * Create a new procedural planet viewer dialog.
     *
     * @param planetName The name of the planet being viewed
     * @param planet     The generated planet data
     */
    public ProceduralPlanetViewerDialog(String planetName, GeneratedPlanet planet) {
        this(planetName, planet, null);
    }

    /**
     * Create a new procedural planet viewer dialog with a regeneration callback.
     *
     * @param planetName   The name of the planet being viewed
     * @param planet       The generated planet data
     * @param onRegenerated Callback invoked after regeneration completes
     */
    public ProceduralPlanetViewerDialog(String planetName, GeneratedPlanet planet,
            BiConsumer<GeneratedPlanet, PlanetConfig> onRegenerated) {
        this.planetName = planetName;
        this.session = PlanetGenerationSession.from(planet);
        this.onRegenerated = onRegenerated;

        setTitle("Terrain: " + planetName);
        setResizable(false);  // Fixed size dialog

        // Create 3D scene
        world = new Group();
        planetGroup = new Group();
        planetGroup.getTransforms().addAll(axialTiltRotate, spinRotate);
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

        // Apply axial tilt and pole markers (Phase 4.2: PlanetPoleMarker)
        updateAxialTilt();
        poleMarker = new PlanetPoleMarker(planetGroup, PLANET_SCALE);
        createPoleMarker();

        // Phase 4.2: atmosphere shell lives in PlanetAtmosphereRenderer.
        atmosphereRenderer = new PlanetAtmosphereRenderer(world, PLANET_SCALE);
        createAtmosphere();

        // Phase 4.2: mouse / scroll / keyboard / auto-spin all live in PlanetCameraController.
        cameraController = new PlanetCameraController(
                subScene, camera, rotateX, rotateY, spinRotate, INITIAL_CAMERA_DISTANCE);
        cameraController.install();

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
        setOnCloseRequest(event -> cameraController.stop());
        setResultConverter(button -> {
            cameraController.stop();
            return null;
        });

        log.info("Created procedural planet viewer for: {}", planetName);
    }

    /**
     * Set the surface temperature in Kelvin.
     * This affects terrain type determination:
     * - Below 273K (freezing): ICE terrain (white/blue/gray)
     * - Above 273K with water: WET terrain (ocean blues)
     * - No water: DRY terrain (browns/tans)
     *
     * @param temperatureK Surface temperature in Kelvin
     */
    public void setSurfaceTemperature(double temperatureK) {
        session.setSurfaceTemperature(temperatureK);
        // Re-render if already displayed
        if (session.planet() != null) {
            renderPlanet();
            createAtmosphere();
        }
    }

    /**
     * Set the planet type (e.g., "Gas Giant", "Ice Giant", "Rock", "Terrestrial").
     * Gas giants use cloud band colors instead of terrain colors.
     *
     * @param type Planet type string from ExoPlanet.getPlanetType()
     */
    public void setPlanetType(String type) {
        session.setPlanetType(type);
        // Re-render if already displayed
        if (session.planet() != null) {
            renderPlanet();
            createAtmosphere();
        }
    }

    /**
     * Set the ice cover fraction (0.0-1.0).
     * High ice cover indicates an icy world even if there's no liquid water.
     *
     * @param iceCover Ice cover fraction from ExoPlanet.getIceCover()
     */
    public void setIceCover(double iceCover) {
        session.setIceCover(iceCover);
        // Re-render if already displayed
        if (session.planet() != null) {
            renderPlanet();
            createAtmosphere();
        }
    }

    /**
     * Set the planet density in g/cm³.
     * Low density (< 2.5) indicates ice-rich composition.
     *
     * @param density Density from ExoPlanet.getDensity()
     */
    public void setDensity(double density) {
        session.setDensity(density);
    }

    /**
     * Set the semi-major axis in AU.
     * Beyond ~2.7 AU (frost line), planets are more likely to be icy.
     *
     * @param semiMajorAxis Semi-major axis from ExoPlanet.getSemiMajorAxis()
     */
    public void setSemiMajorAxis(double semiMajorAxis) {
        session.setSemiMajorAxis(semiMajorAxis);
    }

    /**
     * Determine the terrain classification by snapshotting the dialog's current
     * physical-context fields and delegating to {@link PlanetTerrainClassifier}.
     * Phase 4.2 extraction.
     */
    private TerrainType determineTerrainType() {
        return session.determineTerrainType();
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

    // Label styling moved to theme.css: see .trips-text-form-label
    // (#333 + 11), .trips-text-form-info (#555 + 10), and
    // .trips-text-muted-sm (#666 + 10). Applied via getStyleClass().add(...).

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
        seedLabel.getStyleClass().add("trips-text-form-label");
        seedField = new TextField(String.valueOf(session.seed()));
        seedField.setPrefWidth(100);
        seedField.getStyleClass().add("trips-text-sm"); // Issue 50 / Bucket A
        Button randomizeButton = new Button("🎲");
        randomizeButton.setTooltip(new Tooltip("Generate random seed"));
        randomizeButton.setOnAction(e -> {
            session.setSeed(new Random().nextLong());
            seedField.setText(String.valueOf(session.seed()));
        });
        seedRow.getChildren().addAll(seedLabel, seedField, randomizeButton);

        // Size
        HBox sizeRow = new HBox(5);
        sizeRow.setAlignment(Pos.CENTER_LEFT);
        Label sizeLabel = new Label("Size:");
        sizeLabel.getStyleClass().add("trips-text-form-label");
        sizeCombo = new ComboBox<>();
        sizeCombo.getItems().addAll(PlanetConfig.Size.values());
        sizeCombo.setValue(session.size());
        sizeCombo.setPrefWidth(120);
        sizeCombo.getStyleClass().add("trips-text-sm"); // Issue 50 / Bucket A
        sizeRow.getChildren().addAll(sizeLabel, sizeCombo);

        // Plate count
        HBox plateRow = new HBox(5);
        plateRow.setAlignment(Pos.CENTER_LEFT);
        Label platesLabel = new Label("Plates:");
        platesLabel.getStyleClass().add("trips-text-form-label");
        plateSpinner = new Spinner<>(7, 21, session.plateCount());
        plateSpinner.setPrefWidth(70);
        plateSpinner.setEditable(true);
        plateSpinner.getStyleClass().add("trips-text-sm"); // Issue 50 / Bucket A
        plateRow.getChildren().addAll(platesLabel, plateSpinner);

        // Water fraction
        VBox waterBox = new VBox(2);
        HBox waterHeader = new HBox(5);
        waterHeader.setAlignment(Pos.CENTER_LEFT);
        waterLabel = new Label("Water: %.0f%%".formatted(session.waterFraction() * 100));
        waterLabel.getStyleClass().add("trips-text-form-label");
        waterHeader.getChildren().add(waterLabel);
        waterSlider = new Slider(0, 1, session.waterFraction());
        waterSlider.setShowTickMarks(true);
        waterSlider.setMajorTickUnit(0.25);
        waterSlider.valueProperty().addListener((obs, old, val) -> {
            session.setWaterFraction(val.doubleValue());
            waterLabel.setText("Water: %.0f%%".formatted(session.waterFraction() * 100));
        });
        waterBox.getChildren().addAll(waterHeader, waterSlider);

        // Erosion iterations
        HBox erosionRow = new HBox(5);
        erosionRow.setAlignment(Pos.CENTER_LEFT);
        Label erosionLabel = new Label("Erosion:");
        erosionLabel.getStyleClass().add("trips-text-form-label");
        erosionSpinner = new Spinner<>(0, 10, session.erosionIterations());
        erosionSpinner.setPrefWidth(70);
        erosionSpinner.setEditable(true);
        erosionSpinner.getStyleClass().add("trips-text-sm"); // Issue 50 / Bucket A
        erosionRow.getChildren().addAll(erosionLabel, erosionSpinner);

        // River threshold
        VBox riverBox = new VBox(2);
        HBox riverHeader = new HBox(5);
        riverHeader.setAlignment(Pos.CENTER_LEFT);
        riverLabel = new Label("River Thresh: %.2f".formatted(session.riverThreshold()));
        riverLabel.getStyleClass().add("trips-text-form-label");
        riverHeader.getChildren().add(riverLabel);
        riverSlider = new Slider(0.1, 1.0, session.riverThreshold());
        riverSlider.setShowTickMarks(true);
        riverSlider.setMajorTickUnit(0.2);
        riverSlider.valueProperty().addListener((obs, old, val) -> {
            session.setRiverThreshold(val.doubleValue());
            riverLabel.setText("River Thresh: %.2f".formatted(session.riverThreshold()));
        });
        riverBox.getChildren().addAll(riverHeader, riverSlider);

        // Height scale
        VBox heightBox = new VBox(2);
        HBox heightHeader = new HBox(5);
        heightHeader.setAlignment(Pos.CENTER_LEFT);
        heightLabel = new Label("Height Scale: %.1f".formatted(session.heightScale()));
        heightLabel.getStyleClass().add("trips-text-form-label");
        heightHeader.getChildren().add(heightLabel);
        heightSlider = new Slider(0.5, 3.0, session.heightScale());
        heightSlider.setShowTickMarks(true);
        heightSlider.setMajorTickUnit(0.5);
        heightSlider.valueProperty().addListener((obs, old, val) -> {
            session.setHeightScale(val.doubleValue());
            heightLabel.setText("Height Scale: %.1f".formatted(session.heightScale()));
        });
        heightBox.getChildren().addAll(heightHeader, heightSlider);

        // Axial tilt
        VBox tiltBox = new VBox(2);
        HBox tiltHeader = new HBox(5);
        tiltHeader.setAlignment(Pos.CENTER_LEFT);
        axialTiltLabel = new Label("Axial Tilt: %.1f°".formatted(session.axialTilt()));
        axialTiltLabel.getStyleClass().add("trips-text-form-label");
        tiltHeader.getChildren().add(axialTiltLabel);
        axialTiltSlider = new Slider(0, 60, session.axialTilt());
        axialTiltSlider.setShowTickMarks(true);
        axialTiltSlider.setMajorTickUnit(10);
        axialTiltSlider.valueProperty().addListener((obs, old, val) -> {
            session.setAxialTilt(val.doubleValue());
            axialTiltLabel.setText("Axial Tilt: %.1f°".formatted(session.axialTilt()));
            updateAxialTilt();
        });
        tiltBox.getChildren().addAll(tiltHeader, axialTiltSlider);

        // Seasonal offset
        VBox seasonBox = new VBox(2);
        HBox seasonHeader = new HBox(5);
        seasonHeader.setAlignment(Pos.CENTER_LEFT);
        seasonalOffsetLabel = new Label("Season Offset: %.0f°".formatted(session.seasonalOffset()));
        seasonalOffsetLabel.getStyleClass().add("trips-text-form-label");
        seasonHeader.getChildren().add(seasonalOffsetLabel);
        seasonalOffsetSlider = new Slider(0, 360, session.seasonalOffset());
        seasonalOffsetSlider.setShowTickMarks(true);
        seasonalOffsetSlider.setMajorTickUnit(90);
        seasonalOffsetSlider.valueProperty().addListener((obs, old, val) -> {
            session.setSeasonalOffset(val.doubleValue());
            seasonalOffsetLabel.setText("Season Offset: %.0f°".formatted(session.seasonalOffset()));
        });
        seasonBox.getChildren().addAll(seasonHeader, seasonalOffsetSlider);

        // Continuous heights
        VBox continuousBox = new VBox(4);
        continuousHeightsCheckBox = new CheckBox("Continuous Heights");
        continuousHeightsCheckBox.setSelected(session.useContinuousHeights());

        HBox reliefRow = new HBox(5);
        reliefRow.setAlignment(Pos.CENTER_LEFT);
        Label reliefLabel = new Label("Relief:");
        reliefLabel.getStyleClass().add("trips-text-form-label");
        reliefMinSpinner = new Spinner<>(
            new SpinnerValueFactory.DoubleSpinnerValueFactory(-6.0, 0.0, session.reliefMin(), 0.1));
        reliefMinSpinner.setPrefWidth(70);
        reliefMinSpinner.setEditable(true);
        reliefMinSpinner.getStyleClass().add("trips-text-sm"); // Issue 50 / Bucket A
        reliefMaxSpinner = new Spinner<>(
            new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 6.0, session.reliefMax(), 0.1));
        reliefMaxSpinner.setPrefWidth(70);
        reliefMaxSpinner.setEditable(true);
        reliefMaxSpinner.getStyleClass().add("trips-text-sm"); // Issue 50 / Bucket A
        reliefRow.getChildren().addAll(reliefLabel, reliefMinSpinner, reliefMaxSpinner);

        continuousHeightsCheckBox.selectedProperty().addListener((obs, old, val) -> {
            reliefMinSpinner.setDisable(!val);
            reliefMaxSpinner.setDisable(!val);
        });
        reliefMinSpinner.setDisable(!session.useContinuousHeights());
        reliefMaxSpinner.setDisable(!session.useContinuousHeights());

        continuousBox.getChildren().addAll(continuousHeightsCheckBox, reliefRow);

        // Climate model
        HBox climateRow = new HBox(5);
        climateRow.setAlignment(Pos.CENTER_LEFT);
        Label climateLabel = new Label("Climate:");
        climateLabel.getStyleClass().add("trips-text-form-label");
        climateCombo = new ComboBox<>();
        climateCombo.getItems().addAll(ClimateCalculator.ClimateModel.values());
        climateCombo.setValue(session.climateModel());
        climateCombo.setPrefWidth(120);
        climateCombo.getStyleClass().add("trips-text-sm"); // Issue 50 / Bucket A
        climateRow.getChildren().addAll(climateLabel, climateCombo);

        // Regenerate button
        regenerateButton = new Button("Regenerate");
        regenerateButton.setMaxWidth(Double.MAX_VALUE);
        regenerateButton.getStyleClass().add("trips-bold"); // Issue 50 / Bucket A
        regenerateButton.setOnAction(e -> regeneratePlanet());

        // Progress bar (initially hidden)
        progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setVisible(false);
        progressBar.setManaged(false);

        progressLabel = new Label("");
        progressLabel.getStyleClass().add("trips-text-muted-sm");
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
            tiltBox,
            seasonBox,
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
        zoomLabel.getStyleClass().add("trips-text-form-label");
        Slider zoomSlider = new Slider(-8, -1.5, INITIAL_CAMERA_DISTANCE);
        zoomSlider.setShowTickMarks(true);
        zoomSlider.setMajorTickUnit(2);
        zoomSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            camera.setTranslateZ(newVal.doubleValue());
        });
        zoomBox.getChildren().addAll(zoomLabel, zoomSlider);

        // Auto-rotate checkbox (Phase 4.2: delegated to PlanetCameraController)
        CheckBox autoRotateCheckBox = new CheckBox("Auto-spin");
        autoRotateCheckBox.getStyleClass().add("trips-text-form-label");
        autoRotateCheckBox.setSelected(cameraController.isAutoRotate());
        autoRotateCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            cameraController.setAutoRotate(newVal);
        });

        // Reset view button
        Button resetButton = new Button("Reset View");
        resetButton.setMaxWidth(Double.MAX_VALUE);
        resetButton.setOnAction(e -> {
            cameraController.resetView();
            autoRotateCheckBox.setSelected(false);
            cameraController.setAutoRotate(false);
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
        int riverCount = session.riverCount();
        riversCheckBox = new CheckBox("Rivers (" + riverCount + ")");
        riversCheckBox.getStyleClass().add("trips-text-form-label");
        riversCheckBox.setSelected(showRivers);
        riversCheckBox.setDisable(riverCount == 0);
        riversCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            showRivers = newVal;
            renderPlanet();
        });

        // Lakes checkbox
        boolean hasLakes = session.hasLakes();
        lakesCheckBox = new CheckBox("Lakes");
        lakesCheckBox.getStyleClass().add("trips-text-form-label");
        lakesCheckBox.setSelected(showLakes);
        lakesCheckBox.setDisable(!hasLakes);
        lakesCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            showLakes = newVal;
            renderPlanet();
        });

        // Flow-scaled rivers checkbox
        boolean hasFlow = session.hasFlowAccumulation();
        flowRiversCheckBox = new CheckBox("Flow-Scaled Rivers");
        flowRiversCheckBox.getStyleClass().add("trips-text-form-label");
        flowRiversCheckBox.setSelected(useFlowAccumulationRivers);
        flowRiversCheckBox.setDisable(!hasFlow);
        flowRiversCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            useFlowAccumulationRivers = newVal;
            renderPlanet();
        });

        // Plate boundaries checkbox
        boolean hasPlateData = session.hasPlateData();
        plateBoundariesCheckBox = new CheckBox("Plate Boundaries");
        plateBoundariesCheckBox.getStyleClass().add("trips-text-form-label");
        plateBoundariesCheckBox.setSelected(showPlateBoundaries);
        plateBoundariesCheckBox.setDisable(!hasPlateData);
        plateBoundariesCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            showPlateBoundaries = newVal;
            renderPlanet();
        });

        // Climate zones checkbox
        boolean hasClimateData = session.hasClimateData();
        climateZonesCheckBox = new CheckBox("Climate Zones");
        climateZonesCheckBox.getStyleClass().add("trips-text-form-label");
        climateZonesCheckBox.setSelected(showClimateZones);
        climateZonesCheckBox.setDisable(!hasClimateData);
        climateZonesCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            showClimateZones = newVal;
            renderPlanet();
        });

        // Pole marker checkbox (Phase 4.2: delegated to PlanetPoleMarker)
        poleMarkerCheckBox = new CheckBox("Pole Marker");
        poleMarkerCheckBox.getStyleClass().add("trips-text-form-label");
        poleMarkerCheckBox.setSelected(poleMarker.isVisible());
        poleMarkerCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            poleMarker.setVisible(newVal);
            if (newVal) {
                createPoleMarker();
            }
        });

        // Atmosphere checkbox (Phase 4.2: delegated to PlanetAtmosphereRenderer)
        CheckBox atmosphereCheckBox = new CheckBox("Atmosphere");
        atmosphereCheckBox.getStyleClass().add("trips-text-form-label");
        atmosphereCheckBox.setSelected(atmosphereRenderer.isShowAtmosphere());
        atmosphereCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            atmosphereRenderer.setShowAtmosphere(newVal);
        });

        content.getChildren().addAll(
            riversCheckBox, lakesCheckBox, flowRiversCheckBox,
            plateBoundariesCheckBox, climateZonesCheckBox, poleMarkerCheckBox, atmosphereCheckBox);

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
        terrainRadio.getStyleClass().add("trips-text-form-label");
        terrainRadio.setToggleGroup(renderGroup);
        terrainRadio.setSelected(useColorByHeight);

        boolean hasRainfall = session.hasRainfall();
        RadioButton rainfallRadio = new RadioButton("Rainfall Heatmap");
        rainfallRadio.getStyleClass().add("trips-text-form-label");
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
        boolean hasPreciseHeights = session.hasPreciseHeights();
        smoothCheckBox = new CheckBox("Smooth Terrain");
        smoothCheckBox.getStyleClass().add("trips-text-form-label");
        smoothCheckBox.setSelected(useSmoothTerrain);
        smoothCheckBox.setDisable(!hasPreciseHeights);
        smoothCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            useSmoothTerrain = newVal;
            renderPlanet();
        });

        // Wireframe checkbox
        CheckBox wireframeCheckBox = new CheckBox("Wireframe");
        wireframeCheckBox.getStyleClass().add("trips-text-form-label");
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
        int polyCount = session.polygonCount();
        int riverCount = session.riverCount();
        int plateCount = session.plateCountForDisplay();

        infoPolygonsLabel = new Label("Polygons: " + polyCount);
        infoPolygonsLabel.getStyleClass().add("trips-text-form-info");

        infoRiversLabel = new Label("Rivers: " + riverCount);
        infoRiversLabel.getStyleClass().add("trips-text-form-info");

        infoPlatesLabel = new Label("Plates: " + plateCount);
        infoPlatesLabel.getStyleClass().add("trips-text-form-info");

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
        long seed;
        try {
            seed = Long.parseLong(seedField.getText().trim());
        } catch (NumberFormatException e) {
            seed = System.nanoTime();
            seedField.setText(String.valueOf(seed));
        }

        session.captureGenerationControls(
                seed,
                plateSpinner.getValue(),
                waterSlider.getValue(),
                erosionSpinner.getValue(),
                riverSlider.getValue(),
                heightSlider.getValue(),
                continuousHeightsCheckBox.isSelected(),
                reliefMinSpinner.getValue(),
                reliefMaxSpinner.getValue(),
                axialTiltSlider.getValue(),
                seasonalOffsetSlider.getValue(),
                sizeCombo.getValue(),
                climateCombo.getValue());

        // Show progress UI
        regenerateButton.setDisable(true);
        progressBar.setProgress(0);
        progressBar.setVisible(true);
        progressBar.setManaged(true);
        progressLabel.setText("Starting...");
        progressLabel.setVisible(true);
        progressLabel.setManaged(true);

        // Build config
        PlanetConfig config = session.buildConfig();

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
                session.applyPlanet(newPlanet);

                // Re-render
                updateAxialTilt();
                createPoleMarker();
                createAtmosphere();
                renderPlanet();

                // Update info labels
                updateInfoLabels();

                // Update checkbox states based on new data
                updateControlStates();

                if (onRegenerated != null) {
                    try {
                        onRegenerated.accept(newPlanet, newPlanet.config());
                    } catch (Exception ex) {
                        log.warn("Failed to persist regenerated planet metadata", ex);
                    }
                }

                // Hide progress UI
                regenerateButton.setDisable(false);
                progressBar.setVisible(false);
                progressBar.setManaged(false);
                progressLabel.setVisible(false);
                progressLabel.setManaged(false);

                log.info("Regenerated planet with seed={}, size={}, plates={}, water={:.0f}%",
                    session.seed(), session.size(), session.plateCount(), session.waterFraction() * 100);
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
        int polyCount = session.polygonCount();
        int riverCount = session.riverCount();
        int plateCount = session.plateCountForDisplay();

        infoPolygonsLabel.setText("Polygons: " + polyCount);
        infoRiversLabel.setText("Rivers: " + riverCount);
        infoPlatesLabel.setText("Plates: " + plateCount);
    }

    /**
     * Update control states based on new planet data availability.
     */
    private void updateControlStates() {
        int riverCount = session.riverCount();
        riversCheckBox.setText("Rivers (" + riverCount + ")");
        riversCheckBox.setDisable(riverCount == 0);

        lakesCheckBox.setDisable(!session.hasLakes());
        flowRiversCheckBox.setDisable(!session.hasFlowAccumulation());

        boolean hasPlateData = session.hasPlateData();
        plateBoundariesCheckBox.setDisable(!hasPlateData);

        boolean hasClimateData = session.hasClimateData();
        climateZonesCheckBox.setDisable(!hasClimateData);

        smoothCheckBox.setDisable(!session.hasPreciseHeights());
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
     * Snapshot the current physical context and ask the atmosphere renderer to
     * rebuild the shell. Phase 4.2 delegated the actual geometry / colour
     * decisions to {@link PlanetAtmosphereRenderer}.
     */
    private void createAtmosphere() {
        atmosphereRenderer.render(determineTerrainType(),
                session.waterFractionForRendering(),
                session.surfaceTemperatureK());
    }

    private void updateAxialTilt() {
        axialTiltRotate.setAngle(session.axialTilt());
    }

    /** Phase 4.2: delegate to {@link PlanetPoleMarker}. */
    private void createPoleMarker() {
        poleMarker.render();
    }

    /**
     * Render the planet terrain mesh.
     */
    private void renderPlanet() {
        planetGroup.getChildren().clear();

        GeneratedPlanet planet = session.planet();
        List<Polygon> polygons = planet.polygons();
        int[] heights = planet.heights();
        boolean[] lakeMask = planet.lakeMask();
        int[] renderHeights = heights;
        double[] preciseHeights = session.preciseHeights();
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

        double[] rainfall = session.rainfall();
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
            Map<Integer, TriangleMesh> meshByHeight = session.adjacency() != null
                ? JavaFxPlanetMeshConverter.convertByHeightWithAveraging(
                    polygons, renderHeights, session.adjacency(), PLANET_SCALE, renderPreciseHeights)
                : JavaFxPlanetMeshConverter.convertByHeight(polygons, renderHeights, PLANET_SCALE);

            // Determine terrain type based on water fraction and temperature
            // - DRY: No water (browns/tans)
            // - ICE: Water but frozen (whites/light blues)
            // - WET: Liquid water (ocean blues)
            TerrainType terrainType = determineTerrainType();

            for (Map.Entry<Integer, TriangleMesh> entry : meshByHeight.entrySet()) {
                int height = entry.getKey();
                TriangleMesh mesh = entry.getValue();

                MeshView meshView = new MeshView(mesh);
                meshView.setMaterial(JavaFxPlanetMeshConverter.createMaterialForHeight(height, terrainType));
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
        if (showRivers && planet.rivers() != null && !planet.rivers().isEmpty()) {
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

        // Ensure pole markers are restored after clearing the planet group.
        createPoleMarker();
    }

    /**
     * Add river visualization as gradient-colored lines.
     */
    /**
     * Phase 4.2: river-network rendering lives in {@link RiverNetworkRenderer}.
     * The dialog still owns the toggle (useFlowAccumulationRivers) — the
     * renderer is a one-shot scene-graph builder.
     */
    private void addRivers() {
        new RiverNetworkRenderer(session.planet(), planetGroup, PLANET_SCALE,
                useFlowAccumulationRivers).render();
    }

    /**
     * Phase 4.2: plate-boundary rendering lives in {@link PlateBoundaryRenderer}.
     */
    private void addPlateBoundaries() {
        new PlateBoundaryRenderer(session.planet(), session.adjacency(), session.plateAssignment(),
                session.boundaryAnalysis(), planetGroup, PLANET_SCALE).render();
    }

    /**
     * Add climate zone visualization.
     */
    /**
     * Phase 4.2: climate-zone latitude rings live in
     * {@link PlanetClimateZoneOverlay}. The dialog just gates on whether the
     * generator produced climate data.
     */
    private void addClimateZones() {
        if (!session.hasClimateData()) {
            return;
        }
        PlanetClimateZoneOverlay.render(planetGroup, PLANET_SCALE);
    }

    /**
     * Create the LEGEND section. Phase 4.2 extracted the builder into
     * {@link PlanetLegendSection}; this dialog just decides whether to include
     * the plate-boundary key based on whether plate analysis is available.
     */
    private TitledPane createLegendSection() {
        return PlanetLegendSection.create(session.hasPlateData());
    }

    /**
     * Save the current view as a PNG screenshot. Pauses auto-rotation around
     * the capture so the saved frame is stable.
     * <p>
     * The PNG capture itself is delegated to {@link PlanetScreenshotExporter}
     * (Phase 4.2).
     */
    private void saveScreenshot() {
        boolean wasRotating = cameraController.isAutoRotate();
        if (wasRotating) {
            cameraController.setAutoRotate(false);
        }
        String suggestedName = planetName.replaceAll("[^a-zA-Z0-9]", "_") + "_terrain.png";
        PlanetScreenshotExporter.saveSnapshot(subScene, suggestedName,
                getDialogPane().getScene().getWindow());
        if (wasRotating) {
            cameraController.setAutoRotate(true);
        }
    }
}
