package com.teamgannon.trips.service.export.tasks;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.teamgannon.trips.dialogs.dataset.model.ExportOptions;
import com.teamgannon.trips.file.compact.DataSetDescriptorSerializer;
import com.teamgannon.trips.file.compact.StarObjectSerializer;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.StarService;
import com.teamgannon.trips.service.export.ExportResults;
import com.teamgannon.trips.service.importservices.tasks.ProgressUpdater;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;

import javax.xml.crypto.Data;
import java.io.FileOutputStream;
import java.util.List;

@Slf4j
public class KryoDataExportTask extends Task<ExportResults> implements ProgressUpdater {


    private final int PAGE_SIZE = 20000;

    private final ExportOptions export;
    private final DatabaseManagementService databaseManagementService;
    private final StarService starService;

    public KryoDataExportTask(ExportOptions export,
                              DatabaseManagementService databaseManagementService,
                              StarService starService) {
        this.export = export;
        this.databaseManagementService = databaseManagementService;
        this.starService = starService;
    }

    @Override
    public void updateTaskInfo(String message) {
        updateMessage(message + "  ");
    }

    @Override
    protected ExportResults call() throws Exception {
        ExportResults result = processKryoFile(export);
        if (result.isSuccess()) {
            log.info("New dataset {} added", export.getFileName());
        } else {
            log.error("load cpt: " + result.getMessage());
        }

        return result;
    }

    private ExportResults processKryoFile(ExportOptions export) {
        ExportResults exportResults = ExportResults.builder().success(false).build();

        try {
            Long count = starService.getCountOfDataset(export.getDataset().getDataSetName());

            Kryo kryo = new Kryo();

            kryo.register(StarObject.class, new StarObjectSerializer());
            kryo.register(DataSetDescriptor.class, new DataSetDescriptorSerializer());

            String fileName = export.getFileName() + ".trips.cpt";
            Output output = new Output(new FileOutputStream(fileName));

            // write count into export file
            kryo.writeObjectOrNull(output, count, Long.class);

            // write datasetdescriptor into export file
            kryo.writeObjectOrNull(output, export.getDataset(), DataSetDescriptor.class);

            int total = 0;
            int pageNumber = 0;
            Page<StarObject> starObjectPage = starService.getFromDatasetByPage(export.getDataset(), pageNumber, PAGE_SIZE);
            int totalPages = starObjectPage.getTotalPages();

            for (int i = 0; i < totalPages; i++) {
                starObjectPage = starService.getFromDatasetByPage(export.getDataset(), i, PAGE_SIZE);
                List<StarObject> starObjects = starObjectPage.getContent();
                for (StarObject starObject : starObjects) {
                    kryo.writeObjectOrNull(output, starObject, StarObject.class);
                    total++;
                }
                updateTaskInfo(total + " elements written so far");
            }

            output.close();
            log.info("finished exporting file");
            exportResults.setSuccess(true);
        } catch (Exception e) {
            exportResults.setMessage("caught error exporting the file:{}" + e.getMessage());
            log.error("caught error exporting the file:{}", e.getMessage());

        }

        return exportResults;
    }

}
