package com.teamgannon.trips.service.export.tasks;

import com.teamgannon.trips.dialogs.dataset.model.ExportOptions;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.search.SearchContext;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.StarService;
import com.teamgannon.trips.service.export.ExportResults;
import com.teamgannon.trips.service.importservices.tasks.ProgressUpdater;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class CSVQueryDataExportTask extends Task<ExportResults> implements ProgressUpdater {

    private static final int PROGRESS_UPDATE_INTERVAL = 1000;

    private final ExportOptions export;
    private final SearchContext searchContext;
    private final DatabaseManagementService databaseManagementService;
    private final StarService starService;

    // Reusable StringBuilder to avoid allocations per record
    private final StringBuilder csvBuilder = new StringBuilder(1024);

    public CSVQueryDataExportTask(ExportOptions export, SearchContext searchContext,
                                  DatabaseManagementService databaseManagementService,
                                  StarService starService) {
        this.export = export;
        this.searchContext = searchContext;
        this.databaseManagementService = databaseManagementService;
        this.starService = starService;
    }


    @Override
    protected ExportResults call() throws Exception {
        ExportResults result = processCSVFile(export);
        if (result.isSuccess()) {
            log.info("Query export {} completed", export.getFileName());
        } else {
            log.error("Export failed: " + result.getMessage());
        }

        return result;
    }

    public ExportResults processCSVFile(ExportOptions export) {
        ExportResults exportResults = ExportResults.builder().success(false).build();

        log.info("Starting streaming query export");
        long startTime = System.currentTimeMillis();

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(export.getFileName() + ".trips.csv"))) {

            // Write headers
            writer.write(getHeaders());

            // Use atomic counter for lambda
            AtomicLong count = new AtomicLong(0);

            // Stream and write each star within transaction - constant memory usage
            long totalProcessed = starService.processQueryStream(searchContext.getAstroSearchQuery(), starObject -> {
                try {
                    String csvRecord = convertToCSV(starObject);
                    writer.write(csvRecord);
                    long current = count.incrementAndGet();
                    if (current % PROGRESS_UPDATE_INTERVAL == 0) {
                        updateTaskInfo(current + " records so far");
                    }
                } catch (Exception e) {
                    log.error("Error writing star {}: {}", starObject.getDisplayName(), e.getMessage());
                }
            });

            writer.flush();
            long elapsed = System.currentTimeMillis() - startTime;
            String msg = export.getDataset().getDataSetName() + " exported " + totalProcessed + " stars to " + export.getFileName() + ".trips.csv";
            log.info("Finished exporting {} stars in {} ms", totalProcessed, elapsed);

            exportResults.setSuccess(true);
            exportResults.setMessage(msg);

        } catch (Exception e) {
            log.error("Export error: {}", e.getMessage(), e);
            exportResults.setMessage(export.getDataset() + " failed to export: " + e.getMessage());
        }

        return exportResults;
    }

    @Override
    public void updateTaskInfo(String message) {
        updateMessage(message + "  ");
    }


    private @NotNull String getHeaders() {
        // Column order MUST match RegularStarCatalogCsvReader.parseAstroCSVStar() exactly!
        return "id," +                    // 0
                "dataSetName," +          // 1
                "displayName," +          // 2
                "commonName," +           // 3
                "systemName," +           // 4
                "epoch," +                // 5
                "constellationName," +    // 6
                "mass," +                 // 7
                "notes," +                // 8
                "source," +               // 9
                "catalogIdList," +        // 10
                "simbadId," +             // 11
                "gaiaDR2," +              // 12
                "radius," +               // 13
                "ra," +                   // 14
                "declination," +          // 15
                "pmra," +                 // 16
                "pmdec," +                // 17
                "distance," +             // 18
                "radialVelocity," +       // 19
                "spectralClass," +        // 20
                "temperature," +          // 21
                "realStar," +             // 22
                "bprp," +                 // 23
                "bpg," +                  // 24
                "grp," +                  // 25
                "luminosity," +           // 26
                "magu," +                 // 27
                "magb," +                 // 28
                "magv," +                 // 29
                "magr," +                 // 30
                "magi," +                 // 31
                "other," +                // 32
                "anomaly," +              // 33
                "polity," +               // 34
                "worldType," +            // 35
                "fuelType," +             // 36
                "portType," +             // 37
                "populationType," +       // 38
                "techType," +             // 39
                "productType," +          // 40
                "milSpaceType," +         // 41
                "milPlanType," +          // 42
                "age," +                  // 43
                "metallicity," +          // 44
                "miscText1," +            // 45
                "miscText2," +            // 46
                "miscText3," +            // 47
                "miscText4," +            // 48
                "miscText5," +            // 49
                "miscNum1," +             // 50
                "miscNum2," +             // 51
                "miscNum3," +             // 52
                "miscNum4," +             // 53
                "miscNum5," +             // 54
                "numExoplanets," +        // 55
                "absoluteMagnitude," +    // 56
                "gaiaDR3," +              // 57
                "x," +                    // 58
                "y," +                    // 59
                "z," +                    // 60
                "parallax" +              // 61
                "\n";
    }

    private @NotNull String convertToCSV(@NotNull StarObject starObject) {
        // Reuse StringBuilder to avoid allocations
        // Column order MUST match getHeaders() and RegularStarCatalogCsvReader.parseAstroCSVStar()!
        csvBuilder.setLength(0);

        appendField(csvBuilder, starObject.getId().toString());           // 0: id
        appendField(csvBuilder, starObject.getDataSetName());             // 1: dataSetName
        appendField(csvBuilder, starObject.getDisplayName());             // 2: displayName
        appendField(csvBuilder, starObject.getCommonName());              // 3: commonName
        appendField(csvBuilder, starObject.getSystemName());              // 4: systemName
        appendField(csvBuilder, starObject.getEpoch());                   // 5: epoch
        appendField(csvBuilder, starObject.getConstellationName());       // 6: constellationName
        csvBuilder.append(starObject.getMass()).append(", ");             // 7: mass
        appendField(csvBuilder, starObject.getNotes());                   // 8: notes
        appendField(csvBuilder, starObject.getSource());                  // 9: source
        csvBuilder.append(String.join("~", starObject.getCatalogIdList())).append(", "); // 10: catalogIdList
        appendField(csvBuilder, starObject.getSimbadId());                // 11: simbadId
        appendField(csvBuilder, starObject.getGaiaDR2CatId());            // 12: gaiaDR2
        csvBuilder.append(starObject.getRadius()).append(", ");           // 13: radius
        csvBuilder.append(starObject.getRa()).append(", ");               // 14: ra
        csvBuilder.append(starObject.getDeclination()).append(", ");      // 15: declination
        csvBuilder.append(starObject.getPmra()).append(", ");             // 16: pmra
        csvBuilder.append(starObject.getPmdec()).append(", ");            // 17: pmdec
        csvBuilder.append(starObject.getDistance()).append(", ");         // 18: distance
        csvBuilder.append(starObject.getRadialVelocity()).append(", ");   // 19: radialVelocity
        appendField(csvBuilder, starObject.getSpectralClass());           // 20: spectralClass
        csvBuilder.append(starObject.getTemperature()).append(", ");      // 21: temperature
        csvBuilder.append(starObject.isRealStar()).append(", ");          // 22: realStar
        csvBuilder.append(starObject.getBprp()).append(", ");             // 23: bprp
        csvBuilder.append(starObject.getBpg()).append(", ");              // 24: bpg
        csvBuilder.append(starObject.getGrp()).append(", ");              // 25: grp
        appendField(csvBuilder, starObject.getLuminosity());              // 26: luminosity
        csvBuilder.append(starObject.getMagu()).append(", ");             // 27: magu
        csvBuilder.append(starObject.getMagb()).append(", ");             // 28: magb
        csvBuilder.append(starObject.getMagv()).append(", ");             // 29: magv
        csvBuilder.append(starObject.getMagr()).append(", ");             // 30: magr
        csvBuilder.append(starObject.getMagi()).append(", ");             // 31: magi
        csvBuilder.append(starObject.isOther()).append(", ");             // 32: other
        csvBuilder.append(starObject.isAnomaly()).append(", ");           // 33: anomaly
        appendField(csvBuilder, starObject.getPolity());                  // 34: polity
        appendField(csvBuilder, starObject.getWorldType());               // 35: worldType
        appendField(csvBuilder, starObject.getFuelType());                // 36: fuelType
        appendField(csvBuilder, starObject.getPortType());                // 37: portType
        appendField(csvBuilder, starObject.getPopulationType());          // 38: populationType
        appendField(csvBuilder, starObject.getTechType());                // 39: techType
        appendField(csvBuilder, starObject.getProductType());             // 40: productType
        appendField(csvBuilder, starObject.getMilSpaceType());            // 41: milSpaceType
        appendField(csvBuilder, starObject.getMilPlanType());             // 42: milPlanType
        csvBuilder.append(starObject.getAge()).append(", ");              // 43: age
        csvBuilder.append(starObject.getMetallicity()).append(", ");      // 44: metallicity
        appendField(csvBuilder, starObject.getMiscText1());               // 45: miscText1
        appendField(csvBuilder, starObject.getMiscText2());               // 46: miscText2
        appendField(csvBuilder, starObject.getMiscText3());               // 47: miscText3
        appendField(csvBuilder, starObject.getMiscText4());               // 48: miscText4
        appendField(csvBuilder, starObject.getMiscText5());               // 49: miscText5
        csvBuilder.append(starObject.getMiscNum1()).append(", ");         // 50: miscNum1
        csvBuilder.append(starObject.getMiscNum2()).append(", ");         // 51: miscNum2
        csvBuilder.append(starObject.getMiscNum3()).append(", ");         // 52: miscNum3
        csvBuilder.append(starObject.getMiscNum4()).append(", ");         // 53: miscNum4
        csvBuilder.append(starObject.getMiscNum5()).append(", ");         // 54: miscNum5
        csvBuilder.append(starObject.getNumExoplanets()).append(", ");    // 55: numExoplanets
        appendField(csvBuilder, starObject.getAbsoluteMagnitude());       // 56: absoluteMagnitude
        appendField(csvBuilder, starObject.getGaiaDR3CatId());            // 57: gaiaDR3
        csvBuilder.append(starObject.getX()).append(", ");                // 58: x
        csvBuilder.append(starObject.getY()).append(", ");                // 59: y
        csvBuilder.append(starObject.getZ()).append(", ");                // 60: z
        csvBuilder.append(starObject.getParallax());                      // 61: parallax (no trailing comma)
        csvBuilder.append('\n');

        return csvBuilder.toString();
    }

    /**
     * Append a string field to the builder, replacing commas with tildes.
     */
    private void appendField(@NotNull StringBuilder sb, @Nullable String value) {
        if (value != null) {
            // Replace commas inline without creating intermediate String
            for (int i = 0; i < value.length(); i++) {
                char c = value.charAt(i);
                sb.append(c == ',' ? '~' : c);
            }
        }
        sb.append(", ");
    }


}
