package com.teamgannon.trips.routing.routemanagement;

import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.config.application.model.CurrentPlot;
import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import com.teamgannon.trips.routing.model.RouteSegment;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.control.Label;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.*;


/**
 * Manages the display and state of routes in the 3D visualization.
 * <p>
 * This class coordinates route rendering, visibility toggling, and route segment tracking.
 * Label management is delegated to {@link RouteLabelManager}.
 * <p>
 * <b>Responsibilities:</b>
 * <ul>
 *   <li>Route group management (adding, removing, toggling visibility)</li>
 *   <li>Route segment deduplication tracking</li>
 *   <li>Manual routing mode state</li>
 *   <li>Coordination with label manager for route distance labels</li>
 * </ul>
 */
@Slf4j
public class RouteDisplay {

    /**
     * The subscene for managing the display plane.
     */
    private final SubScene subScene;

    /**
     * Reference to the drawing screen.
     */
    @Getter
    private InterstellarSpacePane interstellarSpacePane;

    /**
     * Manager for route distance labels.
     */
    private final RouteLabelManager labelManager;

    /**
     * Maps individual routes to their UUID.
     */
    private final Map<UUID, Group> routeLookup = new HashMap<>();

    /**
     * The group containing all routes (used for bulk visibility toggling).
     */
    @Getter
    private final Group routesGroup = new Group();

    /**
     * Whether route labels are currently enabled.
     */
    @Getter
    private boolean routeLabelsOn = true;

    /**
     * Reference to the current TRIPS context.
     */
    private final TripsContext tripsContext;

    /**
     * Whether manual routing mode is active.
     */
    private boolean manualRoutingActive = false;

    /**
     * Set of route segments for deduplication.
     */
    private final Set<RouteSegment> routeSegments = new HashSet<>();


    /**
     * Creates a new RouteDisplay.
     *
     * @param tripsContext          the application context
     * @param subScene              the 3D subscene
     * @param interstellarSpacePane the interstellar space pane
     */
    public RouteDisplay(TripsContext tripsContext,
                        SubScene subScene,
                        InterstellarSpacePane interstellarSpacePane) {
        log.info("Initializing the Route Display");
        this.tripsContext = tripsContext;
        this.subScene = subScene;
        this.interstellarSpacePane = interstellarSpacePane;
        this.labelManager = new RouteLabelManager(subScene, interstellarSpacePane::getBoundsInParent);
    }

    // =========================================================================
    // Manual Routing State
    // =========================================================================

    public boolean isManualRoutingActive() {
        return manualRoutingActive;
    }

    public void setManualRoutingActive(boolean flag) {
        manualRoutingActive = flag;
    }

    // =========================================================================
    // Color Palette
    // =========================================================================

    public ColorPalette getColorPalette() {
        return tripsContext.getAppViewPreferences().getColorPalette();
    }

    // =========================================================================
    // Control Pane Offset
    // =========================================================================

    /**
     * Sets the vertical offset for the control pane.
     *
     * @param controlPaneOffset the offset in pixels
     */
    public void setControlPaneOffset(double controlPaneOffset) {
        labelManager.setControlPaneOffset(controlPaneOffset);
    }

    // =========================================================================
    // Clear / Reset
    // =========================================================================

    /**
     * Clears all routes and labels.
     */
    public void clear() {
        labelManager.clear();
        routeLabelsOn = false;
        routeLookup.clear();
        routesGroup.getChildren().clear();
        routeSegments.clear();
    }

    // =========================================================================
    // Route Segment Tracking (Deduplication)
    // =========================================================================

    /**
     * Checks if a route segment is already tracked.
     *
     * @param routeSegment the route segment to check
     * @return true if the segment is already present
     */
    public boolean contains(RouteSegment routeSegment) {
        return routeSegments.contains(routeSegment);
    }

    /**
     * Adds a route segment to the tracking set.
     *
     * @param routeSegment the route segment to add
     */
    public void addSegment(RouteSegment routeSegment) {
        routeSegments.add(routeSegment);
    }

    // =========================================================================
    // Route Visibility
    // =========================================================================

    /**
     * Changes the display state of a specific route.
     *
     * @param routeDescriptor the route descriptor
     * @param state           true to show, false to hide
     */
    public void changeDisplayStateOfRoute(RouteDescriptor routeDescriptor, boolean state) {
        log.info("Change state of route {} to {}", routeDescriptor.getName(), state);
        Group route = getRoute(routeDescriptor.getId());
        if (route != null) {
            route.setVisible(state);
        } else {
            log.error("Requested route is null: {}", routeDescriptor);
        }
    }

    /**
     * Toggles visibility of all routes (without affecting labels).
     *
     * @param visible true to show routes
     */
    public void toggleRouteVisibility(boolean visible) {
        routesGroup.setVisible(visible);
    }

    /**
     * Toggles visibility of both routes and labels.
     *
     * @param visible true to show routes and labels
     */
    public void toggleRoutes(boolean visible) {
        routesGroup.setVisible(visible);
        labelManager.setLabelsVisible(visible);
    }

