package com.teamgannon.trips.starplotting;

import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.config.application.model.CurrentPlot;
import com.teamgannon.trips.config.application.model.StarDisplayPreferences;
import com.teamgannon.trips.events.HighlightStarEvent;
import com.teamgannon.trips.events.UpdateSidePanelListEvent;
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
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Manages the plotting and display of stars in the interstellar space view.
 * Coordinates rendering, labels, extensions, and user interactions.
 */
@Slf4j
@Component
public class StarPlotManager {

    // =========================================================================
    // Instance Fields
    // =========================================================================

    /**
     * Manages coordinate scaling between light-years and screen units.
     */
    @Getter
    private final InterstellarScaleManager scaleManager = new InterstellarScaleManager();

    /**
     * The stellar group for display.
     */
    private final Group stellarDisplayGroup = new Group();

    /**
     * Manages star labels (creation, positioning, visibility).
     */
    @Getter
    private final StarLabelManager labelManager = new StarLabelManager();

    /**
     * Manages Level-of-Detail (LOD) for star rendering.
     */
    @Getter
    private final StarLODManager lodManager = new StarLODManager();

    /**
     * Manages extension lines from the grid to stars.
     */
    @Getter
    private final StarExtensionManager extensionManager = new StarExtensionManager();

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
     * Handles star rendering with material caching and batching.
     */
    private final StarRenderer starRenderer;

    /**
     * Handles star highlighting with animation.
     */
    private final StarHighlighter starHighlighter;

    /**
     * Handles context menu actions for stars.
     */
    private final StarContextMenuHandler contextMenuHandler;

    /**
     * Handles mouse click events on stars.
     */
    private final StarClickHandler clickHandler;

    /**
     * Generates random stars for debugging.
     */
    private DebugStarGenerator debugStarGenerator;

    /**
     * Factory for creating polity indicator objects.
     */
    private final PolityObjectFactory polityObjectFactory;

    /**
     * To hold all the polities.
     */
    private final Group politiesDisplayGroup = new Group();

    private Group world;
    private SubScene subScene;

    private final ApplicationEventPublisher eventPublisher;
    private final TripsContext tripsContext;

    private ColorPalette colorPalette;
    private StarDisplayPreferences starDisplayPreferences;

    /**
     * Toggle state of polities.
     */
    private boolean politiesOn = true;

    private final RouteManager routeManager;
    private double controlPaneOffset;

    /**
     * Used to create 3D mesh objects.
     */
    private final MeshViewShapeFactory meshViewShapeFactory = new MeshViewShapeFactory();

    // =========================================================================
    // Constructor
    // =========================================================================

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

        // Initialize factories and managers
        this.polityObjectFactory = new PolityObjectFactory(meshViewShapeFactory);
        this.meshManager = new SpecialStarMeshManager(meshViewShapeFactory);

        // Configure the context menu handler
        contextMenuHandler.setStarsInViewSupplier(this::getCurrentStarsInView);

        // Initialize click handler
        this.clickHandler = new StarClickHandler(contextMenuHandler, this::createContextMenuForStar);

        // Initialize star renderer
        this.starRenderer = new StarRenderer(
                lodManager, labelManager, scaleManager, meshManager,
                polityObjectFactory, clickHandler
        );

