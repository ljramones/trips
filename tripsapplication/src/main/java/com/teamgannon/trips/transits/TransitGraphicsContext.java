package com.teamgannon.trips.transits;

import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import javafx.scene.SubScene;
import lombok.Builder;
import lombok.Getter;

/**
 * Holds shared graphics context and services needed for transit visualization.
 * This context object reduces constructor parameter count for TransitRouteVisibilityGroup.
 */
@Getter
@Builder
public class TransitGraphicsContext {

    /**
     * The 3D subscene for coordinate transformations
     */
    private final SubScene subScene;

    /**
     * The interstellar space pane containing the visualization
     */
    private final InterstellarSpacePane interstellarSpacePane;

    /**
     * Offset from top of window to the control pane
     */
    private final double controlPaneOffset;

    /**
     * Service for calculating distances between stars
     */
    private final ITransitDistanceCalculator distanceCalculator;

    /**
     * Service for building routes from transit segments
     */
    private final ITransitRouteBuilder routeBuilderService;

    /**
     * Application context for accessing current plot settings
     */
    private final TripsContext tripsContext;
}
