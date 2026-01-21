package com.teamgannon.trips.routing.routemanagement;

import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.RouteVisibility;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.routing.model.Route;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for handling partial route visualization.
 * <p>
 * When a complete route has some stars that are not visible in the current view,
 * this class extracts and displays only the visible segments.
 */
@Slf4j
public class PartialRouteUtils {

    private final TripsContext tripsContext;
    private final RouteDisplay routeDisplay;
    private final RouteGraphicsUtil routeGraphicsUtil;
    private final RouteBuilderUtils routeBuilderUtils;

    public PartialRouteUtils(TripsContext tripsContext,
                             RouteDisplay routeDisplay,
                             RouteGraphicsUtil routeGraphicsUtil,
                             RouteBuilderUtils routeBuilderUtils) {
        this.tripsContext = tripsContext;
        this.routeDisplay = routeDisplay;
        this.routeGraphicsUtil = routeGraphicsUtil;
        this.routeBuilderUtils = routeBuilderUtils;
    }

    /**
     * Find and display partial routes for a given route.
     *
     * @param route the complete route definition
     */
    public void findPartialRoutes(@NotNull Route route) {
        List<RouteDescriptor> partialRoutes = getPartialRoutes(route);
        if (!partialRoutes.isEmpty()) {
            log.info("number of partial routes:{}", partialRoutes.size());
            Group partialRouteGraphic = createPartialRoutes(partialRoutes);
            addPartialRoutes(partialRoutes, partialRouteGraphic);
            routeDisplay.toggleRouteVisibility(true);

            // add the partial routes to the current display
            tripsContext.getCurrentPlot().addRoutes(route.getUuid(), partialRoutes);
        } else {
            log.info("!!!!!route {} is not visible in this view port", route.getRouteName());
        }
    }

    /**
     * Create a graphics group with each partial route in it.
     *
     * @param partialRoutes the list of partial routes
     * @return the group with the partial routes
     */
    private @NotNull Group createPartialRoutes(List<RouteDescriptor> partialRoutes) {
        Group route = new Group();
        boolean firstLink = true;

        for (RouteDescriptor routeDescriptor : partialRoutes) {
            int lengthIndex = 0;
            Point3D previousPoint = new Point3D(0, 0, 0);
            List<Double> lengthList = routeDescriptor.getLengthList();

            for (Point3D point3D : routeDescriptor.getRouteCoordinates()) {
                if (firstLink) {
                    previousPoint = point3D;
                    firstLink = false;
                } else {
                    // Safely get length with bounds check
                    double length = safeGetLength(lengthList, lengthIndex++);
                    Label lengthLabel = routeGraphicsUtil.createLabel(firstLink, length);
                    Node lineSegment = routeGraphicsUtil.createLineSegment(
                            previousPoint, point3D,
                            routeDescriptor.getLineWidth(),
                            routeDescriptor.getColor(),
                            lengthLabel);
                    previousPoint = point3D;
                    route.getChildren().add(lineSegment);
                }
            }
        }
        return route;
    }

    /**
     * Add the partial routes to the display.
     *
     * @param partialRoutes       the set of partial routes
     * @param partialRouteGraphic the set of line segments defining this route
     */
    private void addPartialRoutes(List<RouteDescriptor> partialRoutes, Group partialRouteGraphic) {
        for (RouteDescriptor routeDescriptor : partialRoutes) {
            routeDisplay.addRouteToDisplay(routeDescriptor, partialRouteGraphic);
        }
    }

    /**
     * Get all the partial routes from a complete route.
     * Extracts only segments where both endpoints are visible in the current view.
     *
     * @param route the complete route
     * @return the list of partial routes (may be empty if no stars are visible)
     */
    private List<RouteDescriptor> getPartialRoutes(@NotNull Route route) {
        List<RouteDescriptor> routeDescriptorList = new ArrayList<>();

        Set<String> visibleStarIds = findVisibleStarIds(route);

        if (visibleStarIds.isEmpty()) {
            return routeDescriptorList;
        }

        RouteDescriptor routeDescriptor = createBaseRouteDescriptor(route);
        extractVisibleSegments(route, routeDescriptor, visibleStarIds, routeDescriptorList);
        calculateMaxLengths(routeDescriptorList);

        return routeDescriptorList;
    }