        // Initialize star highlighter
        this.starHighlighter = new StarHighlighter(
                tripsContext, meshManager, animationManager, clickHandler, labelManager
        );
        this.starHighlighter.setContextMenuFactory((record, star) ->
                clickHandler.setupContextMenu(record, star)
        );
    }

    /**
     * Creates a context menu for a star (used by click handler).
     */
    private ContextMenu createContextMenuForStar(StarDisplayRecord record, Node star) {
        return contextMenuHandler.createContextMenu(star, record, this);
    }

    // =========================================================================
    // Initialization
    // =========================================================================

    public void setGraphics(Group sceneRoot, Group world, SubScene subScene) {
        this.world = world;
        this.subScene = subScene;

        world.getChildren().add(stellarDisplayGroup);

        // Initialize label manager with scene references
        labelManager.initialize(sceneRoot, subScene);

        // Initialize extension manager
        extensionManager.initialize(world);

        world.getChildren().add(politiesDisplayGroup);

        // Pre-warm the star node pool for faster initial rendering
        lodManager.prewarmPool(100);

        // Initialize debug star generator
        this.debugStarGenerator = new DebugStarGenerator(world, labelManager, extensionManager);

        // Initialize star highlighter
        starHighlighter.initialize(stellarDisplayGroup);

        // Configure star renderer label registration
        starRenderer.setLabelRegistrar((recordId, label) ->
                tripsContext.getCurrentPlot().mapLabelToStar(recordId, label)
        );
    }

    // =========================================================================
    // Star Query Methods
    // =========================================================================

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

    // =========================================================================
    // Star Drawing
    // =========================================================================

    @TrackExecutionTime
    public void drawStars(@NotNull CurrentPlot currentPlot, boolean extensionsVisible) {
        drawStars(currentPlot, extensionsVisible, -1);
    }

    @TrackExecutionTime
    public void drawStars(@NotNull CurrentPlot currentPlot, boolean extensionsVisible, double maxDistance) {
        this.colorPalette = currentPlot.getColorPalette();
        this.starDisplayPreferences = currentPlot.getStarDisplayPreferences();
        this.contextMenuHandler.setStarDisplayPreferences(starDisplayPreferences);

        // Configure LOD manager with center coordinates
        double[] centerCoords = currentPlot.getCenterCoordinates();
        if (centerCoords != null && centerCoords.length >= 3) {
            lodManager.setCenterCoordinates(centerCoords[0], centerCoords[1], centerCoords[2]);
            extensionManager.setReferenceZ(centerCoords[2]);
        }
        lodManager.resetStatistics();
        extensionManager.setExtensionsVisible(extensionsVisible);

        // Get stars to render
        List<StarDisplayRecord> starsToRender;
        if (maxDistance > 0 && centerCoords != null && centerCoords.length >= 3) {
            starsToRender = currentPlot.getStarsWithinRadiusSorted(maxDistance);
            log.debug("Rendering {} stars within {} ly (of {} total)",
                    starsToRender.size(), maxDistance, currentPlot.getStarDisplayRecordList().size());
        } else {
            starsToRender = currentPlot.getStarsSortedByDistance();
        }

        // Plot each star
        for (StarDisplayRecord record : starsToRender) {
            plotStar(record, currentPlot.getCenterStar(), currentPlot);
        }

        // Batch add all collected nodes to scene graph
        flushPendingNodes();

        // Log statistics
        lodManager.logStatistics();
        log.debug("Material cache size: {} unique colors", starRenderer.getMaterialCacheSize());
    }

    @TrackExecutionTime
    private void plotStar(@NotNull StarDisplayRecord record,
                          String centerStar,
                          @NotNull CurrentPlot currentPlot) {

        boolean isCenter = record.isCenter()
                && centerStar != null
                && !centerStar.isBlank()
                && centerStar.equalsIgnoreCase(record.getStarName());

        Node starNode = starRenderer.createStar(
                record,
                isCenter,
                currentPlot.getColorPalette(),
                currentPlot.getStarDisplayPreferences(),
                currentPlot.getCivilizationDisplayPreferences(),
                record.isDisplayLabel(),
                politiesOn
        );

        // Create extension stem to the star from the grid
        extensionManager.createExtension(record, colorPalette);

        tripsContext.getCurrentPlot().addStar(record.getRecordId(), starNode);
        starRenderer.addPendingStarNode(starNode);

        eventPublisher.publishEvent(new UpdateSidePanelListEvent(this, record));
    }

    private void flushPendingNodes() {
        List<Node> starNodes = starRenderer.getPendingStarNodes();
        if (!starNodes.isEmpty()) {
            stellarDisplayGroup.getChildren().addAll(starNodes);
            log.debug("Batch added {} star nodes to scene graph", starNodes.size());
        }

        List<Node> polityNodes = starRenderer.getPendingPolityNodes();
        if (!polityNodes.isEmpty()) {
            politiesDisplayGroup.getChildren().addAll(polityNodes);
            politiesDisplayGroup.setVisible(true);
            log.debug("Batch added {} polity nodes to scene graph", polityNodes.size());
        }

        starRenderer.clearPendingNodes();
    }

    // =========================================================================
    // Clear and Reset
    // =========================================================================

    public void clearStars() {
        // Return star spheres to the pool
        lodManager.releaseNodes(stellarDisplayGroup.getChildren());

        stellarDisplayGroup.getChildren().clear();
        labelManager.clear();
        politiesDisplayGroup.getChildren().clear();
        extensionManager.clear();
        lodManager.resetStatistics();
        starRenderer.clearCaches();
        starHighlighter.removeCurrentHighlight();

        log.debug("Stars cleared. Pool statistics: {}", lodManager.getNodePool().getStatistics());
    }

    public void clearPlot() {
        tripsContext.getCurrentPlot().clearPlot();
    }

    // =========================================================================
    // Toggle Methods
    // =========================================================================

    public void toggleExtensions(boolean extensionsOn) {
        extensionManager.toggleExtensions(extensionsOn);
    }

    public void toggleStars(boolean starsOn) {
        stellarDisplayGroup.setVisible(starsOn);
        labelManager.setLabelsVisible(starsOn);
    }

    public void toggleLabels(boolean labelSetting) {
        if (tripsContext.getCurrentPlot().isPlotActive()) {
            labelManager.setLabelsVisible(labelSetting);
        }
    }

    public void togglePolities(boolean polities) {
        this.politiesOn = polities;
        log.info("toggle polities: {}", polities);

        if (tripsContext.getCurrentPlot().isPlotActive()) {
            politiesDisplayGroup.setVisible(polities);
        }
    }

    // =========================================================================
    // Star Highlighting
    // =========================================================================

    public void highlightStar(String starId) {
        starHighlighter.highlightStar(starId);
    }

    // =========================================================================
    // Label Management
    // =========================================================================

    public void updateLabels(@NotNull InterstellarSpacePane interstellarSpacePane) {
        labelManager.setControlPaneOffset(controlPaneOffset);
        labelManager.setCameraZ(interstellarSpacePane.getCameraZ());
        labelManager.updateLabels(interstellarSpacePane.getBoundsInParent());
    }

    public @NotNull Label createLabel(@NotNull StarDisplayRecord record,
                                      @NotNull ColorPalette colorPalette) {
        return labelManager.createLabel(record, colorPalette);
    }

    // =========================================================================
    // Star Creation (for direct access/testing)
    // =========================================================================

    /**
     * Create a stellar object directly.
     * Exposed for testing and direct access scenarios.
     *
     * @param record                 the star record
     * @param colorPalette           the color palette
     * @param isCenter               whether this is the center star
     * @param labelsOn               whether labels are enabled
     * @param politiesOn             whether polities are enabled
     * @param starDisplayPreferences star display preferences
     * @param polityPreferences      polity display preferences
     * @return the created star node
     */
    public @NotNull Node drawStellarObject(@NotNull StarDisplayRecord record,
                                           @NotNull ColorPalette colorPalette,
                                           boolean isCenter,
                                           boolean labelsOn,
                                           boolean politiesOn,
                                           StarDisplayPreferences starDisplayPreferences,
                                           @NotNull CivilizationDisplayPreferences polityPreferences) {
        return starRenderer.createStar(
                record,
                isCenter,
                colorPalette,
                starDisplayPreferences,
                polityPreferences,
                labelsOn,
                politiesOn
        );
    }

    // =========================================================================
    // Configuration
    // =========================================================================

    public void setControlPaneOffset(double controlPaneOffset) {
        this.controlPaneOffset = controlPaneOffset;
    }

    public void setZoomLevel(double zoomLevel) {
        scaleManager.setZoomLevel(zoomLevel);
        lodManager.setZoomLevel(zoomLevel);
    }

    public void setLodEnabled(boolean enabled) {
        lodManager.setLodEnabled(enabled);
    }

    // =========================================================================
    // Routing Support
    // =========================================================================

    public void clearRoutingFlag() {
        contextMenuHandler.clearRoutingFlag();
    }

    public void setManualRouting(ContextManualRoutingDialog manualRoutingDialog) {
        contextMenuHandler.setManualRoutingDialog(manualRoutingDialog);
    }

    // =========================================================================
    // Debug Support
    // =========================================================================

    public void generateRandomStars(int numberStars) {
        if (debugStarGenerator != null) {
            debugStarGenerator.generateRandomStars(numberStars, colorPalette);
        } else {
            log.warn("Debug star generator not initialized; call setGraphics first");
        }
    }

    // =========================================================================
    // Event Listeners
    // =========================================================================

    @EventListener
    public void onHighlightStarEvent(HighlightStarEvent event) {
        Platform.runLater(() -> {
            log.info("Received highlight star event, star id: {}", event.getRecordId());
            highlightStar(event.getRecordId());
        });
    }
}
