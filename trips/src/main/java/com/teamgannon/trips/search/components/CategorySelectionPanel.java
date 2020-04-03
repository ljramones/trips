package com.teamgannon.trips.search.components;

import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

/**
 * Selection of
 * <p>
 * Created by larrymitchell on 2017-06-24.
 */
public class CategorySelectionPanel extends BaseSearchPane {

    private CheckBox realStars = new CheckBox("Real");
    private CheckBox fictionalStars = new CheckBox("Fictional");

    public CategorySelectionPanel() {
        realStars.setSelected(true);
        fictionalStars.setSelected(true);

        planGrid.setHgap(10);
        planGrid.setVgap(10);

        Label categoryLabel = createLabel("Category");

        planGrid.add(categoryLabel, 0, 0);
        planGrid.add(realStars, 1, 0);
        planGrid.add(fictionalStars, 2, 0);
    }

    public boolean isRealStars() {
        return realStars.isSelected();
    }

    public boolean isFictionalStars() {
        return fictionalStars.isSelected();
    }


}