    /**
     * Toggles visibility of route length labels only.
     *
     * @param visible true to show labels
     */
    public void toggleRouteLengths(boolean visible) {
        labelManager.setLabelsVisible(visible);
    }

    // =========================================================================
    // Label Management (Delegated to RouteLabelManager)
    // =========================================================================

    /**
     * Checks if a label is already registered.
     *
     * @param label the label to check
     * @return true if present
     */
    public boolean isLabelPresent(Label label) {
        return labelManager.containsLabel(label);
    }

    /**
     * Removes the label associated with a 3D object.
     *
     * @param object the anchor node
     */
    public void removeObject(Node object) {
        labelManager.removeLabelForNode(object);
    }

    /**
     * Links a 3D object to a label.
     *
     * @param object the anchor node
     * @param label  the label
     */
    public void linkObjectToLabel(Node object, Label label) {
        labelManager.linkLabel(object, label);
    }

    /**
     * Removes a label.
     *
     * @param label the label to remove
     */
    public void removeLabel(Label label) {
        labelManager.removeLabel(label);
    }

    /**
     * Gets the label display group (for adding to scene root).
     *
     * @return the label display group
     */
    public Group getLabelDisplayGroup() {
        return labelManager.getLabelDisplayGroup();
    }

    /**
     * Updates all label positions after view rotation/zoom.
     */
    public void updateLabels() {
        labelManager.updateLabels();
    }

    // =========================================================================
    // Route Group Management
    // =========================================================================

    /**
     * Gets a route group by its UUID.
     *
     * @param id the route UUID
     * @return the route group, or null if not found
     */
    public Group getRoute(UUID id) {
        return routeLookup.get(id);
    }

    /**
     * Removes a route from the lookup by ID.
     *
     * @param id the route UUID
     */
    public void removeRouteId(UUID id) {
        routeLookup.remove(id);
    }

    /**
     * Adds a route to the display.
     *
     * @param routeDescriptor the route descriptor
     * @param routeToAdd      the route group to add
     */
    public void addRouteToDisplay(RouteDescriptor routeDescriptor, Group routeToAdd) {
        log.info("Add new route to display: {}", routeDescriptor.getName());
        if (routeDescriptor != null) {
            if (!routesGroup.getChildren().contains(routeToAdd)) {
                routeLookup.put(routeDescriptor.getId(), routeToAdd);
                routesGroup.getChildren().add(routeToAdd);
            } else {
                log.error("Already contains route: {}", routeDescriptor);
            }
        }
    }

    /**
     * Removes a route from the display.
     *
     * @param routeToRemove the route group to remove
     */
    public void removeRouteFromDisplay(Group routeToRemove) {
        routesGroup.getChildren().remove(routeToRemove);
    }

    // =========================================================================
    // Lombok @Data replacement - explicit getters for SubScene
    // =========================================================================

    public SubScene getSubScene() {
        return subScene;
    }

    // =========================================================================
    // Spatial Index Support
    // =========================================================================

    /**
     * Gets the route spatial index from the current plot.
     *
     * @return the route spatial index, or null if not available
     */
    public RouteSegmentSpatialIndex getRouteSpatialIndex() {
        CurrentPlot currentPlot = tripsContext.getCurrentPlot();
        if (currentPlot != null) {
            return currentPlot.getRouteSpatialIndex();
        }
        return null;
    }

    /**
     * Gets the set of route IDs that have segments within the specified viewport radius.
     * <p>
     * This is useful for quickly determining which routes need to be rendered
     * without checking every segment individually.
     *
     * @param viewportRadius the viewport radius (typically the view distance)
     * @return set of route IDs with potentially visible segments
     */
    public @NotNull Set<UUID> getVisibleRouteIds(double viewportRadius) {
        CurrentPlot currentPlot = tripsContext.getCurrentPlot();
        if (currentPlot != null) {
            return currentPlot.getVisibleRouteIds(viewportRadius);
        }
        return new HashSet<>(routeLookup.keySet());
    }

    /**
     * Gets route segments within the viewport radius.
     * <p>
     * This can be used for selective segment rendering when many routes exist.
     *
     * @param viewportRadius the viewport radius
     * @return list of indexed segments within the radius
     */
    public @NotNull List<IndexedRouteSegment> getVisibleSegments(double viewportRadius) {
        CurrentPlot currentPlot = tripsContext.getCurrentPlot();
        if (currentPlot != null) {
            return currentPlot.getRouteSegmentsWithinRadius(viewportRadius);
        }
        return Collections.emptyList();
    }

    /**
     * Logs route spatial index statistics for performance monitoring.
     */
    public void logSpatialIndexStatistics() {
        CurrentPlot currentPlot = tripsContext.getCurrentPlot();
        if (currentPlot != null) {
            String stats = currentPlot.getRouteSpatialIndexStatistics();
            if (!stats.isEmpty()) {
                log.info("Route spatial index: {}", stats);
            }
        }
    }

    /**
     * Gets the total number of routes currently displayed.
     *
     * @return route count
     */
    public int getRouteCount() {
        return routeLookup.size();
    }

    /**
     * Gets the total number of route segments tracked.
     *
     * @return segment count
     */
    public int getSegmentCount() {
        return routeSegments.size();
    }
}
