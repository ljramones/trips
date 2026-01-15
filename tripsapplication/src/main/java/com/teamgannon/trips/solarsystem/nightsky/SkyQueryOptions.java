package com.teamgannon.trips.solarsystem.nightsky;

public final class SkyQueryOptions {

    public enum SortMode {
        BRIGHTEST,
        HIGHEST
    }

    private final double minAltitudeDeg;
    private final int maxResults;
    private final SortMode sortMode;
    private final double minMagnitude;

    public SkyQueryOptions() {
        this(0.0, 25, SortMode.BRIGHTEST, Double.POSITIVE_INFINITY);
    }

    public SkyQueryOptions(double minAltitudeDeg, int maxResults, SortMode sortMode, double minMagnitude) {
        this.minAltitudeDeg = minAltitudeDeg;
        this.maxResults = maxResults;
        this.sortMode = sortMode == null ? SortMode.BRIGHTEST : sortMode;
        this.minMagnitude = minMagnitude;
    }

    public double getMinAltitudeDeg() {
        return minAltitudeDeg;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public SortMode getSortMode() {
        return sortMode;
    }

    public double getMinMagnitude() {
        return minMagnitude;
    }
}
