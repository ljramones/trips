package com.teamgannon.trips.dialogs.inventory;

import lombok.Data;

@Data
public class InventoryReport {

    /**
     * whether we measure or not
     */
    private boolean saveSelected;

    private String inventoryDescription;

    public InventoryReport(String inventoryDescription) {
        saveSelected = false;
        this.inventoryDescription = inventoryDescription;
    }

    /**
     * tell the recipient whether we save thi report or not
     *
     * @param save true is save
     */
    public void setSave(boolean save) {
        this.saveSelected = save;
    }
}
