package com.teamgannon.trips.floating;

import javafx.application.Platform;
import javafx.scene.*;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static org.fxyz3d.geometry.MathUtils.clamp;

@Slf4j
public class InterstellarExample extends Pane {

    private double mousePosX;
    private double mousePosY;
    private double mouseOldX;
    private double mouseOldY;
    private double mouseDeltaX;
    private double mouseDeltaY;

    private final Font font = new Font("arial", 10);

    // We'll use custom Rotate transforms to manage the coordinate conversions
    private final Rotate rotateX = new Rotate(25, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(25, Rotate.Y_AXIS);
    private final Rotate rotateZ = new Rotate(0, Rotate.Z_AXIS);

    private final Group root = new Group();
    private final Group world = new Group();  //all 3D nodes in scene
    private final Group labelGroup = new Group(); //all generic 3D labels


    private final SubScene subScene;

    private final StarPlotterManagerExample starPlotterManagerExample;

    private final GridPlotManagerExample gridPlotManagerExample;


    public InterstellarExample(double sceneWidth,
                               double sceneHeight) {

        // attach our custom rotation transforms so we can update the labels dynamically
        world.getTransforms().addAll(rotateX, rotateY, rotateZ);

        subScene = new SubScene(world, sceneWidth, sceneHeight, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.BLACK);

        PerspectiveCamera camera = setPerspectiveCamera();

        subScene.setCamera(camera);
        Group sceneRoot = new Group(subScene);
        sceneRoot.getChildren().add(labelGroup);

        handleMouseEvents();

        // add to the 2D portion of this component
        Pane pane = new Pane();
        pane.setPrefSize(sceneWidth, sceneHeight);
        pane.setMaxSize(Pane.USE_COMPUTED_SIZE, Pane.USE_COMPUTED_SIZE);
        pane.setMinSize(Pane.USE_COMPUTED_SIZE, Pane.USE_COMPUTED_SIZE);
        pane.setBackground(Background.EMPTY);
        pane.getChildren().add(sceneRoot);
        pane.setPickOnBounds(false);

        subScene.widthProperty().bind(pane.widthProperty());
        subScene.heightProperty().bind(pane.heightProperty());
        Platform.runLater(this::updateLabels);
        root.getChildren().add(pane);

        this.starPlotterManagerExample = new StarPlotterManagerExample(
                labelGroup,
                world,
                subScene
        );

        this.gridPlotManagerExample = new GridPlotManagerExample(
                labelGroup,
                world,
                sceneRoot,
                subScene);

        starPlotterManagerExample.generateRandomStars(20);

    }

    @NotNull
    private PerspectiveCamera setPerspectiveCamera() {

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-1500);
        return camera;
    }

    public Group getRoot() {
        return root;
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
        starPlotterManagerExample.updateLabels();
        gridPlotManagerExample.updateScale();
        gridPlotManagerExample.updateLabels();
    }

}
