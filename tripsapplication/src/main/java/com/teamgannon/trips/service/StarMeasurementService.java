package com.teamgannon.trips.service;

import com.teamgannon.trips.algorithms.StarMath;
import com.teamgannon.trips.dialogs.search.model.DistanceRoutes;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.service.model.TransitRoute;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class StarMeasurementService {

    private final Set<String> lookupSourceTarget = new HashSet<>();

    /**
     * calculate the distances between all the stars in view
     *
     * @param distance    the distance measure
     * @param starsInView the list of stars in view
     */
    public @NotNull List<TransitRoute> calculateDistances(@NotNull DistanceRoutes distance, @NotNull List<StarDisplayRecord> starsInView) {
        lookupSourceTarget.clear();
        List<TransitRoute> allTransit = new ArrayList<>();
        for (StarDisplayRecord record : starsInView) {
            List<TransitRoute> recordTransits = findStarsWithLimit(record, starsInView, distance);
            allTransit.addAll(recordTransits);
        }
        return allTransit;
    }

    private @NotNull List<TransitRoute> findStarsWithLimit(@NotNull StarDisplayRecord sourceRecord,
                                                           @NotNull List<StarDisplayRecord> starsInView,
                                                           @NotNull DistanceRoutes distanceRange) {
        List<TransitRoute> routeList = new ArrayList<>();
        for (StarDisplayRecord targetRecord : starsInView) {
            TransitRoute route = calcDistanceAndCheck(sourceRecord, targetRecord, distanceRange);
            if (route.isGood()) {
                routeList.add(route);
            }
        }
        return routeList;
    }

    private TransitRoute calcDistanceAndCheck(@NotNull StarDisplayRecord sourceRecord,
                                              @NotNull StarDisplayRecord targetRecord,
                                              @NotNull DistanceRoutes distanceRange) {
        double[] sourceCoordinates = sourceRecord.getActualCoordinates();
        double[] targetCoordinates = targetRecord.getActualCoordinates();
        if (checkOffSourceTarget(sourceRecord.getStarName(), targetRecord.getStarName())) {
            return TransitRoute.builder().good(false).build();
        }
        try {
            double distance = StarMath.getDistance(sourceCoordinates, targetCoordinates);
            if (checkInRange(distanceRange, distance)) {
                // ok, we have a measure to save
                System.out.printf("%s --> %s is %.2f ly%n",
                        sourceRecord.getStarName(),
                        targetRecord.getStarName(),
                        distance
                );
                return TransitRoute
                        .builder()
                        .good(true)
                        .source(sourceRecord)
                        .target(targetRecord)
                        .distance(distance)
                        .lineWeight((distanceRange.getLineWidth()))
                        .color(distanceRange.getColor())
                        .build();
            } else {
                return TransitRoute.builder().good(false).build();
            }
        } catch (Exception e) {
            log.error("failed to measure");
        }
        return TransitRoute.builder().good(false).build();
    }

    /**
     * check if we calculated either source to target
     * or target to source
     *
     * @param sourceName the source name
     * @param targetName the target name
     * @return true if we saw it, false if not
     */
    private boolean checkOffSourceTarget(String sourceName, String targetName) {
        if (lookupSourceTarget.contains(sourceName + "," + targetName)) {
            return true;
        } else {
            lookupSourceTarget.add(sourceName + "," + targetName);
        }
        if (lookupSourceTarget.contains(targetName + "," + sourceName)) {
            return true;
        } else {
            lookupSourceTarget.add(targetName + "," + sourceName);
        }
        return false;
    }

    private boolean checkInRange(@NotNull DistanceRoutes distanceRange, double distance) {
        return distance < distanceRange.getUpperDistance() && distance > distanceRange.getLowerDistance();
    }

}
