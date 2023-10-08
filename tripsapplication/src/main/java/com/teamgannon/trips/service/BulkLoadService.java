package com.teamgannon.trips.service;

import com.teamgannon.trips.dataset.factories.DataSetDescriptorFactory;
import com.teamgannon.trips.dialogs.dataset.model.Dataset;
import com.teamgannon.trips.file.chview.model.ChViewFile;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.measure.TrackExecutionTime;
import com.teamgannon.trips.service.export.model.JsonExportObj;
import com.teamgannon.trips.service.importservices.tasks.ProgressUpdater;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class BulkLoadService {

    private final StarService starService;
    private final DatasetService datasetService;

    public BulkLoadService(StarService starService, DatasetService datasetService) {
        this.starService = starService;
        this.datasetService = datasetService;
    }

//
//    @TrackExecutionTime
//    public void loadJsonFileSingleDS(ProgressUpdater updater, JsonExportObj jsonExportObj) {
//        datasetService.save(jsonExportObj.getDescriptor().toDataSetDescriptor());
//        updater.updateTaskInfo("saved descriptor in database");
//        starService.saveAllStars(jsonExportObj.getStarObjectList());
//        updater.updateTaskInfo("saved all stars in database");
//    }
//
//
//    @TrackExecutionTime
//    public @NotNull
//    DataSetDescriptor loadCHFile(@NotNull ProgressUpdater progressUpdater, @NotNull Dataset dataset, @NotNull ChViewFile chViewFile) throws Exception {
//
//        // this method call actually saves the dataset in elasticsearch
//        return DataSetDescriptorFactory.createDataSetDescriptor(
//                progressUpdater,
//                dataset,
//                datasetService,
//                starService,
//                chViewFile
//        );
//    }
//
//    /**
//     * remove the dataset by descriptor
//     *
//     * @param descriptor the descriptor to remove
//     */
//    @Transactional
//    public void removeDataSet(@NotNull DataSetDescriptor descriptor) {
//        starService.deleteFromDataset(descriptor.getDataSetName());
//        datasetService.delete(descriptor);
//    }



}
