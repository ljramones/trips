package com.teamgannon.trips.transits;

import com.teamgannon.trips.service.measure.StarMeasurementService;
import com.teamgannon.trips.transits.kdtree.KDTreeTransitCalculator;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

/**
 * Factory for selecting the optimal transit distance calculator based on dataset size.
 * <p>
 * Strategy selection:
 * <ul>
 *   <li>Small datasets (≤ 100 stars): Use brute-force O(n²) - lower overhead</li>
 *   <li>Large datasets (> 100 stars): Use KD-Tree O(n log n) - better scaling</li>
 * </ul>
 */
@Slf4j
@Component
public class TransitCalculatorFactory {

    /**
     * Threshold for switching from brute-force to KD-Tree algorithm.
     * Below this count, the O(n²) algorithm is faster due to lower overhead.
     */
    private static final int KDTREE_THRESHOLD = 100;

    private final StarMeasurementService bruteForceCalculator;
    private final KDTreeTransitCalculator kdTreeCalculator;

    public TransitCalculatorFactory(StarMeasurementService bruteForceCalculator) {
        this.bruteForceCalculator = bruteForceCalculator;
        this.kdTreeCalculator = new KDTreeTransitCalculator(true);
    }

    /**
     * Returns the optimal calculator for the given star count.
     *
     * @param starCount the number of stars to process
     * @return the appropriate calculator
     */
    public @NotNull ITransitDistanceCalculator getCalculator(int starCount) {
        if (starCount <= KDTREE_THRESHOLD) {
            log.debug("Using brute-force calculator for {} stars", starCount);
            return bruteForceCalculator;
        } else {
            log.debug("Using KD-Tree calculator for {} stars", starCount);
            return kdTreeCalculator;
        }
    }

    /**
     * Returns the brute-force O(n²) calculator.
     * Optimal for small datasets (< 100 stars).
     */
    public @NotNull ITransitDistanceCalculator getBruteForceCalculator() {
        return bruteForceCalculator;
    }

    /**
     * Returns the KD-Tree O(n log n) calculator.
     * Optimal for large datasets (> 100 stars).
     */
    public @NotNull ITransitDistanceCalculator getKDTreeCalculator() {
        return kdTreeCalculator;
    }

    /**
     * Returns the KD-Tree calculator with multi-band optimization.
     * Use this when calculating multiple transit bands simultaneously.
     */
    public @NotNull KDTreeTransitCalculator getMultiBandCalculator() {
        return kdTreeCalculator;
    }
}
