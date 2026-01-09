package com.teamgannon.trips.dataset.model;

import com.teamgannon.trips.events.RemoveDataSetEvent;
import com.teamgannon.trips.events.ShowStellarDataEvent;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.ApplicationEventPublisher;

@Slf4j
public class DataSetDescriptorCell extends ListCell<DataSetDescriptor> {

    // We want to create a single Tooltip that will be reused, as needed. We will simply update the text
    // for the Tooltip for each cell
    final Tooltip tooltip = new Tooltip();

    private final ApplicationEventPublisher eventPublisher;

    /**
     * the constructor
     *
     * @param eventPublisher the event publisher
     */
    public DataSetDescriptorCell(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void updateItem(@Nullable DataSetDescriptor descriptor, boolean empty) {
        super.updateItem(descriptor, empty);

        int index = this.getIndex();
        String name = null;
        ContextMenu contextMenu = new ContextMenu();
        MenuItem plotMenuItem = new MenuItem("Plot Star");
        plotMenuItem.setOnAction((event) -> {
            log.info("plot stars!");
            eventPublisher.publishEvent(new ShowStellarDataEvent(this, descriptor, true, false));
        });

        MenuItem displayMenuItem = new MenuItem("Display Data");
        displayMenuItem.setOnAction((event) -> {
            log.info("display star data!");
            eventPublisher.publishEvent(new ShowStellarDataEvent(this, descriptor, false, true));
        });

        MenuItem displayPlotMenuItem = new MenuItem("Plot and display data");
        displayPlotMenuItem.setOnAction((event) -> {
            log.info("plot and display star data!");
            eventPublisher.publishEvent(new ShowStellarDataEvent(this, descriptor, true, true));
        });

        MenuItem deleteMenuItem = new MenuItem("Delete");
        deleteMenuItem.setOnAction((event) -> {
            log.info("delete star data!");
            eventPublisher.publishEvent(new RemoveDataSetEvent(this, descriptor));
        });

        contextMenu.getItems().addAll(plotMenuItem, displayMenuItem, displayPlotMenuItem, deleteMenuItem);

        // Format name
        if (descriptor != null && !empty) {
            name = "Set " + (index + 1) + ": " +
                    descriptor.getDataSetName() + " has " +
                    descriptor.getNumberStars() + " stars";

            tooltip.setText(descriptor.getToolTipText());
            setTooltip(tooltip);
            setContextMenu(contextMenu);
        }

        this.setText(name);
        setGraphic(null);
    }
}
