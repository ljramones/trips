package com.teamgannon.trips.screenobjects;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StarDisplayRecordCell extends ListCell<StarDisplayRecord> {


    // We want to create a single Tooltip that will be reused, as needed. We will simply update the text
    // for the Tooltip for each cell
    final Tooltip tooltip = new Tooltip();


    @Override
    public void updateItem(StarDisplayRecord starDisplayRecord, boolean empty) {
        super.updateItem(starDisplayRecord, empty);


        int index = this.getIndex();
        String entry = null;

        ContextMenu contextMenu = new ContextMenu();
        MenuItem eitMenuItem = new MenuItem("Edit");
        eitMenuItem.setOnAction((event) -> {
            log.info("edit Route");
//            updater.showNewStellarData(true, false);
        });

        contextMenu.getItems().addAll(eitMenuItem);

        // Format name
        if (starDisplayRecord != null && !empty) {
            double[] actualCoordinates = starDisplayRecord.getActualCoordinates();
            entry = starDisplayRecord.getStarName() + " at (" +
                    String.format("%.2f", actualCoordinates[0]) + "," +
                    String.format("%.2f", actualCoordinates[1]) + "," +
                    String.format("%.2f", actualCoordinates[2]) + ")";

            log.info("show route:{}", entry);

            tooltip.setText("tooltip here");
            setTooltip(tooltip);
            setContextMenu(contextMenu);
        }

        this.setText(entry);
        setGraphic(null);
    }

}
