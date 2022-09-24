package com.teamgannon.trips.service.export;

import com.teamgannon.trips.dialogs.dataset.model.ExportOptions;
import com.teamgannon.trips.dialogs.dataset.model.ExportTaskComplete;
import com.teamgannon.trips.listener.StatusUpdaterListener;
import com.teamgannon.trips.search.SearchContext;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.export.tasks.CompactDataSetExportTask;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import static javafx.concurrent.Worker.State.RUNNING;

@Slf4j
public class CompactDataSetExportService extends Service<ExportResults> implements ExportTaskControl {

    private SearchContext searchContext;

    private ExportTaskComplete exportTaskComplete;
    private Label progressText;
    private ProgressBar exportProgressionBar;
    private StatusUpdaterListener statusUpdaterListener;

    private ExportOptions export;

    private DatabaseManagementService databaseManagementService;

    public CompactDataSetExportService(StatusUpdaterListener updaterListener) {
        this.statusUpdaterListener = updaterListener;
    }

    public boolean exportAsCompact(@NotNull ExportOptions export,
                                   @NotNull DatabaseManagementService databaseManagementService,
                                   StatusUpdaterListener statusUpdaterListener,
                                   ExportTaskComplete exportTaskComplete,
                                   Label progressText,
                                   ProgressBar exportProgressionBar,
                                   Button cancelExportButton) {

        this.export = export;
        this.databaseManagementService = databaseManagementService;
        this.exportTaskComplete = exportTaskComplete;
        this.progressText = progressText;
        this.exportProgressionBar = exportProgressionBar;
        this.statusUpdaterListener = statusUpdaterListener;

        progressText.textProperty().bind(this.messageProperty());
        exportProgressionBar.progressProperty().bind(this.progressProperty());
        cancelExportButton.disableProperty().bind(this.stateProperty().isNotEqualTo(RUNNING));

        return true;
    }

    @Override
    public boolean cancelExport() {
        return this.cancel();
    }

    @Override
    public String whoAmI() {
        return "Compact Dataset exporter";
    }

    @Override
    protected Task<ExportResults> createTask() {
        return new CompactDataSetExportTask(export, databaseManagementService);
    }

    @Override
    protected void cancelled() {
        log.warn("dataset export cancelled");
        statusUpdaterListener.updateStatus("dataset export was cancelled for " + export.getFileName());
        unsetProgressControls();
        ExportResults exportResults = this.getValue();
        exportTaskComplete.complete(false, searchContext.getDataSetDescriptor(), exportResults, "dataset load cancelled");

    }

    private void unsetProgressControls() {
        progressText.textProperty().unbind();
        exportProgressionBar.progressProperty().unbind();
        exportProgressionBar.setProgress(1);
    }

}
