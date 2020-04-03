package com.teamgannon.trips.search.components;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Fuel selection panel
 * <p>
 * Created by larrymitchell on 2017-06-24.
 */
@Slf4j
public class FuelSelectionPanel extends BaseSearchPane {


    private CheckBox yesNoFuel = new CheckBox("Yes?");
    private CheckBox h2Fuel = new CheckBox("H2");
    private CheckBox antiFuel = new CheckBox("Antimatter");
    private CheckBox gasGiantFuel = new CheckBox("Gas Giant");
    private CheckBox waterFuel = new CheckBox("Water World");

    private String enableStyle;


    public FuelSelectionPanel() {
        planGrid.setHgap(10); //horizontal gap in pixels
        planGrid.setVgap(10); //vertical gap in pixels

        enableStyle = h2Fuel.getStyle();

        planGrid.add(createLabel("Fuel"), 0, 0);
        planGrid.add(yesNoFuel, 1, 0);
        planGrid.add(h2Fuel, 2, 0);
        planGrid.add(antiFuel, 3, 0);
        planGrid.add(gasGiantFuel, 4, 0);
        planGrid.add(waterFuel, 5, 0);

        yesNoFuel.setSelected(false);
        enable(false);
        clearSelected();

        initEventHandler();
    }

    public boolean isSelected() {
        return yesNoFuel.isSelected();
    }

    public List<String> getSelections() {
        List<String> selections = new ArrayList<>();

        if (h2Fuel.isSelected()) {
            selections.add("H2");
        }
        if (antiFuel.isSelected()) {
            selections.add("Antimatter");
        }
        if (gasGiantFuel.isSelected()) {
            selections.add("Gas Giant");
        }
        if (waterFuel.isSelected()) {
            selections.add("Water World");
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
                        yesNoFuel.setSelected(true);
                        enable(true);
                    } else {
                        yesNoFuel.setSelected(false);
                        clearSelected();
                        enable(false);
                    }
                }
            }
        };

        yesNoFuel.setOnAction(eh);
    }

    /**
     * enable the checkbox selections
     *
     * @param flag the flag to use
     */
    private void enable(boolean flag) {
        h2Fuel.setDisable(!flag);
        antiFuel.setDisable(!flag);
        gasGiantFuel.setDisable(!flag);
        waterFuel.setDisable(!flag);
    }

    /**
     * clear the selections
     */
    private void clearSelected() {
        h2Fuel.setSelected(false);
        antiFuel.setSelected(false);
        gasGiantFuel.setSelected(false);
        waterFuel.setSelected(false);
    }

}
