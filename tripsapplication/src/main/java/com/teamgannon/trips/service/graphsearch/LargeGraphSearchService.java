package com.teamgannon.trips.service.graphsearch;

import com.teamgannon.trips.events.StatusUpdateEvent;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.routing.model.RouteFindingOptions;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.StarService;
import com.teamgannon.trips.service.graphsearch.task.LargeGraphSearchTask;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LargeGraphSearchService extends Service<GraphRouteResult> {

    private RouteFindingOptions routeFindingOptions;
    private DataSetDescriptor currentDataset;
    private DatabaseManagementService databaseManagementService;
    private StarService starService;
    private ApplicationEventPublisher eventPublisher;

    private GraphSearchComplete graphSearchComplete;
    private Label progressText;
    private ProgressBar loadProgressBar;

    public boolean processGraphSearch(RouteFindingOptions routeFindingOptions,
                                      DataSetDescriptor currentDataset,
                                      DatabaseManagementService databaseManagementService,
                                      StarService starService,
                                      ApplicationEventPublisher eventPublisher,
                                      GraphSearchComplete graphSearchComplete,
                                      @NotNull Label progressText,
                                      @NotNull ProgressBar loadProgressBar,
                                      @NotNull Button cancelLoad) {
        this.routeFindingOptions = routeFindingOptions;
        this.currentDataset = currentDataset;

        this.databaseManagementService = databaseManagementService;
        this.starService = starService;
        this.eventPublisher = eventPublisher;
        this.graphSearchComplete = graphSearchComplete;
        this.progressText = progressText;
        this.loadProgressBar = loadProgressBar;

        progressText.textProperty().bind(this.messageProperty());
        loadProgressBar.progressProperty().bind(this.progressProperty());
//        cancelLoad.disableProperty().bind(this.stateProperty().isNotEqualTo(RUNNING));

        return true;
    }

    public boolean cancelSearch() {
        String searchDescription = searchDescription();
        log.warn("Cancelling graph search for {}", searchDescription);
        graphSearchComplete.complete(true, "Cancelled graph search for " + searchDescription);
        return this.cancel();
    }

    @Override
    protected Task<GraphRouteResult> createTask() {
        return new LargeGraphSearchTask(currentDataset, databaseManagementService, starService, routeFindingOptions);
    }

    @Override
    protected void succeeded() {
        GraphRouteResult result = getValue();
        String message = result != null && result.getMessage() != null
                ? result.getMessage()
                : "Graph search completed for " + searchDescription();
        log.info("Graph search completed: {}", message);
        eventPublisher.publishEvent(new StatusUpdateEvent(this, message));
        unsetProgressControls();
        graphSearchComplete.complete(true, message);
    }

    @Override
    protected void failed() {
        Throwable exception = getException();
        String message = "Graph search failed for %s%s".formatted(
                searchDescription(),
                exception == null ? "" : ": " + exception.getMessage());
        log.error("Graph search failed for {}", searchDescription(), exception);
        eventPublisher.publishEvent(new StatusUpdateEvent(this, message));
        unsetProgressControls();
        graphSearchComplete.complete(false, message);
    }

    @Override
    protected void cancelled() {
        String message = "Graph search was cancelled for " + searchDescription();
        log.warn(message);
        eventPublisher.publishEvent(new StatusUpdateEvent(this, message));
        unsetProgressControls();
        graphSearchComplete.complete(false, message);
    }

    private void unsetProgressControls() {
        progressText.textProperty().unbind();
        loadProgressBar.progressProperty().unbind();
        loadProgressBar.setProgress(1);
    }

    private String searchDescription() {
        if (routeFindingOptions == null
                || routeFindingOptions.getOriginStar() == null
                || routeFindingOptions.getDestinationStar() == null) {
            return "unknown route";
        }
        return "%s to %s".formatted(
                routeFindingOptions.getOriginStar().getDisplayName(),
                routeFindingOptions.getDestinationStar().getDisplayName());
    }

}