    /**
     * Find which stars from the route are visible in the current plot.
     */
    private Set<String> findVisibleStarIds(@NotNull Route route) {
        return route.getRouteStars()
                .stream()
                .filter(starId -> tripsContext.getCurrentPlot()
                        .getStarLookup()
                        .containsKey(starId))
                .collect(Collectors.toSet());
    }

    /**
     * Create a base RouteDescriptor with common properties from the route.
     */
    private RouteDescriptor createBaseRouteDescriptor(@NotNull Route route) {
        RouteDescriptor descriptor = RouteDescriptor.builder().build();
        descriptor.setId(route.getUuid());
        descriptor.setName(route.getRouteName());
        descriptor.setLineWidth(route.getLineWidth());
        descriptor.setRouteNotes(route.getRouteNotes());
        descriptor.setColor(Color.valueOf(route.getRouteColor()));
        descriptor.setVisibility(RouteVisibility.PARTIAL);
        return descriptor;
    }

    /**
     * Extract visible route segments from the complete route.
     */
    private void extractVisibleSegments(@NotNull Route route,
                                         RouteDescriptor routeDescriptor,
                                         Set<String> visibleStarIds,
                                         List<RouteDescriptor> routeDescriptorList) {
        List<String> routeStars = route.getRouteStars();
        boolean newRoute = true;
        boolean routeInProgress = false;

        int i = 0;
        while (i < routeStars.size()) {
            String currentStarId = routeStars.get(i);

            if (!visibleStarIds.contains(currentStarId)) {
                // Current star not visible - break any route in progress
                if (routeInProgress) {
                    newRoute = false;
                }
                i++;
                continue;
            }

            // Skip if this is the last star (no segment to form)
            if (i == routeStars.size() - 1) {
                i++;
                continue;
            }

            String nextStarId = routeStars.get(i + 1);
            boolean nextStarVisible = visibleStarIds.contains(nextStarId);

            if (nextStarVisible) {
                if (newRoute) {
                    // Start a new partial route with first two stars
                    if (initializePartialRoute(route, routeDescriptor, routeStars, i)) {
                        routeDescriptorList.add(routeDescriptor);
                        routeInProgress = true;
                        i++; // Skip next star since we added it
                    } else {
                        return; // Corrupt data, abort
                    }
                    newRoute = false;
                } else {
                    // Continue existing route
                    addStarToRoute(route, routeDescriptor, routeStars, i);
                }
            } else {
                // Next star not visible - close current segment if in progress
                if (routeInProgress) {
                    addStarToRoute(route, routeDescriptor, routeStars, i);
                    routeInProgress = false;
                }
                newRoute = false;
            }
            i++;
        }
    }

