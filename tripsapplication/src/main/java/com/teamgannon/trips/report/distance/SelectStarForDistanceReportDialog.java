package com.teamgannon.trips.report.distance;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.collections4.MapUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectStarForDistanceReportDialog extends Dialog<DistanceReportSelection> {

    private final ChoiceBox<String> starChoice = new ChoiceBox<>();

    private final Map<String, StarDisplayRecord> starDisplayRecordMap = new HashMap<>();

    public Button generateButton = new Button("Generate");

    /**
     * constructor
     *
     * @param starsInView the stars in view
     */
    public SelectStarForDistanceReportDialog(List<StarDisplayRecord> starsInView) {

        if (!starsInView.isEmpty()) {
            MapUtils.populateMap(starDisplayRecordMap,
                    starsInView,
                    StarDisplayRecord::getStarName);
        }

        if (starsInView.size() > 0) {
            for (StarDisplayRecord starDisplayRecord : starsInView) {
                starChoice.getItems().add(starDisplayRecord.getStarName());
            }
        }

        this.setTitle("Select a star for a distance report");
        this.setHeight(300);
        this.setWidth(400);

        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(10.0);
        this.getDialogPane().setContent(vBox);

        vBox.getChildren().add(starChoice);

        // set the first one
        if (starsInView.size() > 0) {
            starChoice.setValue(starsInView.get(0).getStarName());
        }

        HBox prefsBox = new HBox();
        vBox.getChildren().add(prefsBox);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        vBox.getChildren().add(hBox);

        generateButton.setOnAction(this::generate);
        hBox.getChildren().add(generateButton);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(this::cancel);
        hBox.getChildren().add(cancelBtn);

        // set the dialog as a utility so that the closing is cancelling
        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(this::close);

    }

    private void close(WindowEvent windowEvent) {
        DistanceReportSelection reportSelection = DistanceReportSelection
                .builder()
                .selected(false)
                .build();
        setResult(reportSelection);
    }

    private void cancel(ActionEvent actionEvent) {
        DistanceReportSelection reportSelection = DistanceReportSelection
                .builder()
                .selected(false)
                .build();
        setResult(reportSelection);
    }

    private void generate(ActionEvent actionEvent) {
        String selected = starChoice.getValue();
        StarDisplayRecord record = starDisplayRecordMap.get(selected);
        DistanceReportSelection reportSelection = DistanceReportSelection
                .builder()
                .selected(true)
                .record(record)
                .build();
        setResult(reportSelection);
    }

}
