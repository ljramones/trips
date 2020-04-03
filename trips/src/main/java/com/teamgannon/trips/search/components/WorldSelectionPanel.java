package com.teamgannon.trips.search.components;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * World Selection panel
 * <p>
 * Created by larrymitchell on 2017-06-24.
 */
@Slf4j
public class WorldSelectionPanel extends BaseSearchPane {


    private CheckBox yesNoWorld = new CheckBox("Yes?");
    private CheckBox greenWorld = new CheckBox("Green");
    private CheckBox greyWorld = new CheckBox("Grey");
    private CheckBox brownWorld = new CheckBox("Brown");


    public WorldSelectionPanel() {
        planGrid.setHgap(10);
        planGrid.setVgap(10);

        Label worldLabel = createLabel("World Present");

        planGrid.add(worldLabel, 0, 0);
        planGrid.add(yesNoWorld, 1, 0);
        planGrid.add(greenWorld, 2, 0);
        planGrid.add(greyWorld, 3, 0);
        planGrid.add(brownWorld, 4, 0);

        yesNoWorld.setSelected(false);
        enable(false);
        clearSelected();

        initEventHandler();

    }

    public boolean isSelected() {
        return yesNoWorld.isSelected();
    }

    public List<String> getSelections() {
        List<String> selections = new ArrayList<>();
        if (greenWorld.isSelected()) {
            selections.add("Green");
        }
        if (greyWorld.isSelected()) {
            selections.add("Grey");
        }
        if (brownWorld.isSelected()) {
            selections.add("Brown");
        }
        return selections;
    }


    /**
     * initialize the event handler
     */
    private void initEventHandler() {
        EventHandler eh = (EventHandler<ActionEvent>) event -> {
            if (event.getSource() instanceof CheckBox) {
                CheckBox chk = (CheckBox) event.getSource();
                log.debug("Action performed on checkbox " + chk.getText());
                if ("Yes?".equals(chk.getText())) {
                    if (isSelected()) {
                        yesNoWorld.setSelected(true);
                        enable(true);
                    } else {
                        yesNoWorld.setSelected(false);
                        clearSelected();
                        enable(false);
                    }
                }
            }
        };

        yesNoWorld.setOnAction(eh);
    }

    /**
     * enable the checkbox selections
     *
     * @param flag the flag to use
     */
    private void enable(boolean flag) {
        greenWorld.setDisable(!flag);
        greyWorld.setDisable(!flag);
        brownWorld.setDisable(!flag);
    }

    /**
     * clear the selections
     */
    private void clearSelected() {
        greenWorld.setSelected(false);
        greyWorld.setSelected(false);
        brownWorld.setSelected(false);
    }

}
