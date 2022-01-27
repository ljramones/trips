package com.teamgannon.trips.graphics.entities;


import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
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
