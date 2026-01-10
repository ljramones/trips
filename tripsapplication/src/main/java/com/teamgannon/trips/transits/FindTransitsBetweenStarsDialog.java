package com.teamgannon.trips.transits;

import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.DatasetService;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;


import java.util.*;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

@Slf4j
public class FindTransitsBetweenStarsDialog extends Dialog<TransitDefinitions> {

    private final Font font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 13);

    private final Map<UUID, TransitRangeDefinition> bandMap = new HashMap<>();

    private final DatabaseManagementService databaseManagementService;

    private final DatasetService datasetService;
    private final TransitDefinitions transitDefinitions;

    private final GridPane gridPane = new GridPane();

    private int currentRow = 0;

    private final Stage stage;


    public FindTransitsBetweenStarsDialog(DatabaseManagementService databaseManagementService,
                                          DatasetService datasetService,
                                          TransitDefinitions transitDefinitions) {

        this.databaseManagementService = databaseManagementService;
        this.datasetService = datasetService;
        this.transitDefinitions = transitDefinitions;
        transitDefinitions.getTransitRangeDefs().sort(Comparator.comparing(TransitRangeDef::getBandName));

        this.setTitle("Select a Range to Find Transits");
        this.setHeight(300);
        this.setWidth(500);

        VBox vBox = new VBox();
        vBox.getChildren().add(new Separator());

        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(5);
        gridPane.setHgap(5);
        vBox.getChildren().add(gridPane);

        setupFirstFiveBands(transitDefinitions, gridPane);

        HBox addRowBox = new HBox();
        addRowBox.setAlignment(Pos.CENTER);
        vBox.getChildren().add(addRowBox);

        Button addRow = new Button("Add Row");
        addRow.setOnAction(this::addRow);
        addRowBox.getChildren().add(addRow);

        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(10.0);

        HBox hBox1 = new HBox();
        hBox1.setAlignment(Pos.CENTER);
        vBox.getChildren().add(hBox1);

        Button gotToStarButton = new Button("Generate Transits");
        gotToStarButton.setOnAction(this::goToStarClicked);
        hBox1.getChildren().add(gotToStarButton);

        Button cancelDataSetButton = new Button("Cancel");
        cancelDataSetButton.setOnAction(this::close);
        hBox1.getChildren().add(cancelDataSetButton);

        this.getDialogPane().setContent(vBox);

        // set the dialog as a utility
        stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(this::close);
    }

    private void addRow(ActionEvent actionEvent) {
        if (currentRow < 20) {
            TransitRangeDef def = new TransitRangeDef();
            def.setBandName("band " + currentRow);
            def.setLineWidth(0.5);
            def.setEnabled(false);
            def.setBandColor(Color.WHEAT);
            addBand(gridPane, currentRow++, def);
            stage.sizeToScene();
        } else {
            showErrorAlert("Add Transit Definition", "Max of 20 bands");
        }
    }

    private void setupFirstFiveBands(TransitDefinitions transitDefinitions, GridPane gridPane) {

        // show headers for this set of tables
        Label enabledLabel = new Label("Use?");
        enabledLabel.setFont(font);
        gridPane.add(enabledLabel, 0, 0);
        Label bandNameLabel = new Label("Band Name");
        bandNameLabel.setFont(font);
        gridPane.add(bandNameLabel, 1, 0);
        Label lowerRangeLabel = new Label("Lower Range");
        lowerRangeLabel.setFont(font);
        gridPane.add(lowerRangeLabel, 2, 0);
        Label rangeSliderLabel = new Label("Range Slider");
        rangeSliderLabel.setFont(font);
        gridPane.add(rangeSliderLabel, 3, 0);
        Label upperRangeLabel = new Label("Upper Range");
        upperRangeLabel.setFont(font);
        gridPane.add(upperRangeLabel, 4, 0);
        Label lineWidthLabel = new Label("Line Width");
        lineWidthLabel.setFont(font);
        gridPane.add(lineWidthLabel, 5, 0);
        Label colorLabel = new Label("Color");
        colorLabel.setFont(font);
        gridPane.add(colorLabel, 6, 0);
        currentRow = 5;

        // create base set of bands
        // note that we need to persist this in the db
        List<TransitRangeDef> transitRangeDefList = transitDefinitions.getTransitRangeDefs();
        // change these
        if (transitRangeDefList.size() != 0) {
            currentRow = 1;
            for (TransitRangeDef def : transitDefinitions.getTransitRangeDefs()) {
                addBand(gridPane, currentRow++, def);
            }
        } else {
            TransitRangeDef def1, def2, def3, def4, def5;
            def1 = createTransitRef(UUID.randomUUID(), "band 1", false, Color.AQUAMARINE, 0.5);
            def2 = createTransitRef(UUID.randomUUID(), "band 2", false, Color.BLUEVIOLET, 0.5);
            def3 = createTransitRef(UUID.randomUUID(), "band 3", false, Color.GREEN, 0.5);
            def4 = createTransitRef(UUID.randomUUID(), "band 4", false, Color.YELLOW, 0.5);
            def5 = createTransitRef(UUID.randomUUID(), "band 5", false, Color.RED, 0.5);
            addBand(gridPane, 1, def1);
            addBand(gridPane, 2, def2);
            addBand(gridPane, 3, def3);
            addBand(gridPane, 4, def4);
            addBand(gridPane, 5, def5);
        }

    }

    private TransitRangeDef createTransitRef(UUID id, String bandName,
                                             boolean enabled, Color bandColor, double lineWidth) {
        TransitRangeDef transitRangeDef = new TransitRangeDef();
        transitRangeDef.setBandId(id);
        transitRangeDef.setBandName(bandName);
        transitRangeDef.setEnabled(enabled);
        transitRangeDef.setBandColor(bandColor);
        transitRangeDef.setLineWidth(lineWidth);
        return transitRangeDef;
    }

    private void addBand(GridPane gridPane, int row, TransitRangeDef transitRangeDef) {
        TransitRangeDefinition transitRangeDefinition = new TransitRangeDefinition(gridPane, row, transitRangeDef);
        bandMap.put(transitRangeDef.getBandId(), transitRangeDefinition);
    }

    private void close(WindowEvent windowEvent) {
        TransitDefinitions transitDefinitions = new TransitDefinitions();
        transitDefinitions.setSelected(false);
        setResult(transitDefinitions);
    }

    private void close(ActionEvent actionEvent) {
        TransitDefinitions transitDefinitions = new TransitDefinitions();
        transitDefinitions.setSelected(false);
        setResult(transitDefinitions);
    }

    private void goToStarClicked(ActionEvent actionEvent) {
        List<TransitRangeDef> transitRangeDefs = getValues();
        transitDefinitions.setTransitRangeDefs(transitRangeDefs);
        transitDefinitions.setSelected(true);
        datasetService.setTransitPreferences(transitDefinitions);
        setResult(transitDefinitions);
    }

    private List<TransitRangeDef> getValues() {
        try {
            List<TransitRangeDef> transitRangeDefs = new ArrayList<>();
            for (TransitRangeDefinition definition : bandMap.values()) {
                transitRangeDefs.add(definition.getValue());
            }
//            validateTransitRefs(transitRangeDefs);
            return transitRangeDefs;
        } catch (Exception e) {
            log.error("transit definition is invalid:" + e.getMessage());
            return null;
        }
    }

    private void validateTransitRefs(List<TransitRangeDef> transitRangeDefs) {
        for (TransitRangeDef rangeDefOuter : transitRangeDefs) {
            double lowerRangeOuter = rangeDefOuter.getLowerRange();
            double upperRangeOuter = rangeDefOuter.getUpperRange();
            UUID idOuter = rangeDefOuter.getBandId();
            for (TransitRangeDef rangeDefInner : transitRangeDefs) {
                if (!rangeDefInner.isEnabled()) {
                    continue;
                }
                UUID idInner = rangeDefInner.getBandId();
                if (!idInner.equals(idOuter)) {
                    double lowerRangeInner = rangeDefInner.getLowerRange();
                    double upperRangeInner = rangeDefInner.getUpperRange();
                    if ((lowerRangeInner > lowerRangeOuter) && (lowerRangeInner < upperRangeOuter) ||
                            (upperRangeInner > lowerRangeOuter) && (upperRangeInner < upperRangeOuter)) {
                        showErrorAlert("Add Transit Definitions",
                                rangeDefOuter.getBandName() + "overlaps with " + rangeDefInner.getBandName());
                        return;
                    }
                }
            }
        }

    }

}
