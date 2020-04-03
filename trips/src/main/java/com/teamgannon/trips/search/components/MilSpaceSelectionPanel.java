package com.teamgannon.trips.search.components;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 * Created by larrymitchell on 2017-06-24.
 */
@Slf4j
public class MilSpaceSelectionPanel extends BaseSearchPane {

    private CheckBox yesNoMilSpace = new CheckBox("Yes?");
    private CheckBox aMilSpac = new CheckBox("A");
    private CheckBox bMilSpac = new CheckBox("B");
    private CheckBox cMilSpac = new CheckBox("C");
    private CheckBox dMilSpac = new CheckBox("D");
    private CheckBox eMilSpac = new CheckBox("E");

    public MilSpaceSelectionPanel() {
        planGrid.setHgap(10);
        planGrid.setVgap(10);

        Label militarySpacesideLabel = createLabel("Military Spaceside");

        planGrid.add(militarySpacesideLabel, 0, 0);
        planGrid.add(yesNoMilSpace, 1, 0);
        planGrid.add(aMilSpac, 2, 0);
        planGrid.add(bMilSpac, 3, 0);
        planGrid.add(cMilSpac, 4, 0);
        planGrid.add(dMilSpac, 5, 0);
        planGrid.add(eMilSpac, 6, 0);

        yesNoMilSpace.setSelected(false);
        enable(false);
        clearSelected();

        initEventHandler();

    }

    public boolean isSelected() {
        return yesNoMilSpace.isSelected();
    }

    public List<String> getSelections() {
        List<String> selections = new ArrayList<>();
        if (aMilSpac.isSelected()) {
            selections.add("A");
        }
        if (bMilSpac.isSelected()) {
            selections.add("B");
        }
        if (cMilSpac.isSelected()) {
            selections.add("C");
        }
        if (dMilSpac.isSelected()) {
            selections.add("D");
        }
        if (eMilSpac.isSelected()) {
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
                        yesNoMilSpace.setSelected(true);
                        enable(true);
                    } else {
                        yesNoMilSpace.setSelected(false);
                        clearSelected();
                        enable(false);
                    }
                }
            }
        };

        yesNoMilSpace.setOnAction(eh);
    }

    /**
     * enable the checkbox selections
     *
     * @param flag the flag to use
     */
    private void enable(boolean flag) {
        aMilSpac.setDisable(!flag);
        bMilSpac.setDisable(!flag);
        cMilSpac.setDisable(!flag);
        dMilSpac.setDisable(!flag);
        eMilSpac.setDisable(!flag);
    }

    /**
     * clear the selections
     */
    private void clearSelected() {
        aMilSpac.setSelected(false);
        bMilSpac.setSelected(false);
        cMilSpac.setSelected(false);
        dMilSpac.setSelected(false);
        eMilSpac.setSelected(false);
    }

}
