package com.teamgannon.trips.experiments;

import javafx.application.Application;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class SubsceneExample extends Application {

    private final double sceneWidth = 600;
    private final double sceneHeight = 600;

    private double mousePosX;
    private double mousePosY;

    @Override
    public void start(Stage stage) throws Exception {

        VBox root = new VBox();

        VBox controls = createControls();
        root.getChildren().add(controls);

        Pane scenePane = createPane();
        SubScene subScene = new SubScene(scenePane, sceneWidth, sceneHeight);
        subScene.setFill(Color.BLACK);
        handleMouseEvents(subScene);
        root.getChildren().add(subScene);

        Scene scene = new Scene(root, sceneWidth, sceneHeight);
        stage.setScene(scene);
        stage.setTitle("Subscene Example");
        stage.show();
    }

    private Pane createPane() {
        Pane pane = new Pane();
        pane.setPrefSize(sceneWidth, sceneHeight);
        pane.setMaxSize(Pane.USE_COMPUTED_SIZE, Pane.USE_COMPUTED_SIZE);
        pane.setMinSize(Pane.USE_COMPUTED_SIZE, Pane.USE_COMPUTED_SIZE);
        pane.setBackground(Background.EMPTY);
        return pane;
    }

    private VBox createControls() {
        HBox hBox = new HBox(new Button("Button A"));
        hBox.setAlignment(Pos.CENTER);
        VBox controls = new VBox(10, hBox);
        controls.setPadding(new Insets(10));
        return controls;
    }

    private void handleMouseEvents(SubScene subScene) {
        subScene.setOnMousePressed((MouseEvent me) -> {
                    mousePosX = me.getSceneX();
                    mousePosY = me.getSceneY();
                    Bounds ofParent = subScene.getBoundsInParent();
                    if (mousePosY >= ofParent.getMinY()) {
                        System.out.printf("press:: x=%.2f, y=%.2f%n", mousePosX - ofParent.getMinX(), mousePosY - ofParent.getMinY());
                    }
                }
        );

        subScene.setOnMouseDragged((MouseEvent me) -> {
                    mousePosX = me.getSceneX();
                    mousePosY = me.getSceneY();
                    Bounds ofParent = subScene.getBoundsInParent();
                    if (mousePosY >= ofParent.getMinY()) {
                        System.out.printf("drag:: x=%.2f, y=%.2f%n", mousePosX - ofParent.getMinX(), mousePosY - ofParent.getMinY());
                    }
                }
        );
    }

    public static void main(String[] args) {
        launch(args);
    }

}
