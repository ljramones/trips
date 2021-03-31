package com.teamgannon.trips.transits;

import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.control.RangeSlider;

import java.util.UUID;

@Slf4j
public class TransitRangeDefinition {

    private UUID id;

    private final CheckBox enabled = new CheckBox();
    private final RangeSlider rangeSlider = new RangeSlider(0, 20, 0, 20);
    private final TextField bandNameTextField = new TextField();
    private final TextField upperRangeTextField = new TextField();
    private final TextField lowerRangeTextField = new TextField();
    private final TextField lineWidthTextField = new TextField();
    private final ColorPicker bandColorPicker = new ColorPicker();

    public TransitRangeDefinition(GridPane gridpane, int row, TransitRangeDef transitRangeDef) {

        id = transitRangeDef.getBandId();
        enabled.setSelected(transitRangeDef.isEnabled());
        bandNameTextField.setText(transitRangeDef.getBandName());

        upperRangeTextField.setText(Double.toString(transitRangeDef.getUpperRange()));
        upperRangeTextField.setPrefWidth(15);
        upperRangeTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.matches("^[+]?([0-9]{0,3}\\.[0-9]{0,2}[\s]+)")) {
                try {
                    double lowValue = Double.parseDouble(newValue);
                    rangeSlider.setHighValue(lowValue);
                } catch (NumberFormatException ignored) {
                }
            }
        });

        lowerRangeTextField.setText(Double.toString(transitRangeDef.getLowerRange()));
        lowerRangeTextField.setPrefWidth(15);
        lowerRangeTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.matches("^[+]?([0-9]{0,3}\\.[0-9]{0,2}[\s]+)")) {
                try {
                    double lowValue = Double.parseDouble(newValue);
                    rangeSlider.setLowValue(lowValue);
                } catch (NumberFormatException ignored) {
                }
            }
        });

        lineWidthTextField.setText(Double.toString(transitRangeDef.getLineWidth()));
        lineWidthTextField.setPrefWidth(5);
        bandColorPicker.setValue(transitRangeDef.getBandColor());
        bandColorPicker.setStyle("-fx-color-label-visible: false ;");

        rangeSlider.setLowValue(transitRangeDef.getLowerRange());
        rangeSlider.setHighValue(transitRangeDef.getUpperRange());
        rangeSlider.setPrefWidth(200);
        rangeSlider.setPrefHeight(40);
        rangeSlider.setMajorTickUnit(5.0);
        rangeSlider.setMinorTickCount(5);
        rangeSlider.setShowTickMarks(true);
        rangeSlider.setShowTickLabels(true);

        rangeSlider.lowValueProperty().addListener((observable, oldValue, newValue) -> {
            lowerRangeTextField.setText(String.format("%.2f", newValue.doubleValue()));
        });

        rangeSlider.highValueProperty().addListener((observable, oldValue, newValue) -> {
            upperRangeTextField.setText(String.format("%.2f", newValue.doubleValue()));
        });

        gridpane.add(enabled, 0, row);
        gridpane.add(bandNameTextField, 1, row);
        gridpane.add(lowerRangeTextField, 2, row);
        gridpane.add(rangeSlider, 3, row);
        gridpane.add(upperRangeTextField, 4, row);
        gridpane.add(lineWidthTextField, 5, row);
        gridpane.add(bandColorPicker, 6, row);
    }

    public TransitRangeDef getValue() {
        TransitRangeDef def = new TransitRangeDef();
        def.setBandColor(bandColorPicker.getValue());
        def.setBandId(id);
        def.setEnabled(enabled.isSelected());
        def.setBandName(bandNameTextField.getText());
        def.setLowerRange(Double.parseDouble(lowerRangeTextField.getText()));
        def.setUpperRange(Double.parseDouble(upperRangeTextField.getText()));
        def.setLineWidth(Double.parseDouble(lineWidthTextField.getText()));

        log.info("def = {}", def);
        return def;
    }


}
