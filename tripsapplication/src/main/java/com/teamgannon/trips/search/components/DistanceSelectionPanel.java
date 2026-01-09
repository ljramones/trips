package com.teamgannon.trips.search.components;

import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.control.RangeSlider;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
/**
 * Created by larrymitchell on 2017-06-25.
 */
@Slf4j
public class DistanceSelectionPanel extends BasePane {

    @FXML
    private Label distanceToEarthLabel;
    @FXML
    private RangeSlider d2EarthSlider;
    @FXML
    private TextField lowRangeTextField;
    @FXML
    private TextField highRangeTextField;

    private DistanceRange distanceRange = DistanceRange.builder().lowValue(0).highValue(20).min(0).max(20).build();
    private final double searchDistance;


    public DistanceSelectionPanel(double searchDistance, @NotNull DistanceRange distanceRange) {

        this.searchDistance = searchDistance;
        this.distanceRange = distanceRange;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("DistanceSelectionPanel.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load DistanceSelectionPanel.fxml", ex);
        }
    }

    @FXML
    private void initialize() {
        applyLabelStyle(distanceToEarthLabel);
        d2EarthSlider.setMin(distanceRange.getMin());
        d2EarthSlider.setMax(distanceRange.getMax());
        d2EarthSlider.setLowValue(distanceRange.getLowValue());
        d2EarthSlider.setHighValue(distanceRange.getHighValue());
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

        final Tooltip tooltip = new Tooltip();
        tooltip.setText(
                """
                        Please type a float point number for slider
                        then enter a space to set the value
                        """
        );

        lowRangeTextField.setPromptText("enter low range");
        lowRangeTextField.setTooltip(tooltip);
        lowRangeTextField.setText(String.format("%.2f", distanceRange.getLowValue()));
        lowRangeTextField.setPrefWidth(60);
        lowRangeTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.matches("^([0-9]{1,3}\\.?[0-9]{0,2})$")) {
                try {
                    double lowValue = Double.parseDouble(newValue);
                    d2EarthSlider.setLowValue(lowValue);
                } catch (NumberFormatException ignored) {
                }
            }
        });

        highRangeTextField.setPromptText("enter high range");
        highRangeTextField.setTooltip(tooltip);
        highRangeTextField.setText(String.format("%.2f", distanceRange.getHighValue()));
        highRangeTextField.setPrefWidth(60);
        highRangeTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            log.info("hi value={}", newValue);
            if (newValue.matches("^([0-9]{1,3}\\.?[0-9]{0,2})$")) {
                try {
                    double highValue = Double.parseDouble(newValue);
                    d2EarthSlider.setHighValue(highValue);
                } catch (NumberFormatException ignored) {
                }
            }
        });
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
