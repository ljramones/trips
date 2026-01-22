package com.teamgannon.trips.transits;

import com.teamgannon.trips.dialogs.routing.RouteDialog;
import com.teamgannon.trips.dialogs.routing.RouteSelector;
import com.teamgannon.trips.events.NewRouteEvent;
import com.teamgannon.trips.events.RoutingStatusEvent;
import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import javafx.scene.control.ButtonType;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.teamgannon.trips.support.AlertFactory.showConfirmationAlert;
import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

/**
 * Service responsible for building routes from transit segments.
 * Manages route construction state and publishes route events.
 */
@Slf4j
@Service
public class TransitRouteBuilderService implements ITransitRouteBuilder {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * The route descriptor being built
     */
    private @Nullable RouteDescriptor routeDescriptor;

    /**
     * Whether route building is currently active
     */
    private boolean routingActive = false;

    /**
     * The list of transit segments in the current route
     */
    private final List<TransitRoute> currentRouteList = new ArrayList<>();

    /**
     * The current dataset for the route
     */
    private @Nullable DataSetDescriptor dataSetDescriptor;

    public TransitRouteBuilderService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public boolean isRoutingActive() {
        return routingActive;
    }

    @Override
    public void setDataSetDescriptor(@Nullable DataSetDescriptor descriptor) {
        this.dataSetDescriptor = descriptor;
    }

    @Override
    public boolean startNewRoute(@NotNull TransitRoute transitRoute) {
        if (!currentRouteList.isEmpty()) {
            Optional<ButtonType> buttonType = showConfirmationAlert(
                    "Remove Dataset",
                    "Restart Route?",
                    "You have a route in progress, Ok will clear current?");

            if (buttonType.isEmpty() || buttonType.get() != ButtonType.OK) {
                return false;
            }
            currentRouteList.clear();
        }

        return createRoute(transitRoute);
    }

    /**
     * Create a new route starting from the given transit segment.
     * Shows a dialog for the user to configure route properties.
     *
     * @param transitRoute the initial transit segment
     * @return true if route was created successfully
     */
    private boolean createRoute(@NotNull TransitRoute transitRoute) {
        StarDisplayRecord sourceRecord = transitRoute.getSource();
        RouteDialog dialog = new RouteDialog(sourceRecord);
        Optional<RouteSelector> resultOptional = dialog.showAndWait();

        if (resultOptional.isPresent()) {
            RouteSelector routeSelector = resultOptional.get();
            if (routeSelector.isSelected()) {
                currentRouteList.clear();
                routingActive = true;
                routeDescriptor = routeSelector.getRouteDescriptor();
                routeDescriptor.setStartStar(sourceRecord.getStarName());
                routeDescriptor.getRouteList().add(sourceRecord.getRecordId());
                currentRouteList.add(transitRoute);
                eventPublisher.publishEvent(new RoutingStatusEvent(this, true));
                log.debug("Started new route from {}", sourceRecord.getStarName());
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean addToRoute(@NotNull TransitRoute transitRoute) {
        if (!routingActive) {
            showErrorAlert("Link Routing", "Start a route first");
            return false;
        }
        currentRouteList.add(transitRoute);
        log.debug("Added segment to route: {} -> {}",
                transitRoute.getSource().getStarName(),
                transitRoute.getTarget().getStarName());
        return true;
    }

    @Override
    public boolean completeRoute(@NotNull TransitRoute transitRoute) {
        if (!routingActive) {
            showErrorAlert("Link Routing", "Start a route first");
            return false;
        }

        currentRouteList.add(transitRoute);
        return constructAndPublishRoute();
    }

    /**
     * Construct the route from collected segments and publish the event.
     *
     * @return true if route was constructed and published
     */
    private boolean constructAndPublishRoute() {
        if (routeDescriptor == null) {
            log.error("Cannot construct route: routeDescriptor is null");
            showErrorAlert("Route Construction", "No route descriptor available. Please start a new route first.");
            return false;
        }

        if (dataSetDescriptor == null) {
            log.error("Cannot construct route: dataSetDescriptor is null");
            showErrorAlert("Route Construction", "No dataset selected. Please select a dataset first.");
            return false;
        }

        log.debug("Constructing route with {} transit segments", currentRouteList.size());
        for (TransitRoute transitRoute : currentRouteList) {
            routeDescriptor.getRouteCoordinates().add(transitRoute.getTargetEndpoint());
            routeDescriptor.getRouteList().add(transitRoute.getTarget().getRecordId());
        }

        eventPublisher.publishEvent(new NewRouteEvent(this, dataSetDescriptor, routeDescriptor));

        // Reset state after completing route
        routingActive = false;
        currentRouteList.clear();
        routeDescriptor = null;

        log.debug("Route completed and published");
        return true;
    }

    @Override
    public void cancelRoute() {
        if (routingActive) {
            log.debug("Route building cancelled");
            routingActive = false;
            currentRouteList.clear();
            routeDescriptor = null;
            eventPublisher.publishEvent(new RoutingStatusEvent(this, false));
        }
    }

    @Override
    public int getCurrentRouteSize() {
        return currentRouteList.size();
    }
}
