package com.teamgannon.trips.controller;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class RotationDialog extends Dialog<Boolean> {

    private RotationController rotationController;

    private final Button setAnglesButton = new Button("set Angles");
    private final Button resetButton = new Button("Reset view");
    private final Button dismissButton = new Button("Dismiss");

    private final TextField xAngleTextField = new TextField();
    private final TextField yAngleTextField = new TextField();
    private final TextField zAngleTextField = new TextField();

    private final Button plusX = new Button("+");
    private final Button plusY = new Button("+");
    private final Button plusZ = new Button("+");

    private final Button negX = new Button("-");
    private final Button negY = new Button("-");
    private final Button negZ = new Button("-");

    private double angleX = 0;
    private double angleY = 0;
    private double angleZ = 0;


    public RotationDialog(RotationController rotationController) {

        this.rotationController = rotationController;

        VBox vBox = new VBox();
        this.getDialogPane().setContent(vBox);

        GridPane gridPane = new GridPane();
        vBox.getChildren().add(gridPane);

        xAngleTextField.setText(Double.toString(angleX));
        gridPane.add(new Label("X Angle"), 0, 0);
        gridPane.add(xAngleTextField, 1, 0);
        gridPane.add(negX, 2, 0);
        gridPane.add(plusX, 3, 0);

        negX.setOnAction(this::negXaction);
        plusX.setOnAction(this::plusXaction);

        yAngleTextField.setText(Double.toString(angleY));
        gridPane.add(new Label("Y Angle"), 0, 1);
        gridPane.add(yAngleTextField, 1, 1);
        gridPane.add(negY, 2, 1);
        gridPane.add(plusY, 3, 1);

        negY.setOnAction(this::negYaction);
        plusY.setOnAction(this::plusYaction);

        zAngleTextField.setText(Double.toString(angleZ));
        gridPane.add(new Label("Z Angle"), 0, 2);
        gridPane.add(zAngleTextField, 1, 2);
        gridPane.add(negZ, 2, 2);
        gridPane.add(plusZ, 3, 2);

        negZ.setOnAction(this::negZaction);
        plusZ.setOnAction(this::plusZaction);

        HBox hBox = new HBox();
        vBox.getChildren().add(hBox);

        setAnglesButton.setOnAction(this::setAnglesClicked);
        hBox.getChildren().add(setAnglesButton);

        resetButton.setOnAction(this::resetView);
        hBox.getChildren().add(resetButton);

        dismissButton.setOnAction(this::close);
        hBox.getChildren().add(dismissButton);

        // set the dialog as a utility
        Stage stage1 = (Stage) this.getDialogPane().getScene().getWindow();
        stage1.setOnCloseRequest(this::close);
    }

    private void resetView(ActionEvent actionEvent) {
        angleX = 105;
        xAngleTextField.setText(Double.toString(angleX));
        angleY = 0;
        yAngleTextField.setText(Double.toString(angleY));
        angleZ = 30;
        zAngleTextField.setText(Double.toString(angleZ));
        rotationController.resetPosition();
    }

    private void plusZaction(ActionEvent actionEvent) {
        angleZ = angleZ + 5;
        zAngleTextField.setText(Double.toString(angleZ));
        rotationController.setRotationAngles(angleX, angleY, angleZ);
    }

    private void negZaction(ActionEvent actionEvent) {
        angleZ = angleZ - 5;
        zAngleTextField.setText(Double.toString(angleZ));
        rotationController.setRotationAngles(angleX, angleY, angleZ);
    }

    private void plusYaction(ActionEvent actionEvent) {
        angleY = angleY + 5;
        yAngleTextField.setText(Double.toString(angleY));
        rotationController.setRotationAngles(angleX, angleY, angleZ);
    }

    private void negYaction(ActionEvent actionEvent) {
        angleY = angleY - 5;
        yAngleTextField.setText(Double.toString(angleY));
        rotationController.setRotationAngles(angleX, angleY, angleZ);
    }

    private void plusXaction(ActionEvent actionEvent) {
        angleX = angleX + 5;
        xAngleTextField.setText(Double.toString(angleX));
        rotationController.setRotationAngles(angleX, angleY, angleZ);
    }

    private void negXaction(ActionEvent actionEvent) {
        angleX = angleX - 5;
        xAngleTextField.setText(Double.toString(angleX));
        rotationController.setRotationAngles(angleX, angleY, angleZ);
    }

    private void setAnglesClicked(ActionEvent actionEvent) {
        angleX = Double.parseDouble(xAngleTextField.getText());
        angleY = Double.parseDouble(yAngleTextField.getText());
        angleZ = Double.parseDouble(xAngleTextField.getText());
        rotationController.setRotationAngles(angleX, angleY, angleZ);
    }

    private void close(WindowEvent windowEvent) {
        setResult(true);
    }

    private void close(ActionEvent actionEvent) {
        setResult(true);
    }


}
