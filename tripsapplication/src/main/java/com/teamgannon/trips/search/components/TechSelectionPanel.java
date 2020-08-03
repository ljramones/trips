package com.teamgannon.trips.search.components;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * the tech seleciton panel
 * <p>
 * Created by larrymitchell on 2017-06-24.
 */
@Slf4j
public class TechSelectionPanel extends BasePane {

    private final CheckBox yesTech = new CheckBox("Yes?");
    private final CheckBox tech1 = new CheckBox("1");
    private final CheckBox tech2 = new CheckBox("2");
    private final CheckBox tech3 = new CheckBox("3");
    private final CheckBox tech4 = new CheckBox("4");
    private final CheckBox tech5 = new CheckBox("5");
    private final CheckBox tech6 = new CheckBox("6");
    private final CheckBox tech7 = new CheckBox("7");
    private final CheckBox tech8 = new CheckBox("8");
    private final CheckBox tech9 = new CheckBox("9");
    private final CheckBox techA = new CheckBox("A");
    private final CheckBox techB = new CheckBox("B");
    private final CheckBox techC = new CheckBox("C");
    private final CheckBox techD = new CheckBox("D");
    private final CheckBox techE = new CheckBox("E");

    public TechSelectionPanel() {
        planGrid.setHgap(10);
        planGrid.setVgap(10);

        Label techLabel = createLabel("Tech");

        planGrid.add(techLabel, 0, 0);

        planGrid.add(yesTech, 1, 0);

        planGrid.add(tech1, 2, 0);
        planGrid.add(tech2, 3, 0);
        planGrid.add(tech3, 4, 0);
        planGrid.add(tech4, 5, 0);
        planGrid.add(tech5, 6, 0);
        planGrid.add(tech6, 7, 0);
        planGrid.add(tech7, 8, 0);

        planGrid.add(tech8, 2, 1);
        planGrid.add(tech9, 3, 1);
        planGrid.add(techA, 4, 1);
        planGrid.add(techB, 5, 1);
        planGrid.add(techC, 6, 1);
        planGrid.add(techD, 7, 1);
        planGrid.add(techE, 8, 1);

        yesTech.setSelected(false);
        enable(false);
        clearSelected();

        initEventHandler();

    }


    public boolean isSelected() {
        return yesTech.isSelected();
    }

    public List<String> getSelections() {
        List<String> selections = new ArrayList<>();
        if (tech1.isSelected()) {
            selections.add("tech1");
        }
        if (tech2.isSelected()) {
            selections.add("tech2");
        }
        if (tech3.isSelected()) {
            selections.add("tech3");
        }
        if (tech4.isSelected()) {
            selections.add("tech4");
        }
        if (tech5.isSelected()) {
            selections.add("tech5");
        }
        if (tech6.isSelected()) {
            selections.add("tech6");
        }
        if (tech7.isSelected()) {
            selections.add("tech7");
        }
        if (tech8.isSelected()) {
            selections.add("tech8");
        }
        if (tech9.isSelected()) {
            selections.add("tech9");
        }
        if (techA.isSelected()) {
            selections.add("techA");
        }
        if (techB.isSelected()) {
            selections.add("techB");
        }
        if (techC.isSelected()) {
            selections.add("techC");
        }
        if (techD.isSelected()) {
            selections.add("techD");
        }
        if (techE.isSelected()) {
            selections.add("techE");
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
                        yesTech.setSelected(true);
                        enable(true);
                    } else {
                        yesTech.setSelected(false);
                        clearSelected();
                        enable(false);
                    }
                }
            }
        };

        yesTech.setOnAction(eh);
    }

    /**
     * enable the checkbox selections
     *
     * @param flag the flag to use
     */
    private void enable(boolean flag) {
        tech1.setDisable(!flag);
        tech2.setDisable(!flag);
        tech3.setDisable(!flag);
        tech4.setDisable(!flag);
        tech5.setDisable(!flag);
        tech6.setDisable(!flag);
        tech7.setDisable(!flag);
        tech8.setDisable(!flag);
        tech9.setDisable(!flag);
        techA.setDisable(!flag);
        techB.setDisable(!flag);
        techC.setDisable(!flag);
        techD.setDisable(!flag);
        techE.setDisable(!flag);
    }

    /**
     * clear the selections
     */
    private void clearSelected() {
        tech1.setSelected(false);
        tech2.setSelected(false);
        tech3.setSelected(false);
        tech4.setSelected(false);
        tech5.setSelected(false);
        tech6.setSelected(false);
        tech7.setSelected(false);
        tech8.setSelected(false);
        tech9.setSelected(false);
        techA.setSelected(false);
        techB.setSelected(false);
        techC.setSelected(false);
        techD.setSelected(false);
        techE.setSelected(false);
    }


}
