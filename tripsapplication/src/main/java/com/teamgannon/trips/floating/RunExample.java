package com.teamgannon.trips.floating;

import com.teamgannon.trips.algorithms.Universe;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class RunExample extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception {

        double sceneWidth = Universe.boxWidth;
        double sceneHeight = Universe.boxHeight;

        InterstellarExample interstellarExample = new InterstellarExample(sceneWidth, sceneHeight);
        Scene scene = new Scene(interstellarExample.getRoot(), sceneWidth, sceneHeight);
        primaryStage.setTitle("2D Labels over 3D SubScene");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
