package com.teamgannon.trips.graphics.panes;

import com.teamgannon.trips.config.application.ScreenSize;
import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.events.ContextSelectionType;
import com.teamgannon.trips.events.ContextSelectorEvent;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.nightsky.bridge.PlanetarySkyBridgeService;
import com.teamgannon.trips.planetary.PlanetaryContext;
import com.teamgannon.trips.planetary.rendering.PlanetarySkyRenderer;
import com.teamgannon.trips.nightsky.model.PlanetarySkyModel;
import javafx.scene.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.transform.Rotate;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import com.teamgannon.trips.jpa.repository.StarObjectRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Displays a night sky view from a planet's surface.
 * Shows stars repositioned based on the planet's location in 3D space,
 * with the host star visible as the "sun" and proper magnitude adjustments.
 */
@Slf4j
@Component
public class PlanetarySpacePane extends Pane {

    /**
     * Rotation controls
     */
    private final Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    private final Rotate rotateZ = new Rotate(0, Rotate.Z_AXIS);

    private final TripsContext tripsContext;
    private final ApplicationEventPublisher eventPublisher;
    private final PlanetarySkyBridgeService skyBridgeService;

    /**
     * Graphical groups
     */
    private final Group world = new Group();
    private final Group root = new Group();
    private final Group labelGroup = new Group();        // For title/legend
    private final Group starLabelGroup = new Group();    // For star billboard labels
    private final Group skyGroup = new Group();
    private final Canvas orientationCanvas = new Canvas();

    /**
     * SubScene for 3D rendering
     */
    private final SubScene subScene;

    /**
     * Perspective camera
     */
    private final PerspectiveCamera camera = new PerspectiveCamera(true);

    /**
     * Sky renderer
     */
    @Getter
    private final PlanetarySkyRenderer skyRenderer;

    /**
     * Helper classes
     */
    private final PlanetaryLabelManager labelManager;
    private final OrientationGridRenderer gridRenderer;
    private final StarDataConverter starDataConverter;

    /**
     * Current planetary context
     */
    @Getter
    private PlanetaryContext currentContext;

    /**
     * List of nearby stars for rendering
     */
    private List<StarDisplayRecord> nearbyStars = new ArrayList<>();
    private List<PlanetarySkyRenderer.BrightStarEntry> computedBrightestStars = new ArrayList<>();
    private int visibleStarCount = 0;

    /**
     * Constructor
     */
    public PlanetarySpacePane(TripsContext tripsContext,
                              ApplicationEventPublisher eventPublisher,
                              StarObjectRepository starObjectRepository,
                              PlanetarySkyBridgeService skyBridgeService) {
        this.tripsContext = tripsContext;
        this.eventPublisher = eventPublisher;
        this.skyBridgeService = skyBridgeService;

        ScreenSize screenSize = tripsContext.getScreenSize();

        // Initialize the sky renderer
        this.skyRenderer = new PlanetarySkyRenderer();

        // Add the sky group to world
        world.getChildren().add(skyGroup);

        // Attach rotation transforms
        world.getTransforms().addAll(rotateX, rotateY, rotateZ);

        subScene = new SubScene(world, screenSize.getSceneWidth(), screenSize.getSceneHeight(),
                true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.BLACK);

        setInitialView();

        subScene.setCamera(camera);
        Group sceneRoot = new Group(subScene);
        orientationCanvas.setMouseTransparent(true);
        orientationCanvas.setVisible(false);
        orientationCanvas.widthProperty().bind(subScene.widthProperty());
        orientationCanvas.heightProperty().bind(subScene.heightProperty());
        sceneRoot.getChildren().add(orientationCanvas);
        sceneRoot.getChildren().add(starLabelGroup);  // Star labels (billboard style)
        sceneRoot.getChildren().add(labelGroup);       // Title/legend on top

        this.setBackground(Background.EMPTY);
        this.getChildren().add(sceneRoot);
        this.setPickOnBounds(false);

        subScene.widthProperty().bind(this.widthProperty());
        subScene.heightProperty().bind(this.heightProperty());

        root.getChildren().add(this);

        // Initialize helper classes (before adding listeners that depend on them)
        this.labelManager = new PlanetaryLabelManager(starLabelGroup, subScene, camera, skyRenderer::getShapeToLabel);
        this.gridRenderer = new OrientationGridRenderer(orientationCanvas, world, skyRenderer, this::getCurrentContext);
        this.starDataConverter = new StarDataConverter();

        // Add size change listeners after gridRenderer is initialized
        subScene.widthProperty().addListener((obs, oldVal, newVal) -> gridRenderer.redraw());
        subScene.heightProperty().addListener((obs, oldVal, newVal) -> gridRenderer.redraw());

        // Initialize mouse handler
        PlanetaryMouseHandler mouseHandler = new PlanetaryMouseHandler(
                subScene, camera, rotateX, rotateY, this::onViewChanged);
        mouseHandler.initialize();
    }

