package com.teamgannon.trips.starplotting;

import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.config.application.model.CurrentPlot;
import com.teamgannon.trips.config.application.model.StarDisplayPreferences;
import com.teamgannon.trips.events.*;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import com.teamgannon.trips.jpa.model.CivilizationDisplayPreferences;
import com.teamgannon.trips.measure.TrackExecutionTime;
import com.teamgannon.trips.objects.MeshViewShapeFactory;
import com.teamgannon.trips.routing.RouteManager;
import com.teamgannon.trips.routing.RouteFindingService;
import com.teamgannon.trips.routing.dialogs.ContextManualRoutingDialog;
import com.teamgannon.trips.service.SolarSystemService;
import com.teamgannon.trips.service.StarService;
import javafx.application.Platform;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class StarPlotManager {

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
     * Manages special star mesh objects (central star, moravian star, etc.).
     */
    @Getter
    private final SpecialStarMeshManager meshManager;

    /**
     * Handles context menu actions for stars.
     */
    private final StarContextMenuHandler contextMenuHandler;

    /**
     * Handles mouse click events on stars.
     */
    private StarClickHandler clickHandler;

    /**
     * Generates random stars for debugging.
     */
    private DebugStarGenerator debugStarGenerator;

    /**
     * to hold all the polities
     */
    private final Group politiesDisplayGroup = new Group();

    private Group world;

    private SubScene subScene;

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
     * toggle state of polities
     */
    private boolean politiesOn = true;

    /**
     * reference to the Route Manager
     */
    private final RouteManager routeManager;

    private double controlPaneOffset;

    /**
     * used as a control for highlighting stars
     */
    private Node highLightStar;

    private StarDisplayPreferences starDisplayPreferences;

    /**
     * used to create 3d mesh objects
     */
    private final MeshViewShapeFactory meshViewShapeFactory = new MeshViewShapeFactory();

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
                           StarService starService,
                           SolarSystemService solarSystemService,
                           RouteFindingService routeFindingService,
                           StarContextMenuHandler contextMenuHandler,
                           ApplicationEventPublisher eventPublisher) {

        this.tripsContext = tripsContext;
        this.colorPalette = tripsContext.getAppViewPreferences().getColorPalette();
        this.routeManager = routeManager;
        this.contextMenuHandler = contextMenuHandler;
        this.eventPublisher = eventPublisher;

        // Initialize polity object factory
        this.polityObjectFactory = new PolityObjectFactory(meshViewShapeFactory);

        // Initialize special star mesh manager
        this.meshManager = new SpecialStarMeshManager(meshViewShapeFactory);

        // Configure the context menu handler
        contextMenuHandler.setStarsInViewSupplier(this::getCurrentStarsInView);

        // Initialize click handler
        this.clickHandler = new StarClickHandler(contextMenuHandler, this::createContextMenuForStar);
    }

    /**
     * Creates a context menu for a star (used by click handler).
     */
    private ContextMenu createContextMenuForStar(StarDisplayRecord record, Node star) {
        return contextMenuHandler.createContextMenu(star, record, this);
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

        // Pre-warm the star node pool for faster initial rendering
        // This creates spheres ahead of time to avoid allocation during first render
        lodManager.prewarmPool(100);

        // Initialize debug star generator
        this.debugStarGenerator = new DebugStarGenerator(world, labelManager, extensionManager);
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

        // Return star spheres to the pool before clearing (if pooling is enabled)
        // This allows sphere objects to be reused in the next render cycle
        lodManager.releaseNodes(stellarDisplayGroup.getChildren());

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

        log.debug("Stars cleared. Pool statistics: {}", lodManager.getNodePool().getStatistics());
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
        this.contextMenuHandler.setStarDisplayPreferences(starDisplayPreferences);

        // Configure LOD manager with center coordinates for distance calculations
        double[] centerCoords = currentPlot.getCenterCoordinates();
        if (centerCoords != null && centerCoords.length >= 3) {
            lodManager.setCenterCoordinates(centerCoords[0], centerCoords[1], centerCoords[2]);
            // Set reference Z for extension manager (grid plane level)
            extensionManager.setReferenceZ(centerCoords[2]);
        }
        lodManager.resetStatistics();
        extensionManager.setExtensionsVisible(extensionsVisible);

        // Get stars to render - sorted by distance for optimal LOD processing
        // Pre-sorting enables cached distance calculations in LOD determination
        List<StarDisplayRecord> starsToRender;
        if (maxDistance > 0 && centerCoords != null && centerCoords.length >= 3) {
            // Use spatial index for filtering, sorted by distance
            starsToRender = currentPlot.getStarsWithinRadiusSorted(maxDistance);
            log.debug("Rendering {} stars within {} ly (of {} total), sorted by distance",
                    starsToRender.size(), maxDistance, currentPlot.getStarDisplayRecordList().size());
        } else {
            // Render all stars, sorted by distance for LOD optimization
            starsToRender = currentPlot.getStarsSortedByDistance();
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

        // Install tooltip lazily on first hover to reduce memory usage
        installLazyTooltip(star, record);

        eventPublisher.publishEvent(new UpdateSidePanelListEvent(this, record));
        star.setId("regularStar");
        star.setUserData(record);

        return star;
    }

    /**
     * Installs a tooltip lazily - only created when user first hovers over the node.
     * This reduces memory usage for large star plots where most tooltips are never seen.
     *
     * @param node   the node to attach the tooltip to
     * @param record the star record for tooltip content
     */
    private void installLazyTooltip(@NotNull Node node, @NotNull StarDisplayRecord record) {
        // Use a single-fire mouse enter handler to install tooltip on first hover
        node.setOnMouseEntered(event -> {
            // Check if tooltip already installed (prevents duplicate creation)
            if (node.getProperties().get("tooltipInstalled") == null) {
                String polity = record.getPolity();
                if (polity.equals("NA")) {
                    polity = "Non-Aligned";
                }
                Tooltip tooltip = new Tooltip(record.getStarName() + "::" + polity);
                Tooltip.install(node, tooltip);
                node.getProperties().put("tooltipInstalled", Boolean.TRUE);
                log.trace("Installed tooltip for star: {}", record.getStarName());
            }
        });
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
     * Delegates to {@link SpecialStarMeshManager#createCentralStar()}.
     *
     * @return a new central star Node
     */
    // Package-private for testing
    Node createCentralStar() {
        return meshManager.createCentralStar();
    }

    /**
     * Sets up a context menu on a star node (eager creation).
     * Delegates to {@link StarClickHandler#setupContextMenu}.
     *
     * @param record the star record
     * @param star   the star node
     */
    private void setContextMenu(@NotNull StarDisplayRecord record, Node star) {
        clickHandler.setupContextMenu(record, star);
    }

    /**
     * Sets up lazy-loaded context menu for a star node.
     * Delegates to {@link StarClickHandler#setupLazyContextMenu}.
     *
     * @param record the star record
     * @param star   the star node
     */
    private void setLazyContextMenu(@NotNull StarDisplayRecord record, Node star) {
        clickHandler.setupLazyContextMenu(record, star);
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







    public void updateLabels(@NotNull InterstellarSpacePane interstellarSpacePane) {
        labelManager.setControlPaneOffset(controlPaneOffset);
        labelManager.updateLabels(interstellarSpacePane.getBoundsInParent());
    }

    /**
     * Generate random stars for testing/debug purposes.
     * Delegates to {@link DebugStarGenerator}.
     *
     * @param numberStars number of stars to generate
     */
    public void generateRandomStars(int numberStars) {
        if (debugStarGenerator != null) {
            debugStarGenerator.generateRandomStars(numberStars, colorPalette);
        } else {
            log.warn("Debug star generator not initialized; call setGraphics first");
        }
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
     * Create a highlight star for blinking animation.
     * Delegates to {@link SpecialStarMeshManager#createHighlightStar(Color)}.
     *
     * @param color the color to display it as (used to match the star)
     * @return the star to display
     */
    // Package-private for testing
    Node createHighlightStar(Color color) {
        return meshManager.createHighlightStar(color);
    }

    /////////////////////////////////////////////////////////////////////




    public void clearRoutingFlag() {
        contextMenuHandler.clearRoutingFlag();
    }

    public void setManualRouting(ContextManualRoutingDialog manualRoutingDialog) {
        contextMenuHandler.setManualRoutingDialog(manualRoutingDialog);
    }


    @EventListener
    public void onHighlightStarEvent(HighlightStarEvent event) {
        Platform.runLater(() -> {
            log.info("STAR PLOT MANAGER ::: Received highlight star event, star id is:{}", event.getRecordId());
            highlightStar(event.getRecordId());
        });
    }
}
