package com.teamgannon.trips.service.export.tasks;

import com.teamgannon.trips.dialogs.dataset.model.ExportOptions;
import com.teamgannon.trips.dialogs.gaiadata.CatalogUtils;
import com.teamgannon.trips.jpa.model.StarObject;
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
public class CSVDataSetDataExportTask extends Task<ExportResults> implements ProgressUpdater {

    private static final int PROGRESS_UPDATE_INTERVAL = 1000;

    private final ExportOptions export;
    private final DatabaseManagementService databaseManagementService;
    private final StarService starService;

    // Reusable StringBuilder to avoid allocations per record
    private final StringBuilder csvBuilder = new StringBuilder(1024);

    public CSVDataSetDataExportTask(ExportOptions export,
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
        ExportResults result = processCSVFile(export);
        if (result.isSuccess()) {
            log.info("Dataset {} exported", export.getFileName());
        } else {
            log.error("Export failed: " + result.getMessage());
        }

        return result;
    }

    private ExportResults processCSVFile(ExportOptions export) {
        ExportResults exportResults = ExportResults.builder().success(false).build();
        String dataSetName = export.getDataset().getDataSetName();

        log.info("Starting streaming export for dataset: {}", dataSetName);
        long startTime = System.currentTimeMillis();

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(export.getFileName() + ".trips.csv"))) {

            // Write headers
            writer.write(getHeaders());

            // Use atomic counter for lambda
            AtomicLong count = new AtomicLong(0);

            // Stream and write each star within transaction - constant memory usage
            long totalProcessed = starService.processDatasetStream(dataSetName, starObject -> {
                try {
                    String csvRecord = convertToCSV(starObject);
                    writer.write(csvRecord);
                    long current = count.incrementAndGet();
                    if (current % PROGRESS_UPDATE_INTERVAL == 0) {
                        updateTaskInfo(current + " elements written so far");
                    }
                } catch (Exception e) {
                    log.error("Error writing star {}: {}", starObject.getDisplayName(), e.getMessage());
                }
            });

            writer.flush();
            long elapsed = System.currentTimeMillis() - startTime;
            log.info("Finished exporting {} stars in {} ms", totalProcessed, elapsed);
            exportResults.setSuccess(true);
            exportResults.setMessage("Exported " + totalProcessed + " stars to " + export.getFileName() + ".trips.csv");

        } catch (Exception e) {
            exportResults.setMessage("Export failed: " + e.getMessage());
            log.error("Export error: {}", e.getMessage(), e);
        }
        return exportResults;
    }


    private @NotNull String getHeaders() {
        return "id," +
                "dataSetName," +
                "displayName," +
                "commonName," +
                "System Name," +
                "Epoch," +
                "constellationName," +
                "mass," +
                "notes," +
                "source," +
                "catalogIdList," +
                "simbadId," +
                "Gaia DR2," +
                "radius," +
                "ra," +
                "declination," +
                "pmra," +
                "pmdec," +
                "distance," +
                "radialVelocity," +
                "spectralClass," +
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
                "age," +
                "metallicity," +
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
                "numExoplanets" +
                "\n";
    }

    private @NotNull String convertToCSV(@NotNull StarObject starObject) {
        // Set the Gaia DR2 id if it is not set
        if (starObject.getGaiaDR2CatId() == null || starObject.getGaiaDR2CatId().isEmpty()) {
            CatalogUtils.setGaiaDR2Id(starObject);
        }

        // Reuse StringBuilder to avoid allocations
        csvBuilder.setLength(0);

        appendField(csvBuilder, starObject.getId());
        appendField(csvBuilder, starObject.getDataSetName());
        appendField(csvBuilder, starObject.getDisplayName());
        appendField(csvBuilder, starObject.getCommonName());
        appendField(csvBuilder, starObject.getSystemName());
        appendField(csvBuilder, starObject.getEpoch());
        appendField(csvBuilder, starObject.getConstellationName());
        csvBuilder.append(starObject.getMass()).append(", ");
        appendField(csvBuilder, starObject.getNotes());
        appendField(csvBuilder, starObject.getSource());
        csvBuilder.append(String.join("~", starObject.getCatalogIdList())).append(", ");
        appendField(csvBuilder, starObject.getSimbadId());
        appendField(csvBuilder, starObject.getGaiaDR2CatId());
        csvBuilder.append(starObject.getRadius()).append(", ");
        csvBuilder.append(starObject.getRa()).append(", ");
        csvBuilder.append(starObject.getDeclination()).append(", ");
        csvBuilder.append(starObject.getPmra()).append(", ");
        csvBuilder.append(starObject.getPmdec()).append(", ");
        csvBuilder.append(starObject.getDistance()).append(", ");
        csvBuilder.append(starObject.getRadialVelocity()).append(", ");
        csvBuilder.append(starObject.getSpectralClass()).append(", ");
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
        csvBuilder.append(starObject.getAge()).append(", ");
        csvBuilder.append(starObject.getMetallicity()).append(", ");
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
        csvBuilder.append(starObject.getNumExoplanets());
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
