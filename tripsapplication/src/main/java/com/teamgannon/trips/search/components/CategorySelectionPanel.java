package com.teamgannon.trips.search.components;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;

/**
 * Selection of
 * <p>
 * Created by larrymitchell on 2017-06-24.
 */
public class CategorySelectionPanel extends BasePane {

    @FXML
    private Label categoryLabel;
    @FXML
    private CheckBox realStars;
    @FXML
    private CheckBox fictionalStars;

    public CategorySelectionPanel() {
        loadFxml("CategorySelectionPanel.fxml");
    }

    @FXML
    private void initialize() {
        applyLabelStyle(categoryLabel);
        realStars.setSelected(true);
        fictionalStars.setSelected(false);
    }

    public boolean isRealStars() {
        return realStars.isSelected();
    }

    public boolean isFictionalStars() {
        return fictionalStars.isSelected();
    }


}
