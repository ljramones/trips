package com.teamgannon.trips.transits;

import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for transit route building operations.
 * Enables testability through mocking and dependency inversion.
 */
public interface ITransitRouteBuilder {

    /**
     * Check if route building is currently active.
     *
     * @return true if a route is being built
     */
    boolean isRoutingActive();

    /**
     * Set the current dataset descriptor for route building.
     *
     * @param descriptor the dataset descriptor
     */
    void setDataSetDescriptor(@Nullable DataSetDescriptor descriptor);

    /**
     * Start a new route from the given transit segment.
     * If a route is already in progress, prompts the user to confirm restart.
     *
     * @param transitRoute the initial transit segment
     * @return true if a new route was started
     */
    boolean startNewRoute(@NotNull TransitRoute transitRoute);

    /**
     * Add a transit segment to the current route.
     *
     * @param transitRoute the transit segment to add
     * @return true if segment was added, false if no route is active
     */
    boolean addToRoute(@NotNull TransitRoute transitRoute);

    /**
     * Complete the current route with the given final transit segment.
     *
     * @param transitRoute the final transit segment
     * @return true if route was completed successfully
     */
    boolean completeRoute(@NotNull TransitRoute transitRoute);

    /**
     * Cancel the current route being built.
     */
    void cancelRoute();

    /**
     * Get the number of segments in the current route.
     *
     * @return segment count
     */
    int getCurrentRouteSize();
}
