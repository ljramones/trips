package com.teamgannon.trips.service;

import com.teamgannon.trips.dataset.factories.DataSetDescriptorFactory;
import com.teamgannon.trips.dialogs.dataset.model.Dataset;
import com.teamgannon.trips.file.chview.model.ChViewFile;
import com.teamgannon.trips.file.compact.CompactFile;
import com.teamgannon.trips.file.csvin.RegCSVFile;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.repository.DataSetDescriptorRepository;
import com.teamgannon.trips.jpa.repository.StarObjectRepository;
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

    /**
     * storage of data sets in DB
     */
    private final DataSetDescriptorRepository dataSetDescriptorRepository;


    /**
     * storage of astrographic objects in DB
     */
    private  StarObjectRepository starObjectRepository;

    private final StarService starService;
    private final DatasetService datasetService;

    public BulkLoadService(StarService starService,
                           DatasetService datasetService,
                           DataSetDescriptorRepository dataSetDescriptorRepository,
                           StarObjectRepository starObjectRepository) {
        this.starService = starService;
        this.datasetService = datasetService;
        this.dataSetDescriptorRepository = dataSetDescriptorRepository;
        this.starObjectRepository = starObjectRepository;
    }


    @TrackExecutionTime
    public @NotNull
    DataSetDescriptor loadCHFile(@NotNull ProgressUpdater progressUpdater, @NotNull Dataset dataset, @NotNull ChViewFile chViewFile) throws Exception {

        // this method call actually saves the dataset in elasticsearch
        return DataSetDescriptorFactory.createDataSetDescriptor(
                progressUpdater,
                dataset,
                dataSetDescriptorRepository,
                starObjectRepository,
                chViewFile
        );
    }


    @TrackExecutionTime
    public @NotNull
    DataSetDescriptor loadCSVFile(@NotNull RegCSVFile regCSVFile) throws Exception {
        return DataSetDescriptorFactory.createDataSetDescriptor(
                dataSetDescriptorRepository,
                regCSVFile
        );
    }

    @TrackExecutionTime
    public @NotNull
    DataSetDescriptor loadCompactFile(CompactFile compactFile) throws Exception {
        return DataSetDescriptorFactory.createDataSetDescriptor(
                dataSetDescriptorRepository,
                compactFile
        );
    }

    /**
     * remove the dataset by descriptor
     *
     * @param descriptor the descriptor to remove
     */
    @Transactional
    public void removeDataSet(@NotNull DataSetDescriptor descriptor) {
        starObjectRepository.deleteByDataSetName(descriptor.getDataSetName());
        dataSetDescriptorRepository.delete(descriptor);
    }


    @TrackExecutionTime
    public void loadJsonFileSingleDS(ProgressUpdater updater, JsonExportObj jsonExportObj) {
        dataSetDescriptorRepository.save(jsonExportObj.getDescriptor().toDataSetDescriptor());
        updater.updateTaskInfo("saved descriptor in database");
        starObjectRepository.saveAll(jsonExportObj.getStarObjectList());
        updater.updateTaskInfo("saved all stars in database");
    }


}
