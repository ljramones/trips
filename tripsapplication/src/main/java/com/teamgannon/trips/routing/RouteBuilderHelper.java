package com.teamgannon.trips.routing;

import com.teamgannon.trips.algorithms.StarMath;
import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Slf4j
public class RouteBuilderHelper {

    /**
     * our lookup
     */
    private final Map<String, StarDisplayRecord> starDisplayRecordMap = new HashMap<>();

    /**
     * the ctor
     *
     * @param starDisplayRecordList the stars in the plot
     */
    public RouteBuilderHelper(@NotNull List<StarDisplayRecord> starDisplayRecordList) {
        starDisplayRecordList.stream().filter(Objects::nonNull).forEach(starDisplayRecord -> starDisplayRecordMap.put(starDisplayRecord.getStarName(), starDisplayRecord));
        log.info("done");
    }

    /**
     * build a route from the graph
     *
     * @param source      the start star
     * @param destination the end star
     * @param pathName    the name of this path
     * @param color       the color
     * @param lineWidth   the route line width
     * @param path        the graph path
     * @return the created route
     */
    public RouteDescriptor buildPath(String source, String destination, String pathName, Color color, double lineWidth, @NotNull String path) {
        RouteDescriptor route = RouteDescriptor.builder().build();
        String extractPath = path.substring(1, path.length() - 1);
        String[] starList = extractPath.split(",");

        // set route info
        route.setName(String.format("Route %s to %s, path %s", source, destination, pathName));
        route.setRouteNotes(path);
        route.setColor(color);
        route.setMaxLength(starList.length - 1);
        route.setStartStar(starList[0]);
        StarDisplayRecord lastStar = starDisplayRecordMap.get(starList[starList.length - 1].trim());
        route.setLastStar(lastStar);
        route.setLineWidth(lineWidth);

        Arrays.stream(starList).forEach(name -> {
            route.getNameList().add(name.trim());
            StarDisplayRecord starDisplayRecord = starDisplayRecordMap.get(name.trim());
            route.getRouteList().add(starDisplayRecord.getRecordId());
        });


        // set segments

        double totalLength = 0;
        boolean first = true;
        double[] coordinates1 = new double[3];
        for (String star : starList) {
            StarDisplayRecord starDisplayRecord = starDisplayRecordMap.get(star.trim());
            if (first) {
                first = false;
                coordinates1 = starDisplayRecord.getActualCoordinates();
            } else {
                double[] coordinates2 = starDisplayRecord.getActualCoordinates();
                double lengthValue = StarMath.getDistance(coordinates1, coordinates2);
                totalLength += lengthValue;
                coordinates1 = coordinates2;
                route.addLengthSegment(lengthValue);
            }
            route.addLineSegment(starDisplayRecord.getCoordinates());

        }
        route.setTotalLength(totalLength);

        return route;
    }

    /**
     * is this star in the plot?
     *
     * @param star the star to check
     * @return true if it is there
     */
    public boolean has(String star) {
        return starDisplayRecordMap.containsKey(star);
    }
}
