package com.teamgannon.trips.experiments.javafxservice;


import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * @author jdeters
 */
public class ServiceDemoApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        ServiceDemoController root = new ServiceDemoController();
        root.initialize();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}