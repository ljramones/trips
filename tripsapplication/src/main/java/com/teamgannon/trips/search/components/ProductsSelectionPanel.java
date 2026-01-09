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
 * products selection panel
 * <p>
 * Created by larrymitchell on 2017-06-24.
 */
@Slf4j
public class ProductsSelectionPanel extends BasePane {

    @FXML
    private Label productsLabel;
    @FXML
    private CheckBox yesNoProducts;
    @FXML
    private CheckBox agriProd;
    @FXML
    private CheckBox indusProd;
    @FXML
    private CheckBox svcsProd;
    @FXML
    private CheckBox rawMatProd;
    @FXML
    private CheckBox bioProd;
    @FXML
    private CheckBox fossilFuelProd;
    @FXML
    private CheckBox finishGoodsProd;
    @FXML
    private CheckBox hiTechProd;
    @FXML
    private CheckBox uniqProd;
    @FXML
    private CheckBox enerProd;

    public ProductsSelectionPanel() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ProductsSelectionPanel.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load ProductsSelectionPanel.fxml", ex);
        }

    }

    @FXML
    private void initialize() {
        applyLabelStyle(productsLabel);
        yesNoProducts.setSelected(false);
        enable(false);
        clearSelected();
        initEventHandler();
    }

    public boolean isSelected() {
        return yesNoProducts.isSelected();
    }

    public @NotNull List<String> getSelections() {
        List<String> selections = new ArrayList<>();
        if (agriProd.isSelected()) {
            selections.add("Agricultural");
        }
        if (indusProd.isSelected()) {
            selections.add("Industry");
        }
        if (svcsProd.isSelected()) {
            selections.add("Services");
        }
        if (rawMatProd.isSelected()) {
            selections.add("Raw Materials");
        }
        if (bioProd.isSelected()) {
            selections.add("Bio");
        }
        if (fossilFuelProd.isSelected()) {
            selections.add("Fossil Fuel");
        }
        if (finishGoodsProd.isSelected()) {
            selections.add("Finished Goods");
        }
        if (hiTechProd.isSelected()) {
            selections.add("HiTech");
        }
        if (uniqProd.isSelected()) {
            selections.add("Unique");
        }
        if (enerProd.isSelected()) {
            selections.add("Energy");
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
                        yesNoProducts.setSelected(true);
                        enable(true);
                    } else {
                        yesNoProducts.setSelected(false);
                        clearSelected();
                        enable(false);
                    }
                }
            }
        };

        yesNoProducts.setOnAction(eh);
    }

    /**
     * enable the checkbox selections
     *
     * @param flag the flag to use
     */
    private void enable(boolean flag) {
        agriProd.setDisable(!flag);
        indusProd.setDisable(!flag);
        svcsProd.setDisable(!flag);
        rawMatProd.setDisable(!flag);
        bioProd.setDisable(!flag);
        fossilFuelProd.setDisable(!flag);
        finishGoodsProd.setDisable(!flag);
        hiTechProd.setDisable(!flag);
        uniqProd.setDisable(!flag);
        enerProd.setDisable(!flag);
    }

    /**
     * clear the selections
     */
    private void clearSelected() {
        agriProd.setSelected(false);
        indusProd.setSelected(false);
        svcsProd.setSelected(false);
        rawMatProd.setSelected(false);
        bioProd.setSelected(false);
        fossilFuelProd.setSelected(false);
        finishGoodsProd.setSelected(false);
        hiTechProd.setSelected(false);
        uniqProd.setSelected(false);
        enerProd.setSelected(false);
    }


}
