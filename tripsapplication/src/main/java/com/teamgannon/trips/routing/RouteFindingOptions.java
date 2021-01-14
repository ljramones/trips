package com.teamgannon.trips.routing;

import javafx.scene.paint.Color;
import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
@Builder
public class RouteFindingOptions {

    private double upperBound;

    private double lowerBound;

    private String originStar;

    private String destinationStar;

    @Builder.Default
    private double lineWidth = 0.5;

    @Builder.Default
    private @NotNull Color color = Color.WHITE;

    @Builder.Default
    private int numberPaths = 3;

    private boolean selected;

    private double maxDistance;

}
