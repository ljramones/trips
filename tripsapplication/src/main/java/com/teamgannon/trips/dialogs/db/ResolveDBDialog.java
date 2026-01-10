package com.teamgannon.trips.dialogs.db;

import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.utility.DialogUtils;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.WindowEvent;
import org.controlsfx.control.spreadsheet.SpreadsheetView;

import java.util.List;

public class ResolveDBDialog extends Dialog<DBComparison> {

    private final ComboBox<String> sourceDBComboBox = new ComboBox<>();
    private final ComboBox<String> targetDBComboBox = new ComboBox<>();
    private final DatabaseManagementService databaseManagementService;

    private SpreadsheetView spreadSheetView;

    public ResolveDBDialog(DatabaseManagementService databaseManagementService,
                           List<String> dataSetList) {

        this.databaseManagementService = databaseManagementService;
        sourceDBComboBox.getItems().addAll(dataSetList);
        sourceDBComboBox.getSelectionModel().select(0);
        targetDBComboBox.getItems().addAll(dataSetList);
        targetDBComboBox.getSelectionModel().select(0);

        this.setWidth(800);

        this.setTitle("Resolve Datasets");

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




        ///////////////////////////////////////
        vBox.getChildren().add(gridPane);

        HBox hBox = new HBox();
        hBox.setSpacing(10);
        hBox.setAlignment(Pos.CENTER);

        Button FindDifferences = new Button("Find Diffs");
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

    }

    private void close(WindowEvent windowEvent) {

    }

    private void close(ActionEvent actionEvent) {

    }

    private void findDiffsClicked(ActionEvent actionEvent) {

    }

}
