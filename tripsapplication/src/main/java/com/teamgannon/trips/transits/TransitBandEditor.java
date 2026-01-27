package com.teamgannon.trips.transits;

import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.control.RangeSlider;

import java.util.UUID;

/**
 * Self-contained editor component for a single transit band definition.
 * Creates an HBox with all controls for editing the band parameters.
 */
@Slf4j
public class TransitBandEditor {

    private static final double RANGE_MIN = TransitConstants.RANGE_MIN;
    private static final double RANGE_MAX = TransitConstants.RANGE_MAX;
    private static final double DEFAULT_LINE_WIDTH = TransitConstants.DEFAULT_LINE_WIDTH;

    @Getter
    private final HBox root;

    private UUID id;
    private final CheckBox enabledCheckBox = new CheckBox();
    private final TextField bandNameField = new TextField();
    private final TextField lowerRangeField = new TextField();
    private final TextField upperRangeField = new TextField();
    private final TextField lineWidthField = new TextField();
    private final RangeSlider rangeSlider = new RangeSlider(RANGE_MIN, RANGE_MAX, RANGE_MIN, RANGE_MAX);
    private final ColorPicker colorPicker = new ColorPicker();

    /**
     * Create a new transit band editor.
     *
     * @param transitRangeDef the initial values for the band
     */
    public TransitBandEditor(TransitRangeDef transitRangeDef) {
        this.id = transitRangeDef.getBandId();

        initializeControls(transitRangeDef);
        setupBindings();

        root = createLayout();
    }

    private void initializeControls(TransitRangeDef def) {
        enabledCheckBox.setSelected(def.isEnabled());

        bandNameField.setText(def.getBandName());
        bandNameField.setPrefWidth(80);

        lowerRangeField.setText(Double.toString(def.getLowerRange()));
        lowerRangeField.setPrefWidth(60);

        upperRangeField.setText(Double.toString(def.getUpperRange()));
        upperRangeField.setPrefWidth(60);

        lineWidthField.setText(Double.toString(def.getLineWidth()));
        lineWidthField.setPrefWidth(50);

        colorPicker.setValue(def.getBandColor());
        colorPicker.setStyle("-fx-color-label-visible: false;");
        colorPicker.setPrefWidth(60);

        rangeSlider.setLowValue(def.getLowerRange());
        rangeSlider.setHighValue(def.getUpperRange());
        rangeSlider.setPrefWidth(180);
        rangeSlider.setPrefHeight(40);
        rangeSlider.setMajorTickUnit(TransitConstants.RANGE_MAJOR_TICK);
        rangeSlider.setMinorTickCount(TransitConstants.RANGE_MINOR_TICK_COUNT);
        rangeSlider.setShowTickMarks(true);
        rangeSlider.setShowTickLabels(true);
    }

    private void setupBindings() {
        // Slider → Text field bindings
        rangeSlider.lowValueProperty().addListener((obs, oldVal, newVal) ->
                lowerRangeField.setText("%.2f".formatted(newVal.doubleValue())));

        rangeSlider.highValueProperty().addListener((obs, oldVal, newVal) ->
                upperRangeField.setText("%.2f".formatted(newVal.doubleValue())));

        // Text field → Slider bindings
        lowerRangeField.textProperty().addListener((obs, oldVal, newVal) ->
                updateSliderFromText(newVal, false));

        upperRangeField.textProperty().addListener((obs, oldVal, newVal) ->
                updateSliderFromText(newVal, true));
    }

    private HBox createLayout() {
        HBox hbox = new HBox(10);
        hbox.setPadding(new Insets(5));
        hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        HBox.setHgrow(rangeSlider, Priority.ALWAYS);

        hbox.getChildren().addAll(
                enabledCheckBox,
                bandNameField,
                lowerRangeField,
                rangeSlider,
                upperRangeField,
                lineWidthField,
                colorPicker
        );

        return hbox;
    }

    /**
     * Get the current values as a TransitRangeDef.
     *
     * @return the transit range definition with current values
     */
    public TransitRangeDef getValue() {
        TransitRangeDef def = new TransitRangeDef();
        def.setBandId(id);
        def.setEnabled(enabledCheckBox.isSelected());
        def.setBandName(bandNameField.getText());
        def.setBandColor(colorPicker.getValue());
        def.setLowerRange(parseDoubleOrDefault(lowerRangeField.getText(), rangeSlider.getLowValue()));
        def.setUpperRange(parseDoubleOrDefault(upperRangeField.getText(), rangeSlider.getHighValue()));
        def.setLineWidth(parseDoubleOrDefault(lineWidthField.getText(), DEFAULT_LINE_WIDTH));

        log.debug("getValue: {}", def);
        return def;
    }

    private void updateSliderFromText(String text, boolean isHigh) {
        if (text == null || text.trim().isEmpty()) {
            return;
        }
        try {
            double value = Double.parseDouble(text.trim());
            if (value >= rangeSlider.getMin() && value <= rangeSlider.getMax()) {
                if (isHigh) {
                    rangeSlider.setHighValue(value);
                } else {
                    rangeSlider.setLowValue(value);
                }
            }
        } catch (NumberFormatException ignored) {
            // Ignore invalid input while user is typing
        }
    }

    private double parseDoubleOrDefault(String text, double defaultValue) {
        if (text == null || text.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            log.warn("Could not parse '{}' as double, using default: {}", text, defaultValue);
            return defaultValue;
        }
    }
}
