package com.teamgannon.trips.transits;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import javafx.geometry.Point3D;
import javafx.scene.paint.Color;
import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
@Builder
public class TransitRoute {

    private boolean good;

    private StarDisplayRecord source;
    private StarDisplayRecord target;

    private double distance;

    /**
     * transit route color
     */
    private Color color;

    private double lineWeight;

    public Point3D getSourceEndpoint() {
        return source.getCoordinates();
    }

    public Point3D getTargetEndpoint() {
        return target.getCoordinates();
    }

    public @NotNull String getName() {
        return source.getStarName() + "," + target.getStarName();
    }

}
