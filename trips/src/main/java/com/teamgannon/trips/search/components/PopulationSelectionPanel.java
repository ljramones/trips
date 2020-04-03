package com.teamgannon.trips.search.components;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * populaiton selection panel
 * <p>
 * Created by larrymitchell on 2017-06-24.
 */
@Slf4j
public class PopulationSelectionPanel extends BaseSearchPane {

    private CheckBox yesNoPop = new CheckBox("Yes?");
    private CheckBox pop0 = new CheckBox("0");
    private CheckBox pop1 = new CheckBox("1");
    private CheckBox pop2 = new CheckBox("2");
    private CheckBox pop3 = new CheckBox("3");
    private CheckBox pop4 = new CheckBox("4");
    private CheckBox pop5 = new CheckBox("5");
    private CheckBox pop6 = new CheckBox("6");
    private CheckBox pop7 = new CheckBox("7");
    private CheckBox pop8 = new CheckBox("8");
    private CheckBox pop9 = new CheckBox("9");


    public PopulationSelectionPanel() {
        planGrid.setHgap(10);
        planGrid.setVgap(10);

        Label populationLabel = createLabel("Population(Billions)");

        planGrid.add(populationLabel, 0, 0);

        planGrid.add(yesNoPop, 1, 0);

        planGrid.add(pop0, 2, 0);
        planGrid.add(pop1, 3, 0);
        planGrid.add(pop2, 4, 0);
        planGrid.add(pop3, 5, 0);
        planGrid.add(pop4, 6, 0);

        planGrid.add(pop5, 2, 1);
        planGrid.add(pop6, 3, 1);
        planGrid.add(pop7, 4, 1);
        planGrid.add(pop8, 5, 1);
        planGrid.add(pop9, 6, 1);

        yesNoPop.setSelected(false);
        enable(false);
        clearSelected();

        initEventHandler();

    }

    public boolean isSelected() {
        return yesNoPop.isSelected();
    }

    public List<String> getSelections() {
        List<String> selections = new ArrayList<>();
        if (pop0.isSelected()) {
            selections.add("0");
        }
        if (pop1.isSelected()) {
            selections.add("1");
        }
        if (pop2.isSelected()) {
            selections.add("2");
        }
        if (pop3.isSelected()) {
            selections.add("3");
        }
        if (pop4.isSelected()) {
            selections.add("4");
        }
        if (pop5.isSelected()) {
            selections.add("5");
        }
        if (pop6.isSelected()) {
            selections.add("6");
        }
        if (pop7.isSelected()) {
            selections.add("7");
        }
        if (pop8.isSelected()) {
            selections.add("8");
        }
        if (pop9.isSelected()) {
            selections.add("9");
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
                        yesNoPop.setSelected(true);
                        enable(true);
                    } else {
                        yesNoPop.setSelected(false);
                        clearSelected();
                        enable(false);
                    }
                }
            }
        };

        yesNoPop.setOnAction(eh);
    }

    /**
     * enable the checkbox selections
     *
     * @param flag the flag to use
     */
    private void enable(boolean flag) {
        pop0.setDisable(!flag);
        pop1.setDisable(!flag);
        pop2.setDisable(!flag);
        pop3.setDisable(!flag);
        pop4.setDisable(!flag);
        pop5.setDisable(!flag);
        pop6.setDisable(!flag);
        pop7.setDisable(!flag);
        pop8.setDisable(!flag);
        pop9.setDisable(!flag);
    }

    /**
     * clear the selections
     */
    private void clearSelected() {
        pop0.setSelected(false);
        pop1.setSelected(false);
        pop2.setSelected(false);
        pop3.setSelected(false);
        pop4.setSelected(false);
        pop5.setSelected(false);
        pop6.setSelected(false);
        pop7.setSelected(false);
        pop8.setSelected(false);
        pop9.setSelected(false);
    }

}
