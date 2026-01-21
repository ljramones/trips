package com.teamgannon.trips.routing.automation;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.routing.RouteFindingService;
import com.teamgannon.trips.routing.dialogs.DisplayAutoRoutesDialog;
import com.teamgannon.trips.routing.dialogs.RouteFinderDialogInView;
import com.teamgannon.trips.routing.model.RouteFindingOptions;
import com.teamgannon.trips.routing.model.RouteFindingResult;
import com.teamgannon.trips.routing.model.RoutingMetric;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

/**
 * Route finder for stars currently in view.
 * <p>
 * This component handles the UI workflow for finding routes:
 * <ol>
 *   <li>Shows dialog to collect route parameters</li>
 *   <li>Delegates to RouteFindingService for path calculation</li>
 *   <li>Displays results and plots selected routes</li>
 * </ol>
 */
@Slf4j
@Component
public class RouteFinderInView {

    private final InterstellarSpacePane interstellarSpacePane;
    private final RouteFindingService routeFindingService;

    public RouteFinderInView(InterstellarSpacePane interstellarSpacePane,
                             RouteFindingService routeFindingService) {
        this.interstellarSpacePane = interstellarSpacePane;
        this.routeFindingService = routeFindingService;
    }

    /**
     * Start the route finding workflow.
     *
     * @param currentDataSet the current dataset
     */
    public void startRouteLocation(DataSetDescriptor currentDataSet) {
        RouteFinderDialogInView routeFinderDialogInView = new RouteFinderDialogInView(
                interstellarSpacePane.getCurrentStarsInView());
        Stage theStage = (Stage) routeFinderDialogInView.getDialogPane().getScene().getWindow();
        theStage.setAlwaysOnTop(true);
        theStage.toFront();

        processRouteRequest(currentDataSet, theStage, routeFinderDialogInView);
    }

    /**
     * Process the route request from the dialog.
     */
    public void processRouteRequest(DataSetDescriptor currentDataSet,
                                    Stage theStage,
                                    @NotNull RouteFinderDialogInView routeFinderDialogInView) {

        Optional<RouteFindingOptions> optionalOptions = routeFinderDialogInView.showAndWait();
        if (optionalOptions.isEmpty()) {
            return;
        }

        RouteFindingOptions options = optionalOptions.get();
        if (!options.isSelected()) {
            return;
        }

        findAndDisplayRoutes(currentDataSet, theStage, routeFinderDialogInView, options);
    }

    /**
     * Find routes and display results.
     */
    private void findAndDisplayRoutes(DataSetDescriptor currentDataSet,
                                       Stage theStage,
                                       RouteFinderDialogInView routeFinderDialogInView,
                                       RouteFindingOptions options) {
        log.info("Finding route between {} and {}", options.getOriginStarName(), options.getDestinationStarName());

        List<StarDisplayRecord> starsInView = interstellarSpacePane.getCurrentStarsInView();

        // Delegate to service
        RouteFindingResult result = routeFindingService.findRoutes(options, starsInView, currentDataSet);

        if (!result.isSuccess()) {
            showErrorAlert("Route Finder", result.getErrorMessage());
            // Allow user to try again with different parameters
            processRouteRequest(currentDataSet, theStage, routeFinderDialogInView);
            return;
        }

        if (!result.hasRoutes()) {
            showErrorAlert("Route Finder", "No routes found with the given parameters.");
            processRouteRequest(currentDataSet, theStage, routeFinderDialogInView);
            return;
        }

        // Display results for user selection
        displayFoundRoutes(currentDataSet, result);
    }

    /**
     * Display the found routes for user selection.
     */
    private void displayFoundRoutes(DataSetDescriptor currentDataSet, @NotNull RouteFindingResult result) {
        DisplayAutoRoutesDialog displayAutoRoutesDialog = new DisplayAutoRoutesDialog(result.getRoutes());
        Stage dialogStage = (Stage) displayAutoRoutesDialog.getDialogPane().getScene().getWindow();
        dialogStage.setAlwaysOnTop(true);
        dialogStage.toFront();

        Optional<List<RoutingMetric>> optionalRoutingMetrics = displayAutoRoutesDialog.showAndWait();
        if (optionalRoutingMetrics.isPresent()) {
            List<RoutingMetric> selectedRoutingMetrics = optionalRoutingMetrics.get();
            if (!selectedRoutingMetrics.isEmpty()) {
                log.info("Plotting {} selected routes", selectedRoutingMetrics.size());
                interstellarSpacePane.plotRouteDescriptors(currentDataSet, selectedRoutingMetrics);
            }
        }
    }
}
