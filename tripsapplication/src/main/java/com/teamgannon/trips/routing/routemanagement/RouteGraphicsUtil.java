package com.teamgannon.trips.routing.routemanagement;

import com.teamgannon.trips.config.application.model.SerialFont;
import com.teamgannon.trips.graphics.entities.StellarEntityFactory;
import javafx.geometry.Insets;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class RouteGraphicsUtil {

    private RouteDisplay routeDisplay;

    public RouteGraphicsUtil(RouteDisplay routeDisplay) {
        this.routeDisplay = routeDisplay;
    }


    /**
     * create a label for representing a length
     *
     * @param firstLink first link is a flag that is used to define whether this link (line segment or route segment)
     *                  is the beginning
     * @param length    the value of the length to turn into a label
     * @return the annotated label
     */
    public @NotNull Label createLabel(boolean firstLink, double length) {
        Label label = new Label(((firstLink) ? " Start -> " : " ") + String.format("%.2f ", length));
        SerialFont serialFont = routeDisplay.getColorPallete().getLabelFont();

        label.setFont(serialFont.toFont());
        return label;
    }


    /**
     * create a point sphere
     * this is used to anchor a label at a point since a label must be attached to something
     *
     * @param label the label to attach to this point sphere
     * @return the point sphere and label
     */
    public @NotNull Sphere createPointSphere(@NotNull Label label) {
        final PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.WHEAT);
        material.setSpecularColor(Color.WHEAT);
        Sphere sphere = new Sphere(1);
        sphere.setMaterial(material);
        label.setLabelFor(sphere);
        routeDisplay.linkObjectToLabel(sphere, label);
        return sphere;
    }


    /**
     * create a line segment from a 3D point to another
     * this is really a cylinder of n pixels in diameter
     *
     * @param origin      the 3d origin point
     * @param target      the 3d destination point
     * @param lineWeight  the width of the line/cyclinder
     * @param color       the line color
     * @param lengthLabel the length label
     * @return the created line segment
     */
    public @NotNull Node createLineSegment(Point3D origin, @NotNull Point3D target,
                                           double lineWeight, Color color, @NotNull Label lengthLabel) {
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

        Group lineGroup = new Group();

        line.getTransforms().addAll(moveToMidpoint, rotateAroundCenter);
        lineGroup.getChildren().add(line);

        if (routeDisplay.isRouteLabelsOn()) {
            // attach label
            Sphere pointSphere = createPointSphere(lengthLabel);
            pointSphere.setTranslateX(mid.getX());
            pointSphere.setTranslateY(mid.getY());
            pointSphere.setTranslateZ(mid.getZ());
            lengthLabel.setTextFill(color);
            Color backgroundColor = determineBckColor(color);
            lengthLabel.setBackground(new Background(new BackgroundFill(backgroundColor, new CornerRadii(5.0), new Insets(0))));
            lineGroup.getChildren().add(pointSphere);
            if (!routeDisplay.isLabelPresent(lengthLabel)) {
                routeDisplay.linkObjectToLabel(pointSphere, lengthLabel);
            } else {
                log.warn("what is <{}> present twice", lengthLabel.getText());
            }
        }

        return lineGroup;
    }

    /**
     * create an appropriate background color based on text color
     *
     * @param color the test color
     * @return the background color
     */
    private Color determineBckColor(Color color) {
        int red = colorNorm(color.getRed());
        int green = colorNorm(color.getGreen());
        int blue = colorNorm(color.getBlue());
        int sum = red + green + blue;
        Color bckColor;
        if (sum < 384) {
//            log.info("dark color:{}", sum);
            bckColor = Color.WHITE;
        } else {
//            log.info("light color:{}", sum);
            bckColor = Color.DARKGRAY;
        }
        return bckColor;
    }

    /**
     * Convert a raww color ro a norm
     *
     * @param raw the raw value
     * @return the nored value
     */
    private int colorNorm(double raw) {
        return (int) (raw * 255.0);
    }

}
