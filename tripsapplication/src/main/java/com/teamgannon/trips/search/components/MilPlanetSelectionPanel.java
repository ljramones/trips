package com.teamgannon.trips.search.components;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Military Planet selection panel
 * <p>
 * Created by larrymitchell on 2017-06-24.
 */
@Slf4j
public class MilPlanetSelectionPanel extends BasePane {

    @FXML
    private Label militaryPlanetsideLabel;
    @FXML
    private CheckBox yesNoMilPlan;
    @FXML
    private CheckBox aMilPlan;
    @FXML
    private CheckBox bMilPlan;
    @FXML
    private CheckBox cMilPlan;
    @FXML
    private CheckBox dMilPlan;
    @FXML
    private CheckBox eMilPlan;

    public MilPlanetSelectionPanel() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MilPlanetSelectionPanel.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load MilPlanetSelectionPanel.fxml", ex);
        }

    }

    @FXML
    private void initialize() {
        applyLabelStyle(militaryPlanetsideLabel);
        yesNoMilPlan.setSelected(false);
        enable(false);
        clearSelected();
        initEventHandler();
    }

    public boolean isSelected() {
        return yesNoMilPlan.isSelected();
    }

    public @NotNull List<String> getSelections() {
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
