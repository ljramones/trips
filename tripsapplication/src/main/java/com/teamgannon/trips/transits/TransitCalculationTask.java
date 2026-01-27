package com.teamgannon.trips.transits;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.transits.kdtree.KDPoint;
import com.teamgannon.trips.transits.kdtree.KDTree3D;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * JavaFX Task for calculating transits with progress reporting.
 * <p>
 * This task wraps the transit calculation logic and provides progress updates
 * that can be bound to UI elements (ProgressBar, Label).
 * <p>
 * Progress is reported as:
 * <ul>
 *   <li>0%: Starting</li>
 *   <li>5%: Building spatial index</li>
 *   <li>10-90%: Processing bands (evenly divided among enabled bands)</li>
 *   <li>100%: Complete</li>
 * </ul>
 */
@Slf4j
public class TransitCalculationTask extends Task<TransitCalculationResult> {

    private final TransitDefinitions transitDefinitions;
    private final List<StarDisplayRecord> starsInView;
    private final ITransitDistanceCalculator calculator;

    /**
     * Creates a new transit calculation task.
     *
     * @param transitDefinitions the transit band definitions
     * @param starsInView        the stars to calculate transits between
     * @param calculator         the calculator to use (for small datasets)
     */
    public TransitCalculationTask(@NotNull TransitDefinitions transitDefinitions,
                                   @NotNull List<StarDisplayRecord> starsInView,
                                   @NotNull ITransitDistanceCalculator calculator) {
        this.transitDefinitions = transitDefinitions;
        this.starsInView = starsInView;
        this.calculator = calculator;
    }

    @Override
    protected TransitCalculationResult call() throws Exception {
        long startTime = System.currentTimeMillis();
        Map<UUID, List<TransitRoute>> routesByBand = new HashMap<>();
        int totalRoutes = 0;

        try {
            updateMessage("Starting transit calculation...");
            updateProgress(0, 100);

            // Get enabled bands
            List<TransitRangeDef> enabledBands = transitDefinitions.getTransitRangeDefs().stream()
                    .filter(TransitRangeDef::isEnabled)
                    .collect(Collectors.toList());

            if (enabledBands.isEmpty()) {
                updateMessage("No bands enabled");
                return buildResult(routesByBand, 0, startTime, false, null);
            }

            if (starsInView.isEmpty()) {
                updateMessage("No stars in view");
                return buildResult(routesByBand, 0, startTime, false, null);
            }

            updateMessage("Processing %,d stars for %d transit bands...".formatted(
                    starsInView.size(), enabledBands.size()));
            updateProgress(5, 100);

            // Check if cancelled
            if (isCancelled()) {
                return buildResult(routesByBand, 0, startTime, true, null);
            }

            // For large datasets, use KD-Tree with progress reporting
            if (starsInView.size() > 100) {
                // Build tree once
                updateMessage("Building spatial index...");
                KDTree3D<StarDisplayRecord> tree = buildTree(starsInView);
                updateProgress(10, 100);

                // Process each band with progress
                double progressPerBand = 80.0 / enabledBands.size();
                int bandIndex = 0;

                for (TransitRangeDef band : enabledBands) {
                    if (isCancelled()) {
                        return buildResult(routesByBand, totalRoutes, startTime, true, null);
                    }

                    updateMessage(String.format("Processing band '%s' (%d of %d)...",
                            band.getBandName(), bandIndex + 1, enabledBands.size()));

                    List<TransitRoute> routes = calculateBandWithProgress(tree, band, bandIndex, enabledBands.size());
                    routesByBand.put(band.getBandId(), routes);
                    totalRoutes += routes.size();

                    bandIndex++;
                    updateProgress(10 + (int) (progressPerBand * bandIndex), 100);
                }
            } else {
                // For small datasets, use the standard calculator (fast enough)
                double progressPerBand = 85.0 / enabledBands.size();
                int bandIndex = 0;

                for (TransitRangeDef band : enabledBands) {
                    if (isCancelled()) {
                        return buildResult(routesByBand, totalRoutes, startTime, true, null);
                    }

                    updateMessage(String.format("Processing band '%s' (%d of %d)...",
                            band.getBandName(), bandIndex + 1, enabledBands.size()));

                    List<TransitRoute> routes = calculator.calculateDistances(band, starsInView);
                    routesByBand.put(band.getBandId(), routes);
                    totalRoutes += routes.size();

                    bandIndex++;
                    updateProgress(10 + (int) (progressPerBand * bandIndex), 100);
                }
            }

            updateMessage("Complete: found %,d transits".formatted(totalRoutes));
            updateProgress(100, 100);

            return buildResult(routesByBand, totalRoutes, startTime, false, null);

        } catch (Exception e) {
            log.error("Transit calculation failed", e);
            updateMessage("Calculation failed: " + e.getMessage());
            return buildResult(routesByBand, totalRoutes, startTime, false, e.getMessage());
        }
    }

