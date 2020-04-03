package com.teamgannon.trips.experiments;

import com.teamgannon.trips.graphics.entities.Xform;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Box;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Translate;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class NewFloatingLabels extends Pane {

    ////////////   Graphics Section of definitions  ////////////////
    private Group root = new Group();

    private Xform world = new Xform();

    // camera work
    private PerspectiveCamera camera = new PerspectiveCamera(true);
    private final Xform cameraXform = new Xform();
    private final Xform cameraXform2 = new Xform();
    private final Xform cameraXform3 = new Xform();
    private static final double CAMERA_INITIAL_DISTANCE = 0;
    private static final double CAMERA_INITIAL_X_ANGLE = 0;//   -90
    private static final double CAMERA_INITIAL_Y_ANGLE = 0; // 0
    private static final double CAMERA_NEAR_CLIP = 0.1;
    private static final double CAMERA_FAR_CLIP = 10000.0;

    // mouse positions
    private double mousePosX, mousePosY = 0;
    private double mouseOldX, mouseOldY = 0;
    private double mouseDeltaX, mouseDeltaY = 0;


    /////////////////
    // screen real estate
    private int width;
    private int height;

    private Xform xBox = new Xform();


    public NewFloatingLabels(int width, int height) {
        this.width = width;
        this.height = height;

        this.setMinHeight(height);
        this.setMinWidth(width);

        buildRoot();
        buildScene();
        buildCamera();

        Box box = new Box(150, 100, 50);
        box.setDrawMode(DrawMode.LINE);
        box.setCullFace(CullFace.NONE);
        xBox.getChildren().add(box);

        world.getChildren().add(xBox);

        updateLabels(xBox);
        handleMouseEvents(this);
    }


    private void buildRoot() {
        // hooks this into the
        this.getChildren().add(root);
    }

    private void buildScene() {
        SubScene subScene = new SubScene(
                world,
                width, height,
                true, SceneAntialiasing.BALANCED);
        subScene.setCamera(camera);
        subScene.setFill(Color.WHITE);

        root.getChildren().add(subScene);
    }

    private void buildCamera() {

        root.getChildren().add(cameraXform);
        cameraXform.getChildren().add(cameraXform2);
        cameraXform2.getChildren().add(cameraXform3);
        cameraXform3.getChildren().add(camera);
//        cameraXform3.setRotateZ(180.0);

        // set camera POV and initial position
        camera.setNearClip(CAMERA_NEAR_CLIP);
        camera.setFarClip(CAMERA_FAR_CLIP);
        camera.setFieldOfView(20);
        camera.setTranslateZ(CAMERA_INITIAL_DISTANCE);

        // rotate camera along x and y axis
        cameraXform.ry.setAngle(CAMERA_INITIAL_Y_ANGLE);
        cameraXform.rx.setAngle(CAMERA_INITIAL_X_ANGLE);

        // push camera back to see the object
        cameraXform3.setTranslate(0, 0, -500);

    }

    /**
     * used to handle rotation of the scene
     *
     * @param pane the subscene to manage rotation
     */
    private void handleMouseEvents(Pane pane) {

        // get initial position of the mouse
        pane.setOnMousePressed((MouseEvent me) -> {
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            mouseOldX = me.getSceneX();
            mouseOldY = me.getSceneY();
        });

        // rotate the scene based on whether move moves
        pane.setOnMouseDragged((MouseEvent me) -> {
            mouseOldX = mousePosX;
            mouseOldY = mousePosY;
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            mouseDeltaX = (mousePosX - mouseOldX);
            mouseDeltaY = (mousePosY - mouseOldY);
            updateLabels(xBox);
//
//            double modifier = 1.0;
//            double modifierFactor = 0.1;
//
//            if (me.isControlDown()) {
//                modifier = 0.1;
//            }
//            if (me.isShiftDown()) {
//                modifier = 10.0;
//            }

            if (me.isPrimaryButtonDown()) {
                cameraXform.ry.setAngle(cameraXform.ry.getAngle() - mouseDeltaX);  // +
                cameraXform.rx.setAngle(cameraXform.rx.getAngle() + mouseDeltaY);  // -
            } else if (me.isSecondaryButtonDown()) {
                log.info("secondary button pushed, x={}, y={}", mousePosX, mousePosY);
            } else if (me.isMiddleButtonDown()) {
                log.info("middle button pushed, x={}, y={}", mousePosX, mousePosY);
            }
        });

    }


    private List<Point3D> generateDots(Box box) {
        List<Point3D> vertices = new ArrayList<>();
        Bounds bounds = box.getBoundsInLocal();
        vertices.add(box.localToScene(new Point3D(bounds.getMinX(), bounds.getMinY(), bounds.getMinZ())));
        vertices.add(box.localToScene(new Point3D(bounds.getMinX(), bounds.getMinY(), bounds.getMaxZ())));
        vertices.add(box.localToScene(new Point3D(bounds.getMinX(), bounds.getMaxY(), bounds.getMinZ())));
        vertices.add(box.localToScene(new Point3D(bounds.getMinX(), bounds.getMaxY(), bounds.getMaxZ())));
        vertices.add(box.localToScene(new Point3D(bounds.getMaxX(), bounds.getMinY(), bounds.getMinZ())));
        vertices.add(box.localToScene(new Point3D(bounds.getMaxX(), bounds.getMinY(), bounds.getMaxZ())));
        vertices.add(box.localToScene(new Point3D(bounds.getMaxX(), bounds.getMaxY(), bounds.getMinZ())));
        vertices.add(box.localToScene(new Point3D(bounds.getMaxX(), bounds.getMaxY(), bounds.getMaxZ())));

        return vertices;
    }

    private void updateLabels(Xform box) {
        root.getChildren().removeIf(Label.class::isInstance);
        AtomicInteger counter = new AtomicInteger(1);
        Box rbox = (Box) box.getChildren().get(0);
        List<Point3D> dots = generateDots(rbox);

        for (Point3D dot : dots) {
            Point3D p2 = box.localToScene(dot, true);
            Label label = new Label(
                    "" + counter.getAndIncrement() +
                            String.format(" (%.1f,%.1f)",
                                    p2.getX(),
                                    p2.getY()));
            label.setStyle("-fx-font-size:1.3em; -fx-text-fill: blue;");
            label.getTransforms().setAll(new Translate(p2.getX(), p2.getY()));
            root.getChildren().add(label);
            System.out.println(String.format("x(%5.2f),y(%5.2f),z(%5.2f)", dot.getX(), dot.getY(), dot.getZ()));
        }
        log.info("one transition");
    }


}