    /**
     * Callback when view changes (rotation or zoom).
     */
    private void onViewChanged() {
        gridRenderer.redraw();
        labelManager.updateLabels();
    }

    /**
     * Set the planetary context and render the sky.
     *
     * @param context the planetary viewing context
     */
    public void setContext(PlanetaryContext context) {
        this.currentContext = context;

        if (context == null) {
            log.warn("Null planetary context provided");
            return;
        }

        // Create the title legend
        createLegend(context);

        setViewingDirection(context.getViewingAzimuth(), context.getViewingAltitude());

        // Render the sky dome
        recomputeSky();
    }

    /**
     * Set the list of nearby stars for rendering.
     *
     * @param stars list of star display records
     */
    public void setNearbyStars(List<StarDisplayRecord> stars) {
        this.nearbyStars = stars != null ? new ArrayList<>(stars) : new ArrayList<>();
    }

    /**
     * Render the sky dome.
     */
    private void renderSky() {
        // Clear previous rendering
        skyGroup.getChildren().clear();

        // Clear old labels - new ones will be created by the renderer
        labelManager.clear();

        if (currentContext == null) {
            return;
        }

        // Use the renderer to create the sky dome
        Group renderedSky = skyRenderer.render(currentContext, nearbyStars);
        skyGroup.getChildren().add(renderedSky);

        log.info("Rendered planetary sky from {}", currentContext.getPlanetName());
    }

    public void recomputeSky() {
        if (currentContext == null) {
            return;
        }

        // Use the efficient top-level services with caching and spatial queries
        String datasetName = (tripsContext.getDataSetContext() != null
                && tripsContext.getDataSetContext().getDescriptor() != null)
                ? tripsContext.getDataSetContext().getDescriptor().getDataSetName()
                : null;
        PlanetarySkyModel model = skyBridgeService.computeSky(currentContext, datasetName);
        log.debug("Sky computed using PlanetarySkyBridgeService");

        nearbyStars = starDataConverter.toStarDisplayRecords(model.getVisibleStars());
        computedBrightestStars = starDataConverter.toBrightestEntries(model.getTopBrightest());
        visibleStarCount = model.getVisibleCount();
        boolean isDay = model.getHostStarAltitudeDeg() > 0.0;
        subScene.setFill(isDay ? Color.web("#6FA9E6") : Color.BLACK);
        currentContext.setShowHostStar(isDay);
        currentContext.setShowHorizon(!isDay);
        currentContext.setDaylight(isDay);
        currentContext.setHostStarAltitudeDeg(model.getHostStarAltitudeDeg());
        renderSky();
        gridRenderer.redraw();
        labelManager.updateLabels();
    }

    public void updateLocalTime(double time) {
        if (currentContext == null) {
            return;
        }
        currentContext.setLocalTime(time);
        recomputeSky();
    }

    public void updateMagnitudeLimit(double magnitudeLimit) {
        if (currentContext == null) {
            return;
        }
        currentContext.setMagnitudeLimit(magnitudeLimit);
        recomputeSky();
    }

    public void updateAtmosphere(boolean enabled) {
        if (currentContext == null) {
            return;
        }
        currentContext.setShowAtmosphereEffects(enabled);
        renderSky();
    }

    public void updateOrientationGrid(boolean enabled) {
        if (currentContext == null) {
            return;
        }
        currentContext.setShowOrientationGrid(enabled);
        skyRenderer.setOrientationGridVisible(enabled);
        gridRenderer.redraw();
    }

    public void updateLabelMagnitudeLimit(double limit) {
        skyRenderer.setLabelMagnitudeLimit(limit);
        // Re-render to apply the new limit
        recomputeSky();
    }

    /**
     * Set callback for when a star is clicked.
     */
    public void setOnStarClicked(java.util.function.Consumer<PlanetarySkyRenderer.BrightStarEntry> callback) {
        skyRenderer.setOnStarClicked(callback);
    }

    /**
     * Reset the view.
     * Camera must be INSIDE the sky dome (radius=500) for CullFace.FRONT to work.
     */
    public void reset() {
        log.info(">>> reset CALLED - rotateX BEFORE: {}", rotateX.getAngle());
        labelGroup.getChildren().clear();
        labelManager.clear();
        skyGroup.getChildren().clear();
        skyRenderer.clear();
        currentContext = null;
        nearbyStars.clear();

        // Reset camera position but NOT rotateX/Y - those are set by setContext
        rotateZ.setAngle(0);
        camera.setNearClip(5.0);
        camera.setTranslateY(-150);  // Vertical offset to push horizon down
        camera.setTranslateZ(-300);  // Inside the 500-radius dome
        log.info("    reset done - rotateX AFTER: {}", rotateX.getAngle());
    }

