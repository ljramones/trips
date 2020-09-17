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

public class StellarEntityFactory {

    /**
     * we do this to make the star size a constant size bigger x3
     */
    private final static double GRAPHICS_FUDGE_FACTOR = 1.5;


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
                                         boolean labelsOn,
                                         boolean politiesOn,
                                         StarDisplayPreferences starDisplayPreferences,
                                         CivilizationDisplayPreferences polityPreferences) {

        Group group = createStellarShape(record, colorPalette, labelsOn, politiesOn, polityPreferences);
        group.setUserData(record);
        return group;
    }


    public static Node drawCentralIndicator(StarDisplayRecord record,
                                            ColorPalette colorPalette,
                                            Label label,
                                            StarDisplayPreferences starDisplayPreferences) {

        Box box = createBox(record, label);
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

    private static Box createBox(StarDisplayRecord record, Label label) {
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
     * create a stellar object
     *
     * @param record            the star record
     * @param colorPalette      the color palette to use
     * @param labelsOn          are labels on?
     * @param politiesOn        are polities on?
     * @param polityPreferences the plo
     * @return the created object
     */
    public static Group createStellarShape(StarDisplayRecord record,
                                           ColorPalette colorPalette,
                                           boolean labelsOn,
                                           boolean politiesOn,
                                           CivilizationDisplayPreferences polityPreferences) {

        Group group = new Group();

        final PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(record.getStarColor());
        material.setSpecularColor(record.getStarColor());
        Sphere sphere = new Sphere(record.getRadius() * GRAPHICS_FUDGE_FACTOR);
        sphere.setMaterial(material);
        Point3D point3D = record.getCoordinates();
        sphere.setTranslateX(point3D.getX());
        sphere.setTranslateY(point3D.getY());
        sphere.setTranslateZ(point3D.getZ());
        group.getChildren().add(sphere);

        if (labelsOn) {
            Label label = createLabel(record, colorPalette);
            label.setLabelFor(sphere);
            group.getChildren().add(label);

        }

        if (politiesOn) {
            if (!record.getPolity().equals("NA")) {
                Color polityColor = polityPreferences.getColorForPolity(record.getPolity());
                // add a polity indicator
                double polityShellRadius = record.getRadius() * GRAPHICS_FUDGE_FACTOR * 1.5;
                // group.getChildren().add(politySphere);
                PhongMaterial polityMaterial = new PhongMaterial();
//            polityMaterial.setDiffuseMap(earthImage);
                polityMaterial.setDiffuseColor(new Color(polityColor.getRed(), polityColor.getGreen(), polityColor.getBlue(), 0.2));  // Note alpha of 0.6
                polityMaterial.diffuseMapProperty();
                Sphere politySphere = new Sphere(polityShellRadius);
                politySphere.setMaterial(polityMaterial);
                politySphere.setTranslateX(point3D.getX());
                politySphere.setTranslateY(point3D.getY());
                politySphere.setTranslateZ(point3D.getZ());
                group.getChildren().add(politySphere);
            }
        }
        return group;
    }


    /**
     * create a label for a shape
     *
     * @param record       the star record
     * @param colorPalette the color palette to use
     * @return the created object
     */
    public static Label createLabel(StarDisplayRecord record,
                                    ColorPalette colorPalette) {
        Label label = new Label(record.getStarName());
        label.setFont(new Font("Arial", 8));
        label.setTextFill(colorPalette.getLabelColor());
        Point3D point3D = record.getCoordinates();
        label.setTranslateX(point3D.getX());
        label.setTranslateY(point3D.getY());
        label.setTranslateZ(point3D.getZ());
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