    /**
     * Build KD-Tree from stars.
     */
    private KDTree3D<StarDisplayRecord> buildTree(List<StarDisplayRecord> stars) {
        List<KDPoint<StarDisplayRecord>> points = stars.stream()
                .map(star -> new KDPoint<>(star.getActualCoordinates(), star))
                .collect(Collectors.toList());
        return new KDTree3D<>(points);
    }

    /**
     * Calculate transits for a single band with progress reporting.
     */
    private List<TransitRoute> calculateBandWithProgress(
            KDTree3D<StarDisplayRecord> tree,
            TransitRangeDef rangeDef,
            int bandIndex,
            int totalBands) {

        Set<String> seen = ConcurrentHashMap.newKeySet();
        List<TransitRoute> routes = new ArrayList<>();
        double upperRange = rangeDef.getUpperRange();
        double lowerRange = rangeDef.getLowerRange();

        int starCount = starsInView.size();
        int progressReportInterval = Math.max(1, starCount / 20); // Report progress every 5%

        for (int i = 0; i < starCount; i++) {
            if (isCancelled()) {
                break;
            }

            StarDisplayRecord star = starsInView.get(i);
            double[] coords = star.getActualCoordinates();
            List<KDPoint<StarDisplayRecord>> neighbors = tree.rangeSearch(coords, upperRange);

            for (KDPoint<StarDisplayRecord> neighbor : neighbors) {
                StarDisplayRecord target = neighbor.data();
                if (star == target) continue;

                String key = pairKey(star, target);
                if (!seen.add(key)) continue;

                double distance = neighbor.distanceTo(coords);
                if (distance > lowerRange) {
                    routes.add(createRoute(star, target, distance, rangeDef));
                }
            }

            // Update progress periodically
            if (i % progressReportInterval == 0) {
                double bandProgress = (double) i / starCount;
                double overallProgress = 10 + (80.0 / totalBands) * (bandIndex + bandProgress);
                updateProgress((int) overallProgress, 100);

                updateMessage(String.format("Band '%s': processed %,d of %,d stars, found %,d routes...",
                        rangeDef.getBandName(), i, starCount, routes.size()));
            }
        }

        return routes;
    }

    private String pairKey(StarDisplayRecord a, StarDisplayRecord b) {
        String nameA = a.getStarName();
        String nameB = b.getStarName();
        return nameA.compareTo(nameB) < 0 ? nameA + "|" + nameB : nameB + "|" + nameA;
    }

    private TransitRoute createRoute(StarDisplayRecord source,
                                      StarDisplayRecord target,
                                      double distance,
                                      TransitRangeDef rangeDef) {
        return TransitRoute.builder()
                .good(true)
                .source(source)
                .target(target)
                .distance(distance)
                .lineWeight(rangeDef.getLineWidth())
                .color(rangeDef.getBandColor())
                .build();
    }

    private TransitCalculationResult buildResult(Map<UUID, List<TransitRoute>> routesByBand,
                                                   int totalRoutes,
                                                   long startTime,
                                                   boolean cancelled,
                                                   String errorMessage) {
        return TransitCalculationResult.builder()
                .routesByBand(routesByBand)
                .transitDefinitions(transitDefinitions)
                .totalRoutes(totalRoutes)
                .calculationTimeMs(System.currentTimeMillis() - startTime)
                .cancelled(cancelled)
                .errorMessage(errorMessage)
                .build();
    }
}
