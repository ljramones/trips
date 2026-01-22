package com.teamgannon.trips.starplotting;

import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.config.application.model.CurrentPlot;
import com.teamgannon.trips.config.application.model.StarDisplayPreferences;
import com.teamgannon.trips.events.*;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.graphics.entities.StarSelectionModel;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import com.teamgannon.trips.jpa.model.CivilizationDisplayPreferences;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.measure.TrackExecutionTime;
import com.teamgannon.trips.service.SolarSystemService;
import com.teamgannon.trips.service.StarService;
import com.teamgannon.trips.objects.MeshViewShapeFactory;
import com.teamgannon.trips.routing.RouteManager;
import com.teamgannon.trips.routing.RouteFindingService;
import com.teamgannon.trips.routing.dialogs.ContextAutomatedRoutingDialog;
import com.teamgannon.trips.routing.dialogs.ContextManualRoutingDialog;
import com.teamgannon.trips.routing.model.RoutingType;
import com.teamgannon.trips.screenobjects.StarEditDialog;
import com.teamgannon.trips.screenobjects.StarEditStatus;
import com.teamgannon.trips.service.measure.StarMeasurementService;
import com.teamgannon.trips.solarsystem.PlanetDialog;
import com.teamgannon.trips.solarsystem.SolarSystemGenOptions;
import com.teamgannon.trips.solarsystem.SolarSystemGenerationDialog;
import com.teamgannon.trips.solarsystem.SolarSystemReport;
import com.teamgannon.trips.solarsystem.SolarSystemSaveResult;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.transform.Rotate;
import javafx.stage.Modality;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;


import java.util.*;

import static com.teamgannon.trips.support.AlertFactory.showConfirmationAlert;
import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

@Slf4j
@Component
public class StarPlotManager {

    // =========================================================================
    // Display Constants - Star Appearance
    // =========================================================================

    /**
     * Default scale factor for special star meshes (central star, 4pt, 5pt stars).
     */
    private static final double DEFAULT_STAR_MESH_SCALE = 30.0;

    /**
     * Scale factor for smaller mesh objects (pyramid, geometric shapes).
     */
    private static final double SMALL_MESH_SCALE = 10.0;

    /**
     * Scale factor for polity indicator objects.
     */
    private static final double POLITY_MESH_SCALE = 1.0;

    /**
     * Default rotation angle for star meshes (degrees).
     */
    private static final double DEFAULT_ROTATION_ANGLE = 90.0;

    /**
     * Inverted rotation angle for some mesh objects (degrees).
     */
    private static final double INVERTED_ROTATION_ANGLE = -90.0;

    /**
     * Fallback sphere radius when mesh loading fails.
     */
    private static final double FALLBACK_SPHERE_RADIUS = 10.0;


    // =========================================================================
    // Animation Constants
    // =========================================================================

    /**
     * Duration of scale transition animation in seconds.
     */
    private static final double SCALE_TRANSITION_DURATION_SECONDS = 2.0;

    /**
     * Scale multiplier for animation start/end states.
     */
    private static final double ANIMATION_SCALE_MULTIPLIER = 2.0;

    /**
     * Number of blink cycles for star highlighting.
     */
    private static final int HIGHLIGHT_BLINK_CYCLES = 100;


    // =========================================================================
    // Random Generation Constants (Test/Debug Only)
    // =========================================================================

    /**
     * Maximum radius for randomly generated test stars.
     */
    private static final double MAX_RANDOM_STAR_RADIUS = 7.0;

    /**
     * Fraction of max range used for random star positions.
     */
    private static final double RANDOM_POSITION_FRACTION = 2.0 / 3.0;

    /**
     * Maximum X coordinate for random star generation.
     */
    private static final double X_MAX = 300.0;

    /**
     * Maximum Y coordinate for random star generation.
     */
    private static final double Y_MAX = 300.0;

    /**
     * Maximum Z coordinate for random star generation.
     */
    private static final double Z_MAX = 300.0;

    // =========================================================================
    // Instance Fields
    // =========================================================================

    /**
     * Manages coordinate scaling between light-years and screen units.
     * Provides star radius calculation, zoom support, and coordinate transformation.
     */
    @Getter
    private final InterstellarScaleManager scaleManager = new InterstellarScaleManager();


    /**
     * the stellar group for display
     */
    private final Group stellarDisplayGroup = new Group();

    /**
     * Manages star labels (creation, positioning, visibility).
     */
    @Getter
    private final StarLabelManager labelManager = new StarLabelManager();

    /**
     * Manages Level-of-Detail (LOD) for star rendering.
     * Determines appropriate detail level based on distance, magnitude, and zoom.
     */
    @Getter
    private final StarLODManager lodManager = new StarLODManager();

    /**
     * Manages extension lines from the grid to stars.
     */
    @Getter
    private final StarExtensionManager extensionManager = new StarExtensionManager();

    /**
     * Factory for creating polity indicator objects.
     */
    private final PolityObjectFactory polityObjectFactory;

    /**
     * Manages star animations (highlight blinking, rotation).
     */
    @Getter
    private final StarAnimationManager animationManager = new StarAnimationManager();

    /**
     * to hold all the polities
     */
    private final Group politiesDisplayGroup = new Group();

    private Group world;

    private SubScene subScene;

    /**
     * to make database changes
     */
    private final StarService starService;

    /**
     * to manage solar systems and planet persistence
     */
    private final SolarSystemService solarSystemService;

    /**
     * the report generator
     */
    private final ApplicationEventPublisher eventPublisher;


    /**
     * the global context of TRIPS
     */
    private final TripsContext tripsContext;

    /**
     * our color palette
     */
    private ColorPalette colorPalette;

    /**
     * used to implement a selection model for selecting stars
     */
    private final Map<Node, StarSelectionModel> selectionModel = new HashMap<>();

    /**
     * source of reasonably random numbers
     */
    private final Random random = new Random();

    /**
     * toggle state of polities
     */
    private boolean politiesOn = true;


    /**
     * reference to the Route Manager
     */
    private final RouteManager routeManager;

    /**
     * a utility class to measure specific star qualities
     */
    private final StarMeasurementService starMeasurementService;

