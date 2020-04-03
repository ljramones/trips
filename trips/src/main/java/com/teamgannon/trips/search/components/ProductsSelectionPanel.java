package com.teamgannon.trips.search.components;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * products selection panel
 * <p>
 * Created by larrymitchell on 2017-06-24.
 */
@Slf4j
public class ProductsSelectionPanel extends BaseSearchPane {

    private CheckBox yesNoProducts = new CheckBox("Yes?");
    private CheckBox agriProd = new CheckBox("Agricultural");
    private CheckBox indusProd = new CheckBox("Industry");
    private CheckBox svcsProd = new CheckBox("Services");
    private CheckBox rawMatProd = new CheckBox("Raw Materials");
    private CheckBox bioProd = new CheckBox("Bio");
    private CheckBox fossilFuelProd = new CheckBox("Fossil Fuel");
    private CheckBox finishGoodsProd = new CheckBox("Finished Goods");
    private CheckBox hiTechProd = new CheckBox("Hi Tech");
    private CheckBox uniqProd = new CheckBox("Unique");
    private CheckBox enerProd = new CheckBox("Energy (Nuke, AM or Fuel Cells \nat high export levels");

    public ProductsSelectionPanel() {
        planGrid.setHgap(10);
        planGrid.setVgap(10);

        Label productsLabel = createLabel("Products");

        planGrid.add(productsLabel, 0, 0);
        planGrid.add(yesNoProducts, 1, 0);

        planGrid.add(agriProd, 2, 0);
        planGrid.add(indusProd, 3, 0);
        planGrid.add(svcsProd, 4, 0);

        planGrid.add(bioProd, 2, 1);
        planGrid.add(fossilFuelProd, 3, 1);
        planGrid.add(finishGoodsProd, 4, 1);


        planGrid.add(hiTechProd, 2, 2);
        planGrid.add(uniqProd, 3, 2);
        planGrid.add(rawMatProd, 4, 2);

        planGrid.add(enerProd, 2, 3, 3, 1);

        yesNoProducts.setSelected(false);
        enable(false);
        clearSelected();

        initEventHandler();

    }

    public boolean isSelected() {
        return yesNoProducts.isSelected();
    }

    public List<String> getSelections() {
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
            selections.add("RawMaterials");
        }
        if (bioProd.isSelected()) {
            selections.add("Bio");
        }
        if (fossilFuelProd.isSelected()) {
            selections.add("FossilFuel");
        }
        if (finishGoodsProd.isSelected()) {
            selections.add("FinishedGoods");
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
