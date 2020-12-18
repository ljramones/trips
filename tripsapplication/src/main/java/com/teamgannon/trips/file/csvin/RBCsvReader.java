package com.teamgannon.trips.file.csvin;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.teamgannon.trips.dialogs.dataset.Dataset;
import com.teamgannon.trips.dialogs.dataset.LoadUpdater;
import com.teamgannon.trips.file.csvin.model.RBCSVStar;
import com.teamgannon.trips.jpa.model.AstrographicObject;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.importservices.tasks.ProgressUpdater;
import com.teamgannon.trips.stardata.StellarFactory;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

/**
 * parse a csv file with the following entries
 * <p>
 * name,type,ra,dec,pmra,pmdec,parallax,radialvel,spectral,magv,bprp,bpg,grp,temp,position,distance,source,nnclass,catalogid
 */
@Slf4j
@Component
public class RBCsvReader {

    /**
     * the stellar factory
     */
    private final @NotNull StellarFactory stellarFactory;
    private final DatabaseManagementService databaseManagementService;

    public RBCsvReader(DatabaseManagementService databaseManagementService) {
        this.stellarFactory = new StellarFactory();
        this.databaseManagementService = databaseManagementService;
    }

    public @NotNull RBCsvFile loadFile(@NotNull ProgressUpdater loadUpdater, @NotNull File file, @NotNull Dataset dataset) {
        RBCsvFile rbCsvFile = new RBCsvFile();
        rbCsvFile.setDataset(dataset);
        dataset.setFileSelected(file.getAbsolutePath());

        long totalCount = 0;
        boolean readComplete = false;
        double maxDistance = 0.0;

        try {
            Reader reader = Files.newBufferedReader(Paths.get(file.toURI()));
            CSVReader csvReader = new CSVReader(reader);
            // skip header
            csvReader.skip(1);

            do {
                Set<AstrographicObject> starSet = new HashSet<>();
                int loopCounter = 0;
                for (int i = 0; i < 20000; i++) {

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

                // save all the stars we've read so far
                databaseManagementService.starBulkSave(starSet);
                totalCount += loopCounter;
                log.info("\n\nsaving {} entries, total count is {}\n\n", loopCounter, totalCount);
                loadUpdater.updateLoadInfo(String.format(" saving %s entries, total count is %s ", loopCounter, totalCount));
            } while (!readComplete); // the moment readComplete turns true, we stop

            log.info("File load report: total:{}, accepts:{}, rejects:{}",
                    rbCsvFile.getSize(),
                    rbCsvFile.getNumbAccepts(),
                    rbCsvFile.getNumbRejects());
        } catch (@NotNull IOException | CsvValidationException e) {
            log.error("failed to read file because: {}", e.getMessage());
        }

        rbCsvFile.setMaxDistance(maxDistance);

        return rbCsvFile;
    }

}
