package com.teamgannon.trips.controller.splitpane;

import com.teamgannon.trips.events.*;
import com.teamgannon.trips.graphics.PlotManager;
import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import com.teamgannon.trips.javafxsupport.BackgroundTaskRunner;
import com.teamgannon.trips.javafxsupport.FxThread;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.routing.sidepanel.RoutingPanel;
import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.controller.statusbar.StatusBarController;
import com.teamgannon.trips.service.DatasetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.CancellationException;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

/**
 * Handles route-related events (create, update, display, delete).
 * <p>
 * This class manages all route lifecycle events, coordinating between
 * the routing panel, interstellar space pane, and dataset service.
 */
@Slf4j
@Component
public class RouteEventHandler {

    private final ApplicationEventPublisher eventPublisher;
    private final StatusBarController statusBarController;
    private final TripsContext tripsContext;
    private final DatasetService datasetService;
    private final SearchContextCoordinator searchContextCoordinator;
    private final RoutingPanel routingPanel;
    private final InterstellarSpacePane interstellarSpacePane;

    private PlotManager plotManager;

    public RouteEventHandler(ApplicationEventPublisher eventPublisher,
                             StatusBarController statusBarController,
                             TripsContext tripsContext,
                             DatasetService datasetService,
                             SearchContextCoordinator searchContextCoordinator,
                             RoutingPanel routingPanel,
                             InterstellarSpacePane interstellarSpacePane) {
        this.eventPublisher = eventPublisher;
        this.statusBarController = statusBarController;
        this.tripsContext = tripsContext;
        this.datasetService = datasetService;
        this.searchContextCoordinator = searchContextCoordinator;
        this.routingPanel = routingPanel;
        this.interstellarSpacePane = interstellarSpacePane;
    }

    /**
     * Initialize with the plot manager (called after construction).
     *
     * @param plotManager the plot manager
     */
    public void initialize(PlotManager plotManager) {
        this.plotManager = plotManager;
    }

    @EventListener
    public void onRoutingStatusEvent(RoutingStatusEvent event) {
        FxThread.runOnFxThread(() -> {
            try {
                statusBarController.routingStatus(event.isStatusFlag());
            } catch (Exception e) {
                log.error("Error handling routing status event", e);
            }
        });
    }

    @EventListener
    public void onNewRouteEvent(NewRouteEvent event) {
        eventPublisher.publishEvent(new StatusUpdateEvent(this, "Saving new route..."));
        String taskId = createTaskId("add-route");
        BackgroundTaskRunner.TaskHandle taskHandle = BackgroundTaskRunner.runCancelable(
                "trips-add-route",
                () -> {
                    DataSetDescriptor dataSetDescriptor = event.getDataSetDescriptor();
                    RouteDescriptor routeDescriptor = event.getRouteDescriptor();
                    datasetService.addRouteToDataSet(dataSetDescriptor, routeDescriptor);
                    return null;
                },
                result -> FxThread.runOnFxThread(() -> {
                    log.info("new route");
                    DataSetDescriptor dataSetDescriptor = event.getDataSetDescriptor();
                    routingPanel.setContext(dataSetDescriptor, plotManager.getRouteVisibility());
                    statusBarController.routingStatus(false);
                    eventPublisher.publishEvent(new StatusUpdateEvent(this, "Route saved."));
                }),
                exception -> FxThread.runOnFxThread(() -> {
                    if (isCancellation(exception)) {
                        eventPublisher.publishEvent(new StatusUpdateEvent(this, "Route save cancelled."));
                        return;
                    }
                    String message = exception == null ? "Failed to add route." : exception.getMessage();
                    showErrorAlert("Route Update Error", message);
                    eventPublisher.publishEvent(new StatusUpdateEvent(this, "Failed to save route."));
                }),
                () -> eventPublisher.publishEvent(new BusyStateEvent(this, taskId, false, null, null)));
        eventPublisher.publishEvent(new BusyStateEvent(this, taskId, true, "Saving route...", taskHandle::cancel));
    }

