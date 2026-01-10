package com.teamgannon.trips.dialogs.db;

import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.StarService;
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

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

@Slf4j
public class CompareDBDialog extends Dialog<DBComparison> {

    private final ComboBox<String> sourceDBComboBox = new ComboBox<>();
    private final ComboBox<String> targetDBComboBox = new ComboBox<>();

    private final TextArea nameDiffsTextArea = new TextArea();
    private final DatabaseManagementService databaseManagementService;
    private final StarService starService;

    private Label diffsDBLabel = new Label("DB Diffs");

    private final DBComparison dbComparison = DBComparison.builder().build();

    public CompareDBDialog(DatabaseManagementService databaseManagementService,
                           StarService starService,
                           List<String> dataSetList) {
        this.databaseManagementService = databaseManagementService;
        this.starService = starService;
        sourceDBComboBox.getItems().addAll(dataSetList);
        sourceDBComboBox.getSelectionModel().select(0);
        targetDBComboBox.getItems().addAll(dataSetList);
        targetDBComboBox.getSelectionModel().select(0);

        this.setWidth(800);

        this.setTitle("Compare Datasets");

        VBox vBox = new VBox();

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(5);
        gridPane.setHgap(5);
        gridPane.setPrefWidth(750);

        Font font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 13);

        Label sourceDBLabel = new Label("Source DB");
        sourceDBLabel.setFont(font);
        gridPane.add(sourceDBLabel, 0, 0);
        gridPane.add(sourceDBComboBox, 1, 0);

        Label targetDBLabel = new Label("Target DB");
        targetDBLabel.setFont(font);
        gridPane.add(targetDBLabel, 0, 1);
        gridPane.add(targetDBComboBox, 1, 1);


        diffsDBLabel.setFont(font);
        gridPane.add(diffsDBLabel, 0, 2);
        nameDiffsTextArea.setPrefWidth(600);
        nameDiffsTextArea.setPrefHeight(500);
        gridPane.add(nameDiffsTextArea, 1, 2);

        vBox.getChildren().add(gridPane);

        HBox hBox = new HBox();
        hBox.setSpacing(10);
        hBox.setAlignment(Pos.CENTER);

        Button FindDifferences = new Button("Compare");
        FindDifferences.setOnAction(this::findDiffsClicked);
        hBox.getChildren().add(FindDifferences);

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(this::close);
        hBox.getChildren().add(cancelButton);

        Button dismissButton = new Button("Dismiss");
        dismissButton.setOnAction(this::dismiss);
        hBox.getChildren().add(dismissButton);
        vBox.getChildren().add(hBox);

        this.getDialogPane().setContent(vBox);

        // set the dialog as a utility
        DialogUtils.bindCloseHandler(this, this::close);

    }

    private void dismiss(ActionEvent actionEvent) {
        setResult(dbComparison);
    }

    private void close(WindowEvent windowEvent) {
        setResult(dbComparison);
        close();
    }

    private void close(ActionEvent actionEvent) {
        setResult(dbComparison);
        close();
    }

    private void findDiffsClicked(ActionEvent actionEvent) {
        String sourceSelection = sourceDBComboBox.getSelectionModel().getSelectedItem();
        String targetSelection = targetDBComboBox.getSelectionModel().getSelectedItem();

        // check that we aren't comparing a database with itself
        if (sourceSelection.equals(targetSelection)) {
            showErrorAlert("Compare Datasets", "Source and Target cannot be the same");
            // error return
            return;
        }

        // get the differences for the two databases
        log.info("start comparison ...");
        List<DBReference> namesNotFound = starService.compareStars(sourceSelection, targetSelection);
        dbComparison.setNamesNotFound(new HashSet<>(namesNotFound));
        log.info("comparison complete, number of missing is {}", namesNotFound.size());

        diffsDBLabel.setText("DB Diffs\n(" + namesNotFound.size() + " diffs)");

        // create a single string with the names we retrieved
        String stringBuffer = namesNotFound.stream().map(reference -> "id=" + reference.getId() + "-> name=" + reference.getDisplayName() + ",\n").collect(Collectors.joining());

        // add to our text area
        nameDiffsTextArea.setText(stringBuffer);

        // success return
        dbComparison.setSelected(true);

    }
}

