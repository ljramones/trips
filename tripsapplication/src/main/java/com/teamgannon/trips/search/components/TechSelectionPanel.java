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
 * the tech seleciton panel
 * <p>
 * Created by larrymitchell on 2017-06-24.
 */
@Slf4j
public class TechSelectionPanel extends BasePane {

    @FXML
    private Label techLabel;
    @FXML
    private CheckBox yesTech;
    @FXML
    private CheckBox tech1;
    @FXML
    private CheckBox tech2;
    @FXML
    private CheckBox tech3;
    @FXML
    private CheckBox tech4;
    @FXML
    private CheckBox tech5;
    @FXML
    private CheckBox tech6;
    @FXML
    private CheckBox tech7;
    @FXML
    private CheckBox tech8;
    @FXML
    private CheckBox tech9;
    @FXML
    private CheckBox techA;
    @FXML
    private CheckBox techB;
    @FXML
    private CheckBox techC;
    @FXML
    private CheckBox techD;
    @FXML
    private CheckBox techE;

    public TechSelectionPanel() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("TechSelectionPanel.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load TechSelectionPanel.fxml", ex);
        }

    }

    @FXML
    private void initialize() {
        applyLabelStyle(techLabel);
        yesTech.setSelected(false);
        enable(false);
        clearSelected();
        initEventHandler();
    }


    public boolean isSelected() {
        return yesTech.isSelected();
    }

    public @NotNull List<String> getSelections() {
        List<String> selections = new ArrayList<>();
        if (tech1.isSelected()) {
            selections.add("1");
        }
        if (tech2.isSelected()) {
            selections.add("2");
        }
        if (tech3.isSelected()) {
            selections.add("3");
        }
        if (tech4.isSelected()) {
            selections.add("4");
        }
        if (tech5.isSelected()) {
            selections.add("5");
        }
        if (tech6.isSelected()) {
            selections.add("6");
        }
        if (tech7.isSelected()) {
            selections.add("7");
        }
        if (tech8.isSelected()) {
            selections.add("8");
        }
        if (tech9.isSelected()) {
            selections.add("9");
        }
        if (techA.isSelected()) {
            selections.add("A");
        }
        if (techB.isSelected()) {
            selections.add("B");
        }
        if (techC.isSelected()) {
            selections.add("C");
        }
        if (techD.isSelected()) {
            selections.add("D");
        }
        if (techE.isSelected()) {
            selections.add("E");
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
