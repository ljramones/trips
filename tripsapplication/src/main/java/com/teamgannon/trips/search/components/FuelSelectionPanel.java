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
 * Fuel selection panel
 * <p>
 * Created by larrymitchell on 2017-06-24.
 */
@Slf4j
public class FuelSelectionPanel extends BasePane {


    @FXML
    private Label fuelLabel;
    @FXML
    private CheckBox yesNoFuel;
    @FXML
    private CheckBox h2Fuel;
    @FXML
    private CheckBox antiFuel;
    @FXML
    private CheckBox gasGiantFuel;
    @FXML
    private CheckBox waterFuel;


    public FuelSelectionPanel() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("FuelSelectionPanel.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load FuelSelectionPanel.fxml", ex);
        }
    }

    @FXML
    private void initialize() {
        applyLabelStyle(fuelLabel);
        yesNoFuel.setSelected(false);
        enable(false);
        clearSelected();
        initEventHandler();
    }

    public boolean isSelected() {
        return yesNoFuel.isSelected();
    }

    public @NotNull List<String> getSelections() {
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
