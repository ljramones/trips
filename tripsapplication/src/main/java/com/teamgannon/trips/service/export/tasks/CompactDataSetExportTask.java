package com.teamgannon.trips.service.export.tasks;

import com.teamgannon.trips.dialogs.dataset.ExportOptions;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.export.ExportResults;
import com.teamgannon.trips.service.importservices.tasks.ProgressUpdater;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CompactDataSetExportTask extends Task<ExportResults> implements ProgressUpdater {

    private final ExportOptions export;
    private final DatabaseManagementService databaseManagementService;

    public CompactDataSetExportTask(ExportOptions export, DatabaseManagementService databaseManagementService) {
        this.export = export;
        this.databaseManagementService = databaseManagementService;
    }

    @Override
    public void updateTaskInfo(String message) {
        updateMessage(message + "  ");
    }

    @Override
    protected ExportResults call() throws Exception {
        ExportResults result = exportCompact(export);
        if (result.isSuccess()) {
            log.info("New dataset {} added", export.getFileName());
        } else {
            log.error("load csv: " + result.getMessage());
        }

        return result;
    }

    private ExportResults exportCompact(ExportOptions export) {
        return null;
    }
}