    /**
     * service for finding routes between stars
     */
    private final RouteFindingService routeFindingService;


    private double controlPaneOffset;


    /**
     * used as a control for highlighting stars
     */
    private Node highLightStar;


    private StarDisplayPreferences starDisplayPreferences;


    private final static String CENTRAL_STAR = "centralStar";
    private final static String MORAVIAN_STAR = "moravianStar";
    private final static String FOUR_PT_STAR = "4PtStar";
    private final static String FIVE_PT_STAR = "5PtStar";
    private final static String PYRAMID = "pyramid";

    /**
     * the special objects
     */
    private final Map<String, MeshObjectDefinition> specialObjects = new HashMap<>();

    /**
     * used to create 3d mesh objects
     */
    private final MeshViewShapeFactory meshViewShapeFactory = new MeshViewShapeFactory();


    private ContextAutomatedRoutingDialog automatedRoutingDialog;

    private ContextManualRoutingDialog manualRoutingDialog;

    // =========================================================================
    // Performance Optimization Caches
    // =========================================================================

    /**
     * Cache of PhongMaterial objects by color.
     * Avoids creating duplicate materials for stars of the same color.
     * Cleared on each new plot.
     */
    private final Map<Color, PhongMaterial> materialCache = new HashMap<>();

    /**
     * Batch collection for star nodes to add to scene graph.
     * Using addAll() is more efficient than individual add() calls.
     */
    private final List<Node> pendingStarNodes = new ArrayList<>();

    /**
     * Batch collection for polity nodes.
     */
    private final List<Node> pendingPolityNodes = new ArrayList<>();

    /**
     * constructor
     *
     * @param tripsContext the trips context
     */
    public StarPlotManager(TripsContext tripsContext,
                           RouteManager routeManager,
                           StarMeasurementService starMeasurementService,
                           RouteFindingService routeFindingService,
                           StarService starService,
                           SolarSystemService solarSystemService,
                           ApplicationEventPublisher eventPublisher) {

        this.tripsContext = tripsContext;
        this.colorPalette = tripsContext.getAppViewPreferences().getColorPalette();
        this.routeManager = routeManager;
        this.starMeasurementService = starMeasurementService;
        this.routeFindingService = routeFindingService;
        this.starService = starService;
        this.solarSystemService = solarSystemService;
        this.eventPublisher = eventPublisher;

        // Initialize polity object factory
        this.polityObjectFactory = new PolityObjectFactory(meshViewShapeFactory);

        // special graphical objects in MeshView format
        loadSpecialObjects();
    }

    public void setGraphics(Group sceneRoot,
                            Group world,
                            SubScene subScene) {

        this.world = world;
        this.subScene = subScene;

        world.getChildren().add(stellarDisplayGroup);

        // Initialize label manager with scene references
        labelManager.initialize(sceneRoot, subScene);

        // Initialize extension manager
        extensionManager.initialize(world);

        world.getChildren().add(politiesDisplayGroup);
    }

    @TrackExecutionTime
    private void loadSpecialObjects() {

        // load central star
        Group centralStar = meshViewShapeFactory.starCentral();
        if (centralStar != null) {
            MeshObjectDefinition objectDefinition = MeshObjectDefinition
                    .builder()
                    .name(CENTRAL_STAR)
                    .id(UUID.randomUUID())
                    .object(centralStar)
                    .xScale(DEFAULT_STAR_MESH_SCALE)
                    .yScale(DEFAULT_STAR_MESH_SCALE)
                    .zScale(DEFAULT_STAR_MESH_SCALE)
                    .axis(Rotate.X_AXIS)
                    .rotateAngle(DEFAULT_ROTATION_ANGLE)
                    .build();
            specialObjects.put(CENTRAL_STAR, objectDefinition);
        } else {
            log.error("Unable to load the central star object");
        }

        // load 4 pt star
        Node fourPtStar = meshViewShapeFactory.star4pt();
        if (fourPtStar != null) {
            MeshObjectDefinition objectDefinition = MeshObjectDefinition
                    .builder()
                    .name(FOUR_PT_STAR)
                    .id(UUID.randomUUID())
                    .object(fourPtStar)
                    .xScale(DEFAULT_STAR_MESH_SCALE)
                    .yScale(DEFAULT_STAR_MESH_SCALE)
                    .zScale(DEFAULT_STAR_MESH_SCALE)
                    .axis(Rotate.X_AXIS)
                    .rotateAngle(DEFAULT_ROTATION_ANGLE)
                    .build();
            specialObjects.put(FOUR_PT_STAR, objectDefinition);
        } else {
            log.error("Unable to load the 4 pt star object");
        }

        // load 5 pt star
        Group fivePtStar = meshViewShapeFactory.star5pt();
        if (fivePtStar != null) {
            MeshObjectDefinition objectDefinition = MeshObjectDefinition
                    .builder()
                    .name(FIVE_PT_STAR)
                    .id(UUID.randomUUID())
                    .object(fivePtStar)
                    .xScale(DEFAULT_STAR_MESH_SCALE)
                    .yScale(DEFAULT_STAR_MESH_SCALE)
                    .zScale(DEFAULT_STAR_MESH_SCALE)
                    .axis(Rotate.X_AXIS)
                    .rotateAngle(DEFAULT_ROTATION_ANGLE)
                    .build();
            specialObjects.put(FIVE_PT_STAR, objectDefinition);
        } else {
            log.error("Unable to load the 5 pt star object");
        }

        // load pyramid
        MeshView pyramid = meshViewShapeFactory.pyramid();
        if (pyramid != null) {
            MeshObjectDefinition objectDefinition = MeshObjectDefinition
                    .builder()
                    .name(PYRAMID)
                    .id(UUID.randomUUID())
                    .object(pyramid)
                    .xScale(SMALL_MESH_SCALE)
                    .yScale(SMALL_MESH_SCALE)
                    .zScale(SMALL_MESH_SCALE)
                    .axis(Rotate.X_AXIS)
                    .rotateAngle(INVERTED_ROTATION_ANGLE)
                    .build();
            specialObjects.put(PYRAMID, objectDefinition);
        } else {
            log.error("Unable to load the pyramid object");
        }

        // load geometric shape
        MeshView geometric0 = meshViewShapeFactory.geometric0();
        if (geometric0 != null) {
            MeshObjectDefinition objectDefinition = MeshObjectDefinition
                    .builder()
                    .name("geometric0")
                    .id(UUID.randomUUID())
                    .object(geometric0)
                    .xScale(SMALL_MESH_SCALE)
                    .yScale(SMALL_MESH_SCALE)
                    .zScale(SMALL_MESH_SCALE)
                    .axis(Rotate.X_AXIS)
                    .rotateAngle(INVERTED_ROTATION_ANGLE)
                    .build();
            specialObjects.put("geometric0", objectDefinition);
        } else {
            log.error("Unable to load the geometric object");
        }

        log.info("All MeshView objects loaded");
    }

