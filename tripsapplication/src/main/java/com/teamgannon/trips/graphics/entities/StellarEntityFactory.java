package com.teamgannon.trips.graphics.entities;


import com.teamgannon.trips.config.application.StarDisplayPreferences;
import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.jpa.model.CivilizationDisplayPreferences;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNull;

public class StellarEntityFactory {

    public static @NotNull Cylinder createCylinder(double width, Color color, double height) {
        Cylinder line = new Cylinder(width, height);
        final PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(color);
        material.setSpecularColor(color);
        line.setMaterial(material);
        return line;
    }

    public static @NotNull Node drawCentralIndicator(@NotNull StarDisplayRecord record,
                                                     ColorPalette colorPalette,
                                                     @NotNull Label label,
                                                     StarDisplayPreferences starDisplayPreferences) {

        Box box = createBox(record, label);
        Group group = new Group(box, label);
        group.setUserData(record);
        RotateTransition rotator = setRotationAnimation(group);
        rotator.play();
        return group;
    }

    private static @NotNull RotateTransition setRotationAnimation(Group group) {
        RotateTransition rotate = new RotateTransition(
                Duration.seconds(10),
                group
        );
        rotate.setAxis(Rotate.Y_AXIS);
        rotate.setFromAngle(360);
        rotate.setToAngle(0);
        rotate.setInterpolator(Interpolator.LINEAR);
        rotate.setCycleCount(RotateTransition.INDEFINITE);
        return rotate;
    }

    private static @NotNull Box createBox(@NotNull StarDisplayRecord record, @NotNull Label label) {
        final PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(record.getStarColor());
        material.setSpecularColor(record.getStarColor());
        Box box = new Box(4, 4, 4);
        box.setMaterial(material);
        Point3D point3D = record.getCoordinates();
        box.setTranslateX(point3D.getX());
        box.setTranslateY(point3D.getY());
        box.setTranslateZ(point3D.getZ());
        label.setLabelFor(box);
        return box;
    }


    /**
     * create a label for a shape
     *
     * @param record       the star record
     * @param colorPalette the color palette to use
     * @return the created object
     */
    public static @NotNull Label createLabel(@NotNull StarDisplayRecord record,
                                             @NotNull ColorPalette colorPalette) {
        Label label = new Label(record.getStarName());
        label.setFont(new Font("Arial", 8));
        label.setTextFill(colorPalette.getLabelColor());
        Point3D point3D = record.getCoordinates();
        label.setTranslateX(point3D.getX());
        label.setTranslateY(point3D.getY());
        label.setTranslateZ(point3D.getZ());
        return label;
    }

}
