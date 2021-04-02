package com.teamgannon.trips.starplotting;

import javafx.geometry.Point3D;
import javafx.scene.Node;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class MeshObjectDefinition {

    /**
     * the object name for lookup
     */
    private String name;

    /**
     * a unique id where such things are required
     */
    private UUID id;

    /**
     * the actual imported mesh object
     */
    private Node object;

    /**
     * to scale the x dimension
     */
    private double xScale;

    /**
     * to scale the y dimension
     */
    private double yScale;

    /**
     * to scale the z dimension
     */
    private double zScale;

    /**
     * the rotation axis
     */
    private Point3D axis;

    /**
     * the rotation angle to apply on the axis above
     */
    private double rotateAngle;


}
