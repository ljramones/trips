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
 * Created by larrymitchell on 2017-06-24.
 */
@Slf4j
public class MilSpaceSelectionPanel extends BasePane {

    @FXML
    private Label militarySpacesideLabel;
    @FXML
    private CheckBox yesNoMilSpace;
    @FXML
    private CheckBox aMilSpac;
    @FXML
    private CheckBox bMilSpac;
    @FXML
    private CheckBox cMilSpac;
    @FXML
    private CheckBox dMilSpac;
    @FXML
    private CheckBox eMilSpac;

    public MilSpaceSelectionPanel() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MilSpaceSelectionPanel.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load MilSpaceSelectionPanel.fxml", ex);
        }

    }

    @FXML
    private void initialize() {
        applyLabelStyle(militarySpacesideLabel);
        yesNoMilSpace.setSelected(false);
        enable(false);
        clearSelected();
        initEventHandler();
    }

    public boolean isSelected() {
        return yesNoMilSpace.isSelected();
    }

    public @NotNull List<String> getSelections() {
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
