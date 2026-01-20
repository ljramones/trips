package com.teamgannon.trips.graphics.panes;

import com.teamgannon.trips.config.application.ScreenSize;
import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.dialogs.solarsystem.PlanetEditResult;
import com.teamgannon.trips.dialogs.solarsystem.ProceduralPlanetViewerDialog;
import com.teamgannon.trips.events.ContextSelectionType;
import com.teamgannon.trips.events.ContextSelectorEvent;
import com.teamgannon.trips.events.SolarSystemAnimationEvent;
import com.teamgannon.trips.events.SolarSystemCameraEvent;
import com.teamgannon.trips.events.SolarSystemDisplayToggleEvent;
import com.teamgannon.trips.events.SolarSystemScaleEvent;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.ExoPlanet;
import com.teamgannon.trips.planetary.PlanetaryContext;
import com.teamgannon.trips.planetarymodelling.PlanetDescription;
import com.teamgannon.trips.planetarymodelling.SolarSystemDescription;
import com.teamgannon.trips.planetarymodelling.procedural.PlanetConfig;
import com.teamgannon.trips.planetarymodelling.procedural.PlanetGenerator;
import com.teamgannon.trips.planetarymodelling.procedural.ProceduralPlanetPersistenceHelper;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.SolarSystemService;
import com.teamgannon.trips.solarsystem.SolarSystemContextMenuFactory;
import com.teamgannon.trips.solarsystem.SolarSystemContextMenuHandler;
import com.teamgannon.trips.solarsystem.animation.OrbitalAnimationController;
import com.teamgannon.trips.solarsystem.rendering.SolarSystemRenderer;
import javafx.animation.RotateTransition;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.geometry.Rectangle2D;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This is used to display a solar system
 * <p>
 * Created by larrymitchell on 2017-02-05.
 */
@Slf4j
@Component
public class SolarSystemSpacePane extends Pane implements SolarSystemContextMenuHandler {

