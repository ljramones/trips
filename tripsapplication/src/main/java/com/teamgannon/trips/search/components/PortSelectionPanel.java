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
 * The port selection panel
 * <p>
 * Created by larrymitchell on 2017-06-24.
 */
@Slf4j
public class PortSelectionPanel extends BasePane {

    @FXML
    private Label portLabel;
    @FXML
    private CheckBox yesNoPort;
    @FXML
    private CheckBox aPort;
    @FXML
    private CheckBox bPort;
    @FXML
    private CheckBox cPort;
    @FXML
    private CheckBox dPort;
    @FXML
    private CheckBox ePort;

    public PortSelectionPanel() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("PortSelectionPanel.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load PortSelectionPanel.fxml", ex);
        }
    }

    @FXML
    private void initialize() {
        applyLabelStyle(portLabel);
        yesNoPort.setSelected(false);
        enable(false);
        clearSelected();
        initEventHandler();
    }


    public boolean isSelected() {
        return yesNoPort.isSelected();
    }

    public @NotNull List<String> getSelections() {
        List<String> selections = new ArrayList<>();
        if (aPort.isSelected()) {
            selections.add("A");
        }
        if (bPort.isSelected()) {
            selections.add("B");
        }
        if (cPort.isSelected()) {
            selections.add("C");
        }
        if (dPort.isSelected()) {
            selections.add("D");
        }
        if (ePort.isSelected()) {
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
                        yesNoPort.setSelected(true);
                        enable(true);
                    } else {
                        yesNoPort.setSelected(false);
                        clearSelected();
                        enable(false);
                    }
                }
            }
        };

        yesNoPort.setOnAction(eh);
    }

    /**
     * enable the checkbox selections
     *
     * @param flag the flag to use
     */
    private void enable(boolean flag) {
        aPort.setDisable(!flag);
        bPort.setDisable(!flag);
        cPort.setDisable(!flag);
        dPort.setDisable(!flag);
        ePort.setDisable(!flag);
    }

    /**
     * clear the selections
     */
    private void clearSelected() {
        aPort.setSelected(false);
        bPort.setSelected(false);
        cPort.setSelected(false);
        dPort.setSelected(false);
        ePort.setSelected(false);
    }
}
