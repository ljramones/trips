package com.teamgannon.trips.controller;

import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class ShowZoomWarning extends Dialog<Boolean> {

    private CheckBox checkBox = new CheckBox("Remove this warning");

    public ShowZoomWarning() {
        setTitle("Zoom Warning");
        setWidth(300);

        String warning =
                """
                        This button zooms in (or out) on the set of starPositionDescriptors currently selected.
                                               
                        If you want more, or fewer starPositionDescriptors, a larger, or smaller radius, go to
                        View / Select starPositionDescriptors to display or press (Some shortcut key) 
                        """;


        VBox vBox = new VBox();

        Label label = new Label(warning);
        vBox.getChildren().add(label);

        vBox.getChildren().add(checkBox);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        Button dismiss = new Button("Dismiss");
        hBox.getChildren().add(dismiss);
        dismiss.setOnAction(this::close);
        vBox.getChildren().add(hBox);
        this.getDialogPane().setContent(vBox);

        // set the dialog as a utility
        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(this::close);
    }

    private void close(WindowEvent windowEvent) {
        setResult(false);
    }

    private void close(ActionEvent actionEvent) {
        if (checkBox.isSelected()) {
            setResult(true);
        } else {
            setResult(false);
        }
    }

}
