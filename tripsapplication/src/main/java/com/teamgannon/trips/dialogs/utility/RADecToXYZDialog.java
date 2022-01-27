package com.teamgannon.trips.dialogs.utility;

import com.teamgannon.trips.algorithms.StarMath;
import com.teamgannon.trips.dialogs.utility.model.RADecXYZObject;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;


public class RADecToXYZDialog extends Dialog<RADecXYZObject> {

    private RADecXYZObject raDecXYZObject = RADecXYZObject.builder().calculated(false).build();

    private final Font font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 13);

    private final TextField raTextField = new TextField();
    private final TextField decTextField = new TextField();
    private final TextField distanceTextField = new TextField();

    private final Label xLabel = new Label("0.0");
    private final Label yLabel = new Label("0.0");
    private final Label zLabel = new Label("0.0");

    public RADecToXYZDialog() {

        // set the dialog as a utility
        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(this::close);

        this.setTitle("Calculate the Coordinates from RA, Declination, Distance");
        this.setHeight(600);
        this.setWidth(500);

        Insets insets1 = new Insets(6.0, 6.0, 6.0, 6.0);

        VBox vBox = new VBox(5);
        this.getDialogPane().setContent(vBox);

        GridPane gridPane = new GridPane();
        gridPane.setPadding(insets1);
        vBox.getChildren().add(gridPane);

        Label rightAscensionLabel = new Label("Right Ascension (RA): ");
        rightAscensionLabel.setFont(font);
        gridPane.add(rightAscensionLabel, 0, 0);
        gridPane.add(raTextField, 1, 0);

        Label decAscensionLabel = new Label("Declination: ");
        decAscensionLabel.setFont(font);
        gridPane.add(decAscensionLabel, 0, 1);
        gridPane.add(decTextField, 1, 1);

        Label distanceTextLabel = new Label("Distance: ");
        distanceTextLabel.setFont(font);
        gridPane.add(distanceTextLabel, 0, 2);
        gridPane.add(distanceTextField, 1, 2);

        Label xxLabel = new Label("X: ");
        xxLabel.setFont(font);
        gridPane.add(xxLabel, 0, 3);
        gridPane.add(xLabel, 1, 3);

        Label yyLabel = new Label("Y: ");
        yyLabel.setFont(font);
        gridPane.add(yyLabel, 0, 4);
        gridPane.add(yLabel, 1, 4);

        Label zzLabel = new Label("Z: ");
        zzLabel.setFont(font);
        gridPane.add(zzLabel, 0, 5);
        gridPane.add(zLabel, 1, 5);

        HBox hBox = new HBox(5);
        hBox.setPadding(insets1);
        hBox.setAlignment(Pos.CENTER);
        vBox.getChildren().add(hBox);

        Button calculateCoordinatesButton = new Button("Calculate Coordinates");
        calculateCoordinatesButton.setOnAction(this::calculate);
        hBox.getChildren().add(calculateCoordinatesButton);

        Button acceptButton = new Button("Accept");
        acceptButton.setOnAction(this::accept);
        hBox.getChildren().add(acceptButton);

        Button cancelDataSetButton = new Button("Cancel");
        cancelDataSetButton.setOnAction(this::close);
        hBox.getChildren().add(cancelDataSetButton);

        gridPane.add(hBox, 0, 6, 2, 1);

    }


    private void calculate(ActionEvent actionEvent) {
        try {
            raDecXYZObject.setRightAscension(Double.parseDouble(raTextField.getText()));
            raDecXYZObject.setDeclination(Double.parseDouble(decTextField.getText()));
            raDecXYZObject.setDistance(Double.parseDouble(distanceTextField.getText()));
            double[] coordinates = StarMath.getPosition(raDecXYZObject.getRightAscension(), raDecXYZObject.getDeclination(), raDecXYZObject.getDistance());
            raDecXYZObject.setCoordinates(coordinates);
            xLabel.setText(String.format("%.4f", coordinates[0]));
            yLabel.setText(String.format("%.4f",coordinates[1]));
            zLabel.setText(String.format("%.4f",coordinates[2]));
        } catch (NumberFormatException nfe) {
            showErrorAlert("Calculate XYZ", "Entered value is not a double!");
        }
    }

    private void accept(ActionEvent actionEvent) {
        setResult(raDecXYZObject);
    }

    private void close(ActionEvent actionEvent) {
        RADecXYZObject object = RADecXYZObject
                .builder()
                .calculated(false)
                .build();
        setResult(raDecXYZObject);
    }

    private void close(WindowEvent windowEvent) {
        RADecXYZObject object = RADecXYZObject
                .builder()
                .calculated(false)
                .build();
        setResult(raDecXYZObject);
    }

}
