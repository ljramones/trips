package com.teamgannon.trips.search.components;

import com.teamgannon.trips.jpa.model.CivilizationDisplayPreferences;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Selection pane for polities
 * <p>
 * Created by larrymitchell on 2017-06-24.
 */
@Slf4j
public class PolitySelectionPanel extends BasePane {

    private final CheckBox yesPolity = new CheckBox("Yes?");

    private final CheckBox polity1 = new CheckBox(CivilizationDisplayPreferences.TERRAN);
    private final CheckBox polity2 = new CheckBox(CivilizationDisplayPreferences.DORNANI);
    private final CheckBox polity3 = new CheckBox(CivilizationDisplayPreferences.KTOR);
    private final CheckBox polity4 = new CheckBox(CivilizationDisplayPreferences.ARAKUR);
    private final CheckBox polity5 = new CheckBox(CivilizationDisplayPreferences.HKHRKH);
    private final CheckBox polity6 = new CheckBox(CivilizationDisplayPreferences.SLAASRIITHI);
    private final CheckBox polity7 = new CheckBox(CivilizationDisplayPreferences.OTHER1);
    private final CheckBox polity8 = new CheckBox(CivilizationDisplayPreferences.OTHER2);
    private final CheckBox polity9 = new CheckBox( CivilizationDisplayPreferences.OTHER3);
    private final CheckBox polity10 = new CheckBox(CivilizationDisplayPreferences.OTHER4);

    public PolitySelectionPanel() {

        yesPolity.setSelected(false);

        planGrid.setHgap(10);
        planGrid.setVgap(10);

        Label polityLabel = createLabel("Polity");

        planGrid.add(polityLabel, 0, 0);
        planGrid.add(yesPolity, 1, 0);

        planGrid.add(polity1, 2, 0);
        planGrid.add(polity2, 2, 1);
        planGrid.add(polity3, 2, 2);

        planGrid.add(polity4, 3, 0);
        planGrid.add(polity5, 3, 1);
        planGrid.add(polity6, 3, 2);

        planGrid.add(polity7, 4, 0);
        planGrid.add(polity8, 4, 1);
        planGrid.add(polity9, 4, 2);
        planGrid.add(polity10, 4, 3);

        enable(false);
        clearSelected();

        initEventHandler();
    }

    public boolean isSelected() {
        return yesPolity.isSelected();
    }

    public @NotNull List<String> getPolitySelections() {
        List<String> selections = new ArrayList<>();

        if (polity1.isSelected()) {
            selections.add(polity1.getText());
        }
        if (polity2.isSelected()) {
            selections.add(polity2.getText());
        }
        if (polity3.isSelected()) {
            selections.add(polity3.getText());
        }
        if (polity4.isSelected()) {
            selections.add(polity4.getText());
        }
        if (polity5.isSelected()) {
            selections.add(polity5.getText());
        }
        if (polity6.isSelected()) {
            selections.add(polity6.getText());
        }

        if (polity7.isSelected()) {
            selections.add(polity7.getText());
        }
        if (polity8.isSelected()) {
            selections.add(polity8.getText());
        }
        if (polity9.isSelected()) {
            selections.add(polity9.getText());
        }
        if (polity10.isSelected()) {
            selections.add(polity10.getText());
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
                        yesPolity.setSelected(true);
                        enable(true);
                    } else {
                        yesPolity.setSelected(false);
                        clearSelected();
                        enable(false);
                    }
                }
            }
        };

        yesPolity.setOnAction(eh);
    }

    /**
     * enable the checkbox selections
     *
     * @param flag the flag to use
     */
    private void enable(boolean flag) {
        polity1.setDisable(!flag);
        polity2.setDisable(!flag);
        polity3.setDisable(!flag);
        polity4.setDisable(!flag);
        polity5.setDisable(!flag);
        polity6.setDisable(!flag);
        polity7.setDisable(!flag);
        polity8.setDisable(!flag);
        polity9.setDisable(!flag);
        polity10.setDisable(!flag);
    }

    /**
     * clear the selections
     */
    private void clearSelected() {
        polity1.setSelected(false);
        polity2.setSelected(false);
        polity3.setSelected(false);
        polity4.setSelected(false);
        polity5.setSelected(false);
        polity6.setSelected(false);
        polity7.setSelected(false);
        polity8.setSelected(false);
        polity9.setSelected(false);
        polity10.setSelected(false);
    }


}
