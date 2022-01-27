package com.teamgannon.trips.service.measure;

import com.teamgannon.trips.algorithms.StarMath;
import com.teamgannon.trips.dialogs.search.model.DistanceRoutes;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.measure.OshiMeasure;
import com.teamgannon.trips.measure.TrackExecutionTime;
import com.teamgannon.trips.transits.TransitRangeDef;
import com.teamgannon.trips.transits.TransitRoute;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class StarMeasurementService {

    private final Set<String> lookupSourceTarget = new HashSet<>();
    private final OshiMeasure oshiMeasure;

    private Random random = new Random();

    public StarMeasurementService(OshiMeasure oshiMeasure) {
        this.oshiMeasure = oshiMeasure;
    }

    public PerformanceMeasure calculateTimeToDoSearch(long starCount) {
        double timeFor1MDistCalcs = benchmark();
        int numberOfProcessors = oshiMeasure.numberOfLogicalProcessors();
        long memory = oshiMeasure.getAvailableMemoryInMb();
        long totalPathsWorstCase = starCount * starCount / 2;
        double costForCalcsPerprocessor = totalPathsWorstCase * timeFor1MDistCalcs;
        double costForCalcs = costForCalcsPerprocessor / numberOfProcessors * 200; // 70 is an arbitrary to scale the application
        log.info("cost for calcs ={}", costForCalcs);

        return PerformanceMeasure
                .builder()
                .numberProcessors(numberOfProcessors)
                .numbersOfStars(starCount)
                .worseCasePaths(totalPathsWorstCase)
                .timeToDoRouteSearch(costForCalcs)
                .memorySize(memory)
                .build();
    }

    private double benchmark() {
        double distance = 23;
        double[] origin = randomCoordinates(distance);
        double[] destination = randomCoordinates(distance / 21);
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            origin[0] += 0.001;
            origin[1] -= 0.001;
            origin[2] += 0.002;
            destination[0] += 0.0011;
            destination[1] -= 0.0012;
            destination[2] += 0.0021;
            distance = StarMath.getDistance(origin, destination);
        }
        long endTime = System.currentTimeMillis();
        return (endTime - startTime) / 1000.0 / 1000000;
    }

    private double[] randomCoordinates(double distance) {
        double[] array = new double[3];
        array[0] = random.nextDouble() + distance;
        array[1] = random.nextDouble() - distance;
        array[2] = random.nextDouble() + distance / 2;
        return array;
    }

    public double calculateDistance(double[] origin, double[] destination) {
        return StarMath.getDistance(origin, destination);
    }

    /**
     * calculate the distances between all the stars in view
     *
     * @param distance    the distance measure
     * @param starsInView the list of stars in view
     */
    @TrackExecutionTime
    public @NotNull List<TransitRoute> calculateDistances(@NotNull DistanceRoutes distance, @NotNull List<StarDisplayRecord> starsInView) {
        lookupSourceTarget.clear();
        List<TransitRoute> allTransit = new ArrayList<>();
        for (StarDisplayRecord record : starsInView) {
            List<TransitRoute> recordTransits = findStarsWithLimit(record, starsInView, distance);
            allTransit.addAll(recordTransits);
        }
        return allTransit;
    }

    @TrackExecutionTime
    public @NotNull List<TransitRoute> calculateDistances(@NotNull TransitRangeDef transitRangeDef, @NotNull List<StarDisplayRecord> starsInView) {
        lookupSourceTarget.clear();
        List<TransitRoute> allTransit = new ArrayList<>();
        for (StarDisplayRecord record : starsInView) {
            List<TransitRoute> recordTransits = findStarsWithLimit(record, starsInView, transitRangeDef);
            allTransit.addAll(recordTransits);
        }
        return allTransit;
    }

    @TrackExecutionTime
    private @NotNull List<TransitRoute> findStarsWithLimit(@NotNull StarDisplayRecord sourceRecord,
                                                           @NotNull List<StarDisplayRecord> starsInView,
                                                           @NotNull TransitRangeDef transitRangeDef) {
        List<TransitRoute> routeList = new ArrayList<>();
        for (StarDisplayRecord targetRecord : starsInView) {
            if (targetRecord != null) {
                TransitRoute route = calcDistanceAndCheck(sourceRecord, targetRecord, transitRangeDef);
                if (route.isGood()) {
                    routeList.add(route);
                }
            }
        }
        return routeList;
    }

    @TrackExecutionTime
    private @NotNull List<TransitRoute> findStarsWithLimit(@NotNull StarDisplayRecord sourceRecord,
                                                           @NotNull List<StarDisplayRecord> starsInView,
                                                           @NotNull DistanceRoutes distanceRange) {
        List<TransitRoute> routeList = new ArrayList<>();
        for (StarDisplayRecord targetRecord : starsInView) {
            if (targetRecord != null) {
                TransitRoute route = calcDistanceAndCheck(sourceRecord, targetRecord, distanceRange);
                if (route.isGood()) {
                    routeList.add(route);
                }
            }
        }
        return routeList;
    }

    private TransitRoute calcDistanceAndCheck(@NotNull StarDisplayRecord sourceRecord,
                                              @NotNull StarDisplayRecord targetRecord,
                                              @NotNull TransitRangeDef transitRangeDef) {
        double[] sourceCoordinates = sourceRecord.getActualCoordinates();
        double[] targetCoordinates = targetRecord.getActualCoordinates();
        if (checkOffSourceTarget(sourceRecord.getStarName(), targetRecord.getStarName())) {
            return TransitRoute.builder().good(false).build();
        }
        try {
            double distance = StarMath.getDistance(sourceCoordinates, targetCoordinates);
            if (checkInRange(transitRangeDef, distance)) {
                // ok, we have a measure to save
                return TransitRoute
                        .builder()
                        .good(true)
                        .source(sourceRecord)
                        .target(targetRecord)
                        .distance(distance)
                        .lineWeight((transitRangeDef.getLineWidth()))
                        .color(transitRangeDef.getBandColor())
                        .build();
            } else {
                return TransitRoute.builder().good(false).build();
            }
        } catch (Exception e) {
            log.error("failed to measure");
        }
        return TransitRoute.builder().good(false).build();
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

    private boolean checkInRange(@NotNull TransitRangeDef transitRangeDef, double distance) {
        return distance < transitRangeDef.getUpperRange() && distance > transitRangeDef.getLowerRange();
    }

}
