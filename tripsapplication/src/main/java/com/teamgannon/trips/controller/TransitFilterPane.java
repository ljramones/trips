package com.teamgannon.trips.controller;

import com.teamgannon.trips.transits.TransitDefinitions;
import com.teamgannon.trips.transits.TransitManager;
import com.teamgannon.trips.transits.TransitRangeDef;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;


@Slf4j
public class TransitFilterPane extends VBox {

    private final Font font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 10);

    private TransitManager transitManager;

    @FXML
    private GridPane gridPane;

    public TransitFilterPane() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("TransitFilterPane.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load TransitFilterPane.fxml", e);
        }
    }

    /**
     * set up the filter to control the transits
     *
     * @param transitDefinitions the transit definitions
     * @param transitManager     the transit manager for constrolling all of this
     */
    public void setFilter(TransitDefinitions transitDefinitions, TransitManager transitManager) {
        this.transitManager = transitManager;
        gridPane.getChildren().clear();

        Label datasetLabel = new Label("Dataset");
        datasetLabel.setFont(font);
        int currentRow = 0;
        gridPane.add(datasetLabel, 0, currentRow);
        gridPane.add(new Label(transitDefinitions.getDataSetName()), 1, currentRow++);
        createHeaders(gridPane, currentRow++);
        List<TransitRangeDef> transitRangeDefList = transitDefinitions.getTransitRangeDefs();
        transitRangeDefList.sort(Comparator.comparing(TransitRangeDef::getBandName));
        for (TransitRangeDef transitRangeDef : transitRangeDefList) {
            if (transitRangeDef.isEnabled()) {
                addTransitRef(gridPane, transitRangeDef, currentRow++);
            }
        }
    }

    private void createHeaders(GridPane gridPane, int row) {
        // show headers for this set of tables
        Label enabledLabel = new Label("Show?");
        enabledLabel.setFont(font);
        gridPane.add(enabledLabel, 0, row);
        Label showLengthsLabel = new Label("Show?");
        showLengthsLabel.setFont(font);
        gridPane.add(showLengthsLabel, 1, row);
        Label bandNameLabel = new Label("Band\nName");
        bandNameLabel.setFont(font);
        gridPane.add(bandNameLabel, 2, row);
        Label lowerRangeLabel = new Label("Lower\nRange");
        lowerRangeLabel.setFont(font);
        gridPane.add(lowerRangeLabel, 3, row);
        Label upperRangeLabel = new Label("Upper\nRange");
        upperRangeLabel.setFont(font);
        gridPane.add(upperRangeLabel, 4, row);
        Label lineWidthLabel = new Label("Line\nWidth");
        lineWidthLabel.setFont(font);
        gridPane.add(lineWidthLabel, 5, row);
        Label colorLabel = new Label("Color");
        colorLabel.setFont(font);
        gridPane.add(colorLabel, 6, row);
    }

    private void addTransitRef(GridPane gridPane, TransitRangeDef transitRangeDef, int row) {

        // checkbox
        CheckBox showTransit = new CheckBox();
        showTransit.setSelected(transitRangeDef.isEnabled());
        showTransit.setOnAction(e -> {
            if (transitManager != null) {
                transitManager.showTransit(transitRangeDef.getBandId(), showTransit.isSelected());
            }
        });
        gridPane.add(showTransit, 0, row);

        CheckBox showLabels = new CheckBox();
        showLabels.setSelected(true);
        showLabels.setOnAction(e -> {
            if (transitManager != null) {
                transitManager.showLabels(transitRangeDef.getBandId(), showLabels.isSelected());
            }
        });
        gridPane.add(showLabels, 1, row);

        // name
        Label nameLabel = new Label(transitRangeDef.getBandName());
        gridPane.add(nameLabel, 2, row);

        // lower range
        Label lowerRangeLabel = new Label(Double.toString(transitRangeDef.getLowerRange()));
        gridPane.add(lowerRangeLabel, 3, row);

        // upper range
        Label upperRangeLabel = new Label(Double.toString(transitRangeDef.getUpperRange()));
        gridPane.add(upperRangeLabel, 4, row);

        // upper range
        Label lineWidthLabel = new Label(Double.toString(transitRangeDef.getLineWidth()));
        gridPane.add(lineWidthLabel, 5, row);

        // upper range
        ColorPicker colorPicker = new ColorPicker();
        colorPicker.setValue(transitRangeDef.getBandColor());
        colorPicker.setDisable(true);
        colorPicker.setOpacity(1.0);
        colorPicker.setStyle("-fx-opacity : 1.0; -fx-color-label-visible: false ;");
        gridPane.add(colorPicker, 6, row);
    }

    public void clear() {
        gridPane.getChildren().clear();
    }
}
