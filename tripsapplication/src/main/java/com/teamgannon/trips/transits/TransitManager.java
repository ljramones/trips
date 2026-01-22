package com.teamgannon.trips.transits;

import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import javafx.scene.Group;
import javafx.scene.SubScene;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Manages transit visualization across all transit bands.
 * Coordinates TransitRouteVisibilityGroup instances and their scene graph placement.
 */
@Slf4j
@Component
public class TransitManager {

    private final Map<UUID, TransitRouteVisibilityGroup> transitMap = new HashMap<>();

    private Group transitGroup;
    private final Group labelDisplayGroup = new Group();

    /**
     * Spatial index for efficient transit queries (viewport culling, nearest neighbor, etc.).
     */
    @Getter
    private TransitSpatialIndex spatialIndex = new TransitSpatialIndex();

    private final TripsContext tripsContext;
    private final TransitCalculatorFactory calculatorFactory;
    private final TransitRouteBuilderService routeBuilderService;

    // Graphics state for building context
    private SubScene subScene;
    private InterstellarSpacePane interstellarSpacePane;
    private double controlPaneOffset;

    public TransitManager(TripsContext tripsContext,
                          TransitCalculatorFactory calculatorFactory,
                          TransitRouteBuilderService routeBuilderService) {
        this.tripsContext = tripsContext;
        this.calculatorFactory = calculatorFactory;
        this.routeBuilderService = routeBuilderService;
    }

    /**
     * Initialize graphics references. Must be called before using transit features.
     */
    public void setGraphics(Group sceneRoot,
                            Group world,
                            SubScene subScene,
                            InterstellarSpacePane interstellarSpacePane) {
        this.interstellarSpacePane = interstellarSpacePane;
        this.subScene = subScene;
        transitGroup = new Group();
        world.getChildren().add(transitGroup);
        sceneRoot.getChildren().add(labelDisplayGroup);
    }

    /**
     * Set the control pane offset for label positioning.
     */
    public void setControlPaneOffset(double controlPaneOffset) {
        this.controlPaneOffset = controlPaneOffset;
    }

    /**
     * Set the current dataset for route building.
     */
    public void setDataSetDescriptor(DataSetDescriptor descriptor) {
        routeBuilderService.setDataSetDescriptor(descriptor);
    }

    /**
     * Build the graphics context for creating visibility groups.
     *
     * @param starCount the number of stars being processed (used to select optimal algorithm)
     */
    private TransitGraphicsContext buildContext(int starCount) {
        ITransitDistanceCalculator calculator = calculatorFactory.getCalculator(starCount);
        return TransitGraphicsContext.builder()
                .subScene(subScene)
                .interstellarSpacePane(interstellarSpacePane)
                .controlPaneOffset(controlPaneOffset)
                .distanceCalculator(calculator)
                .routeBuilderService(routeBuilderService)
                .tripsContext(tripsContext)
                .build();
    }

    /**
     * Find and display transits for stars in view.
     * <p>
     * Automatically selects the optimal algorithm based on star count:
     * <ul>
     *   <li>≤ 100 stars: O(n²) brute-force (lower overhead)</li>
     *   <li>> 100 stars: O(n log n) KD-Tree with parallel queries</li>
     * </ul>
     */
    public void findTransits(TransitDefinitions transitDefinitions, @NotNull List<StarDisplayRecord> starsInView) {
        clearTransits();

        log.debug("Finding transits for {} stars", starsInView.size());
        TransitGraphicsContext context = buildContext(starsInView.size());
        List<TransitRangeDef> transitRangeDefList = transitDefinitions.getTransitRangeDefs();

        for (TransitRangeDef transitRangeDef : transitRangeDefList) {
            if (transitRangeDef.isEnabled()) {
                TransitRouteVisibilityGroup visibilityGroup = new TransitRouteVisibilityGroup(context, transitRangeDef);
                visibilityGroup.plotTransit(transitRangeDef, starsInView);
                installGroup(visibilityGroup);
            }
        }

        // Build spatial index from computed transits
        rebuildSpatialIndex();

        updateLabels();
        log.debug("Transits computed and displayed");
    }

    private void installGroup(TransitRouteVisibilityGroup visibilityGroup) {
        transitMap.put(visibilityGroup.getGroupId(), visibilityGroup);
        transitGroup.getChildren().add(visibilityGroup.getGroup());
        labelDisplayGroup.getChildren().add(visibilityGroup.getLabelGroup());
    }

    private void uninstallGroup(TransitRouteVisibilityGroup visibilityGroup) {
        transitGroup.getChildren().remove(visibilityGroup.getGroup());
        labelDisplayGroup.getChildren().remove(visibilityGroup.getLabelGroup());
        visibilityGroup.clear();
    }

    /**
     * Show or hide a specific transit band.
     */
    public void showTransit(UUID bandId, boolean show) {
        log.debug("Transit band {} visibility: {}", bandId, show);
        TransitRouteVisibilityGroup visibilityGroup = transitMap.get(bandId);
        if (visibilityGroup != null) {
            visibilityGroup.toggleTransit(show);
        }
    }

    /**
     * Show or hide labels for a specific transit band.
     */
    public void showLabels(UUID bandId, boolean show) {
        log.debug("Transit band {} labels: {}", bandId, show);
        TransitRouteVisibilityGroup visibilityGroup = transitMap.get(bandId);
        if (visibilityGroup != null) {
            visibilityGroup.toggleLabels(show);
        }
    }

