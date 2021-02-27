package com.teamgannon.trips.starplotting;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.StarObject;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class SolarSystemGenerationDialog extends Dialog<SolarSystemGenOptions> {

    private final SolarSystemGenOptions solarSystemGenOptions = SolarSystemGenOptions
            .builder()
            .verbose(false).extraVerbose(false).createMoons(false).doGeneration(false)
            .build();

    private final CheckBox createMoonsBox = new CheckBox("Create Moons");
    private final CheckBox verboseBox = new CheckBox("Verbose mode");
    private final CheckBox extraVerboseBox = new CheckBox("Extra Verbose mode");

    public SolarSystemGenerationDialog(StarObject starObject) {

        this.setTitle("Solar System Generation Options for " + starObject.getDisplayName());
        solarSystemGenOptions.setStarObject(starObject);
        VBox vBox = new VBox();

        GridPane gridPane = createSelectionPane();
        vBox.getChildren().add(gridPane);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        hBox.setSpacing(5);
        vBox.getChildren().add(hBox);

        Button generateButton = new Button("Generate");
        generateButton.setOnAction(this::generate);
        hBox.getChildren().add(generateButton);

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(this::close);
        hBox.getChildren().add(cancelButton);

        this.getDialogPane().setContent(vBox);

        // set the dialog as a utility
        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(this::close);
    }

    private GridPane createSelectionPane() {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(5);
        gridPane.setHgap(5);

        createMoonsBox.setSelected(false);
        gridPane.add(createMoonsBox, 0, 1);

        verboseBox.setSelected(false);
        gridPane.add(verboseBox, 0, 2);

        extraVerboseBox.setSelected(true);
        gridPane.add(extraVerboseBox, 0, 3);

        return gridPane;
    }

    private void generate(ActionEvent actionEvent) {
        solarSystemGenOptions.setDoGeneration(true);
        solarSystemGenOptions.setCreateMoons(createMoonsBox.isSelected());
        solarSystemGenOptions.setVerbose(verboseBox.isSelected());
        solarSystemGenOptions.setExtraVerbose(extraVerboseBox.isSelected());
        setResult(solarSystemGenOptions);
    }

    private void close(ActionEvent actionEvent) {
        setResult(solarSystemGenOptions);
    }

    private void close(WindowEvent windowEvent) {
        setResult(solarSystemGenOptions);
    }


}
