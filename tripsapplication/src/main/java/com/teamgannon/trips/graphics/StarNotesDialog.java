package com.teamgannon.trips.graphics;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StarNotesDialog extends Dialog<String> {

    private final TextArea notesArea = new TextArea();

    public StarNotesDialog(String notesData) {
        VBox vBox = new VBox();

        notesArea.setText(notesData);

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(5);
        gridPane.setHgap(5);
        vBox.getChildren().add(gridPane);

        gridPane.add(notesArea, 1, 1);

        HBox hBox = new HBox();

        hBox.setAlignment(Pos.CENTER);

        Button resetBtn = new Button("Cancel");
        resetBtn.setOnAction(this::cancelClicked);
        hBox.getChildren().add(resetBtn);

        Button addBtn = new Button("Save");
        addBtn.setOnAction(this::saveClicked);
        hBox.getChildren().add(addBtn);

        vBox.getChildren().add(hBox);

        this.getDialogPane().setContent(vBox);
    }

    private void saveClicked(ActionEvent actionEvent) {
        setResult(notesArea.getText());
    }

    private void cancelClicked(ActionEvent actionEvent) {
        setResult("");
    }
}
