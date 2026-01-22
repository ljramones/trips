package com.teamgannon.trips.routing.automation;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.routing.RouteFindingService;
import com.teamgannon.trips.routing.model.RouteFindingOptions;
import com.teamgannon.trips.routing.model.RouteFindingResult;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Asynchronous service for finding routes within visible stars.
 * <p>
 * This service wraps the synchronous {@link RouteFindingService} in a JavaFX
 * {@link Service} to prevent UI blocking during route calculations.
 * <p>
 * <b>Usage:</b>
 * <pre>{@code
 * InViewRouteFinderService service = new InViewRouteFinderService(routeFindingService);
 * service.configure(options, starsInView, dataSet);
 *
 * service.setOnSucceeded(event -> {
 *     RouteFindingResult result = service.getValue();
 *     // Handle result
 * });
 *
 * service.setOnFailed(event -> {
 *     // Handle failure
 * });
 *
 * service.start();
 * }</pre>
 * <p>
 * <b>Progress Binding:</b>
 * <p>
 * The service supports progress binding for UI feedback:
 * <pre>{@code
 * progressLabel.textProperty().bind(service.messageProperty());
 * progressBar.progressProperty().bind(service.progressProperty());
 * }</pre>
 */
@Slf4j
public class InViewRouteFinderService extends Service<RouteFindingResult> {

    private final RouteFindingService routeFindingService;

    private RouteFindingOptions options;
    private List<StarDisplayRecord> starsInView;
    private DataSetDescriptor dataSet;

    /**
     * Creates a new InViewRouteFinderService.
     *
     * @param routeFindingService the route finding service
     */
    public InViewRouteFinderService(RouteFindingService routeFindingService) {
        this.routeFindingService = routeFindingService;
    }

    /**
     * Configures the service for a route finding operation.
     * <p>
     * Must be called before {@link #start()}.
     *
     * @param options     the route finding options
     * @param starsInView the stars available for routing
     * @param dataSet     the current dataset
     */
    public void configure(RouteFindingOptions options,
                          List<StarDisplayRecord> starsInView,
                          DataSetDescriptor dataSet) {
        this.options = options;
        this.starsInView = starsInView;
        this.dataSet = dataSet;
    }

    @Override
    protected Task<RouteFindingResult> createTask() {
        return new InViewRouteFinderTask();
    }

    /**
     * The task that performs the actual route finding.
     */
    private class InViewRouteFinderTask extends Task<RouteFindingResult> {

        @Override
        protected RouteFindingResult call() {
            log.info("Starting async route finding: {} → {}",
                    options.getOriginStarName(), options.getDestinationStarName());

            updateMessage("Finding routes...");
            updateProgress(-1, 1); // Indeterminate progress

            try {
                // Perform route finding
                RouteFindingResult result = routeFindingService.findRoutes(options, starsInView, dataSet);

                if (result.isSuccess()) {
                    int routeCount = result.getRoutes().getRoutes().size();
                    updateMessage(String.format("Found %d route%s", routeCount, routeCount == 1 ? "" : "s"));
                    log.info("Async route finding completed: {} routes found", routeCount);
                } else {
                    updateMessage("No routes found");
                    log.info("Async route finding completed: no routes found");
                }

                updateProgress(1, 1);
                return result;

            } catch (Exception e) {
                log.error("Async route finding failed: {}", e.getMessage(), e);
                updateMessage("Route finding failed: " + e.getMessage());
                throw e;
            }
        }
    }
}
