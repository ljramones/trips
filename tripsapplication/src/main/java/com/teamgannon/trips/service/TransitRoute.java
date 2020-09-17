package com.teamgannon.trips.service;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import javafx.geometry.Point3D;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransitRoute {

    private boolean good;

    private StarDisplayRecord source;
    private StarDisplayRecord target;

    private double distance;

    public Point3D getSourceEndpoint() {
        return source.getCoordinates();
    }

    public Point3D getTargetEndpoint() {
        return target.getCoordinates();
    }

    public String getName() {
        return source.getStarName()+","+target.getStarName();
    }

}
