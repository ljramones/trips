package com.teamgannon.trips.search.components;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;

/**
 * Miscellaneous selection panel
 * <p>
 * Created by larrymitchell on 2017-06-25.
 */
public class MiscellaneousSelectionPanel extends BasePane {

    @FXML
    private Label otherLabel;
    @FXML
    private Label anomalyLabel;
    @FXML
    private CheckBox anomalyPresent;
    @FXML
    private CheckBox otherPresent;

    public MiscellaneousSelectionPanel() {
        loadFxml("MiscellaneousSelectionPanel.fxml");
    }

    @FXML
    private void initialize() {
        applyLabelStyle(otherLabel);
        applyLabelStyle(anomalyLabel);
    }

    public boolean isAnomalyPresent() {
        return anomalyPresent.isSelected();
    }

    public boolean isOtherPresent() {
        return otherPresent.isSelected();
    }

}
