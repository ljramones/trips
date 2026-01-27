package com.teamgannon.trips.tableviews;

import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.search.AstroSearchQuery;
import com.teamgannon.trips.service.StarService;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Background service for streaming export of all stars matching a query to CSV.
 * Uses true streaming to maintain constant memory usage regardless of dataset size.
 */
@Slf4j
public class StarTableExportService extends Service<Long> {

    private static final int PROGRESS_UPDATE_INTERVAL = 100;

    private final StarService starService;
    private final AstroSearchQuery query;
    private final File outputFile;

    // Reusable StringBuilder to avoid allocations per record
    private final StringBuilder lineBuilder = new StringBuilder(512);

    /**
     * Create a new export service.
     *
     * @param starService the star service for database access
     * @param query       the search query defining which stars to export
     * @param outputFile  the file to write to
     */
    public StarTableExportService(StarService starService, AstroSearchQuery query, File outputFile) {
        this.starService = starService;
        this.query = query;
        this.outputFile = outputFile;
    }

    @Override
    protected Task<Long> createTask() {
        return new Task<>() {
            @Override
            protected Long call() throws Exception {
                // Get count for progress tracking (fast COUNT query)
                long total = starService.countBySearchQuery(query);
                AtomicLong exported = new AtomicLong(0);

                updateMessage("Starting export...");
                updateProgress(0, total);

                try (BufferedWriter writer = Files.newBufferedWriter(outputFile.toPath())) {
                    // Write header
                    writeHeader(writer);

                    // Stream all matching stars with constant memory usage
                    starService.processQueryStream(query, star -> {
                        // Skip invalid records
                        if (star.getDisplayName() == null || star.getDisplayName().equalsIgnoreCase("name")) {
                            return;
                        }

                        try {
                            // Log first few records being exported
                            long count = exported.get();
                            if (count < 3) {
                                log.info("=== EXPORT RECORD {} ===", count);
                                log.info("  displayName: '{}'", star.getDisplayName());
                                log.info("  ra: {}, dec: {}", star.getRa(), star.getDeclination());
                                log.info("  distance: {}", star.getDistance());
                                log.info("  realStar: {}", star.isRealStar());
                                log.info("  x: {}, y: {}, z: {}", star.getX(), star.getY(), star.getZ());
                                log.info("  other: {}, anomaly: {}", star.isOther(), star.isAnomaly());
                            }

                            writeLine(writer, star);
                            long current = exported.incrementAndGet();
                            if (current % PROGRESS_UPDATE_INTERVAL == 0) {
                                updateProgress(current, total);
                                updateMessage(String.format("Exported %d of %d records...", current, total));
                            }
                        } catch (Exception e) {
                            log.error("Error writing star {}: {}", star.getDisplayName(), e.getMessage());
                        }
                    });

                    updateMessage(String.format("Export complete: %d records", exported.get()));
                }

                return exported.get();
            }

            private void writeHeader(BufferedWriter writer) throws Exception {
                // Write header in .trips.csv format for re-import compatibility
                writer.write("id," +
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
                        "numExoplanets," +
                        "absoluteMagnitude," +
                        "gaiaDR3CatId," +
                        "x," +
                        "y," +
                        "z," +
                        "parallax");
                writer.newLine();
            }

            private void writeLine(BufferedWriter writer, StarObject star) throws Exception {
                // Reuse StringBuilder to avoid allocations
                // Output in .trips.csv format for re-import compatibility
                lineBuilder.setLength(0);

                appendCsvCell(lineBuilder, star.getId());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getDataSetName());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getDisplayName());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getCommonName());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getSystemName());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getEpoch());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getConstellationName());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getMass());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getNotes());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getSource());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, String.join("~", star.getCatalogIdList()));
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getSimbadId());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getGaiaDR2CatId());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getRadius());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getRa());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getDeclination());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getPmra());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getPmdec());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getDistance());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getRadialVelocity());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getSpectralClass());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getTemperature());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.isRealStar());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getBprp());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getBpg());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getGrp());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getLuminosity());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getMagu());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getMagb());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getMagv());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getMagr());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getMagi());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.isOther());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.isAnomaly());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getPolity());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getWorldType());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getFuelType());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getPortType());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getPopulationType());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getTechType());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getProductType());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getMilSpaceType());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getMilPlanType());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getAge());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getMetallicity());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getMiscText1());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getMiscText2());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getMiscText3());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getMiscText4());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getMiscText5());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getMiscNum1());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getMiscNum2());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getMiscNum3());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getMiscNum4());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getMiscNum5());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getNumExoplanets());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getAbsoluteMagnitude());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getGaiaDR3CatId());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getX());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getY());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getZ());
                lineBuilder.append(", ");
                appendCsvCell(lineBuilder, star.getParallax());

                writer.write(lineBuilder.toString());
                writer.newLine();
            }

            private void appendCsvCell(StringBuilder sb, String value) {
                if (value == null) {
                    return;
                }
                boolean needsQuotes = value.indexOf(',') >= 0 || value.indexOf('"') >= 0 || value.indexOf('\n') >= 0;
                if (needsQuotes) {
                    sb.append('"');
                    for (int i = 0; i < value.length(); i++) {
                        char c = value.charAt(i);
                        if (c == '"') {
                            sb.append("\"\"");
                        } else {
                            sb.append(c);
                        }
                    }
                    sb.append('"');
                } else {
                    sb.append(value);
                }
            }

            private void appendCsvCell(StringBuilder sb, Double value) {
                if (value != null) {
                    sb.append(value);
                }
            }

            private void appendCsvCell(StringBuilder sb, Boolean value) {
                if (value != null) {
                    sb.append(value);
                }
            }

            private void appendCsvCell(StringBuilder sb, boolean value) {
                sb.append(value);
            }

            private void appendCsvCell(StringBuilder sb, double value) {
                sb.append(value);
            }

            private void appendCsvCell(StringBuilder sb, int value) {
                sb.append(value);
            }
        };
    }
}
