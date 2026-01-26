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
        return "id," +
                "dataSetName," +
                "displayName," +
                "commonName," +
                "gaiaId," +
                "simbadId," +
                "constellationName," +
                "mass," +
                "age," +
                "metallicity," +
                "source," +
                "catalogIdList," +
                "X," +
                "Y," +
                "Z," +
                "radius," +
                "ra," +
                "pmra," +
                "declination," +
                "pmdec," +
                "parallax," +
                "distance," +
                "radialVelocity," +
                "spectralClass," +
                "orthoSpectralClass," +
                "temperature," +
                "realStar," +
                "bprp," +
                "bpg," +
                "grp," +
                "luminosity," +
                "magu," +
                "magb," +
                "magv," +
                "magr," +
                "magi," +
                "other," +
                "anomaly," +
                "polity," +
                "worldType," +
                "fuelType," +
                "portType," +
                "populationType," +
                "techType," +
                "productType," +
                "milSpaceType," +
                "milPlanType," +
                "miscText1," +
                "miscText2," +
                "miscText3," +
                "miscText4," +
                "miscText5," +
                "miscNum1," +
                "miscNum2," +
                "miscNum3," +
                "miscNum4," +
                "miscNum5," +
                "Notes," +
                "L," +
                "B" +
                "\n";
    }

    private @NotNull String convertToCSV(@NotNull StarObject starObject) {
        // Reuse StringBuilder to avoid allocations
        csvBuilder.setLength(0);

        appendField(csvBuilder, starObject.getId().toString());
        appendField(csvBuilder, starObject.getDataSetName());
        appendField(csvBuilder, starObject.getDisplayName());
        appendField(csvBuilder, starObject.getCommonName());
        appendField(csvBuilder, starObject.getGaiaDR2CatId());
        appendField(csvBuilder, starObject.getSimbadId());
        appendField(csvBuilder, starObject.getConstellationName());
        csvBuilder.append(starObject.getMass()).append(", ");
        csvBuilder.append(starObject.getAge()).append(", ");
        csvBuilder.append(starObject.getMetallicity()).append(", ");
        appendField(csvBuilder, starObject.getSource());
        csvBuilder.append(String.join("~", starObject.getCatalogIdList())).append(", ");
        csvBuilder.append(starObject.getX()).append(", ");
        csvBuilder.append(starObject.getY()).append(", ");
        csvBuilder.append(starObject.getZ()).append(", ");
        csvBuilder.append(starObject.getRadius()).append(", ");
        csvBuilder.append(starObject.getRa()).append(", ");
        csvBuilder.append(starObject.getPmra()).append(", ");
        csvBuilder.append(starObject.getDeclination()).append(", ");
        csvBuilder.append(starObject.getPmdec()).append(", ");
        csvBuilder.append(starObject.getParallax()).append(", ");
        csvBuilder.append(starObject.getDistance()).append(", ");
        csvBuilder.append(starObject.getRadialVelocity()).append(", ");
        csvBuilder.append(starObject.getSpectralClass()).append(", ");
        csvBuilder.append(starObject.getOrthoSpectralClass()).append(", ");
        csvBuilder.append(starObject.getTemperature()).append(", ");
        csvBuilder.append(starObject.isRealStar()).append(", ");
        csvBuilder.append(starObject.getBprp()).append(", ");
        csvBuilder.append(starObject.getBpg()).append(", ");
        csvBuilder.append(starObject.getGrp()).append(", ");
        csvBuilder.append(starObject.getLuminosity()).append(", ");
        csvBuilder.append(starObject.getMagu()).append(", ");
        csvBuilder.append(starObject.getMagb()).append(", ");
        csvBuilder.append(starObject.getMagv()).append(", ");
        csvBuilder.append(starObject.getMagr()).append(", ");
        csvBuilder.append(starObject.getMagi()).append(", ");
        csvBuilder.append(starObject.isOther()).append(", ");
        csvBuilder.append(starObject.isAnomaly()).append(", ");
        csvBuilder.append(starObject.getPolity()).append(", ");
        csvBuilder.append(starObject.getWorldType()).append(", ");
        csvBuilder.append(starObject.getFuelType()).append(", ");
        csvBuilder.append(starObject.getPortType()).append(", ");
        csvBuilder.append(starObject.getPopulationType()).append(", ");
        csvBuilder.append(starObject.getTechType()).append(", ");
        csvBuilder.append(starObject.getProductType()).append(", ");
        csvBuilder.append(starObject.getMilSpaceType()).append(", ");
        csvBuilder.append(starObject.getMilPlanType()).append(", ");
        appendField(csvBuilder, starObject.getMiscText1());
        appendField(csvBuilder, starObject.getMiscText2());
        appendField(csvBuilder, starObject.getMiscText3());
        appendField(csvBuilder, starObject.getMiscText4());
        appendField(csvBuilder, starObject.getMiscText5());
        csvBuilder.append(starObject.getMiscNum1()).append(", ");
        csvBuilder.append(starObject.getMiscNum2()).append(", ");
        csvBuilder.append(starObject.getMiscNum3()).append(", ");
        csvBuilder.append(starObject.getMiscNum4()).append(", ");
        csvBuilder.append(starObject.getMiscNum5()).append(", ");
        appendField(csvBuilder, starObject.getNotes());
        csvBuilder.append(starObject.getGalacticLat()).append(", ");
        csvBuilder.append(starObject.getGalacticLong());
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
