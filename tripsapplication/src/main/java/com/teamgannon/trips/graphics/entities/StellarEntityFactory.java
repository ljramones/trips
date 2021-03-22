package com.teamgannon.trips.graphics.entities;


import com.teamgannon.trips.config.application.StarDisplayPreferences;
import com.teamgannon.trips.config.application.model.ColorPalette;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.text.Font;
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

}
