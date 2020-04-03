package com.teamgannon.trips.dialog.example;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class BorderExample2 extends Application {
    //Declare label controls for the different BorderPane areas
    final Label topLabel = new Label("Top Pane");
    final Label leftLabel = new Label("Left Pane");
    final Label rightLabel = new Label("Right Pane");
    final Label centerLabel = new Label("Center Pane");
    final Label bottomLabel = new Label("Bottom Pane");

    @Override
    public void start(Stage primaryStage) {
//The scene will have a VBox containing
//a HBox and a BorderPabe
        VBox root = new VBox(10);
        HBox showControls = new HBox(10);
        final BorderPane controlLayout = new BorderPane();
//Set the size of the BorderPane and show its borders
//by making them black
        controlLayout.setPrefSize(600, 400);
        controlLayout.setStyle("-fx-border-color: black;");
//Call the setLabelVisible method which sets one label to be visible
//and the others to be hidden
        setLabelVisible("Top");
//Put each label in its correponding BorderPane area
        controlLayout.setTop(topLabel);
        controlLayout.setLeft(leftLabel);
        controlLayout.setRight(rightLabel);
        controlLayout.setCenter(centerLabel);
        controlLayout.setBottom(bottomLabel);
//Align the labels to be in the center of their BorderPane
//area
        controlLayout.setAlignment(topLabel, Pos.CENTER);
        controlLayout.setAlignment(centerLabel, Pos.CENTER);
        controlLayout.setAlignment(bottomLabel, Pos.CENTER);
//Create a ChoiceBox to hold the BorderPane area names
        final ChoiceBox panes = new ChoiceBox();
        panes.getItems().addAll("Top", "Left", "Right", "Center", "Bottom");
        panes.setValue("Top");
//Create a button to trigger which label is visible
        Button moveBut = new Button("Show Pane");
        moveBut.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
//Call the setLabelVisible method to set the
//correct label to be visible based on the
//value of the ChoiceBox
                setLabelVisible(panes.getValue().toString());
            }
        });
//Add the Button and ChoiceBox to the HBox
        showControls.getChildren().add(moveBut);
        showControls.getChildren().add(panes);
//Add the HBox and BorderPane to the VBOx
        root.getChildren().add(showControls);
        root.getChildren().add(controlLayout);
        Scene scene = new Scene(root, 600, 500);
        primaryStage.setTitle("BorderPane Layout Example");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    //A simple method which changes the visibility of the
//labels depending on the string passed
    public void setLabelVisible(String labelName) {
        switch (labelName) {
            case "Top":
                topLabel.setVisible(true);
                leftLabel.setVisible(false);
                rightLabel.setVisible(false);
                centerLabel.setVisible(false);
                bottomLabel.setVisible(false);
                break;
            case "Left":
                topLabel.setVisible(false);
                leftLabel.setVisible(true);
                rightLabel.setVisible(false);
                centerLabel.setVisible(false);
                bottomLabel.setVisible(false);
                break;
            case "Right":
                topLabel.setVisible(false);
                leftLabel.setVisible(false);
                rightLabel.setVisible(true);
                centerLabel.setVisible(false);
                bottomLabel.setVisible(false);
                break;
            case "Center":
                topLabel.setVisible(false);
                leftLabel.setVisible(false);
                rightLabel.setVisible(false);
                centerLabel.setVisible(true);
                bottomLabel.setVisible(false);
                break;
            case "Bottom":
                topLabel.setVisible(false);
                leftLabel.setVisible(false);
                rightLabel.setVisible(false);
                centerLabel.setVisible(false);
                bottomLabel.setVisible(true);
                break;
            default:
                break;
        }
        ;
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}