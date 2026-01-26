package com.teamgannon.trips.config.application.model;

import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.RouteVisibility;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.CivilizationDisplayPreferences;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.measure.TrackExecutionTime;
import com.teamgannon.trips.routing.routemanagement.IndexedRouteSegment;
import com.teamgannon.trips.routing.routemanagement.RouteSegmentSpatialIndex;
import com.teamgannon.trips.starplotting.VisualizationSpatialIndex;
import javafx.scene.Node;
import javafx.scene.control.Label;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;


@Slf4j
@Data
public class CurrentPlot {

    /**
     * the lookout for drawn stars
     */
    private final Map<String, Node> starLookup = new HashMap<>();

    /**
     * a one way map form star id to label of the star
     */
    private final Map<String, Label> starToLabelLookup = new HashMap<>();

    /**
     * the dataset descriptor for this plot
     */
    private DataSetDescriptor dataSetDescriptor;

    /**
     * whether the plot is currently active
     */
    private boolean plotActive = false;

    /**
     * the center coordinates for this plot
     */
    private double[] centerCoordinates;

    /**
     * the center star
     */
    private String centerStar;

    /**
     * the list of stars
     */
    private @NotNull List<StarDisplayRecord> starDisplayRecordList = new ArrayList<>();

    /**
     * the color palette
     */
    private ColorPalette colorPalette;

    /**
     * a map from the full route to the partial routes associated with it
     */
    private Map<UUID, RouteDescriptor> routeMapping = new HashMap<>();

    /**
     * civilization preferences from DB
     */
    private CivilizationDisplayPreferences civilizationDisplayPreferences;

    /**
     * star display preferences from DB
     */
    private StarDisplayPreferences starDisplayPreferences;

    /**
     * Spatial index for efficient star queries (viewport culling, nearest neighbor, etc.).
     * Built lazily when first needed after stars are added.
     */
    private transient VisualizationSpatialIndex spatialIndex;

    /**
     * Flag indicating the spatial index needs to be rebuilt.
     */
    private transient boolean spatialIndexDirty = true;

    /**
     * Spatial index for efficient route segment queries (viewport culling).
     * Built lazily when first needed after routes are added.
     */
    private transient RouteSegmentSpatialIndex routeSpatialIndex;

    /**
     * Flag indicating the route spatial index needs to be rebuilt.
     */
    private transient boolean routeSpatialIndexDirty = true;


    /**
     * setup the mechanics of the plot
     *
     * @param dataSetDescriptor the dataset descriptor that this plot belongs to
     * @param centerCoordinates the center x,y,z coordinates of the plot
     * @param centerStar        the center star of the plot
     * @param colorPalette      the color palette
     */
    public void setupPlot(DataSetDescriptor dataSetDescriptor,
                          double[] centerCoordinates,
                          String centerStar,
                          ColorPalette colorPalette) {

        this.dataSetDescriptor = dataSetDescriptor;
        this.centerCoordinates = centerCoordinates;
        this.centerStar = centerStar;
        this.colorPalette = colorPalette;
    }

    public void setCivilizationDisplayPreferences(CivilizationDisplayPreferences civilizationDisplayPreferences) {
        this.civilizationDisplayPreferences = civilizationDisplayPreferences;
    }

    public void setStarDisplayPreferences(StarDisplayPreferences starDisplayPreferences) {
        this.starDisplayPreferences = starDisplayPreferences;
    }

    /**
     * clear the plot
     */
    public void clearPlot() {
        starDisplayRecordList.clear();
        starToLabelLookup.clear();
        starLookup.clear();
        plotActive = false;
        centerCoordinates = new double[3];
        clearRoutes();
        // Clear spatial indices
        spatialIndex = null;
        spatialIndexDirty = true;
        routeSpatialIndex = null;
        routeSpatialIndexDirty = true;
        // Clear route star filters
        routeStarFilterIds.clear();
        filteredStarIds = null;
    }

    ////////////////  route management   //////////////////////

