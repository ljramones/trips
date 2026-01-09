package com.teamgannon.trips.search.components;

import com.teamgannon.trips.jpa.model.CivilizationDisplayPreferences;
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
 * Selection pane for polities
 * <p>
 * Created by larrymitchell on 2017-06-24.
 */
@Slf4j
public class PolitySelectionPanel extends BasePane {

    @FXML
    private Label polityLabel;
    @FXML
    private CheckBox yesPolity;

    @FXML
    private CheckBox polity1;
    @FXML
    private CheckBox polity2;
    @FXML
    private CheckBox polity3;
    @FXML
    private CheckBox polity4;
    @FXML
    private CheckBox polity5;
    @FXML
    private CheckBox polity6;
    @FXML
    private CheckBox polity7;
    @FXML
    private CheckBox polity8;
    @FXML
    private CheckBox polity9;
    @FXML
    private CheckBox polity10;
    @FXML
    private CheckBox polity11;

    public PolitySelectionPanel() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("PolitySelectionPanel.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load PolitySelectionPanel.fxml", ex);
        }
    }

    @FXML
    private void initialize() {
        applyLabelStyle(polityLabel);
        polity1.setText(CivilizationDisplayPreferences.TERRAN);
        polity2.setText(CivilizationDisplayPreferences.DORNANI);
        polity3.setText(CivilizationDisplayPreferences.KTOR);
        polity4.setText(CivilizationDisplayPreferences.ARAKUR);
        polity5.setText(CivilizationDisplayPreferences.HKHRKH);
        polity6.setText(CivilizationDisplayPreferences.SLAASRIITHI);
        polity7.setText(CivilizationDisplayPreferences.OTHER1);
        polity8.setText(CivilizationDisplayPreferences.OTHER2);
        polity9.setText(CivilizationDisplayPreferences.OTHER3);
        polity10.setText(CivilizationDisplayPreferences.OTHER4);
        polity11.setText(CivilizationDisplayPreferences.NONE);

        yesPolity.setSelected(false);
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
        if (polity11.isSelected()) {
            selections.add(polity11.getText());
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
        polity11.setDisable(!flag);
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
        polity11.setSelected(false);
    }


}
