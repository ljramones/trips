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
 * population selection panel
 * <p>
 * Created by larrymitchell on 2017-06-24.
 */
@Slf4j
public class PopulationSelectionPanel extends BasePane {

    @FXML
    private Label populationLabel;
    @FXML
    private CheckBox yesNoPop;
    @FXML
    private CheckBox pop0;
    @FXML
    private CheckBox pop1;
    @FXML
    private CheckBox pop2;
    @FXML
    private CheckBox pop3;
    @FXML
    private CheckBox pop4;
    @FXML
    private CheckBox pop5;
    @FXML
    private CheckBox pop6;
    @FXML
    private CheckBox pop7;
    @FXML
    private CheckBox pop8;
    @FXML
    private CheckBox pop9;


    public PopulationSelectionPanel() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("PopulationSelectionPanel.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load PopulationSelectionPanel.fxml", ex);
        }

    }

    @FXML
    private void initialize() {
        applyLabelStyle(populationLabel);
        yesNoPop.setSelected(false);
        enable(false);
        clearSelected();
        initEventHandler();
    }

    public boolean isSelected() {
        return yesNoPop.isSelected();
    }

    public @NotNull List<String> getSelections() {
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
        EventHandler<ActionEvent> eh = event -> {
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
