package com.teamgannon.trips.graphics.entities;


import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

public class CustomObjectFactory {


    public static Node createLineSegment(Point3D origin,
                                               Point3D target,
                                               double width,
                                               Color color) {

        Point3D yAxis = new Point3D(0, 1, 0);
        Point3D diff = target.subtract(origin);
        double height = diff.magnitude();

        Point3D mid = target.midpoint(origin);
        Translate moveToMidpoint = new Translate(mid.getX(), mid.getY(), mid.getZ());

        Point3D axisOfRotation = diff.crossProduct(yAxis);
        double angle = Math.acos(diff.normalize().dotProduct(yAxis));
        Rotate rotateAroundCenter = new Rotate(-Math.toDegrees(angle), axisOfRotation);

        // create cylinder and color it with phong material
        Cylinder line = StellarEntityFactory.createCylinder(width, color, height);

        Xform lineGroup = new Xform();

        line.getTransforms().addAll(moveToMidpoint, rotateAroundCenter);
        lineGroup.getChildren().add(line);

        return lineGroup;
    }


}
