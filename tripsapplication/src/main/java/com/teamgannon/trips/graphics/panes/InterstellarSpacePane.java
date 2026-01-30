package com.teamgannon.trips.graphics.panes;

import com.teamgannon.trips.config.application.ScreenSize;
import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.config.application.model.CurrentPlot;
import com.teamgannon.trips.config.application.model.StarDisplayPreferences;
import com.teamgannon.trips.config.application.model.UserControls;
import com.teamgannon.trips.controller.RotationController;
import com.teamgannon.trips.events.ClearListEvent;
import com.teamgannon.trips.events.ColorPaletteChangeEvent;
import com.teamgannon.trips.events.RouteStarFilterEvent;
import com.teamgannon.trips.events.StatusUpdateEvent;
import com.teamgannon.trips.events.UserControlsChangeEvent;
import com.teamgannon.trips.graphics.AstrographicTransformer;
import com.teamgannon.trips.graphics.GridPlotManager;
import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.model.GraphEnablesPersist;
import com.teamgannon.trips.particlefields.InterstellarRingAdapter;
import com.teamgannon.trips.routing.RouteManager;
import com.teamgannon.trips.routing.model.Route;
import com.teamgannon.trips.routing.model.RoutingMetric;
import com.teamgannon.trips.starplotting.NebulaManager;
import com.teamgannon.trips.starplotting.StarPlotManager;
import com.teamgannon.trips.transits.TransitDefinitions;
import com.teamgannon.trips.transits.TransitManager;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Main pane for displaying the 3D interstellar space visualization.
 * Coordinates star plotting, routing, transits, and grid display.
 */
@Slf4j
@Component
public class InterstellarSpacePane extends Pane implements RotationController {

    private final Group world = new Group();
    private final @NotNull SubScene subScene;

    /**
     * Application context.
     */
    private final @NotNull TripsContext tripsContext;

    /**
     * The grid plot manager.
     */
    private final GridPlotManager gridPlotManager;

    @Getter
    private final RouteManager routeManager;

    private final ApplicationEventPublisher eventPublisher;

    @Getter
    private final @NotNull TransitManager transitManager;

    @Getter
    private final @NotNull StarPlotManager starPlotManager;

    /**
     * Nebula rendering manager.
     */
    @Getter
    private final @NotNull NebulaManager nebulaManager;

    /**
     * Camera and view management.
     */
    private final InterstellarCameraController cameraController;

    /**
     * Input handling (mouse/keyboard).
     */
    private final InterstellarInputHandler inputHandler;

    /**
     * The general color palette of the graph.
     */
    private ColorPalette colorPalette;

    /**
     * Star display specifics.
     */
    private StarDisplayPreferences starDisplayPreferences;

    /**
     * Offset to scene coordinates to account for the top UI plane.
     */
    private double controlPaneOffset;

    private final PauseTransition labelUpdatePause = new PauseTransition(Duration.millis(75));

