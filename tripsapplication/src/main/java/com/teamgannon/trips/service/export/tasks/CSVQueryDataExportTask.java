package com.teamgannon.trips.service.export.tasks;

import com.teamgannon.trips.dialogs.dataset.model.ExportOptions;
import com.teamgannon.trips.search.SearchContext;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.StarService;
import com.teamgannon.trips.service.export.ExportResults;
import com.teamgannon.trips.service.export.StarCsvFormatter;
import com.teamgannon.trips.service.importservices.tasks.ProgressUpdater;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class CSVQueryDataExportTask extends Task<ExportResults> implements ProgressUpdater {

    private static final int PROGRESS_UPDATE_INTERVAL = 1000;

    private final ExportOptions export;
    private final SearchContext searchContext;
    private final DatabaseManagementService databaseManagementService;
    private final StarService starService;

    public CSVQueryDataExportTask(ExportOptions export, SearchContext searchContext,
                                  DatabaseManagementService databaseManagementService,
                                  StarService starService) {
        this.export = export;
        this.searchContext = searchContext;
        this.databaseManagementService = databaseManagementService;
        this.starService = starService;
    }


    @Override
    protected ExportResults call() throws Exception {
        ExportResults result = processCSVFile(export);
        if (result.isSuccess()) {
            log.info("Query export {} completed", export.getFileName());
        } else {
            log.error("Export failed: {}", result.getMessage());
        }

        return result;
    }

    public ExportResults processCSVFile(ExportOptions export) {
        ExportResults exportResults = ExportResults.builder().success(false).build();

        log.info("Starting streaming query export");
        long startTime = System.currentTimeMillis();

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(export.getFileName() + ".trips.csv"))) {

            // Write headers
            writer.write(StarCsvFormatter.headers());

            // Use atomic counter for lambda
            AtomicLong count = new AtomicLong(0);

            // Stream and write each star within transaction - constant memory usage
            long totalProcessed = starService.processQueryStream(searchContext.getAstroSearchQuery(), starObject -> {
                try {
                    writer.write(StarCsvFormatter.format(starObject));
                    long current = count.incrementAndGet();
                    if (current % PROGRESS_UPDATE_INTERVAL == 0) {
                        updateTaskInfo(current + " records so far");
                    }
                } catch (Exception e) {
                    log.error("Error writing star {}: {}", starObject.getDisplayName(), e.getMessage());
                }
            });

            writer.flush();
            long elapsed = System.currentTimeMillis() - startTime;
            String msg = export.getDataset().getDataSetName() + " exported " + totalProcessed + " stars to " + export.getFileName() + ".trips.csv";
            log.info("Finished exporting {} stars in {} ms", totalProcessed, elapsed);

            exportResults.setSuccess(true);
            exportResults.setMessage(msg);

        } catch (Exception e) {
            log.error("Export error: {}", e.getMessage(), e);
            exportResults.setMessage(export.getDataset() + " failed to export: " + e.getMessage());
        }

        return exportResults;
    }

    @Override
    public void updateTaskInfo(String message) {
        updateMessage(message + "  ");
    }
}
