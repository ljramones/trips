package com.teamgannon.trips.routing.model;

import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.routing.RoutingConstants;
import javafx.scene.paint.Color;
import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
public class RouteFindingOptions {

    @Builder.Default
    private double upperBound = RoutingConstants.DEFAULT_UPPER_DISTANCE;

    @Builder.Default
    private double lowerBound = RoutingConstants.DEFAULT_LOWER_DISTANCE;

    private StarObject originStar;
    private StarObject destinationStar;

    private String originStarName;

    private String destinationStarName;

    @Builder.Default
    private double lineWidth = RoutingConstants.DEFAULT_LINE_WIDTH;

    @Builder.Default
    private @NotNull Color color = Color.WHITE;

    @Builder.Default
    private int numberPaths = RoutingConstants.DEFAULT_NUMBER_PATHS;

    /**
     * the stars to exclude when finding routes
     */
    @Builder.Default
    private Set<String> starExclusions = new HashSet<>();

    /**
     * the polities that we want to exclude
     */
    @Builder.Default
    private Set<String> polityExclusions = new HashSet<>();

    private boolean selected;

    private double maxDistance;

}
