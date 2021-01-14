package com.teamgannon.trips.search.components;

import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.controlsfx.control.RangeSlider;
import org.jetbrains.annotations.NotNull;

/**
 * Created by larrymitchell on 2017-06-25.
 */
public class DistanceSelectionPanel extends BasePane {

    private final @NotNull RangeSlider d2EarthSlider;
    private DistanceRange distanceRange = DistanceRange.builder().lowValue(0).highValue(20).min(0).max(20).build();

    TextField lowRangeTextField = new TextField();
    TextField highRangeTextField = new TextField();


    public DistanceSelectionPanel(double searchDistance, @NotNull DistanceRange distanceRange) {

        this.distanceRange = distanceRange;
        Label distanceToEarthLabel = createLabel("Radius from Center\n   (in ly)");


        d2EarthSlider = new RangeSlider(distanceRange.getMin(), distanceRange.getMax(), distanceRange.getLowValue(), distanceRange.getHighValue());
        d2EarthSlider.setPrefWidth(400);
        d2EarthSlider.setPrefHeight(25);
        d2EarthSlider.setHighValue(searchDistance);
        d2EarthSlider.setMajorTickUnit(5.0);
        d2EarthSlider.setMinorTickCount(5);
        d2EarthSlider.setShowTickMarks(true);
        d2EarthSlider.setShowTickLabels(true);
        d2EarthSlider.lowValueProperty().addListener((observable, oldValue, newValue) -> {
            lowRangeTextField.setText(String.format("%.2f", newValue.doubleValue()));
        });

        d2EarthSlider.highValueProperty().addListener((observable, oldValue, newValue) -> {
            highRangeTextField.setText(String.format("%.2f", newValue.doubleValue()));
        });

        lowRangeTextField.setPromptText("enter low range");
        lowRangeTextField.setText(String.format("%.2f", distanceRange.getLowValue()));
        lowRangeTextField.setPrefWidth(60);
        lowRangeTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("^(\\d+(\\.\\d{0,2})?|\\.?\\d{1,2})$")) {
                try {
                    double lowValue = Double.parseDouble(newValue);
                    d2EarthSlider.setLowValue(lowValue);
                } catch (NumberFormatException ignored) {
                }
            }
        });

        highRangeTextField.setPromptText("enter high range");
        highRangeTextField.setText(String.format("%.2f", distanceRange.getHighValue()));
        highRangeTextField.setPrefWidth(60);
        highRangeTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("^(\\d+(\\.\\d{0,2})?|\\.?\\d{1,2})$")) {
                try {
                    double highValue = Double.parseDouble(newValue);
                    d2EarthSlider.setHighValue(highValue);
                } catch (NumberFormatException ignored) {
                }
            }
        });

        planGrid.add(distanceToEarthLabel, 0, 0);
        planGrid.add(lowRangeTextField, 1, 0);
        planGrid.add(d2EarthSlider, 2, 0);
        planGrid.add(highRangeTextField, 3, 0);
    }


    public void setRange(@NotNull DistanceRange distanceRange) {
        this.distanceRange = distanceRange;
        d2EarthSlider.setLowValue(distanceRange.getLowValue());
        d2EarthSlider.setHighValue(distanceRange.getHighValue());
        d2EarthSlider.setMin(distanceRange.getMin());
        d2EarthSlider.setMax(distanceRange.getMax());
    }

    public DistanceRange getDistance() {
        return DistanceRange
                .builder()
                .lowValue(d2EarthSlider.getLowValue())
                .highValue(d2EarthSlider.getHighValue())
                .min(d2EarthSlider.getMin())
                .max(d2EarthSlider.getMax())
                .build();
    }

    public void setDataSetDescriptor(@NotNull DataSetDescriptor descriptor) {
        distanceRange.setMax(descriptor.getDistanceRange());
        d2EarthSlider.setMax(descriptor.getDistanceRange());
    }

}
