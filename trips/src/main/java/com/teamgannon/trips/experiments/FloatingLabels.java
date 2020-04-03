package com.teamgannon.trips.experiments;

import javafx.application.Application;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.Label;
import javafx.scene.shape.Box;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class FloatingLabels extends Application {

    private final Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);

    private double mousePosX;
    private double mousePosY;
    private double mouseOldX;
    private double mouseOldY;

    private Group root;

    @Override
    public void start(Stage primaryStage) {

        Box box = new Box(150, 100, 50);
        box.setDrawMode(DrawMode.LINE);
        box.setCullFace(CullFace.NONE);

        Group group = new Group(box);

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setFieldOfView(20);
        camera.getTransforms().addAll (
                rotateX, rotateY, new Translate(0, 0, -500)
        );
        SubScene subScene =
                new SubScene(
                        group,
                        500,
                        400, true, SceneAntialiasing.BALANCED);
        subScene.setCamera(camera);
        root = new Group(subScene);

        Scene scene = new Scene(root, 500, 400);

        primaryStage.setTitle("HUD: 2D Labels over 3D SubScene");
        primaryStage.setScene(scene);
        primaryStage.show();

        updateLabels(box);

        scene.setOnMousePressed(event -> {
            mousePosX = event.getSceneX();
            mousePosY = event.getSceneY();
        });

        scene.setOnMouseDragged(event -> {
            mousePosX = event.getSceneX();
            mousePosY = event.getSceneY();
            rotateX.setAngle(rotateX.getAngle() - (mousePosY - mouseOldY));
            rotateY.setAngle(rotateY.getAngle() + (mousePosX - mouseOldX));
            mouseOldX = mousePosX;
            mouseOldY = mousePosY;
            updateLabels(box);
        });
    }

    private List<Point3D> generateDots(Node box) {
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

    private void updateLabels(Node box) {
        root.getChildren().removeIf(Label.class::isInstance);
        AtomicInteger counter = new AtomicInteger(1);
        generateDots(box)
                .forEach(dot -> {
                    Point3D p2 = box.localToScene(dot, true);
                    Label label = new Label(
                            "" + counter.getAndIncrement() +
                                    String.format(" (%.1f,%.1f)",
                                            p2.getX(),
                                            p2.getY()));
                    label.setStyle("-fx-font-size:1.3em; -fx-text-fill: blue;");
                    label.getTransforms().setAll(new Translate(p2.getX(), p2.getY()));
                    root.getChildren().add(label);
                });
    }


    public static void main(String[] args) {
        launch(args);
    }

}
