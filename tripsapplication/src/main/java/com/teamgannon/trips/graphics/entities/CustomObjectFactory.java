package com.teamgannon.trips.graphics.entities;


import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Cylinder;
import javafx.scene.text.Font;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CustomObjectFactory {

    public static @NotNull Node createLineSegment(@NotNull Point3D origin,
                                                  @NotNull Point3D target,
                                                  double width,
                                                  Color color,
                                                  Font font) {
        return createLineSegment(origin, target, width, color, font, null, false);
    }


    public static @NotNull Node createLineSegment(@NotNull Point3D origin,
                                                  @NotNull Point3D target,
                                                  double width,
                                                  Color color,
                                                  Font font,
                                                  @Nullable String tag,
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

        Group lineGroup = new Group();

        line.getTransforms().addAll(moveToMidpoint, rotateAroundCenter);
        lineGroup.getChildren().add(line);
        if (tag != null) {
            Label label = new Label(tag);
            label.setFont(font);
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


}
