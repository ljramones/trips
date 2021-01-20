package com.teamgannon.trips.screenobjects;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.listener.DatabaseListener;
import com.teamgannon.trips.listener.ListSelectorActionsListener;
import com.teamgannon.trips.listener.RedrawListener;
import com.teamgannon.trips.listener.ReportGenerator;
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


    private final DatabaseListener databaseListener;
    private final ListSelectorActionsListener listSelectorActionsListener;
    private final ReportGenerator reportGenerator;
    private final RedrawListener redrawListener;

    /**
     * the constructor for this cell
     *
     * @param listSelectorActionsListener the listener
     */
    public StarDisplayRecordCell(DatabaseListener databaseListener,
                                 ListSelectorActionsListener listSelectorActionsListener,
                                 ReportGenerator reportGenerator,
                                 RedrawListener redrawListener) {

        this.databaseListener = databaseListener;
        this.listSelectorActionsListener = listSelectorActionsListener;
        this.reportGenerator = reportGenerator;
        this.redrawListener = redrawListener;
    }

    @Override
    public void updateItem(StarDisplayRecord starDisplayRecord, boolean empty) {
        super.updateItem(starDisplayRecord, empty);

        String entry = null;
        ContextMenu contextMenu = new ContextMenu();
        MenuItem recenterMenuItem = new MenuItem("Center on this star");
        recenterMenuItem.setOnAction((event) -> {
            log.info("recenter on {}", starDisplayRecord.getStarName());
            listSelectorActionsListener.recenter(starDisplayRecord);
        });

        MenuItem highlightMenuItem = new MenuItem("Highlight this star");
        highlightMenuItem.setOnAction((event) -> {
            log.info("Highlight on {}", starDisplayRecord.getStarName());
            redrawListener.highlightStar(starDisplayRecord.getRecordId());
        });


        MenuItem editMenuItem = new MenuItem("Edit this star");
        editMenuItem.setOnAction((event) -> {
            log.info("editing {}", starDisplayRecord.getStarName());
            StarObject starObject = databaseListener.getStar(starDisplayRecord.getRecordId());
            StarEditDialog starEditDialog = new StarEditDialog(starObject);
            Optional<StarEditStatus> optionalStarDisplayRecord = starEditDialog.showAndWait();
            if (optionalStarDisplayRecord.isPresent()) {
                StarEditStatus status = optionalStarDisplayRecord.get();
                if (status.isChanged()) {
                    StarObject record = status.getRecord();
                    databaseListener.updateStar(record);
                    log.info("Changed value: {}", record);
                } else {
                    log.error("no return");
                }
            }
        });

        MenuItem distanceReportMenuItem = new MenuItem("Generate distance report");
        distanceReportMenuItem.setOnAction((event) -> {
            log.info("generate distance report {}", starDisplayRecord.getStarName());
            reportGenerator.generateDistanceReport(starDisplayRecord);
        });

        contextMenu.getItems().addAll(recenterMenuItem, highlightMenuItem, editMenuItem, distanceReportMenuItem);

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
