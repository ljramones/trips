package com.teamgannon.trips.routing.model;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Immutable cache key for route finding results.
 * <p>
 * Two cache keys are equal if they have the same algorithmic parameters:
 * origin, destination, distance bounds, number of paths, exclusions, and
 * optionally a hash of the available stars (for in-view searches).
 * <p>
 * Display-only parameters (color, lineWidth) are NOT included.
 * <p>
 * The key is normalized to ensure consistent hashing:
 * <ul>
 *   <li>Exclusion sets are sorted for consistent ordering</li>
 *   <li>Distance bounds are rounded to avoid floating-point comparison issues</li>
 *   <li>Star collection is hashed by star names for efficient comparison</li>
 * </ul>
 */
public final class RouteCacheKey {

    private static final int DISTANCE_PRECISION = 100; // 2 decimal places

    private final String originStarName;
    private final String destinationStarName;
    private final int normalizedUpperBound;
    private final int normalizedLowerBound;
    private final int numberPaths;
    private final Set<String> starExclusions;
    private final Set<String> polityExclusions;
    private final int starsHash;
    private final int hashCode;

    /**
     * Creates a cache key from route finding options (without star collection hash).
     * <p>
     * Use this for dataset-wide searches where the star collection is fixed.
     *
     * @param options the route finding options
     * @return a new cache key
     */
    public static RouteCacheKey fromOptions(@NotNull RouteFindingOptions options) {
        return new RouteCacheKey(
                options.getOriginStarName(),
                options.getDestinationStarName(),
                options.getUpperBound(),
                options.getLowerBound(),
                options.getNumberPaths(),
                options.getStarExclusions(),
                options.getPolityExclusions(),
                0 // No star hash
        );
    }

    /**
     * Creates a cache key from route finding options with a star collection.
     * <p>
     * Use this for in-view searches where the available stars may vary.
     *
     * @param options the route finding options
     * @param stars   the available stars for routing
     * @return a new cache key
     */
    public static RouteCacheKey fromOptionsWithStars(@NotNull RouteFindingOptions options,
                                                      @NotNull List<StarDisplayRecord> stars) {
        return new RouteCacheKey(
                options.getOriginStarName(),
                options.getDestinationStarName(),
                options.getUpperBound(),
                options.getLowerBound(),
                options.getNumberPaths(),
                options.getStarExclusions(),
                options.getPolityExclusions(),
                computeStarsHash(stars)
        );
    }

    /**
     * Computes a hash of the star collection based on star names.
     * <p>
     * Uses a sorted list of names for consistent ordering.
     *
     * @param stars the star collection
     * @return hash code
     */
    private static int computeStarsHash(@NotNull List<StarDisplayRecord> stars) {
        List<String> names = new ArrayList<>(stars.size());
        for (StarDisplayRecord star : stars) {
            if (star != null && star.getStarName() != null) {
                names.add(star.getStarName());
            }
        }
        Collections.sort(names);
        return names.hashCode();
    }

    /**
     * Creates a new cache key.
     *
     * @param originStarName      the origin star name
     * @param destinationStarName the destination star name
     * @param upperBound          the maximum jump distance
     * @param lowerBound          the minimum jump distance
     * @param numberPaths         the number of paths to find (K)
     * @param starExclusions      spectral classes to exclude
     * @param polityExclusions    polities to exclude
     * @param starsHash           hash of the available stars (0 if not used)
     */
    public RouteCacheKey(String originStarName,
                         String destinationStarName,
                         double upperBound,
                         double lowerBound,
                         int numberPaths,
                         Set<String> starExclusions,
                         Set<String> polityExclusions,
                         int starsHash) {
        this.originStarName = originStarName != null ? originStarName : "";
        this.destinationStarName = destinationStarName != null ? destinationStarName : "";
        this.normalizedUpperBound = normalizeDistance(upperBound);
        this.normalizedLowerBound = normalizeDistance(lowerBound);
        this.numberPaths = numberPaths;
        this.starsHash = starsHash;

        // Use TreeSet for consistent ordering
        this.starExclusions = starExclusions != null
                ? Collections.unmodifiableSet(new TreeSet<>(starExclusions))
                : Collections.emptySet();
        this.polityExclusions = polityExclusions != null
                ? Collections.unmodifiableSet(new TreeSet<>(polityExclusions))
                : Collections.emptySet();

        // Pre-compute hash code (immutable object)
        this.hashCode = computeHashCode();
    }

    /**
     * Normalizes a distance value to avoid floating-point comparison issues.
     *
     * @param distance the distance value
     * @return the normalized integer value
     */
    private static int normalizeDistance(double distance) {
        return (int) Math.round(distance * DISTANCE_PRECISION);
    }

    private int computeHashCode() {
        return Objects.hash(
                originStarName,
                destinationStarName,
                normalizedUpperBound,
                normalizedLowerBound,
                numberPaths,
                starExclusions,
                polityExclusions,
                starsHash
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RouteCacheKey that = (RouteCacheKey) o;
        return normalizedUpperBound == that.normalizedUpperBound
                && normalizedLowerBound == that.normalizedLowerBound
                && numberPaths == that.numberPaths
                && starsHash == that.starsHash
                && originStarName.equals(that.originStarName)
                && destinationStarName.equals(that.destinationStarName)
                && starExclusions.equals(that.starExclusions)
                && polityExclusions.equals(that.polityExclusions);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        String starsInfo = starsHash != 0 ? String.format(", stars=%08x", starsHash) : "";
        return String.format("RouteCacheKey[%s → %s, bounds=%.2f-%.2f, paths=%d, excl=%d/%d%s]",
                originStarName,
                destinationStarName,
                normalizedUpperBound / (double) DISTANCE_PRECISION,
                normalizedLowerBound / (double) DISTANCE_PRECISION,
                numberPaths,
                starExclusions.size(),
                polityExclusions.size(),
                starsInfo);
    }

    // Getters for debugging/logging
    public String getOriginStarName() {
        return originStarName;
    }

    public String getDestinationStarName() {
        return destinationStarName;
    }

    public double getUpperBound() {
        return normalizedUpperBound / (double) DISTANCE_PRECISION;
    }

    public double getLowerBound() {
        return normalizedLowerBound / (double) DISTANCE_PRECISION;
    }

    public int getNumberPaths() {
        return numberPaths;
    }
}