    /**
     * rotation angle controls
     */
    private static final double ROTATE_SECS = 60;
    private final Rotate rotateX = new Rotate(25, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(25, Rotate.Y_AXIS);
    private final Rotate rotateZ = new Rotate(0, Rotate.Z_AXIS);
    private final Translate worldTranslate = new Translate(0, 0, 0);
    private final TripsContext tripsContext;
    private final ApplicationEventPublisher eventPublisher;
    private final SolarSystemService solarSystemService;
    private final SolarSystemContextMenuFactory contextMenuFactory;
    private final DatabaseManagementService databaseManagementService;

    // mouse positions
    private double mousePosX, mousePosY = 0;
    private double mouseOldX, mouseOldY = 0;
    private double mouseDeltaX, mouseDeltaY = 0;

    /**
     * graphical groups
     */
    private final Group world = new Group();
    private final Group root = new Group();
    private final Group starNameGroup = new Group();

    /**
     * 2D label display group - added to sceneRoot for flat labels that face the camera
     */
    private final Group labelDisplayGroup = new Group();

    /**
     * contains all the entities in the solar system
     */
    private final Group systemEntityGroup = new Group();

    private static final double LABEL_COLLISION_PADDING = 4.0;
    private static final double LABEL_MOVE_EPSILON = 0.5;

    /**
     * Whether labels are currently visible
     */
    private boolean labelsOn = true;

    /**
     * the subscene which is used for a glass pane flat screen
     */
    private final SubScene subScene;

    /**
     * the perspective camera for selecting views on the scene
     */
    private final PerspectiveCamera camera = new PerspectiveCamera(true);

    /**
     * the depth of the screen in pixels
     */
    private final double depth;


    /**
     * animation rotator
     */
    private RotateTransition rotator;

    /**
     * animation toggle
     */
    private final boolean animationPlay = false;

    /**
     * The renderer that creates all 3D visualization elements
     */
    private final SolarSystemRenderer solarSystemRenderer;
    private final SolarSystemCameraController cameraController;

    /**
     * The current solar system description being displayed
     */
    private SolarSystemDescription currentSystem;
    private Node selectedNode;
    private final Map<Node, Point2D> lastLabelPositions = new HashMap<>();

    /**
     * Animation controller for orbital dynamics
     */
    private OrbitalAnimationController animationController;

    /**
     * constructor
     *
     * @param tripsContext              the trips context
     * @param databaseManagementService the database management service
     */
    public SolarSystemSpacePane(TripsContext tripsContext,
                                ApplicationEventPublisher eventPublisher,
                                DatabaseManagementService databaseManagementService,
                                SolarSystemService solarSystemService,
                                SolarSystemContextMenuFactory contextMenuFactory) {

        this.tripsContext = tripsContext;
        this.eventPublisher = eventPublisher;
        this.solarSystemService = solarSystemService;
        this.contextMenuFactory = contextMenuFactory;
        ScreenSize screenSize = tripsContext.getScreenSize();
        this.databaseManagementService = databaseManagementService;

        this.depth = screenSize.getDepth();

        // Initialize the solar system renderer
        this.solarSystemRenderer = new SolarSystemRenderer();
        this.cameraController = new SolarSystemCameraController(camera, rotateX, rotateY, rotateZ, worldTranslate, this::updateLabels);

        // Set up context menu handler
        this.solarSystemRenderer.setContextMenuHandler(this);

        // Add the system entity group to world (will hold rendered solar system)
        world.getChildren().add(systemEntityGroup);

        // attach our custom rotation transforms so we can update the labels dynamically
        world.getTransforms().addAll(worldTranslate, rotateX, rotateY, rotateZ);

        subScene = new SubScene(world, screenSize.getSceneWidth(), screenSize.getSceneHeight(), true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.BLACK);

        setInitialView();

        subScene.setCamera(camera);
        Group sceneRoot = new Group(subScene);

        sceneRoot.getChildren().add(starNameGroup);

        // Add label display group to sceneRoot (2D overlay) - labels stay flat to camera
        sceneRoot.getChildren().add(labelDisplayGroup);
        labelDisplayGroup.setMouseTransparent(true);

        this.setBackground(Background.EMPTY);
        this.getChildren().add(sceneRoot);
        this.setPickOnBounds(false);

        subScene.widthProperty().bind(this.widthProperty());
        subScene.heightProperty().bind(this.heightProperty());

        root.getChildren().add(this);

        handleMouseEvents();
    }

    /**
     * Update label positions to follow their associated 3D nodes.
     * Labels stay flat (billboard-style) because they're in a 2D overlay group.
     *
     * This follows the same pattern as StarPlotManager.updateLabels() -
     * using getBoundsInParent() to calculate offset and Translate transforms.
     */
    public void updateLabels() {
        if (!labelsOn) {
            return;
        }

        Map<Node, Label> shapeToLabel = solarSystemRenderer.getShapeToLabel();
        if (shapeToLabel.isEmpty()) {
            return;
        }

        List<LabelCandidate> candidates = new java.util.ArrayList<>(shapeToLabel.size());
        for (Map.Entry<Node, Label> entry : shapeToLabel.entrySet()) {
            Node node = entry.getKey();
            Label label = entry.getValue();

            // Skip nodes with invalid coordinates
            if (Double.isNaN(node.getTranslateX())) {
                label.setVisible(false);
                continue;
            }

            // Get 3D node's position in root Scene coordinates
            Point3D coordinates = node.localToScene(Point3D.ZERO, true);

            // Skip if localToScene returned NaN
            if (Double.isNaN(coordinates.getX()) || Double.isNaN(coordinates.getY())) {
                label.setVisible(false);
                continue;
            }

            double distanceToCamera = Math.abs(coordinates.getZ() - camera.getTranslateZ());
            candidates.add(new LabelCandidate(node, label, coordinates, distanceToCamera));
        }

        candidates.sort((a, b) -> Double.compare(a.distanceToCamera(), b.distanceToCamera()));
        List<Rectangle2D> occupied = new java.util.ArrayList<>();

        for (LabelCandidate candidate : candidates) {
            Node node = candidate.node();
            Label label = candidate.label();
            Point2D localPoint = labelDisplayGroup.sceneToLocal(candidate.scenePoint().getX(),
                    candidate.scenePoint().getY());
            double x = localPoint.getX();
            double y = localPoint.getY();

            // Clipping Logic - keep labels within the visible overlay
            if (x < 20 || x > (subScene.getWidth() - 20)) {
                label.setVisible(false);
                continue;
            } else {
                label.setVisible(true);
            }
            if (y < 20 || y > (subScene.getHeight() - 20)) {
                label.setVisible(false);
                continue;
            } else {
                label.setVisible(true);
            }

            label.applyCss();
            label.autosize();
            // Boundary checks
            if (x < 0) {
                x = 0;
            }
            double labelWidth = label.getWidth();
            double labelHeight = label.getHeight();
            if ((x + labelWidth + 5) > subScene.getWidth()) {
                x = subScene.getWidth() - (labelWidth + 5);
            }
            if (y < 0) {
                y = 0;
            }
            if ((y + labelHeight) > subScene.getHeight()) {
                y = subScene.getHeight() - (labelHeight + 5);
            }

            Rectangle2D bounds = new Rectangle2D(
                    x - LABEL_COLLISION_PADDING,
                    y - LABEL_COLLISION_PADDING,
                    labelWidth + (LABEL_COLLISION_PADDING * 2),
                    labelHeight + (LABEL_COLLISION_PADDING * 2));
            boolean collides = false;
            for (Rectangle2D occupiedBounds : occupied) {
                if (occupiedBounds.intersects(bounds)) {
                    collides = true;
                    break;
                }
            }
            if (collides) {
                label.setVisible(false);
                continue;
            }
            occupied.add(bounds);

            Point2D lastPosition = lastLabelPositions.get(node);
            if (lastPosition != null) {
                double dx = Math.abs(lastPosition.getX() - x);
                double dy = Math.abs(lastPosition.getY() - y);
                if (dx < LABEL_MOVE_EPSILON && dy < LABEL_MOVE_EPSILON) {
                    continue;
                }
            }

            // Use Translate transform - same as StarPlotManager
            label.getTransforms().setAll(new Translate(x, y));
            lastLabelPositions.put(node, new Point2D(x, y));
        }

        // Update moon orbit visibility based on current zoom level
        solarSystemRenderer.updateMoonOrbitVisibility(camera.getTranslateZ());
    }

    /**
     * Toggle label visibility
     *
     * @param labelsOn true to show labels
     */
    public void toggleLabels(boolean labelsOn) {
        this.labelsOn = labelsOn;
        labelDisplayGroup.setVisible(labelsOn);
    }

    public void toggleEclipticPlane(boolean enabled) {
        solarSystemRenderer.setShowEclipticPlane(enabled);
    }

    public void toggleOrbitNodes(boolean enabled) {
        solarSystemRenderer.setShowOrbitNodes(enabled);
    }

    public void toggleApsides(boolean enabled) {
        solarSystemRenderer.setShowApsides(enabled);
    }

    public void toggleOrbits(boolean enabled) {
        solarSystemRenderer.setShowOrbits(enabled);
    }

    public void toggleHabitableZone(boolean enabled) {
        solarSystemRenderer.setShowHabitableZone(enabled);
    }

    public void toggleScaleGrid(boolean enabled) {
        solarSystemRenderer.setShowScaleGrid(enabled);
    }

    public void toggleRelativePlanetSizes(boolean enabled) {
        solarSystemRenderer.setUseRelativePlanetSizes(enabled);
        // Re-render to apply new planet sizing
        if (currentSystem != null) {
            refreshCurrentSystem();
        }
    }

    @EventListener
    public void onSolarSystemDisplayToggleEvent(SolarSystemDisplayToggleEvent event) {
        log.info("Solar system display toggle: {} -> {}", event.getToggleType(), event.isEnabled());
        switch (event.getToggleType()) {
            case ECLIPTIC_PLANE -> toggleEclipticPlane(event.isEnabled());
            case ORBIT_NODES -> toggleOrbitNodes(event.isEnabled());
            case APSIDES -> toggleApsides(event.isEnabled());
            case ORBITS -> toggleOrbits(event.isEnabled());
            case LABELS -> toggleLabels(event.isEnabled());
            case HABITABLE_ZONE -> toggleHabitableZone(event.isEnabled());
            case SCALE_GRID -> toggleScaleGrid(event.isEnabled());
            case RELATIVE_PLANET_SIZES -> toggleRelativePlanetSizes(event.isEnabled());
        }
    }

    @EventListener
    public void onSolarSystemScaleEvent(SolarSystemScaleEvent event) {
        log.info("Solar system scale event: {} ", event.getChangeType());
        switch (event.getChangeType()) {
            case SCALE_MODE -> {
                SolarSystemRenderer.ScaleMode mode = switch (event.getScaleMode()) {
                    case LINEAR -> SolarSystemRenderer.ScaleMode.LINEAR;
                    case LOGARITHMIC -> SolarSystemRenderer.ScaleMode.LOGARITHMIC;
                    case AUTO -> SolarSystemRenderer.ScaleMode.AUTO;
                };
                solarSystemRenderer.setScaleMode(mode);
                // Re-render the current system with new scale
                if (currentSystem != null) {
                    refreshCurrentSystem();
                }
            }
            case ZOOM_LEVEL -> {
                solarSystemRenderer.setZoomLevel(event.getZoomLevel());
                // Re-render the current system with new zoom
                if (currentSystem != null) {
                    refreshCurrentSystem();
                }
            }
        }
    }

    @EventListener
    public void onSolarSystemAnimationEvent(SolarSystemAnimationEvent event) {
        if (animationController == null) {
            log.warn("Animation event received but no animation controller available");
            return;
        }

        log.info("Animation event: {}", event.getAction());
        switch (event.getAction()) {
            case PLAY -> animationController.play();
            case PAUSE -> animationController.pause();
            case TOGGLE_PLAY_PAUSE -> animationController.togglePlayPause();
            case RESET -> animationController.stop();
            case SET_SPEED -> animationController.setSpeed(event.getSpeedMultiplier());
        }
    }


    /**
     * set the system to show
     *
     * @param starDisplayRecord object properties of this system
     */
    public void setSystemToDisplay(@NotNull StarDisplayRecord starDisplayRecord) {
        String systemName = starDisplayRecord.getStarName();
        createScaleLegend(systemName);

        // get the solar system description
        SolarSystemDescription solarSystemDescription = solarSystemService.getSolarSystem(starDisplayRecord);

        // render the solar system
        render(solarSystemDescription);
    }


    /**
     * Render the solar system using the SolarSystemRenderer
     *
     * @param solarSystemDescription the system to render
     */
    private void render(SolarSystemDescription solarSystemDescription) {
        // Store current system for reference
        this.currentSystem = solarSystemDescription;

        // Clean up previous animation if any
        cleanupAnimation();

        // Clear previous rendering
        systemEntityGroup.getChildren().clear();
        labelDisplayGroup.getChildren().clear();

        if (solarSystemDescription == null) {
            log.warn("Cannot render null solar system");
            return;
        }

        // Use the renderer to create all 3D elements
        Group renderedSystem = solarSystemRenderer.render(solarSystemDescription);

        // Add the rendered system to our entity group
        systemEntityGroup.getChildren().add(renderedSystem);

        // Create labels for all rendered objects and add to 2D overlay
        createLabelsForRenderedObjects(solarSystemDescription);

        // Initial label positioning
        solarSystemRenderer.clearSelection();
        updateLabels();

        // Log summary
        int planetCount = solarSystemDescription.getPlanetDescriptionList().size();
        String systemName = solarSystemDescription.getStarDisplayRecord() != null
                ? solarSystemDescription.getStarDisplayRecord().getStarName()
                : "Unknown";
        log.info("Rendered solar system '{}' with {} planets, HZ: {}-{} AU",
                systemName,
                planetCount,
                solarSystemDescription.getHabitableZoneInnerAU(),
                solarSystemDescription.getHabitableZoneOuterAU());

        // Initialize orbital animation
        initializeAnimation(solarSystemDescription);
    }

    /**
     * Create 2D labels for all rendered 3D objects (planets and stars).
     * Labels are added to the 2D labelDisplayGroup and registered with the renderer.
     *
     * @param solarSystemDescription the system being rendered
     */
    private void createLabelsForRenderedObjects(SolarSystemDescription solarSystemDescription) {
        Map<String, javafx.scene.shape.Sphere> planetNodes = solarSystemRenderer.getPlanetNodes();

        Map<String, PlanetDescription> planetsById = new HashMap<>();
        Map<String, Integer> moonCountsByParentId = new HashMap<>();

        if (solarSystemDescription != null && solarSystemDescription.getPlanetDescriptionList() != null) {
            for (PlanetDescription planet : solarSystemDescription.getPlanetDescriptionList()) {
                if (planet == null) {
                    continue;
                }
                planetsById.put(planet.getId(), planet);
                if (planet.isMoon() && planet.getParentPlanetId() != null) {
                    moonCountsByParentId.merge(planet.getParentPlanetId(), 1, Integer::sum);
                }
            }
        }

        // Create labels for planets/moons (skip moon labels if too close to parent)
        if (solarSystemDescription != null && solarSystemDescription.getPlanetDescriptionList() != null) {
            for (PlanetDescription planet : solarSystemDescription.getPlanetDescriptionList()) {
                if (planet == null) {
                    continue;
                }
                javafx.scene.shape.Sphere planetSphere = planetNodes.get(planet.getName());
                if (planetSphere == null) {
                    continue;
                }

                if (planet.isMoon()) {
                    PlanetDescription parent = planetsById.get(planet.getParentPlanetId());
                    if (parent != null) {
                        javafx.scene.shape.Sphere parentSphere = planetNodes.get(parent.getName());
                        if (parentSphere != null && isMoonLabelTooClose(planetSphere, parentSphere)) {
                            continue;
                        }
                    }
                }

                String labelText = planet.getName() != null ? planet.getName() : "Unknown";
                if (!planet.isMoon()) {
                    int moonCount = moonCountsByParentId.getOrDefault(planet.getId(), 0);
                    if (moonCount > 0) {
                        labelText = labelText + " (" + moonCount + ")";
                    }
                }

                Label label = solarSystemRenderer.createLabel(labelText);
                label.setLabelFor(planetSphere);
                labelDisplayGroup.getChildren().add(label);
                solarSystemRenderer.registerLabel(planetSphere, label);
            }
        }

        // Create label for the central star
        if (solarSystemDescription.getStarDisplayRecord() != null) {
            String starName = solarSystemDescription.getStarDisplayRecord().getStarName();
            // Find the star sphere in the planets group (it's rendered there)
            Group planetsGroup = solarSystemRenderer.getPlanetsGroup();
            for (Node node : planetsGroup.getChildren()) {
                if (node.getUserData() instanceof StarDisplayRecord star) {
                    Label label = solarSystemRenderer.createLabel(star.getStarName());
                    label.setLabelFor(node);
                    labelDisplayGroup.getChildren().add(label);
                    solarSystemRenderer.registerLabel(node, label);
                    break; // Only label the primary star for now
                }
            }
        }

        log.debug("Created {} labels for solar system objects", solarSystemRenderer.getShapeToLabel().size());
    }

    /**
     * Get the current solar system description
     *
     * @return the current system being displayed
     */
    public SolarSystemDescription getCurrentSystem() {
        return currentSystem;
    }

    /**
     * Get the renderer for external access (e.g., for animation)
     *
     * @return the solar system renderer
     */
    public SolarSystemRenderer getRenderer() {
        return solarSystemRenderer;
    }

    private record LabelCandidate(Node node, Label label, Point3D scenePoint, double distanceToCamera) {
    }

    private static final double MIN_MOON_LABEL_DISTANCE = 12.0;

    /**
     * Reset the system view
     */
    public void reset() {
        // Clean up animation
        cleanupAnimation();

        // Clear the UI legend
        starNameGroup.getChildren().clear();

        // Clear the 2D labels
        labelDisplayGroup.getChildren().clear();

        // Clear the 3D rendered elements
        systemEntityGroup.getChildren().clear();
        solarSystemRenderer.clear();

        // Clear current system reference
        currentSystem = null;
    }

    /**
     * Initialize orbital animation for the rendered system.
     *
     * @param solarSystemDescription the solar system being displayed
     */
    private void initializeAnimation(SolarSystemDescription solarSystemDescription) {
        if (solarSystemDescription == null || solarSystemDescription.getPlanetDescriptionList().isEmpty()) {
            log.warn("No planets to animate - skipping animation setup");
            return;
        }

        List<PlanetDescription> planets = solarSystemDescription.getPlanetDescriptionList();
        log.info("Setting up animation for {} planets:", planets.size());
        for (PlanetDescription p : planets) {
            log.info("  - {} : sma={} AU, period={} days, ecc={}",
                    p.getName(), p.getSemiMajorAxis(), p.getOrbitalPeriod(), p.getEccentricity());
        }

        // Create animation controller (controlled via SimulationControlPane events)
        animationController = new OrbitalAnimationController(solarSystemRenderer, this::updateLabels);
        animationController.setPlanets(planets);

        log.info("Orbital animation initialized with {} planets, planetNodes keys: {}",
                planets.size(), solarSystemRenderer.getPlanetNodes().keySet());
    }

    /**
     * Clean up animation resources.
     */
    private void cleanupAnimation() {
        if (animationController != null) {
            animationController.dispose();
            animationController = null;
        }
    }

    private boolean isMoonLabelTooClose(Sphere moon, Sphere parent) {
        double dx = moon.getTranslateX() - parent.getTranslateX();
        double dy = moon.getTranslateY() - parent.getTranslateY();
        double dz = moon.getTranslateZ() - parent.getTranslateZ();
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        return distance < MIN_MOON_LABEL_DISTANCE;
    }

    // ---------------------- helpers -------------------------- //


    /////////////////////////////////////

    /**
     * set the initial view
     */
    private void setInitialView() {
        setPerspectiveCamera();
    }

    /**
     * create the scale legend
     *
     * @param starName the star name
     */
    private void createScaleLegend(String starName) {
        // clear group to redraw
        starNameGroup.getChildren().clear();

        GridPane titlePane = new GridPane();
        titlePane.setPrefWidth(450);
        starNameGroup.getChildren().add(titlePane);

        Label starNameLabel = new Label(starName);
        starNameLabel.setFont(Font.font("Verdana", FontPosture.ITALIC, 20));
        starNameLabel.setTextFill(Color.WHEAT);

        titlePane.add(starNameLabel, 0, 0);

        Separator separator1 = new Separator();
        separator1.setMinWidth(40.0);
        titlePane.add(separator1, 1, 0);

        // setup return button to jump back to interstellar space
        Button returnButton = new Button("Jump Back");
        returnButton.setOnAction(e -> jumpBackToInterstellarSpace());
        titlePane.add(returnButton, 2, 0);

        titlePane.setTranslateX(subScene.getWidth() - 430);
        titlePane.setTranslateY(subScene.getHeight() - 30);
        titlePane.setTranslateZ(0);
    }

    /**
     * jump back to the interstellar space
     */
    private void jumpBackToInterstellarSpace() {
        // Clean up animation before leaving
        cleanupAnimation();

        // there is no specific context at the moment.  We assume the same interstellar space we came form
        eventPublisher.publishEvent(new ContextSelectorEvent(
                this,
                ContextSelectionType.INTERSTELLAR,
                null,
                new HashMap<>()));
    }

    /**
     * Get the animation controller for external control.
     *
     * @return the animation controller, or null if no system is loaded
     */
    public OrbitalAnimationController getAnimationController() {
        return animationController;
    }

    /**
     * set the perspective camera parameters
     */
    private void setPerspectiveCamera() {
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-1600);
    }

