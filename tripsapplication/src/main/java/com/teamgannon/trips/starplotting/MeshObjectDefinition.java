package com.teamgannon.trips.starplotting;

import javafx.geometry.Point3D;
import javafx.scene.Node;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MeshObjectDefinition {

    private String name;

    private Node object;

    private double xScale;
    private double yScale;
    private double zScale;

    private Point3D axis;
    private double rotateAngle;


}
