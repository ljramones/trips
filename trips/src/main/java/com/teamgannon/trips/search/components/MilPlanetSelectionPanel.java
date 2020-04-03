package com.teamgannon.trips.search.components;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Military Planet selection panel
 * <p>
 * Created by larrymitchell on 2017-06-24.
 */
@Slf4j
public class MilPlanetSelectionPanel extends BaseSearchPane {

    private CheckBox yesNoMilPlan = new CheckBox("Yes?");
    private CheckBox aMilPlan = new CheckBox("A");
    private CheckBox bMilPlan = new CheckBox("B");
    private CheckBox cMilPlan = new CheckBox("C");
    private CheckBox dMilPlan = new CheckBox("D");
    private CheckBox eMilPlan = new CheckBox("E");

    public MilPlanetSelectionPanel() {
        planGrid.setHgap(10);
        planGrid.setVgap(10);

        Label militaryPlanetsideLabel = createLabel("Military Planetside");

        planGrid.add(militaryPlanetsideLabel, 0, 0);
        planGrid.add(yesNoMilPlan, 1, 0);
        planGrid.add(aMilPlan, 2, 0);
        planGrid.add(bMilPlan, 3, 0);
        planGrid.add(cMilPlan, 4, 0);
        planGrid.add(dMilPlan, 5, 0);
        planGrid.add(eMilPlan, 6, 0);

        yesNoMilPlan.setSelected(false);
        enable(false);
        clearSelected();

        initEventHandler();

    }

    public boolean isSelected() {
        return yesNoMilPlan.isSelected();
    }

    public List<String> getSelections() {
        List<String> selections = new ArrayList<>();
        if (aMilPlan.isSelected()) {
            selections.add("A");
        }
        if (bMilPlan.isSelected()) {
            selections.add("B");
        }
        if (cMilPlan.isSelected()) {
            selections.add("C");
        }
        if (dMilPlan.isSelected()) {
            selections.add("D");
        }
        if (eMilPlan.isSelected()) {
            selections.add("E");
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
                        yesNoMilPlan.setSelected(true);
                        enable(true);
                    } else {
                        yesNoMilPlan.setSelected(false);
                        clearSelected();
                        enable(false);
                    }
                }
            }
        };

        yesNoMilPlan.setOnAction(eh);
    }

    /**
     * enable the checkbox selections
     *
     * @param flag the flag to use
     */
    private void enable(boolean flag) {
        aMilPlan.setDisable(!flag);
        bMilPlan.setDisable(!flag);
        cMilPlan.setDisable(!flag);
        dMilPlan.setDisable(!flag);
        eMilPlan.setDisable(!flag);
    }

    /**
     * clear the selections
     */
    private void clearSelected() {
        aMilPlan.setSelected(false);
        bMilPlan.setSelected(false);
        cMilPlan.setSelected(false);
        dMilPlan.setSelected(false);
        eMilPlan.setSelected(false);
    }

}