    /**
     * handle the mouse events
     */
    private void handleMouseEvents() {

        subScene.setOnScroll((ScrollEvent event) -> {
            double deltaY = event.getDeltaY();
            zoomGraph(deltaY * 5);
            updateLabels();
        });

        subScene.setOnMousePressed((MouseEvent me) -> {
                    mousePosX = me.getSceneX();
                    mousePosY = me.getSceneY();
                    mouseOldX = me.getSceneX();
                    mouseOldY = me.getSceneY();
                }
        );

        subScene.setOnMouseDragged((MouseEvent me) -> {
                    mouseOldX = mousePosX;
                    mouseOldY = mousePosY;
                    mousePosX = me.getSceneX();
                    mousePosY = me.getSceneY();
                    mouseDeltaX = (mousePosX - mouseOldX);
                    mouseDeltaY = (mousePosY - mouseOldY);
                    double modifier = 1.0;
                    double modifierFactor = 0.1;

                    // Middle mouse button OR Shift+Primary = Pan (translate)
                    if (me.isMiddleButtonDown() || (me.isPrimaryButtonDown() && me.isShiftDown())) {
                        // Pan the view - adjust world translate
                        double panSpeed = 2.0;
                        worldTranslate.setX(worldTranslate.getX() + mouseDeltaX * panSpeed);
                        worldTranslate.setY(worldTranslate.getY() + mouseDeltaY * panSpeed);
                    } else if (me.isPrimaryButtonDown()) {
                        if (me.isAltDown()) { //roll
                            rotateZ.setAngle(((rotateZ.getAngle() + mouseDeltaX * modifierFactor * modifier * 2.0) % 360 + 540) % 360 - 180); // +
                        } else {
                            rotateY.setAngle(((rotateY.getAngle() + mouseDeltaX * modifierFactor * modifier * 2.0) % 360 + 540) % 360 - 180); // +
                            rotateX.setAngle(
                                    (((rotateX.getAngle() - mouseDeltaY * modifierFactor * modifier * 2.0) % 360 + 540) % 360 - 180)
                            ); // -
                        }
                    }
                    updateLabels();
                }
        );

        // Right-click on empty space shows "Add Planet" menu
        subScene.setOnMouseClicked((MouseEvent me) -> {
            if (me.getButton() == MouseButton.PRIMARY && !me.isConsumed()) {
                solarSystemRenderer.clearSelection();
                updateLabels();
            }
            if (me.getButton() == MouseButton.SECONDARY && !me.isConsumed()) {
                // Only show if we have a current system loaded
                if (currentSystem != null && currentSystem.getStarDisplayRecord() != null) {
                    showEmptySpaceContextMenu(me.getScreenX(), me.getScreenY());
                }
            }
        });
    }