    /**
     * get the plotted stars in view
     *
     * @return the list of star display records
     */
    @TrackExecutionTime
    public @NotNull List<StarDisplayRecord> getCurrentStarsInView() {
        List<StarDisplayRecord> starsInView = new ArrayList<>();
        for (String id : tripsContext.getCurrentPlot().getStarIds()) {
            StarDisplayRecord starDisplayRecord = (StarDisplayRecord) tripsContext.getCurrentPlot().getStar(id).getUserData();
            starsInView.add(starDisplayRecord);
        }
        starsInView.sort(Comparator.comparing(StarDisplayRecord::getStarName));
        return starsInView;
    }

    /**
     * toggle the extensions
     *
     * @param extensionsOn the extensions flag
     */
    public void toggleExtensions(boolean extensionsOn) {
        extensionManager.toggleExtensions(extensionsOn);
    }

    /**
     * clear the stars from the display
     */
    public void clearStars() {

        // remove stars
        stellarDisplayGroup.getChildren().clear();
        labelManager.clear();
        politiesDisplayGroup.getChildren().clear();

        // remove the extension points to the stars
        extensionManager.clear();

        // reset LOD statistics
        lodManager.resetStatistics();

        // clear performance caches
        materialCache.clear();
        pendingStarNodes.clear();
        pendingPolityNodes.clear();
    }

    /**
     * Gets or creates a cached PhongMaterial for the given color.
     * Reusing materials reduces memory allocation and improves rendering performance.
     *
     * @param color the color for the material
     * @return the cached or newly created material
     */
    private @NotNull PhongMaterial getCachedMaterial(@NotNull Color color) {
        return materialCache.computeIfAbsent(color, c -> {
            PhongMaterial material = new PhongMaterial();
            material.setDiffuseColor(c);
            material.setSpecularColor(c);
            return material;
        });
    }

    public void highlightStar(String starId) {
        // remove old star
        if (highLightStar != null) {
            stellarDisplayGroup.getChildren().remove(highLightStar);
        }
        // now create a new one
        Node starShape = tripsContext.getCurrentPlot().getStar(starId);
        StarDisplayRecord record = (StarDisplayRecord) starShape.getUserData();
        Color color = record.getStarColor();

        // make highLight star same as under-lying one, with the star record and context menu
        highLightStar = createHighlightStar(color);
        if (highLightStar != null) {
            highLightStar.setUserData(record);
            setContextMenu(record, highLightStar);

            // superimpose this highlight over top of star
            Point3D point3D = record.getCoordinates();
            highLightStar.setTranslateX(point3D.getX());
            highLightStar.setTranslateY(point3D.getY());
            highLightStar.setTranslateZ(point3D.getZ());
            highLightStar.setVisible(true);
            stellarDisplayGroup.getChildren().add(highLightStar);

            // now blink for highlight cycles
            log.info("starting blink");
            blinkStar(highLightStar, HIGHLIGHT_BLINK_CYCLES);

            log.info("mark point");
        }
    }

    private void blinkStar(Node starShape, int cycleCount) {
        // Set up completion callback to remove highlight star when animation finishes
        animationManager.setOnHighlightFinished(node -> {
            log.info("highlight star expiring and will be removed");
            stellarDisplayGroup.getChildren().remove(node);
        });

        // Start the highlight animation
        animationManager.startHighlightAnimation(starShape, cycleCount,
                SCALE_TRANSITION_DURATION_SECONDS, ANIMATION_SCALE_MULTIPLIER);
    }

    public void toggleStars(boolean starsOn) {
        stellarDisplayGroup.setVisible(starsOn);
        labelManager.setLabelsVisible(starsOn);
    }

    @TrackExecutionTime
    public void drawStars(@NotNull CurrentPlot currentPlot, boolean extensionsVisible) {
        drawStars(currentPlot, extensionsVisible, -1); // -1 means no distance filtering
    }

    /**
     * Draw stars with optional viewport distance filtering.
     * <p>
     * When maxDistance > 0, only stars within that distance from the center
     * are rendered. This uses the spatial index for O(log n + k) performance
     * where k is the number of visible stars.
     *
     * @param currentPlot        the current plot configuration
     * @param extensionsVisible  whether to show extension lines
     * @param maxDistance        maximum distance from center to render (-1 for all stars)
     */
    @TrackExecutionTime
    public void drawStars(@NotNull CurrentPlot currentPlot, boolean extensionsVisible, double maxDistance) {
        this.colorPalette = currentPlot.getColorPalette();
        this.starDisplayPreferences = currentPlot.getStarDisplayPreferences();

        // Configure LOD manager with center coordinates for distance calculations
        double[] centerCoords = currentPlot.getCenterCoordinates();
        if (centerCoords != null && centerCoords.length >= 3) {
            lodManager.setCenterCoordinates(centerCoords[0], centerCoords[1], centerCoords[2]);
            // Set reference Z for extension manager (grid plane level)
            extensionManager.setReferenceZ(centerCoords[2]);
        }
        lodManager.resetStatistics();
        extensionManager.setExtensionsVisible(extensionsVisible);

        // Get stars to render - use spatial filtering if maxDistance is specified
        List<StarDisplayRecord> starsToRender;
        if (maxDistance > 0 && centerCoords != null && centerCoords.length >= 3) {
            // Use spatial index for efficient filtering
            starsToRender = currentPlot.getStarsWithinRadius(maxDistance);
            log.debug("Rendering {} stars within {} ly (of {} total)",
                    starsToRender.size(), maxDistance, currentPlot.getStarDisplayRecordList().size());
        } else {
            // Render all stars
            starsToRender = currentPlot.getStarDisplayRecordList();
        }

        for (StarDisplayRecord starDisplayRecord : starsToRender) {
            plotStar(starDisplayRecord, currentPlot.getCenterStar(),
                    currentPlot.getColorPalette(), currentPlot.getStarDisplayPreferences(),
                    currentPlot.getCivilizationDisplayPreferences());
        }

        // Batch add all collected nodes to scene graph (more efficient than individual adds)
        flushPendingNodes();

        // Log LOD statistics for performance monitoring
        lodManager.logStatistics();
        log.debug("Material cache size: {} unique colors", materialCache.size());
    }

