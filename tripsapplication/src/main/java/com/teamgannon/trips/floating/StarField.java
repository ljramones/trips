package com.teamgannon.trips.floating;

import com.teamgannon.trips.starplotting.LabelDescriptor;
import javafx.application.Application;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StarField extends Application {

    private final Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);

    private double mousePosX;
    private double mousePosY;
    private double mouseOldX;
    private double mouseOldY;

    private Group root;

    private Random random = new Random();

    private double radiusRange = 7;

    private double xMax = 200;
    private double yMax = 200;
    private double zMax = 200;

    Group displayStarGroup = new Group();
    Group labelGroup = new Group();

    List<Node> labelNodes = new ArrayList<>();


    @Override
    public void start(Stage primaryStage) {

        generateRandomStars(20);

        Group group = new Group();
        group.getChildren().add(displayStarGroup);
        group.getChildren().add(labelGroup);

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setFieldOfView(20);
        camera.getTransforms().addAll(rotateX, rotateY, new Translate(0, 0, -1000));

        SubScene subScene = new SubScene(group, 500, 400, true, SceneAntialiasing.BALANCED);
        subScene.setCamera(camera);
        subScene.setFill(Color.BLACK);
        root = new Group(subScene);

        Scene scene = new Scene(root, 500, 400);

        primaryStage.setTitle("HUD: 2D Labels over 3D SubScene");
        primaryStage.setScene(scene);
        primaryStage.show();

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
            updateLabels();
        });

    }

    private void updateLabels() {
        labelGroup.getChildren().clear();
        for (Node star : displayStarGroup.getChildren()) {
            LabelDescriptor labelDescriptor = (LabelDescriptor) star.getUserData();
            double x = star.getTranslateX();
            double y = star.getTranslateY();
            double z = star.getTranslateZ();
            Point3D origin = new Point3D(x, y, z);

            Label label = new Label(labelDescriptor.getText());
            label.setTextFill(Color.WHEAT);
            label.setTranslateX(x);
            label.setTranslateY(y);
            label.setTranslateZ(z);
            label.getTransforms().setAll(new Rotate(rotateX.getAngle(), origin), new Rotate(rotateY.getAngle(), origin));
            //            Point3D p2 = star.localToScene(origin, true);
            //            label.getTransforms().setAll(new Translate(p2.getX(), p2.getY()));
            labelGroup.getChildren().add(label);
        }
    }

    private void generateRandomStars(int n) {
        for (int i = 0; i < n; i++) {

            final PhongMaterial material = new PhongMaterial();
            Color color = randomColor();
            material.setDiffuseColor(color);
            material.setSpecularColor(color);

            double x = random.nextDouble() * xMax * 2 / 3 * (random.nextBoolean() ? 1 : -1);
            double y = random.nextDouble() * yMax * 2 / 3 * (random.nextBoolean() ? 1 : -1);
            double z = random.nextDouble() * zMax * 2 / 3 * (random.nextBoolean() ? 1 : -1);

            Sphere sphere = new Sphere(random.nextDouble() * radiusRange);
            sphere.setMaterial(material);
            sphere.setTranslateX(x);
            sphere.setTranslateY(y);
            sphere.setTranslateZ(z);

            Label label = new Label("Star:" + i);
            label.setTranslateX(x);
            label.setTranslateY(y);
            label.setTranslateZ(z);
            label.setTextFill(Color.WHEAT);
            label.setLabelFor(sphere);
            labelGroup.getChildren().add(label);
            labelNodes.add(label);

            LabelDescriptor descriptor = LabelDescriptor
                    .builder()
                    .labelLocation(new Point3D(x, y, z))
                    .text(label.getText())
                    .build();
            sphere.setUserData(descriptor);

            displayStarGroup.getChildren().add(sphere);
        }
    }

    private Color randomColor() {
        int r = random.nextInt(255);
        int g = random.nextInt(255);
        int b = random.nextInt(255);
        return Color.rgb(r, g, b);
    }

}