    /**
     * add a route to the plot
     *
     * @param routeDescriptor the route descriptor to add
     */
    public void addRoute(UUID parent, RouteDescriptor routeDescriptor) {
        log.info("\nAdd route with Id={}\n", routeDescriptor.getId());
        if (!routeMapping.containsKey(routeDescriptor.getId())) {
            routeMapping.put(routeDescriptor.getId(), routeDescriptor);
            routeSpatialIndexDirty = true;
        }
    }

    public void addRoutes(UUID parent, List<RouteDescriptor> routeDescriptorList) {
        for (RouteDescriptor routeDescriptor : routeDescriptorList) {
            addRoute(parent, routeDescriptor);
        }
    }


    /**
     * remove a route from the plot
     *
     * @param routeDescriptor the route descriptor to remove
     */
    public void removeRoute(RouteDescriptor routeDescriptor) {
        log.info("\nRemoved route with Id={}\n", routeDescriptor.getId());
        routeMapping.remove(routeDescriptor.getId());
        routeSpatialIndexDirty = true;
    }

    /**
     * get a route based on id
     *
     * @param id the id to look up
     * @return the route descriptor or null if not found
     */
    public RouteDescriptor getRoute(UUID id) {
        return routeMapping.get(id);
    }

    /**
     * get all the routes as a List
     *
     * @return the routes
     */
    public List<RouteDescriptor> getRoutes() {
        return new ArrayList<>(routeMapping.values());
    }

    /**
     * clear the routes
     */
    public void clearRoutes() {
        routeMapping.clear();
        if (routeSpatialIndex != null) {
            routeSpatialIndex.clear();
        }
        routeSpatialIndexDirty = true;
    }

    /**
     * get the map showing which routes are visible
     *
     * @return the route visibility map
     */
    public Map<UUID, RouteVisibility> getVisibilityMap() {
        Map<UUID, RouteVisibility> visibilityMap = new HashMap<>();
        for (UUID id : routeMapping.keySet()) {
            RouteDescriptor routeDescriptor = routeMapping.get(id);
            visibilityMap.put(id, routeDescriptor.getVisibility());
        }
        return visibilityMap;
    }

    ////////////////// route star filter /////////////////

    /**
     * Set of route IDs to filter stars by.
     * When not empty, only stars that are part of these routes will be displayed.
     */
    private final Set<UUID> routeStarFilterIds = new HashSet<>();

    /**
     * Cached set of star IDs that are part of filtered routes.
     * Rebuilt when filter changes.
     */
    private transient Set<String> filteredStarIds = null;

    /**
     * Enable filtering to show only stars from the specified route.
     *
     * @param routeId the route ID to filter by
     */
    public void addRouteStarFilter(UUID routeId) {
        if (routeId != null && routeMapping.containsKey(routeId)) {
            routeStarFilterIds.add(routeId);
            rebuildFilteredStarIds();
            log.info("Added route star filter for route: {}", routeId);
        }
    }

    /**
     * Enable filtering to show only stars from the specified routes.
     *
     * @param routeIds the set of route IDs to filter by
     */
    public void setRouteStarFilters(Set<UUID> routeIds) {
        routeStarFilterIds.clear();
        if (routeIds != null) {
            for (UUID id : routeIds) {
                if (routeMapping.containsKey(id)) {
                    routeStarFilterIds.add(id);
                }
            }
        }
        rebuildFilteredStarIds();
        log.info("Set route star filters: {} routes", routeStarFilterIds.size());
    }

    /**
     * Remove a route from the star filter.
     *
     * @param routeId the route ID to remove from filter
     */
    public void removeRouteStarFilter(UUID routeId) {
        if (routeStarFilterIds.remove(routeId)) {
            rebuildFilteredStarIds();
            log.info("Removed route star filter for route: {}", routeId);
        }
    }

