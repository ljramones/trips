package com.teamgannon.trips.search.components;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.paint.Color;

/**
 * Created by larrymitchell on 2017-06-25.
 */
public class DistanceSelectionPanel extends BasePane {

    private final static Color textColor = Color.BLACK;
    private final Slider d2EarthSlider;
    private final Label d2EarthLabel;
    private double distanceRange;


    public DistanceSelectionPanel(double searchDistance, double distanceRange) {

        this.distanceRange = distanceRange;
        Label distanceToEarth = createLabel("Radius from Earth in ly");

        d2EarthSlider = new Slider(0, distanceRange, distanceRange);
        d2EarthSlider.setPrefWidth(400);
        d2EarthSlider.setValue(searchDistance);

        d2EarthSlider.valueProperty().addListener(new ChangeListener<>() {
            public void changed(ObservableValue<? extends Number> ov,
                                Number old_val, Number new_val) {

                // set distance to earth
                d2EarthLabel.setText(String.format("%.2f of %.2f (ly)", new_val, getDistanceValue()));
            }


        });

        d2EarthLabel = new Label(String.format("%.2f of %.2f (ly)",
                d2EarthSlider.getValue(), distanceRange));

        d2EarthLabel.setTextFill(textColor);

        planGrid.add(distanceToEarth, 0, 0);
        planGrid.add(d2EarthSlider, 1, 0);
        planGrid.add(d2EarthLabel, 2, 0);
    }

    private double getDistanceValue() {
        return distanceRange;
    }

    public void setMaxRange(double distanceRange) {
        this.distanceRange = distanceRange;
        d2EarthSlider.setMax(distanceRange);
        d2EarthLabel.setText(String.format("%.2f of %.2f (ly)",
                d2EarthSlider.getValue(), distanceRange));
    }

    public double getDistance() {
        return d2EarthSlider.getValue();
    }

}
