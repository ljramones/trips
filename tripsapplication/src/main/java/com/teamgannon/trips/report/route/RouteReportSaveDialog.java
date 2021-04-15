package com.teamgannon.trips.report.route;


import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

@Slf4j
public class RouteReportSaveDialog extends Dialog<Boolean> {

    private Stage stage;
    private String report;

    public RouteReportSaveDialog(String report) {
        this.report = report;

        setTitle("Save Route Report");
        setWidth(200);
        VBox vBox = new VBox();
        vBox.setSpacing(10);
        vBox.setPadding(new Insets(10, 10, 10, 10));

        TextArea reportArea = new TextArea();
        reportArea.setPrefWidth(300);
        reportArea.setPrefHeight(300);
        reportArea.setText(report);
        vBox.getChildren().add(reportArea);

        HBox hBox2 = new HBox();
        hBox2.setAlignment(Pos.CENTER);
        hBox2.setSpacing(5);
        vBox.getChildren().add(hBox2);

        Button dismissButton = new Button("Dismiss");
        dismissButton.setOnAction(this::dismissAction);

        Button runReportButton = new Button("Save Report");
        runReportButton.setOnAction(this::saveReportAction);
        hBox2.getChildren().addAll(dismissButton, runReportButton);

        this.getDialogPane().setContent(vBox);

        stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(this::close);

    }

    private void saveReportAction(ActionEvent actionEvent) {
        // save the file
        FileChooser fileChooser = new FileChooser();

        //Set extension filter for text files
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show save file dialog
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            saveTextToFile(report.toString(), file);
        }
        setResult(true);
    }

    private void dismissAction(ActionEvent actionEvent) {
        setResult(false);
    }

    private void close(WindowEvent windowEvent) {
        setResult(false);
    }

    private void saveTextToFile(String generatedReport, File file) {
        try (PrintWriter out = new PrintWriter(file)) {
            out.println(generatedReport);
        } catch (FileNotFoundException e) {
            log.error("Can't create file {} because of {}", file.getAbsolutePath(), e.getMessage());
        }
    }

}