    /**
     * Show context menu when right-clicking on empty space
     */
    private void showEmptySpaceContextMenu(double screenX, double screenY) {
        ContextMenu menu = contextMenuFactory.createEmptySpaceContextMenu(
                currentSystem,
                this::handleAddPlanet
        );
        menu.show(this, screenX, screenY);
    }

    /**
     * Handle adding a new planet to the system
     */
    private void handleAddPlanet(ExoPlanet newPlanet) {
        if (newPlanet == null) return;

        log.info("Adding new planet: {}", newPlanet.getName());

        // Save to database
        solarSystemService.addExoPlanet(newPlanet);

        // Refresh the visualization
        refreshCurrentSystem();
    }

    /**
     * do actual zoom
     *
     * @param zoomAmt the amount to zoom
     */
    private void zoomGraph(double zoomAmt) {
        double z = camera.getTranslateZ();
        double newZ = z - zoomAmt;
        camera.setTranslateZ(newZ);

        // Update moon orbit visibility based on zoom level
        solarSystemRenderer.updateMoonOrbitVisibility(newZ);
    }

    // ==================== Context Menu Handler Implementation ====================

    @Override
    public void onPlanetContextMenu(Node source, PlanetDescription planet, double screenX, double screenY) {
        log.info("Planet context menu requested for: {}", planet.getName());

        // Find the ExoPlanet entity from the database
        ExoPlanet exoPlanet = solarSystemService.findExoPlanetByName(planet.getName());

        // Get sibling planets for orbit validation
        List<PlanetDescription> siblings = currentSystem != null
                ? currentSystem.getPlanetDescriptionList()
                : List.of();

        // Create and show context menu with all options including terrain viewing
        ContextMenu menu = contextMenuFactory.createPlanetContextMenu(
                planet,
                exoPlanet,
                siblings,
                this::handlePlanetEdit,
                this::handlePlanetDelete,
                this::handleLandOnPlanet,
                this::handleViewTerrain
        );

        menu.show(source, screenX, screenY);
    }

