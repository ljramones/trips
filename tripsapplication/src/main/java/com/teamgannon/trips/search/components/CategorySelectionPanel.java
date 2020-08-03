package com.teamgannon.trips.search.components;

import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;

/**
 * Selection of
 * <p>
 * Created by larrymitchell on 2017-06-24.
 */
public class CategorySelectionPanel extends BasePane {

    private final CheckBox realStars = new CheckBox("Real");
    private final CheckBox fictionalStars = new CheckBox("Fictional");

    public CategorySelectionPanel() {
        realStars.setSelected(true);
        fictionalStars.setSelected(false);

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
