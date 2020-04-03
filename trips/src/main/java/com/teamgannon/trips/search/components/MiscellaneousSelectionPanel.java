package com.teamgannon.trips.search.components;

import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;

/**
 * Miscellaneous selection panel
 * <p>
 * Created by larrymitchell on 2017-06-25.
 */
public class MiscellaneousSelectionPanel extends BaseSearchPane {

    private CheckBox anomalyPresent = new CheckBox("Present?");
    private CheckBox otherPresent = new CheckBox("present?");

    public MiscellaneousSelectionPanel() {
        planGrid.setHgap(10);
        planGrid.setVgap(10);

        Label otherLabel = createLabel("Other");

        planGrid.add(otherLabel, 0, 0);
        planGrid.add(otherPresent, 1, 0);

        Label anomalyLabel = createLabel("Anomaly");

        planGrid.add(anomalyLabel, 0, 1);
        planGrid.add(anomalyPresent, 1, 1);

    }

    public boolean isAnomalyPresent() {
        return anomalyPresent.isSelected();
    }

    public boolean isOtherPresent() {
        return otherPresent.isSelected();
    }

}
