package com.teamgannon.trips.dead.examples;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;

import javafx.application.Application;

import javafx.geometry.VPos;

import javafx.scene.Group;
import javafx.scene.Scene;

import javafx.scene.effect.Reflection;

import javafx.scene.paint.Color;

import javafx.scene.text.Font;
import javafx.scene.text.Text;

import javafx.stage.Stage;

import javafx.util.Duration;

public class RotateTDemo extends Application
{
    final static int SCENE_WIDTH = 300;
    final static int SCENE_HEIGHT = 300;

    @Override
    public void start(Stage primaryStage)
    {
        Text text = new Text();
        text.setText("JavaFX RotateTransition Demo");
        text.setFont(new Font("Arial Bold", 16));
        text.setTextOrigin(VPos.TOP);
        text.setX(SCENE_WIDTH / 2);
        text.setY(SCENE_HEIGHT / 2);
        text.setTranslateX(-text.layoutBoundsProperty().get().getWidth() / 2);
        text.setTranslateY(-text.layoutBoundsProperty().get().getHeight() / 2);
        text.setEffect(new Reflection());

        RotateTransition rt = new RotateTransition();
        rt.setNode(text);
        rt.setFromAngle(0);
        rt.setToAngle(360);
        rt.setInterpolator(Interpolator.LINEAR);
        rt.setCycleCount(Timeline.INDEFINITE);
        rt.setDuration(new Duration(3000));

        Group root = new Group();
        root.getChildren().add(text);
        Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT, Color.ORANGE);
        scene.setOnMouseClicked(me ->
        {
            Animation.Status status = rt.getStatus();
            if (status == Animation.Status.RUNNING &&
                    status != Animation.Status.PAUSED)
                rt.pause();
            else
                rt.play();
        });

        primaryStage.setTitle("RotateTransition Demo");
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setResizable(false);
    }
}