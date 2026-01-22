package com.teamgannon.trips.routing.dialogs;


import com.teamgannon.trips.routing.RoutingConstants;
import com.teamgannon.trips.service.measure.PerformanceMeasure;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class SearchPerformanceDialog extends Dialog<Boolean> {

    public SearchPerformanceDialog(PerformanceMeasure performanceMeasure) {

        this.setTitle("Tell me what the Expected Time of Execution");
        this.setWidth(400);
        this.setHeight(400);

        VBox vBox = new VBox();

        // define buttons
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);

        GridPane gridPane = new GridPane();
        gridPane.setMinWidth(380);
        vBox.getChildren().add(gridPane);

        Label memoryLabel = new Label("1. Memory Available: \n(should be greater than 2 Gb)");
        memoryLabel.setFont(RoutingConstants.createInfoFont());
        gridPane.add(memoryLabel, 0, 0);
        gridPane.add(new Label(String.format("%,d Gb", performanceMeasure.getMemorySize())), 1, 0);

        Label numberProcessorsLabel = new Label("2. # Processors:\n (should be at least 2) ");
        numberProcessorsLabel.setFont(RoutingConstants.createInfoFont());
        gridPane.add(numberProcessorsLabel, 0, 1);
        gridPane.add(new Label(String.format("%,d", performanceMeasure.getNumberProcessors())), 1, 1);

        Label numberStarsLabel = new Label("3. # Stars: ");
        numberStarsLabel.setFont(RoutingConstants.createInfoFont());
        gridPane.add(numberStarsLabel, 0, 2);
        gridPane.add(new Label(String.format("%,d", performanceMeasure.getNumbersOfStars())), 1, 2);

        Label distanceLabel = new Label("4. Distance: ");
        distanceLabel.setFont(RoutingConstants.createInfoFont());
        gridPane.add(distanceLabel, 0, 3);
        gridPane.add(new Label(String.format("%,.2f ly", performanceMeasure.getDistance())), 1, 3);

        Label worseCasePathsLabel = new Label("5. # Worse Case Paths: ");
        worseCasePathsLabel.setFont(RoutingConstants.createInfoFont());
        gridPane.add(worseCasePathsLabel, 0, 4);
        gridPane.add(new Label(String.format("%,d", performanceMeasure.getWorseCasePaths())), 1, 4);

        Label timeToExecuteLabel = new Label("6. Expected Execution Time: ");
        timeToExecuteLabel.setFont(RoutingConstants.createInfoFont());
        gridPane.add(timeToExecuteLabel, 0, 5);
        gridPane.add(new Label(String.format("%,.2f seconds", performanceMeasure.getTimeToDoRouteSearch())), 1, 5);

        Button dismissBtn = new Button("Dismiss");
        dismissBtn.setDisable(false);
        dismissBtn.setOnAction(this::dismissClicked);
        hBox.getChildren().add(dismissBtn);
        vBox.getChildren().add(hBox);

        this.getDialogPane().setContent(vBox);

        // set the dialog as a utility
        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(this::close);
        stage.setAlwaysOnTop(true);
        stage.toFront(); // not sure if necessary
    }

    private void close(WindowEvent windowEvent) {
        setResult(Boolean.TRUE);
    }

    private void dismissClicked(ActionEvent actionEvent) {
        setResult(Boolean.TRUE);
    }

}
