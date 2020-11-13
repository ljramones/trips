package com.teamgannon.trips.search.components;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.paint.Color;
import org.controlsfx.control.RangeSlider;

/**
 * Created by larrymitchell on 2017-06-25.
 */
public class DistanceSelectionPanel extends BasePane {

    private final RangeSlider d2EarthSlider;
    private DistanceRange distanceRange;


    public DistanceSelectionPanel(double searchDistance, DistanceRange distanceRange) {

        this.distanceRange = distanceRange;
        Label distanceToEarth = createLabel("Radius from Earth in ly");

        d2EarthSlider = new RangeSlider(distanceRange.getMin(), distanceRange.getMax(), distanceRange.getLowValue(), distanceRange.getHighValue());
        d2EarthSlider.setPrefWidth(400);
        d2EarthSlider.setPrefHeight(25);
        d2EarthSlider.setHighValue(searchDistance);
        d2EarthSlider.setMajorTickUnit(5.0);
        d2EarthSlider.setMinorTickCount(5);
        d2EarthSlider.setShowTickMarks(true);
        d2EarthSlider.setShowTickLabels(true);

        planGrid.add(distanceToEarth, 0, 0);
        planGrid.add(d2EarthSlider, 1, 0);
    }


    public void setRange(DistanceRange distanceRange) {
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

}
