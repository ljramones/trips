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
                writer.write(String.join(",",
                        "Display Name",
                        "Common Name",
                        "Distance (LY)",
                        "Spectra",
                        "Radius",
                        "Mass",
                        "Luminosity",
                        "Temperature",
                        "RA",
                        "Declination",
                        "Parallax",
                        "X",
                        "Y",
                        "Z",
                        "Real",
                        "Polity",
                        "Constellation",
                        "Notes"));
                writer.newLine();
            }

            private void writeLine(BufferedWriter writer, StarObject star) throws Exception {
                // Reuse StringBuilder to avoid allocations
                lineBuilder.setLength(0);

                appendCsvCell(lineBuilder, star.getDisplayName());
                lineBuilder.append(',');
                appendCsvCell(lineBuilder, star.getCommonName());
                lineBuilder.append(',');
                appendCsvCell(lineBuilder, star.getDistance());
                lineBuilder.append(',');
                appendCsvCell(lineBuilder, star.getSpectralClass());
                lineBuilder.append(',');
                appendCsvCell(lineBuilder, star.getRadius());
                lineBuilder.append(',');
                appendCsvCell(lineBuilder, star.getMass());
                lineBuilder.append(',');
                appendCsvCell(lineBuilder, star.getLuminosity());
                lineBuilder.append(',');
                appendCsvCell(lineBuilder, star.getTemperature());
                lineBuilder.append(',');
                appendCsvCell(lineBuilder, star.getRa());
                lineBuilder.append(',');
                appendCsvCell(lineBuilder, star.getDeclination());
                lineBuilder.append(',');
                appendCsvCell(lineBuilder, star.getParallax());
                lineBuilder.append(',');
                appendCsvCell(lineBuilder, star.getX());
                lineBuilder.append(',');
                appendCsvCell(lineBuilder, star.getY());
                lineBuilder.append(',');
                appendCsvCell(lineBuilder, star.getZ());
                lineBuilder.append(',');
                appendCsvCell(lineBuilder, star.isRealStar());
                lineBuilder.append(',');
                appendCsvCell(lineBuilder, star.getPolity());
                lineBuilder.append(',');
                appendCsvCell(lineBuilder, star.getConstellationName());
                lineBuilder.append(',');
                appendCsvCell(lineBuilder, star.getNotes());

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
        };
    }
}
