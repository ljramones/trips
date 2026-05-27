package com.teamgannon.trips.graphics;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StarNotesDialog extends Dialog<String> {

    private final TextArea notesArea = new TextArea();

    public StarNotesDialog(String notesData) {
        setTitle("Star notes");
        getDialogPane().setAccessibleText("Edit star notes");
        getDialogPane().setAccessibleHelp("Free-form annotations attached to this star");

        VBox vBox = new VBox();

        notesArea.setAccessibleText("Notes editor");
        notesArea.setText(notesData);
        notesArea.setPromptText("Free-form notes about this star (binary, variable, recent observation, etc.)");
        notesArea.setTooltip(new Tooltip("Notes are saved with the star and survive dataset exports"));

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(5);
        gridPane.setHgap(5);
        vBox.getChildren().add(gridPane);

        gridPane.add(notesArea, 1, 1);

        HBox hBox = new HBox();

        hBox.setAlignment(Pos.CENTER);

        Button resetBtn = new Button("Cancel");
        resetBtn.setAccessibleHelp("Discard note changes");
        resetBtn.setOnAction(this::cancelClicked);
        hBox.getChildren().add(resetBtn);

        Button addBtn = new Button("Save");
        addBtn.setAccessibleHelp("Save notes back to the star");
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
