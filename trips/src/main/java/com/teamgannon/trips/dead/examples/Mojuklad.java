package com.teamgannon.trips.dead.examples;

import javafx.animation.Interpolator;
import javafx.animation.PathTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Sphere;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Mojuklad extends Application {
    public Sphere earth;
    public Sphere moon;

    @Override
    public void start(Stage primaryStage) {
//        Button btn = new Button();
//        btn.setText("Say 'Hello World'");
//        btn.setOnAction(new EventHandler<ActionEvent>() {
//
//            @Override
//            public void handle(ActionEvent event) {
//                System.out.println("Hello World!");
//            }
//        });
        Sphere star = new Sphere(45);
        earth = new Sphere(45);
        moon = new Sphere(25);

        Ellipse ellipseEarth = new Ellipse();
        ellipseEarth.setCenterX(star.getTranslateX());
        ellipseEarth.setCenterY(star.getTranslateY());
        ellipseEarth.translateXProperty().bind(star.translateXProperty());
        ellipseEarth.translateYProperty().bind(star.translateYProperty());
        ellipseEarth.setRadiusX(star.getBoundsInLocal().getWidth() / 2.0 +
                1.01671388 * 70);
        ellipseEarth.setRadiusY(star.getBoundsInLocal().getHeight() / 2.0 +
                0.98329134 * 70);

        PathTransition transitionEarth = new PathTransition();
        transitionEarth.setPath(ellipseEarth);
        transitionEarth.setNode(earth);
        transitionEarth.setInterpolator(Interpolator.LINEAR);
        transitionEarth.setDuration(Duration.seconds(10.000017421));
        transitionEarth.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
        transitionEarth.setCycleCount(Timeline.INDEFINITE);

        transitionEarth.play();


        Ellipse ellipseMoon = new Ellipse();
        ellipseMoon.setCenterX(earth.getTranslateX());
        ellipseMoon.setCenterY(earth.getTranslateY());
        ellipseMoon.translateXProperty().bind(earth.translateXProperty());
        ellipseMoon.translateYProperty().bind(earth.translateYProperty());
        ellipseMoon.setRadiusX(earth.getBoundsInLocal().getWidth() / 2.0 +
                1.01671388 * 70);
        ellipseMoon.setRadiusY(earth.getBoundsInLocal().getHeight() / 2.0 +
                0.98329134 * 70);
        // ellipse.setStrokeWidth(5);

        PathTransition transitionMoon = new PathTransition();
        transitionMoon.setPath(ellipseMoon);
        transitionMoon.setNode(moon);
        transitionMoon.setInterpolator(Interpolator.LINEAR);
        transitionMoon.setDuration(Duration.seconds(1.000017421));
        transitionMoon.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
        transitionMoon.setCycleCount(Timeline.INDEFINITE);
        transitionMoon.play();


        StackPane root = new StackPane();

        root.getChildren().add(star);

        root.getChildren().add(ellipseMoon);
        root.getChildren().add(ellipseEarth);
        root.getChildren().add(earth);

        root.getChildren().add(moon);
        Scene scene = new Scene(root, 800, 600);

        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}