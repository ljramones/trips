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
 * World Selection panel
 * <p>
 * Created by larrymitchell on 2017-06-24.
 */
@Slf4j
public class WorldSelectionPanel extends BasePane {


    @FXML
    private Label worldLabel;
    @FXML
    private CheckBox yesNoWorld;
    @FXML
    private CheckBox greenWorld;
    @FXML
    private CheckBox greyWorld;
    @FXML
    private CheckBox brownWorld;


    public WorldSelectionPanel() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("WorldSelectionPanel.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load WorldSelectionPanel.fxml", ex);
        }

    }

    @FXML
    private void initialize() {
        applyLabelStyle(worldLabel);
        yesNoWorld.setSelected(false);
        enable(false);
        clearSelected();
        initEventHandler();
    }

    public boolean isSelected() {
        return yesNoWorld.isSelected();
    }

    public @NotNull List<String> getSelections() {
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
