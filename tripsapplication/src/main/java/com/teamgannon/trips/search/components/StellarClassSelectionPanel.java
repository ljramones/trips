package com.teamgannon.trips.search.components;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static javafx.scene.text.FontWeight.MEDIUM;

/**
 * The selection grid
 * <p>
 * Created by larrymitchell on 2017-06-24.
 */
@Slf4j
public class StellarClassSelectionPanel extends BasePane {

    private final Font secondaryFont = Font.font("Arial", MEDIUM, FontPosture.ITALIC, 13);

    @FXML
    private Label stellarClassLabel;
    @FXML
    private CheckBox yesClassStar;
    @FXML
    private CheckBox commonStars;
    @FXML
    private CheckBox oClassStar;
    @FXML
    private CheckBox bClassStar;
    @FXML
    private CheckBox aClassStar;
    @FXML
    private CheckBox fClassStar;
    @FXML
    private CheckBox gClassStar;
    @FXML
    private CheckBox kClassStar;
    @FXML
    private CheckBox mClassStar;
    @FXML
    private CheckBox otherStars;
    @FXML
    private CheckBox wClassStar;
    @FXML
    private CheckBox lClassStar;
    @FXML
    private CheckBox tClassStar;
    @FXML
    private CheckBox yClassStar;
    @FXML
    private CheckBox cClassStar;
    @FXML
    private CheckBox sClassStar;
    @FXML
    private GridPane commonStarsPane;
    @FXML
    private GridPane otherStarsPane;

    public StellarClassSelectionPanel() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("StellarClassSelectionPanel.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load StellarClassSelectionPanel.fxml", ex);
        }
    }

    @FXML
    private void initialize() {
        applyLabelStyle(stellarClassLabel);
        commonStars.setFont(secondaryFont);
        otherStars.setFont(secondaryFont);
        yesClassStar.setSelected(false);
        enable(false);
        clearSelected();
        initEventHandler();
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



    public @NotNull List<String> getStarSelections() {
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
        EventHandler<ActionEvent> eh = event -> {
            if (event.getSource() instanceof CheckBox chk) {
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
                if ("Common Stars?".equals(chk.getText())) {
                    if (commonStars.isSelected()) {
                        log.info("Select Common stars on");
                        commonStars.setSelected(true);
                        selectCommon(true);
                    } else {
                        log.info("Select Common stars off");
                        commonStars.setSelected(false);
                        selectCommon(false);
                    }
                }
                if ("Other Stars?".equals(chk.getText())) {
                    if (otherStars.isSelected()) {
                        log.info("Select Other stars on");
                        otherStars.setSelected(true);
                        selectOther(true);
                    } else{
                        log.info("Select Other stars off");
                        otherStars.setSelected(false);
                        selectOther(false);
                    }
                }
            }
        };

        yesClassStar.setOnAction(eh);
        commonStars.setOnAction(eh);
        otherStars.setOnAction(eh);
    }

    private void selectCommon(boolean flag) {
        log.info("setting common stars to {}", flag);
        oClassStar.setSelected(flag);
        bClassStar.setSelected(flag);
        aClassStar.setSelected(flag);
        fClassStar.setSelected(flag);
        gClassStar.setSelected(flag);
        kClassStar.setSelected(flag);
        mClassStar.setSelected(flag);
    }


    private void selectOther(boolean flag) {
        log.info("setting other stars to {}", flag);
        wClassStar.setSelected(flag);
        lClassStar.setSelected(flag);
        tClassStar.setSelected(flag);
        yClassStar.setSelected(flag);
        cClassStar.setSelected(flag);
        sClassStar.setSelected(flag);
    }


    /**
     * enable the checkbox selections
     *
     * @param flag the flag to use
     */
    private void enable(boolean flag) {
        commonStars.setDisable(!flag);
        otherStars.setDisable(!flag);
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
        commonStars.setSelected(false);
        otherStars.setSelected(false);
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
