package com.teamgannon.trips.file.csvin;

import com.teamgannon.trips.dialogs.dataset.model.Dataset;
import com.teamgannon.trips.file.csvin.model.AstroCSVStar;
import com.teamgannon.trips.file.csvin.model.LoadStats;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.StarService;
import com.teamgannon.trips.service.importservices.tasks.ProgressUpdater;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static com.teamgannon.trips.astrogation.Coordinates.calculateEquatorialCoordinates;
import static com.teamgannon.trips.astrogation.Coordinates.equatorialToGalactic;
import static com.teamgannon.trips.dialogs.gaiadata.CatalogUtils.cleanUpEntries;
import static com.teamgannon.trips.dialogs.gaiadata.CatalogUtils.setCatalogIds;

@Slf4j
public class RegularStarCatalogCsvReader {

    // Pre-compiled pattern for CSV splitting - handles quoted fields with commas
    private static final Pattern CSV_SPLIT_PATTERN = Pattern.compile(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

    // Increased batch size for better throughput with Hibernate batching
    private static final int BATCH_SIZE = 5000;

    private final DatabaseManagementService databaseManagementService;
    private final StarService starService;

    public RegularStarCatalogCsvReader(DatabaseManagementService databaseManagementService, StarService starService) {
        this.databaseManagementService = databaseManagementService;
        this.starService = starService;
    }

    public RegCSVFile loadFile(@NotNull ProgressUpdater progressUpdater,
                               @NotNull File file,
                               @NotNull Dataset dataset) {

        RegCSVFile csvFile = new RegCSVFile();
        csvFile.setDataset(dataset);
        dataset.setFileSelected(file.getAbsolutePath());

        LoadStats loadStats = LoadStats
                .builder()
                .dataSet(dataset)
                .loopCounter(0)
                .totalCount(0)
                .readComplete(false)
                .csvFile(csvFile)
                .build();

        try (BufferedReader reader = new BufferedReader(new FileReader(file), 65536)) {
            log.info("Starting read of CSV file={}", file.getAbsolutePath());
            long fileStart = System.currentTimeMillis();

            // read descriptor
            // read first to determine whether this file follows the backup protocol or not.
            // backup protocol means that the two lines hold the dataset descriptor
            // if the first header starts with dataSetName, then backup, if it starts with id, then it's a fresh load
            // if it starts with anything else then it's malformed
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

            // read stars in batches
            do {
                loadStats.clearLoopCounter();
                long start = System.currentTimeMillis();
                List<StarObject> starList = parseBulkRecords(BATCH_SIZE, reader, loadStats);
                long parseTime = System.currentTimeMillis() - start;

                // save all the stars we've read so far
                long saveStart = System.currentTimeMillis();
                starService.starBulkSave(starList);
                long saveTime = System.currentTimeMillis() - saveStart;

                loadStats.addToTotalCount(loadStats.getLoopCounter());
                progressUpdater.updateTaskInfo(String.format("--> %d loaded so far, please wait ", loadStats.getTotalCount()));
                log.info("Batch: {} records parsed in {}ms, saved in {}ms, total: {}",
                        loadStats.getLoopCounter(), parseTime, saveTime, loadStats.getTotalCount());
            } while (!loadStats.isReadComplete()); // the moment readComplete turns true, we stop

            long totalTime = System.currentTimeMillis() - fileStart;
            log.info("File load complete: total:{}, accepts:{}, rejects:{}, time:{}ms",
                    csvFile.getSize(), csvFile.getNumbAccepts(), csvFile.getNumbRejects(), totalTime);
            csvFile.setReadSuccess(true);

            csvFile.setProcessMessage(String.format("File load report: total:%d, accepts:%d, rejects:%d, time:%.1fs",
                    csvFile.getSize(), csvFile.getNumbAccepts(), csvFile.getNumbRejects(), totalTime / 1000.0));
        } catch (IOException e) {
            progressUpdater.updateTaskInfo("failed to read file because: " + e.getMessage());
            log.error("failed to read file because: {}", e.getMessage());
            csvFile.setReadSuccess(false);
            csvFile.setProcessMessage("failed to read file because: " + e.getMessage());
        }

        if (csvFile.isReadSuccess()) {
            csvFile.setMaxDistance(loadStats.getMaxDistance());
            csvFile.getDataSetDescriptor().setNumberStars(loadStats.getTotalCount());
            csvFile.getDataSetDescriptor().setDistanceRange(loadStats.getMaxDistance());
            progressUpdater.updateTaskInfo("load of dataset complete with " + loadStats.getTotalCount() + " stars loaded");
        }

        return csvFile;
    }

    private List<StarObject> parseBulkRecords(int numberToParse,
                                              BufferedReader reader,
                                              LoadStats loadStats) throws IOException {
        // Use ArrayList with pre-sized capacity for better performance
        List<StarObject> starList = new ArrayList<>(numberToParse);

        for (int i = 0; i < numberToParse; i++) {
            String line = reader.readLine();

            if (line == null) {
                log.debug("End of file reached at line={}", loadStats.getTotalCount());
                loadStats.setReadComplete(true);
                break;
            }

            // Use pre-compiled pattern for faster splitting
            String[] splitLine = CSV_SPLIT_PATTERN.split(line, -1);
            String[] lineRead = linePad(splitLine, splitLine.length);
            loadStats.incLoopCounter();

            try {
                AstroCSVStar star = parseAstroCSVStar(loadStats.getDataSet(), lineRead);
                StarObject starObject = star.toStarObject();

                if (starObject != null) {
                    starObject.setDataSetName(loadStats.getDataSet().getName());

                    if (starObject.getX() == 0.0 && starObject.getY() == 0.0 && starObject.getZ() == 0.0) {
                        // calculate the equatorial XYZ coordinates
                        double[] coordinates = calculateEquatorialCoordinates(
                                starObject.getRa(), starObject.getDeclination(), starObject.getDistance());
                        starObject.setX(coordinates[0]);
                        starObject.setY(coordinates[1]);
                        starObject.setZ(coordinates[2]);
                    }

                    if (loadStats.getTotalCount() < 5) {
                        log.info("CSV import sample: name={}, ra={}, dec={}, dist={}, x={}, y={}, z={}",
                                starObject.getDisplayName(), starObject.getRa(), starObject.getDeclination(),
                                starObject.getDistance(), starObject.getX(), starObject.getY(), starObject.getZ());
                    }

                    double[] galacticCoordinates = equatorialToGalactic(starObject.getRa(), starObject.getDeclination());
                    starObject.setGalacticLong(galacticCoordinates[0]);
                    starObject.setGalacticLat(galacticCoordinates[1]);

                    // Pull catalog ids and clean up
                    setCatalogIds(starObject);
                    cleanUpEntries(starObject);

                    starList.add(starObject);
                    loadStats.getCsvFile().incAccepts();

                    double distance = starObject.getDistance();
                    if (distance > loadStats.getMaxDistance()) {
                        loadStats.setMaxDistance(distance);
                    }
                } else {
                    loadStats.getCsvFile().incRejects();
                }

                loadStats.getCsvFile().incTotal();
            } catch (Exception e) {
                log.error("Failed to parse line {}: {}", loadStats.getTotalCount() + i, e.getMessage());
                loadStats.getCsvFile().incRejects();
                loadStats.getCsvFile().incTotal();
            }
        }
        return starList;
    }

    private AstroCSVStar parseAstroCSVStar(@NotNull Dataset dataset, String[] lineRead) {
        boolean hasAbsMag = lineRead.length >= 58;
        String absMagValue = hasAbsMag ? testForNull(lineRead, 56, "") : "";
        String gaiaDr3Value = hasAbsMag ? testForNull(lineRead, 57, "") : testForNull(lineRead, 56, "");
        String xValue = lineRead.length >= 61 ? testForNull(lineRead, 58, "") : "";
        String yValue = lineRead.length >= 61 ? testForNull(lineRead, 59, "") : "";
        String zValue = lineRead.length >= 61 ? testForNull(lineRead, 60, "") : "";
        String parallaxValue = lineRead.length >= 62 ? testForNull(lineRead, 61, "0") : "0";

        return AstroCSVStar
                .builder()
                // skip id lineRead[0]
                // read dataset name
                .datasetName(dataset.getName())
                .displayName(reduceSpaces(testForNull(lineRead, 2, "")))
                .commonName(testForNull(lineRead, 3, ""))
                .systemName(testForNull(lineRead, 4, ""))
                .epoch(testForNull(lineRead, 5, ""))
                .constellationName(testForNull(lineRead, 6, ""))
                .mass(parseDouble(testForNull(lineRead, 7, "0")))
                .notes(testForNull(lineRead, 8, ""))
                .source(testForNull(lineRead, 9, ""))
                .catalogIdList(testForNull(lineRead, 10, ""))
                .simbadId(testForNull(lineRead, 11, ""))
                .gaiaDR2CatId(testForNull(lineRead, 12, ""))
                .radius(testForNull(lineRead, 13, "0"))
                .ra(parseDouble(testForNull(lineRead, 14, "0")))
                .declination(parseDouble(testForNull(lineRead, 15, "0")))
                .pmra(parseDouble(testForNull(lineRead, 16, "0")))
                .pmdec(parseDouble(testForNull(lineRead, 17, "0")))
                .parallax(parseDouble(parallaxValue))
                .distance(parseDouble(testForNull(lineRead, 18, "0")))
                .radialVelocity(parseDouble(testForNull(lineRead, 19, "")))
                .spectralClass(testForNull(lineRead, 20, ""))
                .temperature(testForNull(lineRead, 21, "0"))
                .realStar(testForNull(lineRead, 22, "true"))
                .bprp(parseDouble(testForNull(lineRead, 23, "0")))
                .bpg(parseDouble(testForNull(lineRead, 24, "0")))
                .grp(parseDouble(testForNull(lineRead, 25, "0")))
                .luminosity(testForNull(lineRead, 26, ""))
                .magu(parseDouble(testForNull(lineRead, 27, "0")))
                .magb(parseDouble(testForNull(lineRead, 28, "0")))
                .magv(parseDouble(testForNull(lineRead, 29, "0")))
                .magr(parseDouble(testForNull(lineRead, 30, "0")))
                .magi(parseDouble(testForNull(lineRead, 31, "0")))
                .absoluteMagnitude(absMagValue)
                .other(testForNull(lineRead, 32, "false"))
                .anomaly(testForNull(lineRead, 33, "false"))
                .polity(testForNull(lineRead, 34, "NA"))
                .worldType(testForNull(lineRead, 35, "NA"))
                .fuelType(testForNull(lineRead, 36, "NA"))
                .portType(testForNull(lineRead, 37, "NA"))
                .populationType(testForNull(lineRead, 38, "NA"))
                .techType(testForNull(lineRead, 39, "NA"))
                .productType(testForNull(lineRead, 40, "NA"))
                .milSpaceType(testForNull(lineRead, 41, "NA"))
                .milPlanType(testForNull(lineRead, 42, "NA"))
                .age(parseDouble(testForNull(lineRead, 43, "0")))
                .metallicity(parseDouble(testForNull(lineRead, 44, "0")))
                .miscText1(testForNull(lineRead, 45, ""))
                .miscText2(testForNull(lineRead, 46, ""))
                .miscText3(testForNull(lineRead, 47, ""))
                .miscText4(testForNull(lineRead, 48, ""))
                .miscText5(testForNull(lineRead, 49, ""))
                .miscNum1(parseDouble(testForNull(lineRead, 50, "0")))
                .miscNum2(parseDouble(testForNull(lineRead, 51, "0")))
                .miscNum3(parseDouble(testForNull(lineRead, 52, "0")))
                .miscNum4(parseDouble(testForNull(lineRead, 53, "0")))
                .miscNum5(parseDouble(testForNull(lineRead, 54, "0")))
                .numExoplanets(parseInt(testForNull(lineRead, 55, "0")))
                .gaiaDR3CatId(gaiaDr3Value)
                .x(parseDouble(xValue))
                .y(parseDouble(yValue))
                .z(parseDouble(zValue))
                .build();
    }

    private String reduceSpaces(String string) {
        return string.replaceAll("\\s+", " ");
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

    private int parseInt(String string) {
        if (string.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(string);
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

    private DataSetDescriptor transformDescriptor(@NotNull Dataset dataset, String[] descriptorVals) {
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