    /**
     * Set initial camera view.
     * Camera must be INSIDE the sky dome (radius=500) for CullFace.FRONT to work.
     */
    private void setInitialView() {
        log.info(">>> setInitialView CALLED - rotateX BEFORE: {}", rotateX.getAngle());
        camera.setNearClip(5.0);
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-300);  // Inside the 500-radius dome (was -800)
        camera.setTranslateY(-150);  // Vertical offset to push horizon down

        // NOTE: Do NOT set rotateX here - let setContext/setViewingDirection control it
        log.info("    setInitialView done - rotateX AFTER: {}", rotateX.getAngle());
    }

    /**
     * Create the title legend.
     */
    private void createLegend(PlanetaryContext context) {
        labelGroup.getChildren().clear();

        GridPane titlePane = new GridPane();
        titlePane.setPrefWidth(550);
        labelGroup.getChildren().add(titlePane);

        // Planet name label
        Label locationLabel = new Label("Sky from " + context.getPlanetName());
        locationLabel.setFont(Font.font("Verdana", FontPosture.ITALIC, 18));
        locationLabel.setTextFill(Color.WHEAT);
        titlePane.add(locationLabel, 0, 0);

        Separator separator1 = new Separator();
        separator1.setMinWidth(20);
        titlePane.add(separator1, 1, 0);

        // Host star info
        Label hostLabel = new Label("Host: " + context.getHostStarName());
        hostLabel.setFont(Font.font("Verdana", FontPosture.REGULAR, 12));
        hostLabel.setTextFill(Color.LIGHTGRAY);
        titlePane.add(hostLabel, 2, 0);

        Separator separator2 = new Separator();
        separator2.setMinWidth(20);
        titlePane.add(separator2, 3, 0);

        // Return to solar system button
        Button returnToSystemButton = new Button("Return to System");
        returnToSystemButton.setOnAction(e -> returnToSolarSystem());
        titlePane.add(returnToSystemButton, 4, 0);

        Separator separator3 = new Separator();
        separator3.setMinWidth(10);
        titlePane.add(separator3, 5, 0);

        // Jump back to interstellar button
        Button jumpBackButton = new Button("Jump Back");
        jumpBackButton.setOnAction(e -> jumpBackToInterstellarSpace());
        titlePane.add(jumpBackButton, 6, 0);

        // Position at bottom of screen
        titlePane.setTranslateX(subScene.getWidth() - 530);
        titlePane.setTranslateY(subScene.getHeight() - 30);
        titlePane.setTranslateZ(0);
    }

    /**
     * Return to the solar system view.
     */
    private void returnToSolarSystem() {
        if (currentContext == null || currentContext.getHostStar() == null) {
            log.warn("No context to return to solar system");
            return;
        }

        // Publish event to switch back to solar system view
        eventPublisher.publishEvent(new ContextSelectorEvent(
                this,
                ContextSelectionType.SOLARSYSTEM,
                currentContext.getHostStar(),
                new HashMap<>()));
    }

    /**
     * Jump back to interstellar space.
     */
    private void jumpBackToInterstellarSpace() {
        eventPublisher.publishEvent(new ContextSelectorEvent(
                this,
                ContextSelectionType.INTERSTELLAR,
                null,
                new HashMap<>()));
    }

    /**
     * Update viewing direction.
     * rotateX controls pitch: positive = look up, negative = look down
     * rotateY controls azimuth: 0=North, 90=East, etc.
     */
    public void setViewingDirection(double azimuth, double altitude) {
        log.info(">>> setViewingDirection CALLED: azimuth={}, altitude={}", azimuth, altitude);
        log.info("    rotateX BEFORE: {}", rotateX.getAngle());
        log.info("    rotateY BEFORE: {}", rotateY.getAngle());

        rotateY.setAngle(-azimuth);  // NEGATED - rotating world vs rotating camera
        rotateX.setAngle(altitude);  // Positive: Y is negated in sky coords, so rotation flips

        log.info("    rotateX AFTER: {}", rotateX.getAngle());
        log.info("    rotateY AFTER: {}", rotateY.getAngle());
        log.info("    world.getTransforms() contains rotateX: {}", world.getTransforms().contains(rotateX));

        gridRenderer.redraw();
    }

    /**
     * Get the brightest stars from the current render.
     */
    public List<PlanetarySkyRenderer.BrightStarEntry> getBrightestStars() {
        return new ArrayList<>(computedBrightestStars);
    }

    public int getVisibleStarCount() {
        return visibleStarCount;
    }

    /**
     * Toggle star labels on/off.
     */
    public void setStarLabelsOn(boolean on) {
        labelManager.setStarLabelsOn(on);
    }

    public boolean isStarLabelsOn() {
        return labelManager.isStarLabelsOn();
    }

    /**
     * Update billboard-style star labels - delegates to label manager.
     */
    public void updateLabels() {
        labelManager.updateLabels();
    }
}
