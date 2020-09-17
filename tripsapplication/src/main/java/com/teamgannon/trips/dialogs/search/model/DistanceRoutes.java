package com.teamgannon.trips.dialogs.search.model;

import javafx.scene.paint.Color;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DistanceRoutes {

    private boolean selected;

    private double upperDistance;

    private double lowerDistance;

    private Color color;

    private double lineWidth;

}
