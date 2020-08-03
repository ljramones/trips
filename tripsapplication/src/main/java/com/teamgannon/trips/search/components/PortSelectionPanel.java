package com.teamgannon.trips.search.components;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * The port selection panel
 * <p>
 * Created by larrymitchell on 2017-06-24.
 */
@Slf4j
public class PortSelectionPanel extends BasePane {

    private final CheckBox yesNoPort = new CheckBox("Yes?");
    private final CheckBox aPort = new CheckBox("A");
    private final CheckBox bPort = new CheckBox("B");
    private final CheckBox cPort = new CheckBox("C");
    private final CheckBox dPort = new CheckBox("D");
    private final CheckBox ePort = new CheckBox("E");

    public PortSelectionPanel() {
        planGrid.setHgap(10);
        planGrid.setVgap(10);

        Label portLabel = createLabel("Port Present");

        planGrid.add(portLabel, 0, 0);
        planGrid.add(yesNoPort, 1, 0);
        planGrid.add(aPort, 2, 0);
        planGrid.add(bPort, 3, 0);
        planGrid.add(cPort, 4, 0);
        planGrid.add(dPort, 5, 0);
        planGrid.add(ePort, 6, 0);

        yesNoPort.setSelected(false);
        enable(false);
        clearSelected();

        initEventHandler();
    }


    public boolean isSelected() {
        return yesNoPort.isSelected();
    }

    public List<String> getSelections() {
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
