package com.teamgannon.trips.routing.dialogs.components;

import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class ColorChoiceDialog extends Dialog<ColorChoice> {

    ToggleGroup colorGroup = new ToggleGroup();

    public ColorChoiceDialog() {
        // set the dialog as a utility
        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(this::close);
        stage.toFront();
        stage.setAlwaysOnTop(true);

        this.setTitle("Select a Color");
        this.setHeight(400);
        this.setWidth(400);

        VBox vBox = new VBox(6);
        this.getDialogPane().setContent(vBox);
        GridPane gridPane = new GridPane();
        vBox.getChildren().add(gridPane);

        // setup colors
        RadioButton greenButton = new RadioButton("Aquamarine");
        colorGroup.getToggles().add(greenButton);
        greenButton.setTextFill(Color.AQUAMARINE);
        gridPane.add(greenButton, 0, 0);

        RadioButton blueVioletButton = new RadioButton("Blue Violet");
        colorGroup.getToggles().add(blueVioletButton);
        blueVioletButton.setTextFill(Color.BLUEVIOLET);
        gridPane.add(blueVioletButton, 1, 0);

        RadioButton blueButton = new RadioButton("Green");
        colorGroup.getToggles().add(blueButton);
        blueButton.setTextFill(Color.GREEN);
        gridPane.add(blueButton, 2, 0);

        RadioButton yellowButton = new RadioButton("Yellow");
        colorGroup.getToggles().add(yellowButton);
        yellowButton.setTextFill(Color.YELLOW);
        gridPane.add(yellowButton, 0, 1);

        RadioButton redButton = new RadioButton("Yellow");
        colorGroup.getToggles().add(redButton);
        redButton.setTextFill(Color.RED);
        gridPane.add(redButton, 1, 1);

        // setup acceptance button
        HBox hBox = new HBox(6);
        vBox.getChildren().add(hBox);
        Button dismissButton = new Button("Ok");
        dismissButton.setOnAction(this::dismiss);
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().add(dismissButton);
    }

    private void dismiss(ActionEvent actionEvent) {
        Color color = getSelectedColor();
        setResult(ColorChoice.builder().selected(true).swatch(color).build());
    }

    private Color getSelectedColor() {
        RadioButton radioButton = (RadioButton) colorGroup.getSelectedToggle();
        String colorString = radioButton.getText();
        return switch (colorString) {
            case "Aquamarine" -> Color.AQUAMARINE;
            case "Blue Violet" -> Color.BLUEVIOLET;
            case "Green" -> Color.GREEN;
            case "Yellow" -> Color.YELLOW;
            case "Red" -> Color.RED;
            default -> Color.CYAN;
        };
    }

    private void close(WindowEvent windowEvent) {
        setResult(ColorChoice.builder().selected(false).build());
    }

}
