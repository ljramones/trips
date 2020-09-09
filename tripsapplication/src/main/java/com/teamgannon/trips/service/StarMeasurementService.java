package com.teamgannon.trips.service;

import com.teamgannon.trips.algorithms.StarMath;
import com.teamgannon.trips.dialogs.search.model.DistanceRoutes;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class StarMeasurementService {

    /**
     * calculate the distances between all the stars in view
     *
     * @param distance    the distance measure
     * @param starsInView the list of stars in view
     */
    public void calculateDistances(DistanceRoutes distance, List<StarDisplayRecord> starsInView) {
        for (StarDisplayRecord record : starsInView) {
            findStarsWithLimit(record, starsInView, distance);
        }
    }

    private void findStarsWithLimit(StarDisplayRecord record,
                                    List<StarDisplayRecord> starsInView,
                                    DistanceRoutes distanceRange) {
        for (StarDisplayRecord targetRecord : starsInView) {
            if (calcDistanceAndCheck(record, targetRecord, distanceRange)) {
            }
        }
    }

    private boolean calcDistanceAndCheck(StarDisplayRecord sourceRecord,
                                         StarDisplayRecord targetRecord,
                                         DistanceRoutes distanceRange) {
        double[] sourceCoordinates = sourceRecord.getActualCoordinates();
        double[] targetCoordinates = targetRecord.getActualCoordinates();
        try {
            double distance = StarMath.getDistance(sourceCoordinates, targetCoordinates);
            if (checkInRange(distanceRange, distance)) {
                // ok, we have a measure to save
                System.out.println(String.format("%s --> %s is %.2f ly",
                        sourceRecord.getStarName(),
                        targetRecord.getStarName(),
                        distance
                ));
                return true;
            }
        } catch (Exception e) {
            log.error("failed to measure");
        }
        return false;
    }

    private boolean checkInRange(DistanceRoutes distanceRange, double distance) {
        return distance < distanceRange.getUpperDistance() && distance > distanceRange.getLowerDistance();
    }

    /**
     * do a first order difference check to determine if we should do the more complicated distance measure
     *
     * @param sourceCoordinates the source star
     * @param targetCoordinates the the target star
     * @param distance          the distance range to check
     * @return true if the check passes
     */
    private boolean quickCheck(double[] sourceCoordinates,
                               double[] targetCoordinates,
                               DistanceRoutes distance) {

        double min = distance.getLowerDistance();
        double max = distance.getUpperDistance();

        double x1 = sourceCoordinates[0];
        double x2 = targetCoordinates[0];

        double y1 = sourceCoordinates[1];
        double y2 = targetCoordinates[1];

        double z1 = sourceCoordinates[2];
        double z2 = targetCoordinates[2];

        // X2 - X1 + Y2 - y1 + z2 -z1  > min and < max
        double firstOrderDiff = Math.abs((x2 - x1) + (y2 - y1) + (z2 - z1));

        return firstOrderDiff > min && firstOrderDiff < max;
    }

}
