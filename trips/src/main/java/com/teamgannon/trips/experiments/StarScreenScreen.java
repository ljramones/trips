package com.teamgannon.trips.experiments;

import com.teamgannon.trips.experiments.elements.CombinedStarPane;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StarScreenScreen extends Application {

    private Scene scene;

    /**
     * The main entry point for all JavaFX applications.
     * The start method is called after the init method has returned,
     * and after the system is ready for the application to begin running.
     *
     * <p>
     * NOTE: This method is called on the JavaFX Application Thread.
     * </p>
     *
     * @param primaryStage the primary primaryStage for this application, onto which
     *                     the application scene can be set. The primary primaryStage will be embedded in
     *                     the browser if the application was launched as an applet.
     *                     Applications may create other stages, if needed, but they will not be
     *                     primary stages and will not be embedded in the browser.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setResizable(false);

        int width = 1100;
        int height = 700;
        int depth = 700;
        int spacing = 20;

        CombinedStarPane pane = new CombinedStarPane(
                width, height, depth, spacing
        );

        scene = new Scene(pane, width, height);
        log.info("Size of Scene is x({}), y({})", scene.getWidth(), scene.getHeight());

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }


}
