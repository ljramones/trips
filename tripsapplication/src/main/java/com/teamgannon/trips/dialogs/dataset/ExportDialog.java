package com.teamgannon.trips.dialogs.dataset;

import com.teamgannon.trips.dialogs.dataset.model.ExportOptions;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.service.model.ExportFileType;
import javafx.collections.FXCollections;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public class ExportDialog extends Dialog<ExportOptions> {


    private final TextField fileNameTextField = new TextField();
    private final DataSetDescriptor selectedDataSet;

    private ComboBox<ExportFileType> exportChoice = new ComboBox<>();


    public ExportDialog(DataSetDescriptor selectedDataSet) {
        this.selectedDataSet = selectedDataSet;

        this.exportChoice.setItems(FXCollections.observableArrayList( ExportFileType.values()));
        this.exportChoice.getSelectionModel().select(ExportFileType.COMPACT);

        this.setTitle("Export Dataset");
        this.setWidth(600);

        VBox vBox = new VBox();

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(5);
        gridPane.setHgap(5);
        gridPane.setPrefWidth(450);

        Label exportTypeLabel = new Label("Export type");
        Font font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 13);
        exportTypeLabel.setFont(font);
        gridPane.add(exportTypeLabel, 0, 1);
        gridPane.add(exportChoice, 1, 1);

        gridPane.add(new Label("FileName"), 0, 2);
        fileNameTextField.setPrefWidth(300);
        gridPane.add(fileNameTextField, 1, 2);

        Button fileDialogButton = new Button("Pick file \nand location");
        fileDialogButton.setFont(font);
        gridPane.add(fileDialogButton, 0, 2);
        fileDialogButton.setOnAction(event -> {
            showDialog();
        });

        vBox.getChildren().add(gridPane);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);

        Button doExportButton = new Button("Export");
        doExportButton.setOnAction(this::exportClicked);
        hBox.getChildren().add(doExportButton);

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(this::close);
        hBox.getChildren().add(cancelButton);
        vBox.getChildren().add(hBox);

        this.getDialogPane().setContent(vBox);

        // set the dialog as a utility
        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(this::close);
    }

    private void showDialog() {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Database to export as a CSV file");
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            fileNameTextField.setText(file.getAbsolutePath());
        } else {
            log.warn("file export cancelled");
            setResult(ExportOptions.builder().doExport(false).build());
        }
    }

    private void close(WindowEvent windowEvent) {
        setResult(ExportOptions.builder().doExport(false).build());
    }

    private void close(ActionEvent actionEvent) {
        setResult(ExportOptions.builder().doExport(false).build());
    }

    private void exportClicked(ActionEvent actionEvent) {
        ExportOptions options = ExportOptions
                .builder()
                .doExport(true)
                .exportFormat(exportChoice.getSelectionModel().getSelectedItem())
                .fileName(fileNameTextField.getText())
                .dataset(selectedDataSet)
                .build();
        setResult(options);
    }

}
