package com.teamgannon.trips.dialogs;

import javafx.scene.control.Dialog;
import javafx.scene.control.Slider;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * This dialog is concerned with providing a filter for plotting
 * <p>
 * Created by larrymitchell on 2017-02-23.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PlottingFilterDialog extends Dialog {

    private double distanceFromEarth = 10.0;

    private Slider distanceSlider = new Slider();

    public PlottingFilterDialog() {

        // setup slider range and default setting
        distanceSlider.setMin(0);
        distanceSlider.setMax(200);
        distanceSlider.setValue(10);

        distanceSlider.setShowTickLabels(true);
        distanceSlider.setShowTickMarks(true);

    }
}