    /**
     * Constructor for the Graphics Pane.
     *
     * @param tripsContext the application context
     */
    public InterstellarSpacePane(TripsContext tripsContext,
                                 ApplicationEventPublisher eventPublisher,
                                 StarPlotManager starPlotManager,
                                 RouteManager routeManager,
                                 GridPlotManager gridPlotManager,
                                 @NotNull TransitManager transitManager,
                                 @NotNull NebulaManager nebulaManager) {

        this.tripsContext = tripsContext;
        this.eventPublisher = eventPublisher;
        this.transitManager = transitManager;
        this.nebulaManager = nebulaManager;
        ScreenSize screenSize = tripsContext.getScreenSize();
        this.starPlotManager = starPlotManager;
        this.routeManager = routeManager;
        this.gridPlotManager = gridPlotManager;

        this.colorPalette = tripsContext.getAppViewPreferences().getColorPalette();
        this.starDisplayPreferences = tripsContext.getAppViewPreferences().getStarDisplayPreferences();

        // Create camera controller and attach transforms to world
        cameraController = new InterstellarCameraController(world);
        cameraController.setOnViewChange(this::updateLabels);

        subScene = new SubScene(world, screenSize.getSceneWidth(), screenSize.getSceneHeight(), true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.BLACK);
        subScene.setCamera(cameraController.getCamera());

        Group sceneRoot = new Group(subScene);

        this.setBackground(Background.EMPTY);
        this.getChildren().add(sceneRoot);
        this.setPickOnBounds(false);

        subScene.widthProperty().bind(this.widthProperty());
        subScene.heightProperty().bind(this.heightProperty());
        widthProperty().addListener((obs, oldValue, newValue) -> scheduleLabelUpdate());
        heightProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue.doubleValue() > 0) {
                scheduleLabelUpdate();
            }
        });

        // Create input handler and initialize events
        inputHandler = new InterstellarInputHandler(subScene, cameraController, this::updateLabels);
        inputHandler.initialize();

        // Set up star plot manager
        starPlotManager.setGraphics(sceneRoot, world, subScene);

        // Setup route manager
        routeManager.setGraphics(sceneRoot, world, subScene, this);

        // Setup grid manager
        gridPlotManager.setGraphics(sceneRoot, world, subScene);

        // Setup transit manager
        this.transitManager.setGraphics(sceneRoot, world, subScene, this);

        // Setup nebula manager
        nebulaManager.setParentGroup(world);

        log.info("startup complete");
    }

    // =========================================================================
    // View Control Methods
    // =========================================================================

    public void setRotationAngles(double xAngle, double yAngle, double zAngle) {
        cameraController.setRotationAngles(xAngle, yAngle, zAngle);
    }

    public void resetPosition() {
        cameraController.resetPosition();
    }

    public void setInitialView() {
        cameraController.setInitialView();
    }

    public void resetView() {
        cameraController.resetView();
    }

    public void zoomIn() {
        cameraController.zoomIn();
    }

    public void zoomIn(int amount) {
        cameraController.zoomIn(amount);
    }

    public void zoomOut() {
        cameraController.zoomOut();
    }

    public void zoomOut(int amount) {
        cameraController.zoomOut(amount);
    }

    public void shiftDisplayLeft(boolean shift) {
        double width = getWidth();
        if (width <= 0) {
            width = subScene.getWidth();
        }
        cameraController.shiftDisplayLeft(shift, width);
    }

    public void toggleAnimation() {
        cameraController.toggleAnimation();
    }

    /**
     * Get the current camera Z position (for zoom-based calculations).
     *
     * @return camera Z translation value
     */
    public double getCameraZ() {
        return cameraController.getCamera().getTranslateZ();
    }

    // =========================================================================
    // User Controls
    // =========================================================================

    public void changeUserControls(UserControls userControls) {
        inputHandler.setUserControls(userControls);
    }

    @EventListener
    public void onUserControlsChangeEvent(UserControlsChangeEvent event) {
        changeUserControls(event.getUserControls());
    }

    // =========================================================================
    // Label Updates
    // =========================================================================

    public void updateLabels() {
        starPlotManager.updateLabels(this);
        gridPlotManager.updateScale();
        gridPlotManager.updateLabels(this);
        routeManager.updateLabels();
        transitManager.updateLabels();
    }

    private void scheduleLabelUpdate() {
        labelUpdatePause.setOnFinished(event -> updateLabels());
        labelUpdatePause.playFromStart();
    }

    // =========================================================================
    // Star Management
    // =========================================================================

    public void simulateStars(int numberStars) {
        starPlotManager.generateRandomStars(numberStars);
        Platform.runLater(this::updateLabels);
    }

    public void plotStars(CurrentPlot currentPlot) {
        boolean showStems = tripsContext.getAppViewPreferences().getGraphEnablesPersist().isDisplayStems() &&
                tripsContext.getAppViewPreferences().getGraphEnablesPersist().isDisplayGrid();
        starPlotManager.drawStars(currentPlot, showStems);
    }

    public List<StarDisplayRecord> getCurrentStarsInView() {
        return starPlotManager.getCurrentStarsInView();
    }

    public void clearStars() {
        starPlotManager.clearStars();
        clearRoutes();
        eventPublisher.publishEvent(new ClearListEvent(this));
    }

    // =========================================================================
    // Transit Management
    // =========================================================================

    public void findTransits(TransitDefinitions transitDefinitions) {
        List<StarDisplayRecord> starsInView = getCurrentStarsInView();
        transitManager.findTransits(transitDefinitions, starsInView);
    }

    public void clearTransits() {
        transitManager.clearTransits();
    }

    public void toggleTransits(boolean transitsOn) {
        transitManager.setVisible(transitsOn);
    }

    public void toggleTransitLengths(boolean transitsLengthsOn) {
        transitManager.toggleTransitLengths(transitsLengthsOn);
    }

    // =========================================================================
    // Route Management
    // =========================================================================

    public void plotRoutes(@NotNull List<Route> routeList) {
        routeManager.clearRoutes();
        routeManager.plotRoutes(routeList);
    }

    public void clearRoutes() {
        routeManager.clearRoutes();
    }

    public void toggleRoutes(boolean routesOn) {
        routeManager.toggleRoutes(routesOn);
    }

    public void toggleRouteLengths(boolean routesLengthsOn) {
        routeManager.toggleRouteLengths(routesLengthsOn);
    }

    public void redrawRoutes(@NotNull List<Route> routes) {
        routeManager.clearRoutes();
        routeManager.plotRoutes(routes);
        updateLabels();
    }

    public void plotRouteDescriptors(DataSetDescriptor currentDataSet, @NotNull List<RoutingMetric> routeDescriptorList) {
        routeManager.plotRouteDescriptors(currentDataSet, routeDescriptorList);
        updateLabels();
    }

    public void displayRoute(RouteDescriptor routeDescriptor, boolean state) {
        routeManager.changeDisplayStateOfRoute(routeDescriptor, state);
    }

    // =========================================================================
    // Grid Management
    // =========================================================================

    public void rebuildGrid(double[] centerCoordinates, @NotNull AstrographicTransformer transformer, CurrentPlot colorPalette) {
        gridPlotManager.rebuildGrid(centerCoordinates, transformer, colorPalette);
    }

    public void toggleGrid(boolean gridToggle) {
        gridPlotManager.toggleGrid(gridToggle);
        starPlotManager.toggleExtensions(gridPlotManager.isVisible());
    }

    public void toggleScale(boolean scaleOn) {
        gridPlotManager.toggleScale(scaleOn);
    }

    // =========================================================================
    // Nebula Management
    // =========================================================================

    /**
     * Render nebulae within the plot range.
     *
     * @param datasetName     the dataset to query for nebulae
     * @param centerX         plot center X in light-years
     * @param centerY         plot center Y in light-years
     * @param centerZ         plot center Z in light-years
     * @param plotRadius      plot radius in light-years
     * @param scalingFactor   screen units per light-year (from AstrographicTransformer)
     */
    public void renderNebulae(String datasetName,
                               double centerX, double centerY, double centerZ,
                               double plotRadius, double scalingFactor) {
        // Configure adapter with the current scale
        InterstellarRingAdapter adapter = new InterstellarRingAdapter(scalingFactor);
        nebulaManager.setAdapter(adapter);

        // Render nebulae in range
        nebulaManager.renderNebulaeInRange(datasetName, centerX, centerY, centerZ, plotRadius);

        log.info("Rendered {} nebulae in plot range", nebulaManager.getActiveNebulaCount());
    }

    /**
     * Clear all rendered nebulae.
     */
    public void clearNebulae() {
        nebulaManager.clearRenderers();
    }

    /**
     * Toggle nebula visibility.
     *
     * @param visible true to show nebulae, false to hide
     */
    public void toggleNebulae(boolean visible) {
        nebulaManager.setVisible(visible);
    }

    /**
     * Toggle nebula animation.
     *
     * @param enabled true to enable animation, false to disable
     */
    public void toggleNebulaAnimation(boolean enabled) {
        nebulaManager.setAnimationEnabled(enabled);
    }

    /**
     * Update nebula animation. Call this from animation loop if needed.
     *
     * @param timeScale animation speed multiplier
     */
    public void updateNebulaAnimation(double timeScale) {
        nebulaManager.updateAnimation(timeScale);
    }

    /**
     * Get the number of active nebulae.
     *
     * @return count of rendered nebulae
     */
    public int getActiveNebulaCount() {
        return nebulaManager.getActiveNebulaCount();
    }

    // =========================================================================
    // Display Toggles
    // =========================================================================

    public void setGraphPresets(@NotNull GraphEnablesPersist graphEnablesPersist) {
        gridPlotManager.toggleGrid(graphEnablesPersist.isDisplayGrid());
        starPlotManager.toggleExtensions(graphEnablesPersist.isDisplayStems());
        gridPlotManager.toggleScale(graphEnablesPersist.isDisplayLegend());
        starPlotManager.toggleLabels(graphEnablesPersist.isDisplayLabels());
    }

    public void togglePolities(boolean polities) {
        starPlotManager.togglePolities(polities);
    }

    public void toggleExtensions(boolean extensionsOn) {
        starPlotManager.toggleExtensions(extensionsOn);
    }

    public void toggleStars(boolean starsOn) {
        starPlotManager.toggleStars(starsOn);
        if (gridPlotManager.isVisible()) {
            starPlotManager.toggleExtensions(starsOn);
        }
    }

    public void toggleLabels(boolean labelSetting) {
        starPlotManager.toggleLabels(labelSetting);
    }

    // =========================================================================
    // Route Star Filtering
    // =========================================================================

    /**
     * Filter the display to show only stars that are part of the specified routes.
     * This helps visualize route overlaps and identify key crossroads systems.
     *
     * @param routeIds the IDs of routes whose stars should be displayed
     */
    public void filterStarsByRoutes(Set<UUID> routeIds) {
        CurrentPlot currentPlot = tripsContext.getCurrentPlot();
        currentPlot.setRouteStarFilters(routeIds);
        refreshStarDisplay();
    }

    /**
     * Filter the display to show only stars that are part of the specified route.
     *
     * @param routeId the ID of the route whose stars should be displayed
     */
    public void filterStarsByRoute(UUID routeId) {
        CurrentPlot currentPlot = tripsContext.getCurrentPlot();
        currentPlot.clearRouteStarFilters();
        currentPlot.addRouteStarFilter(routeId);
        refreshStarDisplay();
    }

    /**
     * Add a route to the current star filter (shows stars from this route in addition to existing filter).
     *
     * @param routeId the ID of the route to add to filter
     */
    public void addRouteToStarFilter(UUID routeId) {
        CurrentPlot currentPlot = tripsContext.getCurrentPlot();
        currentPlot.addRouteStarFilter(routeId);
        refreshStarDisplay();
    }

    /**
     * Remove a route from the current star filter.
     *
     * @param routeId the ID of the route to remove from filter
     */
    public void removeRouteFromStarFilter(UUID routeId) {
        CurrentPlot currentPlot = tripsContext.getCurrentPlot();
        currentPlot.removeRouteStarFilter(routeId);
        refreshStarDisplay();
    }

    /**
     * Clear all route star filters, showing all stars again.
     */
    public void clearRouteStarFilters() {
        CurrentPlot currentPlot = tripsContext.getCurrentPlot();
        currentPlot.clearRouteStarFilters();
        refreshStarDisplay();
    }

    /**
     * Check if route star filtering is currently active.
     *
     * @return true if filtering is active
     */
    public boolean isRouteStarFilterActive() {
        return tripsContext.getCurrentPlot().isRouteStarFilterActive();
    }

    /**
     * Refresh the star display, applying any active filters.
     */
    private void refreshStarDisplay() {
        CurrentPlot currentPlot = tripsContext.getCurrentPlot();
        boolean showStems = tripsContext.getAppViewPreferences().getGraphEnablesPersist().isDisplayStems() &&
                tripsContext.getAppViewPreferences().getGraphEnablesPersist().isDisplayGrid();

        // Clear current stars and redraw with filter applied
        starPlotManager.clearStars();
        starPlotManager.drawStars(currentPlot, showStems);
        updateLabels();

        // Update status to show filter state
        if (currentPlot.isRouteStarFilterActive()) {
            Set<String> filteredIds = currentPlot.getFilteredStarIds();
            int starCount = filteredIds != null ? filteredIds.size() : 0;
            int routeCount = currentPlot.getRouteStarFilterIds().size();
            eventPublisher.publishEvent(new StatusUpdateEvent(this,
                    "Showing %d stars from %d route(s)".formatted(starCount, routeCount)));
        } else {
            eventPublisher.publishEvent(new StatusUpdateEvent(this, "Showing all stars"));
        }
    }

    // =========================================================================
    // Color and Preferences
    // =========================================================================

    public void changeColors(ColorPalette colorPalette) {
        this.colorPalette = colorPalette;
        tripsContext.getAppViewPreferences().setColorPalette(colorPalette);
    }

    @EventListener
    public void onColorPaletteChangeEvent(ColorPaletteChangeEvent event) {
        changeColors(event.getColorPalette());
    }

    // =========================================================================
    // Route Star Filter Event Handling
    // =========================================================================

    @EventListener
    public void onRouteStarFilterEvent(RouteStarFilterEvent event) {
        javafx.application.Platform.runLater(() -> {
            switch (event.getAction()) {
                case FILTER_BY_ROUTE:
                    filterStarsByRoute(event.getRouteId());
                    eventPublisher.publishEvent(new StatusUpdateEvent(this,
                            "Showing stars from route: " + event.getRouteName()));
                    break;
                case ADD_TO_FILTER:
                    addRouteToStarFilter(event.getRouteId());
                    eventPublisher.publishEvent(new StatusUpdateEvent(this,
                            "Added stars from route: " + event.getRouteName()));
                    break;
                case REMOVE_FROM_FILTER:
                    removeRouteFromStarFilter(event.getRouteId());
                    eventPublisher.publishEvent(new StatusUpdateEvent(this,
                            "Removed stars from route: " + event.getRouteName()));
                    break;
                case CLEAR_FILTER:
                    clearRouteStarFilters();
                    eventPublisher.publishEvent(new StatusUpdateEvent(this,
                            "Showing all stars"));
                    break;
            }
        });
    }

    // =========================================================================
    // Control Pane Offset
    // =========================================================================

    public void setControlPaneOffset(double controlPaneOffset) {
        this.controlPaneOffset = controlPaneOffset;
        starPlotManager.setControlPaneOffset(controlPaneOffset);
        gridPlotManager.setControlPaneOffset(controlPaneOffset);
        routeManager.setControlPaneOffset(controlPaneOffset);
        transitManager.setControlPaneOffset(controlPaneOffset);
    }

    // =========================================================================
    // Clear All
    // =========================================================================

    public void clearAll() {
        clearStars();
        clearRoutes();
        clearTransits();
        clearNebulae();
    }
}
