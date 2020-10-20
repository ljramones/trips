package com.teamgannon.trips.floating;

import com.teamgannon.trips.algorithms.Universe;
import com.teamgannon.trips.config.application.model.ColorPalette;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class RunExample extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception {

        double sceneWidth = Universe.boxWidth;
        double sceneHeight = Universe.boxHeight;
        double depth = Universe.boxDepth;

        ColorPalette colorPalette = new ColorPalette();
        colorPalette.setDefaults();

        double spacing = 20;

        InterstellarExample interstellarExample = new InterstellarExample(
                sceneWidth, sceneHeight, depth, spacing, colorPalette
        );

        Scene scene = new Scene(interstellarExample.getRoot(), sceneWidth, sceneHeight);
        primaryStage.setTitle("2D Labels over 3D SubScene");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
