package com.teamgannon.trips.search.components;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * The selection grid
 * <p>
 * Created by larrymitchell on 2017-06-24.
 */
@Slf4j
public class StellarClassSelectionPanel extends BasePane {

    private final CheckBox yesClassStar = new CheckBox("Yes?");

    private final CheckBox oClassStar = new CheckBox("O");
    private final CheckBox bClassStar = new CheckBox("B");
    private final CheckBox aClassStar = new CheckBox("A");
    private final CheckBox fClassStar = new CheckBox("F");
    private final CheckBox gClassStar = new CheckBox("G");
    private final CheckBox kClassStar = new CheckBox("K");
    private final CheckBox mClassStar = new CheckBox("M");
    private final CheckBox wClassStar = new CheckBox("W");
    private final CheckBox lClassStar = new CheckBox("L");
    private final CheckBox tClassStar = new CheckBox("T");
    private final CheckBox yClassStar = new CheckBox("Y");
    private final CheckBox cClassStar = new CheckBox("C");
    private final CheckBox sClassStar = new CheckBox("S");

    public StellarClassSelectionPanel() {

        yesClassStar.setSelected(false);

        planGrid.setHgap(10);
        planGrid.setVgap(10);

        Label stellarClassLabel = createLabel("Stellar Class");

        planGrid.add(stellarClassLabel, 0, 0);
        planGrid.add(yesClassStar, 1, 0);

        planGrid.add(oClassStar, 2, 0);
        planGrid.add(bClassStar, 2, 1);
        planGrid.add(aClassStar, 2, 2);

        planGrid.add(fClassStar, 3, 0);
        planGrid.add(gClassStar, 3, 1);
        planGrid.add(kClassStar, 3, 2);

        planGrid.add(mClassStar, 4, 0);
        planGrid.add(wClassStar, 4, 1);
        planGrid.add(lClassStar, 4, 2);

        planGrid.add(tClassStar, 5, 0);
        planGrid.add(yClassStar, 5, 1);
        planGrid.add(cClassStar, 5, 2);
        planGrid.add(sClassStar, 5, 3);

        enable(false);
        clearSelected();

        initEventHandler();
    }

    public @NotNull GridPane getPane() {
        return planGrid;
    }

    public @NotNull List<String> getSelection() {
        List<String> selections = new ArrayList<>();
        if (oClassStar.isSelected()) {
            selections.add(oClassStar.getText());
        }
        if (bClassStar.isSelected()) {
            selections.add(bClassStar.getText());
        }
        if (aClassStar.isSelected()) {
            selections.add(aClassStar.getText());
        }

        if (fClassStar.isSelected()) {
            selections.add(fClassStar.getText());
        }
        if (gClassStar.isSelected()) {
            selections.add(gClassStar.getText());
        }
        if (kClassStar.isSelected()) {
            selections.add(kClassStar.getText());
        }
        if (mClassStar.isSelected()) {
            selections.add(mClassStar.getText());
        }
        if (wClassStar.isSelected()) {
            selections.add(wClassStar.getText());
        }
        if (lClassStar.isSelected()) {
            selections.add(lClassStar.getText());
        }
        if (tClassStar.isSelected()) {
            selections.add(tClassStar.getText());
        }
        if (yClassStar.isSelected()) {
            selections.add(yClassStar.getText());
        }
        if (cClassStar.isSelected()) {
            selections.add(cClassStar.getText());
        }
        if (sClassStar.isSelected()) {
            selections.add(sClassStar.getText());
        }

        return selections;
    }


    public boolean isSelected() {
        return yesClassStar.isSelected();
    }


    public @NotNull List<String> getPolitySelections() {
        List<String> selections = new ArrayList<>();

        if (oClassStar.isSelected()) {
            selections.add(oClassStar.getText());
        }
        if (bClassStar.isSelected()) {
            selections.add(bClassStar.getText());
        }
        if (aClassStar.isSelected()) {
            selections.add(aClassStar.getText());
        }
        if (fClassStar.isSelected()) {
            selections.add(fClassStar.getText());
        }
        if (gClassStar.isSelected()) {
            selections.add(gClassStar.getText());
        }
        if (kClassStar.isSelected()) {
            selections.add(kClassStar.getText());
        }

        if (mClassStar.isSelected()) {
            selections.add(mClassStar.getText());
        }
        if (wClassStar.isSelected()) {
            selections.add(wClassStar.getText());
        }
        if (lClassStar.isSelected()) {
            selections.add(lClassStar.getText());
        }
        if (tClassStar.isSelected()) {
            selections.add(tClassStar.getText());
        }
        if (yClassStar.isSelected()) {
            selections.add(yClassStar.getText());
        }
        if (cClassStar.isSelected()) {
            selections.add(cClassStar.getText());
        }
        if (sClassStar.isSelected()) {
            selections.add(sClassStar.getText());
        }
        return selections;
    }

    /**
     * initialize the event handler
     */
    private void initEventHandler() {
        EventHandler<ActionEvent> eh = (EventHandler<ActionEvent>) event -> {
            if (event.getSource() instanceof CheckBox) {
                CheckBox chk = (CheckBox) event.getSource();
                log.debug("Action performed on checkbox " + chk.getText());
                if ("Yes?".equals(chk.getText())) {
                    if (isSelected()) {
                        yesClassStar.setSelected(true);
                        enable(true);
                    } else {
                        yesClassStar.setSelected(false);
                        clearSelected();
                        enable(false);
                    }
                }
            }
        };

        yesClassStar.setOnAction(eh);
    }

    /**
     * enable the checkbox selections
     *
     * @param flag the flag to use
     */
    private void enable(boolean flag) {
        oClassStar.setDisable(!flag);
        bClassStar.setDisable(!flag);
        aClassStar.setDisable(!flag);
        fClassStar.setDisable(!flag);
        gClassStar.setDisable(!flag);
        kClassStar.setDisable(!flag);
        mClassStar.setDisable(!flag);
        wClassStar.setDisable(!flag);
        lClassStar.setDisable(!flag);
        tClassStar.setDisable(!flag);
        yClassStar.setDisable(!flag);
        cClassStar.setDisable(!flag);
        sClassStar.setDisable(!flag);
    }

    /**
     * clear the selections
     */
    private void clearSelected() {
        oClassStar.setSelected(false);
        bClassStar.setSelected(false);
        aClassStar.setSelected(false);
        fClassStar.setSelected(false);
        gClassStar.setSelected(false);
        kClassStar.setSelected(false);
        mClassStar.setSelected(false);
        wClassStar.setSelected(false);
        lClassStar.setSelected(false);
        tClassStar.setSelected(false);
        yClassStar.setSelected(false);
        cClassStar.setSelected(false);
        sClassStar.setSelected(false);
    }

}
