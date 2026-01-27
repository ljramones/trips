package com.teamgannon.trips.screenobjects;

import com.teamgannon.trips.events.DistanceReportEvent;
import com.teamgannon.trips.events.HighlightStarEvent;
import com.teamgannon.trips.events.RecenterStarEvent;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.service.StarService;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

@Slf4j
public class StarDisplayRecordCell extends ListCell<StarDisplayRecord> {


    // We want to create a single Tooltip that will be reused, as needed. We will simply update the text
    // for the Tooltip for each cell
    private final Tooltip tooltip = new Tooltip();


    private final StarService starService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * the constructor for this cell
     */
    public StarDisplayRecordCell(StarService starService,
                                 ApplicationEventPublisher eventPublisher) {

        this.starService = starService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void updateItem(StarDisplayRecord starDisplayRecord, boolean empty) {
        super.updateItem(starDisplayRecord, empty);

        String entry = null;
        ContextMenu contextMenu = new ContextMenu();
        MenuItem recenterMenuItem = new MenuItem("Center on this star");
        recenterMenuItem.setOnAction((event) -> {
            log.info("recenter on {}", starDisplayRecord.getStarName());
            eventPublisher.publishEvent(new RecenterStarEvent(this, starDisplayRecord));
        });

        MenuItem highlightMenuItem = new MenuItem("Highlight this star");
        highlightMenuItem.setOnAction((event) -> {
            log.info("Highlight on {}", starDisplayRecord.getStarName());
            eventPublisher.publishEvent(new HighlightStarEvent(this, starDisplayRecord.getRecordId()));
        });


        MenuItem editMenuItem = new MenuItem("Edit this star");
        editMenuItem.setOnAction((event) -> {
            log.info("editing {}", starDisplayRecord.getStarName());
            StarObject starObject = starService.getStar(starDisplayRecord.getRecordId());
            StarEditDialog starEditDialog = new StarEditDialog(starObject);
            Optional<StarEditStatus> optionalStarDisplayRecord = starEditDialog.showAndWait();
            if (optionalStarDisplayRecord.isPresent()) {
                StarEditStatus status = optionalStarDisplayRecord.get();
                if (status.isChanged()) {
                    StarObject record = status.getRecord();
                    starService.updateStar(record);
                    log.info("Changed value: {}", record);
                } else {
                    log.error("no return");
                }
            }
        });

        MenuItem distanceReportMenuItem = new MenuItem("Generate distance report");
        distanceReportMenuItem.setOnAction((event) -> {
            log.info("generate distance report {}", starDisplayRecord.getStarName());
            eventPublisher.publishEvent(new DistanceReportEvent(this, starDisplayRecord));
        });

        contextMenu.getItems().addAll(recenterMenuItem, highlightMenuItem, editMenuItem, distanceReportMenuItem);

        // Format name
        if (starDisplayRecord != null && !empty) {
            double[] actualCoordinates = starDisplayRecord.getActualCoordinates();
            entry = starDisplayRecord.getStarName() + " at (" +
                    "%.2f".formatted(actualCoordinates[0]) + "," +
                    "%.2f".formatted(actualCoordinates[1]) + "," +
                    "%.2f".formatted(actualCoordinates[2]) + ")";

            tooltip.setText("tooltip here");
            setTooltip(tooltip);
            setContextMenu(contextMenu);
        } else {
            setTooltip(null);
            setContextMenu(null);
        }

        this.setText(entry);
        setGraphic(null);
    }

}
