package com.teamgannon.trips.floating;

import com.teamgannon.trips.graphics.entities.CustomObjectFactory;
import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.transform.Translate;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Slf4j
public class StarPlotterManagerExample {

    //////  support
    private final Random random = new Random();

    private final static double RADIUS_MAX = 7;
    private final static double X_MAX = 300;
    private final static double Y_MAX = 300;
    private final static double Z_MAX = 300;
    private final Group labelGroup;
    private final Group world;
    private final SubScene subScene;

    private final Group extensionsGroup = new Group();


    private final Font font = new Font("arial", 10);

    private final Map<Node, Label> shapeToLabel = new HashMap<>();

    public StarPlotterManagerExample(Group labelGroup,
                                     Group world,
                                     SubScene subScene) {

        this.labelGroup = labelGroup;
        this.world = world;
        this.subScene = subScene;

        world.getChildren().add(extensionsGroup);

    }

    public void generateRandomStars(int numberStars) {
        for (int i = 0; i < numberStars; i++) {
            double radius = random.nextDouble() * RADIUS_MAX;
            Color color = randomColor();
            double x = random.nextDouble() * X_MAX * 2 / 3 * (random.nextBoolean() ? 1 : -1);
            double y = random.nextDouble() * Y_MAX * 2 / 3 * (random.nextBoolean() ? 1 : -1);
            double z = random.nextDouble() * Z_MAX * 2 / 3 * (random.nextBoolean() ? 1 : -1);

            String labelText = "Star " + i;
            boolean fadeFlag = random.nextBoolean();
            createSphereAndLabel(radius, x, y, z, color, labelText, fadeFlag);
            createExtension(x,y,z, Color.VIOLET);
        }

        log.info("shapes:{}", shapeToLabel.size());
    }

    private void createExtension(double x, double y, double z, Color extensionColor) {
        Point3D point3DFrom = new Point3D(x, y, z);
        Point3D point3DTo = new Point3D(point3DFrom.getX(), 0, point3DFrom.getZ());
        double lineWidth = 0.3;
        Node lineSegment = CustomObjectFactory.createLineSegment(point3DFrom, point3DTo, lineWidth, extensionColor);
        extensionsGroup.getChildren().add(lineSegment);
        // add the extensions group to the world model
        extensionsGroup.setVisible(true);
    }

    private Color randomColor() {
        int r = random.nextInt(255);
        int g = random.nextInt(255);
        int b = random.nextInt(255);
        return Color.rgb(r, g, b);
    }


    private void createSphereAndLabel(double radius, double x, double y, double z, Color color, String labelText, boolean fadeFlag) {
        Sphere sphere = new Sphere(radius);
        sphere.setTranslateX(x);
        sphere.setTranslateY(y);
        sphere.setTranslateZ(z);
        sphere.setMaterial(new PhongMaterial(color));
        //add our nodes to the group that will later be added to the 3D scene
        world.getChildren().add(sphere);

        Label label = new Label(labelText);
        label.setTextFill(color);
        label.setFont(font);
        ObjectDescriptor descriptor = ObjectDescriptor
                .builder()
                .name(labelText)
                .color(color)
                .x(x)
                .y(y)
                .z(z)
                .build();
        sphere.setUserData(descriptor);
        Tooltip tooltip = new Tooltip(descriptor.toString());
        Tooltip.install(sphere, tooltip);
        if (fadeFlag) {
            //have some fun, just one example of what you can do with the 2D node
            //in parallel to the 3D transformation. Be careful when you manipulate
            //the position of the 2D label as putting it off screen can mess with
            //your 2D layout.  See the clipping logic in updateLabels() for details
//            setupFade(label);
        }
        labelGroup.getChildren().add(label);

        //Add to hashmap so updateLabels() can manage the label position
        shapeToLabel.put(sphere, label);

    }

    private void setupFade(Node node) {
        FadeTransition fader = new FadeTransition(Duration.seconds(5), node);
        fader.setFromValue(1.0);
        fader.setToValue(0.1);
        fader.setCycleCount(Timeline.INDEFINITE);
        fader.setAutoReverse(true);
        fader.play();
    }

    public void updateLabels() {
        shapeToLabel.forEach((node, label) -> {
            Point3D coordinates = node.localToScene(Point3D.ZERO, true);

            //Clipping Logic
            //if coordinates are outside of the scene it could
            //stretch the screen so don't transform them
            double x = coordinates.getX();
            double y = coordinates.getY();

            // is it left of the view?
            if (x < 0) {
                x = 0;
            }

            // is it right of the view?
            if ((x + label.getWidth() + 5) > subScene.getWidth()) {
                x = subScene.getWidth() - (label.getWidth() + 5);
            }

            // is it above the view?
            if (y < 0) {
                y = 0;
            }

            // is it below the view
            if ((y + label.getHeight()) > subScene.getHeight()) {
                y = subScene.getHeight() - (label.getHeight() + 5);
            }

            //update the local transform of the label.
            label.getTransforms().setAll(new Translate(x, y));
        });

    }

}
