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
                        log.error(">>> Null line encountered at line={}", totalCount);
                        readComplete = true;
                        break;
                    }

                    String[] splitLine = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                    String[] lineRead = linePad(splitLine, splitLine.length);
                    loopCounter++;
                    AstroCSVStar star = AstroCSVStar
                            .builder()
                            // skip id lineRead[0]
                            // read dataset name
                            .datasetName(dataset.getName())
                            .displayName(testForNull(lineRead, 2, ""))
                            .commonName(testForNull(lineRead, 3, ""))
                            .simbadId(testForNull(lineRead, 4, ""))
                            .gaiaId(testForNull(lineRead, 5, ""))
                            .constellationName(testForNull(lineRead, 6, ""))
                            .mass(testForNull(lineRead, 7, "0"))
                            .age(testForNull(lineRead, 8, "0"))
                            .metallicity(testForNull(lineRead, 9, "0"))
                            .source(testForNull(lineRead, 10, ""))
                            .catalogIdList(testForNull(lineRead, 11, ""))
                            .x(testForNull(lineRead, 12, "0"))
                            .y(testForNull(lineRead, 13, "0"))
                            .z(testForNull(lineRead, 14, "0"))
                            .radius(testForNull(lineRead, 15, "0"))
                            .ra(testForNull(lineRead, 16, "0"))
                            .pmra(testForNull(lineRead, 17, "0"))
                            .declination(testForNull(lineRead, 18, "0"))
                            .pmdec(testForNull(lineRead, 19, "0"))
                            .parallax(testForNull(lineRead, 20, "0"))
                            .distance(testForNull(lineRead, 21, "0"))
                            .radialVelocity(testForNull(lineRead, 22, ""))
                            .spectralClass(testForNull(lineRead, 23, ""))
                            .orthoSpectralClass(testForNull(lineRead, 24, ""))
                            .temperature(testForNull(lineRead, 25, "0"))
                            .realStar(testForNull(lineRead, 26, "true"))
                            .bprp(testForNull(lineRead, 27, "0"))
                            .bpg(testForNull(lineRead, 28, "0"))
                            .grp(testForNull(lineRead, 29, "0"))
                            .luminosity(testForNull(lineRead, 30, ""))
                            .magu(testForNull(lineRead, 31, "0"))
                            .magb(testForNull(lineRead, 32, "0"))
                            .magv(testForNull(lineRead, 33, "0"))
                            .magr(testForNull(lineRead, 34, "0"))
                            .magi(testForNull(lineRead, 35, "0"))
                            .other(testForNull(lineRead, 36, "false"))
                            .anomaly(testForNull(lineRead, 37, "false"))
                            .polity(testForNull(lineRead, 38, "NA"))
                            .worldType(testForNull(lineRead, 39, "NA"))
                            .fuelType(testForNull(lineRead, 40, "NA"))
                            .portType(testForNull(lineRead, 41, "NA"))
                            .populationType(testForNull(lineRead, 42, "NA"))
                            .techType(testForNull(lineRead, 43, "NA"))
                            .productType(testForNull(lineRead, 44, "NA"))
                            .milSpaceType(testForNull(lineRead, 45, "NA"))
                            .milPlanType(testForNull(lineRead, 46, "NA"))
                            .miscText1(testForNull(lineRead, 47, ""))
                            .miscText2(testForNull(lineRead, 48, ""))
                            .miscText3(testForNull(lineRead, 49, ""))
                            .miscText4(testForNull(lineRead, 50, ""))
                            .miscText5(testForNull(lineRead, 51, ""))
                            .miscNum1(parseDouble(testForNull(lineRead, 52, "0")))
                            .miscNum2(parseDouble(testForNull(lineRead, 53, "0")))
                            .miscNum3(parseDouble(testForNull(lineRead, 54, "0")))
                            .miscNum4(parseDouble(testForNull(lineRead, 55, "0")))
                            .miscNum5(parseDouble(testForNull(lineRead, 56, "0")))
                            .notes(testForNull(lineRead, 57, "none"))
                            .galacticLattitude(testForNull(lineRead, 58, "0"))
                            .galacticLongitude(testForNull(lineRead, 59, "0"))
                            .build();
                    try {
                        double distance = Double.parseDouble(star.getDistance());
                        if (distance > maxDistance) {
                            maxDistance = distance;
                        }
                    } catch (NumberFormatException nfe) {
                        log.error("Error getting distance for {}, coordinates are ({},{},{})", star.getDisplayName(), star.getX(), star.getY(), star.getZ());
                    }
                    try {
                        StarObject starObject = star.toStarObject();
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
                progressUpdater.updateTaskInfo(String.format("Saving %d  of total so far = %d", 20000, totalCount));
                log.info("\n\nsaving {} entries, total count is {}\n\n", loopCounter, totalCount);
            } while (!readComplete); // the moment readComplete turns true, we stop

            log.info("File load report: total:{}, accepts:{}, rejects:{}", csvFile.getSize(), csvFile.getNumbAccepts(), csvFile.getNumbRejects());
            csvFile.setReadSuccess(true);

            csvFile.setProcessMessage(String.format("File load report: total:%d, accepts:%d, rejects:%d", csvFile.getSize(), csvFile.getNumbAccepts(), csvFile.getNumbRejects()));
        } catch (IOException e) {
            progressUpdater.updateTaskInfo("failed to read file because: " + e.getMessage());
            log.error("failed to read file because: {}", e.getMessage());
            csvFile.setReadSuccess(false);
            csvFile.setProcessMessage("failed to read file because: " + e.getMessage());
        }

        if (csvFile.isReadSuccess()) {
            csvFile.setMaxDistance(maxDistance);
            csvFile.getDataSetDescriptor().setNumberStars(totalCount);
            csvFile.getDataSetDescriptor().setDistanceRange(maxDistance);
            progressUpdater.updateTaskInfo("load of dataset complete with " + totalCount + " stars loaded");
        }

        return csvFile;
    }

    private String testForNull(String[] lineRead, int i, String optional) {
        if (i >= lineRead.length) {
            return "";
        } else {
            String value = lineRead[i].trim();
            return value.isEmpty() ? optional : value;
        }

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
