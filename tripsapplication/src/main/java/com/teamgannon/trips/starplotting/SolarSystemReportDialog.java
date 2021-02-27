package com.teamgannon.trips.starplotting;

import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.jetbrains.annotations.NotNull;

public class SolarSystemReportDialog extends Dialog<SolarSystemReport> {


    private final @NotNull SolarSystemReport report;
    public @NotNull Button changeButton = new Button("Save");

    public SolarSystemReportDialog(SolarSystemReport report) {
        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(this::close);

        this.report = report;

        this.setTitle("Distance Report Dialog for: " + report.getSourceStar().getDisplayName());
        this.setHeight(800);
        this.setWidth(400);

        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(10.0);
        this.getDialogPane().setContent(vBox);

        TextArea reportArea = new TextArea();
        reportArea.setPrefHeight(700);
        reportArea.setText(report.getGeneratedReport());
        vBox.getChildren().add(reportArea);

        HBox prefsBox = new HBox();
        vBox.getChildren().add(prefsBox);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        vBox.getChildren().add(hBox);

        changeButton.setOnAction(this::saveClicked);
        hBox.getChildren().add(changeButton);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(this::cancel);
        hBox.getChildren().add(cancelBtn);

        // set the dialog as a utility so that the closing is cancelling
        stage.setOnCloseRequest(this::close);
    }

    private void close(WindowEvent windowEvent) {
        report.setSave(false);
        setResult(report);
    }

    private void cancel(ActionEvent actionEvent) {
        report.setSave(false);
        setResult(report);
    }

    private void saveClicked(ActionEvent actionEvent) {
        report.setSave(true);
        setResult(report);
    }



}
