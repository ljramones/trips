package com.teamgannon.trips.routing;

import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class RouteBuilderHelper {

    private final Map<String, StarDisplayRecord> starDisplayRecordMap = new HashMap<>();

    public RouteBuilderHelper(List<StarDisplayRecord> starDisplayRecordList) {

        MapUtils.populateMap(starDisplayRecordMap,
                starDisplayRecordList,
                StarDisplayRecord::getStarName);
    }


    public RouteDescriptor buildPath(String source, String destination, String pathName, Color color, double lineWidth, String path) {
        RouteDescriptor route = RouteDescriptor.builder().build();
        String extractPath = path.substring(1, path.length() - 1);
        String[] starList = extractPath.split(",");

        // set route info
        route.setName(String.format("Route %s to %s, path %s", source, destination, pathName));
        route.setRouteNotes(path);
        route.setColor(color);
        route.setMaxLength(starList.length - 1);
        route.setStartStar(starList[0]);
        route.setLineWidth(lineWidth);

        // set segments
        for (String star : starList) {
            StarDisplayRecord starDisplayRecord = starDisplayRecordMap.get(star.trim());
            route.addLineSegment(starDisplayRecord.getCoordinates());
        }

        return route;
    }

    public boolean has(String origin) {
        return starDisplayRecordMap.containsKey(origin);
    }
}
