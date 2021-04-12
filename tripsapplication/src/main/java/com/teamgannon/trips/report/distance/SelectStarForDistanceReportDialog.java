package com.teamgannon.trips.report.distance;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.collections4.MapUtils;
import org.controlsfx.control.textfield.TextFields;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SelectStarForDistanceReportDialog extends Dialog<DistanceReportSelection> {

    private final @NotNull ComboBox<String> cmb;

    private final Map<String, StarDisplayRecord> starDisplayRecordMap = new HashMap<>();

    /**
     * our lookup
     */
    private final Map<String, StarDisplayRecord> starLookup = new HashMap<>();

    public @NotNull Button generateButton = new Button("Generate");

    /**
     * constructor
     *
     * @param starsInView the stars in view
     */
    public SelectStarForDistanceReportDialog(@NotNull List<StarDisplayRecord> starsInView) {

        this.setTitle("Select a star for a distance report");
        this.setHeight(300);
        this.setWidth(500);

        Set<String> searchValues = convertList(starsInView);

        cmb = new ComboBox<>();
        cmb.setPromptText("start typing");
        cmb.setTooltip(new Tooltip());
        cmb.getItems().addAll(searchValues);
        cmb.setEditable(true);
        TextFields.bindAutoCompletion(cmb.getEditor(), cmb.getItems());

        cmb.setPrefWidth(300);

        if (!starsInView.isEmpty()) {
            MapUtils.populateMap(starDisplayRecordMap,
                    starsInView,
                    StarDisplayRecord::getStarName);
        }

        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(10.0);
        this.getDialogPane().setContent(vBox);

        vBox.getChildren().add(cmb);

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

        // set the dialog as a utility
        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(this::close);

    }

    private @NotNull Set<String> convertList(@NotNull List<StarDisplayRecord> starsInView) {
        starsInView.forEach(record -> starLookup.put(record.getStarName(), record));
        return starLookup.keySet();
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
        String selected = cmb.getValue();
        StarDisplayRecord record = starDisplayRecordMap.get(selected);
        DistanceReportSelection reportSelection = DistanceReportSelection
                .builder()
                .selected(true)
                .record(record)
                .build();
        setResult(reportSelection);
    }

}
