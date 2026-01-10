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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
     * create a graphics group with each partial route in it.
     *
     * @param partialRoutes the list of partial routes
     * @return the group with the partial routes
     */
    private @NotNull Group createPartialRoutes(List<RouteDescriptor> partialRoutes) {
        Group route = new Group();
        boolean firstLink = true;

        // for each partial route
        for (RouteDescriptor routeDescriptor : partialRoutes) {
            int i = 0;
            Point3D previousPoint = new Point3D(0, 0, 0);
            for (Point3D point3D : routeDescriptor.getRouteCoordinates()) {
                if (firstLink) {
                    previousPoint = point3D;
                    firstLink = false;
                } else {
                    double length = routeDescriptor.getLengthList().get(i++);
                    Label lengthLabel = routeGraphicsUtil.createLabel(firstLink, length);
                    // create the line segment
                    Node lineSegment = routeGraphicsUtil.createLineSegment(previousPoint, point3D, routeDescriptor.getLineWidth(), routeDescriptor.getColor(), lengthLabel);
                    // step along the segment
                    previousPoint = point3D;

                    // add the completed line segment to overall list
                    route.getChildren().add(lineSegment);
                }

            }
        }
        return route;
    }


    /**
     * add the partial routes (which are really one route
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
     * get all the partial routes
     *
     * @param route the complete route
     * @return the lsit of partial routes
     */
    private List<RouteDescriptor> getPartialRoutes(@NotNull Route route) {
        List<RouteDescriptor> routeDescriptorList = new ArrayList<>();
        Set<String> visibleStarList = route.getRouteStars()
                .stream()
                .filter(starId -> tripsContext.getCurrentPlot()
                        .getStarLookup()
                        .containsKey(starId)).collect(Collectors.toSet());

        // creating this here is mostly to get around the stupid compile warnings
        RouteDescriptor routeDescriptor = RouteDescriptor.builder().build();
        routeDescriptor.setId(route.getUuid());
        routeDescriptor.setName(route.getRouteName());
        routeDescriptor.setLineWidth(route.getLineWidth());
        routeDescriptor.setRouteNotes(route.getRouteNotes());
        routeDescriptor.setColor(Color.valueOf(route.getRouteColor()));

        // this is the case where none of the stars in this route are visible
        if (visibleStarList.isEmpty()) {
            routeDescriptor.setVisibility(RouteVisibility.OFFSCREEN);
        } else {
            routeDescriptor.setVisibility(RouteVisibility.PARTIAL);
            // need to reset in the loop or a new loop can't start after we mark the first as complete
            boolean newRoute = true;
            boolean routeInProgress = false;
            List<String> routeStars = route.getRouteStars();
            int i = 0;
            while (i < routeStars.size()) {
                String starToMatch = routeStars.get(i);
                if (visibleStarList.contains(starToMatch)) {
                    if (i == (routeStars.size() - 1)) {
                        // if this is the last star then skip since there is no segment to join with this one
                        i++;
                        continue; // should be end of loop
                    }
                    // get the next star in series and see if its there
                    String nextStar = routeStars.get(i + 1);
                    if (visibleStarList.contains(nextStar)) {
                        // ok these stars form a segment, so let copy them
                        if (newRoute) {
                            // beginning of route is always here for first star and its pair
                            routeDescriptor.setDescriptor(tripsContext.getDataSetContext().getDescriptor());

                            StarDisplayRecord firstStarDisplayRecord = routeBuilderUtils.getStar(routeStars.get(i));
                            StarDisplayRecord nextStarDisplayRecord = routeBuilderUtils.getStar(routeStars.get(i + 1));
                            if (firstStarDisplayRecord != null && nextStarDisplayRecord != null) {
                                // all of this is supposed to set up the start of a route
                                // first/start star
                                routeDescriptor.setStartStar(firstStarDisplayRecord.getStarName());
                                Point3D firstStarCoordinates = firstStarDisplayRecord.getCoordinates();
                                if (firstStarCoordinates != null) {
                                    routeDescriptor.getRouteCoordinates().add(firstStarCoordinates);
                                } else {
                                    log.error("why are the coordinates empty for this star: {}, " +
                                                    "no point in trying to draw as this is a corrupt segment",
                                            firstStarDisplayRecord.getStarName());
                                    return routeDescriptorList;
                                }

                                Point3D nextStarCoordinates = nextStarDisplayRecord.getCoordinates();
                                if (nextStarCoordinates != null) {
                                    routeDescriptor.getRouteCoordinates().add(nextStarCoordinates);
                                } else {
                                    log.error("why are the coordinates empty for this star: {}, " +
                                                    "no point in trying to draw as this is a corrupt segment",
                                            nextStarDisplayRecord.getStarName());
                                    return routeDescriptorList;
                                }

                                // add route stars and its next segment
                                routeDescriptor.getRouteList().add(routeStars.get(i));
                                routeDescriptor.getRouteList().add(routeStars.get(i + 1));
                                routeDescriptor.setVisibility(RouteVisibility.PARTIAL);
                                // add star records
                                routeDescriptor.getStarDisplayRecords().add(routeBuilderUtils.getStar(routeStars.get(i)));
                                routeDescriptor.getStarDisplayRecords().add(routeBuilderUtils.getStar(routeStars.get(i + 1)));
                                // add names
                                routeDescriptor.getNameList().add(route.getRouteStarNames().get(i));
                                routeDescriptor.getNameList().add(route.getRouteStarNames().get(i + 1));
                                // add length list
                                routeDescriptor.getLengthList().add(route.getRouteLengths().get(i));
                                routeDescriptor.getLengthList().add(route.getRouteLengths().get(i + 1));

                                // add to the list, note this is only part of it but we have to start the addition
                                routeDescriptorList.add(routeDescriptor);

                                // since we add two starts here, advance the counter by 1 to take this into account
                                i++;

                                // set route in progress
                                routeInProgress = true;
                                log.info("Added first two stars");
                            } else {
                                log.error("Why is this star <{}> not found, it should be there, plot is corrupt!!", routeStars.get(i));
                                return routeDescriptorList;
                            }
                            newRoute = false;
                        } else {
                            // rest of stars in the route clock in here
                            saveStarInRoute(route, routeDescriptor, routeStars, i);
                        }
                    } else {
                        // break partial route
                        if (routeInProgress) {
                            saveStarInRoute(route, routeDescriptor, routeStars, i);
                            routeInProgress = false;
                        }

                        newRoute = false;
                    }
                } else {
                    // break partial route
                    if (routeInProgress) {
                        newRoute = false;
                    }
                }
                i++;
            }
            // fix up the partial routes
            for (RouteDescriptor descriptor : routeDescriptorList) {
                double maxLength = descriptor.getLengthList().stream().mapToDouble(length -> length).filter(length -> length >= 0).max().orElse(0);
                descriptor.setMaxLength(maxLength);
                String nameList = descriptor.getNameList().stream().map(name -> name + ", ").collect(Collectors.joining());
                log.info("*>> {} stars in route {} is {} ly and is composed of {}", descriptor.getNameList().size(), descriptor.getName(), maxLength, nameList);
            }
        }

        // return list
        return routeDescriptorList;
    }

    /**
     * save a route star in the route with it values
     *
     * @param route           the route
     * @param routeDescriptor the new route descriptor
     * @param routeStars      the route stars in the entire route
     * @param i               the current position
     */
    private void saveStarInRoute(Route route, @NotNull RouteDescriptor routeDescriptor, @NotNull List<String> routeStars, int i) {

        routeDescriptor.getRouteList().add(routeStars.get(i));
        StarDisplayRecord starDisplayRecord = routeBuilderUtils.getStar(routeStars.get(i));
        if (starDisplayRecord != null) {
            Point3D coordinates = starDisplayRecord.getCoordinates();
            if (coordinates != null) {
                routeDescriptor.getRouteCoordinates().add(coordinates);
            } else {
                log.error("why are the coordinates empty for this star: {}", starDisplayRecord.getStarName());
            }
        } else {
            log.error("Why is this star <{}> not found, it should be there", routeStars.get(i));
        }
        routeDescriptor.getNameList().add(route.getRouteStarNames().get(i));
        routeDescriptor.getLengthList().add(route.getRouteLengths().get(i));
        routeDescriptor.getStarDisplayRecords().add(routeBuilderUtils.getStar(routeStars.get(i)));
        // we get doing this so that we catch the last star
        routeDescriptor.setLastStar(routeBuilderUtils.getStar(routeStars.get(i)));
    }

}
