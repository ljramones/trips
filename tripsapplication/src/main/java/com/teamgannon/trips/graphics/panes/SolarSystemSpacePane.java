package com.teamgannon.trips.graphics.panes;

import com.teamgannon.trips.config.application.ScreenSize;
import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.events.ContextSelectionType;
import com.teamgannon.trips.events.ContextSelectorEvent;
import com.teamgannon.trips.events.SolarSystemAnimationEvent;
import com.teamgannon.trips.events.SolarSystemCameraEvent;
import com.teamgannon.trips.events.SolarSystemDisplayToggleEvent;
import com.teamgannon.trips.events.SolarSystemScaleEvent;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.ExoPlanet;
import com.teamgannon.trips.planetarymodelling.PlanetDescription;
import com.teamgannon.trips.planetarymodelling.SolarSystemDescription;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.SolarSystemService;
import com.teamgannon.trips.solarsystem.SolarSystemContextMenuFactory;
import com.teamgannon.trips.solarsystem.SolarSystemContextMenuHandler;
import com.teamgannon.trips.solarsystem.animation.OrbitalAnimationController;
import com.teamgannon.trips.solarsystem.rendering.SolarSystemRenderer;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
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
    private final Rotate rotateX = new Rotate(25, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(25, Rotate.Y_AXIS);
    private final Rotate rotateZ = new Rotate(0, Rotate.Z_AXIS);
    private final Translate worldTranslate = new Translate(0, 0, 0);
    private final TripsContext tripsContext;
    private final ApplicationEventPublisher eventPublisher;
    private final SolarSystemService solarSystemService;
    private final SolarSystemContextMenuFactory contextMenuFactory;

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

    /**
     * the subscene which is used for a glass pane flat screen
     */
    private final SubScene subScene;

    /**
     * the perspective camera for selecting views on the scene
     */
    private final PerspectiveCamera camera = new PerspectiveCamera(true);

    /**
     * The renderer that creates all 3D visualization elements
     */
    private final SolarSystemRenderer solarSystemRenderer;
    private final SolarSystemCameraController cameraController;

    /**
     * Helper classes for managing specific responsibilities
     */
    private final SolarSystemLabelManager labelManager;
    private final SolarSystemMouseHandler mouseHandler;
    private final PlanetActionHandler planetActionHandler;

    /**
     * The current solar system description being displayed
     */
    private SolarSystemDescription currentSystem;
    private Node selectedNode;

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

        // Initialize helper classes
        this.labelManager = new SolarSystemLabelManager(labelDisplayGroup, subScene, camera, solarSystemRenderer);
        this.planetActionHandler = new PlanetActionHandler(solarSystemService, eventPublisher, this::refreshCurrentSystem);
        this.mouseHandler = new SolarSystemMouseHandler(
                subScene, camera, rotateX, rotateY, rotateZ, worldTranslate,
                solarSystemRenderer, contextMenuFactory, labelManager::throttledUpdateLabels);
        mouseHandler.setContextMenuOwner(this);
        mouseHandler.setAddPlanetCallback(this::handleAddPlanet);
        mouseHandler.initialize();
    }

    /**
     * Update label positions - delegates to label manager.
     */
    public void updateLabels() {
        labelManager.updateLabels();
    }

    /**
     * Toggle label visibility
     *
     * @param labelsOn true to show labels
     */
    public void toggleLabels(boolean labelsOn) {
        labelManager.toggleLabels(labelsOn);
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

    public void toggleRings(boolean enabled) {
        solarSystemRenderer.setShowRings(enabled);
    }

    public void toggleAsteroidBelts(boolean enabled) {
        solarSystemRenderer.setShowAsteroidBelts(enabled);
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
            case PLANETARY_RINGS -> toggleRings(event.isEnabled());
            case ASTEROID_BELTS -> toggleAsteroidBelts(event.isEnabled());
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

        // Update handlers with current system
        mouseHandler.setCurrentSystem(solarSystemDescription);
        planetActionHandler.setCurrentSystem(solarSystemDescription);

        // Clean up previous animation if any
        cleanupAnimation();

        // Clear previous rendering
        systemEntityGroup.getChildren().clear();
        labelManager.clearLabels();

        if (solarSystemDescription == null) {
            log.warn("Cannot render null solar system");
            return;
        }

        // Use the renderer to create all 3D elements
        Group renderedSystem = solarSystemRenderer.render(solarSystemDescription);

        // Add the rendered system to our entity group
        systemEntityGroup.getChildren().add(renderedSystem);

        // Create labels for all rendered objects and add to 2D overlay
        labelManager.createLabelsForRenderedObjects(solarSystemDescription);

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

    /**
     * Reset the system view
     */
    public void reset() {
        // Clean up animation
        cleanupAnimation();

        // Clear the UI legend
        starNameGroup.getChildren().clear();

        // Clear the 2D labels
        labelManager.clearLabels();

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
     * Handle adding a new planet to the system.
     */
    private void handleAddPlanet(ExoPlanet newPlanet) {
        planetActionHandler.handleAddPlanet(newPlanet);
    }

    // ==================== Context Menu Handler Implementation ====================

    @Override
    public void onPlanetContextMenu(Node source, PlanetDescription planet, double screenX, double screenY) {
        log.info("Planet context menu requested for: {}", planet.getName());

        // Find the ExoPlanet entity from the database
        ExoPlanet exoPlanet = planetActionHandler.findExoPlanetByName(planet.getName());

        // Get sibling planets for orbit validation
        List<PlanetDescription> siblings = currentSystem != null
                ? currentSystem.getPlanetDescriptionList()
                : List.of();

        // Create and show context menu with all options including terrain viewing
        ContextMenu menu = contextMenuFactory.createPlanetContextMenu(
                planet,
                exoPlanet,
                siblings,
                planetActionHandler::handlePlanetEdit,
                planetActionHandler::handlePlanetDelete,
                planetActionHandler::handleLandOnPlanet,
                planetActionHandler::handleViewTerrain
        );

        menu.show(source, screenX, screenY);
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
