package com.teamgannon.trips.transits;

import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Result object containing calculated transits for all bands.
 * Returned by async transit calculation.
 */
@Data
@Builder
public class TransitCalculationResult {

    /**
     * Map of band ID to the list of calculated transit routes for that band.
     */
    @NotNull
    private Map<UUID, List<TransitRoute>> routesByBand;

    /**
     * The original transit definitions used for calculation.
     */
    @NotNull
    private TransitDefinitions transitDefinitions;

    /**
     * Total number of routes calculated across all bands.
     */
    private int totalRoutes;

    /**
     * Time taken to calculate in milliseconds.
     */
    private long calculationTimeMs;

    /**
     * Whether the calculation was cancelled.
     */
    private boolean cancelled;

    /**
     * Error message if calculation failed, null otherwise.
     */
    private String errorMessage;

    /**
     * Check if calculation completed successfully.
     */
    public boolean isSuccess() {
        return !cancelled && errorMessage == null;
    }
}
