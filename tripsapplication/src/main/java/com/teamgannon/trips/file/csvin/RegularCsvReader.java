package com.teamgannon.trips.file.csvin;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.teamgannon.trips.dialogs.dataset.Dataset;
import com.teamgannon.trips.file.csvin.model.AstroCSVStar;
import com.teamgannon.trips.jpa.model.AstrographicObject;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.importservices.tasks.ProgressUpdater;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
public class RegularCsvReader {

    private final DatabaseManagementService databaseManagementService;

    public RegularCsvReader(DatabaseManagementService databaseManagementService) {
        this.databaseManagementService = databaseManagementService;
    }

    public RBCsvFile loadFile(ProgressUpdater progressUpdater, File file, Dataset dataset) {
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
                    AstroCSVStar star = AstroCSVStar
                            .builder()
                            // skip id lineRead[0]
                            // read dataset name
                            .datasetName(dataset.getName())
                            .displayName(lineRead[2])
                            .constellationName(lineRead[3])
                            .mass(lineRead[4])
                            .actualMass(lineRead[5])
                            .source(lineRead[6])
                            .catalogIdList(lineRead[7])
                            .x(lineRead[8])
                            .y(lineRead[9])
                            .z(lineRead[10])
                            .radius(lineRead[11])
                            .ra(lineRead[12])
                            .pmra(lineRead[13])
                            .declination(lineRead[14])
                            .pmdec(lineRead[15])
                            .dec_deg(lineRead[16])
                            .rs_cdeg(lineRead[17])
                            .parallax(lineRead[18])
                            .distance(lineRead[19])
                            .radialVelocity(lineRead[20])
                            .spectralClass(lineRead[21])
                            .orthoSpectralClass(lineRead[22])
                            .temperature(lineRead[23])
                            .realStar(lineRead[24])
                            .bprp(lineRead[25])
                            .bpg(lineRead[26])
                            .grp(lineRead[27])
                            .luminosity(lineRead[28])
                            .magu(lineRead[29])
                            .magb(lineRead[30])
                            .magv(lineRead[31])
                            .magr(lineRead[32])
                            .magi(lineRead[33])
                            .other(lineRead[34])
                            .anomaly(lineRead[35])
                            .polity(lineRead[36])
                            .worldType(lineRead[37])
                            .fuelType(lineRead[38])
                            .portType(lineRead[39])
                            .populationType(lineRead[40])
                            .techType(lineRead[41])
                            .productType(lineRead[42])
                            .milSpaceType(lineRead[43])
                            .milPlanType(lineRead[44])
                            .miscText1(lineRead[45])
                            .miscText2(lineRead[46])
                            .miscText3(lineRead[47])
                            .miscText4(lineRead[48])
                            .miscText5(lineRead[49])
                            .miscNum1(Double.parseDouble(lineRead[50]))
                            .miscNum2(Double.parseDouble(lineRead[51]))
                            .miscNum3(Double.parseDouble(lineRead[52]))
                            .miscNum4(Double.parseDouble(lineRead[53]))
                            .miscNum5(Double.parseDouble(lineRead[54]))
                            .notes(lineRead[55])
                            .build();
                    try {
                        double distance = Double.parseDouble(star.getDistance());
                        if (distance > maxDistance) {
                            maxDistance = distance;
                        }
                    } catch (NumberFormatException nfe) {
                        log.error("Error getting distance for {}", star.getDisplayName());
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
                progressUpdater.updateLoadInfo(String.format("Saving %d  of total so far = %d", 20000, totalCount));
                log.info("\n\nsaving {} entries, total count is {}\n\n", loopCounter, totalCount);
            } while (!readComplete); // the moment readComplete turns true, we stop

            log.info("File load report: total:{}, accepts:{}, rejects:{}", rbCsvFile.getSize(), rbCsvFile.getNumbAccepts(), rbCsvFile.getNumbRejects());
        } catch (IOException | CsvValidationException e) {
            progressUpdater.updateLoadInfo("failed to read file because: " + e.getMessage());
            log.error("failed to read file because: {}", e.getMessage());
        }

        rbCsvFile.setMaxDistance(maxDistance);
        progressUpdater.updateLoadInfo("load of dataset Complete");

        return rbCsvFile;
    }


}
