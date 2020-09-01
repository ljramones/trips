package com.teamgannon.trips.dialogs;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

public class AboutDialog extends Dialog<String> {

    private final Font font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 13);

    public AboutDialog(String version, String releaseDate) {

        VBox vBox = new VBox();
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(5);
        gridPane.setHgap(5);

        Label applicationLabel = new Label("Application");
        applicationLabel.setFont(font);
        gridPane.add(applicationLabel, 0, 1);
        gridPane.add(new Label("Terran Republic Interstellar Plotting System (TRIPS)"), 1, 1);

        Label develop = new Label("Developed by");
        develop.setFont(font);
        gridPane.add(develop, 0, 2);
        gridPane.add(new Label("L.J. Ramones, C.E. Gannon"), 1, 2);

        Label date = new Label("Date");
        date.setFont(font);
        gridPane.add(date, 0, 3);
        gridPane.add(new Label(releaseDate), 1, 3);

        Label versionLabel =new Label("Version");
        versionLabel.setFont(font);
        gridPane.add(versionLabel, 0, 4);
        gridPane.add(new Label(version), 1, 4);

        vBox.getChildren().add(gridPane);

        HBox hBox = new HBox();
        Button resetBtn = new Button("Dismiss");
        resetBtn.setOnAction(this::dismiss);
        hBox.getChildren().add(resetBtn);


        vBox.getChildren().add(hBox);


        this.getDialogPane().setContent(vBox);
    }

    private void dismiss(ActionEvent actionEvent) {
        setResult("  ");
    }

}