    /**
     * Flushes all pending star and polity nodes to the scene graph in batch.
     * <p>
     * Using {@code addAll()} is more efficient than individual {@code add()} calls
     * because it triggers only one scene graph restructure operation.
     */
    private void flushPendingNodes() {
        if (!pendingStarNodes.isEmpty()) {
            stellarDisplayGroup.getChildren().addAll(pendingStarNodes);
            log.debug("Batch added {} star nodes to scene graph", pendingStarNodes.size());
            pendingStarNodes.clear();
        }

        if (!pendingPolityNodes.isEmpty()) {
            politiesDisplayGroup.getChildren().addAll(pendingPolityNodes);
            politiesDisplayGroup.setVisible(true);  // Set once after all additions
            log.debug("Batch added {} polity nodes to scene graph", pendingPolityNodes.size());
            pendingPolityNodes.clear();
        }
    }

    public void clearPlot() {
        tripsContext.getCurrentPlot().clearPlot();
    }

    /**
     * toggle the labels
     *
     * @param labelSetting true is labels should be on
     */
    public void toggleLabels(boolean labelSetting) {
        // we can only do this if there are plot element on screen
        if (tripsContext.getCurrentPlot().isPlotActive()) {
            labelManager.setLabelsVisible(labelSetting);
        }
    }

    public void togglePolities(boolean polities) {
        this.politiesOn = polities;
        log.info("toggle polities: {}", polities);

        // we can only do this if there are plot element on screen
        if (tripsContext.getCurrentPlot().isPlotActive()) {
            politiesDisplayGroup.setVisible(polities);
        }
    }

    @TrackExecutionTime
    private void plotStar(@NotNull StarDisplayRecord record,
                          String centerStar,
                          @NotNull ColorPalette colorPalette,
                          StarDisplayPreferences starDisplayPreferences,
                          CivilizationDisplayPreferences politiesPreferences) {

        boolean isCenter = record.isCenter()
                && centerStar != null
                && !centerStar.isBlank()
                && centerStar.equalsIgnoreCase(record.getStarName());

        Node starNode = createStar(
                record,
                isCenter,
                colorPalette,
                starDisplayPreferences,
                politiesPreferences,
                record.isDisplayLabel(),
                politiesOn);

        // create the extension stem tot he star from the grid
        createExtension(record);

        tripsContext.getCurrentPlot().addStar(record.getRecordId(), starNode);

        // collect for batch addition (more efficient than individual adds)
        pendingStarNodes.add(starNode);
    }

    /**
     * create a star named with radius and color located at x,y,z
     *
     * @param record                 the star record
     * @param colorPalette           the color palette to use
     * @param starDisplayPreferences the star preferences
     * @param labelsOn               whether labels are on or off
     * @param politiesOn             whether the polities on or off
     * @return the star to plot
     */
    private @NotNull Node createStar(@NotNull StarDisplayRecord record,
                                     boolean isCenter,
                                     @NotNull ColorPalette colorPalette,
                                     StarDisplayPreferences starDisplayPreferences,
                                     CivilizationDisplayPreferences politiesPreferences,
                                     boolean labelsOn,
                                     boolean politiesOn) {

        Node star = drawStellarObject(
                record,
                colorPalette,
                isCenter,
                labelsOn,
                politiesOn,
                starDisplayPreferences,
                politiesPreferences);

        String polity = record.getPolity();
        if (polity.equals("NA")) {
            polity = "Non-Aligned";
        }
        Tooltip tooltip = new Tooltip(record.getStarName() + "::" + polity);
        Tooltip.install(star, tooltip);
        eventPublisher.publishEvent(new UpdateSidePanelListEvent(this, record));
        star.setId("regularStar");
        star.setUserData(record);

        return star;
    }

    /**
     * create a stellar object
     *
     * @param record            the star record
     * @param isCenter          flag that indicates that it is in the center
     * @param colorPalette      the color palette to use
     * @param labelsOn          are labels on?
     * @param politiesOn        are polities on?
     * @param polityPreferences the polity prefs
     * @return the created object
     */
    public @NotNull Node drawStellarObject(@NotNull StarDisplayRecord record,
                                           @NotNull ColorPalette colorPalette,
                                           boolean isCenter,
                                           boolean labelsOn,
                                           boolean politiesOn,
                                           StarDisplayPreferences starDisplayPreferences,
                                           @NotNull CivilizationDisplayPreferences polityPreferences) {

        // Use cached material to avoid creating duplicate PhongMaterial objects
        final PhongMaterial material = getCachedMaterial(record.getStarColor());

        Node starShape;
        if (isCenter) {
            starShape = createCentralStar();
        } else {
            // Use scale manager for star size calculation
            // record.getRadius() is pre-configured from StarDisplayPreferences based on spectral class
            double displayRadius = record.getRadius() * scaleManager.getStarSizeMultiplier();

            // Use LOD manager to determine detail level and create appropriate geometry
            StarLODManager.LODLevel lodLevel = lodManager.determineLODLevel(record, false);
            starShape = lodManager.createStarWithLOD(record, displayRadius, material, lodLevel);
        }
        Point3D point3D = record.getCoordinates();
        starShape.setTranslateX(point3D.getX());
        starShape.setTranslateY(point3D.getY());
        starShape.setTranslateZ(point3D.getZ());

        // labelsOn is per-star setting from record.isDisplayLabel()
        // labelManager handles global visibility toggle
        if (labelsOn) {
            Label label = labelManager.addLabel(starShape, record, colorPalette);
            tripsContext.getCurrentPlot().mapLabelToStar(record.getRecordId(), label);
        }

        if (politiesOn) {
            if (!record.getPolity().equals("NA") && !record.getPolity().isEmpty()) {

                MeshView polityObject = getPolityObject(record.getPolity(), polityPreferences);

                // attach polity object
                polityObject.setTranslateX(point3D.getX());
                polityObject.setTranslateY(point3D.getY());
                polityObject.setTranslateZ(point3D.getZ());

                // attach a context menu (lazy-loaded on right-click)
                setLazyContextMenu(record, polityObject);
                setLazyContextMenu(record, starShape);

                // collect for batch addition (more efficient than individual adds)
                pendingPolityNodes.add(polityObject);
            } else {
                // set context menu (lazy-loaded on right-click)
                setLazyContextMenu(record, starShape);
                log.debug("No polity to plot");
            }
            // Note: setVisible(true) moved to flushPendingNodes() - called once after all stars
        } else {
            // set context menu (lazy-loaded on right-click)
            setLazyContextMenu(record, starShape);
        }
        return starShape;
    }


