package com.teamgannon.trips.controller.menubar;

import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.events.DistanceReportEvent;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.report.distance.DistanceReportSelection;
import com.teamgannon.trips.report.distance.SelectStarForDistanceReportDialog;
import com.teamgannon.trips.report.route.RouteReportDialog;
import com.teamgannon.trips.service.DatasetService;
import javafx.event.ActionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;
import static com.teamgannon.trips.support.AlertFactory.showWarningMessage;

/**
 * Controller for the Reports menu.
 * Handles distance reports, route reports, and star property reports.
 */
@Slf4j
@Component
public class ReportsMenuController {

    private final TripsContext tripsContext;
    private final InterstellarSpacePane interstellarSpacePane;
    private final DatasetService datasetService;
    private final ApplicationEventPublisher eventPublisher;

    public ReportsMenuController(TripsContext tripsContext,
                                 InterstellarSpacePane interstellarSpacePane,
                                 DatasetService datasetService,
                                 ApplicationEventPublisher eventPublisher) {
        this.tripsContext = tripsContext;
        this.interstellarSpacePane = interstellarSpacePane;
        this.datasetService = datasetService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Opens the distance report dialog to generate a distance report from a selected star.
     */
    public void distanceReport(ActionEvent actionEvent) {
        try {
            List<StarDisplayRecord> starsInView = interstellarSpacePane.getCurrentStarsInView();
            if (!starsInView.isEmpty()) {
                SelectStarForDistanceReportDialog selectDialog = new SelectStarForDistanceReportDialog(starsInView);
                Optional<DistanceReportSelection> optionalStarDisplayRecord = selectDialog.showAndWait();
                if (optionalStarDisplayRecord.isPresent()) {
                    DistanceReportSelection reportSelection = optionalStarDisplayRecord.get();
                    if (reportSelection.isSelected()) {
                        eventPublisher.publishEvent(new DistanceReportEvent(this, reportSelection.getRecord()));
                    }
                }
            } else {
                showWarningMessage("No Visible Stars", "There are no visible stars in the plot. Please plot some first");
            }
        } catch (Exception e) {
            log.error("Error generating distance report", e);
            showErrorAlert("Distance Report", "Failed to generate report: " + e.getMessage());
        }
    }

    /**
     * Opens the route list report dialog.
     */
    public void routeListReport(ActionEvent actionEvent) {
        try {
            List<DataSetDescriptor> dataSetDescriptorList = datasetService.getDataSets();
            RouteReportDialog dialog = new RouteReportDialog(tripsContext.getDataSetDescriptor(), dataSetDescriptorList);
            dialog.showAndWait();
        } catch (Exception e) {
            log.error("Error generating route list report", e);
            showErrorAlert("Route List Report", "Failed to generate report: " + e.getMessage());
        }
    }

    /**
     * Opens the star property report dialog (not yet implemented).
     */
    public void starPropertyReport(ActionEvent actionEvent) {
        showWarningMessage("Star Property Report", "This function hasn't been implemented.");
    }
}
