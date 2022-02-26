package com.teamgannon.trips.graphics.panes;

import com.teamgannon.trips.config.application.ScreenSize;
import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.controller.RotationController;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GalacticSpacePlane extends Pane implements RotationController {

    private final TripsContext tripsContext;


    private static final double ROTATE_SECS = 60;

    // define the rest position of the image
    private final Rotate rotateX = new Rotate(105, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    private final Rotate rotateZ = new Rotate(30, Rotate.Z_AXIS);


    private final Group world = new Group();
    private final @NotNull SubScene subScene;

    @NotNull Group root = new Group();
    @NotNull PerspectiveCamera camera = new PerspectiveCamera(true);

    // mouse positions
    private double mousePosX, mousePosY = 0;
    private double mouseOldX, mouseOldY = 0;
    private double mouseDeltaX, mouseDeltaY = 0;

    /**
     * animation toggle
     */
    private boolean animationPlay = false;

    /**
     * offset to scene coordinates to account for the top UI plane
     */
    private double controlPaneOffset;

    private double deltaX;

    /**
     * animation rotator
     */
    private final @NotNull RotateTransition rotator;


    private boolean sidePanelShiftKludgeFirstTime = true;

    public GalacticSpacePlane(TripsContext tripsContext) {
        this.tripsContext = tripsContext;
        ScreenSize screenSize = tripsContext.getScreenSize();

        // attach our custom rotation transforms so we can update the labels dynamically
        world.getTransforms().addAll(rotateX, rotateY, rotateZ);

        subScene = new SubScene(world, screenSize.getSceneWidth(), screenSize.getSceneHeight(), true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.BLACK);

        setInitialView();

        subScene.setCamera(camera);
        Group sceneRoot = new Group(subScene);

        this.setBackground(Background.EMPTY);
        this.getChildren().add(sceneRoot);
        this.setPickOnBounds(false);

        subScene.widthProperty().bind(this.widthProperty());
        subScene.heightProperty().bind(this.heightProperty());

        root.getChildren().add(this);

        // create a rotation animation
        rotator = createRotateAnimation();



    }


    /**
     * create an animation player
     *
     * @return the rotation animator
     */
    private @NotNull RotateTransition createRotateAnimation() {
        RotateTransition rotate = new RotateTransition(
                Duration.seconds(ROTATE_SECS),
                world
        );
        rotate.setAxis(Rotate.Y_AXIS);
        rotate.setFromAngle(360);
        rotate.setToAngle(0);
        rotate.setInterpolator(Interpolator.LINEAR);
        rotate.setCycleCount(RotateTransition.INDEFINITE);
        updateLabels();
        return rotate;
    }

    /**
     * set the initial view
     */
    public void setInitialView() {
        setPerspectiveCamera();
    }

    public void resetView() {
        setInitialView();
//        camera.setRotate(25);
    }


    public void setRotationAngles(double xAngle, double yAngle, double zAngle) {
        rotateX.setAngle(xAngle);
        rotateY.setAngle(yAngle);
        rotateZ.setAngle(zAngle);
        updateLabels();
    }

    public void resetPosition() {
        setRotationAngles(105, 0, 30);
        setPerspectiveCamera();
    }

    private void setPerspectiveCamera() {
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-1600);
    }


    public void updateLabels() {

    }



    ////////////// zoom and move

    public void zoomIn() {
        zoomGraph(-200);
        updateLabels();
    }

    public void zoomIn(int amount) {
        zoomGraph(-amount);
        updateLabels();
    }


    public void zoomOut() {
        zoomGraph(200);
        updateLabels();
    }

    public void zoomOut(int amount) {
        zoomGraph(amount);
        updateLabels();
    }

    /**
     * do actual zoom
     *
     * @param zoomAmt the amount to zoom
     */
    private void zoomGraph(double zoomAmt) {
        double z = camera.getTranslateZ();
        double newZ = z - zoomAmt;
        camera.setTranslateZ(newZ);
    }



}
