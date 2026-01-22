package com.teamgannon.trips.routing.routemanagement;

import com.teamgannon.trips.config.application.model.SerialFont;
import com.teamgannon.trips.graphics.entities.StellarEntityFactory;
import com.teamgannon.trips.routing.RoutingConstants;
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
        String prefix = firstLink ? RoutingConstants.FIRST_SEGMENT_PREFIX : RoutingConstants.LABEL_SUFFIX;
        Label label = new Label(prefix + String.format("%.2f ", length));
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
        Sphere sphere = new Sphere(RoutingConstants.ROUTE_POINT_SPHERE_RADIUS);
        sphere.setMaterial(material);
        label.setLabelFor(sphere);
        routeDisplay.linkObjectToLabel(sphere, label);
        return sphere;
    }


    /**
     * Create a 3D line segment (cylinder) connecting two points in space.
     * <p>
     * This method creates a cylinder oriented between the origin and target points,
     * with an optional label attached at the midpoint.
     * <p>
     * <b>Geometry Calculation:</b>
     * <ol>
     *   <li><b>Height:</b> The cylinder height equals the distance between origin and target</li>
     *   <li><b>Position:</b> The cylinder is translated to the midpoint between origin and target</li>
     *   <li><b>Rotation:</b> The cylinder (initially along Y-axis) is rotated to align with
     *       the vector from origin to target using cross-product and dot-product calculations</li>
     * </ol>
     * <p>
     * <b>Rotation Mathematics:</b>
     * <pre>
     * yAxis = (0, 1, 0)                    // Cylinder's default orientation
     * diff = target - origin              // Direction vector
     * axisOfRotation = diff × yAxis       // Cross product gives rotation axis
     * angle = acos(diff·yAxis / |diff|)   // Dot product gives rotation angle
     * </pre>
     * <p>
     * <b>Transform Order:</b> JavaFX applies transforms in list order, so
     * {@code [moveToMidpoint, rotateAroundCenter]} first moves, then rotates.
     *
     * @param origin      the 3D starting point of the line segment
     * @param target      the 3D ending point of the line segment
     * @param lineWeight  the diameter/width of the cylinder in scene units
     * @param color       the color for the cylinder material
     * @param lengthLabel the label to attach at the midpoint (shows distance)
     * @return a Group containing the cylinder and optionally a label anchor sphere
     */
    public @NotNull Node createLineSegment(Point3D origin, @NotNull Point3D target,
                                           double lineWeight, Color color, @NotNull Label lengthLabel) {
        // Y-axis is the cylinder's default orientation in JavaFX
        Point3D yAxis = new Point3D(0, 1, 0);
        // Vector from origin to target
        Point3D diff = target.subtract(origin);
        // Cylinder height = distance between points
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
            lengthLabel.setBackground(new Background(new BackgroundFill(backgroundColor, new CornerRadii(RoutingConstants.LABEL_CORNER_RADIUS), new Insets(0))));
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
     * Determine an appropriate background color based on text color.
     * Uses luminance-based threshold to ensure label readability.
     * <p>
     * If the text color is dark (sum of RGB < threshold), use white background.
     * If the text color is light (sum of RGB >= threshold), use dark gray background.
     *
     * @param color the text color
     * @return the background color for optimal contrast
     */
    private Color determineBckColor(Color color) {
        int red = colorNorm(color.getRed());
        int green = colorNorm(color.getGreen());
        int blue = colorNorm(color.getBlue());
        int sum = red + green + blue;
        if (sum < RoutingConstants.DARK_BACKGROUND_THRESHOLD) {
            // Text is dark, use light background
            return Color.WHITE;
        } else {
            // Text is light, use dark background
            return Color.DARKGRAY;
        }
    }

    /**
     * Convert a raw color component (0.0-1.0) to an integer (0-255).
     *
     * @param raw the raw color value (0.0-1.0)
     * @return the normalized integer value (0-255)
     */
    private int colorNorm(double raw) {
        return (int) (raw * RoutingConstants.RGB_MAX_VALUE);
    }

}
