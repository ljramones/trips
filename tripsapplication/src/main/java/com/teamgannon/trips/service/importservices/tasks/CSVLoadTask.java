package com.teamgannon.trips.service.importservices.tasks;

import com.teamgannon.trips.dialogs.dataset.model.Dataset;
import com.teamgannon.trips.dialogs.dataset.model.FileProcessResult;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.service.BulkLoadService;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class CSVLoadTask extends Task<FileProcessResult> implements ProgressUpdater {

    private final BulkLoadService bulkLoadService;
    private final Dataset dataSet;

    public CSVLoadTask(BulkLoadService bulkLoadService, Dataset loadDataset) {
        this.bulkLoadService = bulkLoadService;
        this.dataSet = loadDataset;
    }

    @Override
    protected @NotNull FileProcessResult call() {
        FileProcessResult result = processCSVFile(dataSet);
        if (result.isSuccess()) {
            log.info("New dataset {} added", dataSet.getName());
        } else {
            log.error("CSV load failed: {}", result.getMessage());
        }
        return result;
    }


    public FileProcessResult processCSVFile(@NotNull Dataset dataset) {
        log.info("beginning processing of dataset={}", dataset);
        FileProcessResult processResult = new FileProcessResult();

        try {
            // Phase 1.2: a single transactional entry point drives both the
            // batched star inserts and the descriptor save. A failure anywhere
            // rolls the entire import back — no orphan descriptor, no stars
            // without a descriptor.
            updateMessage(" Loading dataset, please wait...");
            BulkLoadService.LoadOutcome outcome = bulkLoadService.loadCsvDataset(this, dataset);
            DataSetDescriptor dataSetDescriptor = outcome.descriptor();
            String baseMessage = String.format(" %s records loaded from dataset %s",
                    dataSetDescriptor.getNumberStars(),
                    dataSetDescriptor.getDataSetName());
            String message = appendRejectSummary(baseMessage, outcome) + ", Use plot to see data.";
            processResult.setMessage(message);
            processResult.setSuccess(true);
            processResult.setDataSetDescriptor(dataSetDescriptor);
            processResult.setRejectCount(outcome.rejectCount());
            processResult.setSampleBadRows(outcome.sampleBadRows());
            if (outcome.rejectCount() > 0) {
                log.warn("{} row(s) rejected during import of {}; first samples: {}",
                        outcome.rejectCount(), dataset.getName(), outcome.sampleBadRows());
            }
            updateTaskInfo(appendRejectSummary(baseMessage, outcome));
        } catch (Exception e) {
            log.error("Failed to load dataset {}", dataset.getName(), e);
            processResult.setSuccess(false);
            processResult.setMessage("Failed to load the dataset, see log: " + e.getMessage());
        }

        return processResult;
    }

    private static String appendRejectSummary(String base, BulkLoadService.LoadOutcome outcome) {
        if (outcome.rejectCount() <= 0) {
            return base;
        }
        String sample = outcome.sampleBadRows().isEmpty()
                ? ""
                : "; first samples: " + String.join(" | ", outcome.sampleBadRows());
        return String.format("%s (%d row(s) rejected as malformed%s)",
                base, outcome.rejectCount(), sample);
    }

    @Override
    public void updateTaskInfo(String message) {
        updateMessage(message + "  ");
    }


}
