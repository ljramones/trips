package com.teamgannon.trips.dialogs.utility;

import com.teamgannon.trips.algorithms.StarMath;
import com.teamgannon.trips.utility.DialogUtils;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EquatorialToGalacticCoordsDialog extends Dialog<Boolean> {

    private final TextField equX = new TextField();
    private final TextField equY = new TextField();
    private final TextField equZ = new TextField();

    private final TextField galX = new TextField();
    private final TextField galY = new TextField();
    private final TextField galZ = new TextField();

    private final RadioButton radioButton1 = new RadioButton("J1950");
    private final RadioButton radioButton2 = new RadioButton("J2000");
    private final ToggleGroup radioGroup = new ToggleGroup();

    public EquatorialToGalacticCoordsDialog() {
        // set the dialog as a utility
        DialogUtils.bindCloseHandler(this, this::close);

        this.setTitle("Calculate between Equatorial and Galactic Coordinates");
        this.setHeight(600);
        this.setWidth(800);

        Insets insets1 = new Insets(6.0, 6.0, 6.0, 6.0);
        Font font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 13);

        VBox vBox = new VBox(5);
        vBox.setPadding(insets1);
        this.getDialogPane().setContent(vBox);

        GridPane gridPane = new GridPane();
        gridPane.setPadding(insets1);
        vBox.getChildren().add(gridPane);

        GridPane leftPane = new GridPane();
        leftPane.setPadding(insets1);
        leftPane.setStyle("-fx-padding: 10;" +
                "-fx-border-style: solid inside;" +
                "-fx-border-width: 2;" +
                "-fx-border-insets: 5;" +
                "-fx-border-radius: 5;" +
                "-fx-border-color: blue;");
        gridPane.add(leftPane, 0, 0);

        Label equXLabel = new Label("X: ");
        equXLabel.setFont(font);
        leftPane.add(equXLabel, 0, 0);
        leftPane.add(equX, 1, 0);

        Label equYLabel = new Label("Y: ");
        equYLabel.setFont(font);
        leftPane.add(equYLabel, 0, 1);
        leftPane.add(equY, 1, 1);

        Label equZlabel = new Label("Z: ");
        equXLabel.setFont(font);
        leftPane.add(equZlabel, 0, 2);
        leftPane.add(equZ, 1, 2);

        GridPane middlePane = new GridPane();
        middlePane.setPadding(insets1);
        gridPane.add(middlePane, 1, 0);

        radioButton2.setSelected(true);
        radioButton1.setToggleGroup(radioGroup);
        radioButton2.setToggleGroup(radioGroup);
        middlePane.add(radioButton1, 0, 0);
        middlePane.add(radioButton2, 1, 0);
        middlePane.add(new Label("    "), 0,1,2,1);

        Button toEquatorial = new Button("<--Convert to Equatorial");
        toEquatorial.setFont(font);
        toEquatorial.setOnAction(this::convertToEquatorial);
        middlePane.add(toEquatorial, 0, 2, 2, 1);

        Button toGalactic = new Button("Convert to Galactic-->");
        toGalactic.setOnAction(this::convertToGalactic);
        toGalactic.setFont(font);
        middlePane.add(toGalactic, 0, 3, 2, 1);

        GridPane rightPane = new GridPane();
        rightPane.setStyle("-fx-padding: 10;" +
                "-fx-border-style: solid inside;" +
                "-fx-border-width: 2;" +
                "-fx-border-insets: 5;" +
                "-fx-border-radius: 5;" +
                "-fx-border-color: blue;");
        rightPane.setPadding(insets1);
        gridPane.add(rightPane, 2, 0);

        Label galXLabel = new Label("X: ");
        galXLabel.setFont(font);
        rightPane.add(galXLabel, 0, 0);
        rightPane.add(galX, 1, 0);

        Label galYLabel = new Label("Y: ");
        galYLabel.setFont(font);
        rightPane.add(galYLabel, 0, 1);
        rightPane.add(galY, 1, 1);

        Label galZLabel = new Label("Z: ");
        galZLabel.setFont(font);
        rightPane.add(galZLabel, 0, 2);
        rightPane.add(galZ, 1, 2);

        // button box
        HBox hBox = new HBox(5);
        hBox.setPadding(insets1);
        hBox.setAlignment(Pos.CENTER);
        vBox.getChildren().add(hBox);

        Button acceptButton = new Button("Dismiss");
        acceptButton.setFont(font);
        acceptButton.setOnAction(this::dismiss);
        hBox.getChildren().add(acceptButton);

        gridPane.add(hBox, 0, 7, 3, 1);

    }

    private void convertToGalactic(ActionEvent actionEvent) {
        if (!equX.getText().trim().isEmpty() &&
                !equY.getText().trim().isEmpty() &&
                !equZ.getText().trim().isEmpty()) {
            double x = Double.parseDouble(equX.getText());
            double y = Double.parseDouble(equY.getText());
            double z = Double.parseDouble(equZ.getText());
            double[] equatorial = new double[]{x, y, z};
            double[] galactic;
            if (getConversionType().equals("J1950")) {
                log.info("Using J1950 epoch");
                galactic = StarMath.epoch1950ToGalacticCoordinates(equatorial);
            } else {
                log.info("Using J2000 epoch");
                galactic = StarMath.epoch2000ToGalacticCoordinates(equatorial);
            }
            galX.setText(String.format("%.4f", galactic[0]));
            galY.setText(String.format("%.4f", galactic[1]));
            galZ.setText(String.format("%.4f", galactic[2]));
        }
    }

    private void convertToEquatorial(ActionEvent actionEvent) {
        if (!galX.getText().trim().isEmpty() &&
                !galY.getText().trim().isEmpty() &&
                !galZ.getText().trim().isEmpty()) {
            double x = Double.parseDouble(galX.getText());
            double y = Double.parseDouble(galX.getText());
            double z = Double.parseDouble(galZ.getText());
            double[] galactic = new double[]{x, y, z};
            double[] equatorial = StarMath.galacticToEquatorialCoordinates(galactic);
            equX.setText(String.format("%.4f", equatorial[0]));
            equY.setText(String.format("%.4f", equatorial[1]));
            equZ.setText(String.format("%.4f", equatorial[2]));
        }
    }

    private String getConversionType() {
        RadioButton selectedRadioButton = (RadioButton) radioGroup.getSelectedToggle();
        return selectedRadioButton.getText();
    }

    private void dismiss(ActionEvent actionEvent) {
        setResult(true);
    }

    private void close(WindowEvent windowEvent) {
        setResult(true);
        close();
    }

}