    /**
     * Handle "Land on Planet" - switch to planetary view showing sky from planet's surface.
     */
    private void handleLandOnPlanet(ExoPlanet planet) {
        if (planet == null || currentSystem == null) {
            log.warn("Cannot land on planet: planet or system is null");
            return;
        }

        log.info("Landing on planet: {}", planet.getName());

        // Build the planetary context
        PlanetaryContext context = PlanetaryContext.builder()
                .planet(planet)
                .system(currentSystem)
                .hostStar(currentSystem.getStarDisplayRecord())
                .localTime(22.0)  // Default to night
                .viewingAzimuth(0.0)  // Looking north
                .viewingAltitude(15.0)  // Slight look-up, camera Y offset handles horizon placement
                .magnitudeLimit(6.0)  // Default naked-eye limit
                .fieldOfView(90.0)
                .showAtmosphereEffects(true)
                .build();

        // Publish event to switch to planetary view
        eventPublisher.publishEvent(new ContextSelectorEvent(
                this,
                ContextSelectionType.PLANETARY,
                currentSystem.getStarDisplayRecord(),
                context));
    }

    /**
     * Handle "View Terrain" - generate and display procedural terrain for the planet.
     */
    private void handleViewTerrain(ExoPlanet exoPlanet) {
        if (exoPlanet == null) {
            log.warn("Cannot view terrain: planet is null");
            return;
        }

        // Don't allow terrain viewing for gas giants
        if (Boolean.TRUE.equals(exoPlanet.getGasGiant())) {
            log.info("Cannot view terrain for gas giant: {}", exoPlanet.getName());
            return;
        }

        log.info("Generating procedural terrain for: {}", exoPlanet.getName());

        try {
            // Generate deterministic seed from planet ID
            long seed = exoPlanet.getId() != null ? exoPlanet.getId().hashCode() : System.nanoTime();

            // Create PlanetConfig directly from ExoPlanet properties
            PlanetConfig config = ProceduralPlanetPersistenceHelper.buildConfigFromExoPlanet(exoPlanet, seed);

            // Generate procedural terrain
            PlanetGenerator.GeneratedPlanet generated = PlanetGenerator.generate(config);

            log.info("Generated terrain with {} polygons, {} rivers",
                generated.polygons().size(),
                generated.rivers() != null ? generated.rivers().size() : 0);

            ProceduralPlanetPersistenceHelper.populateProceduralMetadata(
                exoPlanet, config, seed, generated, "ACCRETE");
            solarSystemService.updateExoPlanet(exoPlanet);

            // Show the terrain viewer dialog
            ProceduralPlanetViewerDialog dialog = new ProceduralPlanetViewerDialog(
                exoPlanet.getName(),
                generated,
                (planet, planetConfig) -> {
                    ProceduralPlanetPersistenceHelper.populateProceduralMetadata(
                        exoPlanet, planetConfig, planetConfig.seed(), planet, "USER_OVERRIDES");
                    solarSystemService.updateExoPlanet(exoPlanet);
                });
            dialog.showAndWait();

        } catch (Exception e) {
            log.error("Failed to generate terrain for planet: {}", exoPlanet.getName(), e);
        }
    }