    /**
     * Clear all route star filters, showing all stars again.
     */
    public void clearRouteStarFilters() {
        routeStarFilterIds.clear();
        filteredStarIds = null;
        log.info("Cleared all route star filters");
    }

    /**
     * Check if route star filtering is active.
     *
     * @return true if filtering is active
     */
    public boolean isRouteStarFilterActive() {
        return !routeStarFilterIds.isEmpty();
    }

    /**
     * Get the IDs of routes currently being used for filtering.
     *
     * @return unmodifiable set of route IDs
     */
    public Set<UUID> getRouteStarFilterIds() {
        return Collections.unmodifiableSet(routeStarFilterIds);
    }

    /**
     * Check if a star should be displayed based on the current route filter.
     * If no filter is active, all stars are shown.
     *
     * @param starId the star's record ID
     * @return true if the star should be displayed
     */
    public boolean shouldDisplayStar(String starId) {
        if (!isRouteStarFilterActive()) {
            return true; // No filter active, show all stars
        }
        if (filteredStarIds == null) {
            rebuildFilteredStarIds();
        }
        return filteredStarIds.contains(starId);
    }

    /**
     * Get the set of star IDs that pass the current route filter.
     * Returns null if no filter is active.
     *
     * @return set of filtered star IDs, or null if no filter
     */
    @Nullable
    public Set<String> getFilteredStarIds() {
        if (!isRouteStarFilterActive()) {
            return null;
        }
        if (filteredStarIds == null) {
            rebuildFilteredStarIds();
        }
        return Collections.unmodifiableSet(filteredStarIds);
    }

    /**
     * Rebuild the cached set of filtered star IDs from the current route filters.
     */
    private void rebuildFilteredStarIds() {
        if (routeStarFilterIds.isEmpty()) {
            filteredStarIds = null;
            return;
        }

        filteredStarIds = new HashSet<>();
        for (UUID routeId : routeStarFilterIds) {
            RouteDescriptor route = routeMapping.get(routeId);
            if (route != null && route.getRouteList() != null) {
                filteredStarIds.addAll(route.getRouteList());
            }
        }
        log.info("Rebuilt filtered star IDs: {} stars from {} routes",
                filteredStarIds.size(), routeStarFilterIds.size());
    }

    //////////////////// star records  ///////////////////

    /**
     * add a record
     *
     * @param record the record
     */
    public void addRecord(StarDisplayRecord record) {

        // center star is allows labeled
        if (record.isCenter()) {
            record.setDisplayLabel(true);
        }
        if (record.isLabelForced()) {
            record.setCurrentLabelDisplayScore(1000);
        }
        starDisplayRecordList.add(record);
        // Mark spatial index as needing rebuild
        spatialIndexDirty = true;
    }

    /**
     * retrieve a star
     *
     * @param starId the guid for the star
     * @return the star
     */
    public Node getStar(String starId) {
        return starLookup.get(starId);
    }

    /**
     * add a star
     *
     * @param id   the id of the star
     * @param star the star
     */
    public void addStar(String id, Node star) {
        starLookup.put(id, star);
    }

    /**
     * get the ids of the star
     *
     * @return the set of ids
     */
    public @NotNull Set<String> getStarIds() {
        return new HashSet<>(starLookup.keySet());
    }

    /**
     * check if a star is present and visible on the plot
     *
     * @param id the star id
     * @return true is present and visible
     */
    public boolean isStarVisible(UUID id) {
        if (id == null) {
            return false;
        }
        return starLookup.containsKey(id.toString());
    }

    ////////////////// label management  //////////////////

    /**
     * map a label to a star
     *
     * @param starId    the star id
     * @param starLabel the label for the star
     */
    public void mapLabelToStar(String starId, Label starLabel) {
        starToLabelLookup.put(starId, starLabel);
    }

    public void mapLabelToStar(UUID starId, Label starLabel) {
        if (starId == null) {
            return;
        }
        mapLabelToStar(starId.toString(), starLabel);
    }

