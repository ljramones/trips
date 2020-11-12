package com.teamgannon.trips.experiments;

import com.teamgannon.trips.progress.ProgressLoader;
import javafx.application.Application;
import javafx.stage.Stage;

public class TestLoader extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        ProgressLoader loader = new ProgressLoader(null, null, null);
        loader.start(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