    private MeshView getPolityObject(String polity, CivilizationDisplayPreferences polityPreferences) {
        MeshView polityObject = polityObjectFactory.createPolityObject(polity, polityPreferences);
        if (polityObject == null) {
            log.error("Failed to create polity object for: {}", polity);
        }
        return polityObject;
    }

    /**
     * Create a fresh central star mesh for display.
     * IMPORTANT: Each call creates a NEW instance because JavaFX Nodes can only
     * be in one scene graph at a time. Reusing the same Node would cause it to
     * be removed from its previous parent.
     *
     * @return a new central star Node
     */
    // Package-private for testing
    Node createCentralStar() {
        // Get the definition for scale/rotation settings
        MeshObjectDefinition meshObjectDefinition = specialObjects.get(CENTRAL_STAR);

        // Create a FRESH mesh instance - don't reuse cached objects!
        // JavaFX Nodes can only exist in one scene graph at a time.
        Group centralStar = meshViewShapeFactory.starCentral();

        if (centralStar == null) {
            log.error("Failed to load central star mesh from factory");
            return new Sphere(FALLBACK_SPHERE_RADIUS);
        }

        // Apply scale and rotation from the definition
        if (meshObjectDefinition != null) {
            centralStar.setScaleX(meshObjectDefinition.getXScale());
            centralStar.setScaleY(meshObjectDefinition.getYScale());
            centralStar.setScaleZ(meshObjectDefinition.getZScale());
            centralStar.setRotationAxis(meshObjectDefinition.getAxis());
            centralStar.setRotate(meshObjectDefinition.getRotateAngle());
        } else {
            // Default settings if definition is missing
            centralStar.setScaleX(DEFAULT_STAR_MESH_SCALE);
            centralStar.setScaleY(DEFAULT_STAR_MESH_SCALE);
            centralStar.setScaleZ(DEFAULT_STAR_MESH_SCALE);
            centralStar.setRotationAxis(Rotate.X_AXIS);
            centralStar.setRotate(DEFAULT_ROTATION_ANGLE);
        }

        return centralStar;
    }

    private void setContextMenu(@NotNull StarDisplayRecord record, Node star) {
        star.setUserData(record);
        String polity = record.getPolity();
        if (polity.equals("NA")) {
            polity = "Non-aligned";
        }
        ContextMenu starContextMenu = createPopup(record.getStarName() + " (" + polity + ")", star);
        star.addEventHandler(
                MouseEvent.MOUSE_CLICKED,
                e -> starClickEventHandler(star, starContextMenu, e));
        star.setOnMousePressed(event -> {
            Node node = (Node) event.getSource();
            StarDisplayRecord starDescriptor = (StarDisplayRecord) node.getUserData();
            log.info("mouse click detected! " + starDescriptor);
        });
    }

