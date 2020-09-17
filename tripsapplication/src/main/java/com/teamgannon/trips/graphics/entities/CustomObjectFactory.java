package com.teamgannon.trips.graphics.entities;


import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Cylinder;
import javafx.scene.text.Font;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

public class CustomObjectFactory {

    public static Node createLineSegment(Point3D origin,
                                         Point3D target,
                                         double width,
                                         Color color) {
        return createLineSegment(origin, target, width, color, null, false);
    }


    public static Node createLineSegment(Point3D origin,
                                         Point3D target,
                                         double width,
                                         Color color,
                                         String tag,
                                         boolean sense) {

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
        if (tag != null) {
            Label label = new Label(tag);
            label.setFont(new Font("Arial", 8));
            label.setTextFill(Color.WHEAT);
            if (sense) {
                label.setTranslateX(origin.getX());
                label.setTranslateY(origin.getY());
                label.setTranslateZ(origin.getZ());
            } else {
                label.setTranslateX(target.getX());
                label.setTranslateY(target.getY());
                label.setTranslateZ(target.getZ());
            }
            lineGroup.getChildren().add(label);
        }

        return lineGroup;
    }


    public static Node createLineSegment(Point3D origin, Point3D target, double lineWeight, Color color, boolean labelsOn, Label lengthLabel) {
        Point3D yAxis = new Point3D(0, 1, 0);
        Point3D diff = target.subtract(origin);
        double height = diff.magnitude();

        Point3D mid = target.midpoint(origin);
        Translate moveToMidpoint = new Translate(mid.getX(), mid.getY(), mid.getZ());

        Point3D axisOfRotation = diff.crossProduct(yAxis);
        double angle = Math.acos(diff.normalize().dotProduct(yAxis));
        Rotate rotateAroundCenter = new Rotate(-Math.toDegrees(angle), axisOfRotation);

        // create cylinder and color it with phong material
        Cylinder line = StellarEntityFactory.createCylinder(lineWeight, color, height);

        Xform lineGroup = new Xform();

        line.getTransforms().addAll(moveToMidpoint, rotateAroundCenter);
        lineGroup.getChildren().add(line);

        if (labelsOn) {
            // attach label
            lengthLabel.setTranslateX(mid.getX());
            lengthLabel.setTranslateY(mid.getY());
            lengthLabel.setTranslateZ(mid.getZ());
            lengthLabel.setTextFill(color);
            lineGroup.getChildren().add(lengthLabel);
        }

        return lineGroup;
    }
}
