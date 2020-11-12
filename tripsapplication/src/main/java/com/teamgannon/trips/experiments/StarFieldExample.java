package com.teamgannon.trips.experiments;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.fxyz3d.geometry.MathUtils.clamp;

/**
 * example for flat labels
 */
@Slf4j
public class StarFieldExample extends Application {

    public static final int SCALE_X = 510;
    public static final int SCALE_Y = 540;
    public static final int SCALE_Z = 0;
    final double sceneWidth = 600;
    final double sceneHeight = 600;

    private double mousePosX;
    private double mousePosY;
    private double mouseOldX;
    private double mouseOldY;
    private double mouseDeltaX;
    private double mouseDeltaY;

    private final Font font = new Font("arial", 10);

    // We'll use custom Rotate transforms to manage the coordinate conversions
    private final Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    private final Rotate rotateZ = new Rotate(0, Rotate.Z_AXIS);

    private final Group root = new Group();
    private final Group world = new Group();  //all 3D nodes in scene
    private final Group labelGroup = new Group(); //all generic 3D labels

    //All shapes and labels linked via hash for easy update during camera movement
    private final Map<Node, Label> shape3DToLabel = new HashMap<>();

    private SubScene subScene;

    //////  support
    private final Random random = new Random();

    private final static double RADIUS_MAX = 7;
    private final static double X_MAX = 300;
    private final static double Y_MAX = 300;
    private final static double Z_MAX = 300;

    Pane pane;

    private final Label scaleLabel = new Label("Scale: 5 ly");

    public Pane createStarField() {

        PerspectiveCamera camera = setupPerspectiveCamera();

        // attach our custom rotation transforms so we can update the labels dynamically
        world.getTransforms().addAll(rotateX, rotateY, rotateZ);

        subScene = new SubScene(world, sceneWidth, sceneHeight, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.BLACK);
        subScene.setCamera(camera);

        Group sceneRoot = new Group(subScene);
        sceneRoot.getChildren().add(labelGroup);

        generateRandomStars(5);

        // add to the 2D portion of this component
        pane = new Pane();
        pane.setPrefSize(sceneWidth, sceneHeight);
        pane.setMaxSize(Pane.USE_COMPUTED_SIZE, Pane.USE_COMPUTED_SIZE);
        pane.setMinSize(Pane.USE_COMPUTED_SIZE, Pane.USE_COMPUTED_SIZE);
        pane.setBackground(Background.EMPTY);
        pane.getChildren().add(sceneRoot);
        pane.setPickOnBounds(true);

        subScene.widthProperty().bind(pane.widthProperty());
        subScene.heightProperty().bind(pane.heightProperty());
        Platform.runLater(this::updateLabels);

        handleMouseEvents();

        return (pane);
    }

    @NotNull
    private PerspectiveCamera setupPerspectiveCamera() {
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-1000);
        return camera;
    }

    private void handleMouseEvents() {
        subScene.setOnMousePressed((MouseEvent me) -> {
                    mousePosX = me.getSceneX();
                    mousePosY = me.getSceneY();
                    mouseOldX = me.getSceneX();
                    mouseOldY = me.getSceneY();
                }
        );

        subScene.setOnMouseDragged((MouseEvent me) -> {
                    mouseOldX = mousePosX;
                    mouseOldY = mousePosY;
                    mousePosX = me.getSceneX();
                    mousePosY = me.getSceneY();
                    mouseDeltaX = (mousePosX - mouseOldX);
                    mouseDeltaY = (mousePosY - mouseOldY);
                    double modifier = 5.0;
                    double modifierFactor = 0.1;

                    if (me.isPrimaryButtonDown()) {
                        if (me.isAltDown()) { //roll
                            rotateZ.setAngle(((rotateZ.getAngle() + mouseDeltaX * modifierFactor * modifier * 2.0) % 360 + 540) % 360 - 180); // +
                        } else {
                            rotateY.setAngle(((rotateY.getAngle() + mouseDeltaX * modifierFactor * modifier * 2.0) % 360 + 540) % 360 - 180); // +
                            rotateX.setAngle(
                                    clamp(
                                            (((rotateX.getAngle() - mouseDeltaY * modifierFactor * modifier * 2.0) % 360 + 540) % 360 - 180),
                                            -60,
                                            60
                                    )
                            ); // -
                        }
                    }
                    updateLabels();
                }
        );
    }


    private void updateLabels() {
        shape3DToLabel.forEach((node, label) -> {
            Point3D coordinates = node.localToScene(Point3D.ZERO, true);

            //Clipping Logic
            //if coordinates are outside of the scene it could
            //stretch the screen so don't transform them
            double xs = coordinates.getX();
            double ys = coordinates.getY();

            Bounds ofParent = pane.getBoundsInParent();
            double x = xs - ofParent.getMinX();
            double y = ys - ofParent.getMinY();

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

        scaleLabel.setTranslateX(SCALE_X);
        scaleLabel.setTranslateY(SCALE_Y);
        scaleLabel.setTranslateZ(SCALE_Z);
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
            createSphereLabel(radius, x, y, z, color, labelText, fadeFlag);
        }

        //Add to hashmap so updateLabels() can manage the label position

        scaleLabel.setFont(new Font("Arial", 15));
        scaleLabel.setTextFill(Color.WHEAT);
        scaleLabel.setTranslateX(SCALE_X);
        scaleLabel.setTranslateY(SCALE_Y);
        scaleLabel.setTranslateZ(SCALE_Z);
        labelGroup.getChildren().add(scaleLabel);
        log.info("shapes:{}", shape3DToLabel.size());
    }

    private Color randomColor() {
        int r = random.nextInt(255);
        int g = random.nextInt(255);
        int b = random.nextInt(255);
        return Color.rgb(r, g, b);
    }


    private void createSphereLabel(double radius, double x, double y, double z, Color color, String labelText, boolean fadeFlag) {
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

        labelGroup.getChildren().add(label);

        //Add to hashmap so updateLabels() can manage the label position
        shape3DToLabel.put(sphere, label);

    }


    //////////////////////////////////

    @Override
    public void start(Stage primaryStage) throws Exception {

        Pane controls = createControls();
        Pane pane = createStarField();
        VBox vBox = new VBox(
                controls,
                pane
        );

        root.getChildren().add(vBox);
        Scene scene = new Scene(root, sceneWidth, sceneHeight - 40);
        primaryStage.setTitle("2D Labels over 3D SubScene");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createControls() {
        VBox controls = new VBox(10, new Button("Button"));
        controls.setPadding(new Insets(10));
        return controls;
    }

    public static void main(String[] args) {
        launch(args);
    }

}