    /**
     * Check if transits are currently visible.
     */
    public boolean isVisible() {
        return transitGroup != null && transitGroup.isVisible();
    }

    /**
     * Set visibility of all transits and labels.
     */
    public void setVisible(boolean visible) {
        transitGroup.setVisible(visible);
        labelDisplayGroup.setVisible(visible);
    }

    /**
     * Clear all transits from the display.
     */
    public void clearTransits() {
        transitGroup.getChildren().clear();
        for (TransitRouteVisibilityGroup group : transitMap.values()) {
            if (group != null) {
                uninstallGroup(group);
            }
        }
        transitMap.clear();
        labelDisplayGroup.getChildren().clear();
        spatialIndex.clear();
    }

    /**
     * Toggle visibility of all transit labels.
     */
    public void toggleTransitLengths(boolean showLabels) {
        log.debug("Transit labels visibility: {}", showLabels);
        labelDisplayGroup.setVisible(showLabels);
        for (TransitRouteVisibilityGroup visibilityGroup : transitMap.values()) {
            visibilityGroup.toggleLabels(showLabels);
        }
    }

    /**
     * Update all label positions (call after rotation/zoom).
     */
    public void updateLabels() {
        for (TransitRouteVisibilityGroup visibilityGroup : transitMap.values()) {
            visibilityGroup.updateLabels();
        }
    }

    /**
     * Apply pre-calculated transit routes to the display.
     * Use this when routes have been calculated asynchronously via {@link TransitCalculationService}.
     *
     * @param result the calculation result containing routes by band
     */
    public void applyCalculatedTransits(TransitCalculationResult result) {
        clearTransits();

        if (!result.isSuccess()) {
            log.warn("Cannot apply transits: calculation was not successful (cancelled={}, error={})",
                    result.isCancelled(), result.getErrorMessage());
            return;
        }

        log.debug("Applying {} pre-calculated transits", result.getTotalRoutes());
        TransitGraphicsContext context = buildContext(result.getTotalRoutes());

        TransitDefinitions definitions = result.getTransitDefinitions();
        for (TransitRangeDef rangeDef : definitions.getTransitRangeDefs()) {
            if (!rangeDef.isEnabled()) {
                continue;
            }

            List<TransitRoute> routes = result.getRoutesByBand().get(rangeDef.getBandId());
            if (routes == null || routes.isEmpty()) {
                log.debug("No routes for band {}", rangeDef.getBandName());
                continue;
            }

            TransitRouteVisibilityGroup visibilityGroup = new TransitRouteVisibilityGroup(context, rangeDef);
            visibilityGroup.plotPreCalculatedRoutes(routes);
            installGroup(visibilityGroup);
        }

        // Build spatial index from applied transits
        rebuildSpatialIndex();

        updateLabels();
        log.debug("Pre-calculated transits applied and displayed");
    }

    /**
     * Get the calculator factory for access to calculators.
     */
    public TransitCalculatorFactory getCalculatorFactory() {
        return calculatorFactory;
    }

    // =========================================================================
    // Spatial Index Support
    // =========================================================================

    /**
     * Rebuilds the spatial index from all current transit visibility groups.
     */
    private void rebuildSpatialIndex() {
        spatialIndex.clear();

        for (TransitRouteVisibilityGroup group : transitMap.values()) {
            String bandId = group.getGroupId().toString();
            Collection<TransitRoute> routes = group.getTransitRoutes();
            if (!routes.isEmpty()) {
                spatialIndex.addTransits(bandId, routes);
            }
        }

        if (!spatialIndex.isEmpty()) {
            log.info("Transit spatial index built: {} bands, {} transits",
                    spatialIndex.getBandCount(), spatialIndex.getTotalTransits());
        }
    }

    /**
     * Finds transits within the specified radius of a point.
     * <p>
     * This is useful for viewport culling and click detection.
     *
     * @param centerX query center X coordinate
     * @param centerY query center Y coordinate
     * @param centerZ query center Z coordinate
     * @param radius  query sphere radius
     * @return list of indexed transits within the radius
     */
    public @NotNull List<IndexedTransit> findTransitsWithinRadius(
            double centerX, double centerY, double centerZ, double radius) {
        return spatialIndex.findTransitsWithinRadius(centerX, centerY, centerZ, radius);
    }

    /**
     * Finds the nearest transit to a point.
     * <p>
     * Useful for click detection on transit lines.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     * @return the nearest transit, or null if no transits exist
     */
    public @Nullable IndexedTransit findNearestTransit(double x, double y, double z) {
        return spatialIndex.findNearestTransit(x, y, z);
    }

    /**
     * Gets the set of band IDs that have transits within the query sphere.
     *
     * @param centerX query center X
     * @param centerY query center Y
     * @param centerZ query center Z
     * @param radius  query radius
     * @return set of band IDs with visible transits
     */
    public @NotNull Set<String> getVisibleBandIds(
            double centerX, double centerY, double centerZ, double radius) {
        return spatialIndex.getVisibleBandIds(centerX, centerY, centerZ, radius);
    }

    /**
     * Gets transit spatial index statistics for monitoring.
     *
     * @return statistics string
     */
    public @NotNull String getSpatialIndexStatistics() {
        return spatialIndex.getStatistics();
    }

    /**
     * Logs transit spatial index statistics.
     */
    public void logSpatialIndexStatistics() {
        spatialIndex.logStatistics();
    }
}
