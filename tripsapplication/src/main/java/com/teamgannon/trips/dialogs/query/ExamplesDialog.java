package com.teamgannon.trips.dialogs.query;

import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.List;
import java.util.stream.Collectors;

public class ExamplesDialog extends Dialog<Boolean> {

    public ExamplesDialog(List<String> examples) {

        String text = examples.stream().map(item -> item + "\n").collect(Collectors.joining());
        Font font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 13);

        VBox vBox = new VBox();
        this.getDialogPane().setContent(vBox);

        Label titleLabel = new Label("SQL Examples");
        titleLabel.setFont(font);
        vBox.getChildren().add(titleLabel);
        TextArea examplesTextArea = new TextArea();
        vBox.getChildren().add(examplesTextArea);
        examplesTextArea.setText(text);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        vBox.getChildren().add(hBox);
        Button dismissBtn = new Button("Dismiss");
        dismissBtn.setOnAction(this::close);
        hBox.getChildren().add(dismissBtn);

        // set the dialog as a utility
        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(this::close);
    }

    private void close(ActionEvent actionEvent) {
        setResult(true);
    }

    private void close(WindowEvent windowEvent) {
        setResult(true);
    }

}
