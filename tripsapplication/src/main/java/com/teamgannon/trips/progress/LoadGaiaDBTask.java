package com.teamgannon.trips.progress;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.teamgannon.trips.dialogs.dataset.Dataset;
import com.teamgannon.trips.file.csvin.RBCsvFile;
import com.teamgannon.trips.file.csvin.model.RBCSVStar;
import com.teamgannon.trips.jpa.model.AstrographicObject;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.stardata.StellarFactory;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class LoadGaiaDBTask extends Task<Integer> {

    private int totalCount = 0;
    private int loopCounter = 0;
    private boolean readComplete = false;
    private double maxDistance = 0.0;
    private final File file;
    private final Dataset dataset;
    private final DatabaseManagementService databaseManagementService;

    private final StellarFactory stellarFactory;

    private RBCsvFile rbCsvFile;

    public LoadGaiaDBTask(File file,
                          Dataset dataset,
                          DatabaseManagementService databaseManagementService) {
        this.file = file;
        this.dataset = dataset;
        this.databaseManagementService = databaseManagementService;
        this.stellarFactory = new StellarFactory();
    }

    @Override
    protected Integer call() throws Exception {
        updateMessage("loading DB . . .");
        rbCsvFile = new RBCsvFile();
        rbCsvFile.setDataset(dataset);
        dataset.setFileSelected(file.getAbsolutePath());

        try {

            Reader reader = Files.newBufferedReader(Paths.get(file.toURI()));
            CSVReader csvReader = new CSVReader(reader);
            // skip header
            csvReader.skip(1);

            do {
                Set<AstrographicObject> starSet = new HashSet<>();

                // read bulk records
                readBatchRecords(csvReader, starSet, 20000);

                // save all the stars we've read so far
                databaseManagementService.starBulkSave(starSet);
                totalCount += loopCounter;
                log.info("\n\nsaving {} entries, total count is {}\n\n", loopCounter, totalCount);

                updateProgress(totalCount, Long.MAX_VALUE);

            } while (!readComplete); // the moment readComplete turns true, we stop

            updateMessage("Loading DB records . . . found " + totalCount);
        } catch (IOException | CsvValidationException e) {
            log.error("failed to read file because: {}", e.getMessage());
        }
        rbCsvFile.setMaxDistance(maxDistance);

        return totalCount;
    }

    private void readBatchRecords(CSVReader csvReader, Set<AstrographicObject> starSet, int recordsInBatch) throws IOException, CsvValidationException {
        for (int i = 0; i < recordsInBatch; i++) {

            String[] lineRead = csvReader.readNext();
            if (lineRead == null) {
                readComplete = true;
                break;
            }
            loopCounter++;
            RBCSVStar star = new RBCSVStar(stellarFactory,
                    lineRead[0], lineRead[1], lineRead[2],
                    lineRead[3], lineRead[4], lineRead[5],
                    lineRead[6], lineRead[7], lineRead[8],
                    lineRead[9], lineRead[10], lineRead[11],
                    lineRead[12], lineRead[13], lineRead[14],
                    lineRead[15], lineRead[16], lineRead[17],
                    lineRead[18]);
            try {
                double distance = Double.parseDouble(star.getDistance());
                if (distance > maxDistance) {
                    maxDistance = distance;
                }
            } catch (NumberFormatException nfe) {
                log.error("Error getting distance for {}", star.getName());
            }
            try {
                AstrographicObject astrographicObject = star.toAstrographicObject();
                if (astrographicObject != null) {
                    astrographicObject.setDataSetName(dataset.getName());
                    starSet.add(astrographicObject);
                    rbCsvFile.incAccepts();
                } else {
                    rbCsvFile.incRejects();
                }
                rbCsvFile.incTotal();
            } catch (Exception e) {
                log.error("failed to parse star:{}, because of {}", star, e.getMessage());
            }
        }
    }

    public Integer getFinalCount() {
        return totalCount;
    }

    public RBCsvFile getFile() {
        return rbCsvFile;
    }

}
