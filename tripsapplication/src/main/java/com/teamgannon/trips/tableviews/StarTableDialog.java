package com.teamgannon.trips.tableviews;

import com.teamgannon.trips.search.AstroSearchQuery;
import com.teamgannon.trips.service.StarService;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Modality;
import lombok.extern.slf4j.Slf4j;

/**
 * Dialog wrapper for StarTablePane.
 * Provides a non-modal dialog for viewing and editing star data with server-side pagination.
 */
@Slf4j
public class StarTableDialog extends Dialog<Void> {

    private final StarTablePane starTablePane;

    /**
     * Create a new StarTableDialog.
     *
     * @param starService the star service for database access
     * @param query       the search query to use
     */
    public StarTableDialog(StarService starService, AstroSearchQuery query) {
        String dataSetName = query.getDataSetContext().getDescriptor().getDataSetName();

        setTitle("Star Data: " + dataSetName);
        setResizable(true);
        initModality(Modality.NONE);

        // Set minimum and preferred sizes
        getDialogPane().setMinWidth(900);
        getDialogPane().setMinHeight(500);
        getDialogPane().setPrefWidth(1100);
        getDialogPane().setPrefHeight(650);

        // Create the star table pane
        starTablePane = new StarTablePane(starService, query);

        // Set content
        getDialogPane().setContent(starTablePane);

        // Add close button
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        // Handle close via window close button
        setOnCloseRequest(event -> {
            log.debug("Star table dialog closed");
        });

        log.info("Star table dialog created for dataset: {}", dataSetName);
    }

    /**
     * Get the underlying StarTablePane.
     *
     * @return the star table pane
     */
    public StarTablePane getStarTablePane() {
        return starTablePane;
    }

    /**
     * Refresh the table data.
     */
    public void refresh() {
        starTablePane.refresh();
    }
}
