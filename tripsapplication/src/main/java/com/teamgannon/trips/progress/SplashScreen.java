package com.teamgannon.trips.progress;

import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;

public class SplashScreen extends Pane {

    public static final String APPLICATION_ICON =
            "http://cdn1.iconfinder.com/data/icons/Copenhagen/PNG/32/people.png";
    public static final String SPLASH_IMAGE =
            "http://fxexperience.com/wp-content/uploads/2010/06/logo.png";

    private static final int SPLASH_WIDTH = 676;
    private static final int SPLASH_HEIGHT = 227;

    private ProgressBar loadProgress;
    private Label progressText;
    private Pane splashLayout;


    public SplashScreen() {
        ImageView splash = new ImageView(new Image(
                SPLASH_IMAGE
        ));
        loadProgress = new ProgressBar();
        loadProgress.setPrefWidth(SPLASH_WIDTH - 20);
        progressText = new Label("Waiting to start . . .");
        splashLayout = new VBox();
        splashLayout.getChildren().addAll(splash, loadProgress, progressText);
        progressText.setAlignment(Pos.CENTER);
        splashLayout.setStyle(
                "-fx-padding: 5; " +
                        "-fx-background-color: cornsilk; " +
                        "-fx-border-width:5; " +
                        "-fx-border-color: " +
                        "linear-gradient(" +
                        "to bottom, " +
                        "chocolate, " +
                        "derive(chocolate, 50%)" +
                        ");"
        );
        splashLayout.setEffect(new DropShadow());
        this.getChildren().add(splashLayout);
    }

    public void set(@NotNull Task<?> task) {
        progressText.textProperty().bind(task.messageProperty());
        loadProgress.progressProperty().bind(task.progressProperty());
    }

    public void finish() {
        loadProgress.progressProperty().unbind();
        loadProgress.setProgress(1);
    }

}