    /**
     * get a label for the star
     *
     * @param starId the star id
     * @return the label
     */
    public Label getLabelForStar(UUID starId) {
        if (starId == null) {
            return null;
        }
        return starToLabelLookup.get(starId.toString());
    }

    public Label getLabelForStar(String starId) {
        if (starId == null) {
            return null;
        }
        return starToLabelLookup.get(starId);
    }

    /**
     * once we have all the labels, we create a sub list that has the stars that we allow labels to show
     *
     * @param labelCount the user supplied count of labels to show
     */
    public void determineVisibleLabels(int labelCount) {
        if (labelCount > starDisplayRecordList.size()) {
            labelCount = starDisplayRecordList.size();
        }

        for (StarDisplayRecord record : starDisplayRecordList) {
            record.setDisplayLabel(false);
        }

        // sort the list in order, duplicates are keep
        starDisplayRecordList.sort(Comparator.comparing(StarDisplayRecord::getCurrentLabelDisplayScore).reversed());

        // create a check set for the user count
        for (int i = 0; i < labelCount; i++) {
            StarDisplayRecord starDisplayRecord = starDisplayRecordList.get(i);
            starDisplayRecord.setDisplayLabel(true);
        }
    }

    // =========================================================================
    // Spatial Index Methods
    // =========================================================================

    /**
     * Gets the spatial index, building it if necessary.
     * <p>
     * The spatial index enables efficient queries like:
     * <ul>
     *   <li>Find stars within a viewport distance</li>
     *   <li>Find nearest stars to a point</li>
     *   <li>Find top-scoring stars within a radius for label display</li>
     * </ul>
     *
     * @return the spatial index, or null if no stars are loaded
     */
    public @Nullable VisualizationSpatialIndex getSpatialIndex() {
        if (starDisplayRecordList.isEmpty()) {
            return null;
        }

        if (spatialIndex == null || spatialIndexDirty) {
            rebuildSpatialIndex();
        }

        return spatialIndex;
    }

    /**
     * Rebuilds the spatial index from the current star list.
     * <p>
     * Call this after bulk modifications to the star list.
     * The index is automatically rebuilt when accessed if dirty.
     */
    @TrackExecutionTime
    public void rebuildSpatialIndex() {
        if (!starDisplayRecordList.isEmpty()) {
            log.debug("Building spatial index for {} stars", starDisplayRecordList.size());
            spatialIndex = new VisualizationSpatialIndex(starDisplayRecordList);
            spatialIndexDirty = false;
        } else {
            spatialIndex = null;
            spatialIndexDirty = false;
        }
    }

    /**
     * Marks the spatial index as needing rebuild.
     * <p>
     * Call this when stars are added or removed.
     */
    public void invalidateSpatialIndex() {
        spatialIndexDirty = true;
    }

    /**
     * Finds stars within the specified radius of the center coordinates.
     * <p>
     * Uses the spatial index for O(log n + k) performance if available,
     * otherwise falls back to linear search.
     *
     * @param radius the search radius in light-years
     * @return stars within the radius, or all stars if center not set
     */
    public @NotNull List<StarDisplayRecord> getStarsWithinRadius(double radius) {
        if (centerCoordinates == null || centerCoordinates.length < 3) {
            return new ArrayList<>(starDisplayRecordList);
        }

        VisualizationSpatialIndex index = getSpatialIndex();
        if (index != null) {
            return index.findStarsWithinRadius(
                    centerCoordinates[0],
                    centerCoordinates[1],
                    centerCoordinates[2],
                    radius);
        }

        return new ArrayList<>(starDisplayRecordList);
    }

    /**
     * Finds stars within the specified distance range of the center.
     *
     * @param minRadius minimum distance (exclusive)
     * @param maxRadius maximum distance (inclusive)
     * @return stars within the distance range
     */
    public @NotNull List<StarDisplayRecord> getStarsInRange(double minRadius, double maxRadius) {
        if (centerCoordinates == null || centerCoordinates.length < 3) {
            return new ArrayList<>(starDisplayRecordList);
        }

        VisualizationSpatialIndex index = getSpatialIndex();
        if (index != null) {
            return index.findStarsInRange(
                    centerCoordinates[0],
                    centerCoordinates[1],
                    centerCoordinates[2],
                    minRadius,
                    maxRadius);
        }

        return new ArrayList<>(starDisplayRecordList);
    }