    @Override
    public void onStarContextMenu(Node source, StarDisplayRecord star, double screenX, double screenY) {
        log.info("Star context menu requested for: {}", star.getStarName());

        // Create and show context menu
        ContextMenu menu = contextMenuFactory.createStarContextMenu(
                star,
                this::jumpBackToInterstellarSpace
        );

        menu.show(source, screenX, screenY);
    }

    @Override
    public void onOrbitContextMenu(Node source, PlanetDescription planet, double screenX, double screenY) {
        log.info("Orbit context menu requested for planet: {}", planet.getName());

        // Delegate to planet context menu
        onPlanetContextMenu(source, planet, screenX, screenY);
    }

    @Override
    public void onPlanetSelected(Node source, PlanetDescription planet) {
        selectedNode = source;
        solarSystemRenderer.selectPlanet(planet);
        updateLabels();
    }

    @Override
    public void onStarSelected(Node source, StarDisplayRecord star) {
        selectedNode = source;
        solarSystemRenderer.selectStar(star);
        updateLabels();
    }

    @Override
    public void onOrbitSelected(Node source, PlanetDescription planet) {
        Node planetNode = solarSystemRenderer.getPlanetNodes().get(planet.getName());
        selectedNode = planetNode != null ? planetNode : source;
        solarSystemRenderer.selectPlanet(planet);
        updateLabels();
    }

