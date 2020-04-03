package com.teamgannon.trips.experiments;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LabelsContainer extends Application {


    @Override
    public void start(Stage primaryStage) {

        NewFloatingLabels labels = new NewFloatingLabels(500, 400);
        Scene scene = new Scene(labels, 500, 400);

        primaryStage.setTitle("HUD: 2D Labels over 3D SubScene");
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }

}
