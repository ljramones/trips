package com.teamgannon.trips.screenobjects;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class StarDisplayRecordCell extends ListCell<StarDisplayRecord> {


    // We want to create a single Tooltip that will be reused, as needed. We will simply update the text
    // for the Tooltip for each cell
    private final Tooltip tooltip = new Tooltip();


    private final ListSelecterActionsListener listSelecterActionsListener;

    /**
     * the constructor for this cell
     *
     * @param listSelecterActionsListener the listener
     */
    public StarDisplayRecordCell(ListSelecterActionsListener listSelecterActionsListener) {
        this.listSelecterActionsListener = listSelecterActionsListener;
    }


    @Override
    public void updateItem(StarDisplayRecord starDisplayRecord, boolean empty) {
        super.updateItem(starDisplayRecord, empty);

        int index = this.getIndex();
        String entry = null;

        ContextMenu contextMenu = new ContextMenu();

        MenuItem recenterMenuItem = new MenuItem("Center on this star");
        recenterMenuItem.setOnAction((event) -> {
            log.info("recenter on {}", starDisplayRecord.getStarName());
            listSelecterActionsListener.recenter(starDisplayRecord);
        });

        MenuItem editMenuItem = new MenuItem("Edit this star");
        editMenuItem.setOnAction((event) -> {
            log.info("editing {}", starDisplayRecord.getStarName());
            StarEditDialog starEditDialog = new StarEditDialog(starDisplayRecord);
            Optional<StarDisplayRecord> optionalStarDisplayRecord = starEditDialog.showAndWait();
            if (optionalStarDisplayRecord.isPresent()) {
                StarDisplayRecord record = optionalStarDisplayRecord.get();
                if (record != null) {
                    log.info("Changed value: {}", starDisplayRecord);
                } else {
                    log.error("no return");
                }
            }
        });

        contextMenu.getItems().addAll(recenterMenuItem, editMenuItem);

        // Format name
        if (starDisplayRecord != null && !empty) {
            double[] actualCoordinates = starDisplayRecord.getActualCoordinates();
            entry = starDisplayRecord.getStarName() + " at (" +
                    String.format("%.2f", actualCoordinates[0]) + "," +
                    String.format("%.2f", actualCoordinates[1]) + "," +
                    String.format("%.2f", actualCoordinates[2]) + ")";

            tooltip.setText("tooltip here");
            setTooltip(tooltip);
            setContextMenu(contextMenu);
        }

        this.setText(entry);
        setGraphic(null);
    }

}
