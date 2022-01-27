package com.teamgannon.trips.service.graphsearch;

import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.listener.StatusUpdaterListener;
import com.teamgannon.trips.routing.model.RouteFindingOptions;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.graphsearch.task.LargeGraphSearchTask;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LargeGraphSearchService extends Service<GraphRouteResult> {

    private RouteFindingOptions routeFindingOptions;
    private DataSetDescriptor currentDataset;
    private DatabaseManagementService databaseManagementService;
    private StatusUpdaterListener statusUpdaterListener;

    private GraphSearchComplete graphSearchComplete;
    private Label progressText;
    private ProgressBar loadProgressBar;

    public boolean processGraphSearch(RouteFindingOptions routeFindingOptions,
                                      DataSetDescriptor currentDataset,
                                      DatabaseManagementService databaseManagementService,
                                      StatusUpdaterListener statusUpdaterListener,
                                      GraphSearchComplete graphSearchComplete,
                                      @NotNull Label progressText,
                                      @NotNull ProgressBar loadProgressBar,
                                      @NotNull Button cancelLoad) {
        this.routeFindingOptions = routeFindingOptions;
        this.currentDataset = currentDataset;

        this.databaseManagementService = databaseManagementService;
        this.statusUpdaterListener = statusUpdaterListener;
        this.graphSearchComplete = graphSearchComplete;
        this.progressText = progressText;
        this.loadProgressBar = loadProgressBar;

        progressText.textProperty().bind(this.messageProperty());
        loadProgressBar.progressProperty().bind(this.progressProperty());
//        cancelLoad.disableProperty().bind(this.stateProperty().isNotEqualTo(RUNNING));

        return true;
    }

    public boolean cancelSearch() {
        log.warn("cancelling graph search for" + "graph search name TBD");
        graphSearchComplete.complete(true, "cancelled");
        return this.cancel();
    }

    @Override
    protected Task<GraphRouteResult> createTask() {
        return new LargeGraphSearchTask(currentDataset, databaseManagementService, routeFindingOptions);
    }

    @Override
    protected void succeeded() {
        log.info("graph search found");
        String message = String.format("new graph search found -> %s", "graph search name TBD");
        statusUpdaterListener.updateStatus(message);
        unsetProgressControls();
        graphSearchComplete.complete(true, "some message");
    }

    @Override
    protected void failed() {
        log.error("graph search failed due to: " + getException().getMessage());
        statusUpdaterListener.updateStatus("graph search failed due to: " + getException().getMessage());
        unsetProgressControls();
        graphSearchComplete.complete(false, "some message");
    }

    @Override
    protected void cancelled() {
        log.warn("graph search cancelled");
        statusUpdaterListener.updateStatus("graph search was cancelled for " + "graph search name TBD");
        unsetProgressControls();
        graphSearchComplete.complete(false, "cancelled for " + "graph search name TBD");
    }

    private void unsetProgressControls() {
        progressText.textProperty().unbind();
        loadProgressBar.progressProperty().unbind();
        loadProgressBar.setProgress(1);
    }


}
