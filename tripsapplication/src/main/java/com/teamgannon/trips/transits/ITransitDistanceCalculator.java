package com.teamgannon.trips.transits;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Interface for calculating transit distances between stars.
 * Provides a narrow interface for the transit visualization system,
 * following the Interface Segregation Principle.
 */
public interface ITransitDistanceCalculator {

    /**
     * Calculate transit routes between stars within the specified range.
     *
     * @param transitRangeDef the transit range definition (min/max distance, color, etc.)
     * @param starsInView     the list of stars to check for transits
     * @return list of valid transit routes within the range
     */
    @NotNull
    List<TransitRoute> calculateDistances(@NotNull TransitRangeDef transitRangeDef,
                                          @NotNull List<StarDisplayRecord> starsInView);
}