    /**
     * Sets up lazy-loaded context menu for a star node.
     * <p>
     * Unlike {@link #setContextMenu}, this method does NOT create the ContextMenu upfront.
     * Instead, the menu is created on-demand when the user clicks the node.
     * This significantly reduces memory usage and initialization time for large star plots.
     *
     * @param record the star record
     * @param star   the star node
     */
    private void setLazyContextMenu(@NotNull StarDisplayRecord record, Node star) {
        star.setUserData(record);

        // Create context menu lazily on click
        star.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            // Handle primary button (left-click)
            if (e.getButton() == MouseButton.PRIMARY) {
                log.info("Primary button pressed");
                if (routeManager.isManualRoutingActive()) {
                    log.info("Manual Routing is active");
                    if (routeManager.getRoutingType().equals(RoutingType.MANUAL)) {
                        if (manualRoutingDialog != null) {
                            manualRoutingDialog.addStar(record);
                        }
                    }
                    if (routeManager.getRoutingType().equals(RoutingType.AUTOMATIC)) {
                        if (automatedRoutingDialog != null) {
                            automatedRoutingDialog.setToStar(record.getStarName());
                        }
                    }
                } else {
                    log.info("Manual routing is not active - showing context menu");
                    ContextMenu starContextMenu = createLazyPopup(record, star);
                    starContextMenu.show(star, e.getScreenX(), e.getScreenY());
                }
            }
            // Handle middle button
            else if (e.getButton() == MouseButton.MIDDLE) {
                log.info("Middle button pressed");
                ContextMenu starContextMenu = createLazyPopup(record, star);
                starContextMenu.show(star, e.getScreenX(), e.getScreenY());
            }
            // Handle secondary button (right-click) for selection toggle
            else if (e.getButton() == MouseButton.SECONDARY) {
                log.info("Secondary button pressed");
                if (selectionModel.containsKey(star)) {
                    // remove star and selection rectangle
                    StarSelectionModel starSelectionModel = selectionModel.get(star);
                    Node selectionRectangle = starSelectionModel.getSelectionRectangle();
                    if (selectionRectangle != null && selectionRectangle.getParent() instanceof Group group) {
                        group.getChildren().remove(selectionRectangle);
                    }
                    selectionModel.remove(star);
                } else {
                    // add star selection
                    StarSelectionModel starSelectionModel = new StarSelectionModel();
                    starSelectionModel.setStarNode(star);
                    selectionModel.put(star, starSelectionModel);
                }
            }
        });

        star.setOnMousePressed(event -> {
            Node node = (Node) event.getSource();
            StarDisplayRecord starDescriptor = (StarDisplayRecord) node.getUserData();
            log.debug("mouse click detected! " + starDescriptor);
        });
    }

    /**
     * Creates a context menu popup lazily for a star.
     *
     * @param record the star record
     * @param star   the star node
     * @return the created context menu
     */
    private @NotNull ContextMenu createLazyPopup(@NotNull StarDisplayRecord record, @NotNull Node star) {
        String polity = record.getPolity();
        if (polity.equals("NA")) {
            polity = "Non-aligned";
        }
        return createPopup(record.getStarName() + " (" + polity + ")", star);
    }

    /**
     * create a label for a shape
     *
     * @param record       the star record
     * @param colorPalette the color palette to use
     * @return the created object
     */
    /**
     * Create a label for a star.
     * Delegates to {@link StarLabelManager#createLabel}.
     *
     * @param record       the star record
     * @param colorPalette the color palette
     * @return the created label
     */
    public @NotNull Label createLabel(@NotNull StarDisplayRecord record,
                                      @NotNull ColorPalette colorPalette) {
        return labelManager.createLabel(record, colorPalette);
    }

    /**
     * create an extension for an added star
     *
     * @param record the star
     */
    private void createExtension(@NotNull StarDisplayRecord record) {
        extensionManager.createExtension(record, colorPalette);
    }

    /**
     * setup the event click handler for a star
     *
     * @param star            the star
     * @param starContextMenu the menu
     * @param e               the exception caught
     */
    private void starClickEventHandler(Node star, @NotNull ContextMenu starContextMenu, @NotNull MouseEvent e) {
        if (e.getButton() == MouseButton.PRIMARY) {
            log.info("Primary button pressed");
            if (routeManager.isManualRoutingActive()) {
                log.info("Manual Routing is active");
                StarDisplayRecord record = (StarDisplayRecord) star.getUserData();
                if (routeManager.getRoutingType().equals(RoutingType.MANUAL)) {
                    if (manualRoutingDialog != null) {
                        manualRoutingDialog.addStar(record);
                    }
                }
                if (routeManager.getRoutingType().equals(RoutingType.AUTOMATIC)) {
                    if (automatedRoutingDialog != null) {
                        automatedRoutingDialog.setToStar(record.getStarName());
                    }
                }
            } else {
                log.info("Manual routing is not active");
                starContextMenu.show(star, e.getScreenX(), e.getScreenY());
            }
        }
        if (e.getButton() == MouseButton.MIDDLE) {
            log.info("Middle button pressed");
            starContextMenu.show(star, e.getScreenX(), e.getScreenY());
        }

        if (e.getButton() == MouseButton.SECONDARY) {
            log.info("Secondary button pressed");
            if (selectionModel.containsKey(star)) {
                // remove star and selection rectangle
                StarSelectionModel starSelectionModel = selectionModel.get(star);
                Node selectionRectangle = starSelectionModel.getSelectionRectangle();
                if (selectionRectangle != null && selectionRectangle.getParent() instanceof Group group) {
                    group.getChildren().remove(selectionRectangle);
                }

                // remove the selection model
                selectionModel.remove(star);

            } else {
                // add star selection
                StarSelectionModel starSelectionModel = new StarSelectionModel();
                starSelectionModel.setStarNode(star);
                selectionModel.put(star, starSelectionModel);

            }
        }
    }

    /**
     * create a context menu for clicking on the stars
     *
     * @param name the name of the star
     * @param star the star
     * @return the menu
     */
    private @NotNull ContextMenu createPopup(String name, @NotNull Node star) {
        StarDisplayRecord record = (StarDisplayRecord) star.getUserData();

        return new StarContextMenuBuilder(star, record)
                .withTitle(name)
                .withHighlightAction(r ->
                        eventPublisher.publishEvent(new HighlightStarEvent(this, r.getRecordId())))
                .withPropertiesAction(r -> {
                    StarObject starObject = starService.getStar(r.getRecordId());
                    displayProperties(starObject);
                })
                .withRecenterAction(r -> {
                    if (r != null) {
                        eventPublisher.publishEvent(new RecenterStarEvent(this, r));
                    } else {
                        showErrorAlert("Recenter on star", "The star you selected was null!");
                    }
                })
                .withEditAction(r -> {
                    StarDisplayRecord editRecord = editProperties(r);
                    if (editRecord != null) {
                        star.setUserData(editRecord);
                    }
                })
                .withDeleteAction(r -> removeNode(r))
                .withRoutingHeader()
                .withAutomatedRoutingAction(r -> generateAutomatedRoute(r))
                .withManualRoutingAction(r -> generateManualRoute(r))
                .withDistanceReportAction(r ->
                        eventPublisher.publishEvent(new DistanceReportEvent(this, r)))
                .withEnterSystemAction(r -> jumpToSystem(r))
                .withGenerateSolarSystemAction(r -> generateSolarSystem(r))
                .build();
    }


    private void generateAutomatedRoute(StarDisplayRecord starDescriptor) {
        log.info("generate automated route");
        automatedRoutingDialog = new ContextAutomatedRoutingDialog(
                this, routeManager, routeFindingService,
                getCurrentDataSet(), starDescriptor, getCurrentStarsInView());

        automatedRoutingDialog.initModality(Modality.NONE);
        automatedRoutingDialog.show();
        // set the state for the routing so that clicks on stars don't invoke the context menu
//        routeManager.setManualRoutingActive(true);
        routeManager.setRoutingType(RoutingType.AUTOMATIC);
    }

    private void generateManualRoute(StarDisplayRecord starDescriptor) {
        log.info("generate manual route");
        manualRoutingDialog = new ContextManualRoutingDialog(
                routeManager,
                getCurrentDataSet(),
                starDescriptor
        );
        manualRoutingDialog.initModality(Modality.NONE);
        manualRoutingDialog.show();
        // set the state for the routing so that clicks on stars don't invoke the context menu
        routeManager.setManualRoutingActive(true);
        routeManager.setRoutingType(RoutingType.MANUAL);

    }

    private DataSetDescriptor getCurrentDataSet() {
        return tripsContext.getDataSetDescriptor();
    }





    /**
     * remove a star node form the db
     *
     * @param starDisplayRecord the star to remove
     */
    private void removeNode(@NotNull StarDisplayRecord starDisplayRecord) {
        log.info("Removing object for:" + starDisplayRecord.getStarName());
        starService.removeStar(starDisplayRecord.getRecordId());
    }




    ///////////////////////// Simulate  /////////

    /**
     * edit a star in the database
     *
     * @param starDisplayRecord the properties to edit
     */
    private @Nullable StarDisplayRecord editProperties(@NotNull StarDisplayRecord starDisplayRecord) {
        StarObject starObject = starService.getStar(starDisplayRecord.getRecordId());
        StarEditDialog starEditDialog = new StarEditDialog(starObject);
        Optional<StarEditStatus> optionalStarDisplayRecord = starEditDialog.showAndWait();
        if (optionalStarDisplayRecord.isPresent()) {
            StarEditStatus status = optionalStarDisplayRecord.get();
            if (status.isChanged()) {
                StarObject record = status.getRecord();
                StarDisplayRecord record1 = StarDisplayRecord.fromStarObject(record, starDisplayPreferences);
                if (record1 != null) {
                    record1.setCoordinates(starDisplayRecord.getCoordinates());
                    log.info("Changed value: {}", record);
                    starService.updateStar(record);
                } else {
                    log.error("Conversion of {} to star display record, returned a null-->bug!!", record);
                }
                return record1;
            } else {
                log.warn("no return");
                return starDisplayRecord;
            }
        }
        log.info("Editing properties in side panes for:" + starDisplayRecord.getStarName());
        return starDisplayRecord;
    }


    /**
     * display properties for this star
     *
     * @param starObject the properties to display
     */
    private void displayProperties(@NotNull StarObject starObject) {
        log.info("Showing properties in side panes for:" + starObject.getDisplayName());
        eventPublisher.publishEvent(new DisplayStarEvent(this, starObject));
    }

    /**
     * jump to the solar system selected
     *
     * @param starDisplayRecord the properties of the star selected
     */
    private void jumpToSystem(StarDisplayRecord starDisplayRecord) {
        eventPublisher.publishEvent(new ContextSelectorEvent(
                this,
                ContextSelectionType.SOLARSYSTEM,
                starDisplayRecord,
                (java.util.Map<String, String>) null));
    }

    public void updateLabels(@NotNull InterstellarSpacePane interstellarSpacePane) {
        labelManager.setControlPaneOffset(controlPaneOffset);
        labelManager.updateLabels(interstellarSpacePane.getBoundsInParent());
    }

    /**
     * generate random stars (for testing/debug purposes)
     *
     * @param numberStars number of stars
     */
    public void generateRandomStars(int numberStars) {
        if (colorPalette == null) {
            log.warn("color palette not initialized; cannot generate random stars");
            return;
        }
        for (int i = 0; i < numberStars; i++) {
            double radius = random.nextDouble() * MAX_RANDOM_STAR_RADIUS;
            Color color = randomColor();
            double x = random.nextDouble() * X_MAX * RANDOM_POSITION_FRACTION * (random.nextBoolean() ? 1 : -1);
            double y = random.nextDouble() * Y_MAX * RANDOM_POSITION_FRACTION * (random.nextBoolean() ? 1 : -1);
            double z = random.nextDouble() * Z_MAX * RANDOM_POSITION_FRACTION * (random.nextBoolean() ? 1 : -1);

            String labelText = "Star " + i;
            createSphereAndLabel(radius, x, y, z, color, colorPalette.getLabelFont().toFont(), labelText);
            createExtension(x, y, z, Color.VIOLET);
        }

        log.info("shapes:{}", labelManager.getLabelCount());
    }

    private @NotNull Color randomColor() {
        int r = random.nextInt(256);
        int g = random.nextInt(256);
        int b = random.nextInt(256);
        return Color.rgb(r, g, b);
    }

    private void createSphereAndLabel(double radius, double x, double y, double z, Color color, Font font, String labelText) {
        if (colorPalette == null) {
            log.warn("color palette not initialized; cannot create labeled sphere");
            return;
        }
        Sphere sphere = new Sphere(radius);
        sphere.setTranslateX(x);
        sphere.setTranslateY(y);
        sphere.setTranslateZ(z);
        sphere.setMaterial(new PhongMaterial(color));
        //add our nodes to the group that will later be added to the 3D scene
        world.getChildren().add(sphere);

        Label label = new Label(labelText);
        label.setTextFill(color);
        label.setFont(font);
        ObjectDescriptor descriptor = ObjectDescriptor
                .builder()
                .name(labelText)
                .color(color)
                .x(x)
                .y(y)
                .z(z)
                .build();
        sphere.setUserData(descriptor);
        Tooltip tooltip = new Tooltip(descriptor.toString());
        Tooltip.install(sphere, tooltip);

        // Register the label with the label manager
        labelManager.registerLabel(sphere, label);

    }

    private void createExtension(double x, double y, double z, Color extensionColor) {
        extensionManager.createExtension(x, y, z, extensionColor, colorPalette.getLabelFont().toFont());
    }


    public void setControlPaneOffset(double controlPaneOffset) {
        this.controlPaneOffset = controlPaneOffset;
    }

    /**
     * Set the zoom level for both scale and LOD managers.
     * Call this when zoom level changes to ensure LOD thresholds are adjusted.
     *
     * @param zoomLevel the new zoom level
     */
    public void setZoomLevel(double zoomLevel) {
        scaleManager.setZoomLevel(zoomLevel);
        lodManager.setZoomLevel(zoomLevel);
    }

    /**
     * Enable or disable LOD system.
     * When disabled, all stars use MEDIUM detail level.
     *
     * @param enabled true to enable LOD, false to disable
     */
    public void setLodEnabled(boolean enabled) {
        lodManager.setLodEnabled(enabled);
    }


    ///////////



    /**
     * create a highlight star
     *
     * @param color the color to display it as (used to match the star)
     * @return the star to display
     */
    // Package-private for testing
    Node createHighlightStar(Color color) {
        // load the moravian star
        // we have to do this each time because it has to unique
        Group highLightStar = meshViewShapeFactory.starMoravian();
        if (highLightStar != null) {

            // extract the various meshviews and set the color to match
            // we need to do this because the moravian object is a group of mesh objects and
            // we need set the material color on each one.
            for (Node node : highLightStar.getChildren()) {
                MeshView meshView = (MeshView) node;
                PhongMaterial material = (PhongMaterial) meshView.getMaterial();
                material.setSpecularColor(color);
                material.setDiffuseColor(color);
            }

            // now scale it and set it to show properly
            highLightStar.setScaleX(DEFAULT_STAR_MESH_SCALE);
            highLightStar.setScaleY(DEFAULT_STAR_MESH_SCALE);
            highLightStar.setScaleZ(DEFAULT_STAR_MESH_SCALE);
            highLightStar.setRotationAxis(Rotate.X_AXIS);
            highLightStar.setRotate(DEFAULT_ROTATION_ANGLE);
            return highLightStar;
        } else {
            log.error("Unable to load the moravian star object");
            return null;
        }

    }

    /////////////////////////////////////////////////////////////////////




    public void clearRoutingFlag() {
        routeManager.setManualRoutingActive(false);
    }


    private void generateSolarSystem(StarDisplayRecord starDescriptor) {
        StarObject starObject = starService.getStar(starDescriptor.getRecordId());

        // Check for invalid stellar parameters and warn the user
        String parameterIssues = validateStellarParameters(starObject);
        if (parameterIssues != null) {
            Alert warningAlert = new Alert(Alert.AlertType.WARNING);
            warningAlert.setTitle("Missing Stellar Parameters");
            warningAlert.setHeaderText("Some stellar parameters are missing or invalid");
            warningAlert.setContentText(
                    parameterIssues +
                    "\n\nThe generator will use Sun-like default values for missing parameters. " +
                    "This may produce unrealistic results.\n\n" +
                    "Do you want to proceed anyway?"
            );
            warningAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

            Optional<ButtonType> warningResult = warningAlert.showAndWait();
            if (warningResult.isEmpty() || warningResult.get() == ButtonType.NO) {
                log.info("User cancelled solar system generation due to missing stellar parameters for '{}'",
                        starObject.getDisplayName());
                return;
            }
            log.warn("User proceeding with solar system generation despite missing parameters for '{}': {}",
                    starObject.getDisplayName(), parameterIssues.replace("\n", "; "));
        }

        SolarSystemGenerationDialog dialog = new SolarSystemGenerationDialog(starObject);
        Optional<SolarSystemGenOptions> solarSystemGenOptional = dialog.showAndWait();
        if (solarSystemGenOptional.isPresent()) {
            SolarSystemGenOptions solarSystemGenOptions = solarSystemGenOptional.get();
            SolarSystemReport report = new SolarSystemReport(starObject, solarSystemGenOptions);
            report.generateReport();

            PlanetDialog planetDialog = new PlanetDialog(report);
            Optional<SolarSystemSaveResult> resultOptional = planetDialog.showAndWait();

            // Handle save request
            if (resultOptional.isPresent()) {
                SolarSystemSaveResult saveResult = resultOptional.get();
                if (saveResult.isSaveRequested()) {
                    int savedCount = solarSystemService.saveGeneratedPlanets(
                            saveResult.getSourceStar(),
                            saveResult.getPlanets()
                    );
                    log.info("Saved {} generated planets to database for star '{}'",
                            savedCount, starObject.getDisplayName());

                    // Show confirmation to user
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Solar System Saved");
                    alert.setHeaderText("Generated planets saved successfully");
                    alert.setContentText(String.format(
                            "Saved %d planets for %s.\n\n" +
                            "You can now use 'Enter System' to view the generated solar system.",
                            savedCount, starObject.getDisplayName()));
                    alert.showAndWait();
                }
            }
        }
    }

    /**
     * Validates stellar parameters required for solar system generation.
     *
     * @param starObject the star to validate
     * @return null if all parameters are valid, otherwise a string describing the issues
     */
    private String validateStellarParameters(StarObject starObject) {
        StringBuilder issues = new StringBuilder();

        if (starObject.getMass() <= 0) {
            issues.append("• Mass is missing or zero\n");
        }

        if (starObject.getRadius() <= 0) {
            issues.append("• Radius is missing or zero\n");
        }

        if (starObject.getTemperature() <= 0) {
            issues.append("• Temperature is missing or zero\n");
        }

        // Luminosity is a String - check if it's empty or not a valid positive number
        String luminosity = starObject.getLuminosity();
        if (luminosity == null || luminosity.trim().isEmpty()) {
            issues.append("• Luminosity is missing\n");
        } else {
            try {
                double lumValue = Double.parseDouble(luminosity.trim());
                if (lumValue <= 0) {
                    issues.append("• Luminosity value is zero or negative\n");
                }
            } catch (NumberFormatException e) {
                // Luminosity might be a class like "V" or "IV" - that's acceptable
                // Only flag it if it's neither a valid number nor a known luminosity class
                String trimmed = luminosity.trim().toUpperCase();
                if (!trimmed.matches("^(I{1,3}|IV|V|VI|VII|0|Ia|Ib|II|III)?[ab]?$")) {
                    // Not a standard luminosity class, might be invalid
                    // But we'll be lenient here - only flag completely empty strings
                }
            }
        }

        if (issues.length() > 0) {
            return "The following stellar parameters are missing or invalid:\n\n" + issues;
        }
        return null;
    }


    public void setManualRouting(ContextManualRoutingDialog manualRoutingDialog) {
        this.manualRoutingDialog = manualRoutingDialog;
    }


    @EventListener
    public void onHighlightStarEvent(HighlightStarEvent event) {
        Platform.runLater(() -> {
            log.info("STAR PLOT MANAGER ::: Received highlight star event, star id is:{}", event.getRecordId());
            highlightStar(event.getRecordId());
        });
    }
}
