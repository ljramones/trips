package com.teamgannon.trips.service.export;

import com.teamgannon.trips.dialogs.dataset.model.ExportOptions;
import com.teamgannon.trips.dialogs.dataset.model.ExportTaskComplete;
import com.teamgannon.trips.events.StatusUpdateEvent;
import com.teamgannon.trips.search.SearchContext;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.StarService;
import com.teamgannon.trips.service.export.tasks.CSVDataSetDataExportTask;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;

import static javafx.concurrent.Worker.State.RUNNING;

@Slf4j
public class CSVDataSetDataExportService extends Service<ExportResults> implements ExportTaskControl {

    private ApplicationEventPublisher eventPublisher;
    private SearchContext searchContext;

    private DatabaseManagementService databaseManagementService;

    private StarService starService;
    private ExportTaskComplete exportTaskComplete;
    private Label progressText;
    private ProgressBar exportProgressionBar;

    private ExportOptions export;

    public CSVDataSetDataExportService() {
    }


    @Override
    protected Task<ExportResults> createTask() {
        return new CSVDataSetDataExportTask(export, databaseManagementService, starService);
    }

    public boolean exportAsCSV(@NotNull ExportOptions export,
                               @NotNull DatabaseManagementService databaseManagementService,
                               StarService starService,
                               ApplicationEventPublisher eventPublisher,
                               ExportTaskComplete exportTaskComplete,
                               Label progressText,
                               ProgressBar exportProgressionBar,
                               Button cancelExportButton) {

        this.export = export;
        this.databaseManagementService = databaseManagementService;
        this.starService = starService;
        this.eventPublisher = eventPublisher;
        this.exportTaskComplete = exportTaskComplete;
        this.progressText = progressText;
        this.exportProgressionBar = exportProgressionBar;

        progressText.textProperty().bind(this.messageProperty());
        exportProgressionBar.progressProperty().bind(this.progressProperty());
        cancelExportButton.disableProperty().bind(this.stateProperty().isNotEqualTo(RUNNING));

        return true;
    }

    @Override
    protected void succeeded() {
        log.info("dataset exported");
        unsetProgressControls();
        ExportResults fileProcessResult = this.getValue();
        exportTaskComplete.complete(true, export.getDataset(), fileProcessResult, "exported");
        // set context to newly loaded dataset
        eventPublisher.publishEvent(new StatusUpdateEvent(this, "exported " + export.getDataset().getDataSetName() + " to file " + export.getFileName()));
    }

    @Override
    protected void failed() {
        log.error("dataset export failed due to: " + getException().getMessage());
        eventPublisher.publishEvent(new StatusUpdateEvent(this, "dataset export failed due to: " + getException().getMessage()));
        unsetProgressControls();
        ExportResults fileProcessResult = this.getValue();
        exportTaskComplete.complete(false, export.getDataset(), fileProcessResult, "dataset export failed due to: " + getException().getMessage());
    }

    @Override
    protected void cancelled() {
        log.warn("dataset export cancelled");
        eventPublisher.publishEvent(new StatusUpdateEvent(this, "dataset export was cancelled for " + export.getFileName()));
        unsetProgressControls();
        ExportResults exportResults = this.getValue();
        exportTaskComplete.complete(false, searchContext.getDataSetDescriptor(), exportResults, "dataset load cancelled");

    }

    private void unsetProgressControls() {
        progressText.textProperty().unbind();
        exportProgressionBar.progressProperty().unbind();
        exportProgressionBar.setProgress(1);
    }


    @Override
    public boolean cancelExport() {
        return this.cancel();
    }

    public @NotNull String whoAmI() {
        return "CSV Dataset exporter";
    }


}
