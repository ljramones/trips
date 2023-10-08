package com.teamgannon.trips.service.importservices.tasks;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.teamgannon.trips.dialogs.dataset.model.Dataset;
import com.teamgannon.trips.dialogs.dataset.model.FileProcessResult;
import com.teamgannon.trips.file.compact.CompactFile;
import com.teamgannon.trips.file.compact.DataSetDescriptorSerializer;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.file.compact.StarObjectSerializer;
import com.teamgannon.trips.service.DatasetService;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;

import javax.xml.crypto.Data;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class KryoLoadTask extends Task<FileProcessResult> implements ProgressUpdater {


    private final int PAGE_SIZE = 20000;

    private final DatasetService datasetService;
    private final Dataset dataSet;
    private final DatabaseManagementService databaseManagementService;

    public KryoLoadTask(DatabaseManagementService databaseManagementService,
                        DatasetService datasetService,
                        Dataset dataSet) {
        this.datasetService = datasetService;
        this.dataSet = dataSet;
        this.databaseManagementService = databaseManagementService;
    }

    @Override
    public void updateTaskInfo(String message) {
        updateMessage(message + "  ");
    }

    @Override
    protected FileProcessResult call() throws Exception {
        FileProcessResult result = processKryoFile(dataSet);
        if (result.isSuccess()) {
            log.info("New dataset {} added", dataSet.getName());
        } else {
            log.error("load csv" + result.getMessage());
        }

        return result;
    }

    private FileProcessResult processKryoFile(Dataset dataSet) {
        log.info("beginning processing of dataset={}", dataSet);
        CompactFile compactFile = new CompactFile();
        FileProcessResult processResult = new FileProcessResult();

        Kryo kryo = new Kryo();
        try {
            kryo.register(StarObject.class, new StarObjectSerializer());
            kryo.register(DataSetDescriptor.class, new DataSetDescriptorSerializer());

            Input input = new Input(new FileInputStream(dataSet.getFileSelected()));

            Long numberToRead = (Long) kryo.readClassAndObject(input);
            long numberPages = Math.floorDiv(numberToRead, PAGE_SIZE) + 1;
            int pageNumber = 0;

            // load descriptor from file  and make updates based on dataset values
            DataSetDescriptor descriptor = kryo.readObject(input, DataSetDescriptor.class);
            descriptor.setDataSetName(dataSet.getName());
            descriptor.setFileCreator(dataSet.getAuthor());
            if (!dataSet.getNotes().isEmpty()) {
                descriptor.setFileNotes(dataSet.getNotes());
            }

            // create preset space to hold what we read
            Set<StarObject> pageList = new HashSet<>(PAGE_SIZE);
            long total = 0;
            long start = System.currentTimeMillis();
            for (int i = pageNumber; i < numberPages; i++) {
                try {
                    // for the last page, it probably won't be a full page so calculate how many exactly
                    long limit = PAGE_SIZE;
                    if (i == (numberPages - 1)) {
                        limit = numberToRead - ((numberPages - 1) * PAGE_SIZE);
                    }
                    // read all the entries for a page
                    long readStart = System.currentTimeMillis();
                    for (int j = 0; j < limit; j++) {
                        StarObject starObject = kryo.readObjectOrNull(input, StarObject.class);
                        starObject.setDataSetName(dataSet.getName());
                        pageList.add(starObject);
                    }
                    long readEnd = System.currentTimeMillis();
                    log.info("read 20k records from file: time = {}", (readEnd - readStart));
                    total += limit;
                    updateTaskInfo(String.format("Loaded %d of %d", total, numberToRead));

                    // bulk save 20,000 records
                    databaseManagementService.starBulkSave(pageList);
                    pageList.clear();
                } catch (KryoException e) {
                    log.error("buffer underflow: actual count = {}", i);
                }
            }
            long end = System.currentTimeMillis();
            log.info("read Kryo records from file: time {} records = {}", numberToRead, (end - start));
            input.close();

            // set success criteria to report
            datasetService.saveDescriptor(descriptor);
            processResult.setDataSetDescriptor(descriptor);
            processResult.setSuccess(true);
            processResult.setMessage(String.format("Loaded %d records from %s file", numberToRead, dataSet.getFileSelected()));
        } catch (Exception e) {
            log.error("Failed to load file: " + e.getMessage());
            processResult.setSuccess(false);
            processResult.setMessage("Failed to load the dataset, see log ");
        }
        return processResult;
    }

    private void checkStar(CompactFile compactFile, StarObject starObject) {
        compactFile.updateDistance(starObject);
    }
}