    @EventListener
    public void onUpdateRouteEvent(UpdateRouteEvent event) {
        eventPublisher.publishEvent(new StatusUpdateEvent(this, "Updating route..."));
        String taskId = createTaskId("update-route");
        BackgroundTaskRunner.TaskHandle taskHandle = BackgroundTaskRunner.runCancelable(
                "trips-update-route",
                () -> {
                    RouteDescriptor routeDescriptor = event.getRouteDescriptor();
                    String datasetName = searchContextCoordinator.getCurrentDataSetName();
                    return datasetService.updateRoute(datasetName, routeDescriptor);
                },
                descriptor -> FxThread.runOnFxThread(() -> {
                    log.info("update route");
                    searchContextCoordinator.setDescriptor(descriptor);
                    routingPanel.setContext(descriptor, plotManager.getRouteVisibility());
                    interstellarSpacePane.redrawRoutes(descriptor.getRoutes());
                    eventPublisher.publishEvent(new StatusUpdateEvent(this, "Route updated."));
                }),
                exception -> FxThread.runOnFxThread(() -> {
                    if (isCancellation(exception)) {
                        eventPublisher.publishEvent(new StatusUpdateEvent(this, "Route update cancelled."));
                        return;
                    }
                    String message = exception == null ? "Failed to update route." : exception.getMessage();
                    showErrorAlert("Route Update Error", message);
                    eventPublisher.publishEvent(new StatusUpdateEvent(this, "Failed to update route."));
                }),
                () -> eventPublisher.publishEvent(new BusyStateEvent(this, taskId, false, null, null)));
        eventPublisher.publishEvent(new BusyStateEvent(this, taskId, true, "Updating route...", taskHandle::cancel));
    }

    @EventListener
    public void onDisplayRouteEvent(DisplayRouteEvent event) {
        FxThread.runOnFxThread(() -> {
            try {
                interstellarSpacePane.displayRoute(event.getRouteDescriptor(), event.isVisible());
            } catch (Exception e) {
                log.error("Error handling display route event", e);
                showErrorAlert("Route Display Error", "Failed to display route: " + e.getMessage());
            }
        });
    }

    @EventListener
    public void onDeleteRouteEvent(DeleteRouteEvent event) {
        eventPublisher.publishEvent(new StatusUpdateEvent(this, "Deleting route..."));
        String taskId = createTaskId("delete-route");
        BackgroundTaskRunner.TaskHandle taskHandle = BackgroundTaskRunner.runCancelable(
                "trips-delete-route",
                () -> {
                    RouteDescriptor routeDescriptor = event.getRouteDescriptor();
                    DataSetDescriptor descriptor = searchContextCoordinator.getCurrentDescriptor();
                    return datasetService.deleteRoute(descriptor.getDataSetName(), routeDescriptor);
                },
                descriptor -> FxThread.runOnFxThread(() -> {
                    log.info("delete route");
                    RouteDescriptor routeDescriptor = event.getRouteDescriptor();
                    searchContextCoordinator.setDescriptor(descriptor);
                    // clear the route from the plot
                    tripsContext.getCurrentPlot().removeRoute(routeDescriptor);
                    routingPanel.setContext(descriptor, plotManager.getRouteVisibility());
                    interstellarSpacePane.redrawRoutes(descriptor.getRoutes());
                    eventPublisher.publishEvent(new StatusUpdateEvent(this, "Route deleted."));
                }),
                exception -> FxThread.runOnFxThread(() -> {
                    if (isCancellation(exception)) {
                        eventPublisher.publishEvent(new StatusUpdateEvent(this, "Route delete cancelled."));
                        return;
                    }
                    String message = exception == null ? "Failed to delete route." : exception.getMessage();
                    showErrorAlert("Route Update Error", message);
                    eventPublisher.publishEvent(new StatusUpdateEvent(this, "Failed to delete route."));
                }),
                () -> eventPublisher.publishEvent(new BusyStateEvent(this, taskId, false, null, null)));
        eventPublisher.publishEvent(new BusyStateEvent(this, taskId, true, "Deleting route...", taskHandle::cancel));
    }

    private String createTaskId(String base) {
        return base + "-" + System.nanoTime();
    }

    private boolean isCancellation(Throwable exception) {
        return exception instanceof CancellationException;
    }
}
