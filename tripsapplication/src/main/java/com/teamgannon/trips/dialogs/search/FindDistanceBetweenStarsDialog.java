package com.teamgannon.trips.dialogs.search;

import com.teamgannon.trips.dialogs.search.model.DistanceRoutes;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

public class FindDistanceBetweenStarsDialog extends Dialog<DistanceRoutes> {

    private final TextField distanceMeasure = new TextField();

    public FindDistanceBetweenStarsDialog() {

        this.setTitle("Find a star in current view");
        this.setHeight(300);
        this.setWidth(500);

        VBox vBox = new VBox();
        Font font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 13);
        vBox.getChildren().add(new Separator());

        HBox hBox = new HBox();

        Label distanceLabel = new Label("Select Distance Between Stars:  ");
        distanceLabel.setFont(font);

        hBox.getChildren().add(distanceLabel);
        hBox.getChildren().add(distanceMeasure);
        hBox.setAlignment(Pos.CENTER);
        vBox.getChildren().add(hBox);

        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(10.0);

        HBox hBox1 = new HBox();
        hBox1.setAlignment(Pos.CENTER);
        vBox.getChildren().add(hBox1);

        Button gotToStarButton = new Button("Select Distance");
        gotToStarButton.setOnAction(this::goToStarClicked);
        hBox1.getChildren().add(gotToStarButton);

        Button cancelDataSetButton = new Button("Dismiss");
        cancelDataSetButton.setOnAction(this::close);
        hBox1.getChildren().add(cancelDataSetButton);

        this.getDialogPane().setContent(vBox);


        // set the dialog as a utility
        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(this::close);
    }

    private void close(WindowEvent windowEvent) {
        DistanceRoutes distanceRoutes = DistanceRoutes.builder().selected(false).build();
        setResult(distanceRoutes);
    }

    private void close(ActionEvent actionEvent) {
        DistanceRoutes distanceRoutes = DistanceRoutes.builder().selected(false).build();
        setResult(distanceRoutes);
    }

    private void goToStarClicked(ActionEvent actionEvent) {
        try {
            double distance = Double.parseDouble(distanceMeasure.getText());
            DistanceRoutes distanceRoutes = DistanceRoutes.builder().distance(distance).selected(true).build();
            setResult(distanceRoutes);
        } catch (NumberFormatException nfe) {
            showErrorAlert("Find Distance Between Stars", "Not a valid floating point:" + distanceMeasure.getText());
        }
    }

}