    /**
     * Determines which stars should display labels using spatial optimization.
     * <p>
     * This is more efficient than the full sort approach when dealing with
     * a large number of stars and a small label count.
     *
     * @param labelCount the maximum number of labels to display
     * @param radius     the search radius (use plot range or viewport distance)
     */
    @TrackExecutionTime
    public void determineVisibleLabelsWithSpatialIndex(int labelCount, double radius) {
        if (centerCoordinates == null || centerCoordinates.length < 3) {
            // Fall back to non-spatial method
            determineVisibleLabels(labelCount);
            return;
        }

        VisualizationSpatialIndex index = getSpatialIndex();
        if (index == null) {
            determineVisibleLabels(labelCount);
            return;
        }

        // Reset all labels
        for (StarDisplayRecord star : starDisplayRecordList) {
            star.setDisplayLabel(false);
        }

        // Use spatial index to find stars that should have labels
        List<StarDisplayRecord> starsForLabeling = index.findStarsForLabeling(
                centerCoordinates[0],
                centerCoordinates[1],
                centerCoordinates[2],
                radius,
                labelCount);

        for (StarDisplayRecord star : starsForLabeling) {
            star.setDisplayLabel(true);
        }

        log.debug("Determined {} labels using spatial index (within {} ly radius)",
                starsForLabeling.size(), radius);
    }

    // =========================================================================
    // Distance-Sorted Access (for LOD optimization)
    // =========================================================================

    /**
     * Returns stars sorted by distance from the plot center.
     * <p>
     * This method pre-computes and caches the distance for each star,
     * enabling efficient LOD determination without redundant calculations.
     * Stars closer to the center are returned first.
     *
     * @return list of stars sorted by distance from center (nearest first)
     */
    @TrackExecutionTime
    public @NotNull List<StarDisplayRecord> getStarsSortedByDistance() {
        if (centerCoordinates == null || centerCoordinates.length < 3) {
            log.warn("Center coordinates not set, returning unsorted list");
            return new ArrayList<>(starDisplayRecordList);
        }

        double cx = centerCoordinates[0];
        double cy = centerCoordinates[1];
        double cz = centerCoordinates[2];

        // Pre-compute distances for all stars
        for (StarDisplayRecord star : starDisplayRecordList) {
            star.computeAndCacheDistanceFromCenter(cx, cy, cz);
        }

        // Sort by cached distance
        List<StarDisplayRecord> sorted = new ArrayList<>(starDisplayRecordList);
        sorted.sort(Comparator.comparingDouble(StarDisplayRecord::getDistanceFromPlotCenter));

        log.debug("Sorted {} stars by distance from center", sorted.size());
        return sorted;
    }

    /**
     * Returns stars within radius, sorted by distance from center.
     * <p>
     * Combines spatial filtering with distance sorting for optimal LOD rendering.
     *
     * @param radius maximum distance from center
     * @return filtered and sorted list of stars
     */
    @TrackExecutionTime
    public @NotNull List<StarDisplayRecord> getStarsWithinRadiusSorted(double radius) {
        if (centerCoordinates == null || centerCoordinates.length < 3) {
            return getStarsSortedByDistance();
        }

        double cx = centerCoordinates[0];
        double cy = centerCoordinates[1];
        double cz = centerCoordinates[2];

        // Use spatial index for efficient filtering, then sort
        List<StarDisplayRecord> withinRadius = getStarsWithinRadius(radius);

        // Pre-compute distances for filtered stars
        for (StarDisplayRecord star : withinRadius) {
            star.computeAndCacheDistanceFromCenter(cx, cy, cz);
        }

        // Sort by cached distance
        withinRadius.sort(Comparator.comparingDouble(StarDisplayRecord::getDistanceFromPlotCenter));

        return withinRadius;
    }

