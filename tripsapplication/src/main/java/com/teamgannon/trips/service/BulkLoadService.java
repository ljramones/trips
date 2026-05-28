package com.teamgannon.trips.service;

import com.teamgannon.trips.dataset.factories.DataSetDescriptorFactory;
import com.teamgannon.trips.dialogs.dataset.model.Dataset;
import com.teamgannon.trips.file.chview.model.ChViewFile;
import com.teamgannon.trips.file.csvin.RegCSVFile;
import com.teamgannon.trips.file.csvin.RegularStarCatalogCsvReader;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.repository.DataSetDescriptorRepository;
import com.teamgannon.trips.jpa.repository.StarObjectRepository;
import com.teamgannon.trips.measure.TrackExecutionTime;
import com.teamgannon.trips.service.importservices.tasks.ProgressUpdater;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;

@Slf4j
@Service
public class BulkLoadService {

    /**
     * storage of data sets in DB
     */
    private final DataSetDescriptorRepository dataSetDescriptorRepository;


    /**
     * storage of astrographic objects in DB
     */
    private final StarObjectRepository starObjectRepository;

    private final StarService starService;
    private final DatasetService datasetService;
    private final DatabaseManagementService databaseManagementService;

    public BulkLoadService(StarService starService,
                           DatasetService datasetService,
                           DatabaseManagementService databaseManagementService,
                           DataSetDescriptorRepository dataSetDescriptorRepository,
                           StarObjectRepository starObjectRepository) {
        this.starService = starService;
        this.datasetService = datasetService;
        this.databaseManagementService = databaseManagementService;
        this.dataSetDescriptorRepository = dataSetDescriptorRepository;
        this.starObjectRepository = starObjectRepository;
    }


    /**
     * One-shot, atomic CSV import. Reads stars in batches (via
     * {@link RegularStarCatalogCsvReader}) and saves them plus a
     * {@link DataSetDescriptor} under a single transaction; if any batch fails
     * or the reader reports {@code readSuccess=false}, the whole import is
     * rolled back — no orphan descriptor, no half-loaded stars.
     * <p>
     * Phase 1.2 of the codebase-review remediation. Replaces the previous
     * pattern where {@code CSVLoadTask} drove the reader and descriptor save
     * separately, with no rollback boundary between them.
     *
     * @param progressUpdater UI progress sink (typically the calling JavaFX Task)
     * @param dataset         dataset metadata, including the selected file path
     * @return the persisted descriptor plus per-import reject stats (Issue 38)
     * @throws Exception on any read or persistence failure (transaction rolls back)
     */
    @TrackExecutionTime
    @Transactional(rollbackFor = Exception.class)
    public @NotNull LoadOutcome loadCsvDataset(@NotNull ProgressUpdater progressUpdater,
                                               @NotNull Dataset dataset) throws Exception {
        File file = new File(dataset.getFileSelected());
        RegularStarCatalogCsvReader reader =
                new RegularStarCatalogCsvReader(databaseManagementService, starService);
        RegCSVFile regCSVFile = reader.loadFile(progressUpdater, file, dataset);
        if (!regCSVFile.isReadSuccess()) {
            // Throwing inside the @Transactional method triggers a rollback,
            // wiping any per-batch stars that may have been flushed before the failure.
            throw new RuntimeException("CSV load failed: " + regCSVFile.getProcessMessage());
        }
        DataSetDescriptor descriptor = loadCSVFile(regCSVFile);
        return new LoadOutcome(descriptor,
                regCSVFile.getNumbRejects(),
                regCSVFile.getBadRowSamples());
    }

    /**
     * Result of {@link #loadCsvDataset}: the persisted descriptor plus
     * per-import reject statistics (count + sample of the first
     * {@link RegCSVFile#MAX_BAD_ROW_SAMPLES} bad rows).
     */
    public record LoadOutcome(DataSetDescriptor descriptor,
                              long rejectCount,
                              List<String> sampleBadRows) {
    }


    @TrackExecutionTime
    @Transactional(rollbackFor = Exception.class)
    public @NotNull
    DataSetDescriptor loadCHFile(@NotNull ProgressUpdater progressUpdater, @NotNull Dataset dataset, @NotNull ChViewFile chViewFile) throws Exception {

        // ChView import is fully orchestrated inside the factory — wrapping this
        // method in @Transactional makes the whole import atomic (Phase 1.2).
        return DataSetDescriptorFactory.createDataSetDescriptor(
                progressUpdater,
                dataset,
                dataSetDescriptorRepository,
                starObjectRepository,
                chViewFile
        );
    }


    /**
     * Persists the descriptor for an already-parsed CSV import. Public for the
     * legacy {@code CSVLoadTask} flow; new code should call
     * {@link #loadCsvDataset(ProgressUpdater, Dataset)} which gives true
     * read+save atomicity.
     */
    @TrackExecutionTime
    @Transactional(rollbackFor = Exception.class)
    public @NotNull
    DataSetDescriptor loadCSVFile(@NotNull RegCSVFile regCSVFile) throws Exception {
        return DataSetDescriptorFactory.createDataSetDescriptor(
                dataSetDescriptorRepository,
                regCSVFile
        );
    }

    /**
     * remove the dataset by descriptor
     *
     * @param descriptor the descriptor to remove
     */
    @Transactional
    public void removeDataSet(@NotNull DataSetDescriptor descriptor) {
        int deletedStars = starObjectRepository.deleteByDataSetName(descriptor.getDataSetName());
        log.info("Deleted {} stars for dataset {}", deletedStars, descriptor.getDataSetName());
        dataSetDescriptorRepository.delete(descriptor);
    }

}
