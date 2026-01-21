package com.teamgannon.trips.routing.model;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Result of a route finding operation.
 * <p>
 * This is a value object that represents either a successful result with
 * {@link PossibleRoutes} or a failure with an error message.
 */
@Getter
public class RouteFindingResult {

    private final boolean success;
    private final @Nullable PossibleRoutes routes;
    private final @Nullable String errorMessage;

    private RouteFindingResult(boolean success, @Nullable PossibleRoutes routes, @Nullable String errorMessage) {
        this.success = success;
        this.routes = routes;
        this.errorMessage = errorMessage;
    }

    /**
     * Create a successful result with routes.
     *
     * @param routes the found routes
     * @return a successful result
     */
    public static @NotNull RouteFindingResult success(@NotNull PossibleRoutes routes) {
        return new RouteFindingResult(true, routes, null);
    }

    /**
     * Create a failure result with an error message.
     *
     * @param errorMessage the error message
     * @return a failure result
     */
    public static @NotNull RouteFindingResult failure(@NotNull String errorMessage) {
        return new RouteFindingResult(false, null, errorMessage);
    }

    /**
     * Check if routes were found (success and has routes).
     *
     * @return true if routes were found
     */
    public boolean hasRoutes() {
        return success && routes != null && !routes.getRoutes().isEmpty();
    }
}
