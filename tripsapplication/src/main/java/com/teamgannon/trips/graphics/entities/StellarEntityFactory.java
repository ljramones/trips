package com.teamgannon.trips.graphics.entities;


import com.teamgannon.trips.config.application.model.ColorPalette;
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
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

import java.util.Map;

public class StellarEntityFactory {

    /**
     * we do this to make the star size a constant size bigger x3
     */
    private final static int GRAPHICS_FUDGE_FACTOR = 3;


    /**
     * create a line sgment based on a very slender cylinder
     *
     * @param origin the from point
     * @param target the to point
     * @param color  the color of the line
     * @return the line as a cylinder
     */
    public static Cylinder createLineSegment(Point3D origin,
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
        Cylinder line = createCylinder(width, color, height);

        line.getTransforms().addAll(moveToMidpoint, rotateAroundCenter);

        return line;
    }

    public static Cylinder createCylinder(double width, Color color, double height) {
        Cylinder line = new Cylinder(width, height);
        final PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(color);
        material.setSpecularColor(color);
        line.setMaterial(material);
        return line;
    }


    public static Node drawStellarObject(StarDisplayRecord record,
                                         ColorPalette colorPalette,
                                         Xform labelGroup) {

        Sphere sphere = createStellarShape(record);
        Label label = createLabel(record, sphere, colorPalette);
        labelGroup.getChildren().add(label);
        Group group = new Group(sphere, label);
        group.setUserData(record);
        return group;
    }


    public static Node drawCentralIndicator(StarDisplayRecord record,
                                            ColorPalette colorPalette,
                                            Xform labelGroup) {
        Box box = createBox(record);
        Label label = createLabel(record, box, colorPalette);
        labelGroup.getChildren().add(label);
        Group group = new Group(box, label);
        group.setUserData(record);
        RotateTransition rotator = setRotationAnimation(group);
        rotator.play();
        return group;
    }

    private static RotateTransition setRotationAnimation(Group group) {
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

    private static Box createBox(StarDisplayRecord record) {
        final PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(record.getStarColor());
        material.setSpecularColor(record.getStarColor());
        Box box = new Box(4, 4, 4);
        box.setMaterial(material);
        Point3D point3D = record.getCoordinates();
        box.setTranslateX(point3D.getX());
        box.setTranslateY(point3D.getY());
        box.setTranslateZ(point3D.getZ());
        return box;
    }

    /**
     * create a stellar object
     *
     * @param record the star record
     * @return the created object
     */
    public static Sphere createStellarShape(StarDisplayRecord record) {
        final PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(record.getStarColor());
        material.setSpecularColor(record.getStarColor());
        Sphere sphere = new Sphere(record.getRadius() * GRAPHICS_FUDGE_FACTOR);
        sphere.setMaterial(material);
        Point3D point3D = record.getCoordinates();
        sphere.setTranslateX(point3D.getX());
        sphere.setTranslateY(point3D.getY());
        sphere.setTranslateZ(point3D.getZ());
        return sphere;
    }

    /**
     * create a label for a shape
     *
     * @param record       the star record
     * @param sphere       the star/shape
     * @param colorPalette the color palette to use
     * @return the created object
     */
    public static Label createLabel(StarDisplayRecord record, Shape3D sphere, ColorPalette colorPalette) {
        Label label = new Label(record.getStarName());
        label.setFont(new Font("Arial", 8));
        label.setTextFill(colorPalette.getLabelColor());
//        label.setTextFill(record.getStarColor());
        Point3D point3D = record.getCoordinates();
        label.setTranslateX(point3D.getX());
        label.setTranslateY(point3D.getY());
        label.setTranslateZ(point3D.getZ());
        label.setLabelFor(sphere);
        return label;
    }

    /**
     * this creates an independent connect series of lines related to the route
     *
     * @param routeDescriptor the route descriptor
     * @return the route Xform
     */
    public static Xform createRoute(RouteDescriptor routeDescriptor) {
        Xform route = new Xform();
        route.setWhatAmI("route-" + routeDescriptor.getName());
        boolean firstLink = true;

        Point3D previousPoint = new Point3D(0, 0, 0);
        for (Point3D point3D : routeDescriptor.getLineSegments()) {
            if (firstLink) {
                previousPoint = point3D;
                firstLink = false;
            } else {
                // create the line segment
                Node lineSegment
                        = CustomObjectFactory.createLineSegment(
                        previousPoint, point3D, 0.5, routeDescriptor.getColor()
                );
                // step along the segment
                previousPoint = point3D;

                // add the completed line segment to overall list
                route.getChildren().add(lineSegment);
            }

        }
        return route;
    }

}
