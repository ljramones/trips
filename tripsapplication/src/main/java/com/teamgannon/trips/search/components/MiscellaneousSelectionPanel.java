package com.teamgannon.trips.search.components;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;

import java.io.IOException;

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
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MiscellaneousSelectionPanel.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load MiscellaneousSelectionPanel.fxml", ex);
        }
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