    @EventListener
    public void onSolarSystemCameraEvent(SolarSystemCameraEvent event) {
        switch (event.getAction()) {
            case TOP_DOWN -> cameraController.animatePreset(90, 0, 0);
            case EDGE_ON -> cameraController.animatePreset(0, 90, 0);
            case OBLIQUE -> cameraController.animatePreset(35, 45, 0);
            case FOCUS_SELECTED -> cameraController.focusOn(selectedNode, world);
            case RESET_VIEW -> cameraController.resetView();
        }
    }

    /**
     * Handle planet edit result from properties dialog.
     */
    private void handlePlanetEdit(PlanetEditResult result) {
        if (!result.isChanged()) {
            return;
        }

        log.info("Planet edited: {}, orbital changed: {}", result.getPlanet().getName(), result.isOrbitalChanged());

        // Persist changes to database
        solarSystemService.updateExoPlanet(result.getPlanet());

        // If orbital properties changed, refresh the visualization
        if (result.isOrbitalChanged() && currentSystem != null) {
            refreshCurrentSystem();
        }
    }

    /**
     * Handle planet deletion.
     */
    private void handlePlanetDelete(ExoPlanet planet) {
        log.info("Deleting planet: {}", planet.getName());

        // Delete from database
        solarSystemService.deleteExoPlanet(planet.getId());

        // Refresh the visualization
        if (currentSystem != null) {
            refreshCurrentSystem();
        }
    }

    /**
     * Refresh the current solar system display after edits.
     */
    private void refreshCurrentSystem() {
        if (currentSystem == null || currentSystem.getStarDisplayRecord() == null) {
            return;
        }

        // Re-fetch the solar system data and re-render
        StarDisplayRecord star = currentSystem.getStarDisplayRecord();
        SolarSystemDescription refreshedSystem = solarSystemService.getSolarSystem(star);
        render(refreshedSystem);

        log.info("Refreshed solar system display after edit");
    }

}
