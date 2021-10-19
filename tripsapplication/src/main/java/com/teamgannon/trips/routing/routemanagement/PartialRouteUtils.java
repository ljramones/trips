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
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class PartialRouteUtils {

    private TripsContext tripsContext;
    private RouteDisplay routeDisplay;
    private RouteGraphicsUtil routeGraphicsUtil;
    private RouteBuilderUtils routeBuilderUtils;

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
    private Group createPartialRoutes(List<RouteDescriptor> partialRoutes) {
        Group route = new Group();
        boolean firstLink = true;

        // for each partial route
        for (RouteDescriptor routeDescriptor : partialRoutes) {
            int i = 0;
            Point3D previousPoint = new Point3D(0, 0, 0);
            for (Point3D point3D : routeDescriptor.getLineSegments()) {
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
    private List<RouteDescriptor> getPartialRoutes(Route route) {
        List<RouteDescriptor> routeDescriptorList = new ArrayList<>();
        Set<UUID> visibleStarList = route.getRouteStars()
                .stream()
                .filter(starId -> tripsContext.getCurrentPlot()
                        .getStarLookup()
                        .containsKey(starId)).collect(Collectors.toSet());


        // creating this here is mostly to get around the stupid compile warnings
        RouteDescriptor routeDescriptor = RouteDescriptor.builder().build();

        // this is the case where none of the stars in this route are visible
        if (visibleStarList.isEmpty()) {
            routeDescriptor.setName(route.getRouteName());
            routeDescriptor.setVisibility(RouteVisibility.INVISIBLE);
            routeDescriptor.setLineWidth(route.getLineWidth());
            routeDescriptor.setRouteNotes(route.getRouteNotes());
        } else {

            // need to reset in the loop or a new loop can't start after we mark the first as complete
            boolean newRoute = true;
            List<UUID> routeStars = route.getRouteStars();
            for (int i = 0; i < routeStars.size(); i++) {
                UUID starToMatch = routeStars.get(i);
                if (visibleStarList.contains(starToMatch)) {
                    if (i == (routeStars.size() - 1)) {
                        // if this is the last star then skip since there is no segment to join with this one
                        continue; // should be end of loop
                    }
                    // get the next star in series and see if its there
                    UUID nextStar = routeStars.get(i + 1);
                    if (visibleStarList.contains(nextStar)) {
                        // ok these stars form a segment, so let copy them
                        if (newRoute) {
                            routeDescriptor.setDescriptor(tripsContext.getDataSetContext().getDescriptor());

                            // @todo the route name isn't being set properly when created
                            routeDescriptor.setName(route.getRouteName());
                            routeDescriptor.setStartStar(route.getRouteStarNames().get(i));
                            routeDescriptor.setLineWidth(route.getLineWidth());
                            routeDescriptor.setRouteNotes(route.getRouteNotes());

                            StarDisplayRecord starDisplayRecord = routeBuilderUtils.getStar(routeStars.get(i));
                            if (starDisplayRecord != null) {
                                Point3D coordinates = starDisplayRecord.getCoordinates();
                                if (coordinates != null) {
                                    routeDescriptor.getLineSegments().add(coordinates);
                                } else {
                                    log.error("why are the coordinates empty for this star: {}", starDisplayRecord.getStarName());
                                }

                                // add route stars
                                routeDescriptor.getRouteList().add(routeStars.get(i));
                                routeDescriptor.getRouteList().add(routeStars.get(i + 1));
                                // add star records
                                routeDescriptor.getStarDisplayRecords().add(routeBuilderUtils.getStar(routeStars.get(i)));
                                routeDescriptor.getStarDisplayRecords().add(routeBuilderUtils.getStar(routeStars.get(i + 1)));
                                // add names
                                routeDescriptor.getNameList().add(route.getRouteStarNames().get(i));
                                routeDescriptor.getNameList().add(route.getRouteStarNames().get(i + 1));
                                // add length list
                                routeDescriptor.getLengthList().add(route.getRouteLengths().get(i));
                                routeDescriptor.getLengthList().add(route.getRouteLengths().get(i + 1));

                                routeDescriptor.setVisibility(RouteVisibility.PARTIAL);

                                // add to the list
                                routeDescriptorList.add(routeDescriptor);
                            } else {
                                log.error("Why is this star <{}> not found, it should be there", routeStars.get(i));
                            }
                            newRoute = false;
                        } else {
                            routeDescriptor.getRouteList().add(routeStars.get(i + 1));
                            StarDisplayRecord starDisplayRecord = routeBuilderUtils.getStar(routeStars.get(i + 1));
                            if (starDisplayRecord != null) {
                                Point3D coordinates = starDisplayRecord.getCoordinates();
                                if (coordinates != null) {
                                    routeDescriptor.getLineSegments().add(coordinates);
                                } else {
                                    log.error("why are the coordinates empty for this star: {}", starDisplayRecord.getStarName());
                                }
                            } else {
                                log.error("Why is this star <{}> not found, it should be there", routeStars.get(i));
                            }
                            routeDescriptor.getNameList().add(route.getRouteStarNames().get(i + 1));
                            routeDescriptor.getLengthList().add(route.getRouteLengths().get(i + 1));
                            routeDescriptor.getStarDisplayRecords().add(routeBuilderUtils.getStar(routeStars.get(i + 1)));
                            // we get doing this so that we catch the last star
                            routeDescriptor.setLastStar(routeBuilderUtils.getStar(routeStars.get(i + 1)));
                        }
                    } else {
                        // break partial route
                        newRoute = false;
                    }
                } else {
                    // break partial route
                    newRoute = false;
                }
            }
            // fix up the partial routes
            for (RouteDescriptor descriptor : routeDescriptorList) {
                double maxLength = descriptor.getLengthList().stream().mapToDouble(length -> length).filter(length -> length >= 0).max().orElse(0);
                descriptor.setMaxLength(maxLength);
                log.info("*>> {} stars in route {}", descriptor.getNameList().size(), descriptor.getName());
            }
        }

        // return list
        return routeDescriptorList;
    }

}