    // =========================================================================
    // Route Spatial Index Methods
    // =========================================================================

    /**
     * Gets the route spatial index, building it if necessary.
     * <p>
     * The route spatial index enables efficient queries like:
     * <ul>
     *   <li>Find route segments within a viewport distance</li>
     *   <li>Cull route segments that are completely outside the view</li>
     *   <li>Find which routes have visible segments</li>
     * </ul>
     *
     * @return the route spatial index, or null if no routes are loaded
     */
    public @Nullable RouteSegmentSpatialIndex getRouteSpatialIndex() {
        if (routeMapping.isEmpty()) {
            return null;
        }

        if (routeSpatialIndex == null || routeSpatialIndexDirty) {
            rebuildRouteSpatialIndex();
        }

        return routeSpatialIndex;
    }

    /**
     * Rebuilds the route spatial index from the current routes.
     * <p>
     * Call this after bulk modifications to routes.
     * The index is automatically rebuilt when accessed if dirty.
     */
    @TrackExecutionTime
    public void rebuildRouteSpatialIndex() {
        if (!routeMapping.isEmpty()) {
            int totalSegments = routeMapping.values().stream()
                    .mapToInt(r -> Math.max(0, r.getRouteCoordinates().size() - 1))
                    .sum();
            log.debug("Building route spatial index for {} routes ({} segments)",
                    routeMapping.size(), totalSegments);

            routeSpatialIndex = new RouteSegmentSpatialIndex();
            routeSpatialIndex.addRoutes(routeMapping.values());
            routeSpatialIndexDirty = false;
        } else {
            routeSpatialIndex = null;
            routeSpatialIndexDirty = false;
        }
    }

    /**
     * Marks the route spatial index as needing rebuild.
     * <p>
     * Call this when routes are added or removed.
     */
    public void invalidateRouteSpatialIndex() {
        routeSpatialIndexDirty = true;
    }

    /**
     * Finds route segments within the specified radius of the center coordinates.
     * <p>
     * Uses the spatial index for O(log n + k) performance if available.
     *
     * @param radius the search radius
     * @return segments within the radius, or empty list if no routes
     */
    public @NotNull List<IndexedRouteSegment> getRouteSegmentsWithinRadius(double radius) {
        if (centerCoordinates == null || centerCoordinates.length < 3) {
            return Collections.emptyList();
        }

        RouteSegmentSpatialIndex index = getRouteSpatialIndex();
        if (index != null) {
            return index.findSegmentsWithinRadius(
                    centerCoordinates[0],
                    centerCoordinates[1],
                    centerCoordinates[2],
                    radius);
        }

        return Collections.emptyList();
    }

    /**
     * Gets the set of route IDs that have segments within the viewport.
     * <p>
     * This is useful for determining which routes need to be rendered
     * without checking every segment.
     *
     * @param radius the viewport radius
     * @return set of route IDs with visible segments
     */
    public @NotNull Set<UUID> getVisibleRouteIds(double radius) {
        if (centerCoordinates == null || centerCoordinates.length < 3) {
            return new HashSet<>(routeMapping.keySet());
        }

        RouteSegmentSpatialIndex index = getRouteSpatialIndex();
        if (index != null) {
            return index.getVisibleRouteIds(
                    centerCoordinates[0],
                    centerCoordinates[1],
                    centerCoordinates[2],
                    radius);
        }

        return new HashSet<>(routeMapping.keySet());
    }

    /**
     * Returns route spatial index statistics for monitoring.
     *
     * @return statistics string, or empty string if no index
     */
    public @NotNull String getRouteSpatialIndexStatistics() {
        if (routeSpatialIndex != null) {
            return routeSpatialIndex.getStatistics();
        }
        return "";
    }

}
