package com.teamgannon.trips.file.csvin;

import com.teamgannon.trips.dialogs.dataset.Dataset;
import com.teamgannon.trips.file.csvin.model.AstroCSVStar;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.importservices.tasks.ProgressUpdater;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

@Slf4j
@Component
public class RegularCsvReader {

    private final DatabaseManagementService databaseManagementService;

    public RegularCsvReader(DatabaseManagementService databaseManagementService) {
        this.databaseManagementService = databaseManagementService;
    }

    public @NotNull RegCSVFile loadFile(@NotNull ProgressUpdater progressUpdater, @NotNull File file, @NotNull Dataset dataset) {
        RegCSVFile csvFile = new RegCSVFile();
        csvFile.setDataset(dataset);
        dataset.setFileSelected(file.getAbsolutePath());

        long totalCount = 0;
        boolean readComplete = false;
        double maxDistance = 0.0;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));

            // read descriptor
            // read first to determine whether this file follows the backup protocol or not.
            // backup protocol means that the two lines hold the dataset descriptor
            // if the first header starts with dataSetName, then backup, if it start with id, then its a fresh load
            // if it start with anythign else then it's malformed
            String backup = reader.readLine();
            String[] indicator = backup.split(",");
            if (indicator[0].trim().equals("dataSetName")) {
                // this is a dataset
                String readLine = reader.readLine();
                String[] descriptor = readLine.split(",");
                csvFile.setDataSetDescriptor(transformDescriptor(dataset, descriptor));
            } else if (!indicator[0].trim().equals("\uFEFFId")) {
                // we handle the error case here
                // since it is neither of the cases then we can't reliably know what kind of file was loaded.
                String message = "This file is malformed, we did not find either a backup marker(dataSetName) or a load marker(Id) in the first line, first position";
                progressUpdater.updateLoadInfo(message);
                log.error(message);
                csvFile.setReadSuccess(false);
                csvFile.setProcessMessage(message);
                return csvFile;
            } else {
                csvFile.setDataSetDescriptor(createDescriptor(dataset));
            }

            // read stars
            // skip header
            reader.readLine();

            do {
                Set<StarObject> starSet = new HashSet<>();
                int loopCounter = 0;
                for (int i = 0; i < 20000; i++) {

                    String line = reader.readLine();

                    if (line == null) {
                        System.out.println(">>> bad line");
                        readComplete = true;
                        break;
                    }

                    String[] splitLine = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                    String[] lineRead = linePad(splitLine, 56);
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
                            .miscNum1(parseDouble(lineRead[50]))
                            .miscNum2(parseDouble(lineRead[51]))
                            .miscNum3(parseDouble(lineRead[52]))
                            .miscNum4(parseDouble(lineRead[53]))
                            .miscNum5(parseDouble(lineRead[54]))
                            .notes(lineRead[55])
                            .build();
                    try {
                        double distance = Double.parseDouble(star.getDistance());
                        if (distance > maxDistance) {
                            maxDistance = distance;
                            System.out.println("new distance=" + distance);
                        }
                    } catch (NumberFormatException nfe) {
                        log.error("Error getting distance for {}, coordinates are ({},{},{})", star.getDisplayName(), star.getX(), star.getY(), star.getZ());
                    }
                    try {
                        StarObject starObject = star.toAstrographicObject();
                        if (starObject != null) {
                            starObject.setDataSetName(dataset.getName());
                            starSet.add(starObject);
                            csvFile.incAccepts();
                        } else {
                            csvFile.incRejects();
                        }
                        csvFile.incTotal();
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

            log.info("File load report: total:{}, accepts:{}, rejects:{}", csvFile.getSize(), csvFile.getNumbAccepts(), csvFile.getNumbRejects());
            csvFile.setReadSuccess(true);

            csvFile.setProcessMessage(String.format("File load report: total:%d, accepts:%d, rejects:%d", csvFile.getSize(), csvFile.getNumbAccepts(), csvFile.getNumbRejects()));
        } catch (IOException e) {
            progressUpdater.updateLoadInfo("failed to read file because: " + e.getMessage());
            log.error("failed to read file because: {}", e.getMessage());
            csvFile.setReadSuccess(false);
            csvFile.setProcessMessage("failed to read file because: " + e.getMessage());
        }

        if (csvFile.isReadSuccess()) {
            csvFile.setMaxDistance(maxDistance);
            csvFile.getDataSetDescriptor().setNumberStars(totalCount);
            csvFile.getDataSetDescriptor().setDistanceRange(maxDistance);
            progressUpdater.updateLoadInfo("load of dataset complete with " + totalCount + " stars loaded");
        }

        return csvFile;
    }


    private double parseDouble(String string) {
        if (string.isEmpty()) {
            return 0;
        }
        try {
            return Double.parseDouble(string);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String[] linePad(String[] split, int size) {
        String[] pad = new String[size];
        IntStream.range(0, size).forEachOrdered(i -> {
            if (i < split.length) {
                pad[i] = split[i];
            } else {
                pad[i] = "";
            }
        });
        return pad;
    }


    private DataSetDescriptor createDescriptor(Dataset dataset) {
        DataSetDescriptor descriptor = new DataSetDescriptor();
        descriptor.setDataSetName(dataset.getName());
        descriptor.setDatasetType(descriptor.getDatasetType());
        descriptor.setFilePath(dataset.getFileSelected());
        descriptor.setFileCreator(dataset.getAuthor());
        descriptor.setFileOriginalDate(Instant.now().getEpochSecond());
        descriptor.setFileNotes(dataset.getNotes());
        return descriptor;
    }

    private @NotNull DataSetDescriptor transformDescriptor(@NotNull Dataset dataset, String[] descriptorVals) {
        try {
            DataSetDescriptor descriptor = new DataSetDescriptor();
            descriptor.setDataSetName(dataset.getName());
            descriptor.setFilePath(putBackCommas(descriptorVals[1]));
            descriptor.setFileCreator(dataset.getAuthor());
            descriptor.setFileOriginalDate(Long.parseLong(descriptorVals[3]));
            descriptor.setFileNotes(dataset.getNotes());
            descriptor.setDatasetType(putBackCommas(descriptorVals[5]));
            descriptor.setNumberStars(Long.parseLong(descriptorVals[6]));
            descriptor.setDistanceRange(Double.parseDouble(descriptorVals[7]));
            if (descriptorVals[8].equals("null")) {
                descriptor.setNumberRoutes(0);
            } else {
                descriptor.setNumberRoutes(Integer.parseInt(descriptorVals[8]));
            }
            descriptor.setThemeStr(putBackCommas(descriptorVals[9]));
//            descriptor.setAstrographicDataList(new HashSet<>());
            if (!descriptorVals[11].equals("null")) {
                descriptor.setRoutesStr(putBackCommas(descriptorVals[11]));
            } else {
                descriptor.setRoutesStr(null);
            }
            descriptor.setCustomDataDefsStr(putBackCommas(descriptorVals[12]));
            descriptor.setCustomDataValuesStr(putBackCommas(descriptorVals[13]));
            return descriptor;
        } catch (Exception e) {
            log.error("failed to read datadescriptor");
            DataSetDescriptor defaultDes = new DataSetDescriptor();
            defaultDes.setDataSetName("file-" + UUID.randomUUID().toString());
            return defaultDes;
        }
    }


    private @Nullable String putBackCommas(@Nullable String origin) {
        String replaced = origin;
        if (origin != null) {
            replaced = origin.replace("~", ",");
        }
        return replaced;
    }

}
