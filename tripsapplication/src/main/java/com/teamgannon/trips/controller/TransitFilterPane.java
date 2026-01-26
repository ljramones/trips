package com.teamgannon.trips.controller;

import com.teamgannon.trips.transits.TransitDefinitions;
import com.teamgannon.trips.transits.TransitManager;
import com.teamgannon.trips.transits.TransitRangeDef;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
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
    private final Font dataFont = Font.font("Verdana", FontWeight.NORMAL, FontPosture.REGULAR, 10);

    private TransitManager transitManager;
    private boolean columnsInitialized = false;

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
     * Initialize column constraints for proper layout.
     */
    private void initializeColumnConstraints() {
        if (columnsInitialized) {
            return;
        }

        gridPane.getColumnConstraints().clear();

        // Column 0: Show checkbox
        ColumnConstraints col0 = new ColumnConstraints();
        col0.setMinWidth(40);
        col0.setPrefWidth(45);
        col0.setHalignment(HPos.CENTER);

        // Column 1: Show labels checkbox
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(40);
        col1.setPrefWidth(45);
        col1.setHalignment(HPos.CENTER);

        // Column 2: Band name
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setMinWidth(50);
        col2.setPrefWidth(60);
        col2.setHgrow(Priority.SOMETIMES);

        // Column 3: Lower range
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setMinWidth(45);
        col3.setPrefWidth(50);
        col3.setHalignment(HPos.RIGHT);

        // Column 4: Upper range
        ColumnConstraints col4 = new ColumnConstraints();
        col4.setMinWidth(45);
        col4.setPrefWidth(50);
        col4.setHalignment(HPos.RIGHT);

        // Column 5: Line width
        ColumnConstraints col5 = new ColumnConstraints();
        col5.setMinWidth(35);
        col5.setPrefWidth(40);
        col5.setHalignment(HPos.RIGHT);

        // Column 6: Color picker
        ColumnConstraints col6 = new ColumnConstraints();
        col6.setMinWidth(60);
        col6.setPrefWidth(70);
        col6.setHalignment(HPos.CENTER);

        gridPane.getColumnConstraints().addAll(col0, col1, col2, col3, col4, col5, col6);
        columnsInitialized = true;
    }

    /**
     * set up the filter to control the transits
     *
     * @param transitDefinitions the transit definitions
     * @param transitManager     the transit manager for controlling all of this
     */
    public void setFilter(TransitDefinitions transitDefinitions, TransitManager transitManager) {
        this.transitManager = transitManager;

        // Clear all existing content
        gridPane.getChildren().clear();

        // Initialize column constraints (only once)
        initializeColumnConstraints();

        int currentRow = 0;

        // Dataset label row - spans multiple columns
        Label datasetLabel = new Label("Dataset");
        datasetLabel.setFont(font);
        gridPane.add(datasetLabel, 0, currentRow);

        Label datasetNameLabel = new Label(transitDefinitions.getDataSetName());
        datasetNameLabel.setFont(dataFont);
        GridPane.setColumnSpan(datasetNameLabel, 6);
        gridPane.add(datasetNameLabel, 1, currentRow);
        currentRow++;

        // Headers row
        createHeaders(gridPane, currentRow);
        currentRow++;

        // Transit definition rows
        List<TransitRangeDef> transitRangeDefList = transitDefinitions.getTransitRangeDefs();
        transitRangeDefList.sort(Comparator.comparing(TransitRangeDef::getBandName));
        for (TransitRangeDef transitRangeDef : transitRangeDefList) {
            if (transitRangeDef.isEnabled()) {
                addTransitRef(gridPane, transitRangeDef, currentRow);
                currentRow++;
            }
        }

        // Force layout update
        gridPane.requestLayout();
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

        // Show transit checkbox
        CheckBox showTransit = new CheckBox();
        showTransit.setSelected(transitRangeDef.isEnabled());
        showTransit.setOnAction(e -> {
            if (transitManager != null) {
                transitManager.showTransit(transitRangeDef.getBandId(), showTransit.isSelected());
            }
        });
        gridPane.add(showTransit, 0, row);

        // Show labels checkbox
        CheckBox showLabels = new CheckBox();
        showLabels.setSelected(true);
        showLabels.setOnAction(e -> {
            if (transitManager != null) {
                transitManager.showLabels(transitRangeDef.getBandId(), showLabels.isSelected());
            }
        });
        gridPane.add(showLabels, 1, row);

        // Band name
        Label nameLabel = new Label(transitRangeDef.getBandName());
        nameLabel.setFont(dataFont);
        gridPane.add(nameLabel, 2, row);

        // Lower range (formatted to 2 decimal places)
        Label lowerRangeLabel = new Label(String.format("%.2f", transitRangeDef.getLowerRange()));
        lowerRangeLabel.setFont(dataFont);
        gridPane.add(lowerRangeLabel, 3, row);

        // Upper range (formatted to 2 decimal places)
        Label upperRangeLabel = new Label(String.format("%.2f", transitRangeDef.getUpperRange()));
        upperRangeLabel.setFont(dataFont);
        gridPane.add(upperRangeLabel, 4, row);

        // Line width
        Label lineWidthLabel = new Label(String.format("%.1f", transitRangeDef.getLineWidth()));
        lineWidthLabel.setFont(dataFont);
        gridPane.add(lineWidthLabel, 5, row);

        // Color picker (display only)
        ColorPicker colorPicker = new ColorPicker();
        colorPicker.setValue(transitRangeDef.getBandColor());
        colorPicker.setDisable(true);
        colorPicker.setOpacity(1.0);
        colorPicker.setPrefWidth(60);
        colorPicker.setStyle("-fx-opacity: 1.0; -fx-color-label-visible: false;");
        gridPane.add(colorPicker, 6, row);
    }

    public void clear() {
        gridPane.getChildren().clear();
        gridPane.getColumnConstraints().clear();
        columnsInitialized = false;
    }
}
