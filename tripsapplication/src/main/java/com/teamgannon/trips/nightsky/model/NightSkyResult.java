package com.teamgannon.trips.nightsky.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Result of night sky computation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NightSkyResult {
    /** Stars visible in the sky */
    private List<SkyStarPoint> stars;

    /** Host star position (the "sun") */
    private SkyStarPoint hostStar;

    /** Total stars queried from database */
    private int totalStarsQueried;

    /** Stars visible above horizon */
    private int visibleCount;

    /** Computation time */
    private Duration computeTime;

    /** Request that produced this result */
    private NightSkyRequest request;

    /** Timestamp of computation */
    private Instant computedAt;

    /** Whether result was served from cache */
    private boolean fromCache;
}
