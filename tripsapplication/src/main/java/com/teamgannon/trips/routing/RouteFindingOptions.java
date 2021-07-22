package com.teamgannon.trips.routing;

import com.teamgannon.trips.jpa.model.StarObject;
import javafx.scene.paint.Color;
import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
public class RouteFindingOptions {

    private double upperBound;

    private double lowerBound;

    private StarObject originStar;
    private StarObject destinationStar;

    private String originStarName;

    private String destinationStarName;

    @Builder.Default
    private double lineWidth = 0.5;

    @Builder.Default
    private @NotNull Color color = Color.WHITE;

    @Builder.Default
    private int numberPaths = 3;

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
