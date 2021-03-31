package com.teamgannon.trips.controller;

import com.teamgannon.trips.transits.TransitDefinitions;
import com.teamgannon.trips.transits.TransitManager;
import com.teamgannon.trips.transits.TransitRangeDef;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;


@Slf4j
public class TransitFilterPane extends Pane {

    private final Font font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 10);

    private TransitManager transitManager;

    public TransitFilterPane() {

    }

    /**
     * set up the filter to control the transits
     *
     * @param transitDefinitions the transit definitions
     * @param transitManager     the transit manager for constrolling all of this
     */
    public void setFilter(TransitDefinitions transitDefinitions, TransitManager transitManager) {
        this.transitManager = transitManager;

        VBox vBox = new VBox();
        this.getChildren().add(vBox);

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(5);
        gridPane.setHgap(5);
        vBox.getChildren().add(gridPane);

        Label datasetLabel = new Label("Dataset");
        datasetLabel.setFont(font);
        int currentRow = 0;
        gridPane.add(datasetLabel, 0, currentRow);
        gridPane.add(new Label(transitDefinitions.getDataSetName()), 1, currentRow++);
        createHeaders(gridPane);
        currentRow++;
        List<TransitRangeDef> transitRangeDefList = transitDefinitions.getTransitRangeDefs();
        transitRangeDefList.sort(Comparator.comparing(TransitRangeDef::getBandName));
        for (TransitRangeDef transitRangeDef : transitRangeDefList) {
            if (transitRangeDef.isEnabled()) {
                addTransitRef(gridPane, transitRangeDef, currentRow++);
            }
        }
    }

    private void createHeaders(GridPane gridPane) {
        // show headers for this set of tables
        Label enabledLabel = new Label("Show?");
        enabledLabel.setFont(font);
        gridPane.add(enabledLabel, 0, 1);
        Label bandNameLabel = new Label("Band\nName");
        bandNameLabel.setFont(font);
        gridPane.add(bandNameLabel, 1, 1);
        Label lowerRangeLabel = new Label("Lower\nRange");
        lowerRangeLabel.setFont(font);
        gridPane.add(lowerRangeLabel, 2, 1);
        Label upperRangeLabel = new Label("Upper\nRange");
        upperRangeLabel.setFont(font);
        gridPane.add(upperRangeLabel, 3, 1);
        Label lineWidthLabel = new Label("Line\nWidth");
        lineWidthLabel.setFont(font);
        gridPane.add(lineWidthLabel, 4, 1);
        Label colorLabel = new Label("Color");
        colorLabel.setFont(font);
        gridPane.add(colorLabel, 5, 1);
    }

    private void addTransitRef(GridPane gridPane, TransitRangeDef transitRangeDef, int row) {

        // checkbox
        CheckBox showTransit = new CheckBox();
        showTransit.setSelected(transitRangeDef.isEnabled());
        showTransit.setOnAction(e -> {
            transitManager.showTransit(transitRangeDef.getBandId(), showTransit.isSelected());
        });
        gridPane.add(showTransit, 0, row);

        // name
        Label nameLabel = new Label(transitRangeDef.getBandName());
        gridPane.add(nameLabel, 1, row);

        // lower range
        Label lowerRangeLabel = new Label(Double.toString(transitRangeDef.getLowerRange()));
        gridPane.add(lowerRangeLabel, 2, row);

        // upper range
        Label upperRangeLabel = new Label(Double.toString(transitRangeDef.getUpperRange()));
        gridPane.add(upperRangeLabel, 3, row);

        // upper range
        Label lineWidthLabel = new Label(Double.toString(transitRangeDef.getLineWidth()));
        gridPane.add(lineWidthLabel, 4, row);

        // upper range
        ColorPicker colorPicker = new ColorPicker();
        colorPicker.setValue(transitRangeDef.getBandColor());
        colorPicker.setDisable(true);
        colorPicker.setOpacity(1.0);
        colorPicker.setStyle("-fx-opacity : 1.0; -fx-color-label-visible: false ;");
        gridPane.add(colorPicker, 5, row);
    }

}
