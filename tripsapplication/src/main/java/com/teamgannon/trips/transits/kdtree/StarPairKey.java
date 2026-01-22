package com.teamgannon.trips.transits.kdtree;

import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility for generating consistent, order-independent pair keys for star pairs.
 * <p>
 * This ensures that (StarA, StarB) and (StarB, StarA) produce the same key,
 * enabling efficient deduplication of bidirectional relationships.
 * <p>
 * Thread-safe for concurrent use.
 *
 * @see KDTreeTransitCalculator
 * @see KDTreeGraphBuilder
 */
public final class StarPairKey {

    private static final char SEPARATOR = '|';

    private StarPairKey() {
        // Utility class - no instantiation
    }

    /**
     * Generates a consistent key for a pair of star names.
     * The key is order-independent: generate("A", "B") equals generate("B", "A").
     *
     * @param nameA first star name
     * @param nameB second star name
     * @return normalized pair key
     */
    public static @NotNull String generate(@NotNull String nameA, @NotNull String nameB) {
        if (nameA.compareTo(nameB) < 0) {
            return nameA + SEPARATOR + nameB;
        } else {
            return nameB + SEPARATOR + nameA;
        }
    }

    /**
     * Creates a thread-safe set for tracking seen pairs.
     * Use with {@link #addIfAbsent(Set, String, String)} for deduplication.
     *
     * @return a new concurrent set for pair tracking
     */
    public static @NotNull Set<String> createTrackingSet() {
        return ConcurrentHashMap.newKeySet();
    }

    /**
     * Adds a pair to the tracking set if not already present.
     *
     * @param seen  the tracking set
     * @param nameA first star name
     * @param nameB second star name
     * @return true if the pair was added (not seen before), false if already present
     */
    public static boolean addIfAbsent(@NotNull Set<String> seen,
                                       @NotNull String nameA,
                                       @NotNull String nameB) {
        return seen.add(generate(nameA, nameB));
    }

    /**
     * Checks if a pair has been seen.
     *
     * @param seen  the tracking set
     * @param nameA first star name
     * @param nameB second star name
     * @return true if the pair exists in the set
     */
    public static boolean contains(@NotNull Set<String> seen,
                                    @NotNull String nameA,
                                    @NotNull String nameB) {
        return seen.contains(generate(nameA, nameB));
    }
}