    /**
     * Initialize a partial route with the first two visible stars.
     *
     * @return true if successful, false if data is corrupt
     */
    private boolean initializePartialRoute(@NotNull Route route,
                                            @NotNull RouteDescriptor routeDescriptor,
                                            List<String> routeStars,
                                            int index) {
        routeDescriptor.setDescriptor(tripsContext.getDataSetContext().getDescriptor());

        StarDisplayRecord firstStar = routeBuilderUtils.getStar(routeStars.get(index));
        StarDisplayRecord nextStar = routeBuilderUtils.getStar(routeStars.get(index + 1));

        if (firstStar == null || nextStar == null) {
            log.error("Star not found at index {}, plot may be corrupt", index);
            return false;
        }

        Point3D firstCoords = firstStar.getCoordinates();
        Point3D nextCoords = nextStar.getCoordinates();

        if (firstCoords == null) {
            log.error("Coordinates empty for star: {}, segment is corrupt", firstStar.getStarName());
            return false;
        }
        if (nextCoords == null) {
            log.error("Coordinates empty for star: {}, segment is corrupt", nextStar.getStarName());
            return false;
        }

        // Set start star
        routeDescriptor.setStartStar(firstStar.getStarName());

        // Add coordinates
        routeDescriptor.getRouteCoordinates().add(firstCoords);
        routeDescriptor.getRouteCoordinates().add(nextCoords);

        // Add star IDs
        routeDescriptor.getRouteList().add(routeStars.get(index));
        routeDescriptor.getRouteList().add(routeStars.get(index + 1));

        // Add star records
        routeDescriptor.getStarDisplayRecords().add(firstStar);
        routeDescriptor.getStarDisplayRecords().add(nextStar);

        // Add names with bounds check
        addNameSafely(routeDescriptor, route.getRouteStarNames(), index);
        addNameSafely(routeDescriptor, route.getRouteStarNames(), index + 1);

        // Add lengths with bounds check
        addLengthSafely(routeDescriptor, route.getRouteLengths(), index);
        addLengthSafely(routeDescriptor, route.getRouteLengths(), index + 1);

        log.info("Added first two stars to partial route");
        return true;
    }

    /**
     * Add a star to an existing partial route.
     */
    private void addStarToRoute(@NotNull Route route,
                                 @NotNull RouteDescriptor routeDescriptor,
                                 @NotNull List<String> routeStars,
                                 int index) {
        String starId = routeStars.get(index);
        routeDescriptor.getRouteList().add(starId);

        StarDisplayRecord star = routeBuilderUtils.getStar(starId);
        if (star != null) {
            Point3D coordinates = star.getCoordinates();
            if (coordinates != null) {
                routeDescriptor.getRouteCoordinates().add(coordinates);
            } else {
                log.error("Coordinates empty for star: {}", star.getStarName());
            }
            routeDescriptor.getStarDisplayRecords().add(star);
            routeDescriptor.setLastStar(star);
        } else {
            log.error("Star <{}> not found", starId);
        }

        addNameSafely(routeDescriptor, route.getRouteStarNames(), index);
        addLengthSafely(routeDescriptor, route.getRouteLengths(), index);
    }

    /**
     * Calculate max lengths for all partial routes.
     */
    private void calculateMaxLengths(List<RouteDescriptor> routeDescriptorList) {
        for (RouteDescriptor descriptor : routeDescriptorList) {
            double maxLength = descriptor.getLengthList().stream()
                    .mapToDouble(Double::doubleValue)
                    .filter(length -> length >= 0)
                    .max()
                    .orElse(0);
            descriptor.setMaxLength(maxLength);

            String nameList = String.join(", ", descriptor.getNameList());
            log.info("*>> {} stars in route {} is {} ly and is composed of {}",
                    descriptor.getNameList().size(), descriptor.getName(), maxLength, nameList);
        }
    }

    // =========================================================================
    // Safe accessor helpers
    // =========================================================================

    /**
     * Safely get a length from the list with bounds checking.
     */
    private double safeGetLength(@Nullable List<Double> lengths, int index) {
        if (lengths == null || index < 0 || index >= lengths.size()) {
            return 0.0;
        }
        Double value = lengths.get(index);
        return value != null ? value : 0.0;
    }

    /**
     * Safely add a name to the route descriptor with bounds checking.
     */
    private void addNameSafely(@NotNull RouteDescriptor descriptor,
                                @Nullable List<String> names,
                                int index) {
        if (names != null && index >= 0 && index < names.size()) {
            String name = names.get(index);
            if (name != null) {
                descriptor.getNameList().add(name);
            }
        }
    }

    /**
     * Safely add a length to the route descriptor with bounds checking.
     */
    private void addLengthSafely(@NotNull RouteDescriptor descriptor,
                                  @Nullable List<Double> lengths,
                                  int index) {
        if (lengths != null && index >= 0 && index < lengths.size()) {
            Double length = lengths.get(index);
            descriptor.getLengthList().add(length != null ? length : 0.0);
        }
    }
}